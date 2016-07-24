package com.appchina.rpc.thrift.server;

import java.util.concurrent.TimeUnit;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;

import com.appchina.rpc.base.server.ServerProcessor;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * 维护了两个线程池，一个用来处理网络I/O，另一个用来进行请求的处理。<br>
 * 
 * 当网络I/O是瓶颈的时候，TThreadedSelectorServer比THsHaServer的表现要好。
 * 
 * @author dongjian_9@163.com
 * */
public class ThriftTThreadedSelectorServer extends AbstractThriftServer {
	
	protected int selectorThreads = 2;
	
	public ThriftTThreadedSelectorServer() {
		super();
	}

	public ThriftTThreadedSelectorServer(ServerProcessor<Request, Response> processor) {
		super(processor);
	}

	@Override
	protected TServer buildThriftServer(TProcessor thriftProcessor) throws TTransportException {
		TNonblockingServerTransport transport = new TNonblockingServerSocket(port, clientTimeout);
		TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(transport);
		args.processor(thriftProcessor);
		args.transportFactory(new TFramedTransport.Factory());
		args.protocolFactory(protocolFactory);
		args.workerThreads(workerThreads);
		args.selectorThreads(selectorThreads);
		args.stopTimeoutVal(stopTimeoutVal);
		args.stopTimeoutUnit(TimeUnit.MILLISECONDS);
		TThreadedSelectorServer server = new TThreadedSelectorServer(args);
		return server;
	}

	public int getSelectorThreads() {
		return selectorThreads;
	}

	public void setSelectorThreads(int selectorThreads) {
		this.selectorThreads = selectorThreads;
	}

}
