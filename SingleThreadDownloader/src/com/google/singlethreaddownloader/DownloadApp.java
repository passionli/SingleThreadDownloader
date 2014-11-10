package com.google.singlethreaddownloader;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.singlethreaddownloader.util.AppUtil;

public class DownloadApp extends Application {
	private static final String TAG = "DownloadApp";
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		AppUtil.AVAILABLE_PROCESSORS = Runtime.getRuntime()
				.availableProcessors();
		Log.d(TAG, "AppUtil.AVAILABLE_PROCESSORS="
				+ AppUtil.AVAILABLE_PROCESSORS);
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
