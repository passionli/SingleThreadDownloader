package com.google.singlethreaddownloader.dao;

import java.util.List;

import com.google.singlethreaddownloader.DownloadTask;

public interface TaskDao {
	public boolean create(DownloadTask task);
	public boolean delete(DownloadTask task);
	public boolean update(DownloadTask task);
	public boolean findTaskByName(String key);
	public List<DownloadTask> findAllTasks();
}
