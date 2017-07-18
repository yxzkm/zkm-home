package com.zhangkm.spider.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zhangkm.spider.catcher.JobCatcher;

@Controller
@RequestMapping("/")
public class MainAction {

    @ResponseBody
    public String main(
            HttpServletRequest request,
            HttpServletResponse response
        ) throws Exception {
        
        //HttpSession session = request.getSession();
        new JobCatcher().start();
        return "OK"; 
    }
}
