package com.example.administrator.wzm.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("SimpleDateFormat") 
public class DateUtil {
	
	public static final DateFormat STANDARD_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 获取当前时间的标准化时间
	 * @return
	 */
	public static String curDateFormatStandard(){
		return STANDARD_FORMAT.format(getCurDate());
	}
	
	/**
	 * 获取当前时间
	 * @return
	 */
	public static Date getCurDate(){
		return getCalendar().getTime();
	}
	
	/**
	 * 获取当前时间
	 * @return Calendar
	 */
	public static Calendar getCalendar(){
		return Calendar.getInstance();
	}

}
