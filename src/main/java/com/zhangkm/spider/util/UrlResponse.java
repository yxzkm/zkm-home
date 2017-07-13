package com.zhangkm.spider.util;

public class UrlResponse {
	private String requestUrlAbs;//原始请求的url,绝对路径
	private String realHostName;//原始请求的主机名
	private String realUrl;//实际请求的url，判断是否redirect
	private boolean isRedirect = false; //判断是否被重定向
	private String responseEncoding;//返回页面的编码格式,默认为GLOBAL.DEFAULT_CHARSET
	private String mime; //mime类型
	private String responseBody;
	

	public boolean isRedirect() {
		return isRedirect;
	}
	public void setRedirect(boolean isRedirect) {
		this.isRedirect = isRedirect;
	}
	public String getMime() {
		return mime;
	}
	public void setMime(String mime) {
		this.mime = mime;
	}
	public String getRequestUrlAbs() {
		return requestUrlAbs;
	}
	public void setRequestUrlAbs(String requestUrlAbs) {
		this.requestUrlAbs = requestUrlAbs;
	}
	public String getRealHostName() {
		return realHostName;
	}
	public void setRealHostName(String realHostName) {
		this.realHostName = realHostName;
	}
	public String getRealUrl() {
		return realUrl;
	}
	public void setRealUrl(String realUrl) {
		this.realUrl = realUrl;
	}
	public String getResponseEncoding() {
		return responseEncoding;
	}
	public void setResponseEncoding(String responseEncoding) {
		this.responseEncoding = responseEncoding;
	}
	public String getResponseBody() {
		return responseBody;
	}
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

}
