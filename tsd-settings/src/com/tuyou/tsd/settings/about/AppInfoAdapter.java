package com.tuyou.tsd.settings.about;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tuyou.tsd.common.network.AppVersionInfo;
import com.tuyou.tsd.settings.R;

public class AppInfoAdapter extends BaseAdapter {

	private List<AppVersionInfo> lines;
	private LayoutInflater mInflater;

	public AppInfoAdapter(Context context, List<AppVersionInfo> lines) {
		this.lines = lines;
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
			convertView = mInflater.inflate(R.layout.layout_appinfo_item, null);
			holder = new ViewHolder();
			holder.versionTXT = (TextView) convertView
					.findViewById(R.id.txt_about_version);
			holder.timeTXT = (TextView) convertView
					.findViewById(R.id.txt_about_update_time);
			holder.contentTXT = (TextView) convertView
					.findViewById(R.id.txt_about_upadte_content);
			convertView.setTag(holder);
		} else {

			holder = (ViewHolder) convertView.getTag();

		}
		holder.versionTXT.setText(lines.get(position).version);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date dt = sdf.parse(lines.get(position).timestamp);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dt);
			String time = "于" + (cal.get(Calendar.MONTH) + 1) + "月"
					+ cal.get(Calendar.DAY_OF_MONTH) + "日" + "更新";
			holder.timeTXT.setText(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String content = "";
		for (String string : lines.get(position).notes) {
			content += string + "\n";
		}
		holder.contentTXT.setText(content);
		return convertView;

	}

	public class ViewHolder {

		public TextView versionTXT, timeTXT, contentTXT;

	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	public boolean isEnabled(int position) {
		return false;// 当前行是否可以点击
	}

}
