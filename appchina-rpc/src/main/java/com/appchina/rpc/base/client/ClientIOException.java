package com.appchina.rpc.base.client;

/**
 * 表示网络异常
 * 
 * @author dongjian_9@163.com
 * */
public class ClientIOException extends Exception {

	private static final long serialVersionUID = 1L;

	public ClientIOException() {
		super();
	}

	public ClientIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ClientIOException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientIOException(String message) {
		super(message);
	}

	public ClientIOException(Throwable cause) {
		super(cause);
	}

	
}
