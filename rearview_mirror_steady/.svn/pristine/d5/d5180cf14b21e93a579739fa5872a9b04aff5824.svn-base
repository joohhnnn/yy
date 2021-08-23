package com.txznet.test;

import junit.framework.TestCase;

import com.txznet.music.utils.PinYinUtil;

public class TestDB extends TestCase {

	public static void main(String[] args) {
		// AlbumDBHelper.getInstance().findOne(Album.class, "id=?", new
		// String[]{String.valueOf(SharedPreferencesUtils.getCurrentAlbumID())});
	}

	public void testPinyin() {
		String str = "王菲-haoting";
		str = str.replaceAll("[\\p{P}]", "");
		System.out.println(str);
		assertEquals(str, PinYinUtil.getAllPinYin("斑马,"));
	}

}
