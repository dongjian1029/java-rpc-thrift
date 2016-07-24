package com.appchina.rpc.test.api;

import com.appchina.rpc.remote.AService;

public interface AddService {
	
	@AService("add-integer")
	public Integer add(Integer param);

	@AService
	public void exception() throws AddServiceException;
	
	public static class AddServiceException extends Exception {
		
		private static final long serialVersionUID = 1L;
		
		public AddServiceException() { }
		
		public AddServiceException(String message) {
			super(message);
		}
		
	}
}
