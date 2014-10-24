package com.google.singlethreaddownloader;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class DownloadApp extends Application {
	private static final String TAG = "DownloadApp";
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
	}

	public DownloadApp() {
		// context = getApplicationContext();
	}

	public static void setContext(Context c) {
		context = c;
	}

	public static Context getContext() {
		return context;
	}
}
