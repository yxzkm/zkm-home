package com.zhangkm.demo.ssoserver.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class GlobalSettings {

    private String globalVar;
    private String appId;
    private String appSecret;
    
    private String appName;
    

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getGlobalVar() {
		return globalVar;
	}

	public void setGlobalVar(String globalVar) {
		this.globalVar = globalVar;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}  

	
}