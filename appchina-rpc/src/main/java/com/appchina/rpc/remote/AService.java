package com.appchina.rpc.remote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务声明
 * 
 * 接口上标明此类，表示接口的所有方法可提供服务
 * <br>
 * 方法上标明此类，表示该方法可提供服务
 * 
 * @author dongjian_9@163.com
 * */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AService {
	
	/**
	 * 标注到类上，表示服务绝对地址
	 * <br>
	 * 标注到方法上，表示服务相对地址
	 * */
	String value() default "";
	
}
