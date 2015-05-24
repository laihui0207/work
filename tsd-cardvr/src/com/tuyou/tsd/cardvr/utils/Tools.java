package com.tuyou.tsd.cardvr.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.text.format.Formatter;

public class Tools {

	/**
	 * 用字符串生成二维码
	 * 
	 * @param str
	 * @return
	 * @throws WriterException
	 */
	public static Bitmap Create2DCode(String str) throws WriterException {
		// 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
		BitMatrix matrix = new MultiFormatWriter().encode(str,
				BarcodeFormat.QR_CODE, 300, 300);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		// 二维矩阵转为一维像素数组,也就是一直横着排了
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = 0xff000000;
				}
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		// 通过像素数组生成bitmap
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	// public static String getTimeToData(long time){
	// SimpleDateFormat sdf =new SimpleDateFormat("yyyy.MM.dd\nhh:mm:ss");
	// String date = sdf.format(new Date(time));
	// return date;
	// }

	public static String getTime(long time, String tpye) {
		SimpleDateFormat sdf = new SimpleDateFormat(tpye);
		String date = sdf.format(new Date(time));
		return date;
	}

	// public static String getTimeToDataString(long time){
	// SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd\nhhmmss");
	// String date = sdf.format(new Date(time));
	// return date;
	// }

	// public static String getTimeToDataStringNoN(long time){
	// SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMddhhmmss");
	// String date = sdf.format(new Date(time));
	// return date;
	// }

	// public static Bitmap getVideoThumbnail(String videoPath, int width, int
	// height, int kind) {
	// Bitmap bitmap = null;
	// // 获取视频的缩略图
	// bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
	// bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
	// ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	// return bitmap;
	// }

	public static Bitmap showBitmap(String path) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float hh = 220;
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
	
	public static Bitmap showBitmap2(String path) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth/2;
		int h = newOpts.outHeight/2;
		float hh = 110;
		float ww = 60;
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
			ByteArrayInputStream isBm = new ByteArrayInputStream(
					baos.toByteArray());
			Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	@SuppressLint("SimpleDateFormat")
	public static String getWeekStr(String sdate) {
		String[] weekDaysName = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" }; 
		Calendar calendar = Calendar.getInstance(); 
		java.util.Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			date= sdf.parse(sdate);
		} catch (Exception e) {
			e.printStackTrace();
		}  
		calendar.setTime(date);
		int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; 
		return weekDaysName[intWeek]; 
	}
	
	public static  boolean isHadSDcard(){
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {  
			return true;  
			} else  
			return false;  
	}

}
