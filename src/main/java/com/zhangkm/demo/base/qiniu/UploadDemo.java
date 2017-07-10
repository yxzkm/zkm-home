package com.zhangkm.demo.base.qiniu;

import com.qiniu.util.Auth;
import java.io.IOException;
import java.util.UUID;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;

/**
 * 七牛云存储图片上传demo
 * @author p
 *
 */
public class UploadDemo {
	// 设置好账号的ACCESS_KEY和SECRET_KEY
	String ACCESS_KEY = "as2iu0KeMuCC6gXgymKLZJnVc7HSvaVnMEWiQhBZ";
	String SECRET_KEY = "p7g9-9sSfRYku3hcGsoBhSGuBylM6EHnQMVkTn2K";
	// 要上传的空间
	String bucketname = "yxzkm";

	// 上传文件的路径
	String fileName = "C:/Users/p/Desktop/1.png";

	// 密钥配置
	Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
	// 创建上传对象
	UploadManager uploadManager = new UploadManager();

	// 简单上传，使用默认策略，只需要设置上传的空间名就可以了
	public String getUpToken() {
		return auth.uploadToken(bucketname);
	}

	public void upload() throws IOException {
		try {

			UUID uuid = UUID.randomUUID();
			// 调用put方法上传
			Response res = uploadManager.put(fileName, uuid.toString(), getUpToken());
			// 打印返回的信息
			System.out.println(res.bodyString());
			System.out.println("http://pic.zhangkm.com/" + uuid.toString());
			
			String authUrl = auth.privateDownloadUrl("http://pic.zhangkm.com/" + uuid.toString());
			System.out.println(authUrl);
			
		} catch (QiniuException e) {
			Response r = e.response;
			// 请求失败时打印的异常的信息
			System.out.println(r.toString());
			try {
				// 响应的文本信息
				System.out.println(r.bodyString());
			} catch (QiniuException e1) {
				// ignore
			}
		}
	}

	public static void main(String args[]) throws IOException {
		new UploadDemo().upload();
	}

}