package com.appchina.rpc.base.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;

/**
 * @author dongjian_9@163.com
 * */
public class PoolUtils {

	private static Log log = LogFactory.getLog(PoolUtils.class);
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void returnObject(ObjectPool pool, Object obj) {
		if(obj != null && pool != null){
			try {
				pool.returnObject(obj);
			} catch (Exception e) {
				if (log.isWarnEnabled()){
					log.warn(e.getMessage(), e);
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void invalidateObject(ObjectPool pool, Object obj) {
		if(obj != null && pool != null){
			try {
				pool.invalidateObject(obj);
			} catch (Exception e) {
				if (log.isWarnEnabled()){
					log.warn(e.getMessage(), e);
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public static void close(ObjectPool pool) {
		if(pool != null){
			try {
				pool.close();
			} catch (Exception e) {
				if (log.isWarnEnabled()){
					log.warn(e.getMessage(), e);
				}
			}
		}
	}

}
