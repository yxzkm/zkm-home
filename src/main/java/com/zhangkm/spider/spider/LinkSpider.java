package com.zhangkm.spider.spider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhangkm.spider.util.UrlResponse;

import net.sf.json.JSONObject;

import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.frame.HttpclientResponsHandler;
import com.zhangkm.spider.frame.QueueThread;
import com.zhangkm.spider.frame.RedisDAO;
import com.zhangkm.spider.frame.TaskThread;
import com.zhangkm.spider.util.Common;

@Service
public class LinkSpider extends QueueThread {

    @Autowired
    protected RedisDAO redisDAO;

    @Override
	protected boolean beforeRun() {
		super.taskName = "LINK_SPIDER";
		super.MAX_THREAD_NUMBER =1;
		super.SLEEP_BEFORE_NEXT_THREAD = 1000; 
		
		System.out.println("KKKKKKKKKKKKKK:"+redisDAO.getListSize(G.QUEUE_LINK_CHECKER));
		
		return true;
	}

    @Override
	protected void createThread(ExecutorService pool){
		pool.execute(new UrlSpiderThread());
	}

	public class UrlSpiderThread extends TaskThread{

        @Override
		protected boolean initQueue() {
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
					redisDAO.leftPush(QUEUE_NAME_TO, JSONObject.fromObject(fromQueueMap).toString());
					logInfo();
				}
			}
		}
		
        @Override
        protected void getDataFromQueueMap() {
            fromQueueMap = redisDAO.rightPop(QUEUE_NAME_FROM);
        }	        

		/**
		 * 
		 * @param entryMap
		 * @return
		 */
	    private List<String> getPageUrlList(Map<String,String> entryMap){
	    	if(entryMap==null || entryMap.isEmpty()) return new ArrayList<String>();
	    	
//	    	
//	    	{
//	    	    ENTRY_ENCODING=gb2312, 
//	    	    PAGE_JSOUP_TITLE=title, 
//	    	    ENTRY_JSOUP_URLS=a[href], 
//	    	    PAGE_JSOUP_CONTENT=body, 
//	    	    ENTRY_GROUP=bbs, 
//	    	    CHANNEL_URL=http://club.history.sina.com.cn/forum-51-1.html, 
//	    	    CHANNEL_NAME=新浪杂谈, 
//	    	    ENTRY_WEBSITE=新浪论坛
//	    	}
	    	
	    	logger.info("000000000000000000");
            logger.info("000000000000000000: entryMap: \n{}\n",entryMap);

			String channel_url = entryMap.get("channel_url");
			String urlRegex = entryMap.get("jsoup_url_regex");
			if(channel_url==null 
					|| channel_url.trim().equals("")){
				return new ArrayList<String>();
			}

			if(urlRegex==null 
					|| urlRegex.trim().equals("")){
				return new ArrayList<String>();
			}

			logger.info("11111111111111111111111");
			// 访问该页面，获取帖子列表
			
	        CloseableHttpClient httpclient = HttpClients.createDefault();
	        HttpGet httpget = new HttpGet("http://httpbin.org/");

	        System.out.println("Executing request " + httpget.getRequestLine());

	        String bodyString = "";
	        
	        try {
	            bodyString = httpclient.execute(httpget, new HttpclientResponsHandler());
	        }
	        catch (IOException e) {
	            e.printStackTrace();
	        }finally {
	            try {
	                httpclient.close();
	            }catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        System.out.println("----------------------------------------");
	        System.out.println(bodyString);

			if(bodyString==null || bodyString.trim().equals("")) return new ArrayList<String>();
            logger.info("222222222222222222222222222");
			
			Document doc = Jsoup.parse(bodyString);
			Elements links = null; 
			try {
				links = doc.select("a[href]");
			} catch (Exception e) {
				return new ArrayList<String>();
			}
			
			if(links==null||links.size()==0) return new ArrayList<String>();
            logger.info("33333333333333333333333333333");

			List <String> urlList = new ArrayList<String>();
			boolean flag = false;
			for(Element link : links) {
				String sLink = link.attr("href").trim();
	            logger.info("sLink: {}",sLink);

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
