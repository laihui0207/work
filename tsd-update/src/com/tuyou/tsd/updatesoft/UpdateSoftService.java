package com.tuyou.tsd.updatesoft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Vector;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.network.AudioRes;
import com.tuyou.tsd.common.network.GetUpdateInfoRes;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.network.AppVersionInfo;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.common.util.MD5;
import com.tuyou.tsd.common.util.MyAsyncTask;

import android.app.DownloadManager;
import android.app.Service;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class UpdateSoftService extends Service {
	private static final String TAG = "UpdateService";

	private static final String APK_PATH = "/TuYouDownload/";
	private static final String VERSION_PATH = "Version/";
	
	private static final int START_TO_DOWNLOAD = 1;
	private static final int ALL_DOWNLOADS_ARE_COMPLETED = 2;
	private static final int DISPLAY_UPDATE_INFO = 3;
	private static final int INSTALL_DOWNLOAD_PACKAGE = 4;

	private MyBinder myBinder = new MyBinder();
	private DownloadCompleteReceiver receiver;
	private DownloadManager downloadManager;

	private GetUpdateInfoRes updateInfo;
	private int downloadIndex;
	private long downloadManagerId;
	private MyHandler myHandler;
	private GetUpdateInfoReqTask getUpdateInfo;
	private int updateStatus;
	private String versionPath;
	private MyReceiver m_myReceiver = null;

	private SharedPreferences prefs;
	private int updateSource=0;
	
	@Override
	public IBinder onBind(Intent intent) {
		LogUtil.v(TAG, "onBind");
		return myBinder;
	}

	public class MyBinder extends Binder {

		public UpdateSoftService getService() {
			return UpdateSoftService.this;
		}
	}

	@Override
	public void onCreate() {
		LogUtil.v(TAG, "onCreate");
		super.onCreate();

		prefs = getSharedPreferences(UpdateSoftService.class.getName(), 0);
		myHandler = new MyHandler();
		downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		receiver = new DownloadCompleteReceiver();
		registerReceiver(receiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));

		updateStatus = 0;
		m_myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.Update.START_DOWNLOAD);
		filter.addAction(TSDEvent.Update.START_INSTALL);
		filter.addAction(TSDEvent.Update.DISPLAY_INFO);
		filter.addAction(TSDEvent.Push.MESSAGE_ARRIVED);
		
		registerReceiver(m_myReceiver, filter);
		sendBroadcast(new Intent(TSDEvent.Update.SERVICE_STARTED));		 
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.v(TAG, "onStartCommand");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		LogUtil.v(TAG, "onDestroy");
		super.onDestroy();
		if (getUpdateInfo != null) {
			getUpdateInfo.cancel(true);
		}
		if (m_myReceiver != null) {
			unregisterReceiver(m_myReceiver);
		}
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		sendBroadcast(new Intent(TSDEvent.Update.SERVICE_STOPPED));
	}

	@Override
	public boolean onUnbind(Intent intent) {
		LogUtil.v(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	private void startDownLoad(int index) {
		LogUtil.v(TAG, "startDownLoad, index=" + index);
		String url = updateInfo.apk.apps[downloadIndex].file.url;
		String filename = url.substring(url.lastIndexOf("/") + 1);

		Uri resource = Uri.parse(url);
		DownloadManager.Request request = new DownloadManager.Request(resource);
		request.setAllowedNetworkTypes(Request.NETWORK_MOBILE | Request.NETWORK_WIFI);
		request.setAllowedOverRoaming(false);
		// 设置文件类型
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
		request.setMimeType(mimeString);
		// 在通知栏中显示
		request.setShowRunningNotification(true);
		request.setVisibleInDownloadsUi(true);
		// sdcard的目录下的download文件夹
		request.setDestinationInExternalPublicDir(APK_PATH + versionPath, filename);
		request.setTitle("");

//		request.addRequestHeader("hello", "hello");
		downloadManagerId = downloadManager.enqueue(request);
		LogUtil.i(TAG, "start to download new version apk: " + filename);
	}

	private boolean isFolderExist(String dir) {
		File folder = Environment.getExternalStoragePublicDirectory(dir);
		return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
	}
	

	private class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case START_TO_DOWNLOAD:
				if (preDisposeFile(updateInfo.apk.apps[downloadIndex]) == false) {
					startDownLoad(downloadIndex);
				} else {
					downLoadNext();
				}
				break;

			case ALL_DOWNLOADS_ARE_COMPLETED:
				LogUtil.d(TAG, "All packages are downloaded.");
				LogUtil.v(TAG, "prefs.putString, updatepath: " + versionPath);
				updateStatus = 0;
				prefs.edit().putString("updatepath", versionPath).commit();
				if(updateSource==1)
				{
					
				}
				break;

			case DISPLAY_UPDATE_INFO:
				displayUpdateInfo();
				break;

			case INSTALL_DOWNLOAD_PACKAGE:
				installDownloadFile();
				break;
			}
		}
	}

	private class GetUpdateInfoReqTask extends MyAsyncTask<Void, Void, GetUpdateInfoRes> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(final GetUpdateInfoRes result) {
			if (result != null) {
				if((result.errorCode==0)&&(result.apk.apps!=null))
				{
					updateInfo = result;
					Gson gson = new Gson();
					String resultJson = gson.toJson(result);
//					System.out.println("resultJson = " + resultJson);
					if (isLatestVersion()) // 判断是否有新版本，如果没有直接结束，有新版本，开始新版本下载。
					{
						downloadIndex = 0;
						myHandler.sendEmptyMessage(1);
						updateStatus = 2;
						prefs.edit().putString("updateInfo", resultJson).commit();
						versionPath = updateInfo.apk.version.replace('.', '_') + "/";
						isFolderExist(APK_PATH + versionPath);
						prefs.edit().putString("updatepath", "").commit();			
						
						
					} else {
						updateStatus = 0;
					}
				}
				else
				{
					updateStatus = 0;
				}

			} else {
				updateStatus = 0;
			}
		}

		@Override
		protected GetUpdateInfoRes doInBackground(Void... arg0) {
			return JsonOA2.getInstance(UpdateSoftService.this).getUpdateInfo();
		}
	}


	private class DownloadCompleteReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
				long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				if (downId == downloadManagerId) {
					LogUtil.v(TAG, " download complete! id : " + downId);					
					if (disposeDownloadResult()) {
						// 下载下一个
						downLoadNext();
					} else {
						String filename = getFileNameFromUrl(updateInfo.apk.apps[downloadIndex].file.url);
						fileDelete(filename);
					}
				}
			}
		}
	}

	private void downLoadNext() {
		LogUtil.v(TAG, "downLoadNext..., downloadIndex = " + downloadIndex + ", total = " + updateInfo.apk.apps.length);
		int num = updateInfo.apk.apps.length;
		if (downloadIndex < num - 1) {
			downloadIndex++;
			myHandler.sendEmptyMessage(START_TO_DOWNLOAD);
		} else {
			myHandler.sendEmptyMessage(ALL_DOWNLOADS_ARE_COMPLETED);
		}
	}

	private boolean disposeDownloadResult() {
		boolean result = false;
		String filename = getFileNameFromUrl(updateInfo.apk.apps[downloadIndex].file.url);
		String md5 = getMD5String(filename);
		long filesize = updateInfo.apk.apps[downloadIndex].file.size;
		long filesize1 = getFileSize(filename);
		if ((md5.equals(updateInfo.apk.apps[downloadIndex].file.checksum))
				&& (filesize1 == filesize)) {
			result = true;
		}
		LogUtil.v(TAG, "check download package result: " + result);
		return result;
	}

	private String getMD5String(String filename) {
		try {
			String path = Environment.getExternalStorageDirectory().toString();
			path = path + APK_PATH + versionPath + filename;
			return MD5.md5_file(path);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private boolean fileIsExists(String filename) {
		try {
			String path = Environment.getExternalStorageDirectory().toString();
			String fullname = path + APK_PATH + versionPath + filename;
			File f = new File(fullname);
			if (!f.exists()) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private long getFileSize(String filename) {
		long result = 0;
		try {
			String path = Environment.getExternalStorageDirectory().toString();
			String fullname = path + APK_PATH + versionPath + filename;
			File f = new File(fullname);
			result = f.length();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private void fileDelete(String filename) {
		LogUtil.v(TAG, "fileDelete, filename = " + filename);
		try {
			String path = Environment.getExternalStorageDirectory().toString();
			String fullname = path + APK_PATH + versionPath + filename;
			File f = new File(fullname);
			if (f.isFile()) {
				f.delete();
				return;
			}
			if (f.isDirectory()) {
				File[] childFiles = f.listFiles();
				if (childFiles == null || childFiles.length == 0) {
					f.delete();
					return;
				}
				for (int i = 0; i < childFiles.length; i++) {
					childFiles[i].delete();
				}
				f.delete();
			}
		} catch (Exception e) {

		}
	}

	private String getFileNameFromUrl(String url) {
		String filename = url.substring(url.lastIndexOf("/") + 1);
		return filename;
	}

	private boolean preDisposeFile(GetUpdateInfoRes.ApkInfo app) {
		LogUtil.v(TAG, "preDisposeFile, app info: " + app);
		String filename = getFileNameFromUrl(app.file.url);
		String oldVersion = getVersion(app);
		String newVersion = app.version.substring(0);
		LogUtil.v(TAG, "compare file:" + filename + ", old version: " + oldVersion + ", new version: " + newVersion);

		if (oldVersion.compareTo(newVersion) >= 0) {
			LogUtil.d(TAG, "new version is older than current version, ignore download...");
			return true;
		}

		if (fileIsExists(filename)) {
			if ((getFileSize(filename) ==updateInfo.apk.apps[downloadIndex].file.size)
					&& (getMD5String(filename)
							.equals(updateInfo.apk.apps[downloadIndex].file.checksum))) {
				LogUtil.d(TAG, "new version is invalid, ignore download...");
				return true;
			} else {
				fileDelete(filename);
			}
		}
		LogUtil.v(TAG, "new version apk is valid, prepare to download.");
		return false;
	}

	private boolean installApkQuietly(String apkPath) {
		boolean bRet = false;
		try {
			Field field = PackageManager.class.getDeclaredField("INSTALL_REPLACE_EXISTING");
			final int INSTALL_REPLACE_EXISTING = field.getInt(PackageManager.class);

			Method installPackageMethod = PackageManager.class
					.getDeclaredMethod("installPackage", Uri.class,
							IPackageInstallObserver.class, int.class,
							String.class);

			installPackageMethod.invoke(this.getPackageManager(),
					Uri.fromFile(new File(apkPath)),
					new PackageInstallObserver(), INSTALL_REPLACE_EXISTING,
					null);

			bRet = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		LogUtil.v(TAG, "installApkQuietly returns " + bRet);
		return bRet;
	}

	private String getVersion(GetUpdateInfoRes.ApkInfo app) {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(app.name, 0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return "0.0.0";
		}
	}

	private boolean isLatestVersion() {
		boolean result = false;

		for (int i = 0; i < updateInfo.apk.apps.length; i++) {
			String oldVersion = getVersion(updateInfo.apk.apps[i]);
			String newVersion = updateInfo.apk.apps[i].version.substring(0);
			LogUtil.v(TAG, "isLatestVersion(), app: " + updateInfo.apk.apps[i].name + ", old version: " + oldVersion + ", new version: " + newVersion);

			if (newVersion.compareTo(oldVersion) > 0) {
				result = true;
				break;
			}
		}

		LogUtil.v(TAG, "isLatestVersion() returns " + result);
		return result;
	}

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.v(TAG, "received the broadcast: " + action);

			if (action.equals(TSDEvent.Update.START_DOWNLOAD)) {
				if (updateStatus == 0) {
					getUpdateInfo = new GetUpdateInfoReqTask();
					getUpdateInfo.execute();
					updateStatus = 1;
					updateSource=0;
				}
			}
			else if (action.equals(TSDEvent.Update.START_INSTALL)) {
				myHandler.sendEmptyMessage(INSTALL_DOWNLOAD_PACKAGE);
			}
			else if (action.equals(TSDEvent.Update.DISPLAY_INFO)) {
				myHandler.sendEmptyMessage(DISPLAY_UPDATE_INFO);
			}
			else if (action.equals(TSDEvent.Push.MESSAGE_ARRIVED)) {				
				try {
					JSONObject jsobject = new JSONObject(intent.getStringExtra("message"));
					LogUtil.i(TAG, "Push broadcast=" + jsobject.toString());

					if (jsobject.getString("module").equals("update")) {
						String type = jsobject.getString("type");
						if(type.equals("update"))
						{
							JSONObject content = jsobject.getJSONObject("content");
							updateInfo = new GetUpdateInfoRes();
							Gson gson = new Gson();
							updateInfo.apk=gson.fromJson(content.toString(), GetUpdateInfoRes.ApkInfo.class);
							
							if (isLatestVersion()) // 判断是否有新版本，如果没有直接结束，有新版本，开始新版本下载。
							{
								downloadIndex = 0;
								myHandler.sendEmptyMessage(1);
								updateStatus = 2;
								prefs.edit().putString("updateInfo", gson.toJson(updateInfo)).commit();
								versionPath = updateInfo.apk.version.replace('.', '_') + "/";
								isFolderExist(APK_PATH + versionPath);
								prefs.edit().putString("updatepath", "").commit();							
								
							} else {
								updateStatus = 0;
							}
							
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
			} 
		}
	}

	private void displayUpdateInfo() {
		String info = prefs.getString("updateInfo", null);
		boolean installed = prefs.getBoolean("installed", false);
		if (installed) {
			if ((info != null) && (info.length() > 10)) {
				Intent intent1 = new Intent(UpdateSoftService.this,
						UpdateInfoActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("updateinfo", info);
				intent1.putExtras(bundle);
				intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				startActivity(intent1);

				prefs.edit().putString("updateInfo", "").commit();
				prefs.edit().putBoolean("installed", false).commit();
			}
		}
	}

	// 获取当前目录下所有的APK文件
	private static Vector<String> getAppFileName(String fileAbsolutePath) {
		Vector<String> vecFile = new Vector<String>();
		File file = new File(fileAbsolutePath);
		File[] subFile = file.listFiles();

		for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
			// 判断是否为文件夹
			if (!subFile[iFileLength].isDirectory()) {
				String filename = subFile[iFileLength].getName();
				// 判断是否为apk结尾
				if (filename.trim().toLowerCase().endsWith(".apk")) {
					vecFile.add(filename);
				}
			}
		}
		return vecFile;
	}

	private void installDownloadFile() {
		String subpath = prefs.getString("updatepath", null);
		LogUtil.v(TAG, "installDownloadFile..., subpath = " + subpath);

		if ((subpath != null) && (subpath.length() > 1)) {
			String path = Environment.getExternalStorageDirectory().toString();
			String fullpath = path + APK_PATH + subpath;
			Vector<String> vstring = getAppFileName(fullpath);

			for (int i = 0; i < vstring.size(); i++) {				
				String fullname = fullpath + vstring.elementAt(i);
				LogUtil.d(TAG, "install file: " + fullname);

				if( ! installApkQuietly(fullname) ) {
					LogUtil.d(TAG, "Silent install unsuccessful, let user install by manual. ");
					File installfile = new File(fullname);
					openFile(installfile);
				}
			}

			prefs.edit().putString("updatepath", "").commit();
			prefs.edit().putBoolean("installed", true).commit();	
			
			
			
			Gson gson = new Gson();
			String info = prefs.getString("updateInfo", null);
			GetUpdateInfoRes updateInfo1=gson.fromJson(info, GetUpdateInfoRes.class);
			AppVersionInfo version= new AppVersionInfo();			
			
			version.name=updateInfo1.apk.name;
			version.notes=updateInfo1.apk.notes;
			version.description=updateInfo1.apk.description;
			version.timestamp=updateInfo1.apk.timestamp;
			version.version=updateInfo1.apk.version;
			UpdateSoftService.this.isFolderExist(APK_PATH +VERSION_PATH);
			
			String versioninfo=gson.toJson(version);
			String ROOT_PATH = Environment.getExternalStorageDirectory()+APK_PATH +VERSION_PATH;
			File file= new File(ROOT_PATH,updateInfo1.apk.version);
			try {
				FileOutputStream out = new FileOutputStream(file);
				out.write(versioninfo.getBytes());
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			SubmitUpdateLogTask submitUpdateLog=new SubmitUpdateLogTask();
			submitUpdateLog.execute(updateInfo1.apk.id);
			
		}
	}

	private void openFile(File file) {
		LogUtil.v(TAG, "openFile, " + file.getName());
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}
	

	
	private static boolean chmodApkFile(File file) {

		if (null == file || !file.exists()) {
			return false;
		}

		if (Environment.getExternalStorageDirectory() != null && 
				file.getPath().startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())) {
			return false;
		}

		String[] command = { "chmod", "777", file.getParent() };
		ProcessBuilder builder = new ProcessBuilder(command);
		try {
			builder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		String[] command2 = { "chmod", "777", file.getAbsolutePath() };
		ProcessBuilder builder2 = new ProcessBuilder(command2);
		try {
			builder2.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}        
		return true;
	}
	
	public class SubmitUpdateLogTask extends
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

				} else {

				}
			} else {

			}
		}

		@Override
		protected AudioRes doInBackground(String... arg0) {
			return JsonOA2.getInstance(UpdateSoftService.this).submitUpdateLog(
					arg0[0]);
		}
	}
}
