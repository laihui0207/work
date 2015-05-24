package com.tuyou.tsd.launcher;

import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import com.tuyou.tsd.R;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.base.BaseActivity;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.core.CoreService;
import com.tuyou.tsd.core.CoreService.ContentType;
import com.tuyou.tsd.core.CoreService.WorkingMode;

public class AppsActivity extends BaseActivity implements TabContentFactory {
	private static final String TAG = "AppsActivity";
	private AppsInfoAdapter mAdapter;
	
	private CoreService mCoreService;

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			LogUtil.v(TAG, "onServiceDisconnected.");
			mCoreService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtil.v(TAG, "onServiceConnected.");
			mCoreService = ((CoreService.LocalBinder)service).getService();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.apps_activity);

		TabHost tabHost = (TabHost) findViewById(R.id.apps_tabhost);
		tabHost.setup();
		tabHost.addTab(tabHost.newTabSpec("allApps").setIndicator("应用").setContent(this));
//		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("tab2").setContent(this));
//		tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("tab3").setContent(this));
	}

	@Override
	protected void onResume() {
		bindService(new Intent(this, CoreService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unbindService(mServiceConnection);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public View createTabContent(String tag) {
		GridView gridView = new GridView(this);

		mAdapter = new AppsInfoAdapter(this, 0);

		// Load the installed apps
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedApps = getPackageManager().queryIntentActivities(mainIntent, 0);
//        Log.d("AppsActivity", "installedApps.size = " + installedApps.size());

        for (ResolveInfo app : installedApps) {
        	ActivityInfo activity = app.activityInfo;
        	if (activity != null &&
        		(activity.packageName.equals(TSDComponent.CAR_DVR_PACKAGE) ||
        		 activity.packageName.equals(TSDComponent.NAVIGATOR_PACKAGE) ||
        		 activity.packageName.equals(TSDComponent.AUDIO_PACKAGE) ||
        		 activity.packageName.equals(TSDComponent.NEWS_PACKAGE) ||
        		 activity.packageName.equals(TSDComponent.PODCAST_PACKAGE) ||
        		 activity.packageName.equals(TSDComponent.SETTINGS_PACKAGE) ))
        	{
//            	Log.d("AppsActivity", "app = " + app);
            	mAdapter.addData(app);
        	}
        }

		gridView.setNumColumns(4);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// launche the activity
				ResolveInfo info = mAdapter.getItem(position);
				ActivityInfo activity = info.activityInfo;
				jumpToActivity(activity);
			}
		});
		return gridView;
	}

	private void jumpToActivity(ActivityInfo activity) {
		LogUtil.d(TAG, "start activity: " + activity);
		String pkg = activity.packageName;

		// 根据不同选择进入对应的模式中
		if (pkg.equals(TSDComponent.NAVIGATOR_PACKAGE)) {
			if (mCoreService != null) {
				mCoreService.changeMode(WorkingMode.MODE_MAP, ContentType.TYPE_MAP);
			}
		}
		else if (pkg.equals(TSDComponent.AUDIO_PACKAGE)) {
			if (mCoreService != null) {
				mCoreService.changeMode(WorkingMode.MODE_AUDIO, ContentType.TYPE_MUSIC);
			}
		}
		else if (pkg.equals(TSDComponent.NEWS_PACKAGE)) {
			if (mCoreService != null) {
				mCoreService.changeMode(WorkingMode.MODE_AUDIO, ContentType.TYPE_NEWS);
			}
		}
		else if (pkg.equals(TSDComponent.PODCAST_PACKAGE)) {
			if (mCoreService != null) {
				mCoreService.changeMode(WorkingMode.MODE_AUDIO, ContentType.TYPE_JOKE);
			}
		}
		else {
			if (mCoreService != null) {
				mCoreService.changeMode(WorkingMode.MODE_STANDBY);
			}
			HelperUtil.startActivityWithFadeInAnim(AppsActivity.this, activity.packageName, activity.name);
		}
	}

	private static class AppsInfoAdapter extends ArrayAdapter<ResolveInfo> {
		private Context context;

		public AppsInfoAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView v;
			if (convertView == null) {
				v = new TextView(context);
			} else {
				v = (TextView) convertView;
			}

			ResolveInfo info = getItem(position);
			Drawable icon = info.loadIcon(context.getPackageManager());
			CharSequence label = info.loadLabel(context.getPackageManager());
			v.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
			v.setText(label);
			
			v.setPadding(20, 20, 20, 20);
			v.setGravity(Gravity.CENTER);

			return v;
		}

//		public void addAllData(List<ApplicationInfo> list) {
//			addAll(list);
//		}

		public void addData(ResolveInfo data) {
			add(data);
		}
	}

}
