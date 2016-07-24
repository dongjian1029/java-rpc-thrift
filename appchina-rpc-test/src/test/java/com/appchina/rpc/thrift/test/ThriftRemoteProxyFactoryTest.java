package com.appchina.rpc.thrift.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.appchina.rpc.base.cluster.client.DistributeClient;
import com.appchina.rpc.base.server.ServerException;
import com.appchina.rpc.remote.ServiceDefinition;
import com.appchina.rpc.test.api.AddService;
import com.appchina.rpc.test.api.AddService.AddServiceException;
import com.appchina.rpc.test.api.MoodService;
import com.appchina.rpc.test.api.model.GENDER;
import com.appchina.rpc.test.api.model.Mood;
import com.appchina.rpc.test.impl.AddServiceImpl;
import com.appchina.rpc.test.impl.MoodServiceImpl;
import com.appchina.rpc.thrift.cluster.ThriftClientFactoryProvider;
import com.appchina.rpc.thrift.remote.base.ThriftRemoteProxyFactory;
import com.appchina.rpc.thrift.remote.base.ThriftServicePublisher;
import com.appchina.rpc.thrift.server.ThriftThreadPoolServer;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * 测试服务端发布服务，并且被代理对象远程调用
 * 
 * @author dongjian_9@163.com
 */
public class ThriftRemoteProxyFactoryTest {

	private ThriftThreadPoolServer server2;
	private ThriftThreadPoolServer server3;
	private ThriftThreadPoolServer server4;
	private DistributeClient<Request, Response> client;

	@Before
	public void before() throws Exception {
		ServiceDefinition[] definitions = new ServiceDefinition[] {
				new ServiceDefinition(AddService.class, new AddServiceImpl()),
				new ServiceDefinition(MoodService.class, new MoodServiceImpl()) };

		ThriftServicePublisher serverProcessor = new ThriftServicePublisher(definitions);
		serverProcessor.init();
		
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
		

		ThriftClientFactoryProvider factoryProvider = new ThriftClientFactoryProvider();
		List<String> hostPorts = new ArrayList<String>(); 
		hostPorts.add("127.0.0.1:7912");
		hostPorts.add("127.0.0.1:7913");
		hostPorts.add("127.0.0.1:7914");
		factoryProvider.setHostPorts(hostPorts);
		
		client = new DistributeClient<Request, Response>(); 
		client.setFactoryProvider(factoryProvider);
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
	public void test() throws Exception {
		AddService addService = new ThriftRemoteProxyFactory<AddService>(AddService.class, client).getObject();
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			addService.add(i);
		}
		System.out.println("times:" + (System.currentTimeMillis() - start));

		testError();
		testMood();
	}

	public void testError() throws Exception {
		AddService addService = new ThriftRemoteProxyFactory<AddService>(AddService.class, client).getObject();
		try {
			addService.exception();
		} catch (AddServiceException e) {
			e.printStackTrace();
		}
	}

	public void testMood() throws Exception {
		MoodService moodService = new ThriftRemoteProxyFactory<MoodService>(MoodService.class, client).getObject();
		List<Mood> moodList = new LinkedList<Mood>();
		moodList.add(new Mood());
		moodList.add(new Mood());
		moodList.add(new Mood());
		moodList.add(new Mood());
		moodList.add(new Mood());
		moodService.test();
		System.out.println(moodService.test(1));
		System.out.println(moodService.test("name"));
		System.out.println(moodService.test(1, "name"));
		System.out.println(moodService.test(new Mood()));
		System.out.println(moodService.test(moodList));
		System.out.println(Arrays.toString(moodService.test(new Mood[] { new Mood() })));
		System.out.println(Arrays.toString(moodService.test(new int[] { 1, 2, 3 })));
		System.out.println(moodService.test(0, 1));
		System.out.println(moodService.test(GENDER.W));
	}

}
