package com.zhangkm.spider.main;

import com.zhangkm.spider.catcher.JobCatcher;
import com.zhangkm.spider.checker.LinkChecker;
import com.zhangkm.spider.extractor.TextExtractor;
import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.spider.LinkSpider;
import com.zhangkm.spider.spider.PageSpider;
import com.zhangkm.spider.util.RedisUtil;

public class AllTaskMain {

	public static void main(String[] args) {
		G.initParameters();
		if (!RedisUtil.initJedisPool()) return;
		if (!G.initMysqlConnection()) return;
		if (!G.initHttpClient()) return;
		
		new JobCatcher().start();
		new LinkSpider().start();
		new LinkChecker().start();
		new PageSpider().start();
		new TextExtractor().start();

		/*
		new BasicFilter().start();
		new IndustryFilter().start();
		new RegionFilter().start();
*/
		
		//new MongoWriter().start();

	}

}
