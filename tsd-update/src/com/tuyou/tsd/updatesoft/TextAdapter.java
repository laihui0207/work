package com.tuyou.tsd.updatesoft;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TextAdapter extends BaseAdapter{
	
	private List<String> list;
	private Context context;
	private ViewHolder holder;
	
	public TextAdapter(Context context,List<String> list){
		this.context = context;
		this.list = list;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.text, null);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.index = (TextView) convertView.findViewById(R.id.index);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.text.setText(list.get(position));
		holder.index.setText(String.valueOf(position+1)+".");
		return convertView;
	}
	
	class ViewHolder{
		TextView text;
		TextView index;
	}

}
