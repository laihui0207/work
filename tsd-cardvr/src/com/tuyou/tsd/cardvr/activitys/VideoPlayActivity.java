package com.tuyou.tsd.cardvr.activitys;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyou.tsd.cardvr.BaseActivity;
import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.bean.TroubleInfo;
import com.tuyou.tsd.cardvr.bean.VideoInfo;
import com.tuyou.tsd.cardvr.service.IInterfaceService;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.videoMeta.VideoManager;

public class VideoPlayActivity extends BaseActivity implements SurfaceHolder.Callback ,OnClickListener,OnCompletionListener,OnSeekBarChangeListener,OnErrorListener{
    private MediaPlayer player;
    private SurfaceView surface;
    private ImageView videoPrew,videoPlay,videoNext;
    
    private TextView videoTime;
//    private RelativeLayout videoBottomBg;
    private RelativeLayout videoBottomLayout;
    private TextView back;
    private int time=0;
    private String timePlayString = "";
    private TextView timeStart;
    
    private ArrayList<VideoInfo> listVideoInfo;
    private boolean isChild = false;
    private int index;
    private ArrayList<TroubleInfo> listTrouble;
    private SeekBar seekBar;
    private IInterfaceService countService;
    
    private int dur;
    private ImageView isLove;
    private RelativeLayout titleParent;
    private TextView videoAddress;
    
    private int SHOW_TIME = 5000;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_play);
        this.bindService( new Intent("com.tuyou.tsd.cardvr.service.InterfaceService" ), this.serviceConnection, BIND_AUTO_CREATE);
        initView();
    }
	@SuppressWarnings({ "deprecation", "unchecked"})
    private void initView() {
    	  isChild = getIntent().getExtras().getBoolean("is_child");
      	if(isChild){
      		listTrouble = (ArrayList<TroubleInfo>) getIntent().getExtras().getSerializable("data");
      	}else{
      		listVideoInfo =(ArrayList<VideoInfo>) getIntent().getExtras().getSerializable("data");
      	}
      	videoAddress = (TextView) findViewById(R.id.video_address);
      	index = getIntent().getExtras().getInt("index");
      	videoPrew=(ImageView)findViewById(R.id.video_prew);
      	videoPlay=(ImageView)findViewById(R.id.video_play);
      	videoNext=(ImageView)findViewById(R.id.video_next);
      	surface=(SurfaceView)this.findViewById(R.id.surface_view);
      	surface.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
      	surface.getHolder().addCallback(this);
      	surface.getHolder().setKeepScreenOn(true);
      	surface.setOnClickListener(this);
      	player = new MediaPlayer();
//      	videoBottomBg = (RelativeLayout) findViewById(R.id.video_bottom_bg);
      	videoBottomLayout = (RelativeLayout) findViewById(R.id.video_bottom_layout);
      	back = (TextView) findViewById(R.id.back);
      	back.setOnClickListener(this);
      	isLove = (ImageView) findViewById(R.id.is_love); 
      	isLove.setOnClickListener(this);
      	timeStart = (TextView) findViewById(R.id.time_start);
      	videoTime = (TextView) findViewById(R.id.video_time);
      	seekBar = (SeekBar) findViewById(R.id.seekBar);
      	seekBar.setOnSeekBarChangeListener(this);
      	videoPlay = (ImageView) findViewById(R.id.video_play);
      	videoPlay.setOnClickListener(this);
      	videoNext = (ImageView) findViewById(R.id.video_next);
      	videoNext.setOnClickListener(this);
      	videoPrew = (ImageView) findViewById(R.id.video_prew);
      	videoPrew.setOnClickListener(this);
      	titleParent = (RelativeLayout) findViewById(R.id.title_parent_play);
	}
	

	private void show(int type){
    	videoTime.setVisibility(type);
//    	videoBottomBg.setVisibility(type);
    	videoBottomLayout.setVisibility(type);
    	back.setVisibility(type);
    	seekBar.setVisibility(type);
    	titleParent.setVisibility(type);
    	if(type==View.GONE){
    		playHandler.removeCallbacks(showRunnable);
    	}else{
    		playHandler.postDelayed(showRunnable, SHOW_TIME);
    	}
    }
	
	
	
	
	@SuppressLint("SimpleDateFormat")
	private void showPlayTime(int playTime){
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		timeStart.setText(format.format(new Date(Long.valueOf(timePlayString)+1000*playTime)));
//		timeLong.setText("/"+format.format(new Date(Long.valueOf(timePlayString)+player.getDuration()/1000)));
	}
    
    @SuppressLint("HandlerLeak")
	Handler playHandler = new Handler(){
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
			case 1:
				setPlayer();
				break;
			case 2:
				show(View.GONE);
				break;
			case 3:
				try {
					if(player.isPlaying()){
						if(time<player.getDuration()/1000){
							time++;
							if(time>0){
								showPlayTime(time);
								int current = player.getCurrentPosition();
		                        seekBar.setProgress(current);
							}
						}
					}
					playHandler.removeCallbacks(timeRunnable);
					playHandler.postDelayed(timeRunnable,1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
				
			}
    	}
    };
    
    Runnable playRunnable = new Runnable() {
		@Override
		public void run() {
			Message msg = playHandler.obtainMessage();
			msg.what = 1;
			playHandler.sendMessage(msg);
		}
	};
	
	Runnable timeRunnable = new Runnable() {
		@Override
		public void run() {
			Message msg = playHandler.obtainMessage();
			msg.what = 3;
			playHandler.sendMessage(msg);
		}
	};
	
	Runnable showRunnable = new Runnable() {
		
		@Override
		public void run() {
			Message msg = playHandler.obtainMessage();
			msg.what = 2;
			playHandler.sendMessage(msg);
		}
	};
 
    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }
    
 
	@Override
    public void surfaceCreated(SurfaceHolder arg0) {
		playHandler.post(playRunnable);
        playHandler.postDelayed(timeRunnable,1000);
        playHandler.postDelayed(showRunnable, SHOW_TIME);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
		play();
		
    }
	
	
	@SuppressLint("SimpleDateFormat")
	private void showTitle(String time){
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_hh:mm:ss");
			videoTime.setText(format.format(new Date(Long.valueOf(time))));
			timePlayString = time;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void play() {
        try {
        	player.reset();//重置为初始状态
        	player.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置音乐流的类型
        	player.setDisplay(surface.getHolder());
        	if(isChild){
        		showTitle(listTrouble.get(index).getTime());
        		player.setDataSource(countService.getVideo(listTrouble.get(index).getName()));
        		
        	}else{
        		showTitle((listVideoInfo.get(index).getTime()));
        		player.setDataSource(countService.getVideo(listVideoInfo.get(index).getName()));
        	}
        	player.prepare();//缓冲
        	player.start();//播放
        	player.seekTo(dur);
        	} catch (Exception e) {
                e.printStackTrace();
            }
    }
 
    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
    	if(player!=null){
            dur=player.getCurrentPosition();
            player.stop();
        }
    	playHandler.removeCallbacks(playRunnable);
    	playHandler.removeCallbacks(timeRunnable);
    	try {
    		time = Integer.valueOf(timeStart.getText().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
    
    
    @Override 
    protected void onDestroy() {
        super.onDestroy();
        if(player!=null&&player.isPlaying()){
        	player.stop();
        }
        if(player!=null){
         player.release();
        }
        unbindService(serviceConnection);
    }

	@Override
	public void onClick(View v) {
		playHandler.removeCallbacks(showRunnable);
		playHandler.postDelayed(showRunnable, SHOW_TIME);
		switch (v.getId()) {
		case R.id.surface_view:
			if(videoTime.getVisibility()==View.VISIBLE){
				show(View.GONE);
			}else{
				show(View.VISIBLE);
			}
        	break;
		case R.id.back:
			finish();
			break;
		case R.id.video_play:
			if(player.isPlaying()){                    
				player.pause();  
        		videoPlay.setImageResource(R.drawable.video_play_select);
        		}else{ 
        			player.start();
        			videoPlay.setImageResource(R.drawable.video_pause_select);
        		}
			break;
		case R.id.video_next:
			next();
			break;
		case R.id.video_prew:

	    	try {
	    		if(isChild){
	        		if(index>0){
	        			time = 0;
	    	    		player.reset();
	        			index--;
	        			showTitle((listTrouble.get(index).getTime()));
	        			player.setDataSource(countService.getVideo(listTrouble.get(index).getName()));
	        			
	        			player.prepare();
	    	        	setPlayer();
	    	        	videoPlay.setImageResource(R.drawable.video_pause_select);
	        		}
	        	}else{
	        		if(index>0){
	        			time = 0;
	    	    		player.reset();
	        			index--;
	        			showTitle((listVideoInfo.get(index).getTime()));
		        		player.setDataSource(countService.getVideo(listVideoInfo.get(index).getName()));
		        		
		        		player.prepare();
			        	setPlayer();
			        	videoPlay.setImageResource(R.drawable.video_pause_select);
	        		}
	        	}
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			break;
		case R.id.is_love:
			if(countService!=null){
				if(isChild){
					File file = new File(TSDConst.CAR_DVR_VIDEO_PATH+"/"+listTrouble.get(index).getName()+".mp4");
					if(file.exists()){
						if(countService.getFavoriteStatus(listTrouble.get(index).getName())){
							isLove.setImageResource(R.drawable.is_unlove);
							countService.setFavoriteVideo(listTrouble.get(index).getName(), false);
						}else{
							isLove.setImageResource(R.drawable.is_love);
							countService.setFavoriteVideo(listTrouble.get(index).getName(), true);
						}
					}else{
						Toast.makeText(this, R.string.no_video, Toast.LENGTH_SHORT).show();
					}
				}else{
					File file = new File(TSDConst.CAR_DVR_VIDEO_PATH+"/"+listVideoInfo.get(index).getName()+".mp4");
					if(file.exists()){
						if(countService.getFavoriteStatus(listVideoInfo.get(index).getName())){
							isLove.setImageResource(R.drawable.is_unlove);
							countService.setFavoriteVideo(listVideoInfo.get(index).getName(), false);
						}else{
							isLove.setImageResource(R.drawable.is_love);
							countService.setFavoriteVideo(listVideoInfo.get(index).getName(), true);
						}
					}else{
						Toast.makeText(this, R.string.no_video, Toast.LENGTH_SHORT).show();
					}
				}
			}
			break;
		}
	}
	
	private void next(){
		try {
    		if(isChild){
        		if(index<listTrouble.size()-1){
        			time = 0;
    	    		player.reset();
    	    		index++;
        			showTitle((listTrouble.get(index).getTime()));
        			player.setDataSource(countService.getVideo(listTrouble.get(index).getName()));
        			player.prepare();
    	        	setPlayer();
    	        	videoPlay.setImageResource(R.drawable.video_pause_select);
        		}
        	}else{
        		if(index<listVideoInfo.size()-1){
        			time = 0;
    	    		player.reset();
        			index++;
	        		showTitle((listVideoInfo.get(index).getTime()));
	        		player.setDataSource(countService.getVideo(listVideoInfo.get(index).getName()));
	        		player.prepare();
		        	setPlayer();
		        	videoPlay.setImageResource(R.drawable.video_pause_select);
        		}
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	

	@Override
	public void onCompletion(MediaPlayer mp) {
		playNext();
	}
	
	private void playNext() {

    	try {
    		videoPlay.setImageResource(R.drawable.video_play_select);
    		time = 0;
    		showPlayTime(time);
    		seekBar.setProgress(time);
    		player.reset();
    		if(isChild){
        		if(index<listTrouble.size()-1){
        			index++;
        			showTitle((listTrouble.get(index).getTime()));
        			player.setDataSource(countService.getVideo(listTrouble.get(index).getName()));
        			player.prepare();
                	setPlayer();
//        		}else{
//        			showTitle((listTrouble.get(index).getTime()));
//        			player.setDataSource(countService.getVideo(listTrouble.get(index).getName()));
//        			player.prepare();
        		}
        	}else{
        		if(index<listVideoInfo.size()-1){
        			index++;
        			showTitle((listVideoInfo.get(index).getTime()));
            		player.setDataSource(countService.getVideo(listVideoInfo.get(index).getName()));
            		player.prepare();
                	setPlayer();
        		}
//        		if(index>0){
//        			
//        		}else{
//        			showTitle((listVideoInfo.get(index).getTime()));
//            		player.setDataSource(countService.getVideo(listVideoInfo.get(index).getName()));
//            		player.prepare();
//        		}
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	private void setPlayer(){
		if(player!=null){
			player.start();
			seekBar.setMax(player.getDuration());
			videoPlay.setImageResource(R.drawable.video_pause_select);
			
			if(isChild){
				if(countService!=null){
					if(countService.getIsAccident(listTrouble.get(index).getName())){
						isLove.setImageResource(R.drawable.isevent);
						isLove.setEnabled(false);
					}else if(countService.getFavoriteStatus(listTrouble.get(index).getName())){
						isLove.setImageResource(R.drawable.is_love);
					}else{
						isLove.setImageResource(R.drawable.is_unlove);
					}
				}
				try {
					videoAddress.setText(listTrouble.get(index).getAddress());
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(videoAddress.getText().equals("")){
					videoAddress.setText(R.string.no_address);
				}
			}else{
				if(countService!=null){
					try {
						if(countService.getIsAccident(listVideoInfo.get(index).getName())){
							isLove.setImageResource(R.drawable.isevent);
							isLove.setEnabled(false);
						}else if(countService.getFavoriteStatus(listVideoInfo.get(index).getName())){
							isLove.setImageResource(R.drawable.is_love);
						}else{
							isLove.setImageResource(R.drawable.is_unlove);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					videoAddress.setText(VideoManager.getInstance(this).getVideo(listVideoInfo.get(index).getName()).address);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(videoAddress.getText().equals("")){
					videoAddress.setText(R.string.no_address);
				}
			}
			
		}
	}
	

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int progress = seekBar.getProgress();
        if (player != null) {
        	player.seekTo(progress);
            int current = player.getCurrentPosition();
            seekBar.setProgress(current);
            time = current/1000;
            showPlayTime(time);
        }
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		Intent it = new Intent();
		it.setAction(CommonMessage.EVT_VIDEO_PLAYING_BEGIN);
		sendBroadcast(it);
		try {
			if(player!=null){
				if(!player.isPlaying()){
					player.start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		Intent it = new Intent();
		it.setAction(CommonMessage.EVT_VIDEO_PLAYING_END);
		sendBroadcast(it);
		try {
			if(player!=null){
				player.pause();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() { 
		 
		@Override  
		public void onServiceConnected(ComponentName name, IBinder service) {  
			countService = (IInterfaceService) service; 
			Message msg = new Message();
			msg.what = 1;
			moveHandler.sendMessage(msg);
		} 
		  
		@Override  
		public void onServiceDisconnected(ComponentName name) { 
		   countService = null;  
		} 
	}; 
	
	
	@SuppressLint("HandlerLeak")
	private Handler moveHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				
				break;
			default:
				break;
			}
		}
	};

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		playNext();
		return false;
	}
}