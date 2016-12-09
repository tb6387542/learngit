package com.ithm.lotteryhm28.view.manager;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.ithm.lotteryhm28.ConstantValue;
import com.ithm.lotteryhm28.R;
import com.ithm.lotteryhm28.view.FirstUI;

/**
 * 中间容器的管理工具
 * 
 * @author Administrator
 * 
 */
public class MiddleManager1 {
	private static final String TAG = "MiddleManager";
	private static MiddleManager1 instance = new MiddleManager1();

	private MiddleManager1() {
	}

	public static MiddleManager1 getInstance() {
		return instance;
	}

	private RelativeLayout middle;

	public void setMiddle(RelativeLayout middle) {
		this.middle = middle;
	}

	// 利用手机内存空间，换应用应用的运行速度
	private Map<String, BaseUI> VIEWCACHE = new HashMap<String, BaseUI>();// K
	// :唯一的标示BaseUI的子类

	private BaseUI currentUI;// 当前正在展示

	private LinkedList<String> HISTORY = new LinkedList<String>();// 用户操作的历史记录

	/**
	 * 切换界面:解决问题“三个容器的联动”
	 * 
	 * @param ui
	 */
	public void changeUI(Class<? extends BaseUI> targetClazz) {
		// 判断：当前正在展示的界面和切换目标界面是否相同
		if (currentUI != null && currentUI.getClass() == targetClazz) {
			return;
		}

		BaseUI targetUI = null;
		// 一旦创建过，重用
		// 判断是否创建了——曾经创建过的界面需要存储
		String key = targetClazz.getSimpleName();
		if (VIEWCACHE.containsKey(key)) {
			// 创建了，重用
			targetUI = VIEWCACHE.get(key);
		} else {
			// 否则，创建
			try {
				Constructor<? extends BaseUI> constructor = targetClazz
						.getConstructor(Context.class);
				targetUI = constructor.newInstance(getContext());
				VIEWCACHE.put(key, targetUI);
			} catch (Exception e) {
				throw new RuntimeException("constructor new instance error");
			}
		}

		Log.i(TAG, targetUI.toString());

		// 切换界面的核心代码
		middle.removeAllViews();
		// FadeUtil.fadeOut(child1, 2000);
		View child = targetUI.getChild();
		middle.addView(child);
		child.startAnimation(AnimationUtils.loadAnimation(getContext(),
				R.anim.ia_view_change));
		// FadeUtil.fadeIn(child, 2000, 1000);

		currentUI = targetUI;

		// 将当前显示的界面放到栈顶
		HISTORY.addFirst(key);

		// 当中间容器切换成功时，处理另外的两个容器的变化
		changeTitleAndBottom();
	}

	private void changeTitleAndBottom() {
		// 1、界面一对应未登陆标题和通用导航
		// 2、界面二对应通用标题和玩法导航

		// 当前正在展示的如果是第一个界面
		// 方案一：存在问题，比对的依据：名称 或者 字节码
		// 在界面处理初期，将所有的界面名称确定
		// 如果是字节码，将所有的界面都的创建完成
		// if(currentUI.getClass()==FirstUI.class){
		// TitleManager.getInstance().showUnLoginTitle();
		// BottomManager.getInstrance().showCommonBottom();
		// }
		// if(currentUI.getClass().getSimpleName().equals("SecondUI")){
		// TitleManager.getInstance().showCommonTitle();
		// BottomManager.getInstrance().showGameBottom();
		// }

		// 方案二：更换比对依据

		switch (currentUI.getID()) {
		case ConstantValue.VIEW_FIRST:
			TitleManager.getInstance().showUnLoginTitle();
			BottomManager.getInstrance().showCommonBottom();
			// LeftManager\RightManager
			break;
		case ConstantValue.VIEW_SECOND:
			TitleManager.getInstance().showCommonTitle();
			BottomManager.getInstrance().showGameBottom();
			break;
		case 3:
			TitleManager.getInstance().showCommonTitle();
			BottomManager.getInstrance().showGameBottom();
			break;
		}
		
		// 降低三个容器的耦合度
		// 当中间容器变动的时候，中间容器“通知”其他的容器，你们该变动了，唯一的标示传递，其他容器依据唯一标示进行容器内容的切换
		// 通知：
		// 广播：多个应用
		// 为中间容器的变动增加了监听——观察者设计模式
		
		
		// ①将中间容器变成被观察的对象
		// ②标题和底部导航变成观察者
		// ③建立观察者和被观察者之间的关系（标题和底部导航添加到观察者的容器里面）
		// ④一旦中间容器变动，修改boolean，然后通知所有的观察者.updata()
	}

	/**
	 * 切换界面:解决问题“中间容器中，每次切换没有判断当前正在展示和需要切换的目标是不是同一个”
	 * 
	 * @param ui
	 */
	public void changeUI3(Class<? extends BaseUI> targetClazz) {
		// 判断：当前正在展示的界面和切换目标界面是否相同
		if (currentUI != null && currentUI.getClass() == targetClazz) {
			return;
		}

		BaseUI targetUI = null;
		// 一旦创建过，重用
		// 判断是否创建了——曾经创建过的界面需要存储
		String key = targetClazz.getSimpleName();
		if (VIEWCACHE.containsKey(key)) {
			// 创建了，重用
			targetUI = VIEWCACHE.get(key);
		} else {
			// 否则，创建
			try {
				Constructor<? extends BaseUI> constructor = targetClazz
						.getConstructor(Context.class);
				targetUI = constructor.newInstance(getContext());
				VIEWCACHE.put(key, targetUI);
			} catch (Exception e) {
				throw new RuntimeException("constructor new instance error");
			}
		}

		Log.i(TAG, targetUI.toString());

		// 切换界面的核心代码
		middle.removeAllViews();
		// FadeUtil.fadeOut(child1, 2000);
		View child = targetUI.getChild();
		middle.addView(child);
		child.startAnimation(AnimationUtils.loadAnimation(getContext(),
				R.anim.ia_view_change));
		// FadeUtil.fadeIn(child, 2000, 1000);

		currentUI = targetUI;

		// 将当前显示的界面放到栈顶
		HISTORY.addFirst(key);
	}

	/**
	 * 切换界面:解决问题“在标题容器中每次点击都在创建一个目标界面”
	 * 
	 * @param ui
	 */
	public void changeUI2(Class<? extends BaseUI> targetClazz) {
		BaseUI targetUI = null;
		// 一旦创建过，重用
		// 判断是否创建了——曾经创建过的界面需要存储
		String key = targetClazz.getSimpleName();
		if (VIEWCACHE.containsKey(key)) {
			// 创建了，重用
			targetUI = VIEWCACHE.get(key);
		} else {
			// 否则，创建
			try {
				Constructor<? extends BaseUI> constructor = targetClazz
						.getConstructor(Context.class);
				targetUI = constructor.newInstance(getContext());
				VIEWCACHE.put(key, targetUI);
			} catch (Exception e) {
				throw new RuntimeException("constructor new instance error");
			}
		}

		Log.i(TAG, targetUI.toString());

		// 切换界面的核心代码
		middle.removeAllViews();
		// FadeUtil.fadeOut(child1, 2000);
		View child = targetUI.getChild();
		middle.addView(child);
		child.startAnimation(AnimationUtils.loadAnimation(getContext(),
				R.anim.ia_view_change));
		// FadeUtil.fadeIn(child, 2000, 1000);
	}

	/**
	 * 切换界面
	 * 
	 * @param ui
	 */
	public void changeUI1(BaseUI ui) {
		// 切换界面的核心代码
		middle.removeAllViews();
		// FadeUtil.fadeOut(child1, 2000);
		View child = ui.getChild();
		middle.addView(child);
		child.startAnimation(AnimationUtils.loadAnimation(getContext(),
				R.anim.ia_view_change));
		// FadeUtil.fadeIn(child, 2000, 1000);
	}

	public Context getContext() {
		return middle.getContext();
	}

	/**
	 * 返回键处理
	 * 
	 * @return
	 */
	public boolean goBack() {
		// 记录一下用户操作历史
		// 频繁操作栈顶（添加）——在界面切换成功
		// 获取栈顶
		// 删除了栈顶
		// 有序集合
		if (HISTORY.size() > 0) {
			// 当用户误操作返回键（不退出应用）
			if (HISTORY.size() == 1) {
				return false;
			}

			// Throws:NoSuchElementException - if this LinkedList is empty.
			HISTORY.removeFirst();

			if (HISTORY.size() > 0) {
				// Throws:NoSuchElementException - if this LinkedList is empty.
				String key = HISTORY.getFirst();

				BaseUI targetUI = VIEWCACHE.get(key);

				middle.removeAllViews();
				middle.addView(targetUI.getChild());
				currentUI = targetUI;

				changeTitleAndBottom();

				return true;

			}
		}

		return false;
	}
}
