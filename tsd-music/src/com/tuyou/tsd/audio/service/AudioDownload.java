package com.tuyou.tsd.audio.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import com.tuyou.tsd.audio.comm.Contents;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.common.util.MD5;
/**
 * 
 * 该类实现下载音乐文件
 * 如果创建时isPlaying为真则下载到一定数量后开始播放。否则不播放
 * 下载完成保存到制定文件。 
 ***/
public class AudioDownload {

	private String mPlayingFileName;
	private StreamingMediaPlayer mPlayer = null;
	private boolean mIsPlaying = false;
	public int mediaLengthInKb = 0;
	public int totalKbRead = 0;
	private boolean isStop = false;
	private boolean isStartChachePlay = false;
	private int filesize = 0;
	private String mMD5=null;

	public AudioDownload(StreamingMediaPlayer player, boolean isPlaying,String md5) {
		mIsPlaying = isPlaying;
		mPlayer = player;
		mMD5=md5;
	}
	/*
	 * 启动下载
	 * */
	public void runDownLoad(String url, String filename) {
		mPlayingFileName = filename;
		File tmp = new File(Contents.MP3_PATH, mPlayingFileName + "_temp");
		LogUtil.i("audio download url=", url);		
		File lengthfile= new File(Contents.MP3_PATH, mPlayingFileName + ".dat");		
		URL mUrl;
		int savelength=0;
		
		try {
			mUrl = new URL(url);
			RandomAccessFile fos = new RandomAccessFile(tmp, "rw");
			FileInputStream in;
           	if(lengthfile.exists())
       		{       
           		in= new FileInputStream(lengthfile);
           		byte[] buffer=new byte[32];
           		int len=in.read(buffer);
           		byte[] newbuffer=new byte[len];
           		for(int i=0;i<len;i++)
           		{
           			newbuffer[i]=buffer[i];
           		}
           		String aa=new String(newbuffer);
       			savelength=Integer.valueOf(aa);
       			in.close();
       		}

			
			HttpURLConnection con = (HttpURLConnection) mUrl.openConnection();
			con.addRequestProperty("Range","bytes="+savelength+"-");
			con.setConnectTimeout(20000);
			con.setReadTimeout(40000);
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");
			con.connect();
			InputStream is = con.getInputStream();
			if(savelength==0)
			{
			   fos.setLength(con.getContentLength());
			}
			fos.seek(savelength);
			filesize=savelength;
			totalKbRead=savelength;
			mediaLengthInKb = con.getContentLength()+savelength ;// 歌曲文件长度
			byte[] buffer = new byte[64 * 1024];
			int length = 0;

			do {

				length = is.read(buffer);
				if (length <= 0)
					break;
				if(!isStop)
                {
					if (mIsPlaying) {
						synchronized (this) {
							fos.write(buffer, 0, length);
						}
					} else {
						fos.write(buffer, 0, length);
					}	
                
					filesize += length;
					totalKbRead = filesize ;
					{
						 
						FileOutputStream out=new FileOutputStream(lengthfile);
						String aa= String.valueOf(filesize);          		
		           		out.write(aa.getBytes());	
		           		out.close();
					}
	
					float loadProgress = ((float) totalKbRead / (float) mediaLengthInKb);
					float percent = 100 * loadProgress;
					int percentd = (int) percent;
					mPlayer.dataLoadUpdate(mPlayingFileName, percentd);
					if (mIsPlaying) {
						synchronized (this) {
							if ((!isStartChachePlay) && (filesize > 1024 * 200)) {
								mPlayer.startPlayDownloadData();
								isStartChachePlay = true;
							}
						}
					}
                }
			} while (!isStop);
			fos.close();
			if (!isStop) {
				saveDownloadData(tmp,url);
				mPlayer.dataDownLoaded(this, 0);
				tmp.delete();
				lengthfile.delete();
			}
			else
			{
				mPlayer.dataDownLoaded(this, 4);
			}		
			
		} catch (Exception e) {
			e.printStackTrace();
			if (mIsPlaying) {
				mPlayer.dataDownLoaded(this, 1);
			} else {
				mPlayer.dataDownLoaded(this, 3);
			}
		}
	}
/*
 * 下载完成，保存数据时调用
 * */
	private void saveDownloadData(File srcFile,String url) {
		try {
			 String md5=null;
			 md5=MD5.md5_file(srcFile.getPath());
			 if(url.contains("xiami")){
				 File dstFile = new File(Contents.MP3_PATH, mPlayingFileName);
					moveFile(srcFile, dstFile);
			 }else{
				 if((mMD5==null)||(md5.equals(mMD5)))
				 {			
					File dstFile = new File(Contents.MP3_PATH, mPlayingFileName);
					moveFile(srcFile, dstFile);
				 }
			 }
		} catch (Exception e) {
			e.printStackTrace();
			mPlayer.dataDownLoaded(this, 2);
		}
	}

	public void stopDownload() {
		isStop = true;
	}

	private void moveFile(File oldLocation, File newLocation)
			throws IOException {
		if (oldLocation.exists()) {			
			BufferedInputStream reader = new BufferedInputStream(
					new FileInputStream(oldLocation));
			BufferedOutputStream writer = new BufferedOutputStream(
					new FileOutputStream(newLocation, false));
			try {
				byte[] buff = new byte[8192];
				int numChars;
				while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
					writer.write(buff, 0, numChars);
				}
			} catch (IOException ex) {
				throw new IOException("IOException when transferring "
						+ oldLocation.getPath() + " to "
						+ newLocation.getPath());
			} finally {
				try {
					if (reader != null) {
						writer.close();
						reader.close();
					}
				} catch (IOException ex) {
					LogUtil.e(getClass().getName(),
							"Error closing files when transferring "
									+ oldLocation.getPath() + " to "
									+ newLocation.getPath());
				}
			}
		} else {
			throw new IOException(
					"Old location does not exist when transferring "
							+ oldLocation.getPath() + " to "
							+ newLocation.getPath());
		}
	}

	public String getDownloadingFile() {
		return mPlayingFileName;
	}
	/*
	 * 当一个文件下载以缓存方式启动（mIsPlaying=false），但是，在下载完成之前改为当前播放文件，则调用该
	 * 函数播放已缓存部分。
	 * */
	public void startPlay() {
		mIsPlaying = true;
		synchronized (this) {
			if ((!isStartChachePlay) && (!isStop)&&(totalKbRead>200*1024)) {
				mPlayer.startPlayDownloadData();
				isStartChachePlay = true;
				float loadProgress = ((float) totalKbRead / (float) mediaLengthInKb);
				float percent = 100 * loadProgress;
				int percentd = (int) percent;
				mPlayer.dataLoadUpdate(mPlayingFileName, percentd);
			}
		}
	}

}
