package com.tuyou.tsd.cardvr.utils;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;

import com.tuyou.tsd.cardvr.bean.VideoInfo;
import com.tuyou.tsd.cardvr.comm.Constant;

public class Utility {
	/**
	 * 视频文件过滤器[3gp|mp4]
	 */
	private static final FileFilter VIDEOS_FILTER = new FileFilter() {
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().matches("^.*?\\.(3gp|mp4)$");
		}
	};

	/**
	 * 格式化显示输出文件大小
	 * 
	 * @param fileSize
	 * @return
	 */
	private static String formatFileSize(float fileSize) {
		long kb = 1024;
		long mb = (kb * 1024);
		long gb = (mb * 1024);

		if (fileSize < kb)
			return String.format("%d B", (int) fileSize);
		else if (fileSize < mb)
			return String.format("%.2f KB", fileSize / kb);
		else if (fileSize < gb)
			return String.format("%.2f MB", fileSize / mb);
		else
			return String.format("%.2f GB", fileSize / gb);
	}

	/**
	 * 取得视频文件相关信息 填充视频实体类
	 * 
	 * @param ctx
	 * @param videoinfo
	 * @param file
	 */
	private static void fillVideoInfo(Context ctx, VideoInfo videoinfo,
			File file) {
		boolean is3gp = false;
		boolean ismp4 = false;

		is3gp = file.getName().toLowerCase()
				.endsWith(Constant.FILETYPE_VIDEO_3GPP);
		ismp4 = file.getName().toLowerCase()
				.endsWith(Constant.FILETYPE_VIDEO_MP4);

		Bitmap typeIcon = null;// 默认类型
		Bitmap thumbnail = null;// 默认截图

		// 使用AndroidSDK2.2提供的缩略图创建类 可以直接从视频文件中抽取第一帧图片作为缩略图显示
		Bitmap tempThumb = ThumbnailUtils.createVideoThumbnail(
				file.getAbsolutePath(), Thumbnails.MINI_KIND);
		if (is3gp) {
			typeIcon = BitmapFactory.decodeResource(ctx.getResources(),
					android.R.id.icon1);// R.id.icon_3gp);
			thumbnail = (null != tempThumb) ? tempThumb : BitmapFactory
					.decodeResource(ctx.getResources(), android.R.id.icon);// thumb_3gp
		} else if (ismp4) {
			typeIcon = BitmapFactory.decodeResource(ctx.getResources(),
					android.R.id.icon2);// R.id.icon_mp4);
			thumbnail = (null != tempThumb) ? tempThumb : BitmapFactory
					.decodeResource(ctx.getResources(), android.R.id.icon);// thumb_mp4
		}

		videoinfo.setName(file.getName());
		videoinfo.setSize(formatFileSize(file.length()));
		videoinfo.setPath(file.getAbsolutePath());
//		videoinfo.setType(typeIcon);
		videoinfo.setThumbnail(thumbnail);
	}

	// 文件操作相关方法
	/** 遍历SD卡 取得播放视频列表 */
	public static ArrayList<VideoInfo> getVideosFromSD(Context ctx) {
		ArrayList<VideoInfo> videolist = new ArrayList<VideoInfo>();

		// 文件搜索根路径
		File root = Environment.getExternalStorageDirectory();
		if (!root.exists())
			return videolist;

		/*
		 * 使用文件过滤器[视频格式限定为3gp mp4] 遍历sd卡取得所有符合过滤原则的视频文件 添加到视频播放列表
		 */
		for (File file : root.listFiles(VIDEOS_FILTER)) {
			if (file.isFile()) {
				VideoInfo video = new VideoInfo();
				fillVideoInfo(ctx, video, file);
				videolist.add(video);
			}
		}

		return videolist;
	}

	/**
	 * 遍历文件夹 取得播放视频列表
	 * 
	 * @param ctx
	 * @param list
	 * @param file
	 * @author ZL
	 * @date 2014-7-15 上午9:43:51
	 */
	public static void getVideoFile(final Context ctx,
			final List<VideoInfo> list, File file) {

		file.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				String name = file.getName();
				int i = name.indexOf('.');
				if (i != -1) {
					name = name.substring(i);
					if (name.equalsIgnoreCase(".mp4")
							|| name.equalsIgnoreCase(".3gp")) {

						VideoInfo video = new VideoInfo();
						fillVideoInfo(ctx, video, file);
						list.add(video);

						return true;
					}
				} else if (file.isDirectory()) {
					getVideoFile(ctx, list, file);
				}
				return false;
			}
		});
	}

	/** 检查sd卡是否存在 如果sd卡存在，则返回true */
	public static boolean checkSdcard() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * OpenGL 3D动画使用
	 * 
	 * @param data
	 * @return
	 */
	public static FloatBuffer createFloatBuffer(float data[]) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(data.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer outBuffer = vbb.asFloatBuffer();
		outBuffer.put(data).position(0);
		return outBuffer;
	}

	/**
	 * OpenGL 3D动画使用
	 * 
	 * @param data
	 * @return
	 */
	public static Bitmap getTextureFromBitmapResource(Context context,
			int resourceId) {

		Bitmap bitmap = null;
		Matrix yFlipMatrix = new Matrix();
		yFlipMatrix.postScale(1, -1); // flip Y axis
		try {
			bitmap = BitmapFactory.decodeResource(context.getResources(),
					resourceId);

			return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), yFlipMatrix, false);
		} finally {
			if (bitmap != null) {
				bitmap.recycle();
			}
		}
	}

}
