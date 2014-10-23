package com.google.singlethreaddownloader;

public class DownloadResult {
	public enum DownloadResultStatus {
		OK, ERROR
	}

	/**
	 * 结果状态码
	 */
	public DownloadResultStatus status;
}
