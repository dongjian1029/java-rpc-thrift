package com.appchina.rpc.thrift.test;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.appchina.rpc.base.client.Client;
import com.appchina.rpc.base.server.ServerException;
import com.appchina.rpc.base.server.ServerProcessor;
import com.appchina.rpc.thrift.cluster.ThriftClientFactory;
import com.appchina.rpc.thrift.server.ThriftThreadPoolServer;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * 测试工厂，以及线程池
 * 
 * @author dongjian_9@163.com
 * */
public class ClientFactoryTest {

	private ThriftThreadPoolServer server;

	@Before
	public void before() throws Exception {
		server = new ThriftThreadPoolServer(new ServerProcessor<Request, Response>() {
			@Override
			public Response process(Request param) {
				return new Response();
			}

		});
		server.setProtocolFactory(new TBinaryProtocol.Factory());
		server.start();
	}
	
	
	@After
	public void after() throws ServerException {
		server.stop();
	}
	
	@Test
	public void call() throws Throwable {
		ThriftClientFactory factory = new ThriftClientFactory();
		factory.setProtocolFactory(new TBinaryProtocol.Factory());
		
		GenericObjectPool<Client<Request, Response>> pool = new GenericObjectPool<Client<Request, Response>>(factory);
		pool.setTestOnBorrow(true);
		
		long start = System.currentTimeMillis();
		for(int i=0; i<10000; i++){
			Client<Request, Response> client = pool.borrowObject();
			client.send(new Request());
			pool.returnObject(client);
		}
		System.out.println("times:" + (System.currentTimeMillis() - start));;
		
		pool.close();
	}

}
