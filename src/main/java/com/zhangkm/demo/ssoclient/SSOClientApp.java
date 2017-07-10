package com.zhangkm.demo.ssoclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.zhangkm.demo.ssoclient.web.GlobalSettings;
import com.zhangkm.demo.ssoclient.web.UserSecurityInterceptor;

@ComponentScan("com.zhangkm.demo.ssoclient")
@EnableScheduling
@EnableAutoConfiguration
@EnableConfigurationProperties({GlobalSettings.class})  
public class SSOClientApp extends WebMvcConfigurerAdapter{

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController( "/zkm" ).setViewName( "forward:/index.html" );
        registry.setOrder( Ordered.HIGHEST_PRECEDENCE );
        super.addViewControllers(registry);
    } 

    @Bean
    public UserSecurityInterceptor getMyInterceptor(){
        return new UserSecurityInterceptor();
    }
    /**
     * 配置拦截器
     * @author lance
     * @param registry
     */
	public void addInterceptors(InterceptorRegistry registry) {
    	//registry.addInterceptor(new UserSecurityInterceptor()).addPathPatterns("/my**");
    	
        registry.addInterceptor(getMyInterceptor()).addPathPatterns("/my**");
        super.addInterceptors(registry);
	}

    /**
     * spring boot 定时任务
     */
    @Scheduled(fixedRate = 1000 * 6)   // 每6秒执行一次定时任务
    public void reportCurrentTime() {
    	//System.out.println("定时任务:"+System.currentTimeMillis());
    }
	
    public static void main(String[] args) {
		SpringApplication.run(SSOClientApp.class, args);
	} 
    
}