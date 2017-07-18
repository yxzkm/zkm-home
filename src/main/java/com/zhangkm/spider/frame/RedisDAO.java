package com.zhangkm.spider.frame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import net.sf.json.JSONObject;

/**
 * 关于如何使用 RedisTemplate操作redis，建议查阅官方文档，见下面的地址：
 * http://docs.spring.io/spring-data/redis/docs/1.8.1.RELEASE/reference/html/#redis:template
 * @author zhangkm
 *
 */
@Component
public class RedisDAO {
    @Autowired 
    private StringRedisTemplate redisTemplate;  

    /**
     * 向一个String写入值，如果不存在，则创建一个String
     * @param key key
     * @param seconds 持续时间（秒）
     * @param value value
     * @return
     */
    public boolean set(String key,String value){
        return redisTemplate.execute(new RedisCallback<Boolean>() {  
            @Override  
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                connection.set(serializer.serialize(key), serializer.serialize(value));  
                return true;  
            }  
        });  
    }

    /**
     * 获取String的值
     * @param key
     * @return
     */
    public String get(String key){
        //TODO: 这个方法似乎不是官方推荐方案，建议修改。
        String ret = (String) redisTemplate.opsForValue().get(key);
        return ret;
    }

    /**
     * 向有序列表key插入一个成员member，如果member已经存在，则将分数update为score
     * @param key key
     * @param score 分数
     * @param member 成员
     * @return 是否成功
     */
    public boolean zAdd(String key,double score,String member){
        return redisTemplate.execute(new RedisCallback<Boolean>() {  
            @Override  
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                connection.zAdd(serializer.serialize(key), score, serializer.serialize(member));
                return true;  
            }  
        });  
    }

    /**
     * 得到有序列表key中成员member的得分
     * @param key key
     * @param member 成员
     * @return 得分
     */
    public double zScore(String key,String member){
        return redisTemplate.opsForZSet().score(key, member);
    }


    /**
     * 将有序列表key的成员member的分数增加increment
     * @param key key
     * @param increment 分数增量
     * @param member 成员
     * @return 是否成功
     */
    public boolean zIncrBy(String key,double increment,String member){
        return redisTemplate.execute(new RedisCallback<Boolean>() {  
            @Override  
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                connection.zIncrBy(serializer.serialize(key), increment, serializer.serialize(member));
                return true;  
            }  
        });  
    }


    /**
     * 获取String的值
     * @param key
     * @return
     */
    public List<Map<String, Object>> zRange(String key, int start, int end) {
        Set<?> set = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        if(set==null) return list;
        Iterator<?> it = set.iterator();
        while (it.hasNext()) {
            Object str = it.next();
            if (str instanceof TypedTuple<?>) {
                TypedTuple<?> ty = (TypedTuple<?>) str;
                Object obj = ty.getValue();
                if (obj instanceof String) {
                    String k = (String) ty.getValue();
                    Double v = ty.getScore();
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put("member", k);
                    map.put("score", v);
                    list.add(map);
                }
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public Map<String,String> rightPop(String key) {
        String jsonText = redisTemplate.opsForList().rightPop(key);
        JSONObject jsonObject = JSONObject.fromObject(jsonText);
        return  (Map<String,String>)jsonObject;
    }
    
    public int getListSize(String key){
        return redisTemplate.opsForList().size(key).intValue();
    }


    /**
     * (慎用)删除一个String/Set/Hash/List
     * @param key
     */
    public void deleteKey(String key){
        redisTemplate.delete(key);
    }

}
