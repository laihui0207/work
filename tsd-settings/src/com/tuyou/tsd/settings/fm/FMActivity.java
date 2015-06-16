package com.tuyou.tsd.settings.fm;

import java.io.IOException;
import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.BaseActivity;
import com.tuyou.tsd.settings.base.SleepBaseActivity;
import com.tuyou.tsd.settings.base.SysApplication;

public class FMActivity extends SleepBaseActivity implements OnClickListener {
	// 为防止图片变形由原来的button改为imageview
	private ImageView playImageView;
	private ImageButton addButton, reduceButton;
	// 初始化频率
	private double frequency = 88.8;
	private TextView freTextView;
	private DecimalFormat df = new DecimalFormat("#.0");
	private boolean isLongClick = false, isPlay = false;
	private Thread addThread, reduceThread;
	private TextView back;
	private MediaPlayer mediaPlayer;
	private AnimationDrawable animationDrawable;
	private TextView musicTextView;
	private int longTime = 2, time = 0;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			freTextView.setText((String) msg.obj);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fm);
		SysApplication.getInstance().addActivity(this);
		init();
	}

	private void init() {
		freTextView = (TextView) findViewById(R.id.txt_layout_fm_value);
		musicTextView = (TextView) findViewById(R.id.txt_fm_music);
		if (pref != null) {
			frequency = Double.parseDouble(pref.getString("fm_freq", "88.8"));
			freTextView.setText(df.format(frequency));
		}
		playImageView = (ImageView) findViewById(R.id.img_layout_music_paly);
		addButton = (ImageButton) findViewById(R.id.img_layout_fm_add);
		reduceButton = (ImageButton) findViewById(R.id.img_layout_fm_reduce);
		back = (TextView) findViewById(R.id.btn_fm_back);
		back.setOnClickListener(this);
		playImageView.setOnClickListener(this);
		musicTextView.setOnClickListener(this);
		addButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.getParent().requestDisallowInterceptTouchEvent(true);// 通知父控件勿拦截本控件touch事件
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// LogUtil.d("TAG", "ACTION_DOWN.............");
					addButton
							.setBackgroundResource(R.drawable.bg_fm_ctrl_clock);
					reduceButton.setEnabled(false);
					isLongClick = true;
					addThread = new AddLongCLICKThread();
					addThread.start();
					break;
				case MotionEvent.ACTION_MOVE:
					// LogUtil.d("TAG", "ACTION_MOVE.............");
					if (addThread != null) {
						isLongClick = false;
					}
					break;
				case MotionEvent.ACTION_CANCEL:
					// LogUtil.d("TAG", "ACTION_CANCEL.............");
					break;
				case MotionEvent.ACTION_UP:
					// LogUtil.d("TAG", "ACTION_UP.............");
					if (addThread != null) {
						isLongClick = false;
					}
					time = 0;
					addButton
							.setBackgroundResource(R.drawable.bg_fm_ctrl_unclock);
					reduceButton.setEnabled(true);
					alterFM();
					break;
				default:
					break;
				}
				return false;
			}
		});
		reduceButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.getParent().requestDisallowInterceptTouchEvent(true);// 通知父控件勿拦截本控件touch事件
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// LogUtil.d("TAG", "ACTION_DOWN.............");
					reduceButton
							.setBackgroundResource(R.drawable.bg_fm_ctrl_clock);
					addButton.setEnabled(false);
					isLongClick = true;
					reduceThread = new ReduceLongCLICKThread();
					reduceThread.start();
					break;
				case MotionEvent.ACTION_MOVE:
					// LogUtil.d("TAG", "ACTION_MOVE.............");
					if (reduceThread != null) {
						isLongClick = false;
					}
					break;
				case MotionEvent.ACTION_CANCEL:
					// LogUtil.d("TAG", "ACTION_CANCEL.............");
					break;
				case MotionEvent.ACTION_UP:
					// LogUtil.d("TAG", "ACTION_UP.............");
					if (reduceThread != null) {
						isLongClick = false;
					}
					reduceButton
							.setBackgroundResource(R.drawable.bg_fm_ctrl_unclock);
					time = 0;
					addButton.setEnabled(true);
					alterFM();
					break;
				default:
					break;
				}
				return false;
			}
		});
	}

	/**
	 * 函数名称 : valueFM 功能描述 : FM值计算 参数及返回值说明：
	 * 
	 * @param b
	 *            true表示加，false表示减
	 * 
	 *            修改记录： 日期：2014-8-26 下午1:31:49 修改人：wanghh 描述 ：
	 * 
	 */

	public void valueFM(boolean b) {
		if (b) {
			frequency += 0.1;
		} else {
			frequency -= 0.1;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_layout_music_paly:
			playMusic();
			break;
		case R.id.txt_fm_music:
			playMusic();
			break;
		case R.id.btn_fm_back:
			alterFM();
			finish();
			break;
		default:
			break;
		}
	}

	class AddLongCLICKThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (isLongClick) {
				if (frequency < 107.9) {
					if (time == 0) {
						if (frequency < 107.9) {
							valueFM(true);
							Message message = new Message();
							message.obj = df.format(frequency);
							handler.sendMessage(message);
						}
					}
					if (time > longTime) {
						valueFM(true);
						Message message = new Message();
						message.obj = df.format(frequency);
						handler.sendMessage(message);
					}
					time++;
					try {
						sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					isLongClick = false;
				}
			}
		}
	}

	class ReduceLongCLICKThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (isLongClick) {
				if (frequency > 76.1) {
					if (time == 0) {
						if (frequency > 76.1) {
							valueFM(false);
							Message message = new Message();
							message.obj = df.format(frequency);
							handler.sendMessage(message);
						}
					}
					if (time > longTime) {
						valueFM(false);
						Message message = new Message();
						message.obj = df.format(frequency);
						handler.sendMessage(message);
					}
					time++;
					try {
						sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					isLongClick = false;
				}
			}
		}
	}

	/**
	 * 修改频率同时保存
	 */
	public void alterFM() {
		// TsdHelper.setFMFreq((int) (frequency * 100));
		if (pref != null) {
			// 保存fm值
			editor.putString("fm_freq", frequency + "");
			editor.commit();
		}
		Intent i = new Intent(BaseActivity.NACTION);
		i.putExtra("type", "alter");
		i.putExtra("frequency", frequency);
		sendBroadcast(i);
		LogUtil.d("FMActivity", "send:"+BaseActivity.NACTION);
	}

	/**
	 * 函数名称 : playMusic 功能描述 : 试播放一段音乐 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-10-9 下午5:35:15 修改人：wanghh 描述 ：
	 * 
	 */
	public void playMusic() {
		if (isPlay) {
			isPlay = false;
			musicTextView.setText(getResources().getString(
					R.string.txt_fm_play_music));
			clearPlay();
		} else {
			isPlay = true;
			alterFM();
			musicTextView.setText(getResources().getString(
					R.string.txt_fm_stop_music));
			playImageView.setImageResource(R.drawable.list_play);
			animationDrawable = (AnimationDrawable) playImageView.getDrawable();
			animationDrawable.start();
			AssetFileDescriptor fileDescriptor;
			try {
				fileDescriptor = FMActivity.this.getAssets().openFd("124.mp3");
				if (mediaPlayer != null) {
					mediaPlayer.release();
					mediaPlayer = null;
				}
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
						fileDescriptor.getStartOffset(),
						fileDescriptor.getLength());
				mediaPlayer.setOnCompletionListener(mCompleteListener);
				mediaPlayer.prepare();
				mediaPlayer.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private MediaPlayer.OnCompletionListener mCompleteListener = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			animationDrawable.stop();
			musicTextView.setText(getResources().getString(
					R.string.txt_fm_play_music));
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		clearPlay();
	}

	@Override
	protected void onPause() {
		clearPlay();
		musicTextView.setText(getResources().getString(
				R.string.txt_fm_play_music));
		super.onPause();
	}

	public void clearPlay() {
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		if (animationDrawable != null) {
			animationDrawable.stop();
		}
	}
}
