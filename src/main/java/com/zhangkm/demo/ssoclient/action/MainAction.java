package com.zhangkm.demo.ssoclient.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.zhangkm.demo.ssoclient.service.MainService;
import com.zhangkm.demo.ssoclient.util.AisinoUtils;
import com.zhangkm.demo.ssoclient.web.GlobalSettings;
import com.zhangkm.demo.ssoclient.web.MyConfig;

@Controller
public class MainAction {
//	private static Logger logger = Logger.getLogger(MainAction.class);  

	@Autowired  
	GlobalSettings globalSettings;  
	@Autowired  
	MainService mainService;
	@Autowired  
	MyConfig myConfig;  
	
	/**
	 * 显示网站首页
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model) {
        model.addAttribute("appName", globalSettings.getAppName());
		System.out.println("globalSettings.getAppName()===="+globalSettings.getAppName());
		return "sso-demo/client/index";
	}

	/**
	 * 显示网站受控资源（需要登录才能显示的页面）
	 * @param model
	 * @return
	 */
	@RequestMapping("/my")
    public String mySecretPage(Model model) {
        model.addAttribute("appName", globalSettings.getAppName());
		System.out.println("globalSettings.getAppName(): " + globalSettings.getAppName());
        return "sso-demo/client/my_secret_page";
    }

	@RequestMapping("/user/{userId}")
	public String user(@PathVariable("userId") int userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("globalVar", globalSettings.getGlobalVar());
        System.out.println("userId="+userId);
        System.out.println("globalVar="+globalSettings.getGlobalVar());
        return "sso-demo/client/user";
    }

	@RequestMapping("/logout")
    public String logout(
		HttpServletRequest request , 
		HttpServletResponse response , 
		ModelMap model, 
		HttpSession session){
		
		String appId = globalSettings.getAppId();
		String nonce = "mynonce";
		String timestamp = "" + System.currentTimeMillis();
		String appSecret = globalSettings.getAppSecret();
		
		String ssoLogoutUrl = "http://sso.zhangkm.com/ssoDemo/logout"
   				+ "?appid=" + appId
   				+ "&nonce=" + nonce //TODO: 目前nonce随机串是app产生，不安全。将来要改成从ssoserver获取随机串，防止重放攻击
   				+ "&timestamp=" + timestamp
   				+ "&sign=" + AisinoUtils.sign(appId,nonce,timestamp,appSecret)
   				;

		System.out.println("app logout session:"+session.getId());
        session.invalidate();
        return "redirect:"+ssoLogoutUrl;
    }

}
