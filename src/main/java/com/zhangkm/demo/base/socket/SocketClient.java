package com.zhangkm.demo.base.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
	private static final int PORT = 4700;
	
	private SocketClient(){}	

	public static void main(String[] args) {
		try{

			//向本机的4700端口发出客户请求
			Socket socket=new Socket("127.0.0.1",PORT);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream());

			String sendMsg = "啊哈哈zkm张克猛";
			//String sendMsg = "quit";

			//将从系统标准输入读入的字符串输出到Server
			out.println(sendMsg);
			out.flush();
	
			//从Server读入一字符串，并打印到标准输出上
			System.out.println("Back from server:" + in.readLine());

			out.close(); //关闭Socket输出流
			in.close(); //关闭Socket输入流
			socket.close(); //关闭Socket

		}catch(Exception e) {
			System.out.println("Error"+e); //出错，则打印出错信息
		}
	}


}
