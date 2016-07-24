package com.appchina.rpc.base.server;

/**
 * 处理器的标准接口，{@link GenericServer}接到的任何请求都由此类处理。
 * 
 * @author dongjian_9@163.com
 */
public interface ServerProcessor<P, R> {

	/**
	 * @param param 客户端发来的数据
	 * 
	 * @return 将要返回给客户端的数据，包括异常信息。
	 * 
	 * @throws Throwable 未知异常
	 */
	public abstract R process(P param) throws Throwable;

}