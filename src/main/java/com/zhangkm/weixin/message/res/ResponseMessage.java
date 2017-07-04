package com.zhangkm.weixin.message.res;

import com.zhangkm.weixin.message.Message;

/**
 * 消息基类（公众帐号 -> 普通用户）
 * 
 * @author liufeng
 * @date 2013-05-19
 */
public class ResponseMessage extends Message{
	
	// 位0x0001被标志时，星标刚收到的消息
	private int FuncFlag;

	public int getFuncFlag() {
		return FuncFlag;
	}

	public void setFuncFlag(int funcFlag) {
		FuncFlag = funcFlag;
	}
}
