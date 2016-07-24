package com.appchina.rpc.base.cluster.pool;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.appchina.rpc.base.client.Client;
import com.appchina.rpc.base.client.ClientIOException;
import com.appchina.rpc.base.utils.ExceptionUtils;
import com.appchina.rpc.base.utils.PoolUtils;

/**
 * 1、使用commons-pool实现，当集群数量较少，使用此类非常合适，优点是高性能，为每个工厂维护一个连接池，使用负载均衡从不同的连接池取得连接。<br>
 * <br>
 * 2、缺点是浪费连接，即使轮询的方式很均匀，然而连接被借走的时间并不均匀，所以需要为每个池子多设置一些连接，waxIde决定了长连接的数量，maxActive可以设置大一些<br>
 * <br>
 * 3、内置心跳机制，保证连接池的及时弹出和恢复，只检测池内一个实例，建议心跳间隔短一些，心跳并不会影响性能，两次心跳的间隔是容错的空档。<br>
 * <br>
 * 4、内置了重试机制，如果连接池耗尽，在maxWait时限内无法取得连接，可以重试下一个池，当然这会导致整体wait提高，因此可以设置maxWait短一些，甚至取消maxWait<br>
 * <br>
 * 5、心跳机制无法保证每次取出的连接均可用，如果有这种需求，可以启用testOnBorrow，但是会影响性能，增加请求的延迟。<br>
 * <br>
 * 6、由于集群实例数量可能较多，所以禁用了空闲对象扫描线程，以便节省性能，可以通过连接维持时间或者连接使用次数来实现连接的释放，内部使用lifo=false的策略<br>
 * <br>
 * 7、适用场景：集群数量小于 0.1 * maxIdle<br>
 * <br>
 * @author dongjian_9@163.com
 */
public class DistributePool<P, R> extends ConfigurableDistributePool<P, R> implements Pool<P, R>, InitializingBean, DisposableBean {
	protected static Log log = LogFactory.getLog(DistributePool.class);
	
	protected GenericObjectPool<Client<P, R>>[] pools;
	protected ScheduledExecutorService executorService;

	@SuppressWarnings("unchecked")
	@Override
	public void init() throws IllegalAccessException {
		if (factoryProvider == null) {
			throw new IllegalArgumentException("factoryProvider is null");
		}
		//获取所有工厂
		List<ClientFactory<P, R>> factories = factoryProvider.getFactories();
		if(factories == null || factories.isEmpty()){
			throw new IllegalArgumentException("factories is null or empty");
		}
		
		//为每个工厂维护一个连接池
		pools = new GenericObjectPool[factories.size()];
		//禁用了空闲对象扫描线程，以便节省性能，可以通过连接维持时间或者连接使用次数来实现连接的释放。使用lifo=false的策略
		GenericObjectPool.Config config = this.getPoolConfig();
		for (int index = 0; index < pools.length; index++) {
			pools[index] = new GenericObjectPool<Client<P, R>>(factories.get(index), config);
			if (log.isInfoEnabled()) {
				log.info("Create pool:" + this.toString());
			}
		}
		
		//初始化负载均衡器
		loadBalance.allocat(pools.length);
		
		//启用心跳线程，保证连接池的及时弹出和恢复
		if (heartbeat > 0 && pools.length > 1) {
			executorService = Executors.newScheduledThreadPool(pools.length > maxHeartbeatThread ? maxHeartbeatThread : pools.length);
			for (int index = 0; index < pools.length; index++) {
				executorService.scheduleWithFixedDelay(new HeartbeatCommand(index), heartbeat, heartbeat, TimeUnit.MILLISECONDS);
			}
		}
		
		if (log.isInfoEnabled()) {
			log.info("Create DistributePool pool.");
		}
	}

	@Override
	public void close() {
		for (GenericObjectPool<Client<P, R>> pool : pools) {
			if (log.isInfoEnabled()) {
				log.info("Close pool:" + this.toString());
			}
			PoolUtils.close(pool);
		}
		if (executorService != null) {
			executorService.shutdownNow();
		}
		if (log.isInfoEnabled()) {
			log.info("Close DistributePool pool.");
		}
	}

	@Override
	public BorrowedClient<P, R> borrowClient() throws NoSuchElementException, ClientIOException {
		return borrowClientAndRetry(retry);
	}

	//有重试机制，如果连接池耗尽，在maxWait时限内无法取得连接，重试下一个池
	protected BorrowedClient<P, R> borrowClientAndRetry(int retry) throws ClientIOException, NoSuchElementException {
		//使用负载均衡从不同的连接池取得连接。
		int index = loadBalance.nextIndex();
		GenericObjectPool<Client<P, R>> pool = pools[index];
		Client<P, R> client = null;
		try {
			client = borrowClientAndExclude(pool);
			return new BorrowedClient<P, R>(client, index);
		} catch (ClientIOException | NoSuchElementException e) {
			//ClientIOException  when {@link ClientFactory#makeObject()} throws an {@link ClientIOException}.
			//NoSuchElementException 连接池耗尽，无法提供连接，或者maxWait等待超时。
			PoolUtils.invalidateObject(pool, client);
			if(retry > 0){
				return borrowClientAndRetry(--retry);
			} else {
				throw e;
			}
		} catch (Throwable e) {
			//根本不会有这种异常
			PoolUtils.invalidateObject(pool, client);
			throw ExceptionUtils.getRuntimeException(e);
		}
	}
	
	//从pool获取连接，如果连接使用次数过多，或者连接保持时间过久，直接销毁并且再次获取。无限循环，直到获取成功或者池子耗尽抛出异常。
	protected Client<P, R> borrowClientAndExclude(GenericObjectPool<Client<P, R>> pool) throws Exception {
		Client<P, R> client = null;
		try {
			client = pool.borrowObject();
			if(shouldInvalidate(client)){
				PoolUtils.invalidateObject(pool, client);
				return borrowClientAndExclude(pool);
			} else {
				return client;
			}
		} catch (Throwable e) {
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
	
	/**
	 * 返回的连接根据其使用次数，以及使用时间，判断是否将其销毁。
	 * **/
	@Override
	public void returnClient(BorrowedClient<P, R> client) {
		if (client != null) {
			if(shouldInvalidate(client.client)){
				PoolUtils.invalidateObject(pools[client.index], client.client);
			}else{
				PoolUtils.returnObject(pools[client.index], client.client);
			}
		}
	}

	@Override
	public void invalidateClient(BorrowedClient<P, R> client) {
		if (client != null) {
			PoolUtils.invalidateObject(pools[client.index], client.client);
		}
	}

	/**
	 * 定时触发，将有效池放回，将无效池弹出。 <br>
	 * 
	 * 认为同一个池内的连接状态是相同的，一个不可用，全部不可用，一个可用，全部可用 <br>
	 * <br>
	 * 这个方法可以高频率运行，保证了无效的池子及时弹出。 保证有效的池子及时恢复。
	 */
	protected class HeartbeatCommand implements Runnable {
		protected int index;

		protected HeartbeatCommand(int index) {
			this.index = index;
		}
		
		@Override
		public void run() {
			Client<P, R> client = null;
			GenericObjectPool<Client<P, R>> pool = pools[index];
			try {
				client = pool.borrowObject();
				if (client.ping()) {
					// 测试通过认为有效
					PoolUtils.returnObject(pool, client);
					loadBalance.available(index);
				} else { 
					// 测试不通过，认为无效
					PoolUtils.invalidateObject(pool, client);
					loadBalance.invalid(index);
					pools[index].clear();
					if (log.isWarnEnabled()) {
						log.warn("Index invalid, ping=false, index=" + index);
					}
				}
			} catch (ClientIOException e) {
				// when ClientFactory.makeObject() throws an ClientIOException.
				PoolUtils.invalidateObject(pool, client);
				loadBalance.invalid(index);
				pools[index].clear();
				if (log.isWarnEnabled()) {
					log.warn("Index invalid, can not open client, index=" + index);
				}
			} catch (Throwable e) {
				// NoSuchElementException - 连接池耗尽，无法提供连接，或者maxWait等待超时。
				PoolUtils.returnObject(pool, client);
				if (log.isWarnEnabled()) {
					log.warn("Heartbeat error, index=" + index + ", " + e.getMessage());
				}
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
