package com.txznet.txz.component.selector;

import java.util.List;

public interface PagerAdapter<T> {
	public boolean nextPager();

	public boolean prevPager();

	public int getCurPager();

	public int getMaxPager();

	public int getPagerCount();

	public void reset();

	public void setOnListChangeListener(OnListChangeListener listener);

	public void onUpdate(List list, int showCount,boolean useNewSelector);

	public static interface OnListChangeListener {
		public void onListChange(List list);
	}
}