package com.appchina.rpc.thrift.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.appchina.rpc.base.cluster.RandomLoadBalance;
import com.appchina.rpc.base.cluster.client.DistributeClient;
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
public class DistributeClientTest {

	private ThriftThreadPoolServer server2;
	private ThriftThreadPoolServer server3;
	private ThriftThreadPoolServer server4;
	private DistributeClient<Request, Response> client;

	@Before
	public void before() throws Exception {
		ServerProcessor<Request, Response> serverProcessor = new ServerProcessor<Request, Response>() {
			@Override
			public Response process(Request param) {
				return new Response();
			}
		};
		
		server2 = new ThriftThreadPoolServer();
		server2.setProcessor(serverProcessor);
		server2.setPort(7912);
		server2.start();
		
		server3 = new ThriftThreadPoolServer();
		server3.setProcessor(serverProcessor);
		server3.setPort(7913);
		server3.start();
		
		server4 = new ThriftThreadPoolServer();
		server4.setProcessor(serverProcessor);
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
		client = new DistributeClient<Request, Response>();
		client.setLoadBalance(new RandomLoadBalance());
		client.setFactoryProvider(factoryProvider);
		client.setHeartbeat(2000);
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
		long start = System.currentTimeMillis();
		for(int i=0; i<10000; i++){
			client.send(new Request());
		}
		System.out.println("times:" + (System.currentTimeMillis() - start));;
		Thread.sleep(3000);
		
		for(int i=1; i<150; i++){
			Thread.sleep(40);
			client.send(new Request());
			
			if(i == 20){
				new Thread(new Runnable() {
					@Override
					public void run() {
						server2.stop();
						System.out.println("------------------------");
						try {
							server2.start();
						} catch (ServerException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
			if(i == 80){
				new Thread(new Runnable() {
					@Override
					public void run() {
						server3.stop();
					}
				}).start();
			}
			
		}
		Thread.sleep(3000);
	}

}
