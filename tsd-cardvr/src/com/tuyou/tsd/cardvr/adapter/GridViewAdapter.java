package com.tuyou.tsd.cardvr.adapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.activitys.CheckOutActivity;
import com.tuyou.tsd.cardvr.bean.VideoInfo;
import com.tuyou.tsd.common.CommonConsts;
import com.tuyou.tsd.common.TSDConst;

public class GridViewAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<VideoInfo> mList;
	private boolean CAN_CLICK = true;

	public GridViewAdapter(Context mContext,ArrayList<VideoInfo> mList) {
		super();
		this.mContext = mContext;
		this.mList = mList;
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
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from (this.mContext).inflate(R.layout.gridview_item, null); 
			holder.itemImg = (ImageView) convertView.findViewById(R.id.gridview_item_img);
			holder.itemText = (TextView) convertView.findViewById(R.id.gridview_item_text);
			holder.point = (TextView) convertView.findViewById(R.id.point);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String time = mList.get(position).getTime();
		holder.itemText.setText(time.substring(time.indexOf("T")+1, time.indexOf("+")));
		holder.itemImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(CAN_CLICK){
					CAN_CLICK = false;
					new SaveSDCardTask(mList, position).execute();
				}
			}
		});
		try {
			for(int i=0;i<mList.get(position).getSpecials().length;i++){
				if(mList.get(position).getSpecials()[i].equals("event")){
					holder.point.setVisibility(View.VISIBLE);
					holder.point.setText(R.string.event);
					break;
				}else if(mList.get(position).getSpecials()[i].equals("favourite")){
					holder.point.setVisibility(View.VISIBLE);
					holder.point.setText(R.string.love);
					break;
				}else{
					holder.point.setVisibility(View.GONE);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return convertView;
	}

	private class ViewHolder {
		ImageView itemImg;
		TextView itemText;
		TextView point;
	}
	
	
	
	class SaveSDCardTask extends AsyncTask<Void, Void, Void>{
		private ArrayList<VideoInfo> list;
		private int position;
		
		public SaveSDCardTask(ArrayList<VideoInfo> list,int position){
			this.list = list;
			this.position = position;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			SaveSDCard( list);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Intent it = new Intent();
			it.setClass(mContext, CheckOutActivity.class);
			it.putExtra("index_check", position);
			mContext.startActivity(it);
			CAN_CLICK = true;
		}
	}
	
	@SuppressWarnings("deprecation")
	private void SaveSDCard(ArrayList<VideoInfo> list){
		try {
			
			File file=new File(TSDConst.CAR_DVR_VIDEO_PATH, "VideoInfo.txt"); 
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fileOutputStream= new FileOutputStream(file.toString());            
			ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);            
			objectOutputStream.writeObject(list);
			fileOutputStream.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
