package com.appchina.rpc.thrift.test;

import org.junit.Test;

import com.appchina.rpc.remote.ServiceDefinition;
import com.appchina.rpc.test.api.AddService;
import com.appchina.rpc.test.api.MoodService;
import com.appchina.rpc.test.impl.AddServiceImpl;
import com.appchina.rpc.test.impl.MoodServiceImpl;
import com.appchina.rpc.thrift.remote.base.ThriftServicePublisher;
import com.appchina.rpc.thrift.server.ThriftThreadPoolServer;

/**
 * 测试服务端发布服务，并且被代理对象远程调用
 * 
 * @author dongjian_9@163.com
 * */
public class ThriftServicePublisherTest {

	@Test
	public void start() throws Exception {
		ServiceDefinition[] definitions = new ServiceDefinition[]{	
			new ServiceDefinition(AddService.class, new AddServiceImpl()),
			new ServiceDefinition(MoodService.class, new MoodServiceImpl())
		};
		
		ThriftServicePublisher publisher = new ThriftServicePublisher(definitions);
		publisher.init();
		ThriftThreadPoolServer server = new ThriftThreadPoolServer(publisher);
		
		server.start();

		server.stop();
	}

}
