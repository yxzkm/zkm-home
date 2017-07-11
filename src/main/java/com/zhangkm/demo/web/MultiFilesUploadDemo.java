package com.zhangkm.demo.web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.zhangkm.weixin.base.ReturnData;

@Controller
@RequestMapping(value = "/demo/web", produces = "text/html;charset=UTF-8")
public class MultiFilesUploadDemo {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${myapp.upload.file.save.path}")
    private String UPLOAD_FILE_SAVE_PATH;

    /**
     * 多文件上传
     * 注意html中form和input的写法，input的name为：filesSelected，要和后端对应
     * @param request
     * @return
     */
    @RequestMapping(value="/multiFilesUpload", method=RequestMethod.POST)
   	@ResponseBody
   	public ReturnData multiFilesUpload(MultipartHttpServletRequest request){
    			
   		List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("filesSelected");
    		
   		if (files==null) return new ReturnData(-1);
   		if (files.size()==0) return new ReturnData(-1);

   		for(MultipartFile file : files){

   			if (file.isEmpty()) return new ReturnData(-1);
    			
   			logger.info("MultipartFile.getName():"+file.getName());
   			logger.info("MultipartFile.getOriginalFilename():"+file.getOriginalFilename());
   			
			String serverFullFileName = UPLOAD_FILE_SAVE_PATH + UUID.randomUUID().toString() + ".jpg";

			byte[] bytes = null;
			FileOutputStream fos = null;
					
			try {
				bytes = file.getBytes();
				fos = new FileOutputStream(serverFullFileName); 
				fos.write(bytes); 
			} catch (Exception e) {
				e.printStackTrace();
				return new ReturnData(-1);
			} finally {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
   		}
   		return new ReturnData(0);
   	}

}
