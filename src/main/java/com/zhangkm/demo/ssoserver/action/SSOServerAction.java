package com.zhangkm.demo.ssoserver.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.zhangkm.demo.ssoserver.service.SSOServerService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(
		value="/ssoDemo",
		produces = "text/html;charset=UTF-8")  
public class SSOServerAction {
	private static Logger logger = Logger.getLogger(SSOServerAction.class);  

	@Resource 
	private SSOServerService ssoServerService;

    @ExceptionHandler(value = Exception.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
    	
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("ssoDemoError");
        return mav;
    }

	/**
	 * 显示登录页面
	 * @param request
	 * @param response
	 * @param model
	 * @param session
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/showLoginPage")
	public String showLoginPage(
			HttpServletRequest request , 
			HttpServletResponse response , 
			ModelMap model, 
			HttpSession session) throws Exception{

		String sign = request.getParameter("sign");
		String timestamp = request.getParameter("timestamp");
		String appid = request.getParameter("appid");
		String nonce = request.getParameter("nonce");
		String fromUrl = request.getParameter("from");

		System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW="+fromUrl);
		
		if(StringUtils.isBlank(fromUrl)) throw new Exception("发生错误：该请求没有来源url（from）地址");

		//if(StringUtils.isBlank(fromUrl)) return "redirect:/html/errorpage.html";

		//验证签名
		if(!ssoServerService.verifySign(appid, nonce, timestamp, sign)) return "sso-demo/errorpage";
		
		String suid = (String)session.getAttribute("suid");
		//session中没有用户信息，说明没有登录过sso，重定向到sso的登录页面
		if(StringUtils.isEmpty(suid)) {
			logger.info("["+appid+"][未登录]session中没有用户信息suid，重定向到登录页面");
			logger.info(session.getId() + "\n\n");
			model.put("from", fromUrl);
			return "sso-demo/server/sso_login_page";	
		}
		
		//产生票据
		String ssoTicket = ssoServerService.produceSsoTicket(appid);
		fromUrl = fromUrl + "?ssoTicket="+ssoTicket;
		logger.info("["+appid+"][重定向]已登录用户，带着票据ticket:"+ssoTicket+"，重定向到from");
		logger.info(session.getId() + "\n\n");
		return "redirect:"+fromUrl;
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loginVerify")
	@ResponseBody
	public String verifyUserLogin(
			HttpServletRequest request , 
			HttpServletResponse response , 
			ModelMap model, 
			HttpSession session){
		String callback = request.getParameter("callback");
		String suid = request.getParameter("u");
		String fromUrl = request.getParameter("from"); //TODO: 用于记录用户从何处登录

		session.setAttribute("suid", suid);
		logger.info("[登录成功]SSO：将用户基本信息[suid:"+suid+"]写入session");
		logger.info(session.getId() + "\n\n");

		Map<String,String> map = new HashMap<String,String>();
		map.put("status", "success");
		JSONObject returnMapJson = JSONObject.fromObject(map);  
		return callback+"("+returnMapJson.toString()+")";
	}

	/**
	 * 该方法是单点登录系统为各个业务子系统提供的token验证服务，
	 * 只能允许业务系统后台通过httpclient发起请求，
	 * 终端用户通过浏览器是无法访问该地址的。
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/checkTicket")
	@ResponseBody
	public String checkTicket(
			HttpServletRequest request , 
			HttpServletResponse response , 
			ModelMap model, 
			HttpSession session){
		
		String sign = request.getParameter("sign");
		String timestamp = request.getParameter("timestamp");
		String appid = request.getParameter("appid");
		String nonce = request.getParameter("nonce");
		String ticket = request.getParameter("ticket");

		//验证签名是否正确
		if(!ssoServerService.verifySign(appid, nonce, timestamp, sign)) return "error";

		//验证票据是否存在
		if(!ssoServerService.checkTicket(ticket)) {
			logger.info("["+appid+"][ERROR!!!!! 票据验证失败！]\n\n");
			return "error";	
		}
		
		//TODO: 验证票据是否过期
		//TODO: 验证票据是否属于该AppId
		
		//验证已经通过，将票据从队列里移除，防止再次利用。
		Map<String,String> map = ssoServerService.getUserInfoByTicket(ticket);
		JSONObject jsonObject = JSONObject.fromObject(map);  
		String jsonString = jsonObject.toString();

		logger.info("["+appid+"][票据正确]\n\n");
		
		//将当前用户已经登录的appid加入列表，为将来登出使用
		ssoServerService.addAppToLoginList("zkm",appid);

		return jsonString;
	}

	@RequestMapping(value = "/logout")
	public String logout(
			HttpServletRequest request , 
			HttpServletResponse response , 
			ModelMap model, 
			HttpSession session){
		
		String sign = request.getParameter("sign");
		String timestamp = request.getParameter("timestamp");
		String appid = request.getParameter("appid");
		String nonce = request.getParameter("nonce");
		logger.info("\n\n["+appid+"][进入登出页面！！！！！！！！！！！！]");

		//验证签名是否正确
		if(!ssoServerService.verifySign(appid, nonce, timestamp, sign)) return "sso-demo/error";
		logger.info("["+appid+"][登出页面]签名正确！");

		//得到用户唯一标识suid
		String suid = (String)session.getAttribute("suid");
		if(StringUtils.isBlank(suid)) {
			logger.info("["+appid+"][登出页面]session中不存在suid，说明session已经注销了，该用户已经全部退出了，重定向到门户首页！");
			return "redirect:http://sso.zhangkm.com";
		}
		logger.info("["+appid+"][登出页面]session中存在suid！");

		//从队列中移除
		ssoServerService.removeAppFromLoginList(suid,appid);
		logger.info("["+appid+"][登出页面]从applist中移除一条app记录");
			
		List<String> appList = ssoServerService.getAppLoginList(suid);
		if(appList==null || appList.size()==0){
			//列表为空，说明所有应用均已经登出。
			//将session注销
			logger.info("["+appid+"][登出页面]applist中没有数据，说明全都退出了。清除session: "+session.getId());
	        session.invalidate();
			return "redirect:http://sso.zhangkm.com";
		}

		String nextAppid = appList.get(0);
        String logoutUrl = ssoServerService.getAppSsoLogoutUrl(nextAppid);
		logger.info("["+appid+"][登出页面]重定向到下一个appid: "+ nextAppid);
		return "redirect:"+logoutUrl;
	}

}
