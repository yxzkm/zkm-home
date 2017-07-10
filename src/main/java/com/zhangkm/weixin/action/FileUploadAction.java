package com.zhangkm.weixin.action;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.zhangkm.weixin.base.ReturnData;
import com.zhangkm.weixin.service.MediaService;
import com.zhangkm.weixin.service.MetaDataService;
import com.zhangkm.weixin.service.RedisService;

import io.swagger.annotations.Api;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Positions;

/**
 * 
 * @ClassName: FileUploadAction
 * @Description: TODO
 *
 */
@Controller
@RequestMapping("/fileUpload")  
public class FileUploadAction {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static List<String> batchCodeList = new ArrayList<String>();
	
    @Value("${myapp.upload.file.save.path}")
    private String UPLOAD_FILE_SAVE_PATH;
    
    @Value("${myapp.photo.backup.path}")
    private String PHOTO_BACKUP_PATH;
    
    //缩略图文件后缀
    private String THUMBNAIL_SUFFIX = "_thumb.jpg"; 

	@Autowired
    private MediaService mediaService;
    
	@Autowired
    private RedisService redisService;
    
	@Autowired
    private MetaDataService metaDataService;
    
	@RequestMapping(value="/postFileToServer", method=RequestMethod.POST)
	@ResponseBody
	public ReturnData postFileToServer(
			@RequestParam("fileToUpload") MultipartFile multipartFile,
			String batchCode,
			String orderNum){
			
		if(StringUtils.isBlank(batchCode)) return new ReturnData(-1);
		
		if(!batchCodeList.contains(batchCode)) return new ReturnData(-1);
		if(!batchCodeList.remove(batchCode)) return new ReturnData(-1);
		
		if (multipartFile==null) return new ReturnData(-1);
		if (multipartFile.isEmpty()) return new ReturnData(-1);
				
		long size = multipartFile.getSize();
		if(size>10000000) return new ReturnData(-2);
		if(size<10000) return new ReturnData(-3);
		
		byte[] bytes = null;
		try {
			bytes = multipartFile.getBytes();
		} catch (IOException e2) {
			e2.printStackTrace();
			return new ReturnData(-1);
		}
		if(bytes==null||bytes.length==0) return new ReturnData(-1);

		logger.info("图片原始的文件名为[{}]",multipartFile.getOriginalFilename());
		
		//对文件的二进制流进行hash运算，得出图片文件“指纹”，并将指纹作为文件名。指纹用于图片排重。
		String fingerPrint = "";
		try {
			logger.info("计算图片文件指纹...");
			MessageDigest messageDigest = MessageDigest.getInstance("SHA1");  
			messageDigest.update(bytes);  
			fingerPrint = byteToHex(messageDigest.digest());
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			fingerPrint = UUID.randomUUID().toString();
		}

		//创建临时目录，存储用户上传上来的图片文件（先不上传七牛）。如果目录已存在就什么也不做。
		if(!createDir(UPLOAD_FILE_SAVE_PATH + batchCode)) return new ReturnData(-3);
		
		//将图片数据流，保存到服务器本地文件
		String tempFullPathFileName = UPLOAD_FILE_SAVE_PATH 
				+ batchCode + "/" 
				+ fingerPrint + "_" + orderNum + ".jpg";

		FileOutputStream fos = null;
		try {
			logger.info("保存图片到本地文件:[{}]...",tempFullPathFileName);
			fos = new FileOutputStream(tempFullPathFileName); 
			fos.write(bytes);
			
			ReturnData retData = new ReturnData(0);
			retData.getDataBody().put("orgFileName", multipartFile.getOriginalFilename());
			return retData;
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
	
	/**
	 * 获取本次上传图片的批次代码
	 * 因为是多张图片异步上传，必须保证全部图片上传完毕之后，再提交标签并最终传到七牛服务器，
	 * 因此，首先生成一个批处理代码，每次上传图片的时候，带上这个批处理代码，后台就知道是同一个批次得了。
	 * @return
	 */
    @RequestMapping(
    		method = RequestMethod.GET, 
    		value="/getBatchCode")
	@ResponseBody
    public ReturnData getBatchCode(String amount) {
    	
    	int intAmount = Integer.parseInt(amount);
    	String batchCode = UUID.randomUUID().toString();
    	for(int i=0;i<intAmount;i++){
        	batchCodeList.add(batchCode);
    	}
    	logger.info("batchCodeList size: {}",batchCodeList.size());
    	
    	ReturnData retData = new ReturnData(0);
    	retData.getDataBody().put("batchCode", batchCode);
        return retData;  
    }  

    @RequestMapping(
    		method = RequestMethod.GET, 
    		value="/getHotLabels")
	@ResponseBody
    public List<Map<String,Object>> getHotLabels() {
        return redisService.getHotLabels(0);  
    }  

    @RequestMapping(
    		method = RequestMethod.GET, 
    		value="/getLatestTop9Photos")
	@ResponseBody
    public List<Map<String,Object>> getLatestTop9Photos() {
        return redisService.getLatestTop9Photos();  
    }  

    /*
     * 最终提交标签
     */
    @RequestMapping(
    		method = RequestMethod.POST, 
    		value="/submitLabels")
	@ResponseBody
    public ReturnData submitLabels(HttpServletRequest request) {

    	String postString = getPostStringFromRequest(request);
		if(StringUtils.isBlank(postString)) return new ReturnData(-1);
		
		String batchCode = "";
		List<String> labelList = new ArrayList<String>();
		
		try {
			batchCode = postString.split("&")[0].split("=")[1];
			String labelsString = postString.split("&")[1].split("=")[1];
			String k = postString.split("&")[2];
			
			logger.info("\n\n\n传入随机数参数： {}",k);
			
			String[] arr = labelsString.split(",");
			for(String s : arr){
				if(StringUtils.isBlank(s)) continue;
				labelList.add(s.trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnData(-1);
		}
		
		if(StringUtils.isBlank(batchCode))  return new ReturnData(-1);
		if(labelList==null || labelList.size()==0) {
			logger.error("没有提交标签");
			return new ReturnData(-1);
		} 
		
		String labelString = "";
		for(String s : labelList){
			labelString = labelString + "_" + s ; 
		}
		logger.info("本次提交的标签是： {}",labelString);

		File directory = new File( UPLOAD_FILE_SAVE_PATH + batchCode);
		if(!directory.exists()) return new ReturnData(-1);
		
		File[] flist = directory.listFiles();
		if (flist == null || flist.length == 0) return new ReturnData(-1);
		
		for (File f : flist) {
			if (f.isDirectory()) continue;
			
			//取得当前文件全路径名称
			logger.info("当前文件全路径名是： {}", f.getAbsolutePath());
			
			//获取照片拍摄日期
			String shootTime = metaDataService.getPhotoCreateTimeFromMetaData(f.getAbsolutePath());
			if(StringUtils.isBlank(shootTime)) shootTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			//当前图片文件短名称的最前面是图片指纹。取图片指纹的前四位。
			String shortFingerPrint = f.getName().substring(0, 4);
			//组装新文件名： 拍摄时间+照片指纹前四位+标签.jpg
			String backupFileName = shootTime + "_" + shortFingerPrint + labelString + ".jpg";
			String httpFileName = shootTime + "_" + shortFingerPrint + ".jpg";
			logger.info("新的文件名是： [{}]", backupFileName);

			logger.info("正在将大图复制到上一级路径的“photobk”目录下，用于备份...");
			try {
				FileUtils.copyFile(f, new File(PHOTO_BACKUP_PATH + backupFileName));
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("大图片备份失败：备份目标路径[{}]",PHOTO_BACKUP_PATH + backupFileName);
				continue;
			}

			logger.info("正在将大图复制到上一级路径，用于http访问...");
			try {
				FileUtils.copyFile(f, new File(UPLOAD_FILE_SAVE_PATH + httpFileName));
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("大图片备份失败：备份目标路径[{}]",UPLOAD_FILE_SAVE_PATH + httpFileName);
				continue;
			}

//			//上传大图到七牛服务器
//			logger.info("正在将大图上传到七牛服务器...");
//			if(!mediaService.uploadImgFileToQiniu(f.getAbsolutePath(),f.getName())){
//				//TODO: 错误处理，事务回滚. 事物以图片最终成功上传到七牛，并且将标签入库为结束标志
//				logger.error("大图片上传七牛失败：[{}]",f.getName());
//				continue;
//			}

			//生成正方形的缩略图
			logger.info("正在生成缩略图...");
			if(!saveSquareThumbnail(UPLOAD_FILE_SAVE_PATH + httpFileName)){
				logger.error("生成缩略图失败：[{}]",UPLOAD_FILE_SAVE_PATH + httpFileName);
				continue;
			} ;
			
//			//上传缩略图到七牛服务器
//			logger.info("正在将缩略图上传到七牛服务器...");
//			if(!mediaService.uploadImgFileToQiniu(
//					f.getAbsolutePath()+THUMBNAIL_SUFFIX,f.getName()+THUMBNAIL_SUFFIX)){
//				logger.error("缩略图上传七牛失败：[{}]",f.getName()+THUMBNAIL_SUFFIX);
//				continue;
//			}
			
			//将照片和标签信息写入redis
			logger.info("正在将图片和标签元数据写入redis数据库...");
			redisService.wirtePhotoInfoToRedis(httpFileName,labelList);

			logger.info("文件处理完毕： [{}]，准备删除大图和缩略图...", f.getName());
			new File(f.getAbsolutePath()+THUMBNAIL_SUFFIX).delete();
			f.delete();
		
		}
		
		//文件全部处理完成后，删除临时文件夹
		deleteDir(UPLOAD_FILE_SAVE_PATH + batchCode);

		logger.info("所有文件全部处理完毕");

		/*		
		//构造返回数据结构
		ReturnData returnData = new ReturnData(0);
		Map<String,Object> map = returnData.getDataBody();
		//将上传文件的原始名称回传到前台，用于后续处理
		map.put("orgFileName", multipartFile.getOriginalFilename());
		map.put("serverFileName", fingerPrintFileName);

		logger.info("图片上传完毕： orgFileName:{}  serverFileName:{}",multipartFile.getOriginalFilename(),fingerPrintFileName);
		return returnData;
		
	*/

		return new ReturnData(0);  
    }  
    
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

    private String getPostStringFromRequest(HttpServletRequest request){
    	InputStream inputStream = null;
		ByteArrayOutputStream baos = null;
		try {
			inputStream = request.getInputStream();
			baos = new ByteArrayOutputStream();
			int i = -1;
			while ((i = inputStream.read()) != -1) {
				baos.write(i);
			}
			return baos.toString();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			try {
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }

    
    private boolean saveSquareThumbnail(String imgFullFileName) {
		try {
			Builder<File> b = Thumbnails.of(imgFullFileName).scale(1.0);
			BufferedImage image = b.asBufferedImage();

			int height = image.getHeight();
			int width = image.getWidth();
			
			// 此时的图片，已经根据EXIF中的方向参数（Orientation），将图片进行了旋转！！！
			// 判断图片是宽度长还是高度长，然后取窄边长度作为正方形的边长
			int sideSize = height>width?width:height;

			b = Thumbnails.of(imgFullFileName);
			b.sourceRegion(Positions.CENTER, sideSize, sideSize)    // 以图片的中心为中心，取边长为sideSize的正方形
						.height(300)                                // 将上述正方形按比例缩放到边长为300的正方形
						.toFile(imgFullFileName+THUMBNAIL_SUFFIX);             // 保存图片文件
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}  
	}
    
	private String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

    private boolean createDir(String destDirName) {  
        File dir = new File(destDirName);  
        if (dir.exists()) {  
            logger.info("创建目录" + destDirName + "，目标目录已经存在");  
            return true;  
        }  
        if (!destDirName.endsWith(File.separator)) {  
            destDirName = destDirName + File.separator;  
        }  
        //创建目录  
        if (dir.mkdirs()) {  
            logger.info("创建目录" + destDirName + "成功！");  
            return true;  
        } else {  
            logger.info("创建目录" + destDirName + "失败！");  
            return false;  
        }  
    }  

	/**
	 * 删除文件夹
	 * @param destDirName
	 */
    private void deleteDir(String destDirName) {  
        File dir = new File(destDirName);  
        if (!dir.exists()) {  
            logger.info("删除目录：{} 失败，目录不存在", destDirName);  
            return;  
        }
        for(File file : dir.listFiles()){
        	file.delete();
        }
        dir.delete();
    }  

	

}
