package com.appchina.rpc.thrift.server;

import java.util.HashMap;
import java.util.Map;

import com.appchina.rpc.thrift.TConstant;
import com.appchina.rpc.thrift.client.ClientHelper;
import com.appchina.rpc.thrift.support.Request;
import com.appchina.rpc.thrift.support.Response;

/**
 * Server端工具类
 * 
 * @author dongjian_9@163.com
 * */
public final class ServerHelper {

	public static Response newResponse() {
		Response response = new Response();
		response.setHeaders(new HashMap<String, String>());
		response.setStatus(TConstant.RESPONSE_STATUS_SUCCESS);
		response.unsetBody();
		return response;
	}
	
	/**
	 * 向返回结果中添加错误，客户端会对此进行验证。
	 * 
	 * @see {@link ClientHelper#valudateError(Response)}
	 * 
	 * @return 包装了错误信息的返回结果
	 */
	public static Response putError(Response response, String message) {
		response.setStatus(TConstant.RESPONSE_STATUS_ERROR);
		Map<String, String> headers = response.getHeaders();
		if (headers == null) {
			headers = new HashMap<String, String>();
			response.setHeaders(headers);
		}
		response.getHeaders().put(TConstant.RESPONSE_MESSAGE, message);
		return response;
	}

	/**
	 * 验证客户端访问凭据
	 * 
	 * @throws CredentialException
	 *             访问凭据无效
	 *             
	 * @see {@link ClientHelper#putCredential(Request, String, String)}
	 */
	public static void valudateCredential(Request request, Map<String, String> allowedFromTokens) throws CredentialException {
		if(request == null){
			throw new CredentialException();
		}
		Map<String, String> requestHeaders = request.getHeaders();
		if(requestHeaders == null){
			throw new CredentialException();
		}
		String from = requestHeaders.get(TConstant.REQUEST_FROM_KEY);
		if (from == null){
			throw new CredentialException();
		}
		String token = requestHeaders.get(TConstant.REQUEST_TOKEN_KEY);
		if (token == null){
			throw new CredentialException();
		}
		String allowedToken = allowedFromTokens.get(from);
		if(allowedToken == null || !allowedToken.equals(token)){
			throw new CredentialException();
		}
	}
	
}
