package com.zhangkm.spider.config;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(H2databaseProperties.class)
public class H2databaseConfiguration {
    @Autowired
    private H2databaseProperties h2databaseProperties;
    
    @Bean(name = "h2Connection")
    public Connection getH2Conn() {
        Connection conn = null;
        try {
//            conn = DriverManager.getConnection(
//                    "jdbc:h2:/C:/zkm/files4test/h2database/h2db", 
//                    "sa", "");
            conn = DriverManager.getConnection(
                    h2databaseProperties.getUrl(),
                    h2databaseProperties.getUser(),
                    h2databaseProperties.getPassword());
            System.out.println("初始化H2database数据库完毕...");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return conn;
    }
}
