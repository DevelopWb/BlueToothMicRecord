package com.bluetoothmicrecord.upload.uploadUtil;

import android.util.Log;

import com.bluetoothmicrecord.upload.Updater;


/**
 *
 * 任务线程
 *
 * @author: hutuxiansheng
 */
public class TaskThread implements Runnable {

	/**
	 * 任务
	 */
	private Updater task;

	/**
	 * 构造函数
	 *
	 * @param updater
	 */
	public TaskThread(Updater updater) {
		task = updater;
	}

	/**
	 * 继承与Runnable
	 */
	@Override
	public void run() {
		// 执行任务
		try {
			task.StartUpdate();
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("Exception", "Exception : " + e);
		}

		//mthreadPool.afterExecute(this, null);
	}
}