package com.zhangkm.spider.spider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.zhangkm.spider.util.RedisUtil;
import com.zhangkm.spider.util.UrlResponse;
import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.frame.QueueThread;
import com.zhangkm.spider.frame.TaskThread;
import com.zhangkm.spider.util.Common;

public class LinkSpider extends QueueThread {

    @Override
	protected boolean beforeRun() {
		super.taskName = "LINK_SPIDER";
		super.MAX_THREAD_NUMBER =10;
		super.SLEEP_BEFORE_NEXT_THREAD = 1; 
		return true;
	}

    @Override
	protected void createThread(ExecutorService pool){
		pool.execute(new UrlSpiderThread());
	}

	public class UrlSpiderThread extends TaskThread{

        @Override
		protected boolean initQueue() {
			super.logger = Logger.getLogger(taskName);
			super.QUEUE_NAME_FROM = G.QUEUE_LINK_SPIDER;
			super.QUEUE_NAME_TO = G.QUEUE_LINK_CHECKER;
			return true;
		}

/*
		protected boolean getDataFromQueueMap(){
			fromQueueMap = new HashMap<String,String>();
			fromQueueMap.put("channel_url","http://bbs.ifeng.com/forumdisplay.php?fid=324");
			fromQueueMap.put("jsoup_url_regex","(.*)/viewthread\\.php\\?tid=\\d*(.*)");
			return true;
		}
*/

	    @Override
		public void doMainJob() {
			List<String> list = getPageUrlList(fromQueueMap);
			if (list != null && list.size() > 0) {
				for (String url : list) {
					fromQueueMap.put("url", url);
					RedisUtil.pushListData(QUEUE_NAME_TO, fromQueueMap);
					logInfo();
				}
			}
		}
		
		/**
		 * 
		 * @param entryMap
		 * @return
		 */
	    private List<String> getPageUrlList(Map<String,String> entryMap){
	    	if(entryMap==null || entryMap.isEmpty()) return new ArrayList<String>();
	    	
			String channel_url = entryMap.get("channel_url");
			String urlRegex = entryMap.get("jsoup_url_regex");
			if(channel_url==null 
					|| channel_url.trim().equals("")){
				return new ArrayList<String>();
			}

			if(urlRegex==null 
					|| urlRegex.trim().equals("")
					|| urlRegex.trim().equalsIgnoreCase("a[href]")){
				return new ArrayList<String>();
			}

			// 访问该页面，获取帖子列表
			UrlResponse urlResponse = Common.getUrlResponseTimes(channel_url,G.HTTP_CLIENT);
			if(urlResponse==null) return new ArrayList<String>();
			
			String bodyString = urlResponse.getResponseBody();

			if(bodyString==null || bodyString.trim().equals("")) return new ArrayList<String>();
			
			Document doc = Jsoup.parse(bodyString);
			Elements links = null; 
			try {
				links = doc.select("a[href]");
			} catch (Exception e) {
				return new ArrayList<String>();
			}
			
			if(links==null||links.size()==0) return new ArrayList<String>();

			List <String> urlList = new ArrayList<String>();
			boolean flag = false;
			for(Element link : links) {
				String sLink = link.attr("href").trim();
				if(sLink!=null && !sLink.trim().equals("")){
					try {
						if(sLink.matches(urlRegex)){
							//将url加入采集队列。
							urlList.add(sLink);
							flag = true;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if(!flag) {
				System.out.println("Error:正则表达式错误:" 
							+ entryMap.get("website") + ":" 
							+ entryMap.get("channel") + ":"
							+ entryMap.get("channel_url") + ":"
							+ urlRegex);	
			}
					
			return urlList;	
		}

	}
    
}
