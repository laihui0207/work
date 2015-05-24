package com.tuyou.tsd.podcast.adapter;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tuyou.tsd.common.network.AudioSubscription;
import com.tuyou.tsd.podcast.R;
import com.tuyou.tsd.podcast.comm.Contents;
import com.tuyou.tsd.podcast.utils.UtilsTools;

@SuppressLint("NewApi")
public class SubscribeAdapter extends BaseAdapter{
	private List<AudioSubscription> list;
	private Context context;
	private ViewHolder holder;
	private ListView musicPlayList;
	
	private Set<BitmapWorkerTask> taskCollection;
	private LruCache<String, Bitmap> mMemoryCache;
	
	private boolean isClick = true;
	
	private boolean isCanClick = true;
	
	public SubscribeAdapter(Context context,List<AudioSubscription> list,ListView musicPlayList){
		this.context = context;
		this.list = list;
		this.musicPlayList = musicPlayList;
		
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
	
	public void setClick(boolean isClick){
		this.isClick = isClick;
	}
	
	public void setCanClick(boolean isCanClick){
		this.isCanClick = isCanClick;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
//		System.out.println("ssssssssssssssss"+list.size());
		if(convertView==null){
			convertView = LayoutInflater.from(context).inflate(R.layout.subscribe_item, null);
			holder = new ViewHolder();
			holder.imageMusicIcon = (ImageView) convertView.findViewById(R.id.icon);
			holder.musicTitle = (TextView) convertView.findViewById(R.id.name);
			holder.isSub = (TextView) convertView.findViewById(R.id.is_sub);
			holder.size = (TextView) convertView.findViewById(R.id.size);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
			holder.isSub.setBackgroundResource(R.drawable.no_sub_select);
			holder.isSub.setText(R.string.no_sub);
		}
		try {
			holder.size.setVisibility(View.GONE);
			holder.imageMusicIcon.setTag(list.get(position).coverUrl);
			loadBitmaps(position);
			setImageView(list.get(position).coverUrl, holder.imageMusicIcon);
			holder.musicTitle.setText(list.get(position).name);
			if(list.get(position).status==1){
				holder.isSub.setBackgroundResource(R.drawable.no_sub_select);
				holder.isSub.setText(R.string.no_sub);
				holder.isSub.setTextColor(context.getResources().getColor(R.color.white));
			}else{
				if(isClick){
					holder.isSub.setBackgroundResource(R.drawable.had_sub_select);
					holder.isSub.setText(R.string.sub);
					holder.isSub.setTextColor(context.getResources().getColor(R.color.white));
				}else{
					holder.isSub.setBackgroundResource(R.drawable.sub_cannt);
					holder.isSub.setText(R.string.sub);
					holder.isSub.setTextColor(context.getResources().getColor(R.color.gray));
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		holder.isSub.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isCanClick){
					isCanClick = false;
					try {
						if(list.get(position).status==1){
							Intent it = new Intent();
							it.setAction(Contents.REMOVE_SUB);
							it.putExtra("index", position);
							context.sendBroadcast(it);
						}else{
							if(isClick){
								Intent it = new Intent();
								it.setAction(Contents.ADD_SUB);
								it.putExtra("index", position);
								context.sendBroadcast(it);
							}else{
								Intent it = new Intent();
								it.setAction(Contents.CANNOT_ADD);
								context.sendBroadcast(it);
							}
						}
					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		return convertView;
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
		TextView isSub;
		TextView size;
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
			String imageUrl = list.get(index).coverUrl;
			bitmap = getBitmapFromMemoryCache(imageUrl);
			if (bitmap == null) {
				BitmapWorkerTask task = new BitmapWorkerTask(list.get(index).coverUrl);
				taskCollection.add(task);
				task.execute(imageUrl);
			} else {
				ImageView imageView = (ImageView) musicPlayList.findViewWithTag(list.get(index).coverUrl);
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
				task = null;
			}
		}
	}
}
