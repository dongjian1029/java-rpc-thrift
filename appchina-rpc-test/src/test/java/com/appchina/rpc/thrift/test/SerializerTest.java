package com.appchina.rpc.thrift.test;

import java.util.Collections;

import org.junit.Test;

import com.appchina.rpc.test.api.model.Mood;
import com.appchina.rpc.thrift.remote.JavaSerializer;
import com.appchina.rpc.thrift.remote.KryoSerializer;
import com.appchina.rpc.thrift.remote.Serializer;

public class SerializerTest {
	private Serializer java = new JavaSerializer();
	private Serializer kryo = new KryoSerializer();

	private Object[] objs = new Object[] {1, 2D, 3F, new Long(5), Collections.emptyList(), Collections.emptySet(),
			Collections.singleton(new Mood()), Collections.singletonList(new Mood()), new Mood(), new Mood(), new Mood(), new Mood()};

	@Test
	public void test1() throws Exception {
		long javaSize = 0;
		long javaStart = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			java.getObject(java.getBytes(new Mood()));
			java.getObject(java.getBytes(objs));
			javaSize += java.getBytes(objs).length;
		}
		System.out.println("java times:" + (System.currentTimeMillis() - javaStart) + ", javaSize:" + javaSize);

		System.out.println("==============================================");
		long kryoSize = 0;
		long kryoStart = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			kryo.getObject(kryo.getBytes(new Mood()));
			kryo.getObject(kryo.getBytes(objs));
			kryoSize += kryo.getBytes(objs).length;
		}
		System.out.println("kryo times:" + (System.currentTimeMillis() - kryoStart) + ", kryoSize:" + kryoSize);
		System.out.println("==============================================");
	}

}
