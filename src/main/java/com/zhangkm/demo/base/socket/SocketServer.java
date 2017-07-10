package com.zhangkm.demo.base.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;

public class SocketServer {

	private static final int PORT = 4700;
	private static ServerSocket server = null;
	
	private SocketServer(){}	
	
	public static void main(String[] args) {
        System.out.println("Server starting...");  
        
		try {
			// 创建一个ServerSocket在端口4700监听客户请求
			server = new ServerSocket(PORT);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			return;
		}
		Socket socket = null;
		try {
			while(true){
				System.out.println("Server waiting...");  
				// 使用accept()阻塞等待客户请求
				socket = server.accept();
				new WorkerThread(socket).start();
				System.out.println("Server received a request...");  
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	private static class WorkerThread extends Thread {
		
		Socket socket;
		
		public WorkerThread(Socket socket){
			this.socket = socket;	
		}
        
		public void run() {  
            BufferedReader in = null;  
            PrintWriter out = null;  
			
            try {  
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
                out = new PrintWriter(socket.getOutputStream());  

                String msg = in.readLine();  
                msg = URLDecoder.decode(msg,"utf-8");
                System.out.println("org msg: " + msg);
                
                if(msg.equalsIgnoreCase("quit")){
                	try {
						server.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
                	return;
                }
                
                out.println("I got it: " + msg);  
                out.flush();  

            } catch(IOException ex) {  
                ex.printStackTrace();  
			} finally {  
                
                try {  
                    in.close();  
                } catch (Exception e) {}  
                try {  
                    out.close();  
                } catch (Exception e) {}  
                try {  
                    socket.close();  
                } catch (Exception e) {}  
                System.out.println("session close\n");
            }  
        }  
    }

}
