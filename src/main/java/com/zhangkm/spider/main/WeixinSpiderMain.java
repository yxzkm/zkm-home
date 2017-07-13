package com.zhangkm.spider.main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.zhangkm.spider.frame.G;

public class WeixinSpiderMain {

	public static Connection MYSQL_CONN = null;
	public static final String URL_SOGOU_ENTRY = "http://weixin.sogou.com/weixin?type=1&ie=utf8&_sug_=n&_sug_type_=&query=";
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36";

	public static void main(String[] args) {
		initParameters();
		if(!initMysqlConnection()) {
			System.out.println("[数据库初始化失败]");
			return;	
		}
		
		for(int i=1;i<Integer.MAX_VALUE;i++){

			System.out.println("\n\n\n\n\n");
			System.out.println("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"]"
					+ "[现在开始启动第 "+i+" 次抓取任务]");
			System.out.println("\n\n\n\n\n");

			doMainJob();

			try {
				Thread.sleep(8*60*60*1000); //先休息8小时，因为目前生产服务器被封ip
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}

	public static void doMainJob() {
		
		//从数据库中，将微信公众号列表取出
		List<Map<String,Object>> list = getWeixinEntryList();  

		//循环微信公众号列表
		for(Map<String,Object> entryMap : list){
			
			String wxid = (String)entryMap.get("wxid");
			String wxnickname = (String)entryMap.get("wxnickname");
			System.out.println("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"]"
					+ "[抓取目标]["+wxnickname+"]["+wxid+"]");

			//通过搜狗微信搜索，查询微信公众号，搜狗的页面会显示该公众号在腾讯的URL地址
			String weixinEntryUrl = getWeixinEntryUrl(wxid);
			if(weixinEntryUrl==null || weixinEntryUrl.trim().equals("")) {
				System.out.println("[无法从搜狗微信搜索获取最新文章列表入口地址:]["+weixinEntryUrl+"]");
				sleepN(80);
				continue;	
			}

			//根据微信公众号最新文章列表URL（腾讯），抓取该页面，获取文章具体URL和其他信息
			List<Map<String,Object>> newsInfoList = getWeixinNewsInfoList(weixinEntryUrl);
			if(newsInfoList==null || newsInfoList.size()==0){
				System.out.println("[无法获取最新文章列表详细信息:]["+weixinEntryUrl+"]");
				sleepN(80);
				continue;
			}
			
			int dupnum = 0;
			int newnum = 0;
			//遍历最新文章列表
			for(Map<String,Object> map : newsInfoList){

				String title = (String) map.get("title");
				title = StringEscapeUtils.unescapeHtml(title);
				
				String pageUrl = (String) map.get("url");
				pageUrl = StringEscapeUtils.unescapeJava(pageUrl);
				
				String cover = (String) map.get("cover");
				cover = StringEscapeUtils.unescapeJava(cover);
				
				long pubtime = (Long) map.get("pubtime");
				int fileid = (Integer) map.get("fileid");
				
				//根据fileid，查询数据库，判断文章是否已经被抓取过
				if(isFileAlreadyExist(fileid)){
					dupnum++;
					continue;
				}

				sleepN(10);

				//将新闻头图下载
				String fullLocalFileName = downloadCoverImg(wxid,fileid,cover);
				//将新闻头图上传至七牛
				uploadQiniu(fullLocalFileName,fileid+"");

				String content = getWeixinNewsPageContent(wxid,pageUrl,weixinEntryUrl);
				if(content==null || content.trim().equals("")){
					System.out.println("[获取新文章正文内容时失败: ]["+pageUrl+"]");
					continue;
				}
				System.out.println("[新文章]["+fileid+"][内容长度]["+content.length()+"]["+title+"]");
					
				Map<String,Object> pageInfoMap = new HashMap<>();
				pageInfoMap.put("fileid", fileid);
				pageInfoMap.put("pubtime", pubtime);
				pageInfoMap.put("title", title);
				pageInfoMap.put("headimg", "http://img.techjc.cc/"+fileid);
				pageInfoMap.put("mediatype", (Integer)entryMap.get("mediatype"));
				pageInfoMap.put("mediaid", (Integer)entryMap.get("mediaid"));
				pageInfoMap.put("wxnickname", (String)entryMap.get("wxnickname"));
				
				int newsId = insertPageInfoToNews(pageInfoMap);
				if(newsId>0){
					insertPageContentToContentLib(newsId,content);
					insertPageFileidToWXSpiderPage(wxid,fileid);
				}
				newnum++;
			}
			System.out.println("[共抓取到 "+ newsInfoList.size() +" 篇文章][新文章 " + newnum + " 篇][重复文章 " + dupnum + "篇]");
			sleepN(80);
		}
	}
	
	private static void sleepN(int sec){
		int num = new Random().nextInt(20)+sec;
		System.out.println("\n");
		System.out.print("等待"+num+"秒:");
		for(int i=0; i<num; i++){
			try {
				System.out.print(".");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("\n");
	}
	
	public static int insertPageInfoToNews(Map<String,Object> pageInfoMap) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			int mediaType = (Integer)pageInfoMap.get("mediatype");
			String channelId = "0";
			if(mediaType==0) channelId=G.JCRB_CHANNEL_ID;

			String sql0 = ""
					+ " INSERT INTO news ( "
					+ " 	contentid,fromwhere,fromwhereid,source,title,savetime,pubtime,headimg1, "
					+ " 	format,channelid,editorid, "
					+ " 	discussflag,pubflag,topflag,focusflag"
					+ " ) VALUES "
					+ " ("
					+ "		?,?,?,?,?,?,?,?, "
					+ "		0,"+channelId+",1, "
					+ "		1,1,0,0 "
					+ ")"; 
			ps = MYSQL_CONN.prepareStatement(sql0,PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setInt(1, (Integer)pageInfoMap.get("fileid"));
			ps.setInt(2, mediaType);
			ps.setInt(3, (Integer)pageInfoMap.get("mediaid"));
			ps.setString(4, (String)pageInfoMap.get("wxnickname"));
			ps.setString(5, (String)pageInfoMap.get("title"));
			ps.setLong(6, System.currentTimeMillis());
			ps.setLong(7, (Long)pageInfoMap.get("pubtime"));
			ps.setString(8, (String)pageInfoMap.get("headimg"));

			int num = ps.executeUpdate();
			if(num==0) return 0;
			rs = ps.getGeneratedKeys();
			if(rs.next()) return rs.getInt(1);
			return 0;
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				if(rs!=null) rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if(ps!=null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	public static int insertPageContentToContentLib(int newsId, String content) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			
			String sql1 = ""
					+ " INSERT INTO contentlib "
					+ " (id,content) "
					+ " VALUES "
					+ " (?,?)"; 
			ps = MYSQL_CONN.prepareStatement(sql1);
			ps.setInt(1, newsId);
			ps.setString(2, content);
			return ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				if(rs!=null) rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if(ps!=null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	public static int insertPageFileidToWXSpiderPage(String wxid, int fileid) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql2 = ""
					+ " INSERT INTO wxspider_page "
					+ " (wxid,fileid) "
					+ " VALUES "
					+ " (?,?)"; 
			ps = MYSQL_CONN.prepareStatement(sql2);
			ps.setString(1, wxid);
			ps.setInt(2, fileid);
			return ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				if(rs!=null) rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if(ps!=null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	public static String downloadCoverImg(String wxid, int fileid, String cover){
		String localPath = G.LOCAL_FILE_PATH + "/headimg/" + wxid;
		if(!mkdir(localPath)) return null;
		
		String localFileName = localPath + "/" + fileid + ".jpg";

		try {
			Response resultImageResponse = Jsoup.connect(cover).ignoreContentType(true).execute();
			FileOutputStream out = (new FileOutputStream(new java.io.File(localFileName)));
			out.write(resultImageResponse.bodyAsBytes());           
			out.close();
			return localFileName;
		} catch (Exception e) {
			System.out.println("[获取文章头图失败：]["+cover+"]");
		}

		return null;
	}

	public static String downloadFileImg(String wxid, String uuid, String imgUrl){
		String localPath = G.LOCAL_FILE_PATH + "/fileimg/" + wxid;
		if(!mkdir(localPath)) return null;
		
		String localFileName = localPath + "/" + uuid + ".jpg";

		try {
			Response resultImageResponse = Jsoup.connect(imgUrl).ignoreContentType(true).execute();
			FileOutputStream out = (new FileOutputStream(new java.io.File(localFileName)));
			out.write(resultImageResponse.bodyAsBytes());           
			out.close();
			return localFileName;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static boolean mkdir(String destDirName){
	
	    File dir = new File(destDirName);  
	    if (dir.exists()) return true;
	    
	    //创建目录  
	    if (dir.mkdirs()) {  
			System.out.println("[创建目录" + destDirName + "成功！]");
	        return true;  
	    } else {  
			System.out.println("[创建目录" + destDirName + "失败！]");
	        return false;  
	    }  
	}

	public static List<Map<String,Object>> getWeixinEntryList() {
		List<Map<String,Object>> list = new ArrayList<>();
		ResultSet rs = null;
		Statement stmt = null;
		try {
			stmt = MYSQL_CONN.createStatement();
			stmt.setQueryTimeout(10);
			rs = stmt.executeQuery(""
					+ " select wxid,wxnickname,mediatype,mediaid "
					+ " from wxspider_entry "
					+ " where 1=1 "
					//+ " and wxid='lawreaders'"
					+ " order by mediatype asc, mediaid asc ");
			
			while(rs.next()){
				Map<String,Object> map = new HashMap<>();
				map.put("wxid", rs.getString(1));
				map.put("wxnickname", rs.getString(2));
				map.put("mediatype", rs.getInt(3));
				map.put("mediaid", rs.getInt(4));
				list.add(map);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				if(rs!=null) rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if(stmt!=null) stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return new ArrayList<>();
	}

	public static boolean isFileAlreadyExist(int fileid) {
		ResultSet rs = null;
		Statement stmt = null;
		try {
			stmt = MYSQL_CONN.createStatement();
			stmt.setQueryTimeout(10);
			rs = stmt.executeQuery(" select count(1) as num from wxspider_page where fileid = "+fileid);
			
			while(rs.next()){
				int num = rs.getInt(1);
				if(num>0) return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("[数据库访问失败！]");

		} finally{
			try {
				if(rs!=null) rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if(stmt!=null) stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * 通过搜狗微信搜索，查询微信公众号，搜狗的页面会显示该公众号在腾讯的URL地址
	 * @param weixinId 微信号
	 * @return
	 */
	public static String getWeixinEntryUrl(String weixinId) {
		Document doc;
		try {
			String url = URL_SOGOU_ENTRY + weixinId;
			doc = Jsoup.connect(url).get();
			if(doc==null){
				System.out.println("[获取微信公号入口地址失败：DOC文档为空]");
				return null;
			}

			String html = doc.html();
			if(html!=null && html.trim().length()>0){
				if(html.indexOf("恶意抓取")!=-1){
					System.out.println("[抓取过于频繁，被搜狗微信搜索屏蔽！]");
					return null;
				}
			}else{
				System.out.println("[获取微信公号入口地址失败：HTML文档为空]");
				return null;
			}
			
			Elements els = doc.select("div[href]");
			if(els!=null && els.size()>0){
				String weixinEntryUrl = els.get(0).attr("href");
				return weixinEntryUrl;
			}else{
				System.out.println("[获取微信公号入口地址失败：DOM节点无法解析]");
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 抓取腾讯页面，获取某公众号最新文章地址列表和头图地址列表
	 * @param weixinEntryUrl
	 * @return
	 */
	public static List<Map<String,Object>> getWeixinNewsInfoList(String weixinEntryUrl) {
		Document doc;
		try {
			doc = Jsoup.connect(weixinEntryUrl)
					.referrer(URL_SOGOU_ENTRY)
					.userAgent(USER_AGENT)
					.get();     
			
//			System.out.println("doc====================================\n"+doc.html());

			Elements els = doc.select("script");
			
			if(els==null || els.size()==0) return new ArrayList<>();

			Element el = els.last();
			String script = el.html();
			String[] arr = script.split("\n");

			String json = "";
			for (String s : arr) {
				if (s.contains("var msgList =")) {
					json = s;
					break;
				}
			}

			json = json.replaceAll("var msgList = '", "");
			json = json.trim();
			if (json.endsWith("';")) {
				json = json.substring(0, json.length() - 2);
			}

			JSONObject j = new JSONObject();
			try {
				j = JSONObject.fromObject(json);
			} catch (Exception e) {
				//e.printStackTrace();
				try {
					json = StringEscapeUtils.unescapeHtml(json);
					j = JSONObject.fromObject(json);
				} catch (Exception e1) {
					//e1.printStackTrace();
					try {
						json = StringEscapeUtils.unescapeXml(json);
						j = JSONObject.fromObject(json);
					} catch (Exception e2) {
						//e2.printStackTrace();
						System.out.println("[无法解析JSON]["+weixinEntryUrl+"]");
						return new ArrayList<>();
					}
				}
			}
			//json = json.replaceAll("\\\\", "");
			
			JSONArray jsonArray = j.getJSONArray("list");
			
			List<Map<String,Object>> list = new ArrayList<>();
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jo = (JSONObject) jsonArray.get(i);
				Map<String,Object> map = new HashMap<>();

				JSONObject jo1 = (JSONObject) jo.get("comm_msg_info");
				long pubtime = (Integer) jo1.get("datetime");

				JSONObject jo2 = (JSONObject) jo.get("app_msg_ext_info");
				String url = "http://mp.weixin.qq.com" + (String) jo2.get("content_url");
				int fileid = (Integer) jo2.get("fileid");
				String title = (String) jo2.get("title");
				String cover = (String) jo2.get("cover");
				
				map.put("pubtime", pubtime * 1000);
				map.put("fileid", fileid);
				map.put("url", url);
				map.put("title", title);
				map.put("cover", cover);
				
				list.add(map);
			}

			return list;
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	/**
	 * 抓取文章详情
	 * @param wxid
	 * @param url
	 * @return
	 */
	public static String getWeixinNewsPageContent(String wxid, String pageContentUrl, String pageListUrl) {
		Document doc;
		try {
			
			pageContentUrl = StringEscapeUtils.unescapeHtml(pageContentUrl);

			doc = Jsoup.connect(pageContentUrl)
					.referrer(pageListUrl)
					.userAgent(USER_AGENT).get();
			Element e = doc.getElementById("img-content");
			
			Whitelist whitelist =  Whitelist.none();
			whitelist.addTags("img","p");
			whitelist.addAttributes("img","data-src");
			String clean = Jsoup.clean(e.html(),whitelist);
			clean = clean.replaceAll(" data-src=", " src=");
			clean = clean.replaceAll("<img />", "");
			
			doc = Jsoup.parse(clean);
			List<Node> list = doc.childNodes();
			
			String t = "";
			for(Node node : list.get(0).childNodes().get(1).childNodes()){
				if("#text".equals(node.nodeName())){
					t=t+"<p>"+node.outerHtml()+"</p>\n";
				}else{
					t=t+node.outerHtml()+"\n";
				}
			}
			
			doc = Jsoup.parse(t);
			Elements els = doc.select("img[src]");
			if(els!=null && els.size()>0){
				for(Element el : els){
					String uuid = UUID.randomUUID().toString();
					String imgUrl = el.attr("src");
					if(imgUrl!=null && !imgUrl.trim().equals("")){
						String fullLocalFileName = downloadFileImg(wxid,uuid,imgUrl);
						uploadQiniu(fullLocalFileName,uuid);
						el.attr("src","http://img.techjc.cc/"+uuid);
					}
				}
			}

			return doc.select("body").html();

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}


	}
	
	public static void uploadQiniu(String fullLocalFileName, String uuid) {

		if(fullLocalFileName==null || fullLocalFileName.trim().equals("")) return;
		
		// 设置好账号的ACCESS_KEY和SECRET_KEY
		String ACCESS_KEY = "Wx1iS8BHdhqEsHyUkD0-PxbMiA4f8AGgCyJ0G4xF";
		String SECRET_KEY = "MfuF7VFb9okwwXSuIqxOV0Zc4Lbv-4jlTGus7QNZ";
		// 要上传的空间
		String bucketname = "zycx";

		// 密钥配置
		Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
		// 创建上传对象
		UploadManager uploadManager = new UploadManager();
		
		try {
			com.qiniu.http.Response res = uploadManager.put(fullLocalFileName, uuid, auth.uploadToken(bucketname));
		} catch (QiniuException e) {
			com.qiniu.http.Response r = e.response;
			// 请求失败时打印的异常的信息
			System.out.println(r.toString());
			try {
				// 响应的文本信息
				System.out.println(r.bodyString());
			} catch (QiniuException e1) {
				// ignore
			}
		}
	}

	
	/**
	 * 用于初始化各项公共参数
	 */
	public static void initParameters(){
	     Properties prop = null;
	       try {
			Properties ps = System.getProperties();
			// ps.list(System.out);
			G.CRAWLER_HOME = ps.getProperty("user.dir");
			System.out.println("hello G.CRAWLER_HOME:"+G.CRAWLER_HOME);

			BufferedInputStream inBuff = new BufferedInputStream(
					new FileInputStream(G.CRAWLER_HOME + "/cfg/parameters.properties"));
			prop = new Properties();
			prop.load(inBuff);
			inBuff.close();

			G.MYSQL_IP = prop.getProperty("MYSQL_IP");
			G.MYSQL_PORT = Integer.parseInt(prop.getProperty("MYSQL_PORT"));
			G.MYSQL_DATABASE = prop.getProperty("MYSQL_DATABASE");
			G.MYSQL_USER = prop.getProperty("MYSQL_USER");
			G.MYSQL_PWD = prop.getProperty("MYSQL_PWD");

			G.LOCAL_FILE_PATH = prop.getProperty("LOCAL_FILE_PATH");
			G.JCRB_CHANNEL_ID = prop.getProperty("JCRB_CHANNEL_ID");
			
			//初始化log4j配置文件。
			PropertyConfigurator.configure(G.CRAWLER_HOME + "/cfg/log4j.properties"); 

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean initMysqlConnection() {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String dbString = ""
					+"jdbc:mysql://"
					+ G.MYSQL_IP
					+ ":"
					+ G.MYSQL_PORT
					+ "/"
					+ G.MYSQL_DATABASE
					+ "?rewriteBatchedStatements=true"
					+ "&characterEncoding=utf-8"
					+ "&user=" + G.MYSQL_USER
					+ "&password=" + G.MYSQL_PWD;
			System.out.println(dbString);
			MYSQL_CONN = DriverManager.getConnection(dbString);
			stmt = MYSQL_CONN.createStatement();
			stmt.setQueryTimeout(10);
			rs = stmt.executeQuery(" select count(*) as num from wxspider_entry ");
			while(rs.next()){
				System.out.println(rs.getString(1));
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if(rs!=null) rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if(stmt!=null) stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}


}
