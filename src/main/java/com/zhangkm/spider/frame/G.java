package com.zhangkm.spider.frame;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.http.client.HttpClient;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.search.BooleanQuery;

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

	public static String CRAWLER_HOME = null;

	public static HttpClient HTTP_CLIENT; //全局HttpClient池
    public static Connection H2_CONN = null;

	/**********************************************************/
	/** 以下参数，均来自配置文件   **/
	public static int HTTP_CLIENT_MAX_NUM; //最大httpclient并发数量

	public static String BDB_NAME; 
	public static String BDB_PATH; 

	public static String MYSQL_IP;
	public static int MYSQL_PORT;
	public static String MYSQL_DATABASE;
	public static String MYSQL_USER;
	public static String MYSQL_PWD;

	public static String SOLR_SERVER_ADDR;

	public static String LOCAL_FILE_PATH;
	public static String JCRB_CHANNEL_ID;
	

	/*************************************************************/

	/**
	 * 用于初始化各项公共参数
	 */
	public static void initParameters(){
	     Properties prop = null;
	       try {
			Properties ps = System.getProperties();
			// ps.list(System.out);
			G.CRAWLER_HOME = ps.getProperty("user.dir");

			BufferedInputStream inBuff = new BufferedInputStream(
					new FileInputStream(G.CRAWLER_HOME + "/cfg/parameters.properties"));
			prop = new Properties();
			prop.load(inBuff);
			inBuff.close();

			BooleanQuery.setMaxClauseCount(10000); 

			G.HTTP_CLIENT_MAX_NUM = Integer.parseInt(prop.getProperty("HTTP_CLIENT_MAX_NUM"));
			
			G.BDB_NAME = prop.getProperty("BDB_NAME");
			G.BDB_PATH = prop.getProperty("BDB_PATH");
			
			G.MYSQL_IP = prop.getProperty("MYSQL_IP");
			G.MYSQL_PORT = Integer.parseInt(prop.getProperty("MYSQL_PORT"));
			G.MYSQL_DATABASE = prop.getProperty("MYSQL_DATABASE");
			G.MYSQL_USER = prop.getProperty("MYSQL_USER");
			G.MYSQL_PWD = prop.getProperty("MYSQL_PWD");
			
			G.SOLR_SERVER_ADDR = prop.getProperty("SOLR_SERVER_ADDR"); 
			
			//初始化log4j配置文件。
			PropertyConfigurator.configure(G.CRAWLER_HOME + "/cfg/log4j.properties"); 

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	public static Connection getH2Conn() {
        try {
            if(H2_CONN!=null && !H2_CONN.isClosed()) return H2_CONN;
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        
        try {
            H2_CONN = DriverManager.getConnection(
                    "jdbc:h2:/C:/zkm/files4test/h2database/h2db", 
                    "sa", "");
            System.out.println("初始化H2database数据库完毕...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return H2_CONN;
    }
    
}
