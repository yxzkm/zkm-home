package com.zhangkm.spider.filter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.frame.QueueThread;
import com.zhangkm.spider.frame.TaskThread;
import com.zhangkm.spider.util.RedisUtil;

public class BasicFilter extends QueueThread {

	private List<String> termList = null;
	private Analyzer ANALYZER = new CJKAnalyzer();

	public boolean beforeRun(){
		super.taskName = "BASIC_FILTER";
		initBasicTermList();
		return true;
	}
	
	protected void doTimeJob(){
		initBasicTermList();
		return;
	}

	private void initBasicTermList() {
	}

	protected void createThread(ExecutorService pool){
		pool.execute(new MultiTaskThread());
	}
	
	public class MultiTaskThread extends TaskThread{
		private RAMDirectory RAM_WORK_DIRECTORY = null;

		protected boolean initQueue(){
			super.logger = Logger.getLogger(taskName);
			super.QUEUE_NAME_FROM = G.QUEUE_BASIC_FILTER;
			super.QUEUE_NAME_TO = G.QUEUE_INDUSTRY_FILTER;
			return true;
		}
		
		protected void doMainJob(){
			try {
				RAM_WORK_DIRECTORY = new RAMDirectory();
				//showToken();
				if(!writeIndex()) return; 
				if(!doSearch()) return;
				RedisUtil.pushListData(QUEUE_NAME_TO, fromQueueMap);
				logInfo();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					RAM_WORK_DIRECTORY.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		private boolean writeIndex(){
		    return true;
		}
		
		private boolean doSearch() {
            return true;
		}
	
		private class PomsTerm{
			String name = null;
			int score = 0;

			public PomsTerm(String name,int score){
				this.name = name;
				this.score = score;
			}
			
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public int getScore() {
				return score;
			}
			public void setScore(int score) {
				this.score = score;
			}
			
			
		}
		
		public class MyComparator implements Comparator {

			public int compare(Object arg0, Object arg1) {
				PomsTerm user0 = (PomsTerm) arg0;
				PomsTerm user1 = (PomsTerm) arg1;

				if (user0.getScore() < user1.getScore()) return 1;
				return 0;
			}
		}
	}

}
