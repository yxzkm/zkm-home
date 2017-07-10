package com.zhangkm.demo.base.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StringFormat {

	public static void main(String[] args){
		// 格式化（排版）字符串
		System.out.println(String.format("%20s", "zhangkm"));
		
		// 格式化日期时间
		try {
			Date cstDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss +08:00 yyyy",Locale.ENGLISH).parse("Wed Dec 28 14:56:08 +08:00 2016");
			String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cstDate);
			System.out.println("now Date: " + dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
