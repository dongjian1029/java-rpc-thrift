package com.appchina.rpc.remote.base;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.appchina.rpc.base.server.ServerProcessor;
import com.appchina.rpc.remote.MessageConvert;
import com.appchina.rpc.remote.MessageConvert.Param;
import com.appchina.rpc.remote.PathUtils;
import com.appchina.rpc.remote.ServiceDefinition;

/**
 * 用于继承，用来发布服务，实现了{@link ServerProcessor}
 * 
 * @author dongjian_9@163.com
 * */
public class ServicePublisher<P, R> implements ServerProcessor<P, R>, InitializingBean {
	private static Log log = LogFactory.getLog(ServicePublisher.class);
	
	private final Map<String, Resource> resources = new HashMap<String, Resource>();
	private final Set<Class<?>> verified = new HashSet<Class<?>>();
	
	protected MessageConvert<P, R> messageConvert;
	protected ServiceDefinition[] definitions;
	
	public ServicePublisher() { }

	public ServicePublisher(MessageConvert<P, R> messageConvert, ServiceDefinition[] definitions) {
		this.messageConvert = messageConvert;
		this.definitions = definitions;
	}

	public void setMessageConvert(MessageConvert<P, R> messageConvert) {
		this.messageConvert = messageConvert;
	}

	public void setDefinitions(ServiceDefinition[] definitions) {
		this.definitions = definitions;
	}

	public void init() throws Exception {
		if(messageConvert == null){
			throw new IllegalArgumentException("messageConvert is null");
		}
		if(definitions != null){
			for(ServiceDefinition def : definitions){
				this.publish(def);
			}
		}
	}

	private void publish(ServiceDefinition def) throws Exception {
		if(log.isInfoEnabled()){
			log.info(this.getClass().getSimpleName() + ": Procesing service definition:" + def);
		}
		Class<?> interfaceClass = def.getInterfaceClass();
		Object implInstance = def.getImplInstance();
		validate(interfaceClass, implInstance);
		Method[] interfaceMethods = interfaceClass.getMethods();
		for(Method method : interfaceMethods){
			String path = PathUtils.buildServicePath(def.getServiceName(), interfaceClass, method);
			if(path != null){
				validateTypes(method);
				Resource resource = new Resource(method, implInstance);
				Resource exist = resources.put(path, resource);
				if(exist != null){
					throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Publish duplicate " + path);
				}
				if(log.isInfoEnabled()){
					log.info("Publish service [" + path + "]");
				}
			}
		}
		verified.clear();
	}

	private void validateTypes(Method method) throws Exception {
		validateType(method.getReturnType());
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes != null) {
			for (Class<?> parameterType : parameterTypes) {
				validateType(parameterType);
			}
		}
	}

	private void validateType(Class<?> type) throws Exception {
		if(verified.add(type)){
			if(type.isPrimitive() || type.isEnum()){//可以是基本类型，枚举
				return;
			}
			if(type.isInterface() || Modifier.isAbstract(type.getModifiers())){//可以是接口或抽象类
				return;
			}
			if (type.isArray()) {//可以是数组，但是要检查单个元素
				validateType(type.getComponentType()); 
				return;
			}
			if(type.isAnonymousClass()){//不可以是匿名内部类
				throw new IllegalArgumentException(type + " not be anonymous class ");
			}
			if(Modifier.isPrivate(type.getModifiers()) ){//不可以是private修饰的类
				throw new IllegalArgumentException(type + " not be private class ");
			}
			if(!Serializable.class.isAssignableFrom(type)){//必须实现序列化
				throw new IllegalArgumentException(type + " must be " + Serializable.class.getName());
			}
			BeanInfo info = Introspector.getBeanInfo(type);
			PropertyDescriptor[] pds = info.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				if (pd.getName().equals("class")) continue;
				validateType(pd.getPropertyType());
			}
		}
	}

	private void validate(Class<?> interfaceClass, Object implInstance) throws Exception {
		if(interfaceClass == null || implInstance == null){
			throw new IllegalArgumentException("PublishDetail error, interfaceClass:" + interfaceClass + ", implInstance" + implInstance);
		}
		if(!interfaceClass.isInterface()){
			throw new IllegalArgumentException(interfaceClass + " must be interface");
		}
		if(!interfaceClass.isAssignableFrom(implInstance.getClass())){
			throw new IllegalArgumentException(implInstance.getClass() + " must be " + interfaceClass);
		}
	}
	
	@Override
	public R process(P parameter) throws Throwable {
		try {
			Param param = messageConvert.readParameters(parameter);
			Resource resource = resources.get(param.path);
			if(resource == null){
				throw new NoSuchMethodException("service [" + param.path + "] not support");
			}
			Object result = resource.method.invoke(resource.implInstance, param.parameters);
			return messageConvert.buildReturn(result);
		} catch (InvocationTargetException e) {
			//仅仅拦截应用层异常，其它异常视为框架BUG不处理，将会被Client包装为RemoteException抛出
			return messageConvert.buildException(e.getTargetException());
		} catch (Throwable e) {
			throw e;
		}
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}
	
	private static class Resource {
		Method method;
		Object implInstance;
		public Resource(Method method, Object implInstance) throws Exception {
			this.method = method;
			this.implInstance = implInstance;
		}
		@Override
		public String toString() {
			return "[method=" + method + ", implInstance=" + implInstance + "]";
		}
	}

}
