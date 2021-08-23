package com.txznet.txz.module.ui.view;

import com.txznet.comm.remote.GlobalContext;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public abstract class BSView extends BPView {
	public static final int KEY_VIEW_TAG = 0x0001;

	public static final String TAG_TITLE = "tagTitle";
	public static final int ID_TITLE = 0x0001;
	public static final String TAG_PREPAGE = "tagPrePage";
	public static final int ID_PREPAGE = 0x0002;
	public static final String TAG_NEXTPAGE = "tagNextPage";
	public static final int ID_NEXTPAGE = 0x0003;
	public static final String TAG_PAGEINFO = "tagPageInfo";
	public static final int ID_PAGEINFO = 0x0004;
	public static final String TAG_LISTCON = "tagListCon";
	public static final int ID_LISTCON = 0x0005;

	public BSView(String jsonData) {
		super(GlobalContext.get(), jsonData);
	}

	@Override
	public View createView() {
		return createContainerView();
	}

	private View createContainerView() {
		LinearLayout container = new LinearLayout(getContext());
		container.setOrientation(VERTICAL);
		container.addView(createTitlePageView(), new LayoutParams(LayoutParams.MATCH_PARENT, 100));
		container.addView(createContentView(), genLayoutParam(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
		return container;
	}

	private LayoutParams genLayoutParam(int width, int height, int weight) {
		LayoutParams params = new LayoutParams(width, height);
		params.weight = weight;
		return params;
	}

	private View createTitlePageView() {
		LinearLayout ll = new LinearLayout(getContext());
		ll.setOrientation(HORIZONTAL);
		ll.addView(createTitleView(), genLayoutParam(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
		ll.addView(createPageView(), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return ll;
	}

	private View createTitleView() {
		TextView tv = new TextView(getContext());
		tv.setId(ID_TITLE);
		tv.setTag(KEY_VIEW_TAG, TAG_TITLE);
		return tv;
	}

	private View createPageView() {
		LinearLayout ll = new LinearLayout(getContext());
		ll.setOrientation(HORIZONTAL);
		ll.addView(createTextView(ID_PREPAGE, TAG_PREPAGE),
				genLayoutParam(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
		ll.addView(createTextView(ID_PAGEINFO, TAG_PAGEINFO),
				genLayoutParam(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
		ll.addView(createTextView(ID_NEXTPAGE, TAG_NEXTPAGE),
				genLayoutParam(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
		return ll;
	}

	private TextView createTextView(int viewId, String tagId) {
		TextView tv = new TextView(getContext());
		tv.setId(viewId);
		tv.setTag(KEY_VIEW_TAG, tagId);
		return tv;
	}

	private View createContentView() {
		ListView lv = new ListView(getContext());
		lv.setId(ID_LISTCON);
		lv.setTag(KEY_VIEW_TAG, TAG_LISTCON);
		return lv;
	}
}