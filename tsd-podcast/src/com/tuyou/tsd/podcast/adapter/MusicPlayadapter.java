package com.tuyou.tsd.podcast.adapter;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tuyou.tsd.common.network.AudioItem;
import com.tuyou.tsd.podcast.R;
import com.tuyou.tsd.podcast.comm.Contents;
import com.tuyou.tsd.podcast.db.HeardAllItemDAO;
import com.tuyou.tsd.podcast.service.IAudioPlayerService;
import com.tuyou.tsd.podcast.utils.UtilsTools;

@SuppressLint("NewApi")
public class MusicPlayadapter extends BaseAdapter{
	private ArrayList<AudioItem> list;
	private Context context;
	private ViewHolder holder;
	private ListView musicPlayList;
	
	private Set<BitmapWorkerTask> taskCollection;
	private LruCache<String, Bitmap> mMemoryCache;
	private boolean isHot;
	
	
	public MusicPlayadapter(Context context,ArrayList<AudioItem> list,ListView musicPlayList,boolean isHot){
		this.context = context;
		this.list = list;
		this.musicPlayList = musicPlayList;
		this.isHot = isHot;
		
		int maxMemory = (int) Runtime.getRuntime().maxMemory();  
		int cacheSize = maxMemory / 8;  
		taskCollection = new HashSet<BitmapWorkerTask>();  
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {  
			@Override  
			protected int sizeOf(String key, Bitmap bitmap) {  
				return bitmap.getByteCount();  
				}  
			};  
	}

	@Override
	public int getCount() {
		if(list!=null){
			return list.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(convertView==null){
			convertView = LayoutInflater.from(context).inflate(R.layout.music_play_item, null);
			holder = new ViewHolder();
			holder.imageMusicIcon = (ImageView) convertView.findViewById(R.id.image_music_icon);
			holder.musicTitle = (TextView) convertView.findViewById(R.id.music_title);
//			holder.musicIsOnline = (ImageView) convertView.findViewById(R.id.music_is_online);
//			holder.musicSize = (TextView) convertView.findViewById(R.id.music_size);
			holder.musicAuthor = (TextView) convertView.findViewById(R.id.music_author);
			holder.musicIsPlay = (ImageView) convertView.findViewById(R.id.music_is_play);
			holder.musicTime = (TextView) convertView.findViewById(R.id.music_time);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		setText(holder.musicTitle, list.get(position).name);
//		if(!TextUtils.isEmpty(UtilsTools.fileize(list.get(position).size))){
//			setText(holder.musicSize,UtilsTools.fileize(list.get(position).size));
//		}
		if(!TextUtils.isEmpty(list.get(position).author)){
			setText(holder.musicAuthor,list.get(position).author);
		}
		setText(holder.musicTime,UtilsTools.getTime(list.get(position).duration));
//		File file = new File(Contents.MP3_PATH+"/"+list.get(position).item);
//		if(file.exists()){
//			holder.musicIsOnline.setVisibility(View.VISIBLE);
//		}else{
//			holder.musicIsOnline.setVisibility(View.GONE);
//		}
		if(list.get(position).isPlay){
			holder.musicTitle.setTextColor(context.getResources().getColor(R.color.blue));
			holder.musicAuthor.setTextColor(context.getResources().getColor(R.color.blue));
			holder.musicIsPlay.setVisibility(View.VISIBLE);
			holder.musicTime.setVisibility(View.GONE);
			
			AnimationDrawable animationDrawable = (AnimationDrawable) holder.musicIsPlay.getDrawable();  
            animationDrawable.start();  
			
		}else{
			if(HeardAllItemDAO.getInstance(context).isHaveRecord(list.get(position).item)){
				holder.musicTitle.setTextColor(context.getResources().getColor(R.color.gray));
			}else{
				holder.musicTitle.setTextColor(context.getResources().getColor(R.color.white));
			}
			holder.musicAuthor.setTextColor(context.getResources().getColor(R.color.white_50));
			holder.musicIsPlay.setVisibility(View.GONE);
			holder.musicTime.setVisibility(View.VISIBLE);
		}
		if(isHot){
			holder.imageMusicIcon.setTag(list.get(position).item);
			loadBitmaps(position);
			setImageView(list.get(position).icon, holder.imageMusicIcon);
			holder.musicAuthor.setText(list.get(position).album);
		}else{
			holder.imageMusicIcon.setVisibility(View.GONE);
			holder.musicAuthor.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	private void setText(TextView view ,String str){
		if(TextUtils.isEmpty(str)){
			view.setVisibility(View.GONE);
		}else{
			view.setVisibility(View.VISIBLE);
			view.setText(str);
		}
	}
	
	private void setImageView(String imageUrl, ImageView imageView) {         
		Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);          
		if (bitmap != null) {              
			imageView.setImageBitmap(bitmap);          
			} else {              
				imageView.setImageResource(R.drawable.music_category_default);          
			}      
		}  

	public Bitmap getBitmapFromMemoryCache(String key) {
		return mMemoryCache.get(key);
	}
	
	
	class ViewHolder{
		ImageView imageMusicIcon;
		TextView musicTitle;
//		ImageView musicIsOnline;
//		TextView musicSize;
		TextView musicAuthor;
		ImageView musicIsPlay;
		TextView musicTime;
	}
	
	
	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private String item;
		public BitmapWorkerTask(String item){
			this.item = item;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = downloadBitmap(params[0]);
			if (bitmap != null) {
				addBitmapToMemoryCache(params[0], bitmap);
			}
			return bitmap;
		}

		public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
			if (getBitmapFromMemoryCache(key) == null) {
				mMemoryCache.put(key, bitmap);
			}
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			ImageView imageView = (ImageView) musicPlayList.findViewWithTag(item);
			if (imageView != null && bitmap != null) {
				imageView.setImageBitmap(bitmap);
			}
			taskCollection.remove(this);
		}

		private Bitmap downloadBitmap(String imageUrl) {
			Bitmap bitmap = null;
			HttpURLConnection con = null;
			String path = Contents.IMAGE_PATH+"/"+imageUrl.substring(imageUrl.lastIndexOf("/")+1);
			File file = new File(path);
			if(file.exists()&&file.length()>0){
				bitmap = UtilsTools.showBitmap(path);
			}else{
				try {
					bitmap = UtilsTools.downloadImage(imageUrl);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (con != null) {
						con.disconnect();
					}
				}
			}
			
			return bitmap;
		}
	}

	private void loadBitmaps(int index) {
		Bitmap bitmap = null;
		try {
			String imageUrl = list.get(index).icon;
			bitmap = getBitmapFromMemoryCache(imageUrl);
			if (bitmap == null) {
				BitmapWorkerTask task = new BitmapWorkerTask(list.get(index).item);
				taskCollection.add(task);
				task.execute(imageUrl);
			} else {
				ImageView imageView = (ImageView) musicPlayList.findViewWithTag(list.get(index).item);
				if (imageView != null && bitmap != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cancelAllTasks() {
		if (taskCollection != null) {
			for (BitmapWorkerTask task : taskCollection) {
				task.cancel(false);
			}
		}
	}
}
