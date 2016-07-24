package com.appchina.rpc.base.cluster.pool;

import java.util.concurrent.TimeUnit;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

import com.appchina.rpc.base.cluster.LoadBalance;
import com.appchina.rpc.base.cluster.RoundrobinLoadBalance;

/**
 * {@link CentralPool}的配置
 * 
 * @author dongjian_9@163.com
 * */
public abstract class ConfigurableCentralPool<P, R> {
	/**
	 * 提供工厂列表的接口
	 * */
	protected ClientFactoryProvider<P, R> factoryProvider;
	/**
	 * 负载均衡
	 * */
	protected LoadBalance loadBalance = new RoundrobinLoadBalance();
	/**
	 * 心跳频率，小于0时禁用
	 * */
	protected long heartbeat = 5000;
	/**
	 * 当前工厂无法建立连接，尝试其他工厂的次数
	 * */
	protected int retry = 3;
	/**
	 * 连接最大保持时间，小于0表示无限制
	 * */
	protected long maxKeepMillis = TimeUnit.MINUTES.toMillis(5);
	/**
	 * 连接最多发送请求数量，小于0表示无限制
	 * */
	protected long maxSendCount = -1;
	/**
	 * @see GenericObjectPool#setMaxActive
     */
	protected int maxActive = 200;
	/**
	 * @see GenericObjectPool#setMaxIdle
	 */
	protected int maxIdle = 200;
	/**
	 * @see GenericObjectPool#setMaxWait
	 */
	protected long maxWait = 3000;
	/**
	 * @see GenericObjectPool#setMinIdle
	 */
	protected int minIdle = 0;
	/**
	 * @see GenericObjectPool#setTestOnBorrow
	 */
	protected boolean testOnBorrow = false;
    /**
     * @see GenericObjectPool#setWhenExhaustedAction
     */
	protected byte whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;

	protected Config getPoolConfig() {
	  	GenericObjectPool.Config config = new GenericObjectPool.Config();
    	config.maxActive = maxActive;
    	config.maxIdle = maxIdle;
    	config.maxWait = maxWait;
    	config.minIdle = minIdle;
    	config.testOnBorrow = testOnBorrow;
    	config.whenExhaustedAction = whenExhaustedAction;
    	
    	//以下配置写死
    	config.numTestsPerEvictionRun = 0;
    	config.timeBetweenEvictionRunsMillis = -1;
    	config.minEvictableIdleTimeMillis = -1;
    	config.softMinEvictableIdleTimeMillis = -1;
    	config.testWhileIdle = false;
    	config.testOnReturn = false;
    	//保证全部轮询
    	config.lifo = false;
    	return config;
	}
	
	
	public ClientFactoryProvider<P, R> getFactoryProvider() {
		return factoryProvider;
	}
	public void setFactoryProvider(ClientFactoryProvider<P, R> factoryProvider) {
		this.factoryProvider = factoryProvider;
	}
	public LoadBalance getLoadBalance() {
		return loadBalance;
	}
	public void setLoadBalance(LoadBalance loadBalance) {
		this.loadBalance = loadBalance;
	}
	public long getHeartbeat() {
		return heartbeat;
	}
	public void setHeartbeat(long heartbeat) {
		this.heartbeat = heartbeat;
	}
	public int getRetry() {
		return retry;
	}
	public void setRetry(int retry) {
		this.retry = retry;
	}
	public long getMaxKeepMillis() {
		return maxKeepMillis;
	}
	public void setMaxKeepMillis(long maxKeepMillis) {
		this.maxKeepMillis = maxKeepMillis;
	}
	public long getMaxSendCount() {
		return maxSendCount;
	}
	public void setMaxSendCount(long maxSendCount) {
		this.maxSendCount = maxSendCount;
	}
	public int getMaxIdle() {
		return maxIdle;
	}
	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}
	public int getMinIdle() {
		return minIdle;
	}
	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}
	public int getMaxActive() {
		return maxActive;
	}
	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}
	public long getMaxWait() {
		return maxWait;
	}
	public void setMaxWait(long maxWait) {
		this.maxWait = maxWait;
	}
	public byte getWhenExhaustedAction() {
		return whenExhaustedAction;
	}
	public void setWhenExhaustedAction(byte whenExhaustedAction) {
		this.whenExhaustedAction = whenExhaustedAction;
	}
	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}
	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	@Override
	public String toString() {
		return "[loadBalance=" + loadBalance.getClass() + ", factoryProvider=" + factoryProvider.getClass()
				+ ", heartbeat=" + heartbeat  + ", retry=" + retry
				+ ", maxKeepMillis=" + maxKeepMillis + ", maxSendCount=" + maxSendCount + ", maxActive=" + maxActive
				+ ", maxIdle=" + maxIdle + ", maxWait=" + maxWait + ", minIdle=" + minIdle + ", whenExhaustedAction="
				+ whenExhaustedAction + ", testOnBorrow=" + testOnBorrow + "]";
	}
	
}
