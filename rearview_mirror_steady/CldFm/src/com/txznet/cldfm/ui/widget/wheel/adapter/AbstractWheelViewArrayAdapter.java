package com.txznet.cldfm.ui.widget.wheel.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AbstractWheelViewArrayAdapter<T> extends ArrayAdapter<T> implements
		WheelViewAdapter {

	/** Text view resource. Used as a default view for adapter. */
	private static final int TEXT_VIEW_ITEM_RESOURCE = -1;

	/** Default text color */
	private static final int DEFAULT_TEXT_COLOR = 0xFF101010;

	/** Default text color */
	@SuppressWarnings("unused")
	private static final int LABEL_COLOR = 0xFF700070;

	/** No resource constant. */
	private static final int NO_RESOURCE = 0;

	// Text settings
	private int textColor = DEFAULT_TEXT_COLOR;
	private float textSize;

	private Context mContext;
	// Empty items resources
	protected int mEmptyItemResourceId;

	private LayoutInflater mInflater;

	public AbstractWheelViewArrayAdapter(Context context, int itemResId) {
		super(context, itemResId);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		textSize = 20;
	}

	@Override
	public long getItemId(int position) {
		// return getItem(position).getId();
		return -1;
	}

	@Override
	public View getEmptyItem(View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = getView(mEmptyItemResourceId, parent);
		}
		if (mEmptyItemResourceId == TEXT_VIEW_ITEM_RESOURCE
				&& convertView instanceof TextView) {
			configureTextView((TextView) convertView);
		}

		return convertView;
	}

	@Override
	public View getItem(int index, View convertView, ViewGroup parent) {
		// CategoryVo categoryVo = getItem(index);
		// ViewHold hold;
		// if (convertView == null) {
		// hold = new ViewHold();
		// convertView = getInflater().inflate(mItemResId, null, false);
		// hold.iconIv = (ImageView)convertView.findViewById(R.id.icon);
		// hold.nameTv = (TextView)convertView.findViewById(R.id.name);
		// convertView.setTag(hold);
		// }else {
		// hold = (ViewHold) convertView.getTag();
		// }
		//
		// hold.iconIv.setBackgroundResource(CategoryIconResourcesHelper.getIconIndexByIconName(categoryVo.get_tempIconName(),categoryVo.getDepth()));
		// hold.nameTv.setText(categoryVo.getName());
		// return convertView;
		return null;
	}

	class ViewHold {
		ImageView iconIv;
		TextView nameTv;
	}

	@Override
	public int getItemsCount() {
		return getItems().size();
	}

	/**
	 * Configures text view. Is called for the TEXT_VIEW_ITEM_RESOURCE views.
	 * 
	 * @param view
	 *            the text view to be configured
	 */
	protected void configureTextView(TextView view) {
		view.setTextColor(textColor);
		view.setGravity(Gravity.CENTER);
		view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		view.setLines(1);
		view.setSingleLine(true);
		view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
	}

	/**
	 * Loads view from resources
	 * 
	 * @param resource
	 *            the resource Id
	 * @return the loaded view or null if resource is not set
	 */
	private View getView(int resource, ViewGroup parent) {
		switch (resource) {
		case NO_RESOURCE:
			return null;
		case TEXT_VIEW_ITEM_RESOURCE:
			return new TextView(mContext);
		default:
			return mInflater.inflate(resource, parent, false);
		}
	}

	@Override
	protected View createViewFromResource(int position, View convertView,
			ViewGroup parent, int resource) {
		return null;
	}

}
