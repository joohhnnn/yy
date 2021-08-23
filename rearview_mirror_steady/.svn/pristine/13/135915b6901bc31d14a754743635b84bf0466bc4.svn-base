package com.txznet.txz.component.choice;

public interface OnItemSelectListener<V> {
	/**
	 * @param isPreSelect
	 *            是否是选中前（不等播报TTS），为false，则播报TTS后回调，如即将为您播放XXX 的时候回调
	 * @param v
	 *            选中的ITEM
	 * @param fromPage
	 *            idx来源
	 * @param idx
	 *            页索引
	 * @param fromVoice
	 *            是否来自声控选中
	 * @return
	 */
	boolean onItemSelected(boolean isPreSelect, V v, boolean fromPage, int idx, String fromVoice);
}