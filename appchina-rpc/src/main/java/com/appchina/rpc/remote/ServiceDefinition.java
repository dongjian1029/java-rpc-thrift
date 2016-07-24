package com.appchina.rpc.remote;

/**
 * 定义了一个服务的接口和实现类
 * 
 * @author dongjian_9@163.com
 * */
public class ServiceDefinition {
	
	private Class<?> interfaceClass;
	private Object implInstance;
	private String serviceName;

	public ServiceDefinition() {
		super();
	}

	public ServiceDefinition(Class<?> interfaceClass, Object implInstance) {
		this.interfaceClass = interfaceClass;
		this.implInstance = implInstance;
	}

	public ServiceDefinition(Class<?> interfaceClass, Object implInstance, String serviceName) {
		this.interfaceClass = interfaceClass;
		this.implInstance = implInstance;
		this.serviceName = serviceName;
	}
	
	public Class<?> getInterfaceClass() {
		return interfaceClass;
	}

	public Object getImplInstance() {
		return implInstance;
	}

	public void setInterfaceClass(Class<?> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	public void setImplInstance(Object implInstance) {
		this.implInstance = implInstance;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public String toString() {
		return "[interfaceClass=" + interfaceClass + ", implInstance=" + implInstance + ", serviceName=" + serviceName + "]";
	}

}