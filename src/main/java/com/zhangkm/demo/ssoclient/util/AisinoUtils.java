package com.zhangkm.demo.ssoclient.util;

import org.apache.commons.codec.digest.DigestUtils;

public class AisinoUtils {
	/**
	 * 生成签名(将传入各个字符串参数简单排列，SHA256Hex摘要算法进行加密)
	 * @param appId
	 * @param timestamp
	 * @param nonce
	 * @param appSecret
	 * @return
	 */
	public static String sign(String appId, String nonce, String timestamp,String appSecret){
		try {
			String str = appId + nonce + timestamp + appSecret; 
			String sign = DigestUtils.sha256Hex(str);
			return sign;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

}
