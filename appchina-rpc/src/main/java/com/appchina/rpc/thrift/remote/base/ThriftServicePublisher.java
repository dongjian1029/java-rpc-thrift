package com.appchina.rpc.thrift.remote.base;

import com.appchina.rpc.remote.ServiceDefinition;
import com.appchina.rpc.remote.base.ServicePublisher;
import com.appchina.rpc.thrift.remote.ThriftMessageConvert;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * Thrift实现的服务发布处理器
 * 
 * @author dongjian_9@163.com
 * */
public class ThriftServicePublisher extends ServicePublisher<Request, Response> {
	
	public ThriftServicePublisher() {
		this.messageConvert = new ThriftMessageConvert();
	}

	public ThriftServicePublisher(ServiceDefinition... definitions) {
		super(new ThriftMessageConvert(), definitions);
	}
	
}

