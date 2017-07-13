package com.zhangkm.spider.writer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.zhangkm.spider.frame.G;

public class OracleWriterUpdateTest {

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

			sql = "" 
					+ " select " 
					+ "   SEQUENCE_ID, " 
					+ "   ENTRY_ID, " 
					+ "   VERSION_ID " 
					+ " from JCWIKI_ENTRY_SEARCH ";
			ResultSet rs = null;
			List<Integer> list = new ArrayList<Integer>();
			try {
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					list.add(rs.getInt("SEQUENCE_ID"));
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
			
			int icount = 0;
			for(Integer k:list){
				icount++;
				String retString = "";
				
				for (int i = 0; i < icount % 3 + 1; i++) {
					Random r = new Random();
					int cid = r.nextInt(34) + 5;
					if (cid < 12) {
						retString = retString + cid + "|1|";
					} else if (cid < 16) {
						retString = retString + cid + "|2|";
					} else if (cid < 32) {
						retString = retString + cid + "|3|";
					} else {
						retString = retString + cid + "|4|";
					}
				}

				sql = "" 
						+ "\n update JCWIKI_ENTRY_SEARCH" 
						+ "\n set ENTRY_CLASSES_IDS = '" + retString + "' " 
						+ "\n where SEQUENCE_ID = " + k;
				System.out.println(sql);
				try { 
					stmt.execute(sql); 
				} catch (SQLException e) {
					e.printStackTrace(); 
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
