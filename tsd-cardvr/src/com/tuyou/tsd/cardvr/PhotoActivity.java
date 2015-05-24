package com.tuyou.tsd.cardvr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tuyou.tsd.cardvr.adapter.PhotoAdapter;
import com.tuyou.tsd.common.TSDConst;

public class PhotoActivity extends BaseActivity implements OnClickListener{
	
	private String PICTURES_PATH = TSDConst.CAR_DVR_PICTURES_PATH + "/";
	private Map<String,ArrayList<String>> photoMap = new  LinkedHashMap<String, ArrayList<String>>();

	private int num;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo);
		initView();
		initService();
	}

	private void initService() {
		new GetPhotoTask(PICTURES_PATH).execute();
	}

	private void initView() {
		ImageView back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;

		default:
			break;
		}
	}
	
	
	
	class GetPhotoTask extends AsyncTask<Void, Void, Void>{
		private String path;
		public GetPhotoTask(String path){
			this.path = path;
		}
		@Override
		protected Void doInBackground(Void... params) {
			try {
				File file = new File(path);
				File [] files = file.listFiles();
				List<File> fileList = new ArrayList<File>();
				for (File  f : files) {
					if(f.getName().endsWith(".jpg")){
						 fileList.add(f);
					}
				}
				Collections.sort(fileList, new Comparator<File>() {
				    @Override
				    public int compare(File o1, File o2) {
				        if (o1.isDirectory() && o2.isFile())
				            return -1;
				        if (o1.isFile() && o2.isDirectory())
				            return 1;
				        return o2.getName().compareTo(o1.getName());
				    }
				});
				num = fileList.size();
//				photoMap.clear();
				for(File name : fileList){
					if(photoMap.get(name.getName().substring(0, 8))!=null){
						photoMap.get(name.getName().substring(0, 8)).add(name.getName());
					}else{
						ArrayList<String> list = new ArrayList<String>();
						list.add(name.getName());
						photoMap.put(name.getName().substring(0, 8), list);
					}
				}
				SaveSDCard(fileList);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		

		
		private void SaveSDCard(List<File> list){
			try {
				
				File file=new File(TSDConst.CAR_DVR_PICTURES_PATH, "PhotoInfo.txt"); 
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
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			TextView numT = (TextView)findViewById(R.id.photo_num);
			numT.setText("共"+String.valueOf(num)+"张");
			ListView photoList = (ListView) findViewById(R.id.photo_list); 
			
			PhotoAdapter mListViewAdapter=new PhotoAdapter(photoMap, PhotoActivity.this); 
			photoList.setAdapter(mListViewAdapter); 
		}
		
	}
}
