package com.zhangkm.demo.base.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOTest {
	
	public static void main(String[] args){
		RandomAccessFile aFile = null;
		try {
			aFile = new RandomAccessFile("file/nio-data.txt", "rw");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FileChannel inChannel = aFile.getChannel();

		ByteBuffer buf = ByteBuffer.allocate(48);

		int bytesRead = 0;
		try {
			bytesRead = inChannel.read(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (bytesRead != -1) {

		System.out.println("Read " + bytesRead);
		buf.flip();

		while(buf.hasRemaining()){
		System.out.print((char) buf.get());
		}

		buf.clear();
		try {
			bytesRead = inChannel.read(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		try {
			aFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
}
