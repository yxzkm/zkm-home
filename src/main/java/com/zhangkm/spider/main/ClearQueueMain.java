package com.zhangkm.spider.main;

import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.util.RedisUtil;

public class ClearQueueMain {
	public static void main(String[] args){
		G.initParameters();
		
		RedisUtil.clearAllQueue();
		System.out.println("Clear all queue over!");
		
	}
}
