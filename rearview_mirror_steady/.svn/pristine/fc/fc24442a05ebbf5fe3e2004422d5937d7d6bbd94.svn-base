package com.txznet.music.utils;

import java.util.Collection;
import java.util.List;

public class CollectionUtils {

	public static boolean isEmpty(Collection coll) {
		return null == coll || coll.size() <= 0;
	}

	public static boolean isNotEmpty(Collection coll) {
		return !isEmpty(coll);
	}

	public static String toString(List collection) {
		if (CollectionUtils.isEmpty(collection)) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < collection.size(); i++) {
			sb.append(collection.get(i).toString());
			if (i != collection.size() - 1) {// 最后一个不加","
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public static String[] toStrings(List col) {
		if (isEmpty(col)) {
			return null;
		}
		String [] str=new String[col.size()];
		for (int i = 0; i < col.size(); i++) {
			str[i]=col.get(i).toString();
		}
		return str;
	}

	// public static List<Audio> toAudios(List<HistoryAudio> historyAudios) {
	//
	// List<Audio> audios = new ArrayList<Audio>();
	// if (isEmpty(historyAudios)) {
	// return audios;
	// }
	//
	// try {
	// for (int i = 0; i < historyAudios.size(); i++) {
	// Audio audio = Audio.class.newInstance();
	// HistoryAudio historyAudio = historyAudios.get(i);
	// Field[] declaredFields = historyAudio.getClass().getDeclaredFields();
	// for (int j = 0; j < declaredFields.length; j++) {
	// Field field = declaredFields[j];
	// field.setAccessible(true);
	// StringBuffer sb = new StringBuffer();
	// sb.append("set");
	// sb.append(String.valueOf(declaredFields[j].getName().charAt(0)).toUpperCase());
	// sb.append(declaredFields[j].getName().substring(1));
	// Method declaredMethod = audio.getClass().getDeclaredMethod(sb.toString(),
	// declaredFields[j].getType());
	// declaredMethod.invoke(audio, field.get(historyAudio));
	// }
	// audios.add(audio);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return audios;
	//
	// }
}
