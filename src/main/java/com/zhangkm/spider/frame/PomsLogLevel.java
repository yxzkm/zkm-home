package com.zhangkm.spider.frame;

import org.apache.log4j.Level;

import ch.qos.logback.classic.net.SyslogAppender;
// 引用自：
// http://moxue2459.iteye.com/blog/1693625
public class PomsLogLevel  extends Level {
	public static final Level COUNTER_LEVEL = new PomsLogLevel(30050,"COUNTER", 1);
	
	protected PomsLogLevel(int level, String levelStr, int syslogEquivalent) {
		super(level, levelStr, syslogEquivalent);
	}

	public static Level toLevel(int level) {
		return COUNTER_LEVEL;
	}

}
