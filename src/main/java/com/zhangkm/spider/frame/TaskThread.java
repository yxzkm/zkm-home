package com.zhangkm.spider.frame;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhangkm.spider.util.RedisUtil;

public abstract class TaskThread extends Thread{

    @Autowired
    private RedisDAO redisDAO;


	protected Logger logger=null;// = Logger.getLogger(TaskThread.class);

	protected String QUEUE_NAME_FROM;
	protected String QUEUE_NAME_TO;

	protected Map<String, String> fromQueueMap;
	
	public void run() {
		if(!initQueue()) return;
		if(!getDataFromQueueMap()) return;
		doMainJob();
	}

	protected abstract boolean initQueue();
    protected abstract void doMainJob();

	protected void logInfo() {
		String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
		RedisUtil.incrZsetMemberScore(""
					+"INFO:"+logger.getName()+":"
					+"GROUP"
				, fromQueueMap.get("group"));
		RedisUtil.incrZsetMemberScore(""
					+"INFO:"+logger.getName()+":"
					+"WEBSITE"
				, fromQueueMap.get("website"));
		RedisUtil.incrZsetMemberScore(""
					+"INFO:"+logger.getName()+":"
					+"WEBSITE:"+fromQueueMap.get("website")+":"
					+"CHANNEL"
				, fromQueueMap.get("channel"));

		RedisUtil.incrZsetMemberScore(""
					+"INFO:"+logger.getName()+":"
					+date+":"
					+"GROUP"
				, fromQueueMap.get("group"));
		RedisUtil.incrZsetMemberScore(""
					+"INFO:"+logger.getName()+":"
					+date+":"
					+"WEBSITE"
				, fromQueueMap.get("website"));
		RedisUtil.incrZsetMemberScore(""
					+"INFO:"+logger.getName()+":"
					+date+":"
					+"WEBSITE:"+fromQueueMap.get("website")+":"
					+"CHANNEL"
				, fromQueueMap.get("channel"));

		logger.info(""
				+ "["+fromQueueMap.get("group")+"]"
				+ "["+fromQueueMap.get("website")+"]"
				+ "["+fromQueueMap.get("channel")+"]"
				);
		return;
	}

	protected void logError() {
		RedisUtil.incrZsetMemberScore("ERROR:GROUP:"+logger.getName(), fromQueueMap.get("group"));
		RedisUtil.incrZsetMemberScore("ERROR:WEBSITE:"+logger.getName(), fromQueueMap.get("website"));
		RedisUtil.incrZsetMemberScore("ERROR:CHANNEL:"+logger.getName(), fromQueueMap.get("channel"));
		logger.error("【ERROR】"
				+ "["+fromQueueMap.get("group")+"]"
				+ "["+fromQueueMap.get("website")+"]"
				+ "["+fromQueueMap.get("channel")+"]"
				);
		return;
	}

	protected boolean getDataFromQueueMap(){
		//起始任务，不需要从队列里面取数据
		if (!QUEUE_NAME_FROM.equals(G.QUEUE_JOB_CATCHER)) {
			fromQueueMap = redisDAO.rightPop(QUEUE_NAME_FROM);
			if (fromQueueMap == null || fromQueueMap.isEmpty()) {
				//Common.sleep(WAIT_QUEUE_PUSH_DATA);
				return false;
			}
		}
		return true;
	}
	
}
