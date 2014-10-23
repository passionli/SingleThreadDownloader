package com.google.singlethreaddownloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import android.R.integer;
import android.os.Environment;
import android.util.Log;

import com.google.singlethreaddownloader.DownloadResult.DownloadResultStatus;

public class DownloadTask implements Serializable, Callable<DownloadResult>,
		DownloadTaskListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2679706700528032437L;
	private static final String TAG = "DownloadTask";
	public String id;
	public String name;
	public float percent;
	public int startPosition;
	public int endPosition;
	public int completeSize;
	public int fileSize;
	public String localFile;
	public Status status;
	private boolean isFinished;
	public String spec;
	private List<DownloadTaskListener> mDownloadTaskListeners;

	public DownloadTask() {
		// spec = "http://down.mumayi.com/25822";
		spec = "http://down.mumayi.com/1";
		localFile = Environment.getExternalStorageDirectory() + "/file/"
				+ UUID.randomUUID().toString() + ".apk";
		mDownloadTaskListeners = new ArrayList<DownloadTaskListener>();
	}

	public enum Status {
		NOT_STARTED, WAITING, RUNNING, PAUSING, PAUSED, STOPING, STOPED, CANCELED, FINISHED, REMOVED
	}

	@Override
	public String toString() {
		return "DownloadTask [id=" + id + ", name=" + name + ", percent="
				+ percent + ", status=" + status + ", isFinished=" + isFinished
				+ "]";
	}

	private void init() {
		try {
			URL url = new URL(spec);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			fileSize = connection.getContentLength();
			File file = new File(localFile);
			if (!file.exists()) {
				file.createNewFile();
			}
			// 本地访问文件
			RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
			accessFile.setLength(fileSize);
			accessFile.close();
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public DownloadResult call() throws Exception {
		switch (status) {
		case NOT_STARTED:
			break;
		case RUNNING:
			break;
		case WAITING:
			status = Status.RUNNING;
			break;
		case PAUSING:
			break;
		default:
			break;
		}
		DownloadResult result = new DownloadResult();
		result.status = DownloadResultStatus.OK;
		init();

		URL url = new URL(spec);

		File file = new File(localFile);
		if (!file.exists()) {
			System.out.println("file is not exists");
		}
		String dir = Environment.getExternalStorageDirectory() + "/file";
		new File(dir).mkdir();
		file.createNewFile();
		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
		randomAccessFile.seek(startPosition + completeSize);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		int contentLength = endPosition = fileSize;
		conn.setRequestProperty("Range", "bytes="
				+ (startPosition + completeSize) + "-" + endPosition);
		System.out.println("startPos+Complete:"
				+ (startPosition + completeSize) + "#endPos:" + endPosition);
		// float sum = completeSize;
		InputStream input = conn.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		// OutputStream output = new FileOutputStream(file);
		byte[] buffer = new byte[4096];
		int n = -1;
		try {
			while (!Thread.currentThread().isInterrupted()
					&& ((n = input.read(buffer)) != -1)) {
				randomAccessFile.write(buffer, 0, n);
				// sum += n;
				// completeSize = (int) sum;
				completeSize += n;
				// startPosition=completeSize;
				percent = completeSize * 100.0f / contentLength;
				System.out.println(completeSize + "/" + contentLength);
				onTaskStatusChanged(this);
			}
			// output.flush();
		} catch (Exception e) {
			e.printStackTrace();
			result.status = DownloadResultStatus.ERROR;
			onTaskStatusChanged(this);
		} finally {
			try {
				// output.close();
				randomAccessFile.close();
				in.close();
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (completeSize >= contentLength) {
				percent = 100;
				status = Status.FINISHED;
				isFinished = true;
			}

			if (isFinished) {
				onTaskFinished(this);
			}
		}

		return result;
	}

	@Override
	public void onTaskStatusChanged(DownloadTask task) {
		for (DownloadTaskListener downloadTaskListener : mDownloadTaskListeners) {
			downloadTaskListener.onTaskStatusChanged(task);
		}
	}

	public void registeDownloadListener(DownloadTaskListener listener) {
		this.mDownloadTaskListeners.add(listener);
	}

	public void unregisteDownloadListener(DownloadTaskListener listener) {
		this.mDownloadTaskListeners.remove(listener);
	}

	@Override
	public void onTaskFinished(DownloadTask task) {
		for (DownloadTaskListener downloadTaskListener : mDownloadTaskListeners) {
			downloadTaskListener.onTaskFinished(task);
		}
	}

	// public void cancel() {
	// System.out.println("cancel");
	// isFinished = true;
	// onTaskReceive(this);
	// Thread.currentThread().interrupt();
	// }
}
