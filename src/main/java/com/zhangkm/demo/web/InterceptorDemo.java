package com.zhangkm.demo.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class InterceptorDemo implements HandlerInterceptor  { 

    @Override  
    public boolean preHandle(
    		HttpServletRequest request, 
    		HttpServletResponse response, 
    		Object handler) throws Exception {  

    	System.out.println("########################进入拦截器################################");

    	String url = "" +
    			(request.getRequestURL()==null?"/":request.getRequestURL()) +
    			(request.getQueryString()==null?"":"?"+request.getQueryString())
    			;
    	
    	System.out.println(request.getRequestURL());

    	HttpSession session = request.getSession();
       	String tokenInSession = (String)session.getAttribute("token");
       	
       	if(tokenInSession==null || tokenInSession.trim().equals("")){
       		//在session中不存在token信息，则需要进一步检查浏览器提交请求的cookie中是否包含token
       		String tokenInCookie = getTokenFromCookie(request);
       		
       		if(tokenInCookie==null || tokenInCookie.trim().equals("")){
           		//cookie中也不存在token信息，则说明是未登录状态，需要重定向到登录页面
               	String redirectUrl = "http://sso.zhangkm.com/ssoDemo/showLoginPage?from="+url;
           		response.sendRedirect(redirectUrl);
           		System.out.println("[拦截]session和cookie均为空，未登录状态，重定向至SSO登录页面");
           		return false;  
           	}else{
           		if(isTokenInCookieAlive(tokenInCookie)){
               		//验证结果为token存在并且活着，允许通过
               		System.out.println("[通过]session为空，但cookie中包含的token有效，已登录用户，继续进入当前页");
           			return true;
           		}else{
               		//验证结果为token不存在或已经失效，则说明是未登录状态，需要重定向到登录页面
                   	String redirectUrl = "http://sso.zhangkm.com/ssoDemo/showLoginPage?from="+url;
               		response.sendRedirect(redirectUrl);
               		System.out.println("[拦截]session为空，且cookie中包含的token无效，重定向至SSO登录页面");
               		return false;  
           		}
           	}
       		
       	}else{
       		//在session中已经存在token信息，说明是可信任的登录用户，允许通过
       		System.out.println("[通过]在session中存在token信息，已登录用户，继续进入当前页");
    		return true;  
       	}
        	
    }  
	
    /**
     * 向SSO服务器发起请求，验证token是否有效
     * @return
     */
	private boolean isTokenInCookieAlive(String token){
		return true;
	}
	
    /**
     * 从cookie中获取令牌
     * @param request
     * @return
     */
    private String getTokenFromCookie(HttpServletRequest request){
		if(request==null) return null;
		Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (Cookie cookie : cookies) {
				if(null != cookie){
					if("token".equalsIgnoreCase(cookie.getName())){
						return cookie.getValue();
					}
				}
			}
		}
		return null;
	}
	

    @Override  
    public void postHandle(
    		HttpServletRequest request, 
    		HttpServletResponse response, 
    		Object handler, 
    		ModelAndView modelAndView) throws Exception {  
        //System.out.println("===========HandlerInterceptor1 postHandle");  
    }  
    @Override  
    public void afterCompletion(
    		HttpServletRequest request, 
    		HttpServletResponse response, 
    		Object handler, 
    		Exception ex) throws Exception {  
        //System.out.println("===========HandlerInterceptor1 afterCompletion");  
    }  

}
