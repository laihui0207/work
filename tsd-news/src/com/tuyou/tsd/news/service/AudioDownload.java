package com.tuyou.tsd.news.service;

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

import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.news.comm.Contents;



public class AudioDownload {

	private String mPlayingFileName;
	private StreamingMediaPlayer mPlayer = null;
	private boolean mIsPlaying = false;
	public int mediaLengthInKb = 0;
	public int totalKbRead = 0;
	private boolean isStop = false;
	private boolean isStartChachePlay = false;
	private int filesize = 0;

	public AudioDownload(StreamingMediaPlayer player, boolean isPlaying) {
		mIsPlaying = isPlaying;
		mPlayer = player;
	}

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
							if ((!isStartChachePlay) && (filesize > 60 * 200)) {
								mPlayer.startPlayDownloadData();
								isStartChachePlay = true;
							}
						}
					}
                }
			} while (!isStop);
			fos.close();
			if (!isStop) {
				if (mIsPlaying) {
					synchronized (this) {
						if (!isStartChachePlay)  {
							mPlayer.startPlayDownloadData();
							isStartChachePlay = true;
						}
					}
				}
				saveDownloadData(tmp);
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

	private void saveDownloadData(File srcFile) {
		try {
			File dstFile = new File(Contents.MP3_PATH, mPlayingFileName);
			moveFile(srcFile, dstFile);
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

	public void startPlay() {
		mIsPlaying = true;
		synchronized (this) {
			if ((!isStartChachePlay) && (!isStop)&&(totalKbRead>100*1024)) {
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
