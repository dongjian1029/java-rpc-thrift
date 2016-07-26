package com.appchina.rpc.remote.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

import com.appchina.rpc.base.client.ThreadSafetyClient;
import com.appchina.rpc.base.utils.ExceptionUtils;
import com.appchina.rpc.remote.MessageConvert;
import com.appchina.rpc.remote.MessageConvert.Param;
import com.appchina.rpc.remote.PathUtils;

/**
 * 用来创建客户端代理，当代理被调用时转发到 {@link #callRemote(String, Object[])} 
 * 
 * 每个接口只被创建一个实例
 * 
 * @author dongjian_9@163.com
 * */
public class RemoteProxyFactory<T, P, R> implements FactoryBean<T> {
	private static Log log = LogFactory.getLog(RemoteProxyFactory.class);
	
	protected Class<T> proxyInterface;
	protected String serviceName;
	protected ThreadSafetyClient<P, R> client;
	protected MessageConvert<P, R> messageConvert;
	
	private T proxy;
	
	public RemoteProxyFactory() { }

	public RemoteProxyFactory(Class<T> proxyInterface, ThreadSafetyClient<P, R> client, MessageConvert<P, R> messageConvert) {
		this(proxyInterface, null, client, messageConvert);
	}

	public RemoteProxyFactory(Class<T> proxyInterface, String serviceName, ThreadSafetyClient<P, R> client, MessageConvert<P, R> messageConvert) {
		this.proxyInterface = proxyInterface;
		this.serviceName = serviceName;
		this.client = client;
		this.messageConvert = messageConvert;
	}

	public void setProxyInterface(Class<T> proxyInterface) {
		this.proxyInterface = proxyInterface;
	}

	public void setClient(ThreadSafetyClient<P, R> client) {
		this.client = client;
	}

	public void setMessageConvert(MessageConvert<P, R> messageConvert) {
		this.messageConvert = messageConvert;
	}
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized T getObject() throws Exception {
		if(proxy == null){
			if (client == null) {
				throw new IllegalArgumentException("client is null");
			}
			if (messageConvert == null) {
				throw new IllegalArgumentException("messageConvert is null");
			}
			if (proxyInterface == null) {
				throw new IllegalArgumentException("proxyInterface is null");
			}
			if (!proxyInterface.isInterface()) {
				throw new IllegalArgumentException("proxy class not a interface");
			}
			proxy = (T) Proxy.newProxyInstance(proxyInterface.getClassLoader(), new Class<?>[]{ proxyInterface }, new ProxyHandler());
			if (log.isInfoEnabled()) {
				log.info("create proxy:" + proxyInterface);
			}
		}
		return proxy;
	}
	
	protected class ProxyHandler implements InvocationHandler {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String path = PathUtils.buildServicePath(serviceName, proxyInterface, method);
			if(path != null){
				return callRemote(path, args);
			} else {
				throw new RuntimeException("The method does not support remote invoke: " + method);
			}
		}
	}
	
	protected Object callRemote(String path, Object[] args) throws Throwable {
		try {
			Param param = new Param(path, args);
			P parameter = messageConvert.buildParameters(param);
			R result = client.send(parameter);
			Throwable ex = messageConvert.readException(result);
			if(ex != null){
				throw new RemoteInvocationException(ex);
			}
			return messageConvert.readReturn(result);
		} catch (RemoteInvocationException e) {
			//保证抛出原始异常
			Throwable targetException = e.getTargetException();
			targetException.addSuppressed(new Exception());
			throw targetException;
		} catch (Throwable e) {
			//当接口被调用，而实例是个代理，代理之中跑出了接口未定义的异常时，JVM会将这种未定义的异常信息包装为超级丑的 UndeclaredThrowableException。
			//这些都是框架异常，不影响上层异常。
			throw ExceptionUtils.getRuntimeException(e);
		}
	}

	@Override
	public Class<T> getObjectType() {
		return proxyInterface;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	/**
	 * @author dongjian_9@163.com
	 */
	public static class RemoteInvocationException extends InvocationTargetException {

		private static final long serialVersionUID = 1L;

		public RemoteInvocationException(Throwable cause) {
			super(cause);
		}

		@Override
		public Throwable fillInStackTrace() {
			return this;
		}
		
	}
	
}
