package com.example.administrator.wzm.utils;

import android.util.Log;

/**
 * @author xuhui.han
 * 日志工具类
 */
public class LogUtil {
	
	public static boolean DEBUG_MODE = true;
	public static String getPrefix() {
		// Show thread info if in debugging mode
		if (DEBUG_MODE)
			return "[" + Thread.currentThread().getName() + "-"
					+ Thread.currentThread().getId() + "] ";
		else
			return "";
	}

	public static void e(String tag, String message) {
		if (DEBUG_MODE){
			Log.e(tag, getPrefix() + message);
		}
	}

	public static void e(String tag, String message, Exception e) {
		if (DEBUG_MODE){
			Log.e(tag, getPrefix() + message, e);
		}
	}

	public static void w(String tag, String message) {
		if (DEBUG_MODE){
			Log.w(tag, getPrefix() + message);
		}
	}

	public static void i(String tag, String message) {
		if (DEBUG_MODE){
			Log.i(tag, getPrefix() + message);
		}
	}

	public static void d(String tag, String message) {
		if (DEBUG_MODE){
			Log.d(tag, getPrefix() + message);
		}
	}

	public static void v(String tag, String message) {
		if (DEBUG_MODE){
			Log.v(tag, getPrefix() + message);
		}
	}
}
