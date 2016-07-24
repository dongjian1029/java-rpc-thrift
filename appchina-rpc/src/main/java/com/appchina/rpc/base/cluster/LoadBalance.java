package com.appchina.rpc.base.cluster;

/**
 * 负载均衡接口，提供负载均衡的方式
 * 
 * @author dongjian_9@163.com
 * 
 * */
public interface LoadBalance {
	
	/**
	 * 给出一个总数，将在 0 到 count-1 之间进行均衡
	 * */
	void allocat(int count);
	/**
	 * 设置指定位置无效，将不再被使用
	 * */
	void invalid(int index);
	/**
	 * 恢复指定位置有效，继续被使用
	 * */
	void available(int index);
	/**
	 * 用于负载均衡的方法，获取下一个使用的位置
	 * */
	int nextIndex();
	/**
	 * 判断指定位置是否正在被使用
	 * */
	boolean isAvailable(int index);
	/**
	 * 获取已分配的数量
	 * */
	int getCount();
	/**
	 * 返回所有不可用位置，数组内的每个元素表示了位置
	 * */
	int[] getAllInvalid();
}
