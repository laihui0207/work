package com.tuyou.tsd.cardvr.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.utils.AsyncBitmapLoader;
import com.tuyou.tsd.cardvr.utils.Utility;
import com.tuyou.tsd.cardvr.utils.AsyncBitmapLoader.ImageCallBack;

/**
 * 视频列表适配器
 * 
 * @author ZL
 * @date 2014年7月12日 下午12:56:43
 */
public class VideoAdapter extends BaseAdapter {
	private Context context;
	private List<File> list;
	private AsyncBitmapLoader asyncBitmapLoader;
	
	public VideoAdapter(Context context, List<File> list) {
		this.context = context;
		this.list = list;
		this.asyncBitmapLoader=new AsyncBitmapLoader();  
	}

	@Override
	public int getCount() {
		return (null != list) ? list.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.activity_video_item, parent, false);
			// 图片
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.view_item_image);
			// 名称
			holder.textView = (TextView) convertView
					.findViewById(R.id.view_item_text);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//holder.imageView.setImageBitmap(list.get(position).getThumbnail());
		
		Bitmap bitmap=asyncBitmapLoader.loadBitmap(context, holder.imageView,list.get(position), new ImageCallBack() { 
            
            @Override  
            public void imageLoad(ImageView imageView, Bitmap bitmap) {  
                imageView.setImageBitmap(bitmap);  
            }  
        },Utility.checkSdcard());  
		if (bitmap == null) {
			holder.imageView.setImageResource(R.drawable.ic_launcher);
		} else {
			holder.imageView.setImageBitmap(bitmap);
		}
         
		holder.textView.setText(list.get(position).getName());
		return convertView;
	}

	public class ViewHolder {
		private ImageView imageView;
		private TextView textView;
	}
	
}
