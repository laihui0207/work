package com.tuyou.tsd.audio.service;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.tuyou.tsd.audio.db.FavouriteRecord;
import com.tuyou.tsd.audio.db.FavouriteRecordDAO;
import com.tuyou.tsd.audio.db.FavouriteRecordEntity;
import com.tuyou.tsd.common.network.AudioFavouriteAddReq;
import com.tuyou.tsd.common.network.AudioFavouriteDeleteReq;
import com.tuyou.tsd.common.network.AudioItem;
import com.tuyou.tsd.common.network.AudioRes;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.util.MyAsyncTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class FavouriteManager {
	Context mContext = null;
	private AddFavouriteTask addFavouriteTask = null;
	private DeleteFavouriteTask deleteFavouriteTask = null;
	long currentId = -1;
	int currentIndex = 0;
	List<FavouriteRecordEntity> liblist = null;
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				addFavouriteTask = null;
				FavouriteRecordDAO.getInstance(mContext).delete(currentId);
				liblist.remove(currentIndex);
				if (liblist.size() > 0) {
					disposeRecord();
				} else {
					liblist = getLocalFavourite();
					if (liblist == null)
					{						
					}
					else
					{
						disposeRecord();
					}					
				}
				break;

			case 2:
				deleteFavouriteTask = null;
				FavouriteRecordDAO.getInstance(mContext).delete(currentId);
				liblist.remove(currentIndex);
				if (liblist.size() > 0) {
					disposeRecord();
				} else {
					liblist = getLocalFavourite();
					if (liblist == null)
					{						
					}
					else
					{
						disposeRecord();
					}					
				}
				break;
			case 100:
				addFavouriteTask = null;
				break;
			case 101:
				deleteFavouriteTask = null;
				break;
			}
		}
	};

	public FavouriteManager(Context context) {
		mContext = context;
	}

	public void release() {
		if (addFavouriteTask != null) {
			addFavouriteTask.cancel(true);
		}
		if (deleteFavouriteTask != null) {
			deleteFavouriteTask.cancel(true);
		}
	}

	private void disposeRecord() {
		currentIndex = 0;
		FavouriteRecordEntity rec = liblist.get(currentIndex);
		currentId = rec.getId();
		if (rec.getDetail().type == 1) {
			addFavouriteTask = new AddFavouriteTask();
			AudioFavouriteAddReq req = new AudioFavouriteAddReq();
			String[] items = new String[1];
			items[0] = rec.getDetail().item.item;
			req.items = items;
			addFavouriteTask.execute(req);
		} else if (rec.getDetail().type == 2) {
			deleteFavouriteTask = new DeleteFavouriteTask();
			AudioFavouriteDeleteReq req = new AudioFavouriteDeleteReq();
			String[] items = new String[1];
			items[0] = rec.getDetail().item.item;
			req.items = items;
			deleteFavouriteTask.execute(req);
		} else {
			FavouriteRecordDAO.getInstance(mContext).delete(currentId);
		}
	}

	public void disposeSyn() {
		if ((deleteFavouriteTask == null) && (addFavouriteTask == null)) {
			liblist = getLocalFavourite();
			if (liblist == null)
				return;
			disposeRecord();
		}
	}

//	private void synFavouriteItem(AudioItem item, int type) {
//		List<FavouriteItemEntity> reads = FavouriteItemDAO
//				.getInstance(mContext).readAll();
//		if (reads == null) {
//			if (type == 1) {
//				FavouriteItemEntity cc = new FavouriteItemEntity();
//				cc.setDetail(item);
//				FavouriteItemDAO.getInstance(mContext).save(cc);
//				return;
//			}
//		} else {
//			int size = reads.size();
//			for (int i = 0; i < size; i++) {
//				AudioItem rec = reads.get(i).getDetail();
//				if (rec.item.equals(item.item)) {
//					FavouriteItemDAO.getInstance(mContext).delete(
//							reads.get(i).getId());
//				}
//			}
//			if (type == 1) {
//				FavouriteItemEntity cc = new FavouriteItemEntity();
//				cc.setDetail(item);
//				FavouriteItemDAO.getInstance(mContext).save(cc);
//				return;
//			}
//		}
//	}

	private List<FavouriteRecordEntity> getLocalFavourite() {
		List<FavouriteRecordEntity> result = null;
		List<FavouriteRecordEntity> list = FavouriteRecordDAO.getInstance(
				mContext).readAll();
		if (list != null) {
			result = new ArrayList<FavouriteRecordEntity>();
			for (int i = 0; i < list.size(); i++) {
				result.add(list.get(i));
			}
		}
		return result;
	}

	public List<AudioItem> AddLocalFavourite(List<AudioItem> items) {
		List<AudioItem> result = null;
		List<FavouriteRecordEntity> entityList = getLocalFavourite();
		if (entityList == null)
			return items;
		else {
			result = new ArrayList<AudioItem>();
			if (items != null) {
				for (int k = 0; k < items.size(); k++) {
					result.add(0,items.get(k));
				}
			}
			for (int i = 0; i < entityList.size(); i++) {
				FavouriteRecord record = entityList.get(i).getDetail();
				if (record.type == 1) {
					addItem(result, record.item);
					//synFavouriteItem(record.item,1);
				} else if (record.type == 2) {
					deleteItem(result, record.item);
					//synFavouriteItem(record.item,2);
				}
			}
		}
		return result;
	}

	private void addItem(List<AudioItem> items, AudioItem item) {
		try {
			int index = 0;
			if ((items != null) && (item != null)) {
				for (int i = 0; i < items.size(); i++) {
					if (items.get(i).item.equals(item.item)) {
						break;
					} else {
						index++;
					}
				}

				if (index >= items.size()) {
					items.add(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void deleteItem(List<AudioItem> items, AudioItem item) {
		if ((items != null) && (item != null)) {
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).item.equals(item.item)) {
					items.remove(i);
					break;
				}
			}
		}
	}

	public class AddFavouriteTask extends
			MyAsyncTask<AudioFavouriteAddReq, Void, AudioRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final AudioRes result) {
			if (result != null) {
				Gson gson = new Gson();
				String resultJson = gson.toJson(result);
				System.out.println("resultJson = " + resultJson);
				if ((result.errorCode == 0) && (result.status.code == 0)) {
					Message msg = mHandler.obtainMessage(1, result);
					mHandler.sendMessage(msg);
				} else {
					mHandler.sendEmptyMessage(100);
				}
			} else {
				mHandler.sendEmptyMessage(100);
			}
		}

		@Override
		protected AudioRes doInBackground(AudioFavouriteAddReq... arg0) {
			return JsonOA2.getInstance(mContext).submitAudioFavouriteAdd(
					arg0[0]);
		}
	}

	public class DeleteFavouriteTask extends
			MyAsyncTask<AudioFavouriteDeleteReq, Void, AudioRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final AudioRes result) {
			if (result != null) {
				Gson gson = new Gson();
				String resultJson = gson.toJson(result);
				System.out.println("resultJson = " + resultJson);
				if ((result.errorCode == 0) && (result.status.code == 0)) {
					Message msg = mHandler.obtainMessage(2, result);
					mHandler.sendMessage(msg);
				} else {
					mHandler.sendEmptyMessage(101);
				}
			} else {
				mHandler.sendEmptyMessage(101);
			}
		}

		@Override
		protected AudioRes doInBackground(AudioFavouriteDeleteReq... arg0) {
			return JsonOA2.getInstance(mContext).submitAudioFavouriteDelete(
					arg0[0]);
		}
	}

}
