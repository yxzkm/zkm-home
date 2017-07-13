package com.zhangkm.spider.main;

import com.zhangkm.spider.checker.LinkChecker;
import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.util.RedisUtil;

public class CheckerMain {

	public static void main(String[] args) {
		G.initParameters();
		if (!RedisUtil.initJedisPool()) return;
		
		new LinkChecker().start();
	}

}
