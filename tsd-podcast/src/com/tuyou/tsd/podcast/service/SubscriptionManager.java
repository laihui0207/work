package com.tuyou.tsd.podcast.service;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.tuyou.tsd.common.network.AudioCategory;
import com.tuyou.tsd.common.network.AudioItem;
import com.tuyou.tsd.common.network.AudioRes;
import com.tuyou.tsd.common.network.AudioSubscription;
import com.tuyou.tsd.common.network.GetAudioCategoryDetailRes;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.util.MyAsyncTask;
import com.tuyou.tsd.podcast.db.SubscriptionCategoryDAO;
import com.tuyou.tsd.podcast.db.SubscriptionCategoryEntity;
import com.tuyou.tsd.podcast.db.SubscriptionRecord;
import com.tuyou.tsd.podcast.db.SubscriptionRecordDAO;
import com.tuyou.tsd.podcast.db.SubscriptionRecordEntity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

public class SubscriptionManager {
	AudioPlayerService mContext = null;
	private AddSubscriptionTask addSubscriptionTask = null;
	private DeleteSubscriptionTask deleteSubscriptionTask = null;
	private GetSubscriptionDetailListTask subscriptionDetailTask=null;
	long currentId = -1;
	int currentIndex = 0;
	List<SubscriptionRecordEntity> liblist = null;
	List<AudioSubscription> subscriptions=null; 
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			switch (msg.what) {
			case 1:
				addSubscriptionTask = null;
				SubscriptionRecordDAO.getInstance(mContext).delete(currentId);
				liblist.remove(currentIndex);
				if (liblist.size() > 0) {
					disposeRecord();
				} else {
					liblist = getLocalSubscription();
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
				deleteSubscriptionTask = null;
				SubscriptionRecordDAO.getInstance(mContext).delete(currentId);
				liblist.remove(currentIndex);
				if (liblist.size() > 0) {
					disposeRecord();
				} else {
					liblist = getLocalSubscription();
					if (liblist == null)
					{						
					}
					else
					{
						disposeRecord();
					}							
				}
				break;
			case 3:
				AudioCategory audiocategory=(AudioCategory)msg.obj;
				if(mContext.isSubscription(audiocategory.category))
				{
					List<SubscriptionCategoryEntity> all=SubscriptionCategoryDAO.getInstance(mContext).readAll();
					if (all != null) {
						for (int i = 0; i < all.size(); i++) {
							if (all.get(i).getDetail().category
									.equals(audiocategory.category)) {
								SubscriptionCategoryDAO.getInstance(mContext)
										.delete(all.get(i).getId());
							}
						}
					}
					SubscriptionCategoryEntity cc = new SubscriptionCategoryEntity();
					cc.setDetail(audiocategory);
					SubscriptionCategoryDAO.getInstance(mContext).save(
							cc);
					mContext.sendBroadcast(new Intent(AudioPlayerService.DATA_REFRESH));
				}						
				break;
			case 100:
				addSubscriptionTask = null;
				break;
			case 101:
				deleteSubscriptionTask = null;
				break;
			}
		}
	};

	public SubscriptionManager(AudioPlayerService context) {
		mContext = context;
	}

	public void release() {
		if (addSubscriptionTask != null) {
			addSubscriptionTask.cancel(true);
		}
		if (deleteSubscriptionTask != null) {
			deleteSubscriptionTask.cancel(true);
		}
		
		if (subscriptionDetailTask != null) {
			subscriptionDetailTask.cancel(true);
		}
	}

	private void disposeRecord() {
		currentIndex = 0;
		SubscriptionRecordEntity rec = liblist.get(currentIndex);
		currentId = rec.getId();
		if (rec.getDetail().type == 1) {
			addSubscriptionTask = new AddSubscriptionTask();
			addSubscriptionTask.execute(rec.getDetail().item.album, "podcast");
			subscriptionDetailTask=new GetSubscriptionDetailListTask();
			subscriptionDetailTask.execute(rec.getDetail().item);
		} else if (rec.getDetail().type == 2) {
			deleteSubscriptionTask = new DeleteSubscriptionTask();
			deleteSubscriptionTask.execute(rec.getDetail().item.album,
					"podcast");
		} else {
			SubscriptionRecordDAO.getInstance(mContext).delete(currentId);
		}
	}

	public void disposeSyn() {
		if ((deleteSubscriptionTask == null) && (addSubscriptionTask == null)) {
			liblist = getLocalSubscription();
			if (liblist == null)
				return;
			disposeRecord();
		}
	}

	private List<SubscriptionRecordEntity> getLocalSubscription() {
		List<SubscriptionRecordEntity> result = null;
		List<SubscriptionRecordEntity> list = SubscriptionRecordDAO
				.getInstance(mContext).readAll();
		if (list != null) {
			result = new ArrayList<SubscriptionRecordEntity>();
			for (int i = 0; i < list.size(); i++) {
				result.add(list.get(i));
			}
		}
		return result;
	}

	public List<AudioSubscription> AddLocalSubscription(
			List<AudioSubscription> items) {
		List<AudioSubscription> result = null;
		List<SubscriptionRecordEntity> entityList = getLocalSubscription();
		if (entityList == null)
			return items;
		else {
			result = new ArrayList<AudioSubscription>();
			if (items != null) {
				for (int k = 0; k < items.size(); k++) {
					result.add(0, items.get(k));
				}
			}
			for (int i = 0; i < entityList.size(); i++) {
				SubscriptionRecord record = entityList.get(i).getDetail();
				if (record.type == 1) {
					addItem(result, record.item);
				} else if (record.type == 2) {
					deleteItem(result, record.item);
				}
			}
		}
		return result;
	}

	private void addItem(List<AudioSubscription> items, AudioSubscription item) {
		if ((items != null) && (item != null)) {
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).equals(item)) {
					items.get(i).status = 1;
					break;
				}
			}
		}
	}

	private void deleteItem(List<AudioSubscription> items,
			AudioSubscription item) {
		if ((items != null) && (item != null)) {
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).equals(item)) {
					items.get(i).status = 0;
					break;
				}
			}
		}
	}

	public class AddSubscriptionTask extends
			MyAsyncTask<String, Void, AudioRes> {

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
		protected AudioRes doInBackground(String... arg0) {
			return JsonOA2.getInstance(mContext).submitAudioSubscriptionAdd(
					arg0[0], arg0[1]);
		}
	}

	public class DeleteSubscriptionTask extends
			MyAsyncTask<String, Void, AudioRes> {

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
		protected AudioRes doInBackground(String... arg0) {
			return JsonOA2.getInstance(mContext).submitAudioSubscriptionDelete(
					arg0[0], arg0[1]);
		}
	}

	public class GetSubscriptionDetailListTask extends
			MyAsyncTask<AudioSubscription, Void, GetAudioCategoryDetailRes> {
		AudioSubscription source=null;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetAudioCategoryDetailRes result) {
			if (result != null) {
				Gson gson = new Gson();
				String resultJson = gson.toJson(result);
				System.out.println("resultJson = " + resultJson);
				if (result.errorCode == 0) {					
					
					if (result.items != null) {
						ArrayList<AudioItem> items = new ArrayList<AudioItem>();
						for (int i = 0; i < result.items.length; i++) {
							items.add(result.items[i]);
						}
						AudioCategory cc=getAudioCategoryFromSubscription(source);
						cc.item=items;
						Message msg = mHandler.obtainMessage(
								3, cc);
						mHandler.sendMessage(msg);
						return;
					}
					
				}
			}
			subscriptionDetailTask = null;
		}

		@Override
		protected GetAudioCategoryDetailRes doInBackground(AudioSubscription... arg0) {
			source=arg0[0];
			return JsonOA2.getInstance(mContext)
					.getAudioSubscriptionDetail(arg0[0].album);
		}
	}
	private AudioCategory getAudioCategoryFromSubscription(AudioSubscription subscription)
	{
		AudioCategory temp = new AudioCategory();
		temp.category = subscription.album;
		temp.cache = 1;
		temp.description = subscription.name;
		temp.name = subscription.name;
		temp.type = "podcast";
		temp.mode = 1;
		temp.order=10;
		temp.image = subscription.coverUrl;
        return temp;
	}

}
