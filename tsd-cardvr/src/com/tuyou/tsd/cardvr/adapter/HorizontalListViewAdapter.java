package com.tuyou.tsd.cardvr.adapter;



import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.bean.VideoInfo;
import com.tuyou.tsd.cardvr.customView.NoScrollGridView;

public class HorizontalListViewAdapter extends BaseAdapter{
	private Context mContext;
	private LayoutInflater mInflater;
	private TreeMap<String,ArrayList<VideoInfo>> list;
	private List<String> listString;
	private int type;
	
	public HorizontalListViewAdapter(Context context, TreeMap<String,ArrayList<VideoInfo>> list,int type){
		this.mContext = context;
		this.list = list;
		this.type = type;
		mInflater = LayoutInflater.from(mContext);
		listString = new ArrayList<String>();
		Set<String> set = this.list.keySet();
		for(Iterator<String> iter = set.iterator(); iter.hasNext();){
			listString.add(iter.next());
		}
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

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if(convertView==null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.listview_item, null);
			convertView.setTag(holder);
			holder.gridView = (NoScrollGridView) convertView.findViewById(R.id.listview_item_gridview);
			holder.listviewItemText = (TextView) convertView.findViewById(R.id.listview_item_text);
			holder.listviewItemTime = (TextView) convertView.findViewById(R.id.listview_item_time);
		}else{
			holder=(ViewHolder)convertView.getTag();
		}
		ArrayList<VideoInfo> arrayListForEveryGridView = list.get(listString.get(position)); 
		GridViewAdapter adapter = new GridViewAdapter(mContext, arrayListForEveryGridView);
		holder.gridView.setAdapter(adapter);
		holder.listviewItemText.setText(listString.get(position).substring(11));
		if(type==2){
			if(list.size()>0){
				if(list.size()==1){
					holder.listviewItemTime.setText(listString.get(position).substring(5, 10).replace("-", "月")+"日");
					holder.listviewItemTime.setVisibility(View.VISIBLE);
				}else{
					if(position>0){
						if(listString.get(position).equals(listString.get(position-1))){
							holder.listviewItemTime.setVisibility(View.INVISIBLE);
						}else{
							holder.listviewItemTime.setText(listString.get(position).substring(5, 10).replace("-", "月")+"日");
							holder.listviewItemTime.setVisibility(View.VISIBLE);
						}
					}else{
						if(!listString.get(position).equals(listString.get(position+1))){
							holder.listviewItemTime.setText(listString.get(position).substring(5, 10).replace("-", "月")+"日");
							holder.listviewItemTime.setVisibility(View.VISIBLE);
						}
					}
				}
			}
		}
		return convertView;
	}
	
	



	private  class ViewHolder {
		NoScrollGridView gridView ;
		TextView listviewItemText;
		TextView listviewItemTime;
	}
}
