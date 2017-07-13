package com.zhangkm.spider.catcher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

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
			//更新基本负面词汇、领域（栏目）词汇、地域词汇
			updateAllQueryTerms();
		}
		
		public void insertTask(){
			
			Statement stmt = null;

			try {
				try {
					stmt = G.MYSQL_CONN.createStatement();
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}
				
				String sql = ""
						+ "\n SELECT " 
						+ "\n 	a.entry_website entry_website, "
						+ "\n 	a.entry_group entry_group, "
						+ "\n 	b.channel_name channel_name, "
						+ "\n 	b.channel_url channel_url, "
						+ "\n 	if(a.entry_encoding is null,'gb2312',a.entry_encoding) encoding, "
						+ "\n 	if(a.entry_jsoup_urls is null,'href',a.entry_jsoup_urls) url_regex, "
						+ "\n 	if(a.page_jsoup_title is null,'title',a.page_jsoup_title) jsoup_title, "
						+ "\n 	if(a.page_jsoup_content is null,'body',a.page_jsoup_content) jsoup_content "
						+ "\n from "
						+ "\n 	entry_bbs a,"
						+ "\n	entry_bbs_channel b "
						+ "\n WHERE 1=1 "
						+ "\n 	and a.entry_jsoup_urls is not null "
						+ "\n 	and a.entry_jsoup_urls <> '' "
						+ "\n 	and a.entry_jsoup_urls <> 'a[href]' "
						+ "\n 	and a.entry_id = b.entry_id "
						+ "\n order by "
						+ "\n	a.entry_id asc, "
						+ "\n	b.channel_id asc ";

				System.out.println(sql);
				ResultSet rs = null;
				try {
					rs = stmt.executeQuery(sql);
					int n = 0;
					while (rs.next()) {
						fromQueueMap = new HashMap<String,String>();
						fromQueueMap.put("group", rs.getString("entry_group"));
						fromQueueMap.put("website", rs.getString("entry_website"));
						fromQueueMap.put("channel", rs.getString("channel_name"));
						fromQueueMap.put("channel_url", rs.getString("channel_url"));
						fromQueueMap.put("encoding", rs.getString("encoding"));
						fromQueueMap.put("jsoup_url_regex", rs.getString("url_regex"));
						fromQueueMap.put("jsoup_title", rs.getString("jsoup_title"));
						fromQueueMap.put("jsoup_content", rs.getString("jsoup_content"));
						
						RedisUtil.pushListData(QUEUE_NAME_TO, fromQueueMap);
						logInfo();
						
						n++;
					}
					
					String now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
					System.out.println("*******************************");
					System.out.println(now+"   向任务队列导入 " + n + " 个网站采集点");

				} catch (SQLException e) {
					e.printStackTrace();
					return;
				} finally{
					try {
						if(rs!=null) rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				return;
			} finally{
				try {
					if(stmt!=null) stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			return;
	    }
		
		private void updateAllQueryTerms(){
			//更新基本负面词汇
			updateQueryTerms(0);
			for(int k=1001;k<1005;k++){
				//更新领域（栏目）词汇
				updateQueryTerms(k);
			}
			for(int k=2000;k<2018;k++){
				//更新地域词汇
				updateQueryTerms(k);
			}
		}
		
		private boolean updateQueryTerms(int status){
			
			Statement stmt = null;
			try {
				try {
					stmt = G.MYSQL_CONN.createStatement();
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				}

				String sql = "" 
						+ "\n SELECT " 
						+ "\n 	word_name " 
						+ "\n from " 
						+ "\n 	word_table " 
						+ "\n WHERE 1=1 " 
						+ "\n 	and word_status = " + status 
						+ "\n order by " 
						+ "\n	word_id asc ";

				ResultSet rs = null;
				try {
					String terms = "";
					rs = stmt.executeQuery(sql);
					while (rs.next()) {
						String wordName = rs.getString("word_name");
						terms = terms + wordName + "@";
					}
					return RedisUtil.setStringData("QUERY:TERMS:STATUS:"+status, terms);
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				} finally {
					try {
						if (rs != null) rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
	    }
	    
	}

}
