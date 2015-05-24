package com.tuyou.tsd.common.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public abstract class MyAsyncTask<TParams, TProgress, TResult> {
	private Handler holder 		= new Handler();
	private Thread  exeThread 	= null;
	private TParams params[]		= null;
	private volatile boolean canceled	= false;
	private volatile boolean executing	= false;
	private TResult result = null;
	private TProgress progresses[] = null;
	
	public MyAsyncTask()
	{
		
	}
	
	protected abstract TResult doInBackground(TParams... params);
	
	protected void onCancelled()
	{
		//Log.v("mioAsyncTask onCancelled()");
	}
	
	protected void onPostExecute(TResult result)
	{
		//LogUtil.proto("mioAsyncTask onPostExecute()");
	}
	
	protected void onPreExecute()
	{
		//LogUtil.proto("mioAsyncTask onPreExecute()");
	}
	
	protected void onProgressUpdate(TProgress... values)
	{
		//LogUtil.proto("mioAsyncTask onProgressUpdate()");

	}
	
	public final void publishProgress(TProgress... values)
	{
		//LogUtil.proto("mioAsyncTask publishProgress()");
		if (exeThread == null || exeThread != Thread.currentThread())
		{
			if (exeThread == null)
				;//LogUtil.proto("exeThread is null");
			else
				;//LogUtil.proto("can not pulish Progress in other thread.");
			return;
		}
		if (holder != null)
		{
			final TProgress valueArray[] = values;
			holder.post(new Runnable(){
				@Override
				public void run() {
					if (canceled == false)
					{
						onProgressUpdate(valueArray);
					}
				}
			});
		}
	}
	
	public final boolean cancel(boolean mayInterruptIfRunning)
	{
		if (exeThread != null && mayInterruptIfRunning == true)
		{
			if (canceled == false)
			{
				canceled = mayInterruptIfRunning;
				//LogUtil.proto("mioAsynTask cancel. thread_id="+exeThread.getId());
				exeThread.interrupt();
				return true;
			}
		}
		
		return false;
	}
	
	public final void execute(TParams... params)
	{
		if (executing == true && exeThread != null)
		{
			//LogUtil.proto("mioAsynTask already running.... thread_id="+exeThread.getId());
			return;
		}
		
		//LogUtil.proto("mioAsynTask execute() begin.");
		
		onPreExecute();
		this.params = params;
		
		Runnable task = new Runnable(){
			@Override
			public void run() {
				Looper.prepare();
				result = null;
				//LogUtil.proto("enter mioAsynTask run()");
				
				try
				{
					result = doInBackground(MyAsyncTask.this.params);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			
				if (holder != null)
				{
					holder.post(new Runnable(){
						@Override
						public void run() {
							executing = false; //设置任务结束标志
							exeThread = null; //回收thread
							
							//LogUtil.proto("mioAsynTask in holder thread. thread_id="+Thread.currentThread().getId());
							//LogUtil.proto("canceled flag="+canceled);
							if (canceled == true)
							{
								onCancelled();
							}
							else
							{
								onPostExecute(result);
							}
						}
					});
				}
				//LogUtil.proto("exit mioAsynTask run()");
			}
		};
		
		executing = true;//设置任务执行标志
		exeThread = new Thread(task);
		exeThread.start();
		
		//LogUtil.proto("mioAsynTask execute() end.");
	}
}
