package com.appchina.rpc.thrift.client;

/**
 * 不填充堆栈信息的异常
 * 
 * @author dongjian_9@163.com
 * */
public final class SuppressedException extends Throwable {
	private static final long serialVersionUID = 1L;

	public SuppressedException(String message) {
		super(message);
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}