package com.google.singlethreaddownloader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

public class DownloadProgressBar extends ProgressBar {
	private static final String TAG = "DownloadProgressBar";
	private String mText;
	private Paint mPaint;

	public DownloadProgressBar(Context context) {
		super(context);
		initPaint();
	}

	public DownloadProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPaint();
	}

	public DownloadProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}

	private void initPaint() {
		mPaint = new Paint();
		mPaint.setColor(Color.GRAY);
	}

	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		int percent = progress * 100 / getMax();
		mText = String.valueOf(percent) + "%";
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Rect rect = new Rect();
		this.mPaint.getTextBounds(mText, 0, mText.length(), rect);
		int x = (getWidth() / 2) - rect.centerX();
		int y = (getHeight() / 2) - rect.centerY();
		canvas.drawText(mText, x, y, this.mPaint);
	}
}
