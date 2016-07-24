package com.appchina.rpc.thrift.cluster;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.protocol.TProtocolFactory;
import org.springframework.beans.BeanUtils;

import com.appchina.rpc.base.cluster.client.DistributeClient;
import com.appchina.rpc.base.cluster.pool.ClientFactory;
import com.appchina.rpc.base.cluster.pool.ClientFactoryProvider;
import com.appchina.rpc.thrift.client.ThriftClientConfig;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * 为了方便配置，提供此类
 * 
 * @see DistributeClient
 * 
 * @author dongjian_9@163.com
 * */
public class ThriftClientFactoryProvider implements ClientFactoryProvider<Request, Response> {
	
	private List<String> hostPorts = new ArrayList<String>();
	
	private ThriftClientConfig config = new ThriftClientConfig();
	
	public ThriftClientFactoryProvider() { }
	
	@Override
	public List<ClientFactory<Request, Response>> getFactories() {
		List<ClientFactory<Request, Response>> factoryList = new ArrayList<ClientFactory<Request, Response>>(hostPorts.size());
		for(int i=0; i<hostPorts.size(); i++){
			String hostport = hostPorts.get(i);
			if(hostport == null || "".equals(hostport.trim())){
				throw new IllegalArgumentException("hostPort format error, the correct like:127.0.0.1:8080");
			}
			hostport = hostport.trim();
			String[] host_port = hostport.split(":");
			if(host_port.length != 2){
				throw new IllegalArgumentException("hostPort format error, the correct like:127.0.0.1:8080");
			}
			ThriftClientConfig newConfig = new ThriftClientConfig();
			BeanUtils.copyProperties(config, newConfig);
			newConfig.setHost(host_port[0]);
			newConfig.setPort(Integer.parseInt(host_port[1]));
			factoryList.add(new ThriftClientFactory(newConfig));
		}
		return factoryList;
	}
	
	public List<String> getHostPorts() {
		return hostPorts;
	}

	public void setHostPorts(List<String> hostPorts) {
		this.hostPorts = hostPorts;
	}

	public int getTimeout() {
		return config.getTimeout();
	}

	public void setTimeout(int timeout) {
		config.setTimeout(timeout);
	}

	public int getConnectionTimeout() {
		return config.getConnectionTimeout();
	}

	public void setConnectionTimeout(int connectionTimeout) {
		config.setConnectionTimeout(connectionTimeout);
	}

	public boolean isFramed() {
		return config.isFramed();
	}

	public void setFramed(boolean framed) {
		config.setFramed(framed);
	}

	public TProtocolFactory getProtocolFactory() {
		return config.getProtocolFactory();
	}

	public void setProtocolFactory(TProtocolFactory protocolFactory) {
		config.setProtocolFactory(protocolFactory);
	}

	public String getFrom() {
		return config.getFrom();
	}

	public void setFrom(String from) {
		config.setFrom(from);
	}

	public String getToken() {
		return config.getToken();
	}

	public void setToken(String token) {
		config.setToken(token);
	}

	@Override
	public String toString() {
		return "[hostPorts=" + hostPorts + ", config=" + config + "]";
	}

}