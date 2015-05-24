package com.tuyou.tsd.navigation.mode;

import java.util.ArrayList;
import java.util.List;

import com.tuyou.tsd.common.widget.DestinationAdapter.ViewHolder;
import com.tuyou.tsd.navigation.R;
import com.tuyou.tsd.navigation.R.drawable;
import com.tuyou.tsd.navigation.R.id;
import com.tuyou.tsd.navigation.R.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PeripheryAdapter extends BaseAdapter {
	List<String> list = new ArrayList<String>();
	private LayoutInflater mInflater;

	public PeripheryAdapter(Context context, List<String> list) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(R.layout.item_periphery, null);
		TextView nameTXT = (TextView) convertView
				.findViewById(R.id.txt_periphery_name);
		nameTXT.setText(list.get(position));
		switch (position) {
		case 0:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey1_selecter);
			break;
		case 1:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey2_selecter);
			break;
		case 2:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey3_selecter);
			break;
		case 3:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey4_selecter);
			break;
		case 4:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey5_selecter);
			break;
		case 5:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey6_selecter);
			break;
		case 6:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey7_selecter);
			break;
		case 7:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey1_selecter);
			break;
		case 8:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey2_selecter);
			break;
		case 9:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey3_selecter);
			break;
		case 10:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey4_selecter);
			break;
		case 11:
			nameTXT.setBackgroundResource(R.drawable.btn_periphrey5_selecter);
			break;
		default:
			break;
		}
		return convertView;
	}

}
