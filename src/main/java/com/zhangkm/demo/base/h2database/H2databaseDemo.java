package com.zhangkm.demo.base.h2database;

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
 * H2database 官方文档
 * H2Database Quickstart
 * http://www.h2database.com/html/quickstart.html
 * 
 * @ClassName: H2databaseDemo
 * @Description: TODO
 *
 */
public final class H2databaseDemo {
    private static java.sql.Connection dbConn;
    private H2databaseDemo(){}
    public static Connection getDBConn() {
        try {
            if(dbConn!=null && !dbConn.isClosed()) return dbConn;
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        
        try {
            dbConn = DriverManager.getConnection(
                    "jdbc:h2:/C:/zkm/files4test/h2database/h2db", 
                    "sa", "");
            System.out.println("初始化H2database数据库完毕...");
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

    public static void query(String tableName){
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

        QueryRunner run = new QueryRunner();
        Connection conn = getDBConn(); // open a connection
        try{
            List<Map<String,Object>> resultList = run.query(
                    conn, "SELECT * FROM "+tableName+" WHERE id>? ", handler, 0);

            for(Map<String,Object> map : resultList){
                System.out.println(map.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                DbUtils.close(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }  
        }
    }
    
    public static void dropTable(String tableName){
        QueryRunner run = new QueryRunner();
        Connection conn = getDBConn(); // open a connection
        try {
            int nums = run.update( conn,
                    "DROP TABLE IF EXISTS "+tableName+";");
            System.out.println("删除表： "+nums);
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public static void createTable(String tableName){
        QueryRunner run = new QueryRunner();
        Connection conn = getDBConn(); // open a connection
        try {
            int nums = run.update( conn,
                    "CREATE TABLE "+tableName+" (ID INT PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR(255));");
            System.out.println("建表： "+nums);
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public static void insert(String tableName){
        QueryRunner run = new QueryRunner();
        Connection conn = getDBConn(); // open a connection
        try {
            int inserts = run.update( conn,
                    "INSERT INTO "+tableName+" (NAME) VALUES (?)",
                    "哈哈" );
            System.out.println("插入条数： "+inserts);
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public static void update(String tableName){
        QueryRunner run = new QueryRunner();
        Connection conn = getDBConn(); // open a connection
        try {
            int updates = run.update( conn,
                    "UPDATE "+tableName+" SET NAME=? WHERE NAME=?",
                    "哇哈哈", "哈哈" );
            System.out.println("更新条数： "+updates);
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public static void main(String[] args){
        String tableName = "test";
        dropTable(tableName);
        createTable(tableName);
        insert(tableName);
        insert(tableName);
        insert(tableName);
        update(tableName);
        query(tableName);
    }

}

