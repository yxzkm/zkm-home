package com.zhangkm.spider.writer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.zhangkm.spider.frame.G;

public class OracleWriterUserField{
	public static void main(String[] args) {
		G.initParameters();
		G.initOracleConnection();

		String sql = "";
		Statement stmt = null;

		try {
			try {
				stmt = G.ORACLE_CONN.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}

			sql = " select user_id from user_table ";
			ResultSet rs = null;
			List<Integer> list = new ArrayList<Integer>();
			try {
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					list.add(rs.getInt("user_id"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			} finally {
				try {
					if (rs != null)
						rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			for(Integer userId:list){
				sql = "" 
						+ " select entry_class_id "
						+ " from (select entry_class_id "
						+ " from JCWIKI_ENTRY_CLASS "
						+ " where entry_class_id > 10 "
						+ " order by dbms_random.random)" 
						+ " where rownum < 2 ";


				rs = null;
				List<Integer> list1 = new ArrayList<Integer>();
				try {
					rs = stmt.executeQuery(sql);
					while (rs.next()) {
						list1.add(rs.getInt("entry_class_id"));
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				} finally {
					try {
						if (rs != null)
							rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				for(Integer entryClassId:list1){

					sql = "" 
							+ "\n insert into user_field "
							+ "(user_field_id,user_id,class_id)"
							+ " values (jckm_id.nextval, "+userId+", "+ entryClassId+")" ;
					System.out.println(sql);
					try { 
						stmt.execute(sql); 
					} catch (SQLException e) {
						e.printStackTrace(); 
					}

				}
			
			}


		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return;
	}

}
