package com.zhangkm.demo.ssoserver.service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service("ssoServerService")
public class SSOServerService {
	private static Logger logger = Logger.getLogger(SSOServerService.class);  

	private Map<String,String> TICKET_MAP = new HashMap<String,String>();

	//已登录用户map，list是已经登录到的应用系统List
	private Map<String,List<String>> LOGIN_MAP = new HashMap<String,List<String>>();

	public boolean verifySign(
			String appId,
			String nonce,
			String timestamp,
			String sign){

		//TODO: 判断appid是否合法，判断nonce是否合法，判断timestamp是否超时，
		//TODO: 查询该appid所对应的appsecret，然后计算签名，比较sign是否一致
		
		//logger.info("appId,nonce,timestamp,sign分别为：" + appId+" "+nonce+" "+timestamp+" "+sign );
		try {
			String str = appId + nonce + timestamp + getAppSecret(appId);
			String mysign = DigestUtils.sha256Hex(str);
			//logger.info("str："+str);
			//logger.info("签名："+mysign);
			if(sign.equalsIgnoreCase(mysign)) return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//logger.info("签名错误");
		return false;
	}
	
	public List<String> getAppLoginList(String suid){
		List<String> appList = new ArrayList<String>();
		if(!LOGIN_MAP.containsKey(suid)) return appList;
		appList = LOGIN_MAP.get(suid);
		if(appList == null) return new ArrayList<String>();
		return appList;
	}
	
	public void addAppToLoginList(String suid,String appid){
		if(!LOGIN_MAP.containsKey(suid)){
			List<String> appList = new ArrayList<String>();
			appList.add(appid);
			LOGIN_MAP.put(suid, appList);
			logger.info("[addAppToLoginList]appList.size="+appList.size());
		}else{
			List<String> appList = LOGIN_MAP.get(suid);
			if(appList == null || appList.size()==0) appList = new ArrayList<String>();
			appList.add(appid);
			LOGIN_MAP.put(suid, appList);
			logger.info("[addAppToLoginList]appList.size="+appList.size());
		} 
		
	}
	public boolean removeAppFromLoginList(String suid,String appid){
		if(!LOGIN_MAP.containsKey(suid)) return true;
		List<String> appList = LOGIN_MAP.get(suid);
		if(appList == null || appList.size()==0){
			LOGIN_MAP.remove(suid);
			return true;
		}else{
			if(appList.contains(appid)) appList.remove(appid);
			if(appList.size()==0) LOGIN_MAP.remove(suid);
		} 
		return true;
	}
	
	public String sign(
			String appId,
			String nonce,
			String timestamp){

		String str = appId + nonce + timestamp + getAppSecret(appId);
		return DigestUtils.sha256Hex(str);
	}

	public String getAppCallBackUrl(String appid){
		if(appid.equalsIgnoreCase("appid1")) return "http://news.zhangkm.com/ssoCallback";
		return "http://blog.zhangkm.com/ssoCallback";
	}

	public String getAppSsoLogoutUrl(String appid){
		if(appid.equalsIgnoreCase("appid1")) return "http://news.zhangkm.com/logout";
		return "http://blog.zhangkm.com/logout";
	}

	public String getAppHomePage(String appid){
		if(appid.equalsIgnoreCase("appid1")) return "http://news.zhangkm.com/";
		return "http://blog.zhangkm.com/";
	}

	public String getAppSecret(String appid){
		if(appid.equalsIgnoreCase("appid1")) return "e1ad22699c752269a221290cde8c650a";
		return "e1ad22699c752269a221290cde8c650a";
	}

	public String produceSsoTicket(String appid){
		String ticket = sha1(""+System.currentTimeMillis());
		TICKET_MAP.put(ticket,appid);
		return ticket;
	}

	public boolean checkTicket(String ticket){
		if(TICKET_MAP.containsKey(ticket)) return true;
		return false;
	}
	
	public Map<String,String> getUserInfoByTicket(String ticket){
		Map<String,String> map = new HashMap<String,String>();
		map.put("suid", "zkm");
		TICKET_MAP.remove(ticket);
		return map;
	}
	
	private String sha1(String str) {
	    try {
	        // 生成一个MD5加密计算摘要
	        MessageDigest md = MessageDigest.getInstance("SHA1");
	        // 计算md5函数
	        md.update(str.getBytes());
	        // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
	        // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
	        return new BigInteger(1, md.digest()).toString(16);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "";
	    }
	}


}
