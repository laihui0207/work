package com.tuyou.tsd.cardvr.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.service.videoMeta.PictureInfoDAO;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.TSDLocation;
import com.tuyou.tsd.common.UploadInf;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.network.SubmitAccidentInfoReq;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.common.videoMeta.PictureInf;
import com.tuyou.tsd.common.videoMeta.VideoInf;
import com.tuyou.tsd.common.videoMeta.VideoManager;
import com.tuyou.tsd.common.videoMeta.VideoStatInf.VIDEO_TYPE;

public class VideoRec extends Service implements SurfaceHolder.Callback, TakePictureCallback{
	private static final String TAG = "BackgroundVideoRecorder";
	
	private static final int MIN_FREE_MEM_PERCENT = 10;    	// 10% 
    private static final int REC_DURATION = 10 * 1000;  	// 10s
    private IInterfaceService countService;
    
    private static final String VIDEO_FILE_SUFFIX = ".mp4";
    private static final String THUMBNAIL_FILE_SUFFIX = ".jpg";
    
    private String VIDEO_PATH = TSDConst.CAR_DVR_VIDEO_PATH + "/";
    private String PICTURES_PATH = TSDConst.CAR_DVR_PICTURES_PATH + "/";
    
    private static final int SERVICE_RUNNING = 0;
//    private static final int NO_SD_CARD_WARNING = 1;
    private static final int SD_CARD_MEM_NOT_ENOUGH_WARNING = 2;

	private WindowManager mWindowManager;
    private SurfaceView mSurfaceView;
    private Camera mCamera = null;
    private MediaRecorder mMediaRecorder = null;
    private MyBroadcastReceiver mBroadcastReceiver;
    private MyBroadcastSDReceiver mBroadcastSDReceiver;
    
    private boolean mIsInAccident = false;
    private boolean mIsInAlert = false;
    private String mPreRecordFilePath = null;
    private String mCurRecordFilePath = null;
    private int  mLockIndex = 0;  //Used for accident or taking picture with locking video.
    //For the upload of video or image. 
    private String mDeviceId = null;
    private String mLat = "0.0";
    private String mLng = "0.0";
    private String mDistrict="";
    private String mAddress="";
    private String mTimestamp = null;
    //For upload the accident information.
    private SubmitAccidentInfoReq mAccident;
    private ArrayList<String> mAccidentVideoList = new ArrayList<String>();
    protected enum RUNNING_MODE{
    	IDLE,
    	RECORDING,
    	TAKINGPICTURE,
    	CAM_PREVIEWING
    }; 
    
    private RUNNING_MODE mRunningMode = RUNNING_MODE.IDLE;
    private RUNNING_MODE mPreRunningMode = RUNNING_MODE.IDLE;
    private int VIDEO_QUALITY = CamcorderProfile.QUALITY_720P;
    private int time = 0;
    private int SHOW_TIME = 5*1000;
    private AlertDialog.Builder builder;
    private AlertDialog ad;
    private Button ok;
    
    private ServiceConnection serviceConnection = new ServiceConnection() { 
		 
		@Override  
		public void onServiceConnected(ComponentName name, IBinder service) {  
			countService = (IInterfaceService) service; 
		} 
		  
		@Override  
		public void onServiceDisconnected(ComponentName name) { 
		   countService = null;  
		} 
	}; 
    
    @Override
    public void onCreate() {
    	// Create new SurfaceView, set its size to 1x1, move it to the top left corner and set this service as a callback
    	this.bindService( new Intent("com.tuyou.tsd.cardvr.service.InterfaceService" ), this.serviceConnection, BIND_AUTO_CREATE);
		mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
    	mSurfaceView = new SurfaceView(this);
        LayoutParams layoutParams = new WindowManager.LayoutParams(
            1, 1,
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        mWindowManager.addView(mSurfaceView, layoutParams);
        mSurfaceView.getHolder().addCallback(this);
        
        mBroadcastReceiver = new MyBroadcastReceiver();
        registerBroadcasts();
               
        cancelNotification();
        
    	if (mDeviceId == null) {
			mDeviceId = (String) HelperUtil.readFromCommonPreference(getApplicationContext(), "device_id", "string");
		}
    	
    	this.startService(new Intent("com.tuyou.tsd.cardvr.service.InterfaceService"));
    }
    private void showTTS(String text){
		Intent intent= new Intent(CommonMessage.TTS_PLAY);
		intent.putExtra("content", text);
		intent.putExtra("notify", false);
		sendBroadcast(intent);	
    }
    
    private void showDialog(String text){
    	if(builder==null){
    		builder = new AlertDialog.Builder(this);
    		View layout=LayoutInflater.from(this).inflate(R.layout.show_win, null);
        	builder.setView(layout);
        	ok = (Button) layout.findViewById(R.id.ok);
        	TextView title = (TextView) layout.findViewById(R.id.title);
        	title.setText(text);
        	ok.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ad.cancel();
					mHandler.removeMessages(1);
				}
			});
    	}
    	ok.setText("关闭"+time+"s");
    	time --;
    	if(ad==null){
    		ad = builder.create();
    		ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        	ad.setCanceledOnTouchOutside(false); 
    	}
    	if(!ad.isShowing()){
    		ad.show();
    	}
    	if(time>0){
    		Message msg = mHandler.obtainMessage();
    		msg.what = 1;
    		msg.obj =text;
    		mHandler.sendMessageDelayed(msg, 1000);
    	}else{
    		ad.cancel();
    	}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//    	super.onStartCommand(intent, flags, startId);
//    	return START_STICKY;
    	flags = START_STICKY; 
    	return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    	//recording(surfaceHolder, CamcorderProfile.QUALITY_480P);
    }
    
    
    @SuppressLint("HandlerLeak")
	protected  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
			case 1:
				mHandler.removeMessages(1);
				showDialog(String.valueOf(msg.obj));
				break;
			case 2:
				if(ad!=null){
					mHandler.removeMessages(1);
					ad.cancel();
				}
				mRunningMode = RUNNING_MODE.IDLE;
				mIsInAlert = false;
    			releaseCamera();
    			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.cancelAll();
				break;
			
			case 4:
				if(countService!=null){
					toDoVideo();
				}else{
					Message mes2 = new Message();
					mes2.what = 4;
	        		mHandler.sendMessageAtTime(mes2, SHOW_TIME);
				}
				break;
			default:
				break;
			}
        }
    };
    
    private void registerBroadcasts() {
    	IntentFilter intentFilter = new IntentFilter();
    	
    	intentFilter.addAction(TSDEvent.System.LOCATION_UPDATED);
    	intentFilter.addAction(TSDEvent.CarDVR.START_REC);
    	intentFilter.addAction(TSDEvent.CarDVR.STOP_REC);
    	intentFilter.addAction(TSDEvent.CarDVR.ALERT_TRIGGERED);
    	intentFilter.addAction(TSDEvent.CarDVR.ACCIDENT_TRIGGERED);
    	intentFilter.addAction(TSDEvent.CarDVR.TAKE_PICTURE);
    	intentFilter.addAction(TSDEvent.CarDVR.START_CAM_PREVIEW);
    	intentFilter.addAction(TSDEvent.CarDVR.STOP_CAM_PREVIEW);
    	intentFilter.addAction(TSDEvent.System.ACC_OFF);
    	intentFilter.addAction(TSDEvent.CarDVR.CLEAR_PHOTO);
    	intentFilter.addAction(Intent.ACTION_SHUTDOWN);
    	registerReceiver(mBroadcastReceiver, intentFilter);
    	
    	registerSDBroadcasts();
    }
    
    private void registerSDBroadcasts() {
    	
    	mBroadcastSDReceiver = new MyBroadcastSDReceiver();
    	IntentFilter iFilter = new IntentFilter();
    	iFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
    	iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
    	iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
    	iFilter.addDataScheme("file");
    	registerReceiver(mBroadcastSDReceiver , iFilter);

	}
	/**
     * @param surfaceHolder
     */
    void recording(final SurfaceHolder surfaceHolder, int videoQuality) {
		System.out.println("Enter recording");
    	try {
        	try{ 
        		if(mCamera == null) {
        			mCamera = Camera.open();  
        		}
        	}  catch(RuntimeException e){   
        		e.printStackTrace();
        	}
        	
        	if(mMediaRecorder == null) { 
    			mMediaRecorder = new MediaRecorder();
    		    mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        	}
//        	mCamera.lock();
        	mCamera.unlock();
        	mMediaRecorder.setCamera(mCamera);
        	mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        	CamcorderProfile profile = CamcorderProfile.get(videoQuality);
        	mMediaRecorder.setOutputFormat(profile.fileFormat);
        	mMediaRecorder.setVideoEncoder(profile.videoCodec);
        	mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate );//163840
        	mMediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        	mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            mMediaRecorder.setMaxDuration(REC_DURATION);
            
            File destDir = new File(VIDEO_PATH);
            if (!destDir.exists() && !destDir.mkdirs()) {
//            	sendNotification(NO_SD_CARD_WARNING);
            	return;
            }
            mPreRecordFilePath = mCurRecordFilePath;
            long timestamp = new Date().getTime();
            String fileName = DateFormat.format("yyyyMMddkkmmss", timestamp) + VIDEO_FILE_SUFFIX;
            
            mCurRecordFilePath = VIDEO_PATH + fileName;
            
            boolean isFavorite = false;
    		if (mLockIndex == 1) {
    			if(mIsInAccident) {	//Accident
    				VideoManager.getInstance(getApplicationContext()).setAccidentVideo(HelperUtil.getFileNameFromFullPathName(mPreRecordFilePath, VIDEO_FILE_SUFFIX));
    				VideoManager.getInstance(getApplicationContext()).setAccidentVideo(HelperUtil.getFileNameFromFullPathName(mCurRecordFilePath, VIDEO_FILE_SUFFIX));
    				mAccidentVideoList.add(mPreRecordFilePath);
    				mAccidentVideoList.add(mCurRecordFilePath);
    			} else {	//Favorite
    				VideoManager.getInstance(getApplicationContext()).setFavoriteVideo(HelperUtil.getFileNameFromFullPathName(mPreRecordFilePath, VIDEO_FILE_SUFFIX));
    				VideoManager.getInstance(getApplicationContext()).setFavoriteVideo(HelperUtil.getFileNameFromFullPathName(mCurRecordFilePath, VIDEO_FILE_SUFFIX));
    				isFavorite = true;
    			}
    			mLockIndex++;
    		} else if (mLockIndex > 1) {
    			if (mIsInAccident) {
    				mIsInAccident = false;	
    			}
    			mLockIndex = 0;
    		}
    		
            VideoInf videoInf = new VideoInf();
            videoInf.name = DateFormat.format("yyyyMMddkkmmss", timestamp) + "";

            if (mIsInAccident) {
            	videoInf.tag = VideoInf.UNOPEN_FLAG|VideoInf.ACCIDENT_FLAG;
            } else if (isFavorite) {
            	videoInf.tag = VideoInf.FAVORITE_FLAG;
            } else {
            	videoInf.tag = 0;
            }
            videoInf.location.lng = mLng;
            videoInf.location.lat = mLat;
            videoInf.address=mAddress;
            videoInf.district=mDistrict;
            videoInf.timestamp = timestamp;
            VideoManager.getInstance(getApplicationContext()).addVideo(videoInf);
            
            mMediaRecorder.setOutputFile(mCurRecordFilePath);
            mMediaRecorder.setOnInfoListener(new OnInfoListener() {
            	@Override 
                public void onInfo(MediaRecorder mr, int what, int extra) { 
            		if(MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what) {
            			LogUtil.v(TAG, "MEDIA_RECORDER_INFO_MAX_DURATION_REACHED");
            			
            			//Check and upload accident information if needed.
            			if (mIsInAccident && mLockIndex == 2) {
            				uploadAccidentInf();
            			}
            			
            			continueRecording(surfaceHolder);
            		}
                } 
            });
            
            mMediaRecorder.setOnErrorListener(new OnErrorListener() {
    			@Override
    			public void onError(MediaRecorder mr, int what, int extra) {
    				LogUtil.e(TAG, "onError, arg1: " + what + ", arg2: " + extra);
    				if (what == MediaRecorder.MEDIA_ERROR_SERVER_DIED 
    				  ||what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
    					handleMediaRecordError(surfaceHolder);
    				}
    			}
            });
            LogUtil.v(TAG, Environment.getExternalStorageDirectory().toString());
            mMediaRecorder.prepare(); 
            mMediaRecorder.start();
		} catch (Exception e) {
			e.printStackTrace();
			mHandler.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	            	releaseCamera();
	            }
	        }, 1000);
			mHandler.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	            	continueRecording(surfaceHolder);
	            }
	        }, 10*1000);
		}
	
    }
    
    private void continueRecording(SurfaceHolder surfaceHolder) {
    	if( mMediaRecorder != null) {
    		resetMediaRecorder();
    	}    
        
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	checkAndFreeMem();
            }
        }, 500);
        
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	generateThumbnail();
            }
        }, 1000);
        
        recording(surfaceHolder, VIDEO_QUALITY);
    }
    
    private void handleMediaRecordError(final SurfaceHolder surfaceHolder) {
    	if( mMediaRecorder != null) {
    		releaseCamera();
    	}    
        
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	recording(surfaceHolder, VIDEO_QUALITY);
            }
        }, 1000);
    }
    
    // Stop recording and remove SurfaceView
    @Override
    public void onDestroy() {
    	cancelNotification();
    	releaseCamera();
        try {
        	mWindowManager.removeView(mSurfaceView);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        try {
        	unregisterReceiver(mBroadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
        try {
        	unregisterReceiver(mBroadcastSDReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
//        System.out.println("======videoRec停止");
        Intent localIntent = new Intent(); 
        localIntent.setClass(this, VideoRec.class);
        this.startService(localIntent); 
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    	 try {
    		 mCamera.setPreviewCallback(null);
    		 mCamera.stopPreview();  
        	 mCamera.release();    
        	 mCamera = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
    
    private boolean isSDCardMemEnough() {
    	return new File(TSDConst.CAR_ROOT_PATH).getUsableSpace() > new File(TSDConst.CAR_ROOT_PATH).getTotalSpace() * MIN_FREE_MEM_PERCENT/100;
    }
    
    private void checkAndFreeMem() {
    	LogUtil.v(TAG, "Enter check and free mem.");
    	
    	//Check if the memory is enough.
    	 File oldestFile = null;
		 long oldestModifiedTime = 0;
    	 while(!isSDCardMemEnough()) {
		    File file = new File(VIDEO_PATH) ;
		    if(!file.exists()) {
		    	return;
		    }
		    
		    File fileList[] = file.listFiles();
		    
		    int i = 0;
		    oldestFile = null;
		    oldestModifiedTime = 0;
		    
			for (i = 0; i < fileList.length; i++) {
				if (fileList[i].isFile()&& fileList[i].getName().endsWith(VIDEO_FILE_SUFFIX)){
					String filename=fileList[i].getName().substring(0,fileList[i].getName().lastIndexOf("."));
					VIDEO_TYPE type = VideoManager.getInstance(getApplicationContext()).getVideoType(filename);
					if (type == VIDEO_TYPE.NORMAL) {
					oldestFile = fileList[i];
					oldestModifiedTime = fileList[i].lastModified();
					break;
					}
				}
			}

			if (i == fileList.length) {
				sendNotification(SD_CARD_MEM_NOT_ENOUGH_WARNING);
				break;
			}

			for (int j = i + 1; j < fileList.length; j++) {
				if (fileList[j].isFile()&& fileList[j].getName().endsWith(VIDEO_FILE_SUFFIX)&& fileList[j].lastModified() < oldestModifiedTime) {
					String filename=fileList[j].getName().substring(0,fileList[j].getName().lastIndexOf("."));
					VIDEO_TYPE type = VideoManager.getInstance(getApplicationContext()).getVideoType(filename);
					if (type == VIDEO_TYPE.NORMAL) {
						oldestModifiedTime = fileList[j].lastModified();
						oldestFile = fileList[j];
					}
				}
			}
		    
		    if (oldestFile != null) {
		    	String fileName = HelperUtil.getFileNameFromFullPathName(oldestFile.getName(), VIDEO_FILE_SUFFIX);
			    VideoManager.getInstance(getApplicationContext()).deleteVideo(fileName);
			    LogUtil.v(TAG, "Delete the file " + oldestFile.getName());
		    }
    	}
    	LogUtil.v(TAG, "Leave check and free mem.");
    }
    
    private void generateThumbnail() {
    	if (this.mPreRecordFilePath != null) {
    		Bitmap bitmap = null;  
            bitmap = ThumbnailUtils.createVideoThumbnail(mPreRecordFilePath, Thumbnails.MICRO_KIND);
            if(bitmap != null) {
	            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 96, 96,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
	            
	            String thumbnailFile = mPreRecordFilePath.replace(VIDEO_FILE_SUFFIX, THUMBNAIL_FILE_SUFFIX);
				File bitmapFile = new File(thumbnailFile);
	
				if (!bitmapFile.exists()) {
					try {
						bitmapFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(bitmapFile);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
    	}
    }
    
    protected class MyBroadcastSDReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction(); 
			if(action.equals(Intent.ACTION_MEDIA_EJECT)||action.equals(Intent.ACTION_MEDIA_REMOVED)){  
				time = 6;
    			showTTS(getString(R.string.sd_sdeject));
    			showDialog(getString(R.string.sd_sdeject));
        		Message msg = new Message();
        		msg.what = 1;
        		mHandler.sendMessageDelayed(msg, SHOW_TIME);
        		cancelNotification();
        		releaseCamera();
    			mRunningMode = RUNNING_MODE.IDLE;
			}else if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
				
				Message msg = new Message();
        		msg.what = 4;
        		mHandler.sendMessageDelayed(msg, SHOW_TIME);
			}
		}
    	
    };
    
    protected class MyBroadcastReceiver extends BroadcastReceiver {  
        @Override  
        public void onReceive(Context context, Intent intent) {
        	try {
            	String action;
            	
        		if (intent == null || (action = intent.getAction()) == null) {
        	        LogUtil.v(TAG, "Received intent with empty action");
        		    return;
        		}
        		
        		LogUtil.v(TAG, "MyBroadcastReceiver, " + action);
        		
        		//ACC-ON message 
        		if (TSDEvent.CarDVR.START_REC.equals(action)) {
        			toDoVideo();
        		}
        		//ACC-OFF message
        		else if (TSDEvent.CarDVR.STOP_REC.equals(action)) {
        			//Reset the lock index.
        			mLockIndex = 0;
        			cancelNotification();
        			
        			if (mRunningMode == RUNNING_MODE.CAM_PREVIEWING) {
        				LogUtil.w(TAG, "It's running cam_previewing.");
        				mRunningMode = RUNNING_MODE.IDLE;
        			} else if (mRunningMode == RUNNING_MODE.RECORDING) {
//        				cancelNotification();
        				releaseCamera();
    	    			mRunningMode = RUNNING_MODE.IDLE;
    	    			
    	    			//Check and upload accident information if needed.
    	    			if (mIsInAccident) {
    	    				mAccidentVideoList.add(mCurRecordFilePath);
    	    				uploadAccidentInf();
    	    			}
        			} else {
        				LogUtil.w(TAG, "The running mode is not RECORDING, ignored");
        			}
        		} else if (TSDEvent.CarDVR.ALERT_TRIGGERED.equals(action)) {
        			if (mRunningMode == RUNNING_MODE.CAM_PREVIEWING) {
        				LogUtil.w(TAG, "It's running cam_previewing, ignored");
        			} else if (mIsInAlert || mRunningMode != RUNNING_MODE.IDLE) {
        				if (mIsInAlert) {
        					LogUtil.w(TAG, "It's running alert, ignored");
        				} else {
        					LogUtil.w(TAG, "It's running recording or taking pic, ignored");
        				}
        			} else {
        				mIsInAlert = true;
        				parseUploadInfFromIntent(intent);
        				handleAlertEvent();
        			}
        		} else if (TSDEvent.CarDVR.ACCIDENT_TRIGGERED.equals(action)) {
        			if (mRunningMode == RUNNING_MODE.CAM_PREVIEWING) {
        				LogUtil.w(TAG, "It's running cam_previewing, ignored");
        			} else {
        				if(intent.hasExtra("data")) {
        					mAccident = (SubmitAccidentInfoReq)intent.getParcelableExtra("data");
        				} else {
        					mAccident = null;
        				}
        				mAccidentVideoList.clear();
        				handleAccidentEvent();
        			}
        		} else if (TSDEvent.CarDVR.TAKE_PICTURE.equals(action)) {
        			File file = new File(TSDConst.CAR_ROOT_PATH);
        	    	if(file.getTotalSpace()>0){
            			if (mRunningMode == RUNNING_MODE.IDLE) {
            				LogUtil.w(TAG, "Current state is ACC-OFF, ignore taking picture action.");
            			} else if (mRunningMode == RUNNING_MODE.CAM_PREVIEWING) {
            				LogUtil.w(TAG, "It's running cam_previewing, ignored");
            			} else if (mRunningMode != RUNNING_MODE.TAKINGPICTURE && !mIsInAlert) {
            				parseUploadInfFromIntent(intent);
            				handleTakePicture();
            			} else {
            				if (mIsInAlert) {
            					LogUtil.w(TAG, "It's running alert, ignored");
            				} else {
            					LogUtil.w(TAG, "The running mode is already TAKINGPICTURE, ignored");
            				}
            			}
        			}else{
        				Intent it= new Intent(CommonMessage.TTS_CLEAR);
        				sendBroadcast(it);
        				showTTS(getString(R.string.cannot_pic));
        				// 在不能拍照时也应发送消息通知CoreService退出拍照模式
        				sendBroadcast(new Intent(TSDEvent.CarDVR.PICTURE_TAKEN_COMPLETED));
        			}
        		} else if (TSDEvent.CarDVR.START_CAM_PREVIEW.equals(action)) {
        			if(mMediaRecorder!=null){
            			try {
                			Intent resIntent = new Intent();
                			resIntent.setAction(TSDEvent.CarDVR.CAM_AVAILABLE);
                			
                			if (mRunningMode == RUNNING_MODE.IDLE) {
                				resIntent.putExtra(TSDConst.CAR_DVR_START_CAM_PREVIEW_RES, true);
                				LogUtil.d(TAG, "Send broadcast: " + resIntent);
                				sendBroadcast(resIntent);
                			} else if (mRunningMode == RUNNING_MODE.RECORDING) {
                				if(!mIsInAlert && !mIsInAccident) {
            	    				cancelNotification();
            	    				releaseCamera();
            		    			resIntent.putExtra(TSDConst.CAR_DVR_START_CAM_PREVIEW_RES, true);
                				} else { //It's in alert or accident, we will ignore the message.
                					resIntent.putExtra(TSDConst.CAR_DVR_START_CAM_PREVIEW_RES, false);
                				}
                				LogUtil.d(TAG, "Send broadcast: " + resIntent);
                				sendBroadcast(resIntent);
                			}
                			mRunningMode = RUNNING_MODE.CAM_PREVIEWING;
                		
        				} catch (Exception e) {
        					e.printStackTrace();
        				}finally{
        					try {
        						if(mMediaRecorder!=null){
        							releaseCamera();
        						}
    						} catch (Exception e2) {
    							e2.printStackTrace();
    						}
        				}
            		
        			}
        		} else if (TSDEvent.CarDVR.STOP_CAM_PREVIEW.equals(action)) {
        			Toast.makeText(getApplicationContext(), "接收到录像广播", Toast.LENGTH_SHORT).show();
        			if (mRunningMode == RUNNING_MODE.CAM_PREVIEWING) {
        				File file = new File(TSDConst.CAR_ROOT_PATH);
            	    	if(file.getTotalSpace()>0){
            	    		playVideo();
            				mRunningMode = RUNNING_MODE.RECORDING;
            				sendNotification(SERVICE_RUNNING);
            	    	}
        			}
        		} else if (TSDEvent.System.LOCATION_UPDATED.equals(action)) {
        			TSDLocation location = intent.getParcelableExtra("location");
        			mLat = String.valueOf(location.getLatitude());
        			mLng = String.valueOf(location.getLongitude());
        			mAddress=location.getAddrStr();
        			mDistrict=location.getDistrict();
        		}else if(TSDEvent.System.ACC_OFF.equals(action)){
        			Message msg = mHandler.obtainMessage();
        			msg.what= 2;
        			mHandler.sendMessage(msg);
        		}else if(TSDEvent.CarDVR.CLEAR_PHOTO.equals(action)){
        			String id = intent.getExtras().getString("id");
        			String[] ids = id.split("id=");
        			for(int i=0;i<ids.length;i++){
        				if(!ids[i].equals("")){
        					File file ;
        					if(ids[i].length()>=14){
        						file = new File(TSDConst.CAR_DVR_PICTURES_PATH+"/"+ids[i].substring(0, 14)+".jpg");
        						PictureInfoDAO.getInstance(VideoRec.this).delete(Long.valueOf(ids[i].substring(0, 14)));
        					}else{
        						file = new File(TSDConst.CAR_DVR_PICTURES_PATH+"/"+ids[i]+".jpg");
        						PictureInfoDAO.getInstance(VideoRec.this).delete(Long.valueOf(ids[i]));
        					}
                			if(file.exists()){
                				file.delete();
                			}
        				}
        			}
        			
        		}else if(action.equals(Intent.ACTION_SHUTDOWN)){
        			unregisterReceiver(mBroadcastSDReceiver);
    				mHandler.removeMessages(1);
    				ad.cancel();
    			}
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
    
    private boolean videoSize(){
    	JSONArray array = countService.getVideoStats();
		int size = array.length();
		int num =0;
		try {
			for(int i=0;i<size;i++){
				JSONObject obj = array.getJSONObject(i);
				if(!obj.getString("type").equals("normal")){
					num++;
				}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long total =new File(TSDConst.CAR_ROOT_PATH).getTotalSpace();
		if(10*1024*num<total*0.8){
			return true;
		}
		return false;
    }
    
    private void playVideo(){
    	if(videoSize()){
	    	recording(mSurfaceView.getHolder(), VIDEO_QUALITY);
			mRunningMode = RUNNING_MODE.RECORDING;
			sendNotification(SERVICE_RUNNING);
	    }else{
	    	time = 6;
    		showTTS(getString(R.string.sd_is_much));
    		showDialog(getString(R.string.sd_is_much));
    		Message msg = new Message();
    		msg.what = 1;
    		mHandler.sendMessageAtTime(msg, SHOW_TIME);
	    }
    }
    
	private void toDoVideo() {
		File file = new File(TSDConst.CAR_ROOT_PATH);
    	if(file.getTotalSpace()>0){
			if (mRunningMode == RUNNING_MODE.CAM_PREVIEWING) {
				LogUtil.w(TAG, "It's running cam_previewing, ignored");
			} else if (mRunningMode == RUNNING_MODE.IDLE && !mIsInAlert) {
				mPreRecordFilePath = null;
			    mCurRecordFilePath = null;
				//checkAndFreeMem();
			    playVideo();
			} else {
				if (mIsInAlert) {
					LogUtil.w(TAG, "It's running alert, ignored");
				} else {
					LogUtil.w(TAG, "The running mode is not IDLE, ignored");
				}
			}
    	}else{
    		time = 6;
    		showTTS(getString(R.string.photo_no_sd));
    		showDialog(getString(R.string.photo_no_sd));
    		Message msg = new Message();
    		msg.what = 1;
    		mHandler.sendMessageAtTime(msg, SHOW_TIME);
    	}
	}  
    
    private void parseUploadInfFromIntent(Intent intent) {
    	/*
    	if(intent.hasExtra(TSDConst.UPLOAD_LOC_LAT)) {
    		mLat = intent.getStringExtra(TSDConst.UPLOAD_LOC_LAT);
    	}
    	
    	if (intent.hasExtra(TSDConst.UPLOAD_LOC_LNG)) {
    		mLng = intent.getStringExtra(TSDConst.UPLOAD_LOC_LNG);
    	}
    	*/     	    	
    	if (intent.hasExtra(TSDConst.UPLOAD_TIME_STAMP)) {
    		mTimestamp = intent.getStringExtra(TSDConst.UPLOAD_TIME_STAMP);
    	} else {
    		mTimestamp = HelperUtil.getCurrentTimestamp();
    	}
    	
    	if (mDeviceId == null) {
			mDeviceId = (String) HelperUtil.readFromCommonPreference(getApplicationContext(), "device_id", "string");
		}
    }
    
    private void uploadAccidentInf() {
    	LogUtil.d(TAG, "Enter uploadAccidentInf.");
    	if(mAccident != null) {
	    	mAccident.files  = (String[])mAccidentVideoList.toArray(new String[mAccidentVideoList.size()]);
	    	new UploadAccidentInfTask().execute(mAccident);
    	}
    	LogUtil.d(TAG, "Leave uploadAccidentInf.");
    }
    
    private void handleAccidentEvent() {
    	mIsInAccident = true;
    	LogUtil.d(TAG, "Enter handleAccidentEvent.");
    	if (mPreRecordFilePath != null) {
    		String fileName = HelperUtil.getFileNameFromFullPathName(mPreRecordFilePath, VIDEO_FILE_SUFFIX);
    		VideoManager.getInstance(getApplicationContext()).setAccidentVideo(fileName);
    		mAccidentVideoList.add(mPreRecordFilePath);
    	}
    	
    	mLockIndex = 1;
    	LogUtil.d(TAG, "Leave handleAccidentEvent.");
    }
    
    private void handleTakePicture() {
    	mPreRunningMode = mRunningMode;
    	mRunningMode = RUNNING_MODE.TAKINGPICTURE;
    	takePicture();
    	
    	//If the device is in accident, will ignore locking the videos as favorite.
    	if(!mIsInAccident) {
	    	Context shareContext;
	    	//Check whether need to lock the videos like as accident.
			try {
				shareContext = createPackageContext(TSDComponent.CORE_SERVICE_PACKAGE, 0);
				SharedPreferences pref = shareContext.getSharedPreferences(getPackageName(), Context.MODE_MULTI_PROCESS);
				if(pref.getBoolean(TSDEvent.CarDVR.PHOTO_RECORD, false)==true) {
					if (mPreRecordFilePath != null) {
			    		String fileName = HelperUtil.getFileNameFromFullPathName(mPreRecordFilePath, VIDEO_FILE_SUFFIX);
			    		VideoManager.getInstance(getApplicationContext()).setFavoriteVideo(fileName);
			    	}
					mLockIndex = 1;
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
    	}
    }
    
    private void takePicture() {
    	if (mCamera == null) {
	    	mCamera = Camera.open();
	    	
	    	try {
				mCamera.setPreviewDisplay(this.mSurfaceView.getHolder());
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	mCamera.startPreview();
    	}
    	mCamera.takePicture(null, null, pictureCallback); 
    }
    
    PictureCallback pictureCallback = new PictureCallback(this);
    
    class PictureCallback implements Camera.PictureCallback {
    	private TakePictureCallback listener;
    	
    	PictureCallback(TakePictureCallback callback) {
    		listener = callback;
    	}
        //@Override  
        public void onPictureTaken(byte[] data, Camera camera) {  
            new SavePictureTask(listener).execute(data);  
            camera.startPreview();  
        } 
        
    }
    
    
  
    class SavePictureTask extends AsyncTask<byte[], String, String> {
    	private TakePictureCallback takePictureCallback;
    	SavePictureTask(TakePictureCallback callback) {
    		this.takePictureCallback = callback;
    	};
    	
    	@Override
    	protected void onPostExecute(String result) {
    		super.onPostExecute(result);
//    		mCamera.setPreviewCallback(null);
//    		mCamera.stopPreview(); 
//       	 	mCamera.release();
//       	    mCamera = null;
    	}
    	
        @Override  
        protected String doInBackground(byte[]... params) {  
//        	System.out.println("picture background start =============");
            String pictureName=(String)DateFormat.format("yyyyMMddkkmmss", new Date().getTime());
        	String pictureFilePath = PICTURES_PATH + pictureName + THUMBNAIL_FILE_SUFFIX;
            
            File destDir = new File(PICTURES_PATH);
            if (!destDir.exists()) {
            	destDir.mkdirs();
            }
            LogUtil.i(TAG, "pictureFilePath =" + pictureFilePath);  
            try {  
                FileOutputStream fos = new FileOutputStream(pictureFilePath); // Get file output stream  
                fos.write(params[0]); // Write to the file  
                fos.close();
                takePictureCallback.onTakePictureCompleted();
                VideoManager.getInstance(getApplicationContext()).uploadFile(new UploadInf(Uri.parse(pictureFilePath), mDeviceId, mTimestamp, 
                		mLng, mLat,"mblog","test","test",mDistrict,mAddress));
                PictureInf entity=new PictureInf();
                entity.name=pictureName;
                entity.location.lng=mLng;
                entity.location.lat=mLat;
                entity.timestamp=new Date().getTime();
                entity.address=mAddress;
                entity.district=mDistrict;
                PictureInfoDAO.getInstance(VideoRec.this).save(entity);
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
//            System.out.println("picture background end =============");
            return null;  
        }  
    }
    
    class UploadAccidentInfTask extends AsyncTask<SubmitAccidentInfoReq, Void, Void> {
        @Override  
        protected Void doInBackground(SubmitAccidentInfoReq... params) {
        	JsonOA2.getInstance(getApplicationContext()).submitAccidentStatus(params[0]);
			return null;  
           
        }  
    }
    
    private void handleAlertEvent() {
    	mCamera = Camera.open();
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        
        mMediaRecorder.setPreviewDisplay(this.mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setCamera(mCamera);
        //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        CamcorderProfile profile = CamcorderProfile.get(VIDEO_QUALITY);
//        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QCIF)) {     
//        	LogUtil.v(TAG, "QCIF");
//        	profile = CamcorderProfile.get(CamcorderProfile.QUALITY_QCIF);
//        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF)) {
//        	LogUtil.v(TAG, "CIF");
//        	profile = CamcorderProfile.get(CamcorderProfile.QUALITY_CIF);
//        }
        mMediaRecorder.setOutputFormat(profile.fileFormat);
    	mMediaRecorder.setVideoEncoder(profile.videoCodec);
    	mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
    	mMediaRecorder.setVideoFrameRate(profile.videoFrameRate);
    	mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mMediaRecorder.setMaxDuration(REC_DURATION);
        

        
        final String alertFilePath = VIDEO_PATH + DateFormat.format("yyyyMMddkkmmss", new Date().getTime()) + VIDEO_FILE_SUFFIX;
        
        mMediaRecorder.setOutputFile(alertFilePath);
        mMediaRecorder.setOnInfoListener(new OnInfoListener() {
        	@Override 
            public void onInfo(MediaRecorder mr, int what, int extra) { 
        		if(MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what) {
        			LogUtil.v(TAG, "MEDIA_RECORDER_INFO_MAX_DURATION_REACHED");
//        			resetMediaRecorder();
        			mIsInAlert = false;
        			VideoManager.getInstance(getApplicationContext()).uploadFile(new UploadInf(Uri.parse(alertFilePath), mDeviceId, mTimestamp, mLng, mLat,"cardvr","","",mDistrict,mAddress));
        		}
            } 
        });
        
        mMediaRecorder.setOnErrorListener(new OnErrorListener() {
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				LogUtil.e(TAG, "onError, arg1: " + what + ", arg2: " + extra);
//				releaseCamera();
				mIsInAlert = false;
			}
        });

    }

    
    private void resetMediaRecorder() {
    	try {
	    	if (mMediaRecorder != null) {
		    	mMediaRecorder.stop();
		        mMediaRecorder.reset();
	    	}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private void releaseCamera() {
    	try {
	    	if (mMediaRecorder != null) {
	    		mMediaRecorder.setOnErrorListener(null);
	            mMediaRecorder.setPreviewDisplay(null);
		    	try {
		    		mMediaRecorder.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
		        mMediaRecorder.reset();
		        release();
		        mMediaRecorder.release();
		        mMediaRecorder = null;
	    	}
	    	
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
        
    }
    
    private void release(){
    	try {
	    	if (mCamera != null) {
	    		mCamera.setPreviewCallback(null);
		        mCamera.lock();
		        mCamera.release();
		        mCamera = null;
	    	} 
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	@Override
	public void onTakePictureCompleted() {
		LogUtil.v(TAG, "onTakePictureCompleted.");

		mRunningMode = mPreRunningMode;
    	mPreRunningMode = RUNNING_MODE.IDLE;

    	// 播放拍照音效
//		MediaPlayer player = MediaPlayer.create(this, R.raw.photo);
//		player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//			@Override
//			public void onCompletion(MediaPlayer mp) {
//				mp.release();
//				mp = null;
//			}
//		});
//		player.start();

		// 发送消息通知CoreService结束拍照模式
		Intent intent = new Intent();
		intent.setAction(TSDEvent.CarDVR.PICTURE_TAKEN_COMPLETED);
		LogUtil.d(TAG, "Send broadcast: " + intent);
		sendBroadcast(intent);
	}
	
	protected void sendNotification(int notificationType) {
		cancelNotification();
		NotificationCompat.Builder mBuilder;
		switch (notificationType) {
			case SERVICE_RUNNING:
				mBuilder = new NotificationCompat.Builder(this)
			        .setSmallIcon(R.drawable.ic_launcher)
			        .setContentTitle(getString(R.string.noti_server_running_title))
			        .setContentText(getString(R.string.noti_server_running_text));
				break;
				
//			case NO_SD_CARD_WARNING:
//				mBuilder = new NotificationCompat.Builder(this)
//			        .setSmallIcon(R.drawable.ic_launcher)
//			        .setContentTitle(getString(R.string.noti_no_sd_card_running_title))
//			        .setContentText(getString(R.string.noti_no_sd_card_running_text));
//				break;
			
			case SD_CARD_MEM_NOT_ENOUGH_WARNING:
				mBuilder = new NotificationCompat.Builder(this)
			        .setSmallIcon(R.drawable.ic_launcher)
			        .setContentTitle(getString(R.string.noti_sd_card_not_enough_mem_title))
			        .setContentText(getString(R.string.noti_sd_card_not_enough_mem_text));
				break;
			
			default:
				return;
		}
				
		Notification notification = mBuilder.build();
		if (notificationType == SERVICE_RUNNING) {
			notification.flags |= Notification.FLAG_NO_CLEAR;
		} else {
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		}
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notificationType, notification);
	}
	
	protected void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}
}