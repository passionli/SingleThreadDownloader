package com.google.singlethreaddownloader;

public interface DownloadTaskListener{
	/**
	 * 任务状态改变
	 * @param task
	 */
	public void onTaskStatusChanged(DownloadTask task);
	/**
	 * 任务完成
	 * @param task
	 */
	public void onTaskFinished(DownloadTask task);
}