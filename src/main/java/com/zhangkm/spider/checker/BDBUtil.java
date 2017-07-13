package com.zhangkm.spider.checker;

import java.io.File;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import com.zhangkm.spider.frame.G;

public class BDBUtil {

	private static Environment dbEnv = null;
	private static Database BBS_URL_DB = null; //Micro Blog queue
	
	public static final int BDB_SUCCESS = 1;  // 写入成功，说明是一个新的url
	public static final int BDB_KEYEXIST = 0;  // 主键重复，写入失败，说明url已经存在
	public static final int BDB_ERROR = -1;  // url不合法或其他错误
	
	
	public static boolean init(){
		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);
			dbEnv = new Environment(new File(G.BDB_PATH), envConfig);

			DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setAllowCreate(true);
			dbConfig.setSortedDuplicates(false);
			
			BBS_URL_DB = dbEnv.openDatabase(null,G.BDB_NAME, dbConfig); 

			return true;
		} catch (DatabaseException dbe) {
			dbe.printStackTrace();	
			return false;
		}
		
	}

	public static void sync(){
		dbEnv.sync();
	}
	
	public static void close(){
		try {
			if (BBS_URL_DB != null) BBS_URL_DB.close();
			if (dbEnv != null) dbEnv.close();
			
		} catch (DatabaseException dbe) {}
	}

	public static int put(String url){
		DatabaseEntry theKey = null;
		try {
			theKey = new DatabaseEntry(url.getBytes("UTF-8"));
			DatabaseEntry theData = new DatabaseEntry("".getBytes());
			OperationStatus os = BBS_URL_DB.putNoOverwrite(null, theKey, theData);
			dbEnv.sync();
			if(os.equals(OperationStatus.SUCCESS)) return BDB_SUCCESS;
			if(os.equals(OperationStatus.KEYEXIST)) return BDB_KEYEXIST;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return BDB_ERROR;
	}
	
	public static void main(String[] args) {

	}

}
