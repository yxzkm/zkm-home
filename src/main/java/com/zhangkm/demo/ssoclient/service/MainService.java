package com.zhangkm.demo.ssoclient.service;

import org.springframework.stereotype.Service;

@Service
public class MainService {
	
	public String getWelcomeString(){
		return "You are WELCOME!";
	}
	public int uploadAndSavePicService(String filename, String extname){
		String sql = ""
			+ "\n insert into pic ( "
			+ "\n    filename, " 
			+ "\n    extname, " 
			+ "\n    pubtime " 
			+ "\n ) values ("
			+ "\n    ?,?,?)" ;
		return 1;
	}

}
