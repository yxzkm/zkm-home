package com.zhangkm.spider.main;

import com.zhangkm.spider.filter.BasicFilter;
import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.util.RedisUtil;

public class IndexMain {

	public static void main(String[] args) {
		G.initParameters();
		if (!RedisUtil.initJedisPool()) return;

		new BasicFilter().start();
		
	}

}
