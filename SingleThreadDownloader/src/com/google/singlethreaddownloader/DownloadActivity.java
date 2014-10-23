package com.google.singlethreaddownloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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

	private ExecutorService mExecutor;
	private HashMap<String, Future<DownloadResult>> mFutures;
	private final int THREADS = 3;

	private Executor mTemperaryExecutor;
	private static final int CMD_UPDATE_TASK = 1 << 1;
	private static final int CMD_UPDATE_LISTVIEW = CMD_UPDATE_TASK + 1;

	private Handler mHandler = new Handler() {
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
			int size = mListItem.size();
			for (int i = 0; i < size; i++) {
				DownloadTask oldTask = mListItem.get(i);
				if (oldTask.id.equals(task.id)) {
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
			// 及时清理引用
			mFutures.remove(task.id);
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
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		mListView = (ListView) findViewById(R.id.downloadListView);
		mListItem = new ArrayList<DownloadTask>();
		for (int i = 0; i < 30; i++) {
			DownloadTask task = new DownloadTask();
			task.id = String.valueOf(i);
			task.name = "task " + i;
			task.percent = 0;
			task.status = Status.NOT_STARTED;
			task.registeDownloadListener(mDownloadTaskListener);
			mListItem.add(task);
		}
		mDownloadListAdapter = new DownloadListAdapter(this);
		mListView.setAdapter(mDownloadListAdapter);

		mExecutor = Executors.newFixedThreadPool(THREADS);
		mFutures = new HashMap<String, Future<DownloadResult>>();
		mTemperaryExecutor = Executors.newCachedThreadPool();
	}

	private void updateListViewItem(ViewHolder holder, DownloadTask task) {
		// Log.d(TAG, "updateListViewItem "+task);
		holder.title.setText(task.name);
		holder.progress.setProgress(task.percent);
		switch (task.status) {
		case NOT_STARTED:
			holder.status.setText("开始");
			break;
		case RUNNING:
			// holder.status.setText("下载中");
			holder.status.setText(String.valueOf(task.percent*100/100)+"%");
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
			if (oldTask.id.equals(task.id)) {
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
		private LayoutInflater layoutInflater;

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
			return Long.valueOf(mListItem.get(position).id);
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
								// 加入等待队列
								task.status = Status.WAITING;
								mFutures.put(task.id, mExecutor.submit(task));
								// 把状态告诉监听者。不然在executor上等待时无法告知监听者
								task.onTaskStatusChanged(task);
								break;
							case RUNNING:
							case WAITING:// 等待状态下也要取消提交到executor上的任务
								task.status = Status.PAUSING;
								Future<DownloadResult> future = mFutures
										.get(task.id);
								future.cancel(true);

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
							task.onTaskStatusChanged(task);
						}
					});
				}
			});

			return convertView;
		}
	}
}
