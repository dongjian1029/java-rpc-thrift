package com.appchina.rpc.base.client;

/**
 * 服务端抛出了异常，不建议server端直接将异常抛出，如果出现这个异常最好处理掉。<br>
 * 
 * 推荐server端不抛出异常，而将异常包装为返回结果。<br>
 * 
 * @author dongjian_9@163.com
 * */
public class RemoteException extends Exception {

	private static final long serialVersionUID = 1L;

	public RemoteException() {
		super();
	}

	public RemoteException(String message) {
		super(message);
	}

}
