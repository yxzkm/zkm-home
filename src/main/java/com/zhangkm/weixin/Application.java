package com.zhangkm.weixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.zhangkm.weixin.web.UserSecurityInterceptor;

/**
 * springboot 官方文档：
 * http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/
 * @author zhangkm
 *
 */
//关于@EnableWebMvc注解：
//1.springboot不需要使用@EnableWebMvc注解
//2.springboot在/static, /public, META-INF/resources, /resources等存放静态资源的目录
//如果使用了@EnableWebMvc注解，那么会将静态资源定位于src/main/webapp。
//当需要重新定义资源所在目录时，则需要主动添加配置类，并且Override addResourceHandlers方法。
@Controller
@SpringBootApplication
public class Application extends WebMvcConfigurerAdapter{

	private Logger logger = LoggerFactory.getLogger(this.getClass());
			
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	} 

    @RequestMapping("/")
    String home() {
    	logger.info("欢迎来到：zkm-weixin");
        return "redirect:/html/weixin/file_upload.html";
    }
    @RequestMapping("/api")
    String api() {
        return "redirect:/swagger-ui.html";
    }
    
    /**
     * 定时任务
     */
    @Scheduled(fixedRate = 1000 * 60 * 10)   // 每600秒执行一次定时任务
    public void reportCurrentTime() {
    	logger.info("现在是定时任务执行时间[{}]",System.currentTimeMillis());
    }

    /**
     * 配置拦截器
     */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new UserSecurityInterceptor()).addPathPatterns("/my**");
        super.addInterceptors(registry);
	}
    
}