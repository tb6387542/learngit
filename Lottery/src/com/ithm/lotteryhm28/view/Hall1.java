package com.ithm.lotteryhm28.view;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ithm.lotteryhm28.ConstantValue;
import com.ithm.lotteryhm28.R;
import com.ithm.lotteryhm28.engine.CommonInfoEngine;
import com.ithm.lotteryhm28.net.protocal.Element;
import com.ithm.lotteryhm28.net.protocal.Message;
import com.ithm.lotteryhm28.net.protocal.Oelement;
import com.ithm.lotteryhm28.net.protocal.element.CurrentIssueElement;
import com.ithm.lotteryhm28.util.BeanFactory;
import com.ithm.lotteryhm28.util.PromptManager;
import com.ithm.lotteryhm28.view.manager.BaseUI;

/**
 * 购彩大厅
 * 
 * @author Administrator
 * 
 */
public class Hall1 extends BaseUI {
	// 第一步：加载layout（布局参数设置）
	// 第二步：初始化layout中控件
	// 第三步：设置监听

	private TextView ssqIssue;
	private ImageView ssqBet;

	public Hall1(Context context) {
		super(context);
	}

	public void init() {
		showInMiddle = (LinearLayout) View.inflate(context, R.layout.il_hall1,
				null);
		ssqIssue = (TextView) findViewById(R.id.ii_hall_ssq_summary);
		ssqBet = (ImageView) findViewById(R.id.ii_hall_ssq_bet);

		// 只会调用一次
		// 每次进入购彩大厅界面，获取最新的数据——>已进入到某个界面，想去修改界面信息（存储）——>当进入到某个界面的时候，开启耗费资源的操作，当要离开界面，清理耗费资源的操作
		// 进入界面做些工作，出去的时候还需要完成一些工作
		// onResume（当界面被加载了：add（View））
		// onPause（当界面要被删除：removeAllView）——Activity抄了两个方法
		
		
		// 倒计时
		
		// getCurrentIssueInfo();
	}

	@Override
	public void onResume() {
		getCurrentIssueInfo();
		super.onResume();
	}

	@Override
	public int getID() {
		return ConstantValue.VIEW_HALL;
	}

	public void setListener() {
		ssqBet.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
	}

	/**
	 * 获取当前销售期信息(双色球)
	 */
	private void getCurrentIssueInfo() {

		// new MyThread().start();
		// new MyAsyncTask().execute(ConstantValue.SSQ);
		new MyHttpTask<Integer>() {

			@Override
			protected Message doInBackground(Integer... params) {
				// 获取数据——业务的调用
				CommonInfoEngine engine = BeanFactory
						.getImpl(CommonInfoEngine.class);
				return engine.getCurrentIssueInfo(params[0]);
			}

			@Override
			protected void onPostExecute(Message result) {
				// 更新界面
				if (result != null) {
					Oelement oelement = result.getBody().getOelement();

					if (ConstantValue.SUCCESS.equals(oelement.getErrorcode())) {
						changeNotice(result.getBody().getElements().get(0));
					} else {
						PromptManager
								.showToast(context, oelement.getErrormsg());
					}
				} else {
					// 可能：网络不通、权限、服务器出错、非法数据……
					// 如何提示用户
					PromptManager.showToast(context, "服务器忙，请稍后重试……");
				}

				super.onPostExecute(result);
			}
		}.executeProxy(ConstantValue.SSQ);

	}

	/**
	 * 修改界面提示信息
	 * 
	 * @param element
	 */
	protected void changeNotice(Element element) {
		CurrentIssueElement currentIssueElement = (CurrentIssueElement) element;
		String issue = currentIssueElement.getIssue();
		String lasttime = getLasttime(currentIssueElement.getLasttime());
		// 第ISSUE期 还有TIME停售
		String text = context.getResources().getString(
				R.string.is_hall_common_summary);
		text = StringUtils.replaceEach(text, new String[] { "ISSUE", "TIME" },
				new String[] { issue, lasttime });

		ssqIssue.setText(text);

	}

	/**
	 * 将秒时间转换成日时分格式
	 * 
	 * @param lasttime
	 * @return
	 */
	public String getLasttime(String lasttime) {
		StringBuffer result = new StringBuffer();
		if (StringUtils.isNumericSpace(lasttime)) {
			int time = Integer.parseInt(lasttime);
			int day = time / (24 * 60 * 60);
			result.append(day).append("天");
			if (day > 0) {
				time = time - day * 24 * 60 * 60;
			}
			int hour = time / 3600;
			result.append(hour).append("时");
			if (hour > 0) {
				time = time - hour * 60 * 60;
			}
			int minute = time / 60;
			result.append(minute).append("分");
		}
		return result.toString();
	}

	/**
	 * 异步访问网络的工具
	 * 
	 * @author Administrator Params：传输的参数 Progress：下载相关的，进度提示（Integer，Float）
	 *         Result：服务器回复数据的封装
	 */
	/*
	 * private class MyAsyncTask extends AsyncTask<Integer, Void, Message> {
	 *//**
	 * 同run方法，在子线程运行
	 */
	/*
	 * @Override protected Message doInBackground(Integer... params) { // TODO
	 * Auto-generated method stub return null; }
	 * 
	 * @Override protected void onPreExecute() { // TODO UI Thread before
	 * doInBackground // 显示滚动条 super.onPreExecute(); }
	 * 
	 * @Override protected void onPostExecute(Message result) { // TODO UI
	 * Thread after doInBackground // 修改界面提示信息 super.onPostExecute(result); }
	 *//**
	 * 类似与Thread.start方法 由于final修饰，无法Override，方法重命名 省略掉网络判断
	 * 
	 * @param params
	 * @return
	 */
	/*
	 * public final AsyncTask<Integer, Void, Message> executeProxy( Integer...
	 * params) { if (NetUtil.checkNet(context)) { return super.execute(params);
	 * } else { PromptManager.showNoNetWork(context); } return null; }
	 * 
	 * }
	 */

	/*
	 * private class MyThread extends Thread {
	 * 
	 * @Override public void run() { // TODO 访问网络获取数据 super.run(); }
	 * 
	 * @Override public synchronized void start() { if
	 * (NetUtil.checkNet(context)) { super.start(); } else {
	 * PromptManager.showNoNetWork(context); } } }
	 */

}
