package com.zhangkm.demo.ssoclient.web;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.zhangkm.demo.ssoclient.util.AisinoUtils;

import net.sf.json.JSONObject;


/**
 * 拦截未登录的用户信息
 * @author lance
 * 2014-6-10下午9:57:20
 */
@Component
public class UserSecurityInterceptor implements HandlerInterceptor {
	private static Logger logger = Logger.getLogger(UserSecurityInterceptor.class);  

//	@Autowired  
//	GlobalSettings globalSettings;  

	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		
    	String url = "" +
    			(request.getRequestURL()==null?"/":request.getRequestURL()) +
    			(request.getQueryString()==null?"":"?"+request.getQueryString())
    			;
        //TODO: 下面采用ResourceBundle读取配置信息，是因为，GlobalSettings无法注入。
    	ResourceBundle resb = ResourceBundle.getBundle("config/application-sso-client"); 
    	logger.info("进入拦截器\n\n#########################################################");
    	logger.info("["+resb.getString("appName")+"]当前拦截的私有url: "+url);
    	
    	//从session中获取用户信息
    	HttpSession session = request.getSession();
    	String suid = (String)session.getAttribute("suid");
       	// 首先判断session中是否已经包含用户信息，如果包含，说明已经登录成功，予以放行。
       	if(suid!=null && !suid.trim().equals("")){
       		//在session中已经存在token信息，说明是可信任的登录用户，允许通过
       		logger.info("[放行]在session中存在用户基本信息suid：["+suid+"]，说明是已登录用户");
    		return true;  
       	}
       	
       	// 判断请求中是否包含票据ssoTicket，如果包含，说明请求来自SsoServer的重定向
       	String ssoTicket = request.getParameter("ssoTicket");
       	if(ssoTicket!=null){
    	   	//验证票据是否有效
    	   	String userInfoString = verifySSOTicket(ssoTicket);
    		if(userInfoString!=null && !userInfoString.trim().equals("")) {
    			logger.info("userInfoString: "+userInfoString);
    			JSONObject jsonObject = JSONObject.fromObject(userInfoString);  
    		   	suid = (String)jsonObject.get("suid");
    		   	if(suid!=null && !suid.trim().equals("")){
        		   	session.setAttribute("suid", suid);
               		logger.info("[放行]票据验证成功，获得用户基本信息suid：["+suid+"]");
            		return true;  
    		   	}
    		} 
       	}
   		
       	// session没有用户信息并且没有sso票据或票据失效，未登录状态，重定向至SSO登录页面
       	String appId = resb.getString("appId");
       	String appSecret = resb.getString("appSecret");
       	String timestamp = ""+System.currentTimeMillis();
       	String nonce = "mynonce";
   		String redirectUrl = "https://z.zz/ssoDemo/showLoginPage"
   				+ "?from="+url
   				+ "&appid="+appId
   				+ "&nonce=" + nonce //TODO: 目前nonce随机串是app产生，不安全。将来要改成从ssoserver获取随机串，防止重放攻击
   				+ "&timestamp=" + timestamp
   				+ "&sign=" + AisinoUtils.sign(appId,nonce,timestamp,appSecret)
   				;
   		response.sendRedirect(redirectUrl);
   		logger.info("[拦截]session没有用户基本信息，说明是未登录用户，重定向至SSO登录页面");
   		logger.info("[重定向]重定向地址："+redirectUrl);
   		return false;  
	}

	/**
     * 向SSO服务器发起请求，验证ticket是否有效
	 * @param ticket
	 * @return
	 */
	private String verifySSOTicket(String ticket){
		if(ticket==null || ticket.trim().equals("")) return "error";

		ResourceBundle resb = ResourceBundle.getBundle("config/application"); 
		String appId = resb.getString("appId");
		String nonce = "mynonce";
		String timestamp = "" + System.currentTimeMillis();
		String appSecret = resb.getString("appSecret");
		
		String chenckTicketUrl = "http://sso.zhangkm.com/ssoDemo/checkTicket"
   				+ "?ticket="+ticket
   				+ "&appid=" + appId
   				+ "&nonce=" + nonce //TODO: 目前nonce随机串是app产生，不安全。将来要改成从ssoserver获取随机串，防止重放攻击
   				+ "&timestamp=" + timestamp
   				+ "&sign=" + AisinoUtils.sign(appId,nonce,timestamp,appSecret)
   				;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(chenckTicketUrl);

            System.out.println("Executing request " + httpget.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            String responseBody = httpclient.execute(httpget, responseHandler);
    		return responseBody;

		} catch (IOException e) {
			e.printStackTrace();
        } finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		return "error";
	}
	

	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

	}

}