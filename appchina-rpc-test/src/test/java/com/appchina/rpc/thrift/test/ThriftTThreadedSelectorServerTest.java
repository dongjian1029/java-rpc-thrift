package com.appchina.rpc.thrift.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.appchina.rpc.base.server.ServerException;
import com.appchina.rpc.base.server.ServerProcessor;
import com.appchina.rpc.thrift.client.ThriftClient;
import com.appchina.rpc.thrift.server.ThriftTThreadedSelectorServer;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * 简单的Nio Server和Client
 * 
 * @author dongjian_9@163.com
 * */
public class ThriftTThreadedSelectorServerTest {
	
	private ThriftTThreadedSelectorServer server;
	
	@Before
	public void before() throws Exception {
		server = new ThriftTThreadedSelectorServer();
		server.setProcessor(new ServerProcessor<Request, Response>() {
			@Override
			public Response process(Request param) {
				return new Response();
			}

		});
		server.start();
	}
	
	@After
	public void after() throws InterruptedException, ServerException {
		server.stop();
	}
	
	@Test
	public void call() throws Throwable {
		ThriftClient client = new ThriftClient();
		client.setFramed(true);
		client.open();
		
		long start = System.currentTimeMillis();
		for(int i=0; i<10000; i++){
			client.send(new Request());
		}
		System.out.println("times:" + (System.currentTimeMillis() - start));;
		
		client.close();
	}

}
