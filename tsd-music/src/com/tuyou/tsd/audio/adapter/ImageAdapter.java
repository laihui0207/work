package com.tuyou.tsd.audio.adapter;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.tuyou.tsd.audio.R;
import com.tuyou.tsd.audio.comm.Contents;
import com.tuyou.tsd.audio.utils.UtilsTools;
import com.tuyou.tsd.audio.weight.CoverFlow;
import com.tuyou.tsd.common.network.AudioCategory;

@SuppressLint("NewApi")
public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	private List<AudioCategory> list;
	private ViewHolder holder;

	private Set<BitmapWorkerTask> taskCollection;
	private LruCache<String, Bitmap> mMemoryCache;
	private CoverFlow flow;

	public ImageAdapter(Context c, List<AudioCategory> list, CoverFlow flow) {
		this.mContext = c;
		this.list = list;
		this.flow = flow;

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

	public int getCount() {
		//		return Integer.MAX_VALUE;
		if(list!=null){
			return list.size();
		}
		return 0;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}
	

	@SuppressWarnings("deprecation")
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.gallery_item, null);
			holder = new ViewHolder();
			convertView.setLayoutParams(new CoverFlow.LayoutParams(140, 140));
			holder.musicIcon = (ImageView) convertView;
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		convertView.setVisibility(View.GONE);
		
		try {
			convertView.setVisibility(View.VISIBLE);
			holder.musicIcon.setTag(list.get(position).category);
			if(list.get(position).order==-3){
				holder.musicIcon.setImageResource(R.drawable.love_default);
			}else if(list.get(position).order==-4){
				holder.musicIcon.setImageResource(R.drawable.play_default);
			}else{
				loadBitmaps(position);
				setImageView(list.get(position).image, holder.musicIcon);
			}
		
//			if(list.get(position).item!=null&&list.get(position).item.size()>0){
//				convertView.setVisibility(View.VISIBLE);
//				
//				}
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	class ViewHolder {
		ImageView musicIcon;
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private String category;
		public BitmapWorkerTask(String category){
			this.category = category;
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
			ImageView imageView = (ImageView) flow.findViewWithTag(category);
			if (imageView != null && bitmap != null) {
				imageView.setImageBitmap(bitmap);
			}
			taskCollection.remove(this);
		}

		private Bitmap downloadBitmap(String imageUrl) {
			Bitmap bitmap = null;
			HttpURLConnection con = null;
			String path = Contents.IMAGE_PATH + "/"
					+ imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
			File file = new File(path);
			if (file.exists()&&file.length()>0) {
				bitmap = UtilsTools.showBitmap(path);
			} else {
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
			String imageUrl = list.get(index).image;
			bitmap = getBitmapFromMemoryCache(imageUrl);
			if (bitmap == null) {
				BitmapWorkerTask task = new BitmapWorkerTask(list.get(index).category);
				taskCollection.add(task);
				task.execute(imageUrl);
			} else {
				ImageView imageView = (ImageView) flow
						.findViewWithTag(list.get(index).category);
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
