package com.appchina.rpc.thrift;

/**
 * @author dongjian_9@163.com
 * */
public class TConstant {

	//服务端正常返回
	public static final int RESPONSE_STATUS_SUCCESS = 0;
	//服务端抛出异常
	public static final int RESPONSE_STATUS_ERROR = 1;
	
	//服务端返回的附加信息key
	public static final String RESPONSE_MESSAGE = "RESPONSE-MESSAGE";

	//请求凭据key
	public static final String REQUEST_FROM_KEY = "REQUEST-FROM";
	public static final String REQUEST_TOKEN_KEY = "REQUEST-TOKEN";

}
