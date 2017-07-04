package com.zhangkm.weixin.service;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zhangkm.weixin.message.Message;
import com.zhangkm.weixin.message.MessageUtil;
import com.zhangkm.weixin.message.res.TextMessage;

@Service("messageService")
public class MessageService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String CON_HOME_PAGE = ""
			+ "公众号“雷蒙德”微网站首页:\n"
			+ "<a href=\"http://zhangkm.com/\">http://zhangkm.com</a>";

	private final String CON_SAY_HELLO = ""
			+ "您好，请问有什么可以帮您？ 输入home可以进入雷蒙德微网站的首页。";
	
	/**
	 * 处理微信客户端用户输入的消息
	 * @param requestMap
	 * @return
	 */
    public String processRequest(Map<String, String> requestMap) {  
        String msgContent = requestMap.get("Content");  

   		if("home".equalsIgnoreCase(msgContent)){
   			return backTextMessage(requestMap,CON_HOME_PAGE);
   		}else{
   			return backTextMessage(requestMap,CON_SAY_HELLO);
   		}
    }

    private String backTextMessage(Map<String, String> requestMap,String respContent){
        TextMessage responseMessage = new TextMessage();  
        responseMessage = (TextMessage)initResponseMessage(responseMessage,requestMap);
        responseMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);  
        responseMessage.setFuncFlag(0);  
        responseMessage.setContent(respContent);  
        return MessageUtil.textMessageToXml(responseMessage);  
	}

    private Message initResponseMessage(Message responseMessage, Map<String, String> requestMap){
        // 发送方帐号（open_id）  
        String fromUserName = requestMap.get("FromUserName");  
        // 公众帐号  
        String toUserName = requestMap.get("ToUserName");  
        responseMessage.setToUserName(fromUserName);  
        responseMessage.setFromUserName(toUserName);  
        responseMessage.setCreateTime(new Date().getTime());  
        return responseMessage;
    	
    }
    
}
