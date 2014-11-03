package com.google.singlethreaddownloader;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

import com.google.singlethreaddownloader.DownloadTask.Status;
import com.google.singlethreaddownloader.dao.SqlTaskDao;
import com.google.singlethreaddownloader.dao.TaskDao;
import com.google.singlethreaddownloader.util.AppUtil;

public class DownloadManager {
	private static final String TAG = "DownloadManager";
	private static DownloadManager instance;
	private final Context mContext;
	/**
	 * 在内存中缓存任务
	 */
	private final ConcurrentHashMap<String, DownloadTask> mTasks;
	/**
	 * 数据库访问接口
	 */
	private final TaskDao mTaskDao;
	private final ExecutorService mExecutor;
	private final ConcurrentHashMap<String, Future<DownloadResult>> mFutures;
	private int nThreads = 1;
	public DownloadTaskListener mDownloadTaskListener = new DownloadTaskListener() {
		@Override
		public void onTaskStatusChanged(DownloadTask task) {
			// Log.d(TAG, "onTaskStatusChanged" + task);
			mTasks.put(task.key, task);
			// 实时刷新数据库任务进度会不会很影响效率？
			mTaskDao.update(task);
		}

		@Override
		public void onTaskFinished(DownloadTask task) {
			Log.d(TAG, "onTaskFinished " + task);
			// 及时清理引用
			//mFutures.remove(task.key);
			mTasks.put(task.key, task);
			// 状态改变保存数据库
			mTaskDao.update(task);
		}
	};

	private DownloadManager(Context context) {
		mContext = context;
		mTasks = new ConcurrentHashMap<String, DownloadTask>();
		mTaskDao = new SqlTaskDao(mContext);
		// 这个对开发者是透明的，减少复杂度
		nThreads = AppUtil.AVAILABLE_PROCESSORS;
		mExecutor = Executors.newFixedThreadPool(nThreads);
		mFutures = new ConcurrentHashMap<String, Future<DownloadResult>>();
	}

	public static synchronized DownloadManager getInstance(Context context) {
		if (instance == null) {
			instance = new DownloadManager(context);
		}

		return instance;
	}

	public String put(DownloadTask task) {
		task.key = String.valueOf(System.currentTimeMillis());
		// 添加到内存
		mTasks.put(task.key, task);
		// 添加到数据库
		mTaskDao.create(task);
		return task.key;
	}

	public DownloadTask get(String key) {
		// 先在内存中查找
		DownloadTask result = mTasks.get(key);
		if (result == null) {
			// 再到数据库中查找
			result = mTaskDao.getTask(key);
		}
		return result;
	}

	public List<DownloadTask> getAllTasks() {
		return mTaskDao.getAllTasks();
	}

	public void destroy() {
		mExecutor.shutdown();
		try {
			mExecutor.awaitTermination(0, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mTaskDao.close();
	}

	public void pause() {
		// for (; mFutures.size() > 0;) {
		// DownloadTask task = mListItem.get(i);
		// switch (task.status) {
		// case RUNNING:
		// case WAITING:
		// task.status = Status.PAUSING;
		// Future<DownloadResult> future = mFutures.get(task.key);
		// if (future != null) {
		// System.out.println("cancel result is "
		// + future.cancel(true));
		// } else {
		// System.out
		// .println("cancel future is null.Map containsKey is "
		// + mFutures.containsKey(task.key));
		// }
		//
		// // 把状态告诉监听者。不然在executor上等待时无法告知监听者
		// task.onTaskStatusChanged(task);
		// mTaskDao.update(task);
		// break;
		// default:
		// break;
		// }
		// }
	}

	public void start(DownloadTask task) {
		task.registeDownloadListener(mDownloadTaskListener);
		// 加入等待队列
		task.status = Status.WAITING;
		mFutures.put(task.key, mExecutor.submit(task));
		mTasks.put(task.key, task);
		mTaskDao.update(task);
	}

	public void pause(DownloadTask task) {
		task.status = Status.PAUSING;
		Future<DownloadResult> future = mFutures.get(task.key);
		boolean cancelResult = false;
		if (future != null) {
			cancelResult = future.cancel(true);
		}
		Log.d(TAG, cancelResult + " while canceling " + task);
		mFutures.remove(task.key);
		mTasks.put(task.key, task);
		mTaskDao.update(task);
	}
}
