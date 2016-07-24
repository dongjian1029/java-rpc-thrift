package com.appchina.rpc.thrift.server;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TTransportException;

import com.appchina.rpc.base.server.ServerProcessor;
import com.appchina.rpc.thrift.client.ClientHelper;
import com.appchina.rpc.thrift.client.ThriftClient;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;
import com.appchina.rpc.thrift.support.ThriftServce;

/**
 * Thrift实现，用来继承，子类提供{@link TServer}创建方式<br>
 * 
 * @author dongjian_9@163.com
 */
public abstract class AbstractThriftServer extends ConfigurableThriftServer {

	protected TServer server = null;
	protected boolean running = false;
	
	public AbstractThriftServer() { }

	public AbstractThriftServer(ServerProcessor<Request, Response> processor) {
		super(processor);
	}
	
	/**
	 * {@link #stopTimeoutVal} 指定了安全时间<br>
	 * 在这段时间内，{@link ThriftServceImpl#ping()}返回值是false，表示server无法提供服务，但不影响现有请求的处理<br>
	 * 所以、这需要Client端对此支持，这段时间内不要再发送新情求<br>
	 * 
	 * @see {@link ThriftClient#ping()}
	 * */
	@Override
	protected void stopServer() {
		if (server != null) {
			try {
				if (running) {
					running = false;
					Thread.sleep(stopTimeoutVal);
				}
				server.stop();
			} catch (InterruptedException e) {
				server.stop();
				Thread.interrupted();
			}
		}
	}
	
	@Override
	protected void startServer() throws TTransportException {
		TProcessor thriftProcessor = buildThriftProcessor();
		server = buildThriftServer(thriftProcessor);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					running = true;
					server.serve();
				} finally{
					running = false;
				}
			}
		}).start();
	}

	protected TProcessor buildThriftProcessor() {
		ThriftServce.Iface service = new ThriftServceImpl();
		return new ThriftServce.Processor<ThriftServce.Iface>(service);
	}

	protected abstract TServer buildThriftServer(TProcessor thriftProcessor) throws TTransportException;

	/**
	 * 此类是与Thrift的结合点<br>
	 * 在{@link ThriftClient}中调用的{@link ThriftServce.Iface}会被Thrift转发这个类进行处理<br>
	 */
	public class ThriftServceImpl implements ThriftServce.Iface {
		
		/**
		 * 由{@link ThriftClient#send(Request)}发送的请求，会被Thrift框架转发到此方法。<br>
		 * <br>
		 * 与{@link ThriftClient}约定，这里出现了任何异常都会使用{@link ServerHelper#putError(Response, String)}将异常信息放入header<br>
		 * <br>
		 * {@link ThriftClient}需要使用{@link ClientHelper#validateError(Response)}验证。<br>
		 */
		@Override
		public Response execute(Request request) {
			try {
				if (security) {
					ServerHelper.valudateCredential(request, allowedFromTokens);
				}
				Response response = doServerProcess(request);
				return response == null ? ServerHelper.newResponse() : response;
			} catch (CredentialException e) {
				Response response = ServerHelper.newResponse();
				return ServerHelper.putError(response, "Invalid credential.");
			} catch (Throwable e) {
				if(log.isErrorEnabled()){
					log.error("Unexpected exception:" + e.getMessage(), e);
				}
				Response response = ServerHelper.newResponse();
				return ServerHelper.putError(response, new StringBuilder(e.getClass().getName()).append(": ").append(e.getMessage()).toString());
			}
		}
		
		/**
		 * 由{@link ThriftClient#ping()}发送的请求，会被Thrift框架转发到此方法。
		 * <br>
		 * @return Server的状态
		 * */
		@Override
		public boolean ping() {
			return running;
		}
	}
	
}
