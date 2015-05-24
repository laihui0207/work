package com.tuyou.tsd.cardvr.adapter;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.bean.TroubleInfo;
import com.tuyou.tsd.cardvr.customView.NoScrollGridView;
import com.tuyou.tsd.cardvr.service.IInterfaceService;
import com.tuyou.tsd.cardvr.utils.Tools;

public class TroubleAdapter extends BaseAdapter{
	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<TroubleInfo> list;
	
	private Set<BitmapWorkerTask> taskCollection; 
	private LruCache<String, Bitmap> mMemoryCache;
	private NoScrollGridView viewGridview;
	private int type;
	private IInterfaceService countService;

	public TroubleAdapter(Context context, ArrayList<TroubleInfo> list,NoScrollGridView viewGridview,int type,IInterfaceService countService){
		this.mContext = context;
		this.list = list;
		mInflater = LayoutInflater.from(mContext);
		this.viewGridview = viewGridview;
		this.type = type;
		this.countService= countService;
		
		taskCollection = new HashSet<BitmapWorkerTask>();  
		int maxMemory = (int) Runtime.getRuntime().maxMemory();  
		int cacheSize = maxMemory / 8;  
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
		if(list!=null){
			return list.size();
		}
		return 0;
	}
	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if(convertView==null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.trouble_gridview_item, null);
			holder.mImage=(ImageView)convertView.findViewById(R.id.img_list_item);
			holder.mTitle=(TextView)convertView.findViewById(R.id.text_list_item);
			holder.eventPoint = (TextView) convertView.findViewById(R.id.event_point);
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder)convertView.getTag();
		}
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		holder.mTitle.setText(format.format(new Date(Long.valueOf(list.get(position).getTime()))));
//		convertView.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				
//			}
//		});
		holder.mImage.setTag(countService.getVideoThumbnail(list.get(position).getName()));  
		setImageView(countService.getVideoThumbnail(list.get(position).getName()), holder.mImage,position);  
		if(type==1){
			if(list.size()>2){
				if(position==1){
					holder.eventPoint.setVisibility(View.VISIBLE);
				}else{
					holder.eventPoint.setVisibility(View.GONE);
				}
			}else{
				if(position==0){
					holder.eventPoint.setVisibility(View.VISIBLE);
				}else{
					holder.eventPoint.setVisibility(View.GONE);
				}
			}
		}else{
			holder.eventPoint.setVisibility(View.GONE);
		}

		return convertView;
	}
	
	public void cancelAllTasks() {  
		if (taskCollection != null) {  
			for (BitmapWorkerTask task : taskCollection) {  
				task.cancel(false);  
				}  
			}  
		} 
	
	@SuppressWarnings("deprecation")
	private void setImageView(String imageUrl, ImageView imageView,int position) {  
		Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);  
		if (bitmap != null) {  
			imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
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

	
	
	@SuppressWarnings("deprecation")
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
		        			 imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
		        		 }
		        	 }
		            
		         } catch (Exception e) {  
		             e.printStackTrace();  
		         }  
		}  




	private  class ViewHolder {
		 TextView mTitle ;
		 ImageView mImage;
		 TextView eventPoint;
	}
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {  
		if (getBitmapFromMemoryCache(key) == null) {  
			mMemoryCache.put(key, bitmap);  
		         }  
		     }

	
	
	 class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		 private String imageUrl;  
		@Override
		protected Bitmap doInBackground(String... params) {
			imageUrl = params[0];  
			Bitmap bitmap = Tools.showBitmap(imageUrl);
			 if (bitmap != null) {  
				 addBitmapToMemoryCache(params[0], bitmap);  
				 }  

			return bitmap;
		}  
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			ImageView imageView = (ImageView) viewGridview.findViewWithTag(imageUrl);  
			if (imageView != null && result != null) { 
				imageView.setBackgroundDrawable(new BitmapDrawable(result));  
			}
			taskCollection.remove(this);  
		}
	 }
	 
}
