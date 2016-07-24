package com.appchina.rpc.base.cluster.pool;

import java.util.List;

/**
 * 用于提供很多工厂
 * 
 * @author dongjian_9@163.com
 * */
public interface ClientFactoryProvider<P, R> {
	
	public List<ClientFactory<P, R>> getFactories();
	
}
