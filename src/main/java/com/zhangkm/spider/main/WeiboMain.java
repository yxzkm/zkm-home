package com.zhangkm.spider.main;

import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.spider.BaiduWeiboLinkSpider;

public class WeiboMain {

	public static void main(String[] args) {
		G.initParameters();

		new BaiduWeiboLinkSpider().start();

	}

}
