package com.tuyou.tsd.cardvr.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.activitys.PhotoDetailedActivity;
import com.tuyou.tsd.cardvr.customView.NoScrollGridView;
import com.tuyou.tsd.cardvr.utils.Tools;
import com.tuyou.tsd.common.TSDConst;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class PhotoGridViewAdapter extends BaseAdapter
//implements OnScrollListener
{
	private Context mContext;
	private ArrayList<String> mList;
	private Set<BitmapWorkerTask> taskCollection; 
	private LruCache<String, Bitmap> mMemoryCache;
	private NoScrollGridView viewGridview;
	private Map<String,ArrayList<String>> photoMap;
	private int index;
	private boolean ISCAN = true;
	private int  gotoIndex = 0;
	private List<String> listString;

	public PhotoGridViewAdapter(Context mContext,ArrayList<String> mList,NoScrollGridView viewGridview,Map<String,ArrayList<String>> photoMap,int index,List<String> listString) {
		super();
		this.mContext = mContext;
		this.mList = mList;
		this.viewGridview = viewGridview;
		this.photoMap = photoMap;
		this.index = index;
		taskCollection = new HashSet<BitmapWorkerTask>();  
		int memClass = ((ActivityManager)(mContext.getSystemService(Context.ACTIVITY_SERVICE))).getMemoryClass();		
		int cacheSize = memClass / 50; 
		this.listString = listString;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {  
			@SuppressLint("NewApi")
			@Override  
			protected int sizeOf(String key, Bitmap bitmap) {  
				return bitmap.getByteCount();  
				}  
		};  
	}

	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		} else {
			return this.mList.size();
		}
	}

	@Override
	public Object getItem(int position) {
		if (mList == null) {
			return null;
		} else {
			return this.mList.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.photo_gridview_item, null);
			holder = new ViewHolder();
			holder.photo = (ImageView) convertView.findViewById(R.id.photo_imageview);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.photo.setTag(TSDConst.CAR_DVR_PICTURES_PATH + "/"+ mList.get(position));  
		setImageView(TSDConst.CAR_DVR_PICTURES_PATH + "/" +mList.get(position), holder.photo,position); 
		
		holder.photo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(ISCAN){
					ISCAN = false;
					Intent it = new Intent();
					it.setClass(mContext, PhotoDetailedActivity.class);
					int index_pos = 0;
					gotoIndex = 0;
					if(index>0){
						for(int i=0;i<index;i++){
							index_pos = photoMap.get(listString.get(i)).size();
							gotoIndex = gotoIndex+index_pos;
						}
					}
					it.putExtra("photo_index", (gotoIndex+position));
					mContext.startActivity(it);
					ISCAN = true;
				}
			}
		});
		
		return convertView;
	}

	private class ViewHolder {
		ImageView photo;
	}
	
	
	private void setImageView(String imageUrl, ImageView imageView,int position) {  
		Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);  
		if (bitmap != null) {  
			imageView.setImageBitmap(bitmap);
			} else {  
				imageView.setBackgroundResource(R.drawable.sample);
				loadBitmaps(imageUrl, position, imageView);
			}  
		}  
	
	public Bitmap getBitmapFromMemoryCache(String key,int position) { 
		return mMemoryCache.get(key);  
		} 
	
	public Bitmap getBitmapFromMemoryCache(String key) {  
		return mMemoryCache.get(key);  
		} 
	
	private void loadBitmaps(String key,int position,ImageView img) {  
        try {  
        	Bitmap bitmap = getBitmapFromMemoryCache(key);  
        	 if (bitmap == null) {
        		 BitmapWorkerTask task = new BitmapWorkerTask();  
                 taskCollection.add(task);  
                 task.execute(key); 
        	 }else{
        		 ImageView imageView = img;
        		 if (imageView != null && bitmap != null) {  
        			 imageView.setImageBitmap(bitmap);
        		 }
        	 }
            
         } catch (Exception e) {  
             e.printStackTrace();  
         }  
}  
	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		 private String imageUrl;  
		@Override
		protected Bitmap doInBackground(String... params) {
			imageUrl = params[0];  
			Bitmap bitmap = Tools.showBitmap2(imageUrl);
			 if (bitmap != null) {  
				 addBitmapToMemoryCache(params[0], bitmap);  
				 }  

			return bitmap;
		}  
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			ImageView imageView = (ImageView) viewGridview.findViewWithTag(imageUrl); 
			if (imageView != null && result != null) { 
				imageView.setImageBitmap(result);  
			}
			taskCollection.remove(this);  
		}
	 }
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {  
		if (getBitmapFromMemoryCache(key) == null) {  
			mMemoryCache.put(key, bitmap);  
		         }  
		     }

//	@Override
//	public void onScroll(AbsListView view, int firstVisibleItem,
//			int visibleItemCount, int totalItemCount) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onScrollStateChanged(AbsListView view, int scrollState) {
//		// TODO Auto-generated method stub
//		
//	}
}
