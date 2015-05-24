package com.tuyou.tsd.cardvr.adapter;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.customView.NoScrollGridView;
import com.tuyou.tsd.cardvr.utils.Tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PhotoAdapter extends BaseAdapter {
	private Map<String,ArrayList<String>> photoMap;
	private Context mContext;
	private List<String> listString;
	
	public PhotoAdapter(Map<String,ArrayList<String>> photoMap,Context mContext) {
		super();
		this.photoMap = photoMap;
		this.mContext = mContext;
		Set<String> set = this.photoMap.keySet();
		listString = new ArrayList<String>();
		for(Iterator<String> iter = set.iterator(); iter.hasNext();){
			listString.add(iter.next());
		}
	}

	@Override
	public int getCount() {
		if (photoMap == null) {
			return 0;
		} else {
			return photoMap.size();
		}
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView==null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.photo_listview_item, null);
			holder = new ViewHolder();
			holder.photoTime1 = (TextView) convertView.findViewById(R.id.photo_time1);
			holder.photoTime2 = (TextView) convertView.findViewById(R.id.photo_time2);
			holder.photoTime3 = (TextView) convertView.findViewById(R.id.photo_time3);
			holder.gridview = (NoScrollGridView) convertView.findViewById(R.id.listview_item_gridview);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		PhotoGridViewAdapter adapter = new PhotoGridViewAdapter(mContext, photoMap.get(listString.get(position)),holder.gridview,photoMap,position,listString);
		holder.gridview.setAdapter(adapter);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			if(listString.get(position).equals(sdf.format(new Date(System.currentTimeMillis())))){
				holder.photoTime1.setText(listString.get(position).substring(4,6)+"月"+listString.get(position).substring(6)+"日");
				holder.photoTime2.setText("今天");
				holder.photoTime3.setText(Tools.getWeekStr(listString.get(position))+"\t"+"共"+photoMap.get(listString.get(position)).size()+"张");
			}else{
				holder.photoTime1.setText(listString.get(position).substring(4,6)+"月");
				holder.photoTime2.setText(listString.get(position).substring(6)+"日");
				holder.photoTime3.setText(Tools.getWeekStr(listString.get(position))+"\t"+"共"+photoMap.get(listString.get(position)).size()+"张");
			}
		} catch (Exception e) {
			e.toString();
		}
		return convertView;
	}

	private class ViewHolder {
		TextView photoTime1;
		TextView photoTime2;
		TextView photoTime3;
		
		NoScrollGridView gridview;
	}
}