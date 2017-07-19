package com.zhangkm.spider.extractor;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.frame.QueueThread;
import com.zhangkm.spider.frame.RedisDAO;
import com.zhangkm.spider.frame.TaskThread;
import com.zhangkm.spider.util.RedisUtil;

public class TextExtractor extends QueueThread {
    @Autowired
    protected RedisDAO redisDAO;


	protected boolean beforeRun(){
		super.taskName = "TEXT_EXTRACTOR";
		super.MAX_THREAD_NUMBER = 1;
		return true;
	}
	
	protected void createThread(ExecutorService pool){
		pool.execute(new TextExtractorThread());
	}

	public class TextExtractorThread extends TaskThread {
        @Override
        protected void getDataFromQueueMap() {
            fromQueueMap = redisDAO.rightPop(QUEUE_NAME_FROM);
        }           


		protected boolean initQueue(){
			super.QUEUE_NAME_FROM = G.QUEUE_TEXT_EXTRACTOR;
			super.QUEUE_NAME_TO = G.QUEUE_BASIC_FILTER;
			return true;
		}
		
		protected void doMainJob() {
	       	if(extractText(fromQueueMap)){
	       		RedisUtil.pushListData(QUEUE_NAME_TO, fromQueueMap);
				logInfo();
	       	}else{
				logError();
	       	}
		}
	    
		/**
		 * 
		 * @param entryMap
		 * @return
		 */
	    private boolean extractText(Map<String,String> entryMap){
			String html = entryMap.get("html");
			if(html==null || html.trim().equals("")) return false;
			try {
				Document doc = null;
				try {
					doc = Jsoup.parse(html);
				} catch (Exception e) {
					return false;
				}

				String jsoup_title = entryMap.get("jsoup_title");
				if(jsoup_title==null||jsoup_title.trim().equals("")) jsoup_title = "title";
				String sTitle = getElementText(getElements(jsoup_title,doc));
				
				String jsoup_content = entryMap.get("jsoup_content");
				if(jsoup_content==null||jsoup_content.trim().equals("")) jsoup_content = "body";
				Elements contentEles = getElements(jsoup_content,doc);
				String sContent = getElementHtml(contentEles);
				String sContentNoTags = getElementText(contentEles);

				if(sTitle==null || sTitle.trim().equals("")) {
					//System.out.println("extract text error [Title null]: " + entryMap.get("url"));
					//System.out.println("entryMap: \n" + entryMap.toString());
					return false;	
				}else{
					String[] arr = sTitle.split("_");
					if(arr!=null && arr.length>0){
						sTitle = arr[0];
					}
				}

				if(sContentNoTags==null || sContentNoTags.trim().equals("")) {
					//System.out.println("extract text error [Content null]: " + entryMap.get("url"));
					//System.out.println("entryMap: \n" + entryMap.toString());
					return false;	
				}

				entryMap.put("title", sTitle);
				entryMap.put("content", sContent);
				entryMap.put("content_no_tags", sContentNoTags);

				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
		}

	    
	    private Elements getElements(String s,Document doc){
	    	if(s==null||s.trim().equals("")) return new Elements();
	    	Elements eles = new Elements();
	    	try {
				eles = doc.select(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	return eles;
	    } 
	    
	    private String getElementText(Elements eles){
	    	if (eles == null || eles.size() <= 0) return "";
	    	return eles.get(0).text();
	    } 
	    
	    private String getElementHtml(Elements eles){
	    	if (eles == null || eles.size() <= 0) return "";
	    	return eles.get(0).html();
	    } 
	 
	    private String getElementHref(Elements eles){
	    	if (eles == null || eles.size() <= 0) return "";
	    	return eles.get(0).attr("href").trim();
	    } 
	 
	    private String getImageSrc(Elements eles){
	    	if (eles == null || eles.size() <= 0) return "";
	    	return eles.get(0).attr("src").trim();
	    } 

	}

}
