package com.zhangkm.spider.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zhangkm.spider.catcher.JobCatcher;
import com.zhangkm.spider.frame.RedisDAO;
import com.zhangkm.spider.spider.LinkSpider;

@Controller
@RequestMapping("/")
public class MainAction {
    @Autowired
    private JobCatcher jobCatcher;
    @Autowired
    private LinkSpider linkSpider;

    @RequestMapping("/")
    @ResponseBody
    public String main(
            HttpServletRequest request,
            HttpServletResponse response
        ) throws Exception {
        
        //HttpSession session = request.getSession();
        jobCatcher.start();
        linkSpider.start();
        return "OK"; 
    }
}
