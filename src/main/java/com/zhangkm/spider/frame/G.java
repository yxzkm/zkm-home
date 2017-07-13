package com.zhangkm.spider.frame;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import net.sf.json.JSONObject;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.search.BooleanQuery;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

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
	public static Connection ORACLE_CONN = null;
	public static Connection MYSQL_CONN = null;


	/**********************************************************/
	/** 以下参数，均来自配置文件   **/
	public static int HTTP_CLIENT_MAX_NUM; //最大httpclient并发数量

	public static String BDB_NAME; 
	public static String BDB_PATH; 

	public static String REDIS_IP;
	public static int REDIS_PORT;
	public static int REDIS_DB_INDEX;

	public static String ORACLE_IP;
	public static int ORACLE_PORT;
	public static String ORACLE_SID;
	public static String ORACLE_USER;
	public static String ORACLE_PWD;

	public static String MYSQL_IP;
	public static int MYSQL_PORT;
	public static String MYSQL_DATABASE;
	public static String MYSQL_USER;
	public static String MYSQL_PWD;

	public static String SOLR_SERVER_ADDR;

	public static String MONGO_URL;
	public static String MONGO_PORT ;
	public static String MONGO_DATABASE;
	public static String MONGO_OPTION_AUTOCONNECTRETRY;
	public static String MONGO_OPTION_ACTIVECONNECTIONCOUNT;
	public static String MONGO_OPTION_CONNECTIONTIMEOUT;
	public static String MONGO_OPTION_SOCKETTIMEOUT;
	public static String MONGO_OPTION_MAXWAITTIME;
	public static String MONGO_OPTION_THREADFORCONNECTION;
	
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
			
			G.REDIS_IP = prop.getProperty("REDIS_IP");
			G.REDIS_PORT = Integer.parseInt(prop.getProperty("REDIS_PORT"));
			G.REDIS_DB_INDEX = Integer.parseInt(prop.getProperty("REDIS_DB_INDEX"));

			G.BDB_NAME = prop.getProperty("BDB_NAME");
			G.BDB_PATH = prop.getProperty("BDB_PATH");
			
			G.MYSQL_IP = prop.getProperty("MYSQL_IP");
			G.MYSQL_PORT = Integer.parseInt(prop.getProperty("MYSQL_PORT"));
			G.MYSQL_DATABASE = prop.getProperty("MYSQL_DATABASE");
			G.MYSQL_USER = prop.getProperty("MYSQL_USER");
			G.MYSQL_PWD = prop.getProperty("MYSQL_PWD");
			
			G.SOLR_SERVER_ADDR = prop.getProperty("SOLR_SERVER_ADDR"); 
			
			G.MONGO_URL = prop.getProperty("MONGO_URL");
			G.MONGO_PORT = prop.getProperty("MONGO_PORT");
			G.MONGO_DATABASE = prop.getProperty("MONGO_DATABASE");
			G.MONGO_OPTION_AUTOCONNECTRETRY = prop.getProperty("MONGO_OPTION_AUTOCONNECTRETRY");
			G.MONGO_OPTION_ACTIVECONNECTIONCOUNT = prop.getProperty("MONGO_OPTION_ACTIVECONNECTIONCOUNT");
			G.MONGO_OPTION_CONNECTIONTIMEOUT = prop.getProperty("MONGO_OPTION_CONNECTIONTIMEOUT");
			G.MONGO_OPTION_SOCKETTIMEOUT = prop.getProperty("MONGO_OPTION_SOCKETTIMEOUT");
			G.MONGO_OPTION_MAXWAITTIME = prop.getProperty("MONGO_OPTION_MAXWAITTIME");
			G.MONGO_OPTION_THREADFORCONNECTION = prop.getProperty("MONGO_OPTION_THREADFORCONNECTION");

			//初始化log4j配置文件。
			PropertyConfigurator.configure(G.CRAWLER_HOME + "/cfg/log4j.properties"); 

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean initOracleConnection() {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			// new oracle.jdbc.driver.OracleDriver();
			ORACLE_CONN = DriverManager.getConnection(""
					+"jdbc:oracle:thin:@"
						+ORACLE_IP+":"
						+ORACLE_PORT+":"
						+ORACLE_SID, 
					ORACLE_USER, 
					ORACLE_PWD);
			Statement stmt = ORACLE_CONN.createStatement();
			ResultSet rs = stmt.executeQuery("select * from JCWIKI_ENTRY");
			while (rs.next()) {
				//System.out.println(rs.getString("ENTRY_NAME"));
				// System.out.println(rs.getInt("deptno"));
			}
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean initMysqlConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			MYSQL_CONN = DriverManager.getConnection(""
					+"jdbc:mysql://"
					+ G.MYSQL_IP
					+ ":"
					+ G.MYSQL_PORT
					+ "/"
					+ G.MYSQL_DATABASE
					+ "?rewriteBatchedStatements=true"
					+ "&characterEncoding=utf-8"
					+ "&user=" + G.MYSQL_USER
					+ "&password=" + G.MYSQL_PWD
					);
			Statement stmt = MYSQL_CONN.createStatement();
			stmt.setQueryTimeout(10);
			stmt.executeQuery(" select count(*)  num from entry_bbs ");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 初始化HttpClient
	 */
    public static boolean initHttpClient(){
   		//初始化HttpClient
		try {
			ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
			cm.setMaxTotal(G.HTTP_CLIENT_MAX_NUM);
			// 下面这个参数如果设置为1，则采集时间为平均42秒，如果设置为50，则平均采集时间为20秒
			// 这说明，这个参数是控制并发请求数量的。但是，效果并不是线性的。
			cm.setDefaultMaxPerRoute(G.HTTP_CLIENT_MAX_NUM);
			HttpParams params = new BasicHttpParams();  
			HttpConnectionParams.setConnectionTimeout(params, 8*1000);
			HttpConnectionParams.setSoTimeout(params, 8*1000);  
			HttpConnectionParams.setSocketBufferSize(params, 8192);  
			HttpClientParams.setRedirecting(params, true); 
			
			G.HTTP_CLIENT = new DefaultHttpClient(cm,params);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

    }

    
	public static void destroyOracleConnection() {
		try {
			if(ORACLE_CONN!=null) ORACLE_CONN.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	

	
}
