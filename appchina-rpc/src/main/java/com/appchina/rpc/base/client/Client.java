package com.appchina.rpc.base.client;

import java.io.Closeable;

/**
 * 客户端标准接口
 * 
 * @author dongjian_9@163.com
 */
public interface Client<P, R> extends Closeable {
	
	/**
	 * 打开连接
	 * 
	 * @throws ClientIOException
	 *             无法建立连接
	 */
	public void open() throws ClientIOException;

	/**
	 * @return 打开连接成功时，记录的时间戳。
	 */
	public long openTimestamp();

	/**
	 * 测试可用性
	 * 
	 * @return true表示可用，false表示不可用。
	 */
	public boolean ping();

	/**
	 * 发送请求数据到服务端，并获得服务端返回数据。
	 * 
	 * @throws ClientIOException
	 *             网络异常
	 * @throws RemoteException
	 *             远程服务器抛出了异常
	 */
	public R send(P param) throws ClientIOException, RemoteException;

	/**
	 * 调用{@link #send(Object)}发送请求的次数
	 * 
	 * @return 从连接被打开，到当前时间调用send方法的次数。
	 */
	public long sendCount();
	
	/**
	 * 关闭连接
	 */
	@Override
	public void close();
	
	/**
	 * 连接是否已关闭
	 */
	public boolean closed();
	
	
}
