package com.tuyou.tsd.news.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.tuyou.tsd.news.comm.Contents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class UtilsTools {
	public static String fileize(int file) {
		DecimalFormat df = new DecimalFormat("#0.00");
		String fileizeString = "";
		if (file < 1024) {
			fileizeString = df.format((double) file) + "B";
		} else if (file < 1048576) {
			fileizeString = df.format((double) file / 1024) + "K";
		} else if (file < 1073741824) {
			fileizeString = df.format((double) file / 1048576) + "M";
		} else {
			fileizeString = df.format((double) file / 1073741824) + "G";
		}
		return fileizeString;
	}
	
	public static String getTime(int time) {
		if(time%60<10){
			return time/60+":0"+time%60;
		}
		return time/60+":"+time%60;
	}
	
	
	public static Bitmap downloadImage(String path) {
		Bitmap bitmap = null;
		try {
			InputStream input = null;
			try {
				URL url = new URL(path);
				HttpGet httpRequest = null;
				httpRequest = new HttpGet(url.toURI());
				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = (HttpResponse) httpclient
						.execute(httpRequest);
				HttpEntity entity = response.getEntity();
				BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(
						entity);
				input = bufHttpEntity.getContent();
				bitmap = BitmapFactory.decodeStream(input);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return saveFile(bitmap, Contents.IMAGE_PATH, path.substring(path.lastIndexOf("/") + 1));
	}
	
	
	public static Bitmap showBitmap(String path) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float hh = 120;
		float ww = 120;
		int be = 1;
		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(path, newOpts);
		return compressImage(bitmap);
	}

	public static Bitmap compressImage(Bitmap image) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			int options = 100;
			while (baos.toByteArray().length / 1024 > 100) {
				baos.reset();
				options -= 10;
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);

			}
			ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
			Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap saveFile(Bitmap bm, String path, String fileName) {
		try {
			File dirFile = new File(path);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			File myCaptureFile = new File(path + File.separator + fileName);
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(myCaptureFile));
			bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return showBitmap(path + File.separator + fileName);
	}
	
	public static String getAppVersion(Context context) throws Exception {

		PackageManager packageManager = context.getPackageManager();
	
		PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
		String versionName = packInfo.versionName;
		return versionName;
	}
	
	
	@SuppressLint("NewApi")
	public static int getSystemVersion(){

		int version= android.os.Build.VERSION.SDK_INT;
		return version;
	}
}
