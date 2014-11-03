package com.google.singlethreaddownloader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskOpenHelper extends SQLiteOpenHelper {
	private static TaskOpenHelper sInstance;
	private static final String DBNAME = "single_thread_downloader.db";
	public static final String TABLE_NAME = "task";
	private static final int VERSION = 1;
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_KEY = "key";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_PERCENT = "percent";
	public static final String COLUMN_START_POSITION = "startPosition";
	public static final String COLUMN_END_POSITION = "endPosition";
	public static final String COLUMN_DOWNLOAD_SIZE = "downloadSize";
	public static final String COLUMN_LENGTH = "length";
	public static final String COLUMN_PATH = "path";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_ISFINISHED = "isFinished";
	public static final String COLUMN_DOWNLOAD_URL = "downloadURL";

	public TaskOpenHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}

	public static synchronized TaskOpenHelper getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new TaskOpenHelper(context.getApplicationContext());
		}
		return sInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + COLUMN_ID
				+ " integer primary key autoincrement," + COLUMN_KEY
				+ " varchar(100)," + COLUMN_NAME + " varchar(50)," + COLUMN_PERCENT
				+ " FLOAT," + COLUMN_START_POSITION + " INTEGER," + COLUMN_END_POSITION
				+ " INTEGER," + COLUMN_DOWNLOAD_SIZE + " INTEGER," + COLUMN_LENGTH
				+ " INTEGER," + COLUMN_PATH + " varchar(200)," + COLUMN_STATUS
				+ " VARCHAR(50)," + COLUMN_ISFINISHED + " INGETER, " + COLUMN_DOWNLOAD_URL
				+ " varchar(100))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}
