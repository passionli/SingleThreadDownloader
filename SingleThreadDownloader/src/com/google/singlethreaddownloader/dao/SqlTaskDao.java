package com.google.singlethreaddownloader.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.singlethreaddownloader.DownloadTask;
import com.google.singlethreaddownloader.DownloadTask.Status;

public class SqlTaskDao implements TaskDao {
	public TaskOpenHelper taskOpenHelper = null;

	public SqlTaskDao(Context context) {
		taskOpenHelper = TaskOpenHelper.getInstance(context);
	}

	@Override
	public boolean create(DownloadTask task) {
		SQLiteDatabase db = taskOpenHelper.getWritableDatabase();
		db.execSQL("insert into " + TaskOpenHelper.TABLE_NAME + "("
				+ TaskOpenHelper.COLUMN_KEY + "," + TaskOpenHelper.COLUMN_NAME
				+ "," + TaskOpenHelper.COLUMN_PERCENT + ","
				+ TaskOpenHelper.COLUMN_START_POSITION + ","
				+ TaskOpenHelper.COLUMN_END_POSITION + ","
				+ TaskOpenHelper.COLUMN_DOWNLOAD_SIZE + ","
				+ TaskOpenHelper.COLUMN_LENGTH + ","
				+ TaskOpenHelper.COLUMN_PATH + ","
				+ TaskOpenHelper.COLUMN_STATUS + ","
				+ TaskOpenHelper.COLUMN_IS_FINISHED + ","
				+ TaskOpenHelper.COLUMN_DOWNLOAD_URL
				+ ") values(?,?,?,?,?,?,?,?,?,?,?)",
				new Object[] { task.key, task.name, task.percent,
						task.startPosition, task.endPosition,
						task.downloadSize, task.length, task.localPath,
						task.status.toString(), task.isFinished,
						task.downloadURL });
		db.close();
		return true;
	}

	@Override
	public boolean delete(DownloadTask task) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean update(DownloadTask task) {
		SQLiteDatabase db = taskOpenHelper.getWritableDatabase();
		String sql = "update " + TaskOpenHelper.TABLE_NAME + " set "
				+ TaskOpenHelper.COLUMN_NAME + "=?,"
				+ TaskOpenHelper.COLUMN_PERCENT + "=?,"
				+ TaskOpenHelper.COLUMN_START_POSITION + "=?,"
				+ TaskOpenHelper.COLUMN_END_POSITION + "=?,"
				+ TaskOpenHelper.COLUMN_DOWNLOAD_SIZE + " = ?,"
				+ TaskOpenHelper.COLUMN_LENGTH + "=?,"
				+ TaskOpenHelper.COLUMN_PATH + "=?,"
				+ TaskOpenHelper.COLUMN_STATUS + "=?,"
				+ TaskOpenHelper.COLUMN_IS_FINISHED + "=?,"
				+ TaskOpenHelper.COLUMN_DOWNLOAD_URL + "=? where "
				+ TaskOpenHelper.COLUMN_KEY + " = ? ";
		db.execSQL(sql, new Object[] { task.name, task.percent,
				task.startPosition, task.endPosition, task.downloadSize,
				task.length, task.localPath, task.status.toString(),
				task.isFinished, task.downloadURL, task.key });
		return false;
	}

	@Override
	public DownloadTask getTask(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DownloadTask> getAllTasks() {
		List<DownloadTask> list = new ArrayList<DownloadTask>();
		SQLiteDatabase db = taskOpenHelper.getReadableDatabase();
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
			task.localPath = cursor.getString(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_PATH));
			// 数据库中保存枚举为String
			task.status = Status.valueOf(cursor.getString(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_STATUS)));
			task.isFinished = cursor.getInt(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_IS_FINISHED)) > 0;
			task.downloadURL = cursor.getString(cursor
					.getColumnIndex(TaskOpenHelper.COLUMN_DOWNLOAD_URL));
			list.add(task);
		}

		return list;
	}

	@Override
	public void close() {
		if (taskOpenHelper != null) {
			taskOpenHelper.close();
		}
	}

}
