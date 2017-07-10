package com.zhangkm.weixin.base.message.req;

import com.zhangkm.weixin.base.message.Message;

/**
 * 消息基类（普通用户 -> 公众帐号）
 * 
 * @author liufeng
 * @date 2013-05-19
 */
public class RequestMessage extends Message{
	
	// 消息id，64位整型
	private long MsgId;

	public long getMsgId() {
		return MsgId;
	}

	public void setMsgId(long msgId) {
		MsgId = msgId;
	}
}
