package com.appchina.rpc.base.utils;

/**
 * @author dongjian_9@163.com
 */
public class ExceptionUtils {
	
	public static RuntimeException getRuntimeException(Throwable e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}

}
