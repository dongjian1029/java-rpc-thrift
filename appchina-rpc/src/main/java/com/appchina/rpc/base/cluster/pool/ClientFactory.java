package com.appchina.rpc.base.cluster.pool;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;

import com.appchina.rpc.base.client.Client;
import com.appchina.rpc.base.client.ClientIOException;
import com.appchina.rpc.base.utils.CloseUtils;

/**
 * 抽象接口，负责为对象池提供对象。
 * 
 * @see PoolableObjectFactory
 * 
 * @author dongjian_9@163.com
 * */
public abstract class ClientFactory<P, R> extends BasePoolableObjectFactory<Client<P, R>> {
	public ClientFactory() { }
	
	/**
	 * 创建并且打开{@link Client} 
	 * 
	 * @throws ClientIOException 无法建立连接时抛出
	 * 
	 * @see {@link Client#open()}
	 * */
	@Override
	public Client<P, R> makeObject() throws ClientIOException {
		Client<P, R> client = createClient();
		client.open();
		return client;
	}

	/**
	 * 用于提供Client，不需要open，只要创建即可
	 * */
	protected abstract Client<P, R> createClient();

	@Override
	public void destroyObject(Client<P, R> client) {
		CloseUtils.close(client);
	}

	@Override
	public boolean validateObject(Client<P, R> obj) {
		if (obj == null) {
			return false;
		}
		return obj.ping();
	}

}
