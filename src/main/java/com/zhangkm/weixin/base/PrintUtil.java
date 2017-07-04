package com.zhangkm.weixin.base;

import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class PrintUtil {
	private static int GAP = 3;
	private static int LEFT = 21;
	private static int RIGHT = 2 * LEFT; 
	private static int WIDTH = GAP + LEFT + GAP + RIGHT + GAP;
	private static int titleMid = 0;

	public static void printRequest(HttpServletRequest request,Map<String,String> map){
		printRequestHeaderParameter(request);
		printPostDataMap(map);
	}
	
	private static void printRequestHeaderParameter(HttpServletRequest request){

		String headers = "headers";
		String parameters = "parameters";
		//String cookies = "cookies";
		
		// 开始
		System.out.println("\n");
		for(int i=0;i<WIDTH;i++){
			System.out.print("#");
		}
		System.out.println();
				
		// 以下是请求头信息
		titleMid = LEFT + 2 + headers.length() / 2;
		System.out.println(""
				+ "## "
				+ String.format("%"+titleMid+"s", headers)
				+ String.format("%"+(WIDTH-titleMid-2*GAP)+"s", "")
				+ " ##"
				);

		printLeft("RequestURI");
		System.out.print(" : ");
		printRight(request.getRequestURI());
		System.out.print("\n");

		printLeft("Method");
		System.out.print(" : ");
		printRight(request.getMethod());
		System.out.print("\n");

		Enumeration<String> e0 = request.getHeaderNames();
		while (e0.hasMoreElements()) {
			String key = (String) e0.nextElement();
			if(key==null) continue;
			String value = request.getHeader(key);
			value = value==null?"":value;
		
			printLeft(key);
			System.out.print(" : ");
			printRight(value);
			System.out.print("\n");
		}

		// 打印空白分割
		System.out.println("## "+String.format("%-"+(WIDTH-2*GAP)+"s", "")+" ##");
		
		// 以下是请求参数信息
		titleMid = LEFT + 2 + parameters.length() / 2;
		System.out.println(""
				+ "## "
				+ String.format("%"+titleMid+"s", parameters)
				+ String.format("%"+(WIDTH-titleMid-2*GAP)+"s", "")
				+ " ##"
				);

		Enumeration<String> e1 = request.getParameterNames();
		while (e1.hasMoreElements()) {
			String key = (String) e1.nextElement();
			if(key==null) continue;
			String value = request.getParameter(key);
			value = value==null?"":value;
			
			printLeft(key);
			System.out.print(" : ");
			printRight(value);
			System.out.print("\n");
		}

		// 打印空白分割
		System.out.println("## "+String.format("%-"+(WIDTH-2*GAP)+"s", "")+" ##");

	}
	
	private static void printPostDataMap(Map<String,String> map){
		if(map==null || map.size()==0) {
			// 打印收尾
			for(int i=0;i<WIDTH;i++){
				System.out.print("#");
			}
			System.out.println("\n");
			return;	
		}
		
		String headers = "postdata";
		
		// 以下是请求头信息
		titleMid = LEFT + 2 + headers.length() / 2;
		System.out.println(""
				+ "## "
				+ String.format("%"+titleMid+"s", headers)
				+ String.format("%"+(WIDTH-titleMid-2*GAP)+"s", "")
				+ " ##"
				);

		for (Entry<String,String> entry : map.entrySet()) {  
			String key = entry.getKey();
			if(key==null) continue;
			String value = entry.getValue();
			value = value==null?"":value;
		
			printLeft(key);
			System.out.print(" : ");
			printRight(value);
			System.out.print("\n");
		}  

		// 打印空白分割
		System.out.println("## "+String.format("%-"+(WIDTH-2*GAP)+"s", "")+" ##");

		// 打印收尾
		for(int i=0;i<WIDTH;i++){
			System.out.print("#");
		}
		System.out.println("\n");

	}
	
	private static void printLeft(String str){
		if(str.length()>LEFT){
			int leftLength = str.length();
			for(int i=0;i<Integer.MAX_VALUE;i++){
				if(leftLength>LEFT){
					String substr = str.substring(i*LEFT,(i+1)*LEFT);
					System.out.print("## ");
					System.out.print(String.format("%"+LEFT+"s", substr));
					System.out.print("   ");
					System.out.print(String.format("%-"+RIGHT+"s", ""));
					System.out.print(" ##");
					System.out.print("\n");
					leftLength = leftLength - LEFT;
				}else{
					String subKey = str.substring(i*LEFT);
					System.out.print("## ");
					System.out.print(String.format("%"+LEFT+"s", subKey));
					break;
				}
			}
		}else{
			System.out.print("## ");
			System.out.print(String.format("%"+LEFT+"s", str));
		}
	}

	private static void printRight(String str){
		if(str.length()>RIGHT){
			int rightLength = str.length();
			for(int i=0;i<Integer.MAX_VALUE;i++){
				if(rightLength>RIGHT){
					String substr = str.substring(i*RIGHT,(i+1)*RIGHT);
					System.out.print(String.format("%-"+RIGHT+"s", substr));
					System.out.print(" ##");
					System.out.print("\n");
					System.out.print("## ");
					System.out.print(String.format("%"+LEFT+"s", ""));
					System.out.print("   ");
					rightLength = rightLength - RIGHT;
				}else{
					String substr = str.substring(i*RIGHT);
					System.out.print(String.format("%-"+RIGHT+"s", substr));
					System.out.print(" ##");
					break;
				}
			}
		}else{
			System.out.print(String.format("%-"+RIGHT+"s", str));
			System.out.print(" ##");
		}

	}
}
