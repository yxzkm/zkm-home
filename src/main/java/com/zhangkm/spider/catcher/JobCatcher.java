package com.zhangkm.spider.catcher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.frame.QueueThread;
import com.zhangkm.spider.frame.RedisDAO;
import com.zhangkm.spider.frame.TaskThread;

import net.sf.json.JSONObject;

@Service
public class JobCatcher extends QueueThread {

    @Autowired
    private Connection h2Connection;
    @Autowired
    protected RedisDAO redisDAO;

	protected boolean beforeRun(){
		System.out.println("beforeRun");
        System.out.println("QUEUE_LINK_SPIDER SIZE:"+redisDAO.getListSize(G.QUEUE_LINK_SPIDER));

		super.taskName = "JOB_CATCHER";
		super.MAX_THREAD_NUMBER = 1;
		super.SLEEP_BEFORE_NEXT_THREAD = 30000;
		return true;
	}
	
	protected void createThread(ExecutorService pool){
		pool.execute(new MultiTaskThread());
	}
	
	public class MultiTaskThread extends TaskThread{

		protected boolean initQueue(){
			super.QUEUE_NAME_FROM = G.QUEUE_JOB_CATCHER;
			super.QUEUE_NAME_TO = G.QUEUE_LINK_SPIDER;
			return true;
		}

		public void doMainJob(){
			
//			if(redisDAO.getListSize(QUEUE_NAME_TO)>0) return;

			//从数据库中获取采集点，然后放入待采集队列中
			insertTask();
		}

		protected void getDataFromQueueMap(){
		    fromQueueMap = redisDAO.rightPop(QUEUE_NAME_FROM);
		}
		
		public void insertTask(){
		    
	        ResultSetHandler<List<Map<String,String>>> handler = new ResultSetHandler<List<Map<String,String>>>() {
	            public List<Map<String,String>> handle(ResultSet rs) throws SQLException {
	                List<Map<String,String>> list = new ArrayList<Map<String,String>>();
	                while(rs.next()){
	                    Map<String,String> map = new HashMap<String,String>();
	                    ResultSetMetaData meta = rs.getMetaData();
	                    int cols = meta.getColumnCount();
	                    for (int i = 0; i < cols; i++) {
	                        map.put(meta.getColumnName(i+1), rs.getObject(i + 1).toString());
	                    }
	                    list.add(map);
	                }
	                return list;
	            }
	        };

	        QueryRunner run = new QueryRunner();
            String sql = ""
                    + "\n SELECT " 
                    + "\n   a.entry_website entry_website, "
                    + "\n   a.entry_group entry_group, "
                    + "\n   b.channel_name channel_name, "
                    + "\n   b.channel_url channel_url, "
                    + "\n   a.entry_encoding encoding, "
                    + "\n   a.entry_jsoup_urls url_regex, "
                    + "\n   a.page_jsoup_title jsoup_title, "
                    + "\n   a.page_jsoup_content jsoup_content "
                    + "\n from "
                    + "\n   entry_bbs a,"
                    + "\n   entry_bbs_channel b "
                    + "\n WHERE 1=1 "
//                    + "\n   and a.entry_jsoup_urls is not null "
//                    + "\n   and a.entry_jsoup_urls <> '' "
//                    + "\n   and a.entry_jsoup_urls <> 'a[href]' "
                    + "\n   and a.entry_id = b.entry_id "
                    + "\n order by "
                    + "\n   a.entry_id asc, "
                    + "\n   b.channel_id asc ";
//            String sql = "select * from test";
	        
	        try{
	            List<Map<String,String>> resultList = run.query(
	                    h2Connection, sql, handler);

	            for(Map<String,String> map : resultList){
	                System.out.println(map.toString());

                    fromQueueMap = new HashMap<String,String>();
                    fromQueueMap.put("group", map.get("ENTRY_GROUP"));
                    fromQueueMap.put("website", map.get("ENTRY_WEBSITE"));
                    fromQueueMap.put("channel", map.get("CHANNEL_NAME"));
                    fromQueueMap.put("channel_url", map.get("CHANNEL_URL"));
                    fromQueueMap.put("encoding", map.get("ENTRY_ENCODING"));
                    fromQueueMap.put("jsoup_url_regex", map.get("ENTRY_JSOUP_URLS"));
                    fromQueueMap.put("jsoup_title", map.get("PAGE_JSOUP_TITLE"));
                    fromQueueMap.put("jsoup_content", map.get("PAGE_JSOUP_CONTENT"));
                    
                    String json = JSONObject.fromObject(fromQueueMap).toString();
                    logger.info("json: \n{}\n",json);
                    redisDAO.leftPush(QUEUE_NAME_TO, json);
                    logInfo();

	            }

//                String now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
//                System.out.println("*******************************");
//                System.out.println(now+"   向任务队列导入 " + n + " 个网站采集点");

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
		
	}

}
