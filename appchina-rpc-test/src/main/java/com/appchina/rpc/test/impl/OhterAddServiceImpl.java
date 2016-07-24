package com.appchina.rpc.test.impl;

import com.appchina.rpc.test.api.AddService;

public class OhterAddServiceImpl implements AddService {

	@Override
	public Integer add(Integer param) {
		return ++param;
	}

	@Override
	public void exception() {
		
	}
	
}
