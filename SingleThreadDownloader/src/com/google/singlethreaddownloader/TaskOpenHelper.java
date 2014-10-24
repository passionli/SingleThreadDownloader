package com.google.singlethreaddownloader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskOpenHelper extends SQLiteOpenHelper {
	private static final String DBNAME = "single_thread_downloader.db";
	public static final String TABLE_NAME = "task";
	private static final int VERSION = 1;

	public TaskOpenHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME
				+ " (id integer primary key autoincrement,key varchar(100),name varchar(50),percent FLOAT,startPosition INTEGER,endPosition INTEGER,downloadSize INTEGER,length INTEGER,path varchar(200),status VARCHAR(50),isFinished INGETER, downloadURL varchar(100))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}
