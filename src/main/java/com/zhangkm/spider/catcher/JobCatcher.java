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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.frame.QueueThread;
import com.zhangkm.spider.frame.TaskThread;
import com.zhangkm.spider.util.RedisUtil;

public class JobCatcher extends QueueThread {
	
	protected boolean beforeRun(){
		System.out.println("beforeRun");

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
			super.logger = Logger.getLogger(taskName);
			super.QUEUE_NAME_FROM = G.QUEUE_JOB_CATCHER;
			super.QUEUE_NAME_TO = G.QUEUE_LINK_SPIDER;
			return true;
		}

		public void doMainJob(){
			
			if(RedisUtil.getListSize(QUEUE_NAME_TO)>0) return;

			//从数据库中获取采集点，然后放入待采集队列中
			insertTask();
		}
		
		public void insertTask(){
		    
	        ResultSetHandler<List<Map<String,Object>>> handler = new ResultSetHandler<List<Map<String,Object>>>() {
	            public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
	                List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
	                while(rs.next()){
	                    Map<String,Object> map = new HashMap<String,Object>();
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
	        Connection conn = G.getH2Conn();
            String sql = ""
                    + "\n SELECT " 
                    + "\n   a.entry_website entry_website, "
                    + "\n   a.entry_group entry_group, "
                    + "\n   b.channel_name channel_name, "
                    + "\n   b.channel_url channel_url, "
                    + "\n   if(a.entry_encoding is null,'gb2312',a.entry_encoding) encoding, "
                    + "\n   if(a.entry_jsoup_urls is null,'href',a.entry_jsoup_urls) url_regex, "
                    + "\n   if(a.page_jsoup_title is null,'title',a.page_jsoup_title) jsoup_title, "
                    + "\n   if(a.page_jsoup_content is null,'body',a.page_jsoup_content) jsoup_content "
                    + "\n from "
                    + "\n   entry_bbs a,"
                    + "\n   entry_bbs_channel b "
                    + "\n WHERE 1=1 "
                    + "\n   and a.entry_jsoup_urls is not null "
                    + "\n   and a.entry_jsoup_urls <> '' "
                    + "\n   and a.entry_jsoup_urls <> 'a[href]' "
                    + "\n   and a.entry_id = b.entry_id "
                    + "\n order by "
                    + "\n   a.entry_id asc, "
                    + "\n   b.channel_id asc ";

	        
	        try{
	            List<Map<String,Object>> resultList = run.query(
	                    conn, sql, handler);

	            for(Map<String,Object> map : resultList){
	                System.out.println(map.toString());
	                
	                
//                    fromQueueMap = new HashMap<String,String>();
//                    fromQueueMap.put("group", rs.getString("entry_group"));
//                    fromQueueMap.put("website", rs.getString("entry_website"));
//                    fromQueueMap.put("channel", rs.getString("channel_name"));
//                    fromQueueMap.put("channel_url", rs.getString("channel_url"));
//                    fromQueueMap.put("encoding", rs.getString("encoding"));
//                    fromQueueMap.put("jsoup_url_regex", rs.getString("url_regex"));
//                    fromQueueMap.put("jsoup_title", rs.getString("jsoup_title"));
//                    fromQueueMap.put("jsoup_content", rs.getString("jsoup_content"));
//                    
//                    RedisUtil.pushListData(QUEUE_NAME_TO, fromQueueMap);
//                    logInfo();

	            }

//                String now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
//                System.out.println("*******************************");
//                System.out.println(now+"   向任务队列导入 " + n + " 个网站采集点");

	        } catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                DbUtils.close(conn);
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }  
	        }
	    }
		
	}

}
