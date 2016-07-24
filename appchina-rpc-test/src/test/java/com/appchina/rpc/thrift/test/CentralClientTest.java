package com.appchina.rpc.thrift.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.appchina.rpc.base.cluster.client.CentralClient;
import com.appchina.rpc.base.cluster.pool.ClientFactory;
import com.appchina.rpc.base.cluster.pool.ClientFactoryProvider;
import com.appchina.rpc.base.server.ServerException;
import com.appchina.rpc.base.server.ServerProcessor;
import com.appchina.rpc.thrift.cluster.ThriftClientFactory;
import com.appchina.rpc.thrift.server.ThriftThreadPoolServer;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;


/**
 * ClusterClient 封装了多个线程池，支持负载均衡
 * 
 * @author dongjian_9@163.com
 * */
public class CentralClientTest {

	private ThriftThreadPoolServer server2;
	private ThriftThreadPoolServer server3;
	private ThriftThreadPoolServer server4;
	private CentralClient<Request, Response> client;

	@Before
	public void before() throws Exception {
		server2 = new ThriftThreadPoolServer();
		server2.setProcessor(new ServerProcessor<Request, Response>() {
			@Override
			public Response process(Request param) {
				System.out.println("22222222");
				return new Response();
			}

		});
		server2.setPort(7912);
		server2.start();
		
		server3 = new ThriftThreadPoolServer();
		server3.setProcessor(new ServerProcessor<Request, Response>() {
			@Override
			public Response process(Request param) {
				System.out.println("3333333333333333333");
				return new Response();
			}

		});
		server3.setPort(7913);
		server3.start();
		
		server4 = new ThriftThreadPoolServer();
		server4.setProcessor(new ServerProcessor<Request, Response>() {
			@Override
			public Response process(Request param) {
				System.out.println("4444444444444444444444444444444");
				return new Response();
			}

		});
		server4.setPort(7914);
		server4.start();
		
		ClientFactoryProvider<Request, Response> factoryProvider = new ClientFactoryProvider<Request, Response>() {
			@Override
			public List<ClientFactory<Request, Response>> getFactories() {
				ThriftClientFactory fac2 = new ThriftClientFactory();
				fac2.setPort(7912);
				ThriftClientFactory fac3 = new ThriftClientFactory();
				fac3.setPort(7913);
				ThriftClientFactory fac4 = new ThriftClientFactory();
				fac4.setPort(7914);
				List<ClientFactory<Request, Response>> facs = new ArrayList<ClientFactory<Request, Response>>();
				facs.add(fac2);
				facs.add(fac3);
				facs.add(fac4);
				return facs;
			}
		};
		client = new CentralClient<Request, Response>();
		client.setFactoryProvider(factoryProvider);
		client.setHeartbeat(1000);
		client.init();
	}
	
	@After
	public void after() throws InterruptedException, ServerException {
		client.close();
		server2.stop();
		server3.stop();
		server4.stop();
	}
	
	@Test
	public void call() throws Throwable {
		client.setMaxSendCount(-1);
		long start = System.currentTimeMillis();
		for(int i=0; i<10000; i++){
			client.send(new Request());
		}
		System.out.println("times:" + (System.currentTimeMillis() - start));;
		Thread.sleep(3000);
		
		client.setMaxSendCount(3);
		
		for(int i=0; i<3; i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					for(int i=0; i<40; i++){
						try {
							client.send(new Request());
							Thread.sleep(300);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
		
		Thread.sleep(2000);
		server2.stop();
		server3.stop();
		
		server2.start();
		Thread.sleep(2000);
		server3.start();
		
		Thread.sleep(4000);
	}

}
