package com.appchina.rpc.base.client;

/**
 * 线程安全的客户端接口
 * 
 * @author dongjian_9@163.com
 */
public interface ThreadSafetyClient<P, R> {

	/**
	 * 发送请求
	 * 
	 * @throws ClientIOException
	 *             网络异常
	 * @throws RemoteException 服务器发生了异常
	 */
	public R send(P param) throws ClientIOException, RemoteException;

}
