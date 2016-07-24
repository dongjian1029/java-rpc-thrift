package com.appchina.rpc.remote;

import java.lang.reflect.Method;

/**
 * 请求路径处理类<br>
 * 
 * 将接口的方法解析为服务地址（因为方法的参数使用了简洁名，这可能重复）
 * 
 * 可以通过{@link AService}类设置服务地址，避免重复
 * 
 * @author dongjian_9@163.com
 */
public final class PathUtils {

	public static String buildServicePath(String serviceName, Class<?> proxyInterface, Method method) {
		StringBuilder sb = new StringBuilder("/");
		if(serviceName != null){
			sb.append(serviceName).append("/");
		}
		sb.append(proxyInterface.getName()).append("/");
		return sb.append(paseRelativePath(method)).toString();
	}

	private static String paseRelativePath(Method method) {
		AService methodAnnotation = method.getAnnotation(AService.class);
		if(methodAnnotation == null || "".equals(methodAnnotation.value())){
			StringBuilder sb = new StringBuilder(method.getName());
			sb.append("/");
			Class<?>[] types = method.getParameterTypes();
			if (types != null && types.length > 0) {
				for (Class<?> type : types) {
					sb.append(type.getSimpleName());
					sb.append("/");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			return sb.toString();
		} else {
			return methodAnnotation.value();
		}
	}

}
