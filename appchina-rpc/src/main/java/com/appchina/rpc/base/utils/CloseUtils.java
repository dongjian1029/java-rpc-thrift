package com.appchina.rpc.base.utils;

import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dongjian_9@163.com
 * */
public class CloseUtils {

	private static Log log = LogFactory.getLog(CloseUtils.class);
	
	public static void close(Closeable obj) {
		if(obj != null){
			try {
				obj.close();
			} catch (IOException e) {
				if (log.isWarnEnabled()){
					log.warn(e.getMessage(), e);
				}
			}
		}
	}

}
