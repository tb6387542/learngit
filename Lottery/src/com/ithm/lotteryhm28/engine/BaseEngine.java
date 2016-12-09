package com.ithm.lotteryhm28.engine;

import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

import com.ithm.lotteryhm28.ConstantValue;
import com.ithm.lotteryhm28.net.HttpClientUtil;
import com.ithm.lotteryhm28.net.protocal.Message;
import com.ithm.lotteryhm28.util.DES;

public abstract class BaseEngine {
	public Message getResult(String xml) {
		// 绗簩姝ュ拰绗笁姝�

		// 绗簩姝�(浠ｇ爜涓嶅彉)锛氬彂閫亁ml鍒版湇鍔″櫒绔紝绛夊緟鍥炲
		// HttpClientUtil.sendXml
		// 鍦ㄨ繖琛屼唬鐮佸墠锛屾病鏈夊垽鏂綉缁滅被鍨嬶紵
		HttpClientUtil util = new HttpClientUtil();
		InputStream is = util.sendXml(ConstantValue.LOTTERY_URI, xml);
		// 鍒ゆ柇杈撳叆娴侀潪绌�
		if (is != null) {
			Message result = new Message();

			// 绗笁姝�(浠ｇ爜涓嶅彉)锛氭暟鎹殑鏍￠獙锛圡D5鏁版嵁鏍￠獙锛�
			// timestamp+digest+body
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(is, ConstantValue.ENCONDING);

				int eventType = parser.getEventType();
				String name;

				while (eventType != XmlPullParser.END_DOCUMENT) {
					switch (eventType) {
					case XmlPullParser.START_TAG:
						name = parser.getName();
						if ("timestamp".equals(name)) {
							result.getHeader().getTimestamp()
									.setTagValue(parser.nextText());
						}
						if ("digest".equals(name)) {
							result.getHeader().getDigest()
									.setTagValue(parser.nextText());
						}
						if ("body".equals(name)) {
							result.getBody().setServiceBodyInsideDESInfo(
									parser.nextText());
						}
						break;
					}
					eventType = parser.next();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			// 鍘熷鏁版嵁杩樺師锛氭椂闂存埑锛堣В鏋愶級+瀵嗙爜锛堝父閲忥級+body鏄庢枃锛堣В鏋�+瑙ｅ瘑DES锛�
			// body鏄庢枃锛堣В鏋�+瑙ｅ瘑DES锛�
			DES des = new DES();
			String body = "<body>"
					+ des.authcode(result.getBody()
							.getServiceBodyInsideDESInfo(), "ENCODE",
							ConstantValue.DES_PASSWORD) + "</body>";

			String orgInfo = result.getHeader().getTimestamp().getTagValue()
					+ ConstantValue.AGENTER_PASSWORD + body;

			// 鍒╃敤宸ュ叿鐢熸垚鎵嬫満绔殑MD5
			String md5Hex = DigestUtils.md5Hex(orgInfo);
			// 灏嗘墜鏈虹涓庢湇鍔″櫒鐨勮繘琛屾瘮瀵�
			if (md5Hex.equals(result.getHeader().getDigest().getTagValue())) {
				// 姣斿閫氳繃
				return result;
			}
		}
		return null;
	}
}
