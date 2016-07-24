package com.appchina.rpc.thrift.remote;

import java.lang.reflect.Method;

import com.appchina.rpc.remote.MessageConvert;
import com.appchina.rpc.thrift.client.ClientHelper;
import com.appchina.rpc.thrift.server.ServerHelper;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/***
 * 基于thrift的消息转换<br>
 * 
 * 实现{@link Request}与{@link Param}转换<br>
 * 实现{@link Response}与{@link Object}转换<br>
 * 
 * @author dongjian_9@163.com
 *
 */
public class ThriftMessageConvert implements MessageConvert<Request, Response> {
	private static final int EXCEPTION = -1;
	
	private Serializer serializer = new KryoSerializer();
	private static Method getOurStackTraceMethod = null;
	static{
		try {
			getOurStackTraceMethod = Throwable.class.getDeclaredMethod("getOurStackTrace", new Class[]{});
			getOurStackTraceMethod.setAccessible(true);
		} catch (Exception e) {
			getOurStackTraceMethod = null;
		}
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}
	
	@Override
	public Request buildParameters(Param param) throws MessageConvertException {
		try {
			Request request = ClientHelper.newRequest(param.path);
			Object[] parameters = param.parameters;
			if (parameters != null) {
				request.setBody(serializer.getBytes(parameters));
			}
			return request;
		} catch (Throwable e) {
			throw new MessageConvertException("Can not write parameters.", e);
		}
	}
	
	@Override
	public Param readParameters(Request request) throws MessageConvertException {
		try {
			Object[] parameters = null;
			if (request.isSetBody()) {
				parameters = serializer.getObject(request.getBody());
			}
			return new Param(request.getPath(), parameters);
		} catch (Throwable e) {
			throw new MessageConvertException("Can not read parameters.", e);
		}
	}
	
	@Override
	public Response buildReturn(Object data) throws MessageConvertException {
		try {
			Response response = ServerHelper.newResponse();
			if (data != null) {
				response.setBody(serializer.getBytes(data));
			}
			return response;
		} catch (Throwable e) {
			throw new MessageConvertException("Can not write return data.", e);
		}
	}
	
	@Override
	public Object readReturn(Response response) throws MessageConvertException {
		try {
			Object data = null;
			if (response.isSetBody()) {
				data = serializer.getObject(response.getBody());
			}
			return data;
		} catch (Throwable e) {
			throw new MessageConvertException("Can not read return data.", e);
		}
	}

	@Override
	public Response buildException(Throwable exception) throws MessageConvertException {
		try {
			if(getOurStackTraceMethod != null){
				//java默认不会填充异常堆栈，导致kryo序列化无法打印堆栈，调用这个方法会填充堆栈，并且没有内存拷贝
				try {
					getOurStackTraceMethod.invoke(exception);
				} catch (Exception e) {
					getOurStackTraceMethod = null;
				}
			}
			Response response = ServerHelper.newResponse();
			response.setBody(serializer.getBytes(exception));
			response.setStatus(EXCEPTION);
			return response;
		} catch (Throwable e) {
			throw new MessageConvertException("Can not write exception.", e);
		}
	}
	
	@Override
	public Throwable readException(Response response) throws MessageConvertException {
		if(EXCEPTION == response.getStatus()){
			try {
				return serializer.getObject(response.getBody());
			} catch (Throwable e) {
				throw new MessageConvertException("Can not read exception." , e);
			}
		}
		return null;
	}

}
