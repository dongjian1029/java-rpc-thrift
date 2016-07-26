package com.appchina.rpc.base.server;

/**
 * 服务端的标准接口
 * 
 * @author dongjian_9@163.com
 * */
public interface Server {
	
	public void start() throws ServerException;

	public void stop();
}
