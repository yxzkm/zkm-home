package com.zhangkm.spider.spider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.util.Common;
import com.zhangkm.spider.util.RedisUtil;

public class ImageSpider extends Thread{
	static Logger logger = Logger.getLogger(ImageSpider.class);
	private final static String QUEUE_NAME_FROM = "textQueue";
	private final static String QUEUE_NAME_TO = "fullTermQueue";
	
	private static boolean waitingFlag = false;
	private static int MAX_THREAD_NUMBER = 1;
	
	public void run(){
    	ExecutorService pool;
		pool = Executors.newFixedThreadPool(MAX_THREAD_NUMBER);
		
        pool.execute(new ImageSpiderThread());
		for(int i=0;i<Integer.MAX_VALUE;i++){
	        if(waitingFlag) {
	        	waitingFlag = false;
			    Common.sleep(5000);
	        }
	        if(((ThreadPoolExecutor)pool).getActiveCount() < MAX_THREAD_NUMBER){
		        pool.execute(new ImageSpiderThread());
	        }
		    Common.sleep(500);
		}

        pool.shutdown();
        try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
	        System.out.println(Thread.currentThread().getName() + " ImageSpider shudown now!!!!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	private class ImageSpiderThread extends Thread {
		public void run() {
			Map<String,String> map = RedisUtil.popListData(QUEUE_NAME_FROM);
			if(map==null || map.isEmpty()){
				waitingFlag = true;
				return;
			}
			String url = map.get("imageUrl");
			if(url!=null && !url.trim().equals("")){
		       	byte[] image = null;
				try {
					image = Common.getImage(url,G.HTTP_CLIENT);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		   		if(image!=null && image.length>0){
		   			map.put("image", null);
		   			String key = map.get("key");
		   			if(key!=null && !key.trim().equals("")){
						try {
							String pathName1 = "d:/pic/" + key.substring(0, 2);
							String pathName2 = pathName1 + "/" + key.substring(2, 4);
							String fullFileName = pathName2 + "/" + key + ".jpg";
							
							File filePath1 = new File(pathName1);
							if(!filePath1.exists()){
								filePath1.mkdir();
							}
							File filePath2 = new File(pathName2);
							if(!filePath2.exists()){
								filePath2.mkdir();
							}
							File file = new File(fullFileName);
							if(file.exists()) file.delete();

							FileOutputStream fos = new FileOutputStream(fullFileName,true);  
							fos.write(image);  
							fos.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}  
		   			}
		   		}
			}
			RedisUtil.pushListData(QUEUE_NAME_TO, map);
		}
		
	}

}
