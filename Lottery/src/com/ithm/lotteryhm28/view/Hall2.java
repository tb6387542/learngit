package com.ithm.lotteryhm28.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
public class Hall2 extends BaseUI {
	// 第一步：加载layout（布局参数设置）
	// 第二步：初始化layout中控件
	// 第三步：设置监听

	private ListView categoryList;// 彩种的入口
	private CategoryAdapter adapter;

	public Hall2(Context context) {
		super(context);
	}

	public void init() {
		showInMiddle = (LinearLayout) View.inflate(context, R.layout.il_hall2, null);

		categoryList = (ListView) findViewById(R.id.ii_hall_lottery_list);

		// needUpdate=new ArrayList<View>();

		adapter = new CategoryAdapter();
		categoryList.setAdapter(adapter);
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
				CommonInfoEngine engine = BeanFactory.getImpl(CommonInfoEngine.class);
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
						PromptManager.showToast(context, oelement.getErrormsg());
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

	private String text;

	// private List<View> needUpdate;

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
		text = context.getResources().getString(R.string.is_hall_common_summary);
		text = StringUtils.replaceEach(text, new String[] { "ISSUE", "TIME" }, new String[] { issue, lasttime });

		// TODO 更新界面
		// 方式一：
		// adapter.notifyDataSetChanged();// 所有的item更新

		// 方式二：更新需要更新内容（没有必要刷新所有的信息）
		// 获取到需要更新控件的应用
		// TextView view = (TextView) needUpdate.get(0);
		// view.setText(text);

		// 方式三：不想维护needUpdate，如何获取需要更新的控件的引用
		// 将所有的item添加到ListView ，是不是有方式可以获取到ListView的孩子
		// categoryList.findViewById(R.id.ii_hall_lottery_summary);

		// tag The tag to search for, using "tag.equals(getTag())".
		TextView view = (TextView) categoryList.findViewWithTag(0);// tag :唯一
		if (view != null) {
			view.setText(text);
		}

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

	@Override
	public void setListener() {

	}

	// 资源信息
	private int[] logoResIds = new int[] { R.drawable.id_ssq, R.drawable.id_3d, R.drawable.id_qlc };
	private int[] titleResIds = new int[] { R.string.is_hall_ssq_title, R.string.is_hall_3d_title, R.string.is_hall_qlc_title };

	private class CategoryAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();

				convertView = View.inflate(context, R.layout.il_hall_lottery_item, null);

				holder.logo = (ImageView) convertView.findViewById(R.id.ii_hall_lottery_logo);
				holder.title = (TextView) convertView.findViewById(R.id.ii_hall_lottery_title);
				holder.summary = (TextView) convertView.findViewById(R.id.ii_hall_lottery_summary);
				// needUpdate.add(holder.summary);
				holder.bet = (ImageView) convertView.findViewById(R.id.ii_hall_lottery_bet);

				// A tag can be used to mark a view in its hierarchy and does not have to be unique within the hierarchy.
				// Tags can also be used to store data within a view without resorting to another data structure.
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.logo.setImageResource(logoResIds[position]);
			holder.title.setText(titleResIds[position]);

			holder.summary.setTag(position);

			// if (StringUtils.isNotBlank(text) && position == 0) {
			// holder.summary.setText(text);
			// }
			return convertView;
		}

		// 依据item的layout
		class ViewHolder {
			ImageView logo;
			TextView title;
			TextView summary;
			ImageView bet;
		}

	}
}
