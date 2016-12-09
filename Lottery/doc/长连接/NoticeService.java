package cn.itcast.lottery.service;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParser;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.util.Xml;
import cn.itcast.lottery.CommonParams;
import cn.itcast.lottery.R;
import cn.itcast.lottery.activity.MainActivity;
import cn.itcast.lottery.bean.PO.ZCLotteryPO;
import cn.itcast.lottery.bean.VO.LongConnectionInfo;
import cn.itcast.lottery.bean.VO.ZCLotteryVO;
import cn.itcast.lottery.bean.xml.Body;
import cn.itcast.lottery.dao.ZCLotteryDao;
import cn.itcast.lottery.util.BeanFactory;
import cn.itcast.lottery.util.ConstantValue;
import cn.itcast.lottery.util.HttpTask;
import cn.itcast.lottery.util.MException;
import cn.itcast.lottery.util.MessageFactory;
import cn.itcast.lottery.util.MessageQueueManager;
import cn.itcast.lottery.util.NetUtil;
import cn.itcast.lottery.util.platform.HttpClientAdapter;
import cn.itcast.lottery.util.platform.SocketAdapter;

public class NoticeService extends Service {
	private static final String TAG = "NoticeService";
	public static boolean isOpen = false;
	private AlarmManager alarmManager;
	private SocketAdapter socket;
	private String currentIssue;

	private ZCLotteryDao impl;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		impl = BeanFactory.getImpl(ZCLotteryDao.class, NoticeService.this);
		isOpen = true;
		int flag = intent.getIntExtra(ConstantValue.NOTICE_FLAG, 0);

		// 获取当前socket连接,判断是否是连接状态
		boolean isConnected = checkSocket();
		if (isConnected == true) {
			switch (flag) {
				case ConstantValue.NOTICE_CHANGE_NET:// 网络变化
					socket.setSendSwitch(false);
					socket.close();
					Log.i(TAG, "network change.");
					reConnected(false);
					break;
				case ConstantValue.NOTICE_CHANGE_SETTING:// 设置变化
					SharedPreferences sharedPreferences = getSharedPreferences(ConstantValue.SETTING_NAME, MODE_PRIVATE);
					boolean lotterySet = sharedPreferences.getBoolean(CommonParams.LOTTERY_SET_KEY, false);
					if (lotterySet == false) {
						socket.setSendSwitch(false);
						socket.close();
					}
					break;
				case ConstantValue.NOTICE_CHANGE_CLOSE:

					break;

				default:
					break;
			}
		}
		isConnected = checkSocket();
		if (isConnected == false) {
			// 检查当前配置文件中开奖功能设置
			SharedPreferences sharedPreferences = getSharedPreferences(ConstantValue.SETTING_NAME, MODE_PRIVATE);
			boolean lotterySet = sharedPreferences.getBoolean(CommonParams.LOTTERY_SET_KEY, true);
			if (lotterySet == true) {
				boolean openConnection = openConnection();
				if (openConnection) {
					writeInfo();
					sendActive();
					openReceive();
				}
				// sendSystemNotice();
			}

		}

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 开启接收线程
	 */
	private void openReceive() {
		Runnable receiveRunnable = new Runnable() {

			@Override
			public void run() {
				InputStream inputStream = null;
				try {
					// int tryTime = 0;
					Log.i(TAG, "start receive info.");
					socket = SocketAdapter.getSocketAdapterInstance();
					inputStream = socket.openInputStream();

					while (socket.isSendSwitch()) {
						StringBuffer buffer = new StringBuffer("");

						byte[] b = new byte[1024];
						int len = 0;
						len = inputStream.read(b);
						buffer.append(new String(b, 0, len));

						String packageInfo = buffer.toString();
						String[] split = StringUtils.split(packageInfo, LongConnectionInfo.RECEIVE_FLOG);

						for (int i = 0; i < split.length; i++) {
							final String command = split[i];
							MessageQueueManager.receivePoolExecutorService.execute(new Runnable() {

								@Override
								public void run() {
									handleReceiveInfo(command);
								}
							});
						}

						// final byte[] head = new byte[LongConnectionInfo.COMMAND_FLOG_LEN];
						// final byte[] body;
						//
						// int headReadLen = 0;
						// int bodyReadLen = 0;
						//
						// int headTemp = 0;
						// int bodyTemp = 0;
						// while (headReadLen < LongConnectionInfo.COMMAND_FLOG_LEN) {
						// headTemp = inputStream.read(head, headReadLen, LongConnectionInfo.COMMAND_FLOG_LEN);
						// if (headTemp != -1) {
						// headReadLen += headTemp;
						// } else {
						// tryTime++;
						// Thread.sleep(5 * 1000);
						// }
						// if (tryTime == 3) {
						// throw new MException(MException.WRONG_SYNCINFO);
						// }
						// }
						//						
						//						
						//						
						// int n = 0;
						// String flog = "";
						// flog = new String(head);
						//						
						// // if (flog.equals(LongConnectionInfo.SEND_FLOG)) {
						// // n = LongConnectionInfo.RECEIVE_COMMAND_LEN;
						// // }
						// // if (flog.equals(LongConnectionInfo.RECEIVE_FLOG)) {
						// // n = LongConnectionInfo.SEND_COMMAND_LEN;
						// // }
						//
						// body = new byte[n];
						// while (bodyReadLen < n) {
						// bodyTemp = inputStream.read(body, bodyReadLen, n - bodyReadLen);
						// if (bodyTemp != -1)
						// bodyReadLen += bodyTemp;
						// }
						// MessageQueueManager.receivePoolExecutorService.execute(new Runnable() {
						//
						// @Override
						// public void run() {
						// handleReceiveInfo(ArrayUtils.addAll(head, body));
						// }
						// });
					}

				} catch (Exception e) {
					if (e instanceof MException) {
						if (((MException) e).getCode() == MException.WRONG_SYNCINFO) {
							e.printStackTrace();
							Log.i(TAG, "receive info error");
							// 重新连接
							startReConnected();
						}
					}
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		new Thread(receiveRunnable).start();
	}

	/**
	 * 处理接收到的数据
	 * 
	 * @param addAll
	 */
	protected void handleReceiveInfo(String msg) {
		Log.i(TAG, "receive info :" + msg);

		// 处理服务器下发的指令
		/**
		 * 开奖号码（双色球、3D、七乐彩）通知 请求数据:双色球为1，3D为2，七乐彩为3，内蒙时时彩为4.. >1^3 已经派奖消息通知（双色球、3D、七乐彩） 请求数据: 双色球为1，3D为2，七乐彩为3.. >2^1
		 */

		if (msg.contains(LongConnectionInfo.NOTICE_DELIMITER)) {
			String[] split = StringUtils.split(msg, LongConnectionInfo.NOTICE_DELIMITER);
			int type = 0;
			int mimeType = 0;
			try {
				type = Integer.parseInt(split[0]);
				mimeType = Integer.parseInt(split[1]);
			} catch (Exception e) {
			}

			if (type != 0 && mimeType != 0) {
				String lotteryId = "";
				switch (type) {
					case LongConnectionInfo.NOTICE_LOTTERY:
						lotteryId = ZCLotteryVO.lotteryNoticeMap.get(mimeType);
						if (StringUtils.isNotBlank(lotteryId)) {
							try {
								getBonuscodeMessage(lotteryId);
								sendReceiveInfo(LongConnectionInfo.RECEIVE_SUCCESS);
							} catch (Exception e) {
								sendReceiveInfo(LongConnectionInfo.RECEIVE_UNKNOW_ERROR);
							}
						}
						break;
					case LongConnectionInfo.NOTICE_LOTTERY_WIN:
						lotteryId = ZCLotteryVO.lotteryNoticeMap.get(mimeType);
						if (StringUtils.isNotBlank(lotteryId)) {
							try {
								currentIssue = "";
								getBonuscodeMessage(lotteryId);
								MessageFactory factory = MessageFactory.getInstance();
								Body body = new Body();
								body.optype = "1003";// 中奖记录
								body.pageindex = "1";// 开始页码
								body.pagesize = "10";// 页尺寸

								String message = factory.createMessage(HttpTask.USER_TRANSACTION_DETAILS + "", "13300000000", factory.createGetUserTransactionDetails(body), false);
								body = HttpTask.sendInfo(ConstantValue.URL_LOTTERY, message, HttpTask.USER_TRANSACTION_DETAILS);

								// TODO 将body中信息与currentIssue进行比对
								sendReceiveInfo(LongConnectionInfo.RECEIVE_SUCCESS);
							} catch (Exception e) {
								sendReceiveInfo(LongConnectionInfo.RECEIVE_UNKNOW_ERROR);
							}
						}
						break;
				}
			}
		} else {
			int type = 0;
			try {
				type = Integer.parseInt(msg);
			} catch (Exception e) {
			}
			if (type != 0) {
				switch (type) {
					case LongConnectionInfo.NOTICE_PROMOTION:
						sendReceiveInfo(LongConnectionInfo.RECEIVE_SUCCESS);
						break;
					case LongConnectionInfo.NOTICE_EMERGENCY:
						sendReceiveInfo(LongConnectionInfo.RECEIVE_SUCCESS);
						break;
				}
			} else {
				// 处理心跳指令 <1000

				// 心跳计数清空
				MessageQueueManager.initCountPool();
			}
		}

	}

	/**
	 * 重新建立连接
	 * 
	 * @param isDelay
	 */
	private void reConnected(boolean isDelay) {
		Log.i(TAG, "reconnected...");
		if (socket != null) {
			socket.setSendSwitch(false);
			socket.close();
		}
		if (isDelay) {
			try {
				Thread.sleep(ConstantValue.DELAY);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		boolean openConnection = openConnection();
		if (openConnection == true) {
			writeInfo();
			sendActive();
			openReceive();
		} else {
			Log.i(TAG, "open socket failure.");
			reConnected(true);
		}
	}

	/**
	 * 获取当前socket连接,判断是否是连接状态
	 * 
	 * @return
	 */
	private boolean checkSocket() {
		socket = SocketAdapter.getSocketAdapterInstance();
		if (socket != null) {
			return socket.isSendSwitch();
		}
		return false;
	}

	@Override
	public void onDestroy() {
		isOpen = false;
		if (socket != null) {
			socket.setSendSwitch(false);
			socket.close();
		}
		super.onDestroy();
	}

	/**
	 * 发送系统通知提示用户
	 */
	private void sendSystemNotice(String title, String content) {
		// 获取通知管理器
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		// 新建一个通知，指定其图标和标题
		Notification notification = new Notification(R.drawable.icon, null, when);// 第一个参数为图标,第二个参数为标题,第三个为通知时间
		// notification.sound=Uri.parse("file:///android_asset/default.mp3");
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		
		SharedPreferences preferences = getSharedPreferences(ConstantValue.SETTING_NAME, Context.MODE_PRIVATE);
		boolean vibration = preferences.getBoolean("lottery_vibration", false);
		boolean sound = preferences.getBoolean("lottery_sound", false);
		if (vibration) {
			if (sound) {
				notification.defaults = Notification.DEFAULT_VIBRATE + Notification.DEFAULT_SOUND;
			} else {
				notification.defaults = Notification.DEFAULT_VIBRATE;
			}
		} else {
			if (sound)
				notification.defaults = Notification.DEFAULT_SOUND;// 发出默认声音
		}
		// notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soar_raw_lottery_notify_default);
		Intent openintent = new Intent(this, MainActivity.class);
		openintent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, openintent, 0);// 当点击消息时就会向系统发送openintent意图
		notification.setLatestEventInfo(this, title, content, contentIntent);
		mNotificationManager.notify(0, notification);// 第一个参数为自定义的通知唯一标识
		
		Intent MyIntent = new Intent(Intent.ACTION_MAIN);
		MyIntent.addCategory(Intent.CATEGORY_HOME);
		startActivity(MyIntent);
		
		
		
//		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		Intent mIntent = new Intent(NoticeService.this, MainActivity1.class);
//		mIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//		PendingIntent mPendingIntent = PendingIntent.getActivity(NoticeService.this, 0, mIntent, 0);
//
//		Notification mNotification = new Notification();
//
//		mNotification.icon = R.drawable.icon;
//
//		mNotification.setLatestEventInfo(NoticeService.this, "业务通", "", mPendingIntent);
//		mNotification.flags = Notification.FLAG_ONGOING_EVENT;
//
//		mNotificationManager.notify(1, mNotification);
//
//		Intent MyIntent = new Intent(Intent.ACTION_MAIN);
//		MyIntent.addCategory(Intent.CATEGORY_HOME);
//		startActivity(MyIntent);
		
	}

	/**
	 * 定时任务
	 * 
	 * @param triggerAtTime
	 * @param interval
	 */
	public void newAlarm(long triggerAtTime, long interval) {
		Intent intent = new Intent(this, NoticeService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, pendingIntent);
	}

	/**
	 * 建立连接
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	private boolean openConnection() {
		try {
			String getNowAPN = NetUtil.GetNowAPN(NoticeService.this);
			if ("cmwap".equals(getNowAPN)) {
				System.getProperties().put("socksProxySet ", "true ");
				System.getProperties().put("socksProxyHost ", "10.0.0.172");
				System.getProperties().put("socksProxyPort ", 80);
			}

			socket = SocketAdapter.getSocketAdapterInstance(ConstantValue.IP, ConstantValue.PORT);
			return true;
		} catch (IOException e) {
			Log.i(TAG, "open socket error(ip:" + ConstantValue.IP + ";port:" + ConstantValue.PORT + ")");
			return false;
		}
	}

	/**
	 * 发送心跳数据
	 */
	private void sendActive() {
		MessageQueueManager.activeSingleThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				while (socket.isSendSwitch()) {
					boolean addCount = MessageQueueManager.addCount();
					if (addCount) {
						MessageQueueManager.putInfoToSendQueue(LongConnectionInfo.SEND_FLOG + LongConnectionInfo.ACTIVE);

						try {
							Thread.sleep(LongConnectionInfo.ACTIVE_INTERVAL);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						MessageQueueManager.initCountPool();
						Log.i(TAG, "active three time no response");
						startReConnected();
					}

				}

			}
		});

	}

	/**
	 * 发送回复信息
	 * 
	 * @param info
	 */
	private void sendReceiveInfo(int info) {
		String sendInfo = LongConnectionInfo.RECEIVE_FLOG + info;
		MessageQueueManager.putInfoToSendQueue(sendInfo);
	}

	private void writeInfo() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Log.i(TAG, "start send info.");
				while (socket.isSendSwitch()) {
					String item = null;
					try {
						item = MessageQueueManager.SEND_QUEUE.poll();
						if (StringUtils.isBlank(item)) {
							Thread.sleep(100);
							continue;
						}
						Log.i(TAG, "send info:" + item);

						socket.openOutputStream().write(item.getBytes());
						socket.openOutputStream().flush();
					} catch (Exception e) {
						e.printStackTrace();
						// 重新连接
						Log.i(TAG, "write info error.");
						startReConnected();
					}
				}
			}
		}).start();
	}

	private void startReConnected() {
		socket.setSendSwitch(false);
		socket.close();
		reConnected(true);
	}

	/**
	 * 获取指定彩种的中奖信息
	 */
	private void getBonuscodeMessage(String lotteryId) {
		MessageFactory instance = MessageFactory.getInstance();
		try {
			String body = instance.createGetBonuscodeBody(lotteryId);
			String msg = instance.createMessage("12003", "", body, false);
			if (StringUtils.isNotBlank(msg)) {
				httpHandle(ConstantValue.URL_LOTTERY, msg);
			}
		} catch (Exception e) {
			throw new MException(MException.UNKNOW_EXCEPTION);
		}
	}

	private void httpHandle(String url, String msg) {
		HttpClientAdapter adapter = new HttpClientAdapter();
		OutputStream openOutputStream = null;
		try {
			adapter.open(url, HttpClientAdapter.POST);
			adapter.setContent(msg.getBytes());
			adapter.post();
			if (adapter.getResponseCode() == 200) {
				InputStream inputStream = adapter.openInputStream();
				if (inputStream != null && adapter.getContentLength() > 0) {
					analysisBonusCode(inputStream);
				}
			} else {
				throw new MException(MException.NOT_FIND_RESOURCE);
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof MException) {
				throw (MException) e;
			} else {
				throw new MException(MException.NETWORK_ANOMALY);
			}
		} finally {
			try {
				if (openOutputStream != null)
					openOutputStream.close();
			} catch (Exception e2) {
				openOutputStream = null;
			}
		}
	}

	/**
	 * 解析开奖公告
	 * 
	 * @param inputStream
	 */
	private void analysisBonusCode(InputStream inputStream) {
		// 解析xml
		XmlPullParser parser = Xml.newPullParser();

		List<ZCLotteryPO> updateList = null;
		List<ZCLotteryPO> localList = new ArrayList<ZCLotteryPO>();

		ZCLotteryPO item = null;

		try {
			parser.setInput(inputStream, "UTF-8");
			int eventType = parser.getEventType();
			String name = "";
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase("elements")) {
							updateList = new ArrayList<ZCLotteryPO>();
						}
						if (name.equalsIgnoreCase("element")) {
							item = new ZCLotteryPO();
						}
						if (name.equalsIgnoreCase("lotteryid")) {
							item.setLotteryId(parser.nextText());
						}
						if (name.equalsIgnoreCase("lotteryname")) {
							item.setLotteryName(parser.nextText());
						}
						if (name.equalsIgnoreCase("issue")) {
							currentIssue = parser.nextText();
							item.setIssue(currentIssue);
						}
						if (name.equalsIgnoreCase("endtimestamp")) {
							item.setEndTimestamp(parser.nextText());
						}
						if (name.equalsIgnoreCase("endtime1")) {
							item.setEndTime1(parser.nextText());
						}
						if (name.equalsIgnoreCase("endtime2")) {
							item.setEndTime2(parser.nextText());
						}
						if (name.equalsIgnoreCase("endtime3")) {
							item.setEndTime3(parser.nextText());
						}
						if (name.equalsIgnoreCase("bonustime")) {
							item.setBonusTime(parser.nextText());
						}
						if (name.equalsIgnoreCase("bonustime")) {
							// item.setBonusTime(parser.nextText());
						}
						if (name.equalsIgnoreCase("status")) {
							item.setStatus(parser.nextText());
						}
						if (name.equalsIgnoreCase("lasttime")) {
							item.setLastTime(parser.nextText());
						}
						if (name.equalsIgnoreCase("bonuscode")) {
							item.setBonusCode(parser.nextText());
						}

						break;
					case XmlPullParser.END_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase("element")) {
							if (item != null && updateList != null) {
								updateList.add(item);
								item = null;
							}
						}
						break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (updateList != null && updateList.size() > 0) {
			ZCLotteryPO zcLotteryPO = updateList.get(0);
			if (StringUtils.isNotBlank(zcLotteryPO.getBonusCode()))
				sendSystemNotice(zcLotteryPO.getLotteryName() + "第" + zcLotteryPO.getIssue() + "期" + "开奖号码", zcLotteryPO.getBonusCode());
			localList = impl.findAll();
			if (localList != null && localList.size() > 0) {
				if (updateList.size() > 0) {
					// 比对服务器与手机端已有信息
					List<ZCLotteryPO> list = new ArrayList<ZCLotteryPO>();
					for (ZCLotteryPO lotteryPO : updateList) {
						String lotteryId = lotteryPO.getLotteryId();
						ZCLotteryPO findLotteryByLotterId = impl.findLotteryByLotterId(lotteryId);
						if (findLotteryByLotterId != null) {
							if (findLotteryByLotterId.getIssue().compareTo(lotteryPO.getIssue()) < 0) {
								list.add(lotteryPO);
							}
						}
					}
					saveInfo(list);
				}

			} else {
				saveInfo(updateList);
			}
		}
	}

	/**
	 * 存储信息到手机数据库
	 * 
	 * @param list
	 */
	private void saveInfo(List<ZCLotteryPO> list) {
		if (list != null && list.size() > 0) {
			for (ZCLotteryPO item : list) {
				if (ZCLotteryVO.lotteryMap.containsKey(item.getLotteryId())) {
					if (item.getLotteryId().equals(ZCLotteryVO.SSQ) || item.getLotteryId().equals(ZCLotteryVO.QLC)) {
						String bonusCode = item.getBonusCode();
						int lastIndexOf = bonusCode.lastIndexOf(',');
						String read = bonusCode.substring(0, lastIndexOf);
						String blue = bonusCode.substring(lastIndexOf + 1);
						item.setBonusCode(read + "+" + blue);
					}
					impl.insert(item);
				}
			}
		}
	}

	private void aa() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Intent mIntent = new Intent(NoticeService.this, MainActivity.class);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent mPendingIntent = PendingIntent.getActivity(NoticeService.this, 0, mIntent, 0);

		Notification mNotification = new Notification();

		mNotification.icon = R.drawable.icon;

		mNotification.setLatestEventInfo(NoticeService.this, "业务通", "", mPendingIntent);
		mNotification.flags = Notification.FLAG_ONGOING_EVENT;

		mNotificationManager.notify(1, mNotification);

//		Intent MyIntent = new Intent(Intent.ACTION_MAIN);
//		MyIntent.addCategory(Intent.CATEGORY_HOME);
//		startActivity(MyIntent);
	}

}
