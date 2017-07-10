package com.zhangkm.demo.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/demo/web", produces = "text/html;charset=UTF-8")
public class PostDemo {
	
	@Autowired
	private HttpServletRequest request;

	@RequestMapping(
    		method = RequestMethod.POST, 
    		value="/posttest")  
	@ResponseBody
    public String handleAuthEvent() {

		String zkm = request.getParameter("zkm");
		System.out.println("zkm: "+zkm);
		String a = request.getParameter("a");
		System.out.println("a: "+a);
		String b = request.getParameter("b");
		System.out.println("b: "+b);

		
		InputStream inputStream = null;
		ByteArrayOutputStream baos = null;
		try {
			inputStream = request.getInputStream();
			baos = new ByteArrayOutputStream();
			int i = -1;
			while ((i = inputStream.read()) != -1) {
				baos.write(i);
			}
			System.out.println("baos.toString(): "+baos.toString());
			return "ok";
		} catch (IOException e1) {
			e1.printStackTrace();
			return "";
		} finally {
			try {
				inputStream.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			try {
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

    }  
	

}
