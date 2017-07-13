package com.zhangkm.spider.checker;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.frame.QueueThread;
import com.zhangkm.spider.frame.TaskThread;
import com.zhangkm.spider.util.RedisUtil;

public class LinkChecker extends QueueThread {

	protected boolean beforeRun(){
		super.taskName = "LINK_CHECKER";
		super.MAX_THREAD_NUMBER = 100;
		super.SLEEP_BEFORE_NEXT_THREAD = 1;
		return BDBUtil.init();
	}

	protected void createThread(ExecutorService pool){
		pool.execute(new UrlCheckerThread());
	}

	private class UrlCheckerThread extends TaskThread {
		
		protected boolean initQueue(){
			super.logger = Logger.getLogger(taskName);
			super.QUEUE_NAME_FROM = G.QUEUE_LINK_CHECKER;
			super.QUEUE_NAME_TO = G.QUEUE_PAGE_SPIDER;
			return true;
		}
		
		/*
		protected boolean getDataFromQueueMap(){
			fromQueueMap = new HashMap<String,String>();
			fromQueueMap.put("channel_url","http://bbs.ifeng.com/forumdisplay.php?fid=324");
			fromQueueMap.put("url","http://bbs.ifeng.com/viewthread.php?tid=15777648");

			return true;
		}
*/

		protected void doMainJob() {
			if(formatEntryUrl(fromQueueMap)!=1) return;
			if(formatHrefUrl(fromQueueMap)!=1) return;

			String slid = DigestUtils.md5Hex(fromQueueMap.get("url"));
			if(BDBUtil.put(slid) == BDBUtil.BDB_SUCCESS) {
				fromQueueMap.put("slid", slid);
				RedisUtil.pushListData(QUEUE_NAME_TO, fromQueueMap);
				logInfo();
			}
		}
		
		private int formatEntryUrl(Map<String,String> map){
			if(map==null || map.isEmpty()) return 300;
			String url = map.get("channel_url");
			if(url==null) return 300;
			URI fullUri = null;
			try {
				fullUri = new URI(url.trim());
			} catch (URISyntaxException e) {
				return 300;
			}
			String scheme = fullUri.getScheme();
			String host = fullUri.getHost();
	        int port = fullUri.getPort();
	        if(port==80) port=-1;
	        String path = fullUri.getPath();
	        String query = fullUri.getQuery();

	        if(!"http".equalsIgnoreCase(scheme)){
	        	return 301;
	        }
	        if(host==null || host.trim().equals("")){
	        	return 302;
	        }

	        String formatUrl = "http://" + host;
	       	if(port!=-1) formatUrl = formatUrl + ":" + port;
	       	if(path!=null && !path.trim().equals("")) formatUrl = formatUrl + path;
	       	if(query!=null && !query.trim().equals("")) formatUrl = formatUrl + "?" + query;
	       	map.put("channel_url", formatUrl);
	       	return 1;

		}

		private int formatHrefUrl(Map<String,String> map){
			if(map==null || map.isEmpty()) return 300;
			String entryUrl = map.get("channel_url");
			String hrefUrl = map.get("url");
			if(hrefUrl==null || hrefUrl.trim().equals("")) return 300;
			if(entryUrl==null || entryUrl.trim().equals("")) return 300;
			URI fatherUri = null;
			try {
				fatherUri = new URI(entryUrl.trim());
			} catch (URISyntaxException e) {
				return 300;
			}
			String fatherScheme = fatherUri.getScheme();
			String fatherHost = fatherUri.getHost();
			int fatherPort = fatherUri.getPort();
			if(fatherPort==80) fatherPort = -1;
			
			if(!"http".equalsIgnoreCase(fatherScheme)){
	        	return 301;
	        }
	        if(fatherHost==null || fatherHost.trim().equals("")){
	        	return 302;
	        }
	        
			URI uri = null;
			try {
				uri = new URI(hrefUrl.trim());
			} catch (URISyntaxException e) {
				return 300;
			}
			String scheme = uri.getScheme();
			String host = uri.getHost();
			int port = uri.getPort();
			if(port==80) port = -1;
			String path = uri.getPath();
			String query = uri.getQuery();
			//String fragment = uri.getFragment();

			/*
			logger.info("scheme:"+scheme);
			logger.info("host:"+host);
			logger.info("port:"+port);
			logger.info("path:"+path);
			logger.info("query:"+query);
			logger.info("fragment:"+fragment);
			*/
			
			if(scheme!=null 
					&& !scheme.trim().equals("")
					&& !"http".equalsIgnoreCase(scheme)){
	            return 303;
	       	}
	        if(host==null || host.trim().equals("")){
	        	host = fatherHost;
	        	port = fatherPort;
	        }else{
	            if(!host.equalsIgnoreCase(fatherHost)){
	            	return 304;
	            }else{
	            	if(port!=fatherPort){
	            		return 305;
	            	}
	            }
	        }
	        
	        String formatUrl = "http://" + host;
	       	if(port != -1){
	       		formatUrl = formatUrl + ":" + port;
	       	} 
	       	if(path!=null && !path.trim().equals("")){
	       		if(path.startsWith("/")){
	           		formatUrl = formatUrl + path;
	       		}else{
	           		formatUrl = formatUrl + "/" + path;
	       		}
	       	}
	       	if(query!=null && !query.trim().equals("")){
	       		formatUrl = formatUrl + "?" + query;
	       	} 
	       	
	       	map.put("url", formatUrl);
	       	return 1;
		}

	}

	
}
