package com.appchina.rpc.thrift.client;

import java.util.HashMap;
import java.util.Map;

import com.appchina.rpc.base.client.RemoteException;
import com.appchina.rpc.thrift.TConstant;
import com.appchina.rpc.thrift.server.ServerHelper;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * Client端工具类
 * 
 * @author dongjian_9@163.com
 */
public final class ClientHelper {

	public static Request newRequest(String path) {
		Request request = new Request();
		request.setHeaders(new HashMap<String, String>());
		request.setPath(path);
		request.unsetBody();
		return request;
	}

	/**
	 * 验证服务端是否发生了错误
	 * 
	 * @param response
	 *            服务端返回的数据
	 * 
	 * @throws RemoteException
	 *             表示服务端发生了错误
	 * 
	 * @see {@link ServerHelper#putError(Response, String)}
	 */
	public static void validateError(Response response) throws RemoteException {
		int status = response.getStatus();
		if (status == TConstant.RESPONSE_STATUS_ERROR) {
			Map<String, String> headers = response.getHeaders();
			if (headers == null) {
				throw new RemoteException();
			}else{
				throw new RemoteException(headers.get(TConstant.RESPONSE_MESSAGE));
			}
		}
	}

	/**
	 * 写入客户端访问凭据，服务端会对此进行验证。
	 * 
	 * @see {@link ServerHelper#valudateCredential(Request, boolean, Map)}
	 */
	public static void putCredential(Request request, String from, String token) {
		if (request == null){
			return;
		}
		if (from == null) {
			return;
		}
		if (token == null) {
			return;
		}
		Map<String, String> headers = request.getHeaders();
		if (headers == null) {
			headers = new HashMap<String, String>();
			request.setHeaders(headers);
		}
		headers.put(TConstant.REQUEST_FROM_KEY, from);
		headers.put(TConstant.REQUEST_TOKEN_KEY, token);
	}
	
	
}
