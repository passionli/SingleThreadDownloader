package com.google.singlethreaddownloader;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.singlethreaddownloader.DownloadTask.Status;

public class DownloadActivity extends Activity {
	protected static final String TAG = "DownloadActivity";
	private ListView mListView;
	private DownloadListAdapter mDownloadListAdapter;
	/**
	 * 所有任务
	 */
	private List<DownloadTask> mListItem;
	/**
	 * 等待队列
	 */
	private List<DownloadTask> mWaitingItem;
	/**
	 * 已完成队列
	 */
	private List<DownloadTask> mCompeletedItem;
	private DownloadManager mDownloadManager;

	private Executor mTemperaryExecutor;
	private static final int CMD_UPDATE_TASK = 1 << 1;
	private static final int CMD_UPDATE_LISTVIEW = CMD_UPDATE_TASK + 1;

	private final boolean isDownloadBackground = false;
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case CMD_UPDATE_TASK:
				int index = msg.arg1;
				DownloadTask task = mListItem.get(index);
				int firstVisible = mListView.getFirstVisiblePosition();
				int lastVisible = mListView.getLastVisiblePosition();
				if (index >= firstVisible && index <= lastVisible) {
					ViewHolder holder = (ViewHolder) (mListView
							.getChildAt(index - firstVisible).getTag());
					updateListViewItem(holder, task);
				}
				break;
			case CMD_UPDATE_LISTVIEW:
				mDownloadListAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
			// 避免全部刷新引起按钮响应很慢
			// mDownloadListAdapter.notifyDataSetChanged();
		};
	};

	public DownloadTaskListener mDownloadTaskListener = new DownloadTaskListener() {
		@Override
		public void onTaskStatusChanged(DownloadTask task) {
			// 状态改变保存数据库
			// mTaskDBService.save(task);
			int size = mListItem.size();
			for (int i = 0; i < size; i++) {
				DownloadTask oldTask = mListItem.get(i);
				if (oldTask.key.equals(task.key)) {
					mListItem.set(i, task);
					// 异步刷新界面
					mHandler.sendMessage(mHandler.obtainMessage(
							CMD_UPDATE_TASK, i, 0));
					break;
				}
			}
		}

		@Override
		public void onTaskFinished(DownloadTask task) {
			int size = mListItem.size();
			for (int i = 0; i < size; i++) {
				DownloadTask oldTask = mListItem.get(i);
				if (oldTask.key.equals(task.key)) {
					mListItem.set(i, task);
					// 异步刷新界面
					mHandler.sendMessage(mHandler.obtainMessage(
							CMD_UPDATE_TASK, i, 0));
					break;
				}
			}
		}
	};

	public View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

		}
	};

	@Override
	public void onConfigurationChanged(
			android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "new orientation " + newConfig.orientation);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDownloadManager.destroy();
		// close database when application is closed.
		// mTaskDBService.close();
		// mExecutor.shutdown();
		// try {
		// mExecutor.awaitTermination(0, TimeUnit.SECONDS);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "protected void onStop()");
		if (!isDownloadBackground) {
			mDownloadManager.pause();
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DownloadApp.setContext(getApplicationContext());
		setContentView(R.layout.activity_download);
		mListView = (ListView) findViewById(R.id.downloadListView);
		// mListItem = new ArrayList<DownloadTask>();
		mDownloadManager = DownloadManager.getInstance(getApplicationContext());
		String path = Environment.getExternalStorageDirectory().toString();
		// mTaskDBService = new TaskDBService(this);
		mListItem = mDownloadManager.getAllTasks();
		if (mListItem.size() == 0) {
			for (int i = 0; i < 30; i++) {
				DownloadTask task = new DownloadTask();
				// 取当前时刻作为key
				// task.key = String.valueOf(System.currentTimeMillis());
				task.name = "task " + i;
				// task.percent = 0;
				// task.status = Status.NOT_STARTED;
				task.downloadURL = "http://down.mumayi.com/1";
				task.localPath = path + "/file/" + UUID.randomUUID().toString()
						+ ".apk";
				mDownloadManager.put(task);
			}
		}
		mListItem = mDownloadManager.getAllTasks();
		// 添加监听器
		for (DownloadTask task : mListItem) {
			task.registeDownloadListener(mDownloadTaskListener);
		}
		mDownloadListAdapter = new DownloadListAdapter(this);
		mListView.setAdapter(mDownloadListAdapter);
		mTemperaryExecutor = Executors.newCachedThreadPool();
	}

	private void updateListViewItem(ViewHolder holder, DownloadTask task) {
		// Log.d(TAG, "updateListViewItem "+task);
		holder.title.setText(task.name);
		holder.progress.setProgress((int) task.percent);
		switch (task.status) {
		case NOT_STARTED:
			holder.status.setText("开始");
			break;
		case RUNNING:
			// holder.status.setText("下载中");
			holder.status.setText(String.format("%.2f", task.percent) + "%");
			break;
		case PAUSING:
			holder.status.setText("暂停");
			break;
		case WAITING:
			holder.status.setText("等待");
			break;
		case FINISHED:
			holder.status.setText("完成");
			break;
		case REMOVED:
			holder.status.setText("移除");
			break;
		default:
			break;
		}
	}

	private void removeTaskFromListView(DownloadTask task) {
		int size = mListItem.size();
		for (int i = 0; i < size; i++) {
			DownloadTask oldTask = mListItem.get(i);
			if (oldTask.key.equals(task.key)) {
				mListItem.remove(i);
				// 异步刷新界面
				mHandler.sendMessage(mHandler
						.obtainMessage(CMD_UPDATE_LISTVIEW));
				break;
			}
		}
	}

	public final class ViewHolder {
		TextView title;
		DownloadProgressBar progress;
		Button status;
	}

	public class DownloadListAdapter extends BaseAdapter {
		protected final String TAG = DownloadListAdapter.class.getSimpleName();
		Context context;
		private final LayoutInflater layoutInflater;

		public DownloadListAdapter(Context context) {
			this.context = context;
			layoutInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mListItem.size();
		}

		@Override
		public Object getItem(int position) {
			return mListItem.get(position);
		}

		@Override
		public long getItemId(int position) {
			return Long.valueOf(mListItem.get(position).key);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = layoutInflater.inflate(R.layout.list_item, null);
				holder.title = (TextView) convertView
						.findViewById(R.id.textView1);
				holder.progress = (DownloadProgressBar) convertView
						.findViewById(R.id.progressBar1);
				holder.status = (Button) convertView.findViewById(R.id.button1);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final DownloadTask task = mListItem.get(position);
			updateListViewItem(holder, task);
			holder.status.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// 把计算放在另一个线程，保持UI线程响应性
					mTemperaryExecutor.execute(new Runnable() {
						@Override
						public void run() {
							switch (task.status) {
							case NOT_STARTED:
							case PAUSING:
								mDownloadManager.start(task);
								break;
							case RUNNING:
							case WAITING:// 等待状态下也要取消提交到executor上的任务
								mDownloadManager.pause(task);
								break;
							case FINISHED:
								task.status = Status.REMOVED;
								break;
							case REMOVED:
								removeTaskFromListView(task);
								break;
							default:
								break;
							}
							// 把状态告诉监听者。不然在executor上等待时无法告知监听者
							task.notifyTaskStatusChanged(task);
						}
					});
				}
			});

			return convertView;
		}
	}
}
