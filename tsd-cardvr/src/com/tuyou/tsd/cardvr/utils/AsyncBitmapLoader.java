package com.tuyou.tsd.cardvr.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;
import android.widget.ImageView;

public class AsyncBitmapLoader {
	/**
	 * 内存图片软引用缓冲
	 */
	private HashMap<String, SoftReference<Bitmap>> imageCache = null;

	public AsyncBitmapLoader() {
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
	}

	/**
	 * 异步加载
	 * @param context
	 * @param imageView
	 * @param file
	 * @param imageCallBack
	 * @param flag
	 * @return
	 * @author ZL  
	 * @date 2014-8-4 上午11:03:20
	 */
	public Bitmap loadBitmap(final Context context,final ImageView imageView, final File file,
			final ImageCallBack imageCallBack,final boolean flag) {
		// 在内存缓存中，则返回Bitmap对象
		if (imageCache.containsKey(file.getAbsolutePath())) {
			SoftReference<Bitmap> reference = imageCache.get(file.getAbsolutePath());
			Bitmap bitmap = reference.get();
			if (bitmap != null) {
				return bitmap;
			}
		} 
		else {
			/**
			 * 加上一个对本地缓存的查找
			 */
			String bitmapName = file.getAbsolutePath()
					.substring(file.getAbsolutePath().lastIndexOf("/") + 1);
			File cacheDir = null;
			if(flag){
				// 有SD卡
				cacheDir = context.getExternalFilesDir(null);
			}else{
				// 无SD卡
				cacheDir = context.getFilesDir();
			}
			
			File[] cacheFiles = cacheDir.listFiles();
			int i = 0;
			if (null != cacheFiles) {
				for (; i < cacheFiles.length; i++) {
					if (bitmapName.equals(cacheFiles[i].getName())) {
						break;
					}
				}

				if (i < cacheFiles.length) {
					return BitmapFactory.decodeFile(context.getExternalFilesDir(null)
							+ bitmapName);
				}
			}
		}

		final Handler handler = new Handler() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				imageCallBack.imageLoad(imageView, (Bitmap) msg.obj);
			}
		};

		// 如果不在内存缓存中，也不在本地（被jvm回收掉），则开启线程下载图片
		new Thread() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				Bitmap tempThumb = ThumbnailUtils.createVideoThumbnail(
						file.getAbsolutePath(), Thumbnails.MINI_KIND); 
				
				Bitmap bitmap = (null != tempThumb) ? tempThumb : BitmapFactory
						.decodeResource(context.getResources(), android.R.id.icon);// thumb_mp4
				imageCache.put(file.getAbsolutePath(), new SoftReference<Bitmap>(bitmap));
				Message msg = handler.obtainMessage(0, bitmap);
				handler.sendMessage(msg);

				File bitmapFile = null;
				if(flag){
					// 有SD卡
					bitmapFile = new File(context.getExternalFilesDir(null)+ file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1));
				}else{
					// 无SD卡
					bitmapFile = new File(context.getFilesDir()+ file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1));
				}

				if (!bitmapFile.exists()) {
					try {
						bitmapFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(bitmapFile);
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();

		return null;
	}

	public interface ImageCallBack {
		public void imageLoad(ImageView imageView, Bitmap bitmap);
	}
}
