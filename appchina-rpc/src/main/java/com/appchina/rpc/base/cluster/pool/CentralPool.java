package com.appchina.rpc.base.cluster.pool;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.appchina.rpc.base.client.Client;
import com.appchina.rpc.base.client.ClientIOException;
import com.appchina.rpc.base.utils.ExceptionUtils;
import com.appchina.rpc.base.utils.PoolUtils;

/**
 * 1、使用commons-pool实现，当集群数量较多，使用此类非常合适，在性能和均衡上做了折中的选择，waxIde决定了长连接的数量。<BR>
 * <br>
 * 2、所有工厂创建的连接被放在一个连接池，然而创建连接时使用了负载均衡，通过连接的保持时间和使用次数，让其频繁的创建和销毁连接，从而实现均衡以及长连接缓存。<br>
 * <br>
 * 3、在{@link #borrowClient()}时，会检查当前时间与上次测试（ping）的时间差值，如果大于心跳时间进行测试操作，失败设置位置无效，并且重试下一个。<br>
 * <br>
 * 4、由于连接全部放在同一个池子中，即使位置被设置无效，但其已建立的连接却被连接池保存了下来，所以在{@link #borrowClient()}时，检测创建者位置状态<br>
 * <br>
 * 5、由于集群数量可能很多，所以不进行心跳检查，只进行无效位置的测试，以便放回。<br>
 * <br>
 * 6、由于第2条原因，因此连接池取消了空闲对象扫描，内部使用lifo=false的策略<br>
 * <br>
 * 7、适用场景：集群数量处于0.3*maxIdle ~ 3*maxIdle之间此类最为合适<br>
 * <br>
 * @author dongjian_9@163.com
 */
public class CentralPool<P, R> extends ConfigurableCentralPool<P, R> implements Pool<P, R>, InitializingBean, DisposableBean  {
	
	protected static Log log = LogFactory.getLog(CentralPool.class);
	
	protected ClientFactory<P, R>[] factories;
	protected long[] lastPingTimestamps;
	protected Object[] lastPingLocks;
	protected GenericObjectPool<BorrowedClient<P, R>> pool;
	protected ScheduledExecutorService executorService;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init() throws IllegalAccessException {
		if (factoryProvider == null) {
			throw new IllegalArgumentException("factoryProvider is null");
		}
		//获取所有工厂
		List<ClientFactory<P, R>> factoryList = factoryProvider.getFactories();
		if(factoryList == null || factoryList.isEmpty()){
			throw new IllegalArgumentException("factories is null or empty");
		}
		
		//转换为数组使用
		factories = new ClientFactory[factoryList.size()];
		for(int index=0; index<factories.length; index++){
			factories[index] = factoryList.get(index);
		}
		
		//初始化所有位置最后一次ping的时间（以此实现心跳功能）
		lastPingTimestamps = new long[factories.length];
		Arrays.fill(lastPingTimestamps, 0);
		lastPingLocks = new Object[factories.length];
		for(int i=0; i<lastPingLocks.length; i++){
			lastPingLocks[i] = new Object();
		}
		
		//所有工厂创建的连接被放在一个连接池，使用负载均衡选择工厂（推荐轮询）
		pool = new GenericObjectPool<BorrowedClient<P, R>>(new LoadBalanceClientFactory(), getPoolConfig());
		
		//初始化负载均衡
		loadBalance.allocat(factories.length);
		
		//启用心跳线程，保证连接池的及时恢复
		if (heartbeat > 0 && factories.length > 1) {
			executorService = Executors.newScheduledThreadPool(1);
			executorService.scheduleWithFixedDelay(new HeartbeatCommand(), heartbeat, heartbeat, TimeUnit.MILLISECONDS);
		}
		
		if (log.isInfoEnabled()) {
			log.info("Create CentralPool pool:" + this.toString());
		}
	}
	
	@Override
	public void close() {
		if (pool != null) {
			PoolUtils.close(pool);
		}
		if (executorService != null) {
			executorService.shutdownNow();
		}
		if (log.isInfoEnabled()) {
			log.info("Close CentralPool:" + pool);
		}
	}
	
	@Override
	public BorrowedClient<P, R> borrowClient() throws NoSuchElementException, ClientIOException {
		return borrowClientAndRetry(retry);
	}

	//获取连接，如果心跳测试失败，或者出现ClientIOException都重试下一个
	protected BorrowedClient<P, R> borrowClientAndRetry(int retry) throws NoSuchElementException, ClientIOException {
		BorrowedClient<P, R> client = null;
		try {
			client = borrowClientAndExclude();
			//检查当前时间与上次测试（ping）的时间差值，如果大于心跳时间限制，则进行测试操作，失败设置位置无效，并且重试下一个
			if(validateClient(client)){
				return client;
			} else {
				PoolUtils.invalidateObject(pool, client);
				if (log.isWarnEnabled()) {
					log.warn("Index invalid, ping=false, index=" + client.index);
				}
				if(retry > 0){
					return borrowClientAndRetry(--retry);
				}else{
					throw new NoSuchElementException("Can not borrow client from CentralPool.");
				}
			}
		} catch (ClientIOException e) {
			//连接池不够用，在创建连接时可能抛出，应该重试， LoadBalanceClientFactory已将无法创建连接的具体位置设置为无效
			PoolUtils.invalidateObject(pool, client);
			if(retry > 0){
				return borrowClientAndRetry(--retry);
			}else{
				throw new NoSuchElementException("Can not borrow client from CentralPool.");
			}
		} catch (Throwable e) {
			//不对NoSuchElementException进行处理
			PoolUtils.invalidateObject(pool, client);
			throw ExceptionUtils.getRuntimeException(e);
		}
	}
	
	//检查当前时间与上次测试（ping）的时间差值，如果大于心跳时间限制，则进行测试操作
	protected boolean validateClient(BorrowedClient<P, R> client){
		if(testOnBorrow || heartbeat <= 0 || factories.length <= 1){
			return true;
		}
		int index = client.index;
		if(System.currentTimeMillis() - lastPingTimestamps[index] < heartbeat){
			return true;
		}
		synchronized (lastPingLocks[index]) {
			if(System.currentTimeMillis() - lastPingTimestamps[index] < heartbeat){
				return loadBalance.isAvailable(index);
			}
			lastPingTimestamps[index] = System.currentTimeMillis();
			if(client.client.ping()){
				return true;
			}else{
				loadBalance.invalid(client.index);
				return false;
			}
		}
	}

	//由于连接全部放在同一个池子中，即使位置被弹出，但其已建立的连接却被连接池保存了下来，因此需要排除掉。
	//如果连接使用次数过多，或者连接保持时间过久，也需要排除。
	//因此，这个方法将需要排除的进行销毁并且再次获取。无限循环，直到获取成功或者池子耗尽抛出异常。
	protected BorrowedClient<P, R> borrowClientAndExclude() throws ClientIOException, NoSuchElementException, Throwable {
		BorrowedClient<P, R> client = null;
		try {
			client = pool.borrowObject();
			if(shouldInvalidate(client.client) || !loadBalance.isAvailable(client.index)){
				PoolUtils.invalidateObject(pool, client);
				return borrowClientAndExclude();
			} else {
				return client;
			}
		} catch (Throwable e) {
 			//ClientIOException, NoSuchElementException
			PoolUtils.invalidateObject(pool, client);
			throw e;
		} 
	}
	
	public boolean shouldInvalidate(Client<P, R> client){
		if(maxSendCount > 0 && client.sendCount() >= maxSendCount){
			//连接使用次数过多，进入销毁
			return true;
		}else if(maxKeepMillis > 0 && (System.currentTimeMillis() - client.openTimestamp()) >= maxKeepMillis){
			//连接使用时间过久，进入销毁
			return true;
		}else if(client.closed()){
			//防止将销毁的对象放回来
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public void returnClient(BorrowedClient<P, R> client) {
		if (client != null) {
			if(shouldInvalidate(client.client)){
				//连接使用次数过多，进入销毁
				PoolUtils.invalidateObject(pool, client);
			} else {
				PoolUtils.returnObject(pool, client);
			}
		}
	}

	@Override
	public void invalidateClient(BorrowedClient<P, R> client) {
		if (client != null) {
			PoolUtils.invalidateObject(pool, client);
		}
	}
	
	/**
	 * 此工厂使用负载均衡来选择一个具体的Factory创建连接，创建失败则抛出ClientIOException
	 */
	private class LoadBalanceClientFactory extends BasePoolableObjectFactory<BorrowedClient<P, R>> {
		@Override
		public BorrowedClient<P, R> makeObject() throws ClientIOException {
			int index = loadBalance.nextIndex();
			ClientFactory<P, R> factory = factories[index];
			Client<P, R> client = null;
			try {
				client = factory.makeObject();
				BorrowedClient<P, R> result = new BorrowedClient<P, R>(client, index);
				if(validateClient(result)){
					return result; 
				} else {
					factory.destroyObject(client);
					throw new ClientIOException("Index invalid, ping=false, index=" + index);
				}
			} catch (ClientIOException e) {
				//这里只有ClientIOException
				factory.destroyObject(client);
				loadBalance.invalid(index);
				if (log.isWarnEnabled()) {
					log.warn("Index invalid, can not open client, index=" + index);
				}
				throw e;
			} catch (Throwable e) {
				factory.destroyObject(client);
				throw e;
			}
		}

		@Override
		public void destroyObject(BorrowedClient<P, R> obj) throws Exception {
			if(obj != null){
				factories[obj.index].destroyObject(obj.client);
			}
		}
	
		@Override
		public boolean validateObject(BorrowedClient<P, R> obj) {
			if(obj != null){
				return factories[obj.index].validateObject(obj.client);
			}
			return false;
		}
	}
	

	/**
	 * 定时触发，将有效池放回，将无效池弹出。 <br>
	 */
	protected class HeartbeatCommand implements Runnable {
		@Override
		public void run() {
			int[] allInvalids = loadBalance.getAllInvalid();
			for (int i = 0; i < allInvalids.length; i++) {
				try {
					testIndex(allInvalids[i]);
				} catch (Exception e) {
					if (log.isWarnEnabled()) {
						log.warn("Heartbeat: " + e.getMessage());
					}
				}
			}
		}

		private void testIndex(int index) throws ClientIOException {
			ClientFactory<P, R> factory = factories[index];
			Client<P, R> client = null;
			try {
				client = factory.makeObject();
				if (client.ping()) {
					// 测试通过认为有效
					loadBalance.available(index);
				}
			} finally {
				factory.destroyObject(client);
			}		
		}
	}
	
	@Override
	public void destroy() throws Exception {
		this.close();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.init();
	}
	
}



