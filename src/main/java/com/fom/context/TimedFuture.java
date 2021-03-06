package com.fom.context;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 
 * @author shanhm
 *
 * @param <T> 结果类型
 */
class TimedFuture<T> extends FutureTask<T> {

	private long createTime;

	private String executorName;

	public TimedFuture(Runnable runnable, T result) {
		super(runnable, result);
		createTime = System.currentTimeMillis();
	}

	public TimedFuture(Callable<T> callable){
		super(callable);
		createTime = System.currentTimeMillis();
		if(callable instanceof Task){
			executorName = ((Task)callable).contextName;
		}
	}

	public long getCreateTime() {
		return createTime;
	}

	public String getName(){
		return executorName;
	}

}
