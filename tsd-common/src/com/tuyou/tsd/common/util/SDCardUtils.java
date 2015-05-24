//package com.tuyou.tsd.common.util;
//
//import java.io.File;
//
//import android.content.Context;
//import android.os.Environment;
//import android.os.StatFs;
//import android.os.storage.StorageManager;
//import android.text.format.Formatter;
//
//public class SDCardUtils {
//	public static  String SDPath(Context context){
//    	String extSdCard="";
//    	try {
//        	StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
//        	String[] paths = (String[]) sm.getClass().getMethod("getVolumePaths", null).invoke(sm, null);
//        	String esd = Environment.getExternalStorageDirectory().getPath();
//        	for (int i = 0; i < paths.length; i++) {
//        	    if (paths[i].equals(esd)) {
//        	        continue;
//        	    }
//        	    File sdFile = new File(paths[i]);
//        	    if (sdFile.canWrite()){
//        	    	extSdCard =  paths[i];
//        	        return extSdCard;
//        	    }
//        	}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return extSdCard;
//    }
//	
//	public static long getStore(Context context,String filePath) {
//		 StatFs stat = new StatFs(filePath);  
//		 long blockSize = stat.getBlockSize();  
//		 long totalBlocks = stat.getBlockCount();  
//		 return blockSize * totalBlocks;  
//		}
//	
////	public static String CAR_DVR_VIDEO_PATH(Context context){
////		return SDPath(context)+ "/cardvr/videos";
////	}
////	
////	public static String CAR_DVR_ACCIDENT_VIDEO_PATH(Context context){
////		return SDPath(context)+ "/cardvr/accidents";
////	}
////	
////	public static String CAR_DVR_ALERT_VIDEO_PATH(Context context){
////		return SDPath(context)+ "/cardvr/alerts";
////	}
////	
////	public static String CAR_DVR_PICTURES_PATH(Context context){
////		return SDPath(context)+ "/cardvr/pictures";
////	}
//}
