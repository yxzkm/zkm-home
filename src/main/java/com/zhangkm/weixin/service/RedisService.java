package com.zhangkm.weixin.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhangkm.weixin.dao.RedisDao;

@Service
public class RedisService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//主表列表
	private String LABELS_ZSET = "LABELS_ZSET";
	private String PHOTOS_ZSET = "PHOTOS_ZSET";
	
	//主表主键对应其他字段信息
	private String PHOTO_INFO_STRING_PREFIX  = "PHOTO_INFO_STRING:";
	private String LABEL_INFO_STRING_PREFIX  = "LABEL_INFO_STRING:";

	//关联表按主表主键列表
	private String PHOTO_LABELS_ZSET_PREFIX  = "PHOTO_LABELS_ZSET:";
	private String LABEL_PHOTOS_ZSET_PREFIX  = "LABEL_PHOTOS_ZSET:";

	@Autowired 
    private RedisDao redisDao;
	
	/**
	 * 获取10个最热门标签
	 * @param pageNum
	 * @return
	 */
	public List<Map<String,Object>> getHotLabels(int pageNum){
		return redisDao.zRange(LABELS_ZSET, 0, 9);
	}
	
	/**
	 * 得到最新上传的9张照片
	 * @return
	 */
	public List<Map<String,Object>> getLatestTop9Photos(){
		return redisDao.zRange(PHOTOS_ZSET, 0, 8);
	}
	
	/**
	 * 得到某一个照片的全部标签（按照标签当时的热度排序）
	 * @return
	 */
	public List<Map<String,Object>> getPhotoAllLabels(String photoFileName){
		return redisDao.zRange(PHOTO_LABELS_ZSET_PREFIX + photoFileName, 0, -1);
	}
	
	/**
	 * 得到某一个标签的全部照片（按照照片的录入时间排序）
	 * @return
	 */
	public List<Map<String,Object>> getLabelAllPhotos(String label){
		return redisDao.zRange(LABEL_PHOTOS_ZSET_PREFIX + label, 0, -1);
	}
	
	/**
	 * 照片上传成功后，将图片元数据写入redis
	 * @param fileName
	 * @param labelList
	 */
	public void wirtePhotoInfoToRedis(String fileName, List<String> labelList){
		
		//照片大列表，如果文件已经存在，则覆盖其分数（录入时间），而不是增长。
		redisDao.zAdd(PHOTOS_ZSET, System.currentTimeMillis(), fileName);
		for(String label : labelList){
			
			//标签大列表，如果标签已经存在，增长1分分数（引用数），而不是覆盖。
			redisDao.zIncrBy(LABELS_ZSET, 1, label);

			//某一个照片的标签列表，分数是标签当前的引用数
			Double score = redisDao.zScore(LABELS_ZSET,label);
			redisDao.zAdd(PHOTO_LABELS_ZSET_PREFIX + fileName, score, label);
			
			//某一个标签的照片列表，分数是当前时间
			redisDao.zAdd(LABEL_PHOTOS_ZSET_PREFIX + label, System.currentTimeMillis(), fileName);
		}
		
		return;
	}
	
	public void test(){
//		redisDao.zIncrBy(LABELS_ZSET, 100, "睿睿");
//		redisDao.zIncrBy(LABELS_ZSET, 19, "篮球");
//		redisDao.zIncrBy(LABELS_ZSET, 12, "训练");
//		redisDao.zIncrBy(LABELS_ZSET, 1, "做作业");
//		redisDao.zIncrBy(LABELS_ZSET, 6, "绘画");
//		redisDao.zIncrBy(LABELS_ZSET, 1, "旅游");
//		redisDao.zIncrBy(LABELS_ZSET, 9, "公园");
//		redisDao.zIncrBy(LABELS_ZSET, 1, "日常");
//		redisDao.zIncrBy(LABELS_ZSET, 1, "聚会");
//		redisDao.zIncrBy(LABELS_ZSET, 1, "同学");
//		redisDao.zIncrBy(LABELS_ZSET, 1, "家人");
//		redisDao.zIncrBy(LABELS_ZSET, 1, "活动");
//		redisDao.zIncrBy(LABELS_ZSET, 1, "体育");
//		redisDao.zIncrBy(LABELS_ZSET, 1, "美食");
		List<Map<String,Object>> list = redisDao.zRange(LABELS_ZSET, 0, 9);
		if(list==null) return;
		for(Map<String,Object> map : list){
			logger.info("redis zset record: member:{} score:{}",map.get("member"),map.get("score"));
		}
	}
	
}
