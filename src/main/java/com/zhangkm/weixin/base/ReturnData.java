package com.zhangkm.weixin.base;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class ReturnData {
	private final static Map<Integer,String> errorMap = new HashMap<>();

	private int code;
	private String msg;
	private Map<String,Object> dataBody;
	
	public ReturnData(int errorCode){
		this.dataBody = new HashMap<String,Object>();
		this.code = errorCode;
		this.msg = StringUtils.defaultIfEmpty(errorMap.get(errorCode), "未知错误");
		//TODO: 这里需要从常亮中得到错误代码所对应的的错误信息，错误代码不存在，错误信息返回“未知错误”
	}
	
	//TODO 暂存静态块，待考虑清楚可优化
	static{
		errorMap.put(0, "成功");
		errorMap.put(-1, "文件上传失败");
		errorMap.put(-2, "不允许上传大于10M的文件");
		errorMap.put(-3, "不允许上传小于10K的文件");
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Map<String, Object> getDataBody() {
		return dataBody;
	}

	public void setDataBody(Map<String, Object> dataBody) {
		this.dataBody = dataBody;
	}
	
}
