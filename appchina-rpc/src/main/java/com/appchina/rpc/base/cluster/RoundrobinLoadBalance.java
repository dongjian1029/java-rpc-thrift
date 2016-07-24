package com.appchina.rpc.base.cluster;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 轮询方式均衡。
 * 
 * @author dongjian_9@163.com
 */
public class RoundrobinLoadBalance implements LoadBalance {
	protected Log log = LogFactory.getLog(RoundrobinLoadBalance.class);
	
	protected RingItem[] items;
	protected volatile RingItem current;
	
	protected final byte[] RING_LOCK = new byte[0];
	protected final byte[] CURRENT_LOCK = new byte[0];

	@Override
	public synchronized void allocat(int count) {
		if(this.items != null){
			throw new IllegalStateException("not allocated again.");
		}
		if (count <= 1) {
			RingItem item = new RingItem(0);
			item.next = new WeakReference<RingItem>(item);
			item.pre = new WeakReference<RingItem>(item);
			this.items = new RingItem[] { item };
			this.current = item;
		} else {
			RingItem[] items = new RingItem[count];
			for (int i = 0; i < count; i++) {
				items[i] = new RingItem(i);
			}
			int first = 0;
			int last = count - 1;
			for (int i = (first + 1); i < last; i++) {
				items[i].next = new WeakReference<RingItem>(items[i + 1]);
				items[i].pre = new WeakReference<RingItem>(items[i - 1]);
			}
			items[first].next = new WeakReference<RingItem>(items[first + 1]);
			items[first].pre = new WeakReference<RingItem>(items[last]);
			items[last].next = new WeakReference<RingItem>(items[first]);
			items[last].pre = new WeakReference<RingItem>(items[last - 1]);
			this.items = items;
			this.current = items[ThreadLocalRandom.current().nextInt(count)];
		}
	}

	@Override
	public void invalid(int index) {
		if (items.length > 1) {
			RingItem me = items[index];
			if (me.available) {
				synchronized (RING_LOCK) {
					if (me.available) {
						WeakReference<RingItem> myPreRef = me.pre;
						WeakReference<RingItem> myNextRef = me.next;
						RingItem myPre = myPreRef.get();
						RingItem myNext = myNextRef.get();
						if (myNext == me || myPre == me) {
							return;// 这说明已经是最后一个了
						}
						myPre.next = myNextRef;
						myNext.pre = myPreRef;
						me.available = false;
						if (log.isInfoEnabled()) {
							log.info(this.toString());
						}
					}
				}
			}
		}
	}

	@Override
	public void available(int index) {
		if (items.length > 1) {
			RingItem me = items[index];
			if (!me.available) {
				synchronized (RING_LOCK) {
					if (!me.available) {
						RingItem available = null;
						RingItem availableNext = null;
						for (int i = 0; i < items.length; i++) {
							RingItem item = items[i];
							if (item.available) {
								available = item;
								availableNext = available.next.get();
								break;
							}
						}
						me.pre = availableNext.pre;
						me.next = available.next;
						WeakReference<RingItem> meRef = new WeakReference<RingItem>(me);
						availableNext.pre = meRef;
						available.next = meRef; //最后连接这个防止跳跃
						me.available = true;
						if (log.isInfoEnabled()) {
							log.info(this.toString());
						}
					}
				}
			}
		}
	}
	
	@Override
	public int nextIndex() {
		if (items.length > 1) {
			synchronized (CURRENT_LOCK) {
				current = current.next.get();
				return current.index;
			}
		}
		return current.index;
	}

	@Override
	public boolean isAvailable(int index) {
		return items[index].available;
	}
	@Override
	public int getCount() {
		return items.length;
	}
	
	@Override
	public int[] getAllInvalid() {
		List<Integer> invalidIndexs = new LinkedList<Integer>();
		for (int index = 0; index < items.length; index++) {
			RingItem item = items[index];
			if (!item.available) {
				invalidIndexs.add(item.index);
			}
		}
		int[] result = new int[invalidIndexs.size()];
		int i = 0;
		for (int index : invalidIndexs) {
			result[i++] = index;
		}
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		synchronized (RING_LOCK) {
			for (int i = 0; i < items.length; i++) {
				RingItem item = items[i];
				if (item.available) {
					sb.append("[").append(item.pre.get().index).append("<-").append(item.index).append("->").append(item.next.get().index).append("] ");
				} else {
					sb.append("[").append(item.index).append("] ");
				}
			}
		}
		return sb.toString();
	}
	
	protected static final class RingItem {
		protected final int index;
		protected volatile WeakReference<RingItem> pre;
		protected volatile WeakReference<RingItem> next;
		protected volatile boolean available = true;

		protected RingItem(int index) {
			this.index = index;
		}
	}

	
}
