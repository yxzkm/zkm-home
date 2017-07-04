package com.zhangkm.weixin.action;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zhangkm.weixin.service.RedisService;

@Controller
@RequestMapping("/")  
public class Test {
	@Autowired
    private RedisService redisService;

    @RequestMapping(
    		method = RequestMethod.GET, 
    		value="/test")
	@ResponseBody
    public String test() {
    	redisService.test();
    	return null;
    }
}
