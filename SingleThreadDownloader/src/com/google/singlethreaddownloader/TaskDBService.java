package com.google.singlethreaddownloader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.singlethreaddownloader.DownloadTask.Status;

public class TaskDBService {
	private final TaskOpenHelper openHelper;

	public TaskDBService(Context context) {
		openHelper = TaskOpenHelper.getInstance(context);
	}

	public DownloadTask getTask(String key) {
		SQLiteDatabase db = openHelper.getReadableDatabase();
		String sql = "select * from " + TaskOpenHelper.TABLE_NAME + " where "
				+ TaskOpenHelper.COLUMN_KEY + " = " + key;
		Cursor cursor = db.rawQuery(sql, null);
		DownloadTask task = new DownloadTask();
		task.key = cursor.getString(cursor
				.getColumnIndex(TaskOpenHelper.COLUMN_KEY));
		task.name = cursor.getString(cursor
				.getColumnIndex(TaskOpenHelper.COLUMN_NAME));
		task.percent = cursor.getFloat(cursor
				.getColumnIndex(TaskOpenHelper.COLUMN_PERCENT));
		task.startPosition = cursor.getInt(cursor
				.getColumnIndex(TaskOpenHelper.COLUMN_START_POSITION));
		task.endPosition = cursor.getInt(cursor
				.getColumnIndex(TaskOpenHelper.COLUMN_END_POSITION));
		task.downloadSize = cursor.getInt(cursor
				.getColumnIndex(TaskOpenHelper.COLUMN_DOWNLOAD_SIZE));
		task.length = cursor.getInt(cursor
				.getColumnIndex(TaskOpenHelper.COLUMN_LENGTH));
		task.path = cursor.getString(cursor
				.getColumnIndex(TaskOpenHelper.COLUMN_PATH));
		// 数据库中保存枚举为String
		task.status = Status.valueOf(cursor.getString(cursor
				.getColumnIndex(TaskOpenHelper.COLUMN_STATUS)));
		task.isFinished = cursor.getInt(cursor
				.getColumnIndex(TaskOpenHelper.COLUMN_ISFINISHED)) > 0;
		task.downloadURL = cursor.getString(cursor
				.getColumnIndex(TaskOpenHelper.COLUMN_DOWNLOAD_URL));
		return task;
	}

	public List<DownloadTask> getAllTask() {
		List<DownloadTask> list = new ArrayList<DownloadTask>();
		SQLiteDatabase db = openHelper.getReadableDatabase();
		String sql = "select * from " + TaskOpenHelper.TABLE_NAME;
		Cursor cursor = db.rawQuery(sql, null);
		while (cursor.moveToNext()) {
			DownloadTask task = new DownloadTask();
			task.key = cursor.getString(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_KEY));
			task.name = cursor.getString(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_NAME));
			task.percent = cursor.getFloat(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_PERCENT));
			task.startPosition = cursor.getInt(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_START_POSITION));
			task.endPosition = cursor.getInt(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_END_POSITION));
			task.downloadSize = cursor.getInt(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_DOWNLOAD_SIZE));
			task.length = cursor.getInt(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_LENGTH));
			task.path = cursor.getString(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_PATH));
			// 数据库中保存枚举为String
			task.status = Status.valueOf(cursor.getString(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_STATUS)));
			task.isFinished = cursor.getInt(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_ISFINISHED)) > 0;
			task.downloadURL = cursor.getString(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_DOWNLOAD_URL));
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
		// db.close();
	}

	public synchronized void close() {
		if (openHelper != null) {
			openHelper.close();
		}
	}

	public void delete(DownloadTask task) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "delete from " + TaskOpenHelper.TABLE_NAME + " where "
				+ TaskOpenHelper.COLUMN_ID + " = ?";
		Object[] bindArgs = new Object[] { task.key };
		db.execSQL(sql, bindArgs);
		db.close();
	}
}
