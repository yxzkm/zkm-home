package com.zhangkm.spider.main;

import com.zhangkm.spider.util.RedisUtil;

public class ClearQueueMain {
	public static void main(String[] args){
		RedisUtil.clearAllQueue();
		System.out.println("Clear all queue over!");
		
	}
}
