package com.google.singlethreaddownloader;

import java.util.ArrayList;
import java.util.List;

import com.google.singlethreaddownloader.DownloadTask.Status;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TaskDBService {
	private TaskOpenHelper openHelper;

	public TaskDBService(Context context) {
		openHelper = TaskOpenHelper.getInstance(context);
	}

	public DownloadTask getTask(String key) {
		SQLiteDatabase db = openHelper.getReadableDatabase();
		String sql = "select * from " + TaskOpenHelper.TABLE_NAME
				+ " where key = " + key;
		Cursor cursor = db.rawQuery(sql, null);
		DownloadTask task = new DownloadTask();
		task.key = cursor.getString(cursor.getColumnIndex("key"));
		task.name = cursor.getString(cursor.getColumnIndex("name"));
		task.percent = cursor.getFloat(cursor.getColumnIndex("percent"));
		task.startPosition = cursor.getInt(cursor
				.getColumnIndex("startPosition"));
		task.endPosition = cursor.getInt(cursor.getColumnIndex("endPosition"));
		task.downloadSize = cursor
				.getInt(cursor.getColumnIndex("downloadSize"));
		task.length = cursor.getInt(cursor.getColumnIndex("length"));
		task.path = cursor.getString(cursor.getColumnIndex("path"));
		// 数据库中保存枚举为String
		task.status = Status.valueOf(cursor.getString(cursor
				.getColumnIndex("status")));
		task.isFinished = cursor.getInt(cursor.getColumnIndex("isFinished")) > 0;
		task.downloadURL = cursor.getString(cursor
				.getColumnIndex("downloadURL"));
		return task;
	}

	public List<DownloadTask> getAllTask() {
		List<DownloadTask> list = new ArrayList<DownloadTask>();
		SQLiteDatabase db = openHelper.getReadableDatabase();
		String sql = "select * from " + TaskOpenHelper.TABLE_NAME;
		Cursor cursor = db.rawQuery(sql, null);
		while (cursor.moveToNext()) {
			DownloadTask task = new DownloadTask();
			task.key = cursor.getString(cursor.getColumnIndex("key"));
			task.name = cursor.getString(cursor.getColumnIndex("name"));
			task.percent = cursor.getFloat(cursor.getColumnIndex("percent"));
			task.startPosition = cursor.getInt(cursor
					.getColumnIndex("startPosition"));
			task.endPosition = cursor.getInt(cursor
					.getColumnIndex("endPosition"));
			task.downloadSize = cursor.getInt(cursor
					.getColumnIndex("downloadSize"));
			task.length = cursor.getInt(cursor.getColumnIndex("length"));
			task.path = cursor.getString(cursor.getColumnIndex("path"));
			// 数据库中保存枚举为String
			task.status = Status.valueOf(cursor.getString(cursor
					.getColumnIndex("status")));
			task.isFinished = cursor
					.getInt(cursor.getColumnIndex("isFinished")) > 0;
			task.downloadURL = cursor.getString(cursor
					.getColumnIndex("downloadURL"));
			list.add(task);
		}
		return list;
	}

	public void create(DownloadTask task) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL(
				"insert into "
						+ TaskOpenHelper.TABLE_NAME
						+ "(key, name, percent,startPosition,endPosition,downloadSize,length,path,status,isFinished,downloadURL) values(?,?,?,?,?,?,?,?,?,?,?)",
				new Object[] { task.key, task.name, task.percent,
						task.startPosition, task.endPosition,
						task.downloadSize, task.length, task.path,
						task.status.toString(), task.isFinished,
						task.downloadURL });
		db.close();
	}

	public void updateDownloadSize(DownloadTask task) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "update task set downloadSize = ? where key = ? ";
		db.execSQL(sql, new Object[] { task.downloadSize, task.key });
		db.close();
	}

	/**
	 * 可能多个线程并发调用
	 * 
	 * @param task
	 */
	public synchronized void update(DownloadTask task) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "update task set name=?,percent=?,startPosition=?,endPosition=?,downloadSize = ?,length=?,path=?,status=?,isFinished=?,downloadURL=? where key = ? ";
		db.execSQL(sql, new Object[] { task.name, task.percent,
				task.startPosition, task.endPosition, task.downloadSize,
				task.length, task.path, task.status.toString(),
				task.isFinished, task.downloadURL, task.key });
		db.close();
	}

	public void delete(DownloadTask task) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "delete from task where id = ?";
		Object[] bindArgs = new Object[] { task.key };
		db.execSQL(sql, bindArgs);
		db.close();
	}
}
