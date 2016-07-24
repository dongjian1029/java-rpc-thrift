package com.appchina.rpc.thrift.server;

import java.util.concurrent.TimeUnit;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;

import com.appchina.rpc.base.server.ServerProcessor;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * 使用一个单独的线程来处理网络I/O，一个独立的worker线程池来处理消息
 * 
 * @author dongjian_9@163.com
 * */
public class ThriftHsHaServer extends AbstractThriftServer {
	
	protected int minWorkerThreads;

	public ThriftHsHaServer() { }

	public ThriftHsHaServer(ServerProcessor<Request, Response> processor) {
		super(processor);
	}
	
	@Override
	protected TServer buildThriftServer(TProcessor thriftProcessor) throws TTransportException {
		TNonblockingServerTransport transport = new TNonblockingServerSocket(port, clientTimeout);
		THsHaServer.Args args = new THsHaServer.Args(transport);
		args.processor(thriftProcessor);
		args.transportFactory(new TFramedTransport.Factory());
		args.protocolFactory(protocolFactory);
		args.minWorkerThreads(minWorkerThreads);
		args.maxWorkerThreads(workerThreads);
		args.stopTimeoutVal(stopTimeoutVal);
		args.stopTimeoutUnit(TimeUnit.MILLISECONDS);
		return new THsHaServer(args);
	}
	
	public int getMinWorkerThreads() {
		return minWorkerThreads;
	}
	
	public void setMinWorkerThreads(int minWorkerThreads) {
		this.minWorkerThreads = minWorkerThreads;
	}
	
}
