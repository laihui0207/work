package com.tuyou.tsd.cardvr.activitys;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.TextView;

import com.tuyou.tsd.cardvr.BaseActivity;
import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.adapter.PhotoDetailedAdapter;
import com.tuyou.tsd.cardvr.customView.DetialGallery;
import com.tuyou.tsd.cardvr.service.videoMeta.PictureInfoDAO;
import com.tuyou.tsd.cardvr.utils.Tools;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.videoMeta.PictureInf;

public class PhotoDetailedActivity extends BaseActivity implements OnClickListener,OnItemSelectedListener{
	
	private String PICTURES_PATH = TSDConst.CAR_DVR_PICTURES_PATH + "/";
	private List<File> fileList;
	private DetialGallery photo;
	private PhotoDetailedAdapter adapter;
	private int index = 0;
	private TextView title;
	private TextView photoAddress;
	private TextView photoTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_detailed);
		
		initView();
	}

	private void initView() {
		title = (TextView) findViewById(R.id.music_play_title);
		photo = (DetialGallery) findViewById(R.id.photo);
		TextView back = (TextView) findViewById(R.id.back);
		back.setOnClickListener(this);
		photoAddress = (TextView) findViewById(R.id.photo_address);
		photoTime = (TextView) findViewById(R.id.photo_time);
		try {
			FileInputStream fileInputStream = new FileInputStream(PICTURES_PATH+"/PhotoInfo.txt");           
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);           
			fileList =(List<File>) objectInputStream.readObject(); 
	  		index = getIntent().getExtras().getInt("photo_index");
	  		showData();
		} catch (Exception e) {
			e.toString();
		}
		adapter = new PhotoDetailedAdapter(this, fileList);
		photo.setAdapter(adapter);
		photo.setSelection(index);
		photo.setOnItemSelectedListener(this);
	}

	private void showData() {
		title.setText(R.string.app_name2);
		photoAddress.setText("");
		photoTime.setText("");
		try {
			PictureInf info = PictureInfoDAO.getInstance(this).getVideo(fileList.get(index).getName().replace(".jpg", ""));
	  		title.setText(Tools.getTime(info.timestamp, "yyyyMMdd_HH:mm:ss"));
	  		photoAddress.setText(info.address);
	  		String path = fileList.get(index).getName();
	  		photoTime.setText(Tools.getWeekStr(path.replace(".jpg", "").substring(0, 8))+path.replace(".jpg", "").substring(4, 6)+"月"+path.replace(".jpg", "").substring(6,8)+"日");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
		index = arg2;
		showData();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}

}
