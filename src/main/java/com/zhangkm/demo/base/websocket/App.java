package com.zhangkm.demo.base.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@EnableAutoConfiguration
@Configuration
public class App extends WebMvcConfigurerAdapter{
    
	@Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	} 
    
}