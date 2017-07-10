package com.zhangkm.demo.base.common;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileRenameTest {
	public static void main(String[] args){
		try {
			FileUtils.copyFileToDirectory(
					new File("C:/zkm/files4test/testphoto/1.jpg"), 
					new File("C:/zkm/files4test"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	 
}
