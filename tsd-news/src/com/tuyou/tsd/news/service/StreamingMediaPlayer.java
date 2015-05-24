package com.tuyou.tsd.news.service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import com.tuyou.tsd.common.network.AudioItem;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.news.comm.Contents;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class StreamingMediaPlayer {

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	};
	public MediaPlayer mediaPlayer = null;
	private File downloadingMediaFile = null;
	public AudioPlayerService mPlayerService = null;
	private Runnable notification = null;
	private long mFilesize = 0;
	private String mPlayingFileName = null;
	private boolean isManulStop = false;
	private Thread mDownloadThread = null;
	private boolean isStartChachePlay = false;
	private AudioDownload downloadHelper = null;
	private int currentProgress = 0;
	private int currentDuration = 0;
	private int lastProgress = 0;
	private Runnable delayPlay = null;
	private AudioDownload chacheDownload = null;
	private Thread mChacheDownloadThread = null;
	private Runnable updater=null;
	private Runnable dataplayer=null;
	
	private String  oldUrl=null;
	private String  oldFilename=null;
	private long  oldFilesize=0;
	private int  oldPlaytime=0;
	private int tryTimes=0;
	private int networkErrorTime=0;
	private boolean mIsNeedChache=false;
	private boolean isPlayingMusic=false;
	private int resumeTimes=0;
	public PlayMediaThread mPlayMediaThread=null;
	private int oldSongSize=0;
	
	public StreamingMediaPlayer(AudioPlayerService playerService) {
		mPlayerService = playerService;
		File file = new File(Contents.MP3_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		startPlayProgressUpdater();
	}

	public void stop() {
		tryTimes=0;
		resumeTimes=0;
		isManulStop = true;
		isPlayingMusic=false;
		if (downloadHelper != null) {
			downloadHelper.stopDownload();
			downloadHelper = null;
			mDownloadThread = null;			
		}
		if (chacheDownload != null) {
			chacheDownload.stopDownload();
			chacheDownload = null;
			mChacheDownloadThread = null;			
		}
		if(updater!=null)
		{
			handler.removeCallbacks(updater);
		}
		
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer=null;
		}
		if (delayPlay != null) {
			handler.removeCallbacks(delayPlay);
			delayPlay=null;
		}
		if (dataplayer != null) {
			handler.removeCallbacks(dataplayer);
			dataplayer=null;
		}
		
		isStartChachePlay = false;
		if(mPlayMediaThread!=null)
		{
			mPlayMediaThread.stopPlayer(true);
			mPlayMediaThread=null;
		}
	}

	/**
	 * 开启一个线程，下载数据
	 */
	public int startStreaming(final String mediaUrl, final String filename,
			long filesize, final int playtime,boolean isNeedChache) throws IOException {
		mIsNeedChache=isNeedChache;
		oldUrl=mediaUrl;
		oldFilename=filename;
		
		oldFilesize=filesize;
		resumeTimes=0;
		currentProgress = 0;
		currentDuration = 5000;//默认歌曲长于5秒
		lastProgress = playtime;
		isManulStop=false;
		
		if(mPlayMediaThread!=null)
		{
			mPlayMediaThread.stopPlayer(true);
			mPlayMediaThread=null;
		}
        if(mediaPlayer!=null)
        {
        	try
        	{
        		mediaPlayer.stop();
        		mediaPlayer.release();
        	}
        	catch(Exception e)
        	{
        		
        	}
        	mediaPlayer = null;
        }
		
		mFilesize = filesize;
		downloadingMediaFile = new File(Contents.MP3_PATH, filename);
		if (downloadingMediaFile.exists()) {  //&& (downloadingMediaFile.length() == mFilesize)
			mPlayingFileName = filename;
			try
			{
				mPlayMediaThread= new PlayMediaThread(Contents.MP3_PATH + "/"
						+ mPlayingFileName, playtime);
				isPlayingMusic=true;
				mPlayMediaThread.handler.sendEmptyMessage(1);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
					
			startChacheDownload();
			return 1;
		}

		if (JsonOA2.getInstance(mPlayerService).checkNetworkInfo() != -1) {

			Runnable r = new Runnable() {
				public void run() {
					try {
						mPlayingFileName = filename;
						downloadHelper.runDownLoad(mediaUrl, filename);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
			};
			isStartChachePlay = true;			
			delayPlay = null;
			dataplayer=null;
			
			if(chacheDownload!=null)
			{
			  if(chacheDownload.getDownloadingFile().equals(filename))
			  {
				  downloadHelper=chacheDownload;
				  mDownloadThread=mChacheDownloadThread;
				  downloadHelper.startPlay();
				  mPlayingFileName = filename;
				  chacheDownload=null;
				  mChacheDownloadThread=null;
			  }
			  else
			  {
				  chacheDownload.stopDownload();
				  downloadHelper = new AudioDownload(this, isStartChachePlay);
				  mDownloadThread = new Thread(r);
				  mDownloadThread.start();
			  }				
			}
			else
			{
				if (downloadHelper != null) {
					downloadHelper.stopDownload();
					downloadHelper = null;
					mDownloadThread = null;
				}
				downloadHelper = new AudioDownload(this, isStartChachePlay);
				mDownloadThread = new Thread(r);
				mDownloadThread.start();
			}
			
		}
		else
		{
			mPlayerService.mHandler.sendEmptyMessage(401);
		}
		return 0;
	}

	public void startPlayProgressUpdater() {
		try {
			if(isPlayingMusic==true)
			{
				if ((mediaPlayer != null) && (mediaPlayer.isPlaying())) {
					mPlayerService.setProgressBroadCast();
					currentProgress = mediaPlayer.getCurrentPosition();
					currentDuration = mediaPlayer.getDuration();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		notification = new Runnable() {
			public void run() {
				startPlayProgressUpdater();
			}
		};
		handler.postDelayed(notification, 1000);
	}


  private void deleteChacheFile()
  {
	  
		File temp=new File(Contents.MP3_PATH, mPlayingFileName+ "_temp");
		if(temp.exists())
		{
			temp.delete();
		}
//		else
//		{
//			File temp1=new File(Contents.MP3_PATH, mPlayingFileName);
//			if(temp1.exists())
//			{
//				temp1.delete();
//			}
//		}
  }
  
  private void playCompleted()
  {
	  mPlayerService.onCompletion(null);
	  if(!mIsNeedChache)
	  {
		  deleteChacheFile();
	  }
  }



	private void playDownloadData() {
		try {			
			if(mPlayMediaThread!=null)
			{
				mPlayMediaThread.stopPlayer(true);
				mPlayMediaThread=null;
			}
            String filename=null;
			File temp=new File(Contents.MP3_PATH, mPlayingFileName+ "_temp");
			if(temp.exists())
			{
				filename=mPlayingFileName+ "_temp";
			}
			else
			{
				File temp1=new File(Contents.MP3_PATH, mPlayingFileName);
				if(temp1.exists())
				{
					filename=mPlayingFileName;
				}
			}
			if(filename==null)
			{
				if(mediaPlayer!=null)
				{
					mediaPlayer.release();
					mediaPlayer=null;
				}
				playCompleted();
			}
			
			if(downloadHelper!=null)
			{
				synchronized(downloadHelper)
				{
					try
					{
						mPlayMediaThread= new PlayMediaThread(Contents.MP3_PATH + "/"
								+ filename,lastProgress);
						isPlayingMusic=true;
						mPlayMediaThread.handler.sendEmptyMessage(1);						
						mPlayerService.sendPlayStatus(1);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			else
			{
				try
				{
					mPlayMediaThread= new PlayMediaThread(Contents.MP3_PATH + "/"
							+ mPlayingFileName,lastProgress);
					isPlayingMusic=true;
					mPlayMediaThread.handler.sendEmptyMessage(1);
					mPlayerService.sendPlayStatus(1);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
				isPlayingMusic=true;				
				isStartChachePlay = true;
		} catch (Exception e) {
			e.printStackTrace();
			if(mediaPlayer!=null)
			{
				mediaPlayer.release();
				mediaPlayer=null;
			}
			startDelayPlayDownloadData();
		}
	}

	public void startPlayDownloadData() {
		 dataplayer = new Runnable() {
			public void run() {
				playDownloadData();
				dataplayer=null;
			}
		};
		handler.postDelayed(dataplayer, 1000);
	}

	public void startDelayPlayDownloadData() {
		if (resumeTimes <= 10) { // 断点续播10次
			delayPlay = new Runnable() {
				public void run() {
					playDownloadData();
					delayPlay = null;
				}
			};
			resumeTimes++;
			handler.postDelayed(delayPlay, 5000);
		} else {
			playCompleted();
			networkErrorTime++;
			if(networkErrorTime>=2)
			{//播放语音提示网络不畅通
				mPlayerService.playTtsStart("对不起，网络不顺畅，播放失败了。");
				networkErrorTime=0;
			}
		}		
	}
	
	public void startCheckPlayDownloadData() {
		if (resumeTimes <= 20) { // 断点续播10次
			delayPlay = new Runnable() {
				public void run() {
					
					if((downloadHelper!=null)&&(downloadHelper.totalKbRead-oldSongSize<200*1024))
					{
						startCheckPlayDownloadData();
					}
					else
					{
						playDownloadData();
						delayPlay = null;
						resumeTimes=0;
						
					}
				}
			};
			resumeTimes++;
			handler.postDelayed(delayPlay, 1000);
			
		} else {
			playCompleted();
			networkErrorTime++;
			if(networkErrorTime>=2)
			{//播放语音提示网络不畅通
				mPlayerService.playTtsStart("对不起，网络不顺畅，播放失败了。");
				networkErrorTime=0;
			}
		}		
	}
	

	public void dataLoadUpdate(String filename, final int percent) {

		if (filename.equals(mPlayingFileName)) {
			updater = new Runnable() {
				public void run() {
					if (downloadHelper != null) {
						mPlayerService.onBufferingUpdate(null, percent);
					}
				}
			};
			handler.post(updater);

		}
	}

	public void playRepeat()
	{
		if(tryTimes<3)  //错误三次直接跳过
		{
			tryTimes++;
			if (mediaPlayer != null) {
				mediaPlayer.release();
				mediaPlayer=null;
			}

			Runnable updater = new Runnable() {
				public void run() {
					try
					{
						startStreaming(oldUrl,oldFilename,oldFilesize,currentProgress,mIsNeedChache);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			handler.postDelayed(updater, 10000);
		}
		else
		{
			playCompleted();
			networkErrorTime++;
			if(networkErrorTime>=2)
			{//播放语音提示网络不畅通
				mPlayerService.playTtsStart("对不起，网络不顺畅，播放失败了。");
				networkErrorTime=0;
			}
		}
	}
	public void dataDownLoaded(AudioDownload download, int status) {
		if (download.equals(chacheDownload)) {
			chacheDownload = null;			
		} else if (download.equals(downloadHelper)) {
			downloadHelper = null;
			if(status==0)
			{
				mPlayerService.onBufferingUpdate(null, 100);
				startChacheDownload();
			}
			if((status==1)&&(!isManulStop)) //下载异常
			{
				try
				{
				lastProgress = mediaPlayer.getCurrentPosition();
				}catch(Exception e)
				{
					e.printStackTrace();
				}			
				playRepeat();			
			}
		}		
		
		LogUtil.v(getClass().getName(),"dataDownLoad error status="+status);
	}

	public void startChacheDownload() {
		if(chacheDownload!=null)
			return ;
		
		final AudioItem audio = mPlayerService.getNextChache();
		if (audio != null) {
			if (mPlayingFileName.equals(audio.item))
				return; // 正在播放歌曲
			File testFile = new File(Contents.MP3_PATH, audio.item);
			if (testFile.exists())//&& (testFile.length() == audio.size)) 
			{
				return;// 已经是下载的
			}
			chacheDownload = new AudioDownload(this, false);

			Runnable r = new Runnable() {
				public void run() {
					try {

						chacheDownload.runDownLoad(audio.url, audio.item);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
			};

			mChacheDownloadThread = new Thread(r);
			mChacheDownloadThread.start();
		}
	}
	

	class PlayMediaThread extends Thread {
		private Semaphore semaphore = new Semaphore(0);
		private Looper myLooper;
		public Handler handler;
		private String filename;
		private int mProgress;

		public PlayMediaThread(String file,int progress ) throws InterruptedException {
			filename=file;
			mProgress=progress;
			start();
			semaphore.acquire();
		}

		public void quitLooper() {
			try {
				if (myLooper != null) {
					myLooper.quit();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		public void stopPlayer(boolean clean) {
			if (clean) {
				quitLooper();
			}
		}
		public void pausePlayer() {
		 if(mediaPlayer != null)
		 {
			 mediaPlayer.pause();
		 }
		}
		
		@SuppressLint("NewApi")
		private void createMediaPlayer() {
			if (mediaPlayer == null) {
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
					public boolean onError(MediaPlayer mp, int what, int extra) {
						LogUtil.v(getClass().getName(),"mediaPlayer=onError");
						if(mediaPlayer!=null)
						{
				           mediaPlayer.release();
						}
						mediaPlayer=null;
						isPlayingMusic=false;
						if (isStartChachePlay) {
							if (currentProgress >= (currentDuration - 2000)) {
								playRepeat();
							} else {
								lastProgress = currentProgress;
								if(downloadHelper!=null)
								{
								 oldSongSize=downloadHelper.totalKbRead;
								}
								//startDelayPlayDownloadData();
								startCheckPlayDownloadData();
							}
						} else {
							playRepeat();
						}
						
						return false;
					}
				});
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						if(mediaPlayer!=null)
						{
					        mediaPlayer.release();
						}
						mediaPlayer=null;
						isPlayingMusic=false;
						if (!isManulStop) {
							if (isStartChachePlay) {
								if (currentProgress >= (currentDuration - 2000)) {
									playCompleted();
									networkErrorTime=0;									
								} else {
									lastProgress = currentProgress;
									if(downloadHelper!=null)
									{
									 oldSongSize=downloadHelper.totalKbRead;
									}
									//startDelayPlayDownloadData();
									startCheckPlayDownloadData();
								}
							} else {
								playCompleted();
							}
						} else {
							isManulStop = false;
						}
					}

				});
				mediaPlayer.setOnInfoListener(new OnInfoListener() {

					@Override
					public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
						// TODO Auto-generated method stub
						return false;
					}

				});
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

					@Override
					public void onPrepared(MediaPlayer arg0) {
						isManulStop = false;						
					}

				});
			}
			mediaPlayer.reset();
		}

		@Override
		public void run() {
			Looper.prepare();
			myLooper = Looper.myLooper();
			
			handler = new Handler() {
				@Override
				public void handleMessage(Message msg) 
				{
					switch(msg.what)
					{
					case 1:
						createMediaPlayer();
	
						try {
							mediaPlayer.setDataSource(filename);
							mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
							mediaPlayer.prepare();
							mediaPlayer.seekTo(mProgress);
							mediaPlayer.start();
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case 2:
						pausePlayer();
						break;
					case 3:
						break;
					}
				}
			};

			semaphore.release();
			Looper.loop();
		}
	}
}
