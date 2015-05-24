package com.tuyou.tsd.common.widget;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuyou.tsd.common.R;
import com.tuyou.tsd.common.TSDEvent;

public class DestinationAdapter extends BaseAdapter {

	private ArrayList<HashMap<String, String>> lines;
	private Context context;
	private LayoutInflater mInflater;

	public DestinationAdapter(Context context,
			ArrayList<HashMap<String, String>> lines) {
		this.lines = lines;
		this.context = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount() {

		return lines.size();

	}

	@Override
	public long getItemId(int position) {

		return position;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.item_route, null);

			holder = new ViewHolder();

			holder.nameTXT = (TextView) convertView
					.findViewById(R.id.txt_item_route_name);
			holder.distanceTXT = (TextView) convertView
					.findViewById(R.id.txt_item_route_distance);
			holder.addreeTXT = (TextView) convertView
					.findViewById(R.id.txt_item_route_addree);
			holder.numTXT = (TextView) convertView
					.findViewById(R.id.txt_item_route_num);
			holder.lineInfoLayout = (LinearLayout) convertView
					.findViewById(R.id.layout_line_info);
			convertView.setTag(holder);

		} else {

			holder = (ViewHolder) convertView.getTag();

		}
		linstener(holder.lineInfoLayout, position);
		int item = position + 1;
		if (item < 10) {
			holder.numTXT.setText("0" + item);
		} else {
			holder.numTXT.setText(item + "");
		}
		holder.nameTXT.setText(lines.get(position).get("name"));
		if (lines.get(position).get("distance") != null) {
			holder.distanceTXT.setVisibility(View.VISIBLE);
			holder.distanceTXT.setText(lines.get(position).get("distance"));
		} else {
			holder.distanceTXT.setVisibility(View.GONE);
		}

		holder.addreeTXT.setText(lines.get(position).get("addr"));
		return convertView;

	}

	private void linstener(LinearLayout layout, final int position) {
		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putBoolean("navigation", false);
				bundle.putString("latitude", lines.get(position)
						.get("latitude"));
				bundle.putString("longitude",
						lines.get(position).get("longitude"));
				bundle.putString("mylatitude",
						lines.get(position).get("mylatitude"));
				bundle.putString("mylongitude",
						lines.get(position).get("mylongitude"));
				bundle.putString("name", lines.get(position).get("name"));
				bundle.putString("addr", lines.get(position).get("addr"));
				bundle.putString("myname", lines.get(position).get("myname"));
				bundle.putString("myaddr", lines.get(position).get("myaddr"));

				Intent intent = new Intent(TSDEvent.Navigation.START_NAVIGATION);
				intent.putExtra("bundle", bundle);
				context.sendBroadcast(intent);
			}
		});
	}

	public class ViewHolder {

		public TextView numTXT, nameTXT, distanceTXT, addreeTXT;
		public ImageView navigationImageView;
		public LinearLayout lineInfoLayout;

	}

	@Override
	public Object getItem(int position) {
		return null;
	}

}
