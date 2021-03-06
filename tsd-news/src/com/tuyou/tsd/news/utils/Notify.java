package com.tuyou.tsd.news.utils;

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

import com.tuyou.tsd.common.network.AudioCategory;
import com.tuyou.tsd.common.network.AudioItem;
import com.tuyou.tsd.news.MusicActivity;
import com.tuyou.tsd.news.R;
import com.tuyou.tsd.news.comm.Contents;

public class Notify {
	@SuppressLint("NewApi")
	
	public static void showButtonNotify(AudioCategory audioitem,AudioItem item,Context context,boolean isPlaying,boolean isFavourite,int size){
		try {

			Intent it = new Intent(context,MusicActivity.class);
			RemoteViews bitView = new RemoteViews(context.getPackageName(), R.layout.view_custom_button);
			PendingIntent piReceiver = PendingIntent.getActivity(context, 0, it, 0);
			Bitmap bitmap = null;
			try {
				String path = Contents.IMAGE_PATH + "/"+ audioitem.image.substring(audioitem.image.lastIndexOf("/") + 1);
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
				bitView.setTextViewText(R.id.tv_custom_song_singer, item.name);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				bitView.setTextViewText(R.id.tv_custom_song_name, item.album);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			if(isPlaying){
				bitView.setImageViewResource(R.id.btn_custom_play, R.drawable.music_play_select);
			}else{
				bitView.setImageViewResource(R.id.btn_custom_play, R.drawable.music_pause_select);
			}
			if(isFavourite){
				bitView.setTextViewText(R.id.btn_custom_love, context.getString(R.string.re_love));
				bitView.setTextColor(R.id.btn_custom_love, context.getResources().getColor(R.color.gray));
			}else{
				if(size>=6){
					bitView.setTextColor(R.id.btn_custom_love, context.getResources().getColor(R.color.gray));
				}else{
					bitView.setTextColor(R.id.btn_custom_love, context.getResources().getColor(R.color.blue));
				}
				bitView.setTextViewText(R.id.btn_custom_love, context.getString(R.string.add_love));
			}
			Intent buttonIntent = new Intent(Contents.ACTION_NEWS_BUTTON);
			buttonIntent.putExtra("audioitem", audioitem);
			setPeOnClick(buttonIntent, Contents.BUTTON_PREV_ID, bitView, R.id.btn_custom_prev,context);
			setPeOnClick(buttonIntent, Contents.BUTTON_PLAY_ID, bitView, R.id.btn_custom_play,context);
			setPeOnClick(buttonIntent, Contents.BUTTON_NEXT_ID, bitView, R.id.btn_custom_next,context);
			setPeOnClick(buttonIntent, Contents.BUTTON_CLEAN_ID, bitView, R.id.clean,context);
			if(size<6){
				setPeOnClick(buttonIntent, Contents.BUTTON_LOVE_ID, bitView, R.id.btn_custom_love,context);
			}else{
				bitView.setOnClickPendingIntent(R.id.btn_custom_love, null);
			}
			
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
			buttonIntent.putExtra(Contents.INTENT_NEWS_BUTTONID_TAG, BUTTON_ID);
			PendingIntent intent = PendingIntent.getBroadcast(context, BUTTON_ID, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			bitView.setOnClickPendingIntent(id, intent);
		}

}
