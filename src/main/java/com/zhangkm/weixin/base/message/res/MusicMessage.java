package com.zhangkm.weixin.base.message.res;

/**
 * 音乐消息
 * 
 * @author liufeng
 * @date 2013-05-19
 */
public class MusicMessage extends ResponseMessage {
	// 音乐
	private Music Music;

	public Music getMusic() {
		return Music;
	}

	public void setMusic(Music music) {
		Music = music;
	}
}