package com.appchina.rpc.base.cluster.pool;

import java.io.Closeable;
import java.util.NoSuchElementException;

import com.appchina.rpc.base.client.Client;
import com.appchina.rpc.base.client.ClientIOException;

/**
 * 提供获取连接的方式<br>
 * 
 * @author dongjian_9@163.com
 */
public interface Pool<P, R> extends Closeable {
	
	/**
	 * 获得连接
	 * <br>
	 * @throws ClientIOException 无法创建连接
	 * @throws NoSuchElementException 连接池耗尽
	 */
	public BorrowedClient<P, R> borrowClient() throws NoSuchElementException, ClientIOException;
	
	/**
	 * 将连接放回
	 * */
	public void returnClient(BorrowedClient<P, R> client);
	
	/**
	 * 将连接销毁
	 * */
	public void invalidateClient(BorrowedClient<P, R> client);
	
	/**
	 * 初始化Pool 
	 * @throws IllegalAccessException 
	 * */
	public void init() throws IllegalAccessException;
	
	/**
	 * 关闭Pool
	 * */
	@Override
	public void close();

	/**
	 * 被借出的一个连接
	 * */
	public static class BorrowedClient<P, R> {
		public Client<P, R> client;
		public int index;
		public BorrowedClient(Client<P, R> client, int index) {
			this.client = client;
			this.index = index;
		}
	}
	
}
