package com.tuyou.tsd.cardvr.adapter;

import java.io.File;
import java.util.List;

import com.tuyou.tsd.cardvr.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class PhotoDetailedAdapter extends BaseAdapter{
	private ViewHolder holder;
	private Context context;
	private List<File> fileList;
	
	public PhotoDetailedAdapter(Context context,List<File> fileList){
		this.context = context;
		this.fileList = fileList;
		holder = new ViewHolder();
	}

	@Override
	public int getCount() {
		if(fileList!=null&&fileList.size()>0){
			return fileList.size();
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
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.photo_detailed_item, null);
			holder.itemImg = (ImageView) convertView.findViewById(R.id.item_img);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		Bitmap map = BitmapFactory.decodeFile(fileList.get(position).getAbsolutePath());
		holder.itemImg.setImageBitmap(map);
		return convertView;
	}
	
	class ViewHolder{
		ImageView itemImg;
	}

}
