package com.tuyou.tsd.audio.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.tuyou.tsd.audio.MusicActivity;
import com.tuyou.tsd.audio.R;
import com.tuyou.tsd.audio.comm.Contents;
import com.tuyou.tsd.common.network.AudioItem;

public class Notify {
	@SuppressLint("NewApi")
	
	public static void showButtonNotify(AudioItem audioitem,Context context,boolean isPlaying,boolean isFavourite){
		try {

			Intent it = new Intent(context,MusicActivity.class);
			RemoteViews bitView = new RemoteViews(context.getPackageName(), R.layout.view_custom_button);
			PendingIntent piReceiver = PendingIntent.getActivity(context, 0, it, 0);
			Bitmap bitmap = null;
			try {
				String path = Contents.IMAGE_PATH + "/"+ audioitem.icon.substring(audioitem.icon.lastIndexOf("/") + 1);
				bitmap = UtilsTools.showBitmap(path);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(bitmap!=null){
				bitView.setImageViewBitmap(R.id.custom_song_icon, bitmap);
			}else{
				bitView.setImageViewBitmap(R.id.custom_song_icon, BitmapFactory.decodeResource(context.getResources(), R.drawable.music_default));
			}
			try {
				bitView.setTextViewText(R.id.tv_custom_song_singer, audioitem.name);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				bitView.setTextViewText(R.id.tv_custom_song_name, audioitem.author);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(isPlaying){
				bitView.setImageViewResource(R.id.btn_custom_play, R.drawable.music_play_select);
			}else{
				bitView.setImageViewResource(R.id.btn_custom_play, R.drawable.music_pause_select);
			}
			if(isFavourite){
				bitView.setImageViewResource(R.id.btn_custom_love, R.drawable.music_love);
			}else{
				bitView.setImageViewResource(R.id.btn_custom_love, R.drawable.music_unlove);
			}
			Intent buttonIntent = new Intent(Contents.ACTION_BUTTON);
			buttonIntent.putExtra("audioitem", audioitem);
			setPeOnClick(buttonIntent, Contents.BUTTON_PREV_ID, bitView, R.id.btn_custom_prev,context);
			setPeOnClick(buttonIntent, Contents.BUTTON_PLAY_ID, bitView, R.id.btn_custom_play,context);
			setPeOnClick(buttonIntent, Contents.BUTTON_NEXT_ID, bitView, R.id.btn_custom_next,context);
			setPeOnClick(buttonIntent, Contents.BUTTON_CLEAN_ID, bitView, R.id.clean,context);
			setPeOnClick(buttonIntent, Contents.BUTTON_LOVE_ID, bitView, R.id.btn_custom_love,context);
//			setPeOnClick(buttonIntent, Contents.BUTTON_PARENT_ID, bitView, R.id.custom_parent,context);
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
					.setContentIntent(piReceiver)
					.setContent(bitView)
					.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon)).setOngoing(false)
					.setTicker(audioitem.name).setPriority(NotificationCompat.PRIORITY_MAX)
					.setSmallIcon(R.drawable.not_icon, 0);
			Notification notification = builder.build();
			if (Build.VERSION.SDK_INT >= 16) {
				notification.bigContentView = bitView;
			}
			if (Build.VERSION.SDK_INT <= 10) {
				notification.contentView = bitView;
			}
			notification.flags = Notification.FLAG_NO_CLEAR; 
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); 
			mNotificationManager.notify(200, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 @SuppressLint({ "InlinedApi", "NewApi" })
		private static void setPeOnClick(Intent buttonIntent,int BUTTON_ID,RemoteViews bitView,int id,Context context){
			buttonIntent.putExtra(Contents.INTENT_BUTTONID_TAG, BUTTON_ID);
			PendingIntent intent = PendingIntent.getBroadcast(context, BUTTON_ID, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			bitView.setOnClickPendingIntent(id, intent);
		}

}
