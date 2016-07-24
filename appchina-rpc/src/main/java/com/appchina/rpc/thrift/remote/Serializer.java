package com.appchina.rpc.thrift.remote;

/**
 * 序列化接口
 * 
 * @author dongjian_9@163.com
 */
public interface Serializer {
	
	public byte[] getBytes(Object obj) throws Exception;
	
	public <T> T getObject(byte[] bytes) throws Exception;
	
}