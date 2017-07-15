package com.zhangkm.demo.base.DBUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Apache Commons DbUtils 官方 Demo
 * DbUtils: JDBC Utility Component Examples
 * http://commons.apache.org/proper/commons-dbutils/examples.html
 * 
 * @ClassName: DBUtils
 * @Description: TODO
 *
 */
public class DBUtils {
	private static String DB_SERVER_IP = "127.0.0.1";
	private static String DB_SERVER_PORT = "3306";
	private static String DB_DATABASE_NAME = "test";
	private static String DB_USER_NAME = "root";
	private static String DB_PASSWORD = "888888";

	private static java.sql.Connection dbConn;
	
	private DBUtils(){}
	
	public static Connection getDBConn() {
		try {
			if(dbConn!=null && !dbConn.isClosed()) return dbConn;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection(""
					+"jdbc:mysql://"
					+DB_SERVER_IP+":"+DB_SERVER_PORT+"/"+DB_DATABASE_NAME
					+"?user="+DB_USER_NAME
					+"&password="+DB_PASSWORD
					+"&characterEncoding=utf-8"
					+"&rewriteBatchedStatements=true");
			System.out.println("初始化MySql数据库完毕...");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dbConn;
	}
	
	public static void releaseConn(){
		try {
			dbConn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void query(){

		ResultSetHandler<List<Map<String,Object>>> handler = new ResultSetHandler<List<Map<String,Object>>>() {
            public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
                List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
                while(rs.next()){
                    Map<String,Object> map = new HashMap<String,Object>();
                    ResultSetMetaData meta = rs.getMetaData();
                    int cols = meta.getColumnCount();
                    for (int i = 0; i < cols; i++) {
                        map.put(meta.getColumnName(i+1), rs.getObject(i + 1).toString());
                    }
                    list.add(map);
                }
                return list;
            }
		};

		// No DataSource so we must handle Connections manually
		QueryRunner run = new QueryRunner();
		Connection conn = getDBConn(); // open a connection
		try{
		    List<Map<String,Object>> resultList = run.query(conn, "SELECT * FROM test WHERE type=? ", handler, 0);
			if(resultList==null || resultList.size()==0) return;
			System.out.println("result.length: "+resultList.size());

			for(Map<String,Object> map : resultList){
				System.out.println(map.toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    // Use this helper method so we don't have to check for null
		    try {
				DbUtils.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}  
		}
	}
	
	public static void main(String[] args){
		query();
	}
}
