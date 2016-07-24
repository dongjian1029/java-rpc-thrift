package com.appchina.rpc.base.cluster.client;

import com.appchina.rpc.base.client.ClientIOException;
import com.appchina.rpc.base.client.RemoteException;
import com.appchina.rpc.base.client.ThreadSafetyClient;
import com.appchina.rpc.base.cluster.pool.CentralPool;

/**
 * 实现了{@link ThreadSafetyClient}，表示线程安全的<br>
 * 
 * @see CentralPool
 * 
 * @author dongjian_9@163.com
 */
public class CentralClient<P, R> extends CentralPool<P, R> implements ThreadSafetyClient<P, R> {
	
	public CentralClient() {}
	
	@Override
	public R send(P param) throws ClientIOException, RemoteException {
		BorrowedClient<P, R> client = null;
		try {
			client = this.borrowClient();
			return client.client.send(param);
		} catch (ClientIOException e) {
			this.invalidateClient(client);
			throw e;
		} finally {
			this.returnClient(client);
		}
	}
	
}
