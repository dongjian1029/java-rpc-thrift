package com.appchina.rpc.test.api;

import java.util.List;
import java.util.Map;

import com.appchina.rpc.test.api.model.GENDER;
import com.appchina.rpc.test.api.model.Mood;

public interface MoodService {

	public void test();
	
	public Map<String, Mood> test(Integer value);
	
	public Integer test(String value1);
	
	public String test(Integer value, String value1);
	
	public Mood test(Mood value1);
	
	public List<Mood> test(List<Mood> value1);
	
	public Mood[] test(Mood[] value1);
	
	public int[] test(int[] value1);
	
	public int test(int value1, int value2);
	
	public GENDER test(GENDER value1);
	
	
}
