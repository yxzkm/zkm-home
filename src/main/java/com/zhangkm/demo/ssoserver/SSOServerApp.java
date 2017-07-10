package com.zhangkm.demo.ssoserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.zhangkm.demo.ssoserver.web.GlobalSettings;

@ComponentScan("com.zhangkm.demo.ssoserver")
@EnableScheduling
@EnableAutoConfiguration
@EnableConfigurationProperties({GlobalSettings.class})  
public class SSOServerApp extends WebMvcConfigurerAdapter{

    public static void main(String[] args) {
		SpringApplication.run(SSOServerApp.class, args);
	} 
    
}