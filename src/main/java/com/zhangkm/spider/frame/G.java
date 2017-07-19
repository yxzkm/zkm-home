package com.zhangkm.spider.frame;

import org.apache.http.client.HttpClient;

public class G {

    public static final String QUEUE_JOB_CATCHER = "QUEUE:JOB_CATCHER";
	public static final String QUEUE_LINK_SPIDER = "QUEUE:LINK_SPIDER";
	public static final String QUEUE_LINK_CHECKER = "QUEUE:LINK_CHECKER";
	public static final String QUEUE_PAGE_SPIDER = "QUEUE:PAGE_SPIDER";
	public static final String QUEUE_TEXT_EXTRACTOR = "QUEUE:TEXT_EXTRACTOR";
	public static final String QUEUE_BASIC_FILTER = "QUEUE:BASIC_FILTER";
	public static final String QUEUE_INDUSTRY_FILTER = "QUEUE:INDUSTRY_FILTER";
	public static final String QUEUE_REGION_FILTER = "QUEUE:REGION_FILTER";
	public static final String QUEUE_MONGO_WRITER = "QUEUE:MONGO_WRITER";
	public static final String QUEUE_SOLR_WRITER = "QUEUE:SOLR_WRITER";

	public static String LOCAL_FILE_PATH;
	public static String JCRB_CHANNEL_ID;
	

}
