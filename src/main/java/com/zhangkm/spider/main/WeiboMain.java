package com.zhangkm.spider.main;

import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.spider.BaiduWeiboLinkSpider;
import com.zhangkm.spider.util.RedisUtil;

public class WeiboMain {

	public static void main(String[] args) {
		G.initParameters();
		if (!RedisUtil.initJedisPool()) return;
		if (!G.initHttpClient()) return;

		new BaiduWeiboLinkSpider().start();

	}

}
