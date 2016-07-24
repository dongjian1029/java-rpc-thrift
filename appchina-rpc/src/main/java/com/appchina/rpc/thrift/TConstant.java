package com.appchina.rpc.thrift;

/**
 * @author dongjian_9@163.com
 * */
public class TConstant {

	//成功状态
	public static final int RESPONSE_STATUS_SUCCESS = 0;
	
	//服务端发生了错误，以及附加信息
	public static final int RESPONSE_STATUS_ERROR = 1;
	public static final String RESPONSE_MESSAGE = "RESPONSE-MESSAGE";

	//请求凭据
	public static final String REQUEST_FROM_KEY = "REQUEST-FROM";
	public static final String REQUEST_TOKEN_KEY = "REQUEST-TOKEN";

}
