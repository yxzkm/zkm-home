package com.zhangkm.demo.ssoclient.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource("classpath:config/application-sso-client.properties")
@Component
@ConfigurationProperties
public class MyConfig {

    private String appId;
    private String appSecret;
    private String appName;
    private String globalVar;
	
    
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


}
