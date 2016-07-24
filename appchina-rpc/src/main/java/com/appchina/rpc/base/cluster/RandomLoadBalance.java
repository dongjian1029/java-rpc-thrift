package com.appchina.rpc.base.cluster;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.appchina.rpc.base.cluster.pool.Pool;

/**
 * 随机方式均衡，支持重新分配均衡池大小，暂不支持权重分配。（可以通过实例数量的配置实现）
 * 
 * @see Pool
 * 
 * @author dongjian_9@163.com
 */
public class RandomLoadBalance implements LoadBalance {
	protected Log log = LogFactory.getLog(RandomLoadBalance.class);
	protected boolean[] poolsStatus;
	
	@Override
	public synchronized void allocat(int count) {
		boolean[] _poolsStatus = new boolean[count <= 1 ? 1 : count];
		Arrays.fill(_poolsStatus, true);
		poolsStatus = _poolsStatus;
	}

	@Override
	public void invalid(int index) {
		boolean[] _poolsStatus = poolsStatus;
		if(index < _poolsStatus.length && _poolsStatus[index]){
			_poolsStatus[index] = false;
			if (log.isInfoEnabled()) {
				log.info(this.toString());
			}
		}
	}

	@Override
	public void available(int index) {
		boolean[] _poolsStatus = poolsStatus;
		if(index < _poolsStatus.length && !_poolsStatus[index]){
			_poolsStatus[index] = true;
			if (log.isInfoEnabled()) {
				log.info(this.toString());
			}
		}
	}

	@Override
	public int nextIndex() {
		boolean[] _poolsStatus = poolsStatus;
		if(_poolsStatus.length <= 1){
			return 0;
		}
		if (hasInvalid(_poolsStatus)) {
			int[] availableIndexs = new int[_poolsStatus.length];// 可用位置样本
			int availableCount = 0; // 可用数量
			for (int i = 0; i < _poolsStatus.length; i++) {
				if (_poolsStatus[i]) {
					availableIndexs[availableCount++] = i;// 记入样本数组。
				}
			}
			if (availableCount > 0) {
				int randomIndex = ThreadLocalRandom.current().nextInt(availableCount);
				return availableIndexs[randomIndex];
			}
		}
		return ThreadLocalRandom.current().nextInt(_poolsStatus.length);
	}

	@Override
	public boolean isAvailable(int index) {
		boolean[] _poolsStatus = poolsStatus;
		return index < _poolsStatus.length && _poolsStatus[index];
	}

	@Override
	public int getCount() {
		return poolsStatus.length;
	}
	
	@Override
	public int[] getAllInvalid() {
		boolean[] _poolsStatus = poolsStatus;
		List<Integer> invalidIndexs = new LinkedList<Integer>();
		for (int index = 0; index < _poolsStatus.length; index++) {
			if (!_poolsStatus[index]) {
				invalidIndexs.add(index);
			}
		}
		int[] result = new int[invalidIndexs.size()];
		int i = 0;
		for (int index : invalidIndexs) {
			result[i++] = index;
		}
		return result;
	}

	protected boolean hasInvalid(boolean[] _poolsStatus) {
		for (int i = 0; i < _poolsStatus.length; i++) {
			if (!_poolsStatus[i]) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "[" + Arrays.toString(poolsStatus) + "]";
	}

}
