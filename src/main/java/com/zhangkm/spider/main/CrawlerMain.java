package com.zhangkm.spider.main;

import com.zhangkm.spider.catcher.JobCatcher;
import com.zhangkm.spider.extractor.TextExtractor;
import com.zhangkm.spider.filter.BasicFilter;
import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.spider.BaiduWeiboLinkSpider;
import com.zhangkm.spider.spider.LinkSpider;
import com.zhangkm.spider.spider.PageSpider;
import com.zhangkm.spider.util.RedisUtil;

public class CrawlerMain {

	public static void main(String[] args) {
		G.initParameters();
		if (!RedisUtil.initJedisPool()) return;
		if (!G.initMysqlConnection()) return;
		if (!G.initHttpClient()) return;

		new JobCatcher().start();
		new LinkSpider().start();
		new PageSpider().start();
		new TextExtractor().start();

	}

}
