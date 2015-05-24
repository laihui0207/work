package com.tuyou.tsd.audio.service;

import java.util.List;

import com.tuyou.tsd.common.network.AudioCategory;
import com.tuyou.tsd.common.network.AudioItem;

public interface IAudioPlayerService {
	abstract public void StartPlayer(String category,String item);
	abstract public void pause();
	abstract public void rusume();
	abstract public void stop();
	abstract public AudioItem prew();
	abstract public AudioItem next();
	abstract public List<AudioCategory> getCategoryList();
	abstract public List<AudioItem> getCategoryDetailList(String category);
	abstract public int getProgress();
	abstract public AudioItem getPlayingAudio();
	abstract public int getCacheProgress();
	abstract public int setMusicOrder();
	abstract public boolean isPlaying();
	abstract public boolean addFavourite(AudioItem item);
	abstract public boolean deleteFavourite(AudioItem item);
	abstract public boolean isFavourite(AudioItem item);
	abstract public AudioCategory getPlayingCatogory();
	abstract public AudioCategory getHeardCatogory();
	abstract public void showButtonNotify(AudioItem item,boolean isplay,boolean isLove);
	abstract public int setMusicOrder(int order);
}
