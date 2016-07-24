package com.appchina.rpc.thrift.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.appchina.rpc.base.utils.CloseUtils;

/**
 * @author dongjian_9@163.com
 */
public class JavaSerializer implements Serializer {

	@Override
	public byte[] getBytes(Object obj) throws Exception {
		if (obj == null) {
			return null;
		}
		ObjectOutputStream out = null;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bout);
			out.writeObject(obj);
			out.flush();
			return bout.toByteArray();
		} finally {
			CloseUtils.close(out);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(byte[] bytes) throws Exception {
		if (bytes == null) {
			return null;
		}
		ObjectInputStream oi = null;
		try {
			oi = new ObjectInputStream(new ByteArrayInputStream(bytes));
			return (T) oi.readObject();
		} finally {
			CloseUtils.close(oi);
		}
	}

}