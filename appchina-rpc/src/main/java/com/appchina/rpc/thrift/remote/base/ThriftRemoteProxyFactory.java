package com.appchina.rpc.thrift.remote.base;

import com.appchina.rpc.base.client.ThreadSafetyClient;
import com.appchina.rpc.remote.base.RemoteProxyFactory;
import com.appchina.rpc.thrift.remote.ThriftMessageConvert;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * Thrift实现的远程代理工厂
 * 
 * @author dongjian_9@163.com
 * */
public class ThriftRemoteProxyFactory<T> extends RemoteProxyFactory<T, Request, Response> {
	
	public ThriftRemoteProxyFactory() {
		this.messageConvert = new ThriftMessageConvert();
	}

	public ThriftRemoteProxyFactory(Class<T> proxyInterface, ThreadSafetyClient<Request, Response> client) {
		super(proxyInterface, client, new ThriftMessageConvert());
	}

	public ThriftRemoteProxyFactory(Class<T> proxyInterface, String serviceName, ThreadSafetyClient<Request, Response> client) {
		super(proxyInterface, serviceName, client, new ThriftMessageConvert());
	}
	
}

