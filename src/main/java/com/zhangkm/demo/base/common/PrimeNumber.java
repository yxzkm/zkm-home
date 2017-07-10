package com.zhangkm.demo.base.common;

import java.util.ArrayList;
import java.util.List;

public class PrimeNumber {

	/**
	 * 素数生成器
	 * @param args
	 */
	public static void main(String[] args){
		List<Long> list = new ArrayList<Long>();
		list.add(2L);
		
		int num = 1;
		for(long i=3; i<Long.MAX_VALUE; i++){
			boolean flag = true;
			for(int arr=0;arr<list.size(); arr++){
				long s = list.get(arr);
				if(i%s==0){
					flag = false;
					break;
				}
				if(2*s>i){
					break;
				}
			}
			if(flag){
				num++;
				System.out.println(num+":"+i);
				list.add(i);
				if(num>121212) break;
			} 
		}
	}

}
