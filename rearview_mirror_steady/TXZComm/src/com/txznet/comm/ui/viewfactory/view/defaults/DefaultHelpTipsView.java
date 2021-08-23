package com.txznet.comm.ui.viewfactory.view.defaults;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.HelpTipsViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IHelpTipsView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.comm.R;
import com.txznet.txz.util.TXZFileConfigUtil;


public class DefaultHelpTipsView extends IHelpTipsView {
	private static DefaultHelpTipsView instance = new DefaultHelpTipsView();

	public static DefaultHelpTipsView getInstance() {
		return instance;
	}

	@Override
	public void init() {
		mFlags = UPDATEABLE;
		super.init();
	}

	private View convertView;

	@Override
	public ViewFactory.ViewAdapter getView(ViewData data) {
		HelpTipsViewData helpTipsViewData = (HelpTipsViewData) data;
		convertView = LayoutInflater.from(GlobalContext.get()).inflate(R.layout.help_tip_view, null);
		LinearLayout llContent = (LinearLayout) convertView.findViewById(R.id.llContent);
		llContent.removeAllViews();
		llContent.setGravity(Gravity.CENTER_HORIZONTAL);

		int itemHeight = (int)(com.txznet.comm.ui.util.ConfigUtil.getDisplayLvItemH(false) * 0.8f);
		for (int i = 0 ; i < helpTipsViewData.getData().size() ; i++) {
			HelpTipsViewData.HelpTipBean bean = helpTipsViewData.getData().get(i);
			View view = LayoutInflater.from(GlobalContext.get()).inflate(R.layout.help_tip_view_item,null);
			ImageView icon = ((ImageView)view.findViewById(R.id.icon));
			LinearLayout.LayoutParams iconLayoutParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
			if(LayouUtil.getDrawable(bean.resId) == null){
				ImageLoader.getInstance().displayImage(
						"file://" + bean.resId, new ImageViewAware(icon));
			}else {
				icon.setImageDrawable(LayouUtil.getDrawable(bean.resId));
			}
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
			layoutParams.width = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_HELP_ICON_WIDTH);
			layoutParams.height = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_HELP_ICON_HEIGHT);
			TextView title = ((TextView)view.findViewById(R.id.title));
			title.setText(bean.label);
			int textSize = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_HELP_TEXT_SIZE,0);
			if(textSize == 0){
				textSize = (int)LayouUtil.getDimen("m12");
			}
			LinearLayout.LayoutParams params;
            if (i == 2 || helpTipsViewData.getData().size() == 2) {
				TextViewUtil.setTextSize(title, textSize + (int)LayouUtil.getDimen("m12"));
				iconLayoutParams.height = (int)LayouUtil.getDimen("m36");
				iconLayoutParams.width = (int)LayouUtil.getDimen("m36");
				icon.setLayoutParams(iconLayoutParams);
				icon.setScaleX(8.0f/9.0f);
				icon.setScaleY(8.0f/9.0f);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            }else {
                float alpha = (i + 1) < 3 ? i + 1 : 3 - (i + 1) % 3;
				iconLayoutParams.height = (int)(LayouUtil.getDimen("m36") * (1 - (3 - alpha) / 5));
				iconLayoutParams.width = (int)(LayouUtil.getDimen("m36") * (1 - (3 - alpha) / 5));
				icon.setLayoutParams(iconLayoutParams);
				icon.setScaleX(8.0f/9.0f);
				icon.setScaleY(8.0f/9.0f);
                icon.setAlpha(alpha / 3);
                title.setAlpha(alpha / 3);
				TextViewUtil.setTextSize(title, textSize + (int)LayouUtil.getDimen("m12") *(alpha / 3));
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            }
			llContent.addView(view, params);
		}

		ViewFactory.ViewAdapter adapter = new ViewFactory.ViewAdapter();
		adapter.type = data.getType();
		adapter.view = convertView;
		adapter.object = DefaultHelpTipsView.getInstance();
		return adapter;
	}

	@Override
	public Object updateView(ViewData data) {
		HelpTipsViewData helpTipsViewData = (HelpTipsViewData) data;
		LinearLayout llContent = (LinearLayout) convertView.findViewById(R.id.llContent);
		llContent.removeAllViews();
		llContent.setGravity(Gravity.CENTER_HORIZONTAL);

		int itemHeight = (int)(com.txznet.comm.ui.util.ConfigUtil.getDisplayLvItemH(false) * 0.8f);
		for (int i = 0 ; i < helpTipsViewData.getData().size() ; i++) {
			HelpTipsViewData.HelpTipBean bean = helpTipsViewData.getData().get(i);
			View view = LayoutInflater.from(GlobalContext.get()).inflate(R.layout.help_tip_view_item,null);
			ImageView icon = ((ImageView)view.findViewById(R.id.icon));
			if(LayouUtil.getDrawable(bean.resId) == null){
				ImageLoader.getInstance().displayImage(
						"file://" + bean.resId, new ImageViewAware(icon));
			}else {
				icon.setImageDrawable(LayouUtil.getDrawable(bean.resId));
			}
            LinearLayout.LayoutParams iconLayoutParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
			layoutParams.width = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_HELP_ICON_WIDTH);
			layoutParams.height = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_HELP_ICON_HEIGHT);
			TextView title = ((TextView)view.findViewById(R.id.title));
			title.setText(bean.label);
			int textSize = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_HELP_TEXT_SIZE, 0);
			if (textSize == 0) {
				textSize = (int)LayouUtil.getDimen("m12");
			}
			LinearLayout.LayoutParams params;
			if (i == 2 || helpTipsViewData.getData().size() == 2) {
				TextViewUtil.setTextSize(title, textSize + (int)LayouUtil.getDimen("m12"));

                iconLayoutParams.height = (int)LayouUtil.getDimen("m36");
                iconLayoutParams.width = (int)LayouUtil.getDimen("m36");
                icon.setLayoutParams(iconLayoutParams);
                icon.setScaleX(8.0f/9.0f);
                icon.setScaleY(8.0f/9.0f);
				params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			}else {
				float alpha = (i + 1) < 3 ? i + 1 : 3 - (i + 1) % 3;
                iconLayoutParams.height = (int)(LayouUtil.getDimen("m36") * (1 - (3 - alpha) / 5));
                iconLayoutParams.width = (int)(LayouUtil.getDimen("m36") * (1 - (3 - alpha) / 5));
                icon.setLayoutParams(iconLayoutParams);
                icon.setScaleX(8.0f/9.0f);
                icon.setScaleY(8.0f/9.0f);
                icon.setAlpha(alpha / 3);
                title.setAlpha(alpha / 3);
				icon.setAlpha(alpha / 3);
				title.setAlpha(alpha / 3);
				TextViewUtil.setTextSize(title, textSize + (int)LayouUtil.getDimen("m12") *(alpha / 3));
				params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			}
			llContent.addView(view,params);
		}
		return super.updateView(data);
	}
}
