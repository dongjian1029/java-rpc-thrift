package com.appchina.rpc.thrift.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TTupleProtocol;

import com.appchina.rpc.base.server.GenericServer;
import com.appchina.rpc.base.server.ServerProcessor;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * 封装server通用配置
 * 
 * @author dongjian_9@163.com
 * */
public abstract class ConfigurableThriftServer extends GenericServer<Request, Response> {
	
	protected int port = 9090;
	protected int workerThreads = 5;
	protected int clientTimeout = 30000;
	protected TProtocolFactory protocolFactory = new TTupleProtocol.Factory();
	protected boolean security = false;
	protected Map<String, String> allowedFromTokens = new HashMap<String, String>();
	protected int stopTimeoutVal = 2000;
	
	public ConfigurableThriftServer() {
		super();
	}

	public ConfigurableThriftServer(ServerProcessor<Request, Response> processor) {
		super(processor);
	}

	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getWorkerThreads() {
		return workerThreads;
	}
	public void setWorkerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
	}
	public TProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}
	public void setProtocolFactory(TProtocolFactory protocolFactory) {
		this.protocolFactory = protocolFactory;
	}
	public boolean isSecurity() {
		return security;
	}
	public void setSecurity(boolean security) {
		this.security = security;
	}
	public Map<String, String> getAllowedFromTokens() {
		return allowedFromTokens;
	}
	public void setAllowedFromTokens(Map<String, String> allowedFromTokens) {
		this.allowedFromTokens = allowedFromTokens;
	}
	
	public int getClientTimeout() {
		return clientTimeout;
	}

	public void setClientTimeout(int clientTimeout) {
		this.clientTimeout = clientTimeout;
	}
	
	public int getStopTimeoutVal() {
		return stopTimeoutVal;
	}

	public void setStopTimeoutVal(int stopTimeoutVal) {
		this.stopTimeoutVal = stopTimeoutVal;
	}

	@Override
	public String toString() {
		return "[port=" + port + ", workerThreads=" + workerThreads + ", protocolFactory="
				+ protocolFactory.getClass() + ", clientTimeout=" + clientTimeout + ", stopTimeoutVal=" + stopTimeoutVal
				+ ", security=" + security + ", allowedFromTokens=" + allowedFromTokens + "]";
	}
 

}
