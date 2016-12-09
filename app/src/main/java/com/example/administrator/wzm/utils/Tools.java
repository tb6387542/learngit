package com.example.administrator.wzm.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;

import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author xuhui.han
 * 工具类
 */
@SuppressLint("SimpleDateFormat")
public class Tools {

	/**
	 * 根据手机的分辨率把 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 将px值转换为sp值，保证文字大小不变
	 *
	 * @param pxValue
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 *
	 * @param spValue
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	/**
	 * @param activity
	 * @return 屏幕宽度
	 */
	public final static int getWindowsWidth(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	/**
	 * @param activity
	 * @return 屏幕高度
	 */
	public final static int getWindowsHeight(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	/**
	 * @param list
	 *            当前集合
	 * @param pageSize
	 *            每页显示数据条数
	 * @return 下一页的索引值
	 * @author xuhui.han
	 */
	public static <T> int getRecordNextPageIndex(List<T> list, int pageSize) {
		if (list == null || list.size() == 0) {
			return 1;
		} else {
			return (list.size() - 1) / pageSize + 2;
		}
	}

	/**
	 * @param createtime
	 *            传入的时间字符串
	 * @return 当前时间距传入的时间间隔
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getTimeInterval(String createtime) { // 传入的时间格式必须类似于2012-8-21 17:53:20这样的格式
		// 如果传入的时间字符串为空，返回值也为空
		if (TextUtils.isEmpty(createtime)) {
			return "";
		}
		String interval = null;
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date d1 = (Date) sd.parse(createtime, pos);
		// 用现在距离1970年的时间间隔new
		// Date().getTime()减去以前的时间距离1970年的时间间隔d1.getTime()得出的就是以前的时间与现在时间的时间间隔
		long time = new Date().getTime() - d1.getTime();// 得出的时间间隔是毫秒
		if (time / 1000 < 10 && time / 1000 >= 0) {
			// 如果时间间隔小于10秒则显示“刚刚”time/10得出的时间间隔的单位是秒
			interval = "刚刚";
		} else if (time / 3600000 > 0 && time / 3600000 < 24) {
			// 如果时间间隔小于24小时则显示多少小时前
			int h = (int) (time / 3600000);// 得出的时间间隔的单位是小时
			interval = h + "小时前";
		} else if (time / 60000 > 0 && time / 60000 < 60) {
			// 如果时间间隔小于60分钟则显示多少分钟前
			int m = (int) ((time % 3600000) / 60000);// 得出的时间间隔的单位是分钟
			interval = m + "分钟前";
		} else if (time / 1000 > 0 && time / 1000 < 60) {
			// 如果时间间隔小于60秒则显示多少秒前
			int se = (int) ((time % 60000) / 1000);
			interval = se + "秒前";
		} else {
			// 大于24小时，则显示正常的时间，但是不显示秒
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
			ParsePosition pos2 = new ParsePosition(0);
			Date d2 = (Date) sdf.parse(createtime, pos2);
			interval = sdf.format(d2);
		}
		return interval;
	}

	/**
	 * @param createtime
	 *            传入的时间字符串
	 * @return 返回上次刷新的时间
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getTime(String createtime) { // 传入的时间格式必须类似于2012-8-21 17:53:20这样的格式
		// 如果传入的时间字符串为空，返回值也为空
		if (TextUtils.isEmpty(createtime)) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = (Date) sdf.parse(createtime, new ParsePosition(0));
		// 用现在距离1970年的时间间隔new
		// Date().getTime()减去以前的时间距离1970年的时间间隔d1.getTime()得出的就是以前的时间与现在时间的时间间隔
		long time = new Date().getTime() - date.getTime();// 得出的时间间隔是毫秒
		if (time / 3600000 >= 24) {
			// 如果时间间隔大于等于24小时则显示某年某月某日谋时某分
			return new SimpleDateFormat("yyyy-MM-dd").format(date);
		} else {
			// 小于24小时，则显示某时某分
			return new SimpleDateFormat("HH:mm").format(date);
		}
	}

	/**
	 * @param time	必须是	YYYY-mm-dd HH:MM:ss	的格式
	 * @return 得到距离下次整点的毫秒值(即12点)
	 *
	 */
	public static long getNextCountDown(String time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		long now = 0;
		try {
			if(TextUtils.isEmpty(time)){
				now = calendar.getTime().getTime();
			}else {
				Date currentTime = sdf.parse(time);
				now = currentTime.getTime();
				//设置日历时间为服务器日历时间，这个必须做
				calendar.setTime(currentTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long next = calendar.getTime().getTime();	//下一个整点的时间
		long interval = next - now;
		interval = interval > (3600000 * 12) ? (interval - (3600000 * 12)) : interval;
		return interval;
	}

	/**
	 * @param endTime	必须是	YYYY-mm-dd HH:MM:ss	的格式
	 * @return 得到开始时间距离结束时间的时间值
	 *
	 */
	public static long getInterval(String startTime, String endTime){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long now = 0;
		long end = 0;
		try {
			end = sdf.parse(endTime).getTime();
			now = sdf.parse(startTime).getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long interval = end - now;
		return interval;
	}

	public static String getInterval(long startTime, String endTime){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long end = 0;
		try {
			end = sdf.parse(endTime).getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long interval = end - startTime;
		if(interval <= 0){
			return "活动已结束";
		}
		long day = interval / (24 * 60 * 60 * 1000);
		long hour = (interval % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
		long minute = ((interval % (24 * 60 * 60 * 1000)) % (60 * 60 * 1000)) / (60 * 1000);// 得到分钟
		long second = (((interval % (24 * 60 * 60 * 1000)) % (60 * 60 * 1000)) % (60 * 1000)) / 1000;// 得到分钟
		//		Spanned text = Html.fromHtml("剩余" + "<font color=#ED1C24>"+ (day > 10 ? day : "0"+day) +"</font>" + "天"
		//				+ "<font color=#ED1C24>" + hour + "</font>" + "小时"
		//			    + "<font color=#ED1C24>" + minute + "</font>" + "分钟"
		//			    + "<font color=#ED1C24>" + second + "</font>" + "秒");
		return day + "天" + (hour >= 10 ? hour : "0"+hour) + ":" + (minute >= 10 ? minute : "0"+minute) + ":" + (second >= 10 ? second : "0"+second);
	}

	/**
	 * @param time	必须是	YYYY-mm-dd HH:MM:ss	的格式
	 * @return 得到开始时间距离结束时间的时间值
	 *
	 */
	public static long getDateTime(String time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return sdf.parse(time).getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	public static String getDateTimeStr(long interval){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return sdf.format(new Date(interval));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}



	public static String getFileSavePath(Context context){
		String path;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			//有SD卡
			path = Environment.getExternalStorageDirectory()
					+ File.separator
					+ "ImageCache"
					+ File.separator
					+ "GaoxiaoMall"
					+ File.separator;
			File file = new File(path);
			if(!file.exists()){
				file.mkdirs();
			}
		} else {
			//无SD卡
			path = context.getFilesDir().getAbsolutePath() + File.separator;
		}
		return path;
	}

	public static PopupWindow showPopuWindow(Activity context,View view,OnDismissListener dismissListener) {
		// 创建弹出框,设置宽度高度
		PopupWindow popupWindow = new PopupWindow(view,LayoutParams.MATCH_PARENT,getWindowsHeight(context)*2/3,true);
		// 为popupwindow设置背景颜色(透明色)，这样才能播放动画
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setOutsideTouchable(true);
		popupWindow.showAtLocation(context.getCurrentFocus(),Gravity.CENTER, 0,getWindowsHeight(context));
		//popupWindow.showAsDropDown(filterCb);
//		popupWindow.setAnimationStyle(R.style.listviewFooterPopupAnimation);//设置动画样式
		popupWindow.update();
		popupWindow.setOnDismissListener(dismissListener);
		return popupWindow;
	}

	/**
	 * @param bm
	 * @param fileName
	 * @return
	 * 保存Bitmap到本地的方法
	 */
//	public static File saveBitmap(Bitmap bm , String fileName) {
//		//检查目录是否存在
//		File file = new File(Constants.SAVED_IMAGE_DIR_PATH);
//		if(!file.exists()){
//			file.mkdirs();
//		}
//		//检查文件是否存在
//		File shareImageFile = new File(Constants.SAVED_IMAGE_DIR_PATH, fileName);
//		if (shareImageFile.exists()) {
//			shareImageFile.delete();
//		}
//		try {
//			FileOutputStream out = new FileOutputStream(shareImageFile);
//			bm.compress(Bitmap.CompressFormat.PNG, 90, out);
//			out.flush();
//			out.close();
//		}catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//		return shareImageFile;
//	}

	/**
	 * @param convertView
	 * @param id
	 * @return
	 * ViewHolder的极简用法，将ViewHolder隐于无形
	 *
	 * 用法
	 * if (convertView == null) {
	 *      convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_feed_item, parent, false);
	 * }
	 * ImageView thumnailView = getAdapterView(convertView, R.id.video_thumbnail);
	 * ImageView avatarView =  getAdapterView(convertView, R.id.user_avatar);
	 * ImageView appIconView = getAdapterView(convertView, R.id.app_icon);
	 *
	 *
	 */
	@SuppressWarnings("unchecked")
	public static <T extends View> T getAdapterView(View convertView, int id) {
		SparseArray<View> viewHolder = (SparseArray<View>) convertView.getTag();
		if (viewHolder == null) {
			viewHolder = new SparseArray<View>();
			convertView.setTag(viewHolder);
		}
		View childView = viewHolder.get(id);
		if (childView == null) {
			childView = convertView.findViewById(id);
			viewHolder.put(id, childView);
		}
		return (T) childView;
	}
	private static long mExitTime;
	public static boolean doubleClickExit(Activity activity,int keyCode){
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {// 如果两次按键时间间隔大于2000毫秒，则不退出
				Toast.makeText(activity, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();// 更新mExitTime
			} else {
				System.exit(0);// 否则退出程序
			}
			return true;
		}
		return false;
	}
	/**
	 * 得到当前软件的版本名称
	 *
	 * @return
	 */
	public static String getVersion(Activity activity) {
		// 包管理器
		PackageManager pm = activity.getPackageManager();
		// 功能清单文件
		try {
			PackageInfo packageInfo = pm.getPackageInfo(activity.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}
	private Toast toast;
	/**
	 * 短时间显示Toast 作用:不重复弹出Toast,如果当前有toast正在显示，则先取消
	 *
	 * @param info
	 *            显示的内容
	 */
	public void showToast(Activity activity,String info) {
		if (toast != null) {
			toast.cancel();
		}
		toast = Toast.makeText(activity, info, Toast.LENGTH_SHORT);
		toast.setText(info);
		toast.show();
	}
}
