package com.appchina.rpc.thrift.server;

import java.util.concurrent.TimeUnit;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import com.appchina.rpc.base.server.ServerProcessor;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * 每个Client要消耗一个服务器线程<br>
 * 
 * 延迟低，短连接时可以优先考虑
 * 
 * @author dongjian_9@163.com
 * */
public class ThriftThreadPoolServer extends AbstractThriftServer {
	
	protected int requestTimeout = (int) TimeUnit.SECONDS.toMillis(60);
	protected int minWorkerThreads = 1;
	
	public ThriftThreadPoolServer() {
		super();
	}

	public ThriftThreadPoolServer(ServerProcessor<Request, Response> processor) {
		super(processor);
	}

	@Override
	protected TServer buildThriftServer(TProcessor thriftProcessor) throws TTransportException {
		TServerTransport transport = new TServerSocket(port, clientTimeout);
		TThreadPoolServer.Args args = new TThreadPoolServer.Args(transport);
		args.processor(thriftProcessor);
		args.protocolFactory(protocolFactory);
		args.maxWorkerThreads(workerThreads);
		args.minWorkerThreads(minWorkerThreads);
		args.requestTimeout(requestTimeout);
		args.requestTimeoutUnit(TimeUnit.MILLISECONDS);
		args.stopTimeoutVal(stopTimeoutVal);
		args.stopTimeoutUnit = TimeUnit.MILLISECONDS;
		return new TThreadPoolServer(args);
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public int getMinWorkerThreads() {
		return minWorkerThreads;
	}

	public void setMinWorkerThreads(int minWorkerThreads) {
		this.minWorkerThreads = minWorkerThreads;
	}

}
