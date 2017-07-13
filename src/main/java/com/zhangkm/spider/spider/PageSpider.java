package com.zhangkm.spider.spider;

import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.frame.QueueThread;
import com.zhangkm.spider.frame.TaskThread;
import com.zhangkm.spider.util.Common;
import com.zhangkm.spider.util.RedisUtil;
import com.zhangkm.spider.util.UrlResponse;

public class PageSpider extends QueueThread {

	protected boolean beforeRun(){
		super.taskName = "PAGE_SPIDER"; 
		super.MAX_THREAD_NUMBER = 10;
		super.SLEEP_BEFORE_NEXT_THREAD = 1;
		return true;
	}
	
	protected void createThread(ExecutorService pool){
		pool.execute(new HtmlSpiderThread());
	}
	
	public class HtmlSpiderThread extends TaskThread {

		protected boolean initQueue(){
			super.logger = Logger.getLogger(taskName);
			super.QUEUE_NAME_FROM = G.QUEUE_PAGE_SPIDER;
			super.QUEUE_NAME_TO = G.QUEUE_TEXT_EXTRACTOR;
			return true;
		}
		
		protected void doMainJob() {
			String url = fromQueueMap.get("url");
			UrlResponse urlResponse = Common.getUrlResponseTimes(url,G.HTTP_CLIENT);
			if(urlResponse==null){
				logError();
			}else{
				String html = urlResponse.getResponseBody();
				if(html==null || html.trim().equals("")){
					logError();
				}else{
					fromQueueMap.put("html", html);
					fromQueueMap.put("pubtime", ""+System.currentTimeMillis());
					RedisUtil.pushListData(QUEUE_NAME_TO, fromQueueMap);
					logInfo();
				}
			}
		}
		
	}

}
