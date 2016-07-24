package com.appchina.rpc.base.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;

/**
 * 封装通用代码，用于继承。<br>
 * <br>
 * 任何子类，都需要将请求转交给{@link #doServerProcess(Object)} 进行处理
 * <br>
 * @author dongjian_9@163.com
 * */
public abstract class GenericServer<P, R> implements Server, InitializingBean, DisposableBean, Ordered {
	protected static Log log = LogFactory.getLog(GenericServer.class);
	
	protected ServerProcessor<P, R> processor;
	
	public GenericServer() { }
	
	public GenericServer(ServerProcessor<P, R> processor) {
		this.processor = processor;
	}

	public void setProcessor(ServerProcessor<P, R> processor) {
		this.processor = processor;
	}
	
	/**
	 * 将请求交给 {@link ServerProcessor}
	 * 
	 * @throws Throwable 未知异常
	 * */
	protected R doServerProcess(P param) throws Throwable {
		return processor.process(param);
	}
	
	@Override
	public final void start() throws ServerException {
		if (processor == null) {
			throw new ServerException("server processor is null");
		}
		try {
			if(log.isInfoEnabled()){
				log.info("Starting.... " + toString());
			}
			startServer();
			if(log.isInfoEnabled()){
				log.info("Started " + toString());
			}
		} catch (Exception e) {
			throw new ServerException(e);
		}
	}

	@Override
	public final void stop() {
		if(log.isInfoEnabled()){
			log.info("stoping.... " + toString());
		}
		stopServer();
		if(log.isInfoEnabled()){
			log.info("stoped " + toString());
		}
	}

	protected abstract void stopServer();
	protected abstract void startServer() throws Exception;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.start();
	}

	@Override
	public void destroy() throws Exception {
		this.stop();
	}

	@Override
	public int getOrder() {
		return Integer.MIN_VALUE;
	}
	
}
