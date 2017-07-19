package com.zhangkm.spider.frame;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TaskThread extends Thread{

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected String QUEUE_NAME_FROM;
	protected String QUEUE_NAME_TO;

	protected Map<String, String> fromQueueMap;
	
	public void run() {
		if(!initQueue()) return;
		getDataFromQueueMap();
		doMainJob();
	}

	protected abstract boolean initQueue();
    protected abstract void doMainJob();

	protected void logInfo() {
		String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
//		RedisUtil.incrZsetMemberScore(""
//					+"INFO:"+logger.getName()+":"
//					+"GROUP"
//				, fromQueueMap.get("group"));
//		RedisUtil.incrZsetMemberScore(""
//					+"INFO:"+logger.getName()+":"
//					+"WEBSITE"
//				, fromQueueMap.get("website"));
//		RedisUtil.incrZsetMemberScore(""
//					+"INFO:"+logger.getName()+":"
//					+"WEBSITE:"+fromQueueMap.get("website")+":"
//					+"CHANNEL"
//				, fromQueueMap.get("channel"));
//
//		RedisUtil.incrZsetMemberScore(""
//					+"INFO:"+logger.getName()+":"
//					+date+":"
//					+"GROUP"
//				, fromQueueMap.get("group"));
//		RedisUtil.incrZsetMemberScore(""
//					+"INFO:"+logger.getName()+":"
//					+date+":"
//					+"WEBSITE"
//				, fromQueueMap.get("website"));
//		RedisUtil.incrZsetMemberScore(""
//					+"INFO:"+logger.getName()+":"
//					+date+":"
//					+"WEBSITE:"+fromQueueMap.get("website")+":"
//					+"CHANNEL"
//				, fromQueueMap.get("channel"));

		logger.info(""
				+ "["+fromQueueMap.get("group")+"]"
				+ "["+fromQueueMap.get("website")+"]"
				+ "["+fromQueueMap.get("channel")+"]"
				);
		return;
	}

	protected void logError() {
//		RedisUtil.incrZsetMemberScore("ERROR:GROUP:"+logger.getName(), fromQueueMap.get("group"));
//		RedisUtil.incrZsetMemberScore("ERROR:WEBSITE:"+logger.getName(), fromQueueMap.get("website"));
//		RedisUtil.incrZsetMemberScore("ERROR:CHANNEL:"+logger.getName(), fromQueueMap.get("channel"));
		logger.error("【ERROR】"
				+ "["+fromQueueMap.get("group")+"]"
				+ "["+fromQueueMap.get("website")+"]"
				+ "["+fromQueueMap.get("channel")+"]"
				);
		return;
	}

	protected abstract void getDataFromQueueMap();
	
}
