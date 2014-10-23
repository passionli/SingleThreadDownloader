package com.google.singlethreaddownloader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.singlethreaddownloader.DownloadResult.DownloadResultStatus;

public class DownloadTask implements Serializable, Callable<DownloadResult>,
		DownloadTaskListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2679706700528032437L;
	public String id;
	public String name;
	public int percent;
	public Status status;
	private boolean isFinished;
	private List<DownloadTaskListener> mDownloadTaskListeners;

	public DownloadTask() {
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

	@Override
	public DownloadResult call() throws Exception {
		DownloadResult result = new DownloadResult();
		result.status = DownloadResultStatus.OK;
		while (!Thread.currentThread().isInterrupted()) {
			switch (status) {
			case NOT_STARTED:
				break;
			case RUNNING:
				percent += 1;
				break;
			case WAITING:
				status = Status.RUNNING;
				break;
			case PAUSING:
				break;
			default:
				break;
			}

			try {
				if (percent >= 100) {
					percent = 100;
					status = Status.FINISHED;
					isFinished = true;
				}
				onTaskStatusChanged(this);
				if (isFinished) {
					onTaskFinished(this);
					break;
				} else {
					Thread.sleep(50);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				result.status = DownloadResultStatus.ERROR;
				// paused
				onTaskStatusChanged(this);
				Thread.currentThread().interrupt();
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
