package com.zhangkm.spider.spider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhangkm.spider.util.RedisUtil;
import com.zhangkm.spider.util.UrlResponse;
import com.zhangkm.spider.checker.BDBUtil;
import com.zhangkm.spider.frame.G;
import com.zhangkm.spider.frame.QueueThread;
import com.zhangkm.spider.frame.RedisDAO;
import com.zhangkm.spider.frame.TaskThread;
import com.zhangkm.spider.util.Common;

public class BaiduWeiboLinkSpider extends QueueThread {
    @Autowired
    protected RedisDAO redisDAO;

	/*
	private String[] keyString = {
			"湖北","九头鸟",
			"武汉",
			"硚口区","汉阳","武昌","青山区","洪山区","东西湖区","黄陂区","江夏区","蔡甸区","汉南区","新洲区",
			"黄石",
			"黄石港区","西塞山区","下陆区","铁山区","大冶市","阳新县",
			"十堰",
			"茅箭区","张湾区","丹江口","郧县","郧西县","竹山","竹溪","房县",
			"荆州",
			"沙市","公安县","监利","江陵","石首","洪湖",
			"宜昌",
			"西陵区","伍家岗区","点军区","猇亭区","夷陵区","远安县","兴山县","秭归县","宜都市","当阳","枝江","五峰","长阳土家族",
			"襄阳",
			"襄城","樊城","襄州","南漳","谷城县","保康县","枣阳","宜城","老河口",
			"鄂州",
			"梁子湖区","华容","鄂城",
			"荆门",
			"东宝区","掇刀区","京山县","沙洋县","钟祥市",
			"黄冈",
			"黄州区","团风县","红安县","罗田县","英山县","浠水县","蕲春县","黄梅县","麻城市","武穴",
			"孝感","孝南区","孝昌县","大悟县","云梦县","应城市","安陆市","汉川市",
			"咸宁","咸安区","嘉鱼县","通城县","崇阳县","通山县","赤壁",
			"仙桃",	
			"潜江",	
			"神农架",
			"恩施","恩施","利川","建始县","巴东","咸丰县","宣恩县","来凤县","鹤峰县",
			"天门",	
			"随州","曾都","广水","随县"}; 
	*/
	private String[] keyString = {
		"广西",
		"红水河",
		"龙江河",
		"北仑河",
		"北部湾",
		"八桂",
		"西江",
		"邕江",
		"漓江",
		"壮族自治区",
		"宾阳县",
		"横县",
		"邕城",
		"象城",
		"南宁",
		"上林县",
		"马山县",
		"隆安县",
		"兴宁区",
		"西乡塘区",
		"良庆区",
		"邕宁区",
		"武鸣县",
		"柳州",
		"三江侗族自治县",
		"融水苗族自治县",
		"融安",
		"鹿寨",
		"柳城",
		"柳江",
		"柳南",
		"柳北",
		"鱼峰",
		"平乐县",
		"兴安县",
		"灌阳县",
		"荔浦县",
		"资源县",
		"永福县",
		"龙胜各族自治县",
		"恭城瑶族自治县",
		"全州县",
		"灵川县",
		"桂林",
		"象山区",
		"秀峰区",
		"叠彩区",
		"七星区",
		"雁山区",
		"阳朔县",
		"临桂县",
		"玉林",
		"梧州",
		"百色",
		"贵港",
		"钦州",
		"河池",
		"来宾",
		"北海",
		"崇左",
		"防城港",
		"贺州"};

	private int counter = 0;
	
	protected boolean beforeRun() {
		taskName = "BAIDU_WEIBO_SPIDER";
		MAX_THREAD_NUMBER =1;
		SLEEP_BEFORE_NEXT_THREAD = 10000;
		return BDBUtil.init();
	}

	protected void createThread(ExecutorService pool){
		int hour = 0;
		try {
			hour = Integer.parseInt(new SimpleDateFormat("HH").format(new Date()));//获取小时
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		if(hour>8 && hour<18){//工作时间
			//对线程间隔改为1分钟~2分钟之间随机，防止百度对抓取的封锁
			long ran = Math.round(Math.random() * 10 * 1000);
			SLEEP_BEFORE_NEXT_THREAD = new Long(10 * 1000 + ran).intValue();
		}else{//夜间
			//对线程间隔改为2分钟~4分钟之间随机，防止百度对抓取的封锁
			long ran = Math.round(Math.random() * 30 * 1000);
			SLEEP_BEFORE_NEXT_THREAD = new Long(30 * 1000 + ran).intValue();
		}

		pool.execute(new UrlSpiderThread());
		counter++;
	}

	public class UrlSpiderThread extends TaskThread{
        @Override
        protected void getDataFromQueueMap() {
            fromQueueMap = redisDAO.rightPop(QUEUE_NAME_FROM);
        }           

	    public void run() {
			if(!initQueue()) return;
			doMainJob();
		}

		protected boolean initQueue() {
			super.QUEUE_NAME_TO = G.QUEUE_BASIC_FILTER;
			return true;
		}

		protected void doMainJob() {
			String baiduUrl = "http://www.baidu.com/s?rtt=2&tn=baiduwb&rn=20&cl=2&wd=";
			baiduUrl = baiduUrl + keyString[counter%keyString.length];
			System.out.println(baiduUrl);
			fromQueueMap = new HashMap<String, String>();
			fromQueueMap.put("group", "MBLOG");
			fromQueueMap.put("channel_url", baiduUrl);
			fromQueueMap.put("encoding", "utf-8");

			List<Map<String,String>> list = getPageUrlList(fromQueueMap);
			if (list != null && list.size() > 0) {
				for (Map<String,String> map : list) {
					fromQueueMap.putAll(map);
					RedisUtil.pushListData(QUEUE_NAME_TO, fromQueueMap);
					logInfo();
				}
			}
		}
		
		/**
		 * 
		 * @param entryMap
		 * @return
		 */
	    private List<Map<String,String>> getPageUrlList(Map<String,String> entryMap){
	    	ArrayList<Map<String,String>> retList = new ArrayList<Map<String,String>>();
	    	if(entryMap==null || entryMap.isEmpty()) return retList;
	    	
			String channel_url = entryMap.get("channel_url");

			if(channel_url==null 
					|| channel_url.trim().equals("")){
				return new ArrayList<Map<String,String>>();
			}

			// 访问该页面，获取帖子列表
			UrlResponse urlResponse = Common.getUrlResponseTimes(channel_url,null);
			if(urlResponse==null) return retList;
			
			String bodyString = urlResponse.getResponseBody();

			if(bodyString==null || bodyString.trim().equals("")) return retList;
			
			Document doc = Jsoup.parse(bodyString);
			Elements weibo = null; 
			Elements li = null; 
			Elements detail = null; 
			Elements info = null; 
			Elements author = null;
			Elements a = null;
			String sAuthor = null;
			String sLink = null;
			String slid = null;
			String sWeb = null;
			String sContent = null;
			try {
				//links = doc.select("a.weibo_all");
				weibo = doc.select("#weibo");
				if(weibo!=null&&weibo.size()>0){
					li = weibo.get(0).select("li");
					if(li!=null&&li.size()>0){
						for(Element ee:li){
							String weiboHtml = ee.html();
							detail = ee.select("div.weibo_detail > p");
							if(detail!=null && detail.size()>0){
								sContent = detail.get(0).text();
								if(sContent==null || sContent.trim().equals("")) continue;
								author = detail.get(0).select("a");
								if(author!=null && author.size()>0){
									sAuthor = author.get(0).text();
								}
							}
							
							info = ee.select("div.weibo_detail > div > div.m");
							if(info!=null && info.size()>0){
								a = info.select("a"); 
								if(a!=null&&a.size()>1){
									sLink = a.get(0).attr("href").trim();
									if(sLink==null || sLink.trim().equals("")) continue;
									slid = DigestUtils.md5Hex(sLink);
									int retNum = BDBUtil.put(sLink);
									if(retNum != BDBUtil.BDB_SUCCESS) continue;
									sWeb = a.get(1).text().trim();
								}
							}
							
							Map<String,String> map = new HashMap<String,String>();
							map.put("title", sWeb);
							map.put("content", sContent);
							map.put("content_no_tags", sContent);
							map.put("author", sAuthor);
							map.put("website", sWeb);
							map.put("channel", sWeb);
							map.put("url", sLink);
							map.put("slid", slid);
							map.put("pubtime", ""+System.currentTimeMillis());
							map.put("html", weiboHtml);
							
							retList.add(map);
						}
					}
				}

			} catch (Exception e) {
				return retList;
			}

			return retList;	
		}

	}
    
	
	public static void main(String[] args){
		String sLink = "http://bbs.cnhubei.com/thread-3377752-1-1.html";
		System.out.println(sLink.matches("(.*)thread-\\d{7}-1-1.html$"));

	}
}
