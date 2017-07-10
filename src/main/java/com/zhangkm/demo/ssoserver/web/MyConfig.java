package com.zhangkm.demo.ssoserver.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource("classpath:config/application-sso-server.properties")
@Component
@ConfigurationProperties
public class MyConfig {


}
