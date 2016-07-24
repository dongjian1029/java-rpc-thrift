package com.appchina.rpc.thrift.server;

/**
 * 不填充堆栈信息的异常
 * 
 * @author dongjian_9@163.com
 * */
public final class CredentialException extends Throwable {
	private static final long serialVersionUID = 1L;
	
	public CredentialException() {}
	
	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}