package com.zhangkm.spider.util;

import java.util.Map;

import com.zhangkm.spider.frame.G;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

public class RedisUtil {
	
	public static JedisPool jedisPool = null;
	
	public static void clearAllQueue(){
		
		clearList(G.QUEUE_JOB_CATCHER);
		clearList(G.QUEUE_LINK_SPIDER);
		clearList(G.QUEUE_LINK_CHECKER);
		clearList(G.QUEUE_PAGE_SPIDER);
		clearList(G.QUEUE_TEXT_EXTRACTOR);
		clearList(G.QUEUE_BASIC_FILTER);
		clearList(G.QUEUE_INDUSTRY_FILTER);
		clearList(G.QUEUE_REGION_FILTER);
		clearList(G.QUEUE_MONGO_WRITER);
		clearList(G.QUEUE_SOLR_WRITER);
		
	}

	private static Jedis getJedisFromPool() throws Exception{
		Jedis jedis = jedisPool.getResource();
		jedis.select(0);
		return jedis;
	}

	public static boolean pushListData(String queueName,Map<String,String> map){
        Jedis jedis = null;
        try {
			jedis = getJedisFromPool();
		} catch (Exception e1) {
			System.out.println("error: get redis client exception");
			jedisPool.returnBrokenResource(jedis);
			return false;
		}
        try {
			Transaction t = jedis.multi();
	        JSONObject jsonObject = JSONObject.fromObject(map);
		    t.lpush(queueName, jsonObject.toString());
			t.exec();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error: redis transaction exception");
			System.out.println(map.toString());
			jedisPool.returnBrokenResource(jedis);
			return false;
        } finally {
            if (jedis != null && jedisPool!=null) {
            	try {
            		jedisPool.returnResource(jedis);
				} catch (Exception e) {
					System.out.println("error: redis returnResource exception");
				}
            }
        }
	}

	@SuppressWarnings("unchecked")
	public static Map<String,String> popListData(String queueName){
		if(queueName==null || queueName.trim().equals("")) return null;
        Jedis jedis = null;
        try {
			jedis = getJedisFromPool();
		} catch (Exception e1) {
			e1.printStackTrace();
			System.out.println("error: get redis client exception");
			jedisPool.returnBrokenResource(jedis);
			return null;
		}
        try {
        	String jsonText = jedis.rpop(queueName);
        	JSONObject jsonObject = JSONObject.fromObject(jsonText);
        	return  (Map<String,String>)jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error: redis transaction exception");
			jedisPool.returnBrokenResource(jedis);
			return null;
        } finally {
            if (jedis != null && jedisPool!=null) {
            	try {
            		jedisPool.returnResource(jedis);
				} catch (Exception e) {
					System.out.println("error: redis returnResource exception");
				}
            }
        }
	}

	public static int getListSize(String queueName){
		if(queueName==null || queueName.trim().equals("")) return 0;
        Jedis jedis = null;
        try {
			jedis = getJedisFromPool();
		} catch (Exception e1) {
			e1.printStackTrace();
			System.out.println("error: get redis client exception");
			jedisPool.returnBrokenResource(jedis);
			return 0;
		}
        try {
        	Long llen = jedis.llen(queueName);
        	return  llen.intValue();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error: redis transaction exception");
			jedisPool.returnBrokenResource(jedis);
			return 0;
        } finally {
            if (jedis != null && jedisPool!=null) {
            	try {
            		jedisPool.returnResource(jedis);
				} catch (Exception e) {
					System.out.println("error: redis returnResource exception");
				}
            }
        }
	}
	
	public static boolean clearList(String keyName){
        Jedis jedis = null;
        try {
			jedis = getJedisFromPool();
		} catch (Exception e1) {
			System.out.println("error: get redis client exception");
			jedisPool.returnBrokenResource(jedis);
			return false;
		}
        try {
	        jedis.ltrim(keyName, 1, 0); //清空
	        return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error: redis transaction exception");
			jedisPool.returnBrokenResource(jedis);
			return false;
        } finally {
            if (jedis != null && jedisPool!=null) {
            	try {
            		jedisPool.returnResource(jedis);
				} catch (Exception e) {
					System.out.println("error: redis returnResource exception");
				}
            }
        }
	}
	

	public static String getStringData(String stringKey){
		if(stringKey==null || stringKey.trim().equals("")) return null;
        Jedis jedis = null;
        try {
			jedis = getJedisFromPool();
		} catch (Exception e1) {
			e1.printStackTrace();
			System.out.println("error: get redis client exception");
			jedisPool.returnBrokenResource(jedis);
			return null;
		}
        try {
        	return jedis.get(stringKey);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error: redis transaction exception");
			jedisPool.returnBrokenResource(jedis);
			return null;
        } finally {
            if (jedis != null && jedisPool!=null) {
            	try {
            		jedisPool.returnResource(jedis);
				} catch (Exception e) {
					System.out.println("error: redis returnResource exception");
				}
            }
        }
	}

	public static boolean setStringData(String stringKey,String value){
		if(stringKey==null || stringKey.trim().equals("")) return false;
        Jedis jedis = null;
        try {
			jedis = getJedisFromPool();
		} catch (Exception e1) {
			e1.printStackTrace();
			System.out.println("error: get redis client exception");
			jedisPool.returnBrokenResource(jedis);
			return false;
		}
        try {
        	return value.equals(jedis.set(stringKey, value));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error: redis transaction exception");
			jedisPool.returnBrokenResource(jedis);
			return false;
        } finally {
            if (jedis != null && jedisPool!=null) {
            	try {
            		jedisPool.returnResource(jedis);
				} catch (Exception e) {
					System.out.println("error: redis returnResource exception");
				}
            }
        }
	}

	/**
     * 将redis的String类型的数值加1
	 * @param keyName zset名称
     * @param memberName member名称
	 */
	public static void incrZsetMemberScore(String keyName,String memberName){
        Jedis jedis = null;
        try {
			jedis = getJedisFromPool();
		} catch (Exception e1) {
			e1.printStackTrace();
			System.out.println("error: get redis client exception");
			jedisPool.returnBrokenResource(jedis);
			return;
		}
        try {
        	jedis.zincrby(keyName, 1, memberName);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error: redis transaction exception");
			jedisPool.returnBrokenResource(jedis);
			return;
        } finally {
            if (jedis != null && jedisPool!=null) {
            	try {
            		jedisPool.returnResource(jedis);
				} catch (Exception e) {
					System.out.println("error: redis returnResource exception");
				}
            }
        }
	}

	/**
	 * 从redis的String类型中，获取所需的数值.
	 * @param keyName String类型的key值
	 * @return 返回需要获取的数值，如某个词条的点击数
	 */
	public static int getZsetMemberScore(String keyName, String memberName){
        Jedis jedis = null;
        try {
			jedis = getJedisFromPool();
		} catch (Exception e1) {
			e1.printStackTrace();
			System.out.println("error: get redis client exception");
			jedisPool.returnBrokenResource(jedis);
			return 0;
		}
        try {
        	Double sNum = jedis.zscore(keyName, memberName);
        	try {
        		if (sNum == null) {
        			return 0;
        		}
				return  sNum.intValue();
			} catch (Exception e) {
				return  0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error: redis transaction exception");
			jedisPool.returnBrokenResource(jedis);
			return 0;
        } finally {
            if (jedis != null && jedisPool!=null) {
            	try {
            		jedisPool.returnResource(jedis);
				} catch (Exception e) {
					System.out.println("error: redis returnResource exception");
				}
            }
        }
	}

	/**
	 * 从redis的zset类型中，获取成员数量
	 * @param zsetName zset名称
	 * @return 成员数量
	 */
	public static int getZsetSize(String keyName){
        Jedis jedis = null;
        try {
			jedis = getJedisFromPool();
		} catch (Exception e1) {
			e1.printStackTrace();
			System.out.println("error: get redis client exception");
			jedisPool.returnBrokenResource(jedis);
			return -1;
		}
        try {
        	Long sNum = jedis.zcount(keyName, 0, Integer.MAX_VALUE);
        	try {
				return  sNum.intValue();
			} catch (Exception e) {
				return  -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error: redis transaction exception");
			jedisPool.returnBrokenResource(jedis);
			return -1;
        } finally {
            if (jedis != null && jedisPool!=null) {
            	try {
            		jedisPool.returnResource(jedis);
				} catch (Exception e) {
					System.out.println("error: redis returnResource exception");
				}
            }
        }
	}


}
