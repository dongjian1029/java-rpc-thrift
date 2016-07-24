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
	 * @return 调用{@link #open()}的时间戳
	 */
	public long openTimestamp();

	/**
	 * 测试可用性
	 * 
	 * @return true表示可用，false表示不可用
	 */
	public boolean ping();

	/**
	 * 发送请求，并获得返回值。
	 * 
	 * @throws ClientIOException
	 *             网络问题
	 * @throws RemoteException
	 *             远程服务器发生了异常
	 */
	public R send(P param) throws ClientIOException, RemoteException;

	/**
	 * 调用{@link #send(Object)} 方法的次数
	 * 
	 * @return 从连接被打开，到当前时间调用send方法的次数
	 */
	public long sendCount();
	
	/**
	 * 关闭连接
	 */
	@Override
	public void close();
	
	/**
	 * 是否调用过close方法
	 */
	public boolean closed();
	
	
}
