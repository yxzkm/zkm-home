package com.zhangkm.demo.base.common;

public class PrintTest {
	public static void main(String[] args){
		
		System.out.print("11\t");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.print("22\t");
	}
}
