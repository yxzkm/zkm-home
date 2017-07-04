package com.zhangkm.weixin.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zhangkm.weixin.message.MessageUtil;
import com.zhangkm.weixin.service.MessageService;

@Controller
@RequestMapping(
		value="/raymond",
		produces = "text/html;charset=UTF-8")  
public class MessageAction {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${weixin.appid}")
    private String WEIXIN_APPID;

	@Autowired
    private MessageService messageService;

	@RequestMapping(
			method = RequestMethod.GET, 
			value = "/messageHandler")  
	@ResponseBody
	public String verify(
			String signature,String timestamp,String nonce, String echostr
			){
		logger.info("signature:{}  timestamp:{}  nonce:{}  echostr:{}",
				signature,timestamp,nonce,echostr);
		return echostr;
	}
	
    @RequestMapping(
    		method = RequestMethod.POST, 
			value = "/messageHandler")  
	@ResponseBody
    public String handleMessage(HttpServletRequest request) {
    	
        // 调用核心业务类接收消息、处理消息  
        Map<String, String> requestMap = MessageUtil.parseXml(request);  
        requestMap.put("appid", WEIXIN_APPID);
        String openid = requestMap.get("FromUserName");

		requestMap.put("openid", openid);
        String respMessage = messageService.processRequest(requestMap);  
        return respMessage;  
    }  

}
