package com.zhangkm.spider.frame;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.zhangkm.spider.util.Common;
import com.zhangkm.spider.util.RedisUtil;

public abstract class QueueThread extends Thread{
	protected String taskName = null;
	protected int MAX_THREAD_NUMBER = 1;   //默认最大线程数
	protected int SLEEP_BEFORE_NEXT_THREAD = 50;  //默认每次从任务队列取数据的时间间隔毫秒数

	public void run() {
		
		if (!beforeRun()) return;

		new ConsoleThread().start();
		ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_NUMBER);

		for (int k = 0; k < 1;) { // 死循环
			if (((ThreadPoolExecutor) pool).getActiveCount() < MAX_THREAD_NUMBER) {
				createThread(pool);
			} 
			Common.sleep(SLEEP_BEFORE_NEXT_THREAD);
		}

		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected abstract boolean beforeRun();

	protected abstract void createThread(ExecutorService pool);

	/**
	 * 这个线程用来监测队列参数，动态调整该队列的并发线程数
	 * 如果队列中的最大线程数发生变化，立即改变MAX_THREAD_NUMBER。
	 * 
	 * @ClassName: ConsoleThread
	 * @Description: TODO
	 *
	 */
	public class ConsoleThread extends Thread {
		//查询控制台参数完成后，下次查询之前等待的毫秒数
		private final int SLEEP_BEFORE_NEXT_LOOP = 10000;   

		public void run() {
			for (int i = 0; i < 1;) {
				initPara();
				doTimeJob();
				Common.sleep(SLEEP_BEFORE_NEXT_LOOP);
			}
		}

	    //定时任务
	    protected void doTimeJob() {
	        return;
	    }

		private void initPara() {
			String threadNum = RedisUtil.getStringData("PARAMETER:MAX_THREAD_NUMBER:" + taskName);
			String loopTime = RedisUtil.getStringData("PARAMETER:SLEEP_BEFORE_NEXT_THREAD:" + taskName);
			try {
				MAX_THREAD_NUMBER = Integer.parseInt(threadNum);
				if(MAX_THREAD_NUMBER<1) MAX_THREAD_NUMBER = 1;
			} catch (Exception e) {
				//e.printStackTrace();
			}
			try {
				SLEEP_BEFORE_NEXT_THREAD = Integer.parseInt(loopTime);
				if(SLEEP_BEFORE_NEXT_THREAD<1) SLEEP_BEFORE_NEXT_THREAD = 1;
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
	}

}
