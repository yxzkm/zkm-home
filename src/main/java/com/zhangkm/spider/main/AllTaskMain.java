package com.zhangkm.spider.main;

import com.zhangkm.spider.catcher.JobCatcher;
import com.zhangkm.spider.checker.LinkChecker;
import com.zhangkm.spider.extractor.TextExtractor;
import com.zhangkm.spider.filter.BasicFilter;
import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.spider.LinkSpider;
import com.zhangkm.spider.spider.PageSpider;

public class AllTaskMain {

	public static void main(String[] args) {
		G.initParameters();
		new JobCatcher().start();
		new LinkSpider().start();
		new LinkChecker().start();
		new PageSpider().start();
		new TextExtractor().start();
		new BasicFilter().start();
	}

}
