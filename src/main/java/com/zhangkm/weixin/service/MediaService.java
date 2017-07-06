package com.zhangkm.weixin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

@Service("mediaService")
public class MediaService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${qiniu.access.key}")
	private String QINIU_ACCESS_KEY;

    @Value("${qiniu.secret.key}")
    private String QINIU_SECRET_KEY;

    @Value("${qiniu.bucket.name}")
	private String QINIU_BUCKET_NAME;

    /**
     * 将图片上传到七牛
     * @param imgFullFileName
     * @param qiniuFileName
     * @return 是否成功
     */
    public boolean uploadImgFileToQiniu(String imgFullFileName, String qiniuFileName) {
    	// 密钥配置
    	Auth auth = Auth.create(QINIU_ACCESS_KEY, QINIU_SECRET_KEY);
    	// 创建上传对象
    	UploadManager uploadManager = new UploadManager();

    	try {
			// 调用put方法上传
			Response res = uploadManager.put(imgFullFileName, qiniuFileName, auth.uploadToken(QINIU_BUCKET_NAME));
			if(res.statusCode!=200) return false;

			//String authUrl = auth.privateDownloadUrl("http://pic.zhangkm.com/" + qiniuFileName);
			return true;
			
		} catch (QiniuException e) {
			Response r = e.response;
			// 请求失败时打印的异常的信息
			logger.error("错误1："+r.toString());
			try {
				logger.error("错误2："+r.bodyString());
			} catch (QiniuException e1) {
				// ignore
			}
		} catch (Exception e) {
			logger.error("错误0："+e.getMessage());
		}
		return false;
    }  

}
