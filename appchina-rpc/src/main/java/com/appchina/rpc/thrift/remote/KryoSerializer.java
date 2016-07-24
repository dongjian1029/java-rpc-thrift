package com.appchina.rpc.thrift.remote;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.instantiator.basic.AccessibleInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.appchina.rpc.base.utils.CloseUtils;
import com.appchina.rpc.base.utils.PoolUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

/**
 * @author dongjian_9@163.com
 */
public class KryoSerializer implements Serializer {
	private static final ObjectPool<Kryo> pool = new KryoGenericObjectPool();

	private int bufferSize = 4096;
	private int maxSize = 4096 * 4096;
	
	public KryoSerializer() { } 
	
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	public byte[] getBytes(Object obj) throws Exception {
		if (obj == null) {
			return null;
		}
		Kryo kryo = null;
		Output output = null;
		try {
			output = new ByteBufferOutput(bufferSize, maxSize);
			kryo = pool.borrowObject();
			kryo.writeClassAndObject(output, obj);
			output.flush();
			return output.toBytes();
		} finally {
			PoolUtils.returnObject(pool, kryo);
			CloseUtils.close(output);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(byte[] bytes) throws Exception {
		if (bytes == null) {
			return null;
		}
		Kryo kryo = null;
		Input input = null;
		try {
			input = new ByteBufferInput(bytes);
			kryo = pool.borrowObject();
			return (T) kryo.readClassAndObject(input);
		} finally {
			PoolUtils.returnObject(pool, kryo);
			CloseUtils.close(input);
		}
	}
	
	private static class KryoGenericObjectPool extends GenericObjectPool<Kryo> {
		private static KryoFactory factory = new KryoFactory();
		private static GenericObjectPool.Config config = new GenericObjectPool.Config();
		static{
			config.maxActive = 1000;
			config.maxIdle = 500;
			config.minIdle = 0;
			config.maxWait = -1;
			config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
			config.testOnBorrow = false;
			config.testOnReturn = false;
			config.testWhileIdle = false;
			config.minEvictableIdleTimeMillis = -1;
			config.timeBetweenEvictionRunsMillis = -1;
			config.softMinEvictableIdleTimeMillis = -1;
			config.lifo = true;
		}
		
		public KryoGenericObjectPool() {
			super(factory, config);
		}
	}
	
	private static class KryoFactory extends BasePoolableObjectFactory<Kryo> {
		private static InstantiatorStrategy defaultInstantiatorStrategy = new MyInstantiatorStrategy();
		
		@Override
		public Kryo makeObject() throws Exception {
			Kryo kryo = new MyKryo();
			kryo.setReferences(true);
			kryo.setRegistrationRequired(false);
			kryo.setInstantiatorStrategy(defaultInstantiatorStrategy);
			return kryo;
		}
	}

	private static class MyInstantiatorStrategy implements InstantiatorStrategy {
		private InstantiatorStrategy stdInstantiatorStrategy = new StdInstantiatorStrategy();
		
		@Override
		public <T> ObjectInstantiator<T> newInstantiatorOf (Class<T> type) {
			ObjectInstantiator<T> instantiator = null;
			try {
				//先查找无参构造器，否则ArrayList、HashMap、等类会出问题
				instantiator = type.isAnonymousClass() ? null : new AccessibleInstantiator<T>(type);;
			} catch (Exception ignored) {
				//没有无参构造器会报错
			}
			
			if(instantiator != null){
				return instantiator;
			}else{
				return stdInstantiatorStrategy.newInstantiatorOf(type);
			}
		}
	}
	

	private static class MyKryo extends Kryo {
		private static Class<?> unmodifiableCollectionClass = Collections.unmodifiableCollection(new ArrayList<>()).getClass();
		private static Class<?> synchronizedCollectionClass = Collections.synchronizedCollection(new ArrayList<>()).getClass();
		
		private static Class<?> singletonSetClass = Collections.singleton("").getClass();
		private static Class<?> emptySetClass = Collections.emptySet().getClass();
		private static Class<?> synchronizedSetClass = Collections.synchronizedSet(new TreeSet<>()).getClass();
		private static Class<?> synchronizedSortedSetClass = Collections.synchronizedSortedSet(new TreeSet<>()).getClass();
		private static Class<?> unmodifiableSetClass = Collections.unmodifiableSet(new TreeSet<>()).getClass();
		private static Class<?> unmodifiableSortedSetClass = Collections.unmodifiableSortedSet(new TreeSet<>()).getClass();
		
		private static Class<?> singletonMapClass = Collections.singletonMap("", "").getClass();
		private static Class<?> emptyMapClass = Collections.emptyMap().getClass();
		private static Class<?> synchronizedMapClass = Collections.synchronizedMap(new TreeMap<>()).getClass();
		private static Class<?> synchronizedSortedMapClass = Collections.synchronizedSortedMap(new TreeMap<>()).getClass();
		private static Class<?> unmodifiableMapClass = Collections.unmodifiableMap(new TreeMap<>()).getClass();
		private static Class<?> unmodifiableSortedMapClass = Collections.unmodifiableSortedMap(new TreeMap<>()).getClass();
		
		private static Class<?> singletonListClass = Collections.singletonList("").getClass();
		private static Class<?> emptyListClass = Collections.emptyList().getClass();
		private static Class<?> synchronizedListClass = Collections.synchronizedList(new ArrayList<>()).getClass();
		private static Class<?> unmodifiableListClass = Collections.unmodifiableList(new ArrayList<>()).getClass();
		
		
		public MyKryo() {
			this.addDefaultSerializer(Throwable.class, new JavaSerializer());
			this.defaultRegister();
		}

		private void defaultRegister() {
			this.register(void.class);
			this.register(byte.class);
			this.register(byte[].class);
			this.register(char.class);
			this.register(char[].class);
			this.register(short.class);
			this.register(short[].class);
			this.register(int.class);
			this.register(int[].class);
			this.register(long.class);
			this.register(long[].class);
			this.register(float.class);
			this.register(float[].class);
			this.register(double.class);
			this.register(double[].class);
			this.register(boolean.class);
			this.register(boolean[].class);
			this.register(Byte.class);
			this.register(Byte[].class);
			this.register(Character.class);
			this.register(Character[].class);
			this.register(Short.class);
			this.register(Short[].class);
			this.register(Integer.class);
			this.register(Integer[].class);
			this.register(Long.class);
			this.register(Long[].class);
			this.register(Float.class);
			this.register(Float[].class);
			this.register(Double.class);
			this.register(Double[].class);
			this.register(Boolean.class);
			this.register(Boolean[].class);

			this.register(BigInteger.class);
			this.register(BigInteger[].class);
			this.register(BigDecimal.class);
			this.register(BigDecimal[].class);

			this.register(Class.class);
			this.register(Object.class);
			this.register(Object[].class);
			this.register(Date.class);
			this.register(Date[].class);
			this.register(String.class);
			this.register(String[].class);
			this.register(StringBuilder.class);
			this.register(StringBuilder[].class);
			this.register(StringBuffer.class);
			this.register(StringBuffer[].class);

			this.register(Currency.class);
			this.register(Currency[].class);
			this.register(TimeZone.class);
			this.register(TimeZone[].class);
			this.register(Calendar.class);
			this.register(Calendar[].class);
			this.register(Locale.class);
			this.register(Locale[].class);

			this.register(Collection.class);
			this.register(unmodifiableCollectionClass);
			this.register(synchronizedCollectionClass);
			
			this.register(Set.class);
			this.register(TreeSet.class);
			this.register(HashSet.class);
			this.register(LinkedHashSet.class);
			this.register(singletonSetClass);
			this.register(emptySetClass);
			this.register(synchronizedSetClass);
			this.register(synchronizedSortedSetClass);
			this.register(unmodifiableSetClass);
			this.register(unmodifiableSortedSetClass);
			
			this.register(Map.class);
			this.register(TreeMap.class);
			this.register(HashMap.class);
			this.register(LinkedHashMap.class);
			this.register(singletonMapClass);
			this.register(emptyMapClass);
			this.register(synchronizedMapClass);
			this.register(synchronizedSortedMapClass);
			this.register(unmodifiableMapClass);
			this.register(unmodifiableSortedMapClass);

			this.register(List.class);
			this.register(ArrayList.class);
			this.register(LinkedList.class);
			this.register(Vector.class);
			this.register(singletonListClass);
			this.register(emptyListClass);
			this.register(synchronizedListClass);
			this.register(unmodifiableListClass);
			
			this.register(StackTraceElement.class);
			this.register(StackTraceElement[].class);
			this.register(Exception.class);
			this.register(Exception[].class);
			this.register(RuntimeException.class);
			this.register(RuntimeException[].class);
			this.register(Throwable.class);
			this.register(Throwable[].class);
		}
	}
	
}