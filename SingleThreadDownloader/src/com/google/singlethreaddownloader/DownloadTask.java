package com.google.singlethreaddownloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import android.os.Environment;

import com.google.singlethreaddownloader.DownloadResult.DownloadResultStatus;
import com.google.singlethreaddownloader.dao.SqlTaskDao;

public class DownloadTask implements Serializable, Callable<DownloadResult>,
		DownloadTaskListener {
	/**
	 * 序列化id
	 */
	private static final long serialVersionUID = -2679706700528032437L;
	private static final String TAG = "DownloadTask";
	/**
	 * 任务key
	 */
	public String key;
	/**
	 * 名称
	 */
	public String name;
	/**
	 * 完成比例
	 */
	public float percent;
	/**
	 * 开始位置
	 */
	public int startPosition;
	/**
	 * 结束位置
	 */
	public int endPosition;
	/**
	 * 完成大小
	 */
	public int downloadSize;
	/**
	 * 文件总大小
	 */
	public int length;
	/**
	 * 文件本地路径
	 */
	public String localPath;
	/**
	 * 任务状态
	 */
	public volatile Status status;
	/**
	 * 是否完成
	 */
	public boolean isFinished;
	/**
	 * 任务网络url
	 */
	public String downloadURL;
	/**
	 * 任务监听器队列
	 */
	private final List<DownloadTaskListener> mDownloadTaskListeners;
	/**
	 * 数据库业务类
	 */
	private final SqlTaskDao mTaskDao;

	public DownloadTask() {
		percent = 0.0f;
		status = Status.NOT_STARTED;
		mDownloadTaskListeners = new ArrayList<DownloadTaskListener>();
		mTaskDao = new SqlTaskDao(DownloadApp.getContext());
	}

	public void recover(DownloadTask task) {
		this.key = task.key;
		this.name = task.name;
		this.percent = task.percent;
		this.startPosition = task.startPosition;
		this.endPosition = task.endPosition;
		this.downloadSize = task.downloadSize;
		this.length = task.length;
		this.localPath = task.localPath;
		this.status = task.status;
		this.isFinished = task.isFinished;
		this.downloadURL = task.downloadURL;
	}

	public enum Status {
		NOT_STARTED, WAITING, RUNNING, PAUSING, PAUSED, STOPING, STOPED, CANCELED, FINISHED, REMOVED
	}

	@Override
	public String toString() {
		return "DownloadTask [key=" + key + ", name=" + name + ", percent="
				+ percent + ", startPosition=" + startPosition
				+ ", endPosition=" + endPosition + ", downloadSize="
				+ downloadSize + ", length=" + length + ", path=" + localPath
				+ ", status=" + status + ", isFinished=" + isFinished
				+ ", downloadURL=" + downloadURL + "]";
	}

	/**
	 * 发起HTTP请求，拿到文件总长度，创建本地文件，保存数据库
	 */
	private void init() {
		try {
			URL url = new URL(downloadURL);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			length = connection.getContentLength();
			File file = new File(localPath);
			if (!file.exists()) {
				file.createNewFile();
			}
			// 本地访问文件
			RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
			accessFile.setLength(length);
			accessFile.close();
			connection.disconnect();
			mTaskDao.update(this);
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
		// 设置变量判断是否是第一次加载
		init();

		URL url = new URL(downloadURL);
		File file = new File(localPath);
		if (!file.exists()) {
			System.out.println("file is not exists");
		}
		String dir = Environment.getExternalStorageDirectory() + "/file";
		new File(dir).mkdir();
		file.createNewFile();

		// 从数据库恢复数据
		// recover(mTaskDBService.getTask(key));

		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
		randomAccessFile.seek(startPosition + downloadSize);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		int contentLength = endPosition = length;
		conn.setRequestProperty("Range", "bytes="
				+ (startPosition + downloadSize) + "-" + endPosition);
		System.out.println("startPos+Complete:"
				+ (startPosition + downloadSize) + "#endPos:" + endPosition);
		InputStream input = conn.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		byte[] buffer = new byte[4096];
		int n = -1;
		try {
			while ((status == Status.RUNNING) && (downloadSize < length)
					&& !Thread.currentThread().isInterrupted()
					&& ((n = input.read(buffer)) != -1)) {
				randomAccessFile.write(buffer, 0, n);
				downloadSize += n;
				percent = downloadSize * 100.0f / contentLength;
				// System.out.println(downloadSize + "/" + contentLength
				// + "#status=" + status + "#interrupt="
				// + Thread.currentThread().isInterrupted());
				onTaskStatusChanged(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.status = DownloadResultStatus.ERROR;
			onTaskStatusChanged(this);
		} finally {
			try {
				randomAccessFile.close();
				in.close();
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// if (Thread.currentThread().isInterrupted()) {
			// System.out.println("worker thread was interrupted");
			// status=Status.PAUSING;
			// onTaskStatusChanged(this);
			// }

			if (downloadSize >= contentLength) {
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
}
