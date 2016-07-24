package com.appchina.rpc.thrift.client;

import java.util.concurrent.TimeUnit;

import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TTupleProtocol;

/**
 * 封装配置
 * 
 * @author dongjian_9@163.com
 * */
public class ThriftClientConfig {
	
	public String host = "127.0.0.1";
	public int port = 9090;
	public int timeout = (int) TimeUnit.MINUTES.toMillis(1);
	public int connectionTimeout = (int) TimeUnit.SECONDS.toMillis(1);
	public boolean framed = false;
	public TProtocolFactory protocolFactory = new TTupleProtocol.Factory();
	public String from = "";
	public String token = "";
	
	public ThriftClientConfig(){}

	public ThriftClientConfig(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public boolean isFramed() {
		return framed;
	}

	public void setFramed(boolean framed) {
		this.framed = framed;
	}

	public TProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	public void setProtocolFactory(TProtocolFactory protocolFactory) {
		this.protocolFactory = protocolFactory;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return "[host=" + host + ", port=" + port + ", timeout=" + timeout
				+ ", connectionTimeout=" + connectionTimeout + ", framed=" + framed + ", protocolFactory="
				+ protocolFactory.getClass() + ", from=" + from + ", token=" + token + "]";
	}
	
	
}
