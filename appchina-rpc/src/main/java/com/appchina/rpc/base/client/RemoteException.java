package com.appchina.rpc.base.client;

/**
 * 服务端发生了异常
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
