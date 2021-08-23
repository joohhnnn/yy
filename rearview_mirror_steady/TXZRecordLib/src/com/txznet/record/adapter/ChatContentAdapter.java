package com.txznet.record.adapter;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.WriterException;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.BitmapCache;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.ImageUtils;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.data.OfflinePromoteViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultOfflinePromoteView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.bean.BaseDisplayMsg;
import com.txznet.record.bean.ChatMessage;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.FeedbackMsg;
import com.txznet.record.bean.HelpMsg;
import com.txznet.record.bean.HelpTipBean;
import com.txznet.record.bean.HelpTipMsg;
import com.txznet.record.bean.MusicMsg;
import com.txznet.record.bean.OfflinePromoteMsg;
import com.txznet.record.bean.PluginMessage;
import com.txznet.record.bean.PoiMsg;
import com.txznet.record.bean.StockMessage;
import com.txznet.record.bean.WeatherMessage;
import com.txznet.record.bean.WxContactMsg;
import com.txznet.record.bean.PluginMessage.PluginData;
import com.txznet.record.lib.R;
import com.txznet.record.setting.MainActivity;
import com.txznet.record.ui.StockRefresher;
import com.txznet.record.ui.WeatherRefresher;
import com.txznet.record.ui.WinRecord;
import com.txznet.record.view.DisplayLvRef;
import com.txznet.record.view.IconTextView;
import com.txznet.record.view.PrinterTextView;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.QRUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.txznet.comm.ui.viewfactory.data.ConstellationFortuneData;
import com.txznet.comm.ui.viewfactory.data.ConstellationMatchingData;
import com.txznet.comm.ui.viewfactory.data.FeedbackViewData;
import com.txznet.comm.ui.viewfactory.data.LogoQrCodeViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.ConstellationFortuneView;
import com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView;

import com.txznet.comm.ui.viewfactory.view.defaults.DefaultLogoQrCodeView;
import com.txznet.comm.ui.viewfactory.view.defaults.FeedbackView;
import com.txznet.comm.util.DateUtils;
import com.txznet.record.bean.BindDeviceMsg;
import com.txznet.record.bean.ConstellationFortuneMsg;
import com.txznet.record.bean.ConstellationMatchingMsg;
import com.txznet.record.bean.CompetitionMsg;
import com.txznet.record.bean.LogoQrCodeMsg;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatContentAdapter extends BaseAdapter {
	private static class ChatViewHolder {
		public TextView content;
		public ImageView icon;
		public ImageView exIcon;
		public TextView title;
		public ListView list;
		public View cancel;
	}

	public static class WeatherViewHolder extends ChatViewHolder {
		public LinearLayout mCurrentTemp;
		public LinearLayout mBigTempRange;
		public ImageView mMinus;
		public ImageView mTempDecade;
		public ImageView mTempUnits;
		public ImageView mTempDegree;
		public TextView mTempRange;
		public ImageView mTodayWeather;

		public ImageView mBigLowMinus;
		public ImageView mBigLowTempDecade;
		public ImageView mBigLowTempUnits;
		public ImageView mBigHighMinus;
		public ImageView mBigHighTempDecade;
		public ImageView mBigHighTempUnits;
		public ImageView mBigTempDegree;

		public TextView mDate;
		public TextView mDay;
		public TextView mWeather;
		public TextView mWind;

		public TextView mAirQualityText;
		public TextView mAirQuality;
		public TextView mAirDegree;
		public TextView mCity;

		public IconTextView mToday;
		public IconTextView mTomorrow;
		public IconTextView mTheDayAfterTomorrow;
	}

	public static class WeatherViewHolderLarge extends ChatViewHolder {
		public ImageView mMinusIv;
		public ImageView mLeftIv;
		public ImageView mRightIv;
		public ImageView mDegreeIv;
		public TextView mTempRangeTv;
		public ImageView mIv;
		public TextView mDateTv;
		public TextView mTodayTv;
		public TextView mDesTv;
		public TextView mCityTv;
		public TextView mAirNumTv;
		public TextView mAirDesTv;
		public TextView mWingTv;
		public ImageView mTodayIv;
		public TextView mTodayDegree;
		public TextView mTodayDesTv;
		public LinearLayout mTodayLayout;
		public ImageView mTomorrowIv;
		public TextView mTomorrowDegree;
		public TextView mTomorrowDesTv;
		public LinearLayout mTomorrowLayout;
		public ImageView mDayAfterIv;
		public TextView mDayAfterDegree;
		public TextView mDayAfterDesTv;
		public LinearLayout mDayAfterLayout;

		public RelativeLayout mCurrentTempLy;
		public LinearLayout mBigTempLy;
		public ImageView mBigLowMinusIv;
		public ImageView mBigLowTempDecade;
		public ImageView mBigLowTempUnits;
		public ImageView mBigSlash;
		public ImageView mBigHighMinus;
		public ImageView mBigHighTempDecade;
		public ImageView mBigHighTempUnits;
		public ImageView mBigTempDegree;
	}

	public static class StockViewHolder extends ChatViewHolder {
		public TextView mName;
		public TextView mCode;

		public TextView mPrice;
		public ImageView mUpAndDown;

		public ImageView mPic;

		public LinearLayout mStockInfoLy;
		public TextView mChangeAmount;
		public TextView mChangeRate;
		public TextView mYestodayClosePrice;
		public TextView mTodayOpenPrice;
		public TextView mHighestPrice;
		public TextView mLowestPrice;
		public TextView mTradingVolume;
	}

	public static class WXContactViewHolder extends ChatViewHolder{
		
	}
	
	public static class HelpViewHolder extends ChatViewHolder{
		public RelativeLayout mTitleLayout;
		public ImageButton mHelpButton;
		public TextView mTextDespOne;
		public TextView mTextDespTwo;
	}
	
	public static class DisplayListViewHolder extends ChatViewHolder {
		public DisplayLvRef mDisplayLv;
		public TextView mNoResultTv;
	}
	
	public static class PluginViewHolder extends ChatViewHolder {
		public View mView;
	}
	
	public static class QrCodeViewHolder extends ChatViewHolder {
		public ImageView mImageView;
	}

	public static class BindDeviceViewHolder extends ChatViewHolder {
		public ImageView mIvQrCode;
		public ImageView mImageView;
	}
	private String mQrCodeKey;
	private String mImageKey;

	public static class ChatHLViewHolder extends ChatViewHolder {
		public TextView HLcontent;
		public ImageView icon;
		public ImageView exIcon;
		public TextView title;
		public ListView list;
		public View cancel;
	}

	public static class HelpTipViewHolder extends ChatViewHolder {
		LinearLayout llContent;
	}


	public static class CompetitionDetailViewHolder extends ChatViewHolder {
		TextView tvLabel;
		TextView tvTime;
		ImageView ivHomeTeam;
		TextView tvHomeTeam;
		TextView tvGoal;
		TextView tvPeriod;
		ImageView ivAwayTeam;
		TextView tvAwayTeam;
	}

	private Context mContext;
	private List<ChatMessage> mChatMsgs;

	private WeakReference<BaseAdapter> mListAdapterRef;
	private WeakReference<ChatDisplayAdapter> mDisplayAdapterRef;
	private WeakReference<BaseDisplayMsg> mLastMsgRef;

	public ChatContentAdapter(Dialog dialog, List<ChatMessage> chatMsgs) {
		mContext = dialog.getContext();
		this.mChatMsgs = chatMsgs;
	}

	public ChatContentAdapter(Context context,List<ChatMessage> chatMsgs){
		this.mContext = context;
		this.mChatMsgs = chatMsgs;
	}
	
	@Override
	public int getCount() {
		return mChatMsgs.size();
	}

	@Override
	public ChatMessage getItem(int position) {
		return mChatMsgs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
//	private Animation animation = ListViewItemAnim.getAnimationSet();

    private Map<Integer, Boolean> isFrist = new HashMap<Integer, Boolean>();


    private View lastAnimView;
    private void startAnimation(int position,View convertView){
    	if (lastAnimView!=null) {
			lastAnimView.getAnimation().cancel();
		}
    	// 如果是第一次加载该view，则使用动画
//        if (isFrist.get(position) == null || isFrist.get(position)) {
//        	if (position == mChatMsgs.size()-1) {
//        		convertView.startAnimation(animation);
//                isFrist.put(position, false);
//			}
//            
//        }
    }

    /*
    	添加打字效果，补充ViewType重写
    	区分了TYPE_TO_SYS_PART_TEXT和其他类型的数据，使得PrinterTextView复用，避免出现同一个会话对话中同一句话文本实例被替换的问题
     */
    private static final int VIEW_TYPE_TO_SYS_PART_TEXT = 0;
    private static final int VIEW_TYPE_OTHER = 1;

	@Override
	public int getItemViewType(int position) {
		ChatMessage chatMsg = mChatMsgs.get(position);
		if (chatMsg.type == ChatMessage.TYPE_TO_SYS_PART_TEXT) {
			return VIEW_TYPE_TO_SYS_PART_TEXT;
		}
		return VIEW_TYPE_OTHER;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatMessage chatMsg = mChatMsgs.get(position);

		if (chatMsg instanceof PluginMessage) {
			PluginData pData = ((PluginMessage) chatMsg).mPluginData;
			if (pData != null) {
				convertView = pData.mView;
				convertView.setTag(R.id.chat_item_type, chatMsg.type);
				startAnimation(position, convertView);
				return convertView;
			}
		}
		
		int msgType = chatMsg.type;
		if (convertView == null || ((Integer) convertView.getTag(R.id.chat_item_type) != msgType)) {
			ChatViewHolder viewHolder = null;
			if (msgType == WeatherMessage.TYPE_FROM_SYS_WEATHER) {
//				if (ScreenUtil.isLargeScreen(dialog)) {
//					viewHolder = new WeatherViewHolderLarge();
//				} else {
					viewHolder = new WeatherViewHolder();
//				}
			} else if (msgType == StockMessage.TYPE_FROM_SYS_STOCK) {
				viewHolder = new StockViewHolder();
			} else if (msgType == ChatMessage.TYPE_FROM_SYS_POI) {
				viewHolder = new DisplayListViewHolder();
			} else if (msgType == ChatMessage.TYPE_FROM_SYS_AUDIO) {
				viewHolder = new DisplayListViewHolder();
			} else if (msgType == ChatMessage.TYPE_FROM_SYS_WX_PICKER) { // 微信联系人列表
				viewHolder = new DisplayListViewHolder();
			} else if (msgType == ChatMessage.TYPE_FROM_SYS_MUSIC) {
				viewHolder = new DisplayListViewHolder();
			}else if (msgType == ChatMessage.TYPE_FROM_SYS_HELP_WITH_ICON) {
				viewHolder = new HelpViewHolder();
			} else if(msgType == ChatMessage.TYPE_FROM_PLUGIN){
				viewHolder = new PluginViewHolder();
			} else if(msgType == ChatMessage.TYPE_FROM_SYS_QRCODE){
				viewHolder = new QrCodeViewHolder();
			} else if (msgType == ChatMessage.TYPE_FROM_SYS_BIND_SERVICE) {
				viewHolder = new BindDeviceViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_from_sys_bind_service, null);
				((BindDeviceViewHolder) viewHolder).mImageView = (ImageView)convertView.findViewById(R.id.iv_advertise);
				((BindDeviceViewHolder) viewHolder).mIvQrCode = (ImageView)convertView.findViewById(R.id.iv_qrCode);
			} else if(msgType == ChatMessage.TYPE_FROM_SYS_TEXT_HL){
				viewHolder = new ChatHLViewHolder();
			} else if (msgType == ChatMessage.TYPE_FROM_SYS_HELP_TIPS) {
				viewHolder = new HelpTipViewHolder();
			} else if (msgType == ChatMessage.TYPE_FROM_SYS_COMPETITION_DETAIL) {
				viewHolder = new CompetitionDetailViewHolder();
			} else {
				viewHolder = new ChatViewHolder();
			}
			// 系统发送的文本
			if (msgType == ChatMessage.TYPE_FROM_SYS_TEXT) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_from_sys_text, null);
				viewHolder.content = (TextView) convertView.findViewById(R.id.txtChat_Msg_Text);
				TextViewUtil.setTextSize(viewHolder.content,ViewConfiger.SIZE_CHAT_FROM_SYS);
				TextViewUtil.setTextColor(viewHolder.content,ViewConfiger.COLOR_CHAT_FROM_SYS);
			} 
			//系统发送的高亮文本
			else if (msgType == ChatMessage.TYPE_FROM_SYS_TEXT_HL) {
				ChatHLViewHolder hlViewHolder = (ChatHLViewHolder) viewHolder;
				convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_from_sys_text_hl, null);
				hlViewHolder.HLcontent = (TextView) convertView.findViewById(R.id.txtChat_Msg_Text_hl);
				TextViewUtil.setTextSize(hlViewHolder.HLcontent,ViewConfiger.SIZE_CHAT_FROM_SYS);
				TextViewUtil.setTextColor(hlViewHolder.HLcontent,ViewConfiger.COLOR_CHAT_FROM_SYS);
			}
			//系统发送的带打断tips的文本
			else if(msgType == ChatMessage.TYPE_FROM_SYS_TEXT_WITH_INTERRUPT_TIPS) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_from_sys_text_interrupt, null);
				viewHolder.content = (TextView) convertView.findViewById(R.id.tv_chat_msg_interrupt);
				viewHolder.title = (TextView) convertView.findViewById(R.id.tv_chat_interrupt_tips);
				int padding = viewHolder.content.getPaddingLeft();
				viewHolder.title.setPadding(padding, 0, 0, 0);
				float textSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CHAT_FROM_SYS);
				TextViewUtil.setTextSize(viewHolder.title,textSize - 2);
				TextViewUtil.setTextSize(viewHolder.content, textSize);
				TextViewUtil.setTextColor(viewHolder.content,ViewConfiger.COLOR_CHAT_FROM_SYS);
			}
			// 系统发送的带图标文本
			else if (msgType == ChatMessage.TYPE_FROM_SYS_HELP_WITH_ICON) {
				HelpViewHolder helpViewHolder = (HelpViewHolder) viewHolder;
				convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_from_sys_help_with_icon, null);
				helpViewHolder.mTitleLayout = (RelativeLayout) convertView.findViewById(R.id.layout_help_title);
				helpViewHolder.content = (TextView) convertView.findViewById(R.id.txtChat_Msg_Text);
				helpViewHolder.mHelpButton = (ImageButton) convertView.findViewById(R.id.imgChat_Msg_Icon);
				helpViewHolder.exIcon = (ImageView) convertView.findViewById(R.id.imgChat_Msg_Settings_Icon);
				helpViewHolder.mTextDespOne = (TextView) convertView.findViewById(R.id.textChat_help_desp_one);
				helpViewHolder.mTextDespTwo = (TextView) convertView.findViewById(R.id.textChat_help_desp_two);
			}
			// 用户发送的本文
			else if (msgType == ChatMessage.TYPE_TO_SYS_TEXT) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_to_sys_text, null);
				viewHolder.content = (TextView) convertView.findViewById(R.id.txtChat_Msg_Text);
				TextViewUtil.setTextSize(viewHolder.content,ViewConfiger.SIZE_CHAT_TO_SYS);
				TextViewUtil.setTextColor(viewHolder.content,ViewConfiger.COLOR_CHAT_TO_SYS);
			}
			//打字效果
			else if (msgType == ChatMessage.TYPE_TO_SYS_PART_TEXT) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_to_sys_text, null);
				viewHolder.content = (TextView) convertView.findViewById(R.id.txtChat_Msg_Text);
				TextViewUtil.setTextSize(viewHolder.content,ViewConfiger.SIZE_CHAT_TO_SYS_PART);
				TextViewUtil.setTextColor(viewHolder.content,ViewConfiger.COLOR_CHAT_TO_SYS_PART);
			}
			/*
				 * else if (msgType == ChatMessage.TYPE_FROM_SYS_WX_PICKER){
				 * DisplayListViewHolder ph = (DisplayListViewHolder)
				 * viewHolder;
				 * 
				 * convertView = LayoutInflater.from(mContext).inflate(R.layout.
				 * chat_wxcontact, null); ph.mDisplayLv = (DisplayLvRef)
				 * convertView.findViewById(R.id.poi_content_view);
				 * ph.mNoResultTv = (TextView)
				 * convertView.findViewById(R.id.txtChat_Msg_Text); }
				 */
			// 微信联系人列表
			/*
			 * else if (msgType == ChatMessage.TYPE_FROM_SYS_WX_PICKER) {
			 * convertView =
			 * LayoutInflater.from(mContext).inflate(R.layout.chat_wxcontact,
			 * null); viewHolder.title = (TextView)
			 * convertView.findViewById(R.id.wxcontact_title); viewHolder.list =
			 * (ListView) convertView.findViewById(R.id.wxcontact_content_view);
			 * //viewHolder.cancel =
			 * convertView.findViewById(R.id.txtChat_List_Cancel); }
			 */
			// 联系人列表
			else if (msgType == ChatMessage.TYPE_FROM_SYS_CONTACT) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_from_sys_list_cont, parent, false);
				viewHolder.title = (TextView) convertView.findViewById(R.id.txtChat_List_Title);
				viewHolder.list = (ListView) convertView.findViewById(R.id.lvChat_Msg_List);
			} else if (msgType == WeatherMessage.TYPE_FROM_SYS_WEATHER) {
				WeatherViewHolder v = (WeatherViewHolder) viewHolder;
				if (com.txznet.comm.ui.util.ConfigUtil.getLayoutType() == com.txznet.comm.ui.util.ConfigUtil.LAYOUT_TYPE_HORIZONTAL) {
					convertView = LayoutInflater.from(mContext).inflate(R.layout.weather_view, null);
				}else {
					convertView = LayoutInflater.from(mContext).inflate(R.layout.weather_view_vertical, null);
				}
				v.mCurrentTemp = (LinearLayout) convertView.findViewById(R.id.current_temp);
				v.mBigTempRange = (LinearLayout) convertView.findViewById(R.id.bigTempRange);
				v.mMinus = (ImageView) convertView.findViewById(R.id.minus);
				v.mTempDecade = (ImageView) convertView.findViewById(R.id.tempDecade);
				v.mTempUnits = (ImageView) convertView.findViewById(R.id.tempUnits);
				v.mTempDegree = (ImageView) convertView.findViewById(R.id.tempDegree);
				v.mTempRange = (TextView) convertView.findViewById(R.id.tempRange);
				v.mTodayWeather = (ImageView) convertView.findViewById(R.id.todayWeather);

				v.mBigLowMinus = (ImageView) convertView.findViewById(R.id.bigLowMinus);
				v.mBigLowTempDecade = (ImageView) convertView.findViewById(R.id.bigLowTempDecade);
				v.mBigLowTempUnits = (ImageView) convertView.findViewById(R.id.bigLowTempUnits);
				v.mBigHighMinus = (ImageView) convertView.findViewById(R.id.bigHighMinus);
				v.mBigHighTempDecade = (ImageView) convertView.findViewById(R.id.bigHighTempDecade);
				v.mBigHighTempUnits = (ImageView) convertView.findViewById(R.id.bigHighTempUnits);
				v.mBigTempDegree = (ImageView) convertView.findViewById(R.id.bigHighTempDecade);

				v.mDate = (TextView) convertView.findViewById(R.id.date);
				v.mDay = (TextView) convertView.findViewById(R.id.day);
				v.mWeather = (TextView) convertView.findViewById(R.id.weather);
				v.mWind = (TextView) convertView.findViewById(R.id.wind);

				v.mAirQualityText = (TextView) convertView.findViewById(R.id.airQualityText);
				v.mAirQuality = (TextView) convertView.findViewById(R.id.airQuality);
				v.mAirDegree = (TextView) convertView.findViewById(R.id.airDegree);
				v.mCity = (TextView) convertView.findViewById(R.id.city);

				v.mToday = (IconTextView) convertView.findViewById(R.id.today);
				v.mTomorrow = (IconTextView) convertView.findViewById(R.id.tomorrow);
				v.mTheDayAfterTomorrow = (IconTextView) convertView.findViewById(R.id.theDayAfterTomorrow);
				
				
				TextViewUtil.setTextSize(v.mCity,ViewConfiger.SIZE_WEATHER_CITY_SIZE1);
				TextViewUtil.setTextColor(v.mCity,ViewConfiger.COLOR_WEATHER_CITY_COLOR1);
				TextViewUtil.setTextSize(v.mDate,ViewConfiger.SIZE_WEATHER_DATE_SIZE1);
				TextViewUtil.setTextColor(v.mDate,ViewConfiger.COLOR_WEATHER_DATE_COLOR1);
				TextViewUtil.setTextSize(v.mWeather,ViewConfiger.SIZE_WEATHER_STATE_SIZE1);
				TextViewUtil.setTextColor(v.mWeather,ViewConfiger.COLOR_WEATHER_STATE_COLOR1);
				TextViewUtil.setTextSize(v.mWind,ViewConfiger.SIZE_WEATHER_STATE_SIZE1);
				TextViewUtil.setTextColor(v.mWind,ViewConfiger.COLOR_WEATHER_STATE_COLOR1);
				TextViewUtil.setTextSize(v.mTempRange,ViewConfiger.SIZE_WEATHER_TMP_SIZE2);
				TextViewUtil.setTextColor(v.mTempRange,ViewConfiger.COLOR_WEATHER_TMP_COLOR2);

				TextViewUtil.setTextSize(v.mAirQualityText,ViewConfiger.SIZE_WEATHER_AIR_SIZE1);
				TextViewUtil.setTextColor(v.mAirQualityText,ViewConfiger.COLOR_WEATHER_AIR_COLOR1);
				TextViewUtil.setTextSize(v.mAirQuality,ViewConfiger.SIZE_WEATHER_AIR_SIZE1);
				TextViewUtil.setTextColor(v.mAirQuality,ViewConfiger.COLOR_WEATHER_AIR_COLOR1);
				TextViewUtil.setTextSize(v.mAirDegree,ViewConfiger.SIZE_WEATHER_AIR_SIZE1);
				TextViewUtil.setTextColor(v.mAirDegree,ViewConfiger.COLOR_WEATHER_AIR_COLOR1);
				
				v.mToday.setTitleSize((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE2));
				v.mToday.setTitleColor((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR2));
				v.mToday.setHeadSize((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE1));
				v.mToday.setHeadColor((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR1));
				v.mTomorrow.setTitleSize((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE2));
				v.mTomorrow.setTitleColor((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR2));
				v.mTomorrow.setHeadSize((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE1));
				v.mTomorrow.setHeadColor((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR1));
				v.mTheDayAfterTomorrow.setTitleSize((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE2));
				v.mTheDayAfterTomorrow.setTitleColor((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR2));
				v.mTheDayAfterTomorrow.setHeadSize((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE1));
				v.mTheDayAfterTomorrow.setHeadColor((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR1));
				

			} else if (msgType == StockMessage.TYPE_FROM_SYS_STOCK) {
				StockViewHolder v = (StockViewHolder) viewHolder;
				convertView = LayoutInflater.from(mContext).inflate(R.layout.stock_view, null);

				v.mName = (TextView) convertView.findViewById(R.id.name);
				v.mCode = (TextView) convertView.findViewById(R.id.code);

				v.mPrice = (TextView) convertView.findViewById(R.id.price);
				v.mUpAndDown = (ImageView) convertView.findViewById(R.id.upAndDown);

				v.mPic = (ImageView) convertView.findViewById(R.id.pic);

				v.mStockInfoLy = (LinearLayout) convertView.findViewById(R.id.stock_info_ly);
				final View stockView = v.mStockInfoLy;
				stockView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						stockView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						int h = stockView.getMeasuredHeight();
						int sh = ScreenUtil.sWinChatRectHeight;
						if (sh < h) {
							stockView.setPadding(stockView.getPaddingLeft(), 0, stockView.getPaddingRight(),
									stockView.getPaddingBottom());
						}
					}
				});
				v.mChangeAmount = (TextView) convertView.findViewById(R.id.change_amount);
				v.mChangeRate = (TextView) convertView.findViewById(R.id.change_rate);
				v.mYestodayClosePrice = (TextView) convertView.findViewById(R.id.yestoday_close_price);
				v.mTodayOpenPrice = (TextView) convertView.findViewById(R.id.today_open_price);
				v.mHighestPrice = (TextView) convertView.findViewById(R.id.highest_price);
				v.mLowestPrice = (TextView) convertView.findViewById(R.id.lowest_price);
				v.mTradingVolume = (TextView) convertView.findViewById(R.id.trading_volume);
				
				
				TextViewUtil.setTextSize(v.mName,ViewConfiger.SIZE_SHARE_NAME_SIZE1);
				TextViewUtil.setTextColor(v.mName,ViewConfiger.COLOR_SHARE_NAME_COLOR1);
				TextViewUtil.setTextSize(v.mCode,ViewConfiger.SIZE_SHARE_NAME_SIZE2);
				TextViewUtil.setTextColor(v.mCode,ViewConfiger.COLOR_SHARE_NAME_COLOR2);
				TextViewUtil.setTextSize(v.mPrice,ViewConfiger.SIZE_SHARE_VALUE_SIZE1);
				TextViewUtil.setTextSize(v.mChangeAmount,ViewConfiger.SIZE_SHARE_RISE_SIZE1);
				TextViewUtil.setTextSize(v.mChangeRate,ViewConfiger.SIZE_SHARE_RISE_SIZE1);
				TextViewUtil.setTextSize(v.mYestodayClosePrice,ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
				TextViewUtil.setTextSize(v.mTodayOpenPrice,ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
				TextViewUtil.setTextSize(v.mHighestPrice,ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
				TextViewUtil.setTextSize(v.mLowestPrice,ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
				TextViewUtil.setTextSize(((TextView) convertView.findViewById(R.id.yestoday_close_price_label)),ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
				TextViewUtil.setTextSize(((TextView) convertView.findViewById(R.id.today_open_price_label)),ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
				TextViewUtil.setTextSize(((TextView) convertView.findViewById(R.id.highest_price_label)),ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
				TextViewUtil.setTextSize(((TextView) convertView.findViewById(R.id.lowest_price_label)),ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
				
			} else if (msgType == ChatMessage.TYPE_FROM_SYS_QRCODE) {
				QrCodeViewHolder qrViewHolder = (QrCodeViewHolder) viewHolder;
				
				convertView = LayoutInflater.from(mContext).inflate(R.layout.qrcode_view, null);
				qrViewHolder.mImageView = (ImageView) convertView.findViewById(R.id.iv_qrcode);

			} else if (msgType == ChatMessage.TYPE_FROM_SYS_HELP_TIPS) {
				HelpTipViewHolder v = (HelpTipViewHolder) viewHolder;
				convertView = LayoutInflater.from(mContext).inflate(R.layout.help_tip_view, null);
				v.content = (TextView) convertView.findViewById(R.id.tvTitle);
				v.llContent = (LinearLayout) convertView.findViewById(R.id.llContent);

			} else if (chatMsg instanceof BaseDisplayMsg && chatMsg instanceof PoiMsg) {
				DisplayListViewHolder ph = (DisplayListViewHolder) viewHolder;

				convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_poi_ly, null);
				ph.mDisplayLv = (DisplayLvRef) convertView.findViewById(R.id.poi_content_view);
				ph.mNoResultTv = (TextView) convertView.findViewById(R.id.txtChat_Msg_Text);
				TextViewUtil.setTextSize(ph.mNoResultTv,ViewConfiger.SIZE_CHAT_FROM_SYS);
				TextViewUtil.setTextColor(ph.mNoResultTv,ViewConfiger.COLOR_CHAT_FROM_SYS);
			} else if (chatMsg instanceof PluginMessage) {
				PluginMessage pm = (PluginMessage) chatMsg;
				convertView = pm.mPluginData.mView;
				PluginViewHolder pvh = (PluginViewHolder) viewHolder;
				pvh.mView = convertView;
			} else if (msgType == ChatMessage.TYPE_FROM_SYS_COMPETITION_DETAIL) {
				CompetitionDetailViewHolder detailViewHolder = (CompetitionDetailViewHolder) viewHolder;
				convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_competition_detail, null);
				detailViewHolder.tvLabel = (TextView) convertView.findViewById(R.id.tvLabel);
				detailViewHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
				detailViewHolder.ivHomeTeam = (ImageView) convertView.findViewById(R.id.ivHomeTeam);
				detailViewHolder.tvHomeTeam = (TextView) convertView.findViewById(R.id.tvHomeTeam);
				detailViewHolder.tvGoal = (TextView) convertView.findViewById(R.id.tvGoal);
				detailViewHolder.tvPeriod = (TextView) convertView.findViewById(R.id.tvPeriod);
				detailViewHolder.ivAwayTeam = (ImageView) convertView.findViewById(R.id.ivAwayTeam);
				detailViewHolder.tvAwayTeam = (TextView) convertView.findViewById(R.id.tvAwayTeam);
			}
			

			if (convertView != null) {
				convertView.setTag(R.id.chat_item_type, msgType);
				convertView.setTag(R.id.chat_item_view_holder, viewHolder);
			}
		}

		// 初始化数据
        ChatViewHolder viewHolder = null;
        if (convertView != null && convertView.getTag(R.id.chat_item_view_holder) != null) {
            viewHolder = (ChatViewHolder) convertView.getTag(R.id.chat_item_view_holder);
            if (chatMsg.text != null && viewHolder.content != null) {
                if (Build.VERSION.SDK_INT >= 21) {
                    try {
                        Method setLetterSpacing = TextView.class.getDeclaredMethod("setLetterSpacing", float.class);
                        setLetterSpacing.invoke(viewHolder.content, 0.04f);
                    } catch (Exception e) {
					}
				}
                if ((msgType == ChatMessage.TYPE_TO_SYS_PART_TEXT)
						&& viewHolder.content instanceof PrinterTextView) {
                	((PrinterTextView) viewHolder.content).setPrintText(LanguageConvertor.toLocale(chatMsg.text));
				} else {
					viewHolder.content.setText(LanguageConvertor.toLocale(chatMsg.text));
				}
			}
        }
 		if (msgType == ChatMessage.TYPE_FROM_SYS_CONSTELLATION_FORTUNE) {
            ConstellationFortuneMsg constellationFortuneMsg = ((ConstellationFortuneMsg) chatMsg);
            ConstellationFortuneData constellationFortuneData = new ConstellationFortuneData();
            constellationFortuneData.name = constellationFortuneMsg.name;
            constellationFortuneData.level = constellationFortuneMsg.level;
            constellationFortuneData.desc = constellationFortuneMsg.desc;
            constellationFortuneData.fortuneType = constellationFortuneMsg.fortuneType;
            convertView = ConstellationFortuneView.getInstance().getView(constellationFortuneData).view;
            convertView.setTag(R.id.chat_item_type, msgType);
            convertView.setTag(R.id.chat_item_view_holder, new ChatViewHolder());
        }
        if (msgType == ChatMessage.TYPE_FROM_SYS_CONSTELLATION_MATCHING) {
            ConstellationMatchingMsg constellationMatchingMsg = ((ConstellationMatchingMsg) chatMsg);
            ConstellationMatchingData constellationMatchingData = new ConstellationMatchingData();
            constellationMatchingData.name = constellationMatchingMsg.name;
            constellationMatchingData.level = constellationMatchingMsg.level;
            constellationMatchingData.desc = constellationMatchingMsg.desc;
            constellationMatchingData.matchName = constellationMatchingMsg.matchName;
            convertView = ConstellationMatchingView.getInstance().getView(constellationMatchingData).view;
            convertView.setTag(R.id.chat_item_type, msgType);
            convertView.setTag(R.id.chat_item_view_holder, new ChatViewHolder());
		}
        if (msgType == ChatMessage.TYPE_FROM_SYS_FEEDBACK) {
			FeedbackMsg feedbackMsg = ((FeedbackMsg) chatMsg);
			FeedbackViewData feedbackViewData = new FeedbackViewData();
			feedbackViewData.tips = feedbackMsg.tips;
			feedbackViewData.qrCode = feedbackMsg.qrCode;
			convertView = FeedbackView.getInstance().getView(feedbackViewData).view;
			convertView.setTag(R.id.chat_item_type, msgType);
			convertView.setTag(R.id.chat_item_view_holder, new ChatViewHolder());
		}
        //带Logo二维码
		if(msgType == ChatMessage.TYPE_CHAT_LOGO_QRCODE){
			LogoQrCodeMsg logoQrCodeMsg = (LogoQrCodeMsg) chatMsg;
			LogoQrCodeViewData qrCodeViewData = new LogoQrCodeViewData();
			qrCodeViewData.qrCode = logoQrCodeMsg.qrCode;
			convertView = DefaultLogoQrCodeView.getInstance().getView(qrCodeViewData).view;
			convertView.setTag(R.id.chat_item_type, msgType);
			convertView.setTag(R.id.chat_item_view_holder, new ChatViewHolder());
		} else if(msgType == ChatMessage.TYPE_CHAT_OFFLINE_PROMOTE){//OFFLINE PROMOTE
			OfflinePromoteMsg offlinePromoteMsg = (OfflinePromoteMsg) chatMsg;
			OfflinePromoteViewData offlinePromoteViewData = new OfflinePromoteViewData();
			offlinePromoteViewData.qrCode = offlinePromoteMsg.qrCode;
			offlinePromoteViewData.text = offlinePromoteMsg.text;
			convertView = DefaultOfflinePromoteView.getInstance().getView(offlinePromoteViewData).view;
			convertView.setTag(R.id.chat_item_type, msgType);
			convertView.setTag(R.id.chat_item_view_holder, new ChatViewHolder());
		} else if(msgType == ChatMessage.TYPE_FROM_SYS_TEXT_HL) {
			ChatHLViewHolder hlViewHolder = (ChatHLViewHolder) viewHolder;
			hlViewHolder.HLcontent.setText(Html.fromHtml(LanguageConvertor.toLocale(chatMsg.text)));
		} else if(msgType == ChatMessage.TYPE_FROM_SYS_TEXT_WITH_INTERRUPT_TIPS){
			viewHolder.title.setText(Html.fromHtml(LanguageConvertor.toLocale(chatMsg.title)));
		} else if (msgType == ChatMessage.TYPE_FROM_SYS_HELP_WITH_ICON) {
			final HelpViewHolder helpViewHolder = (HelpViewHolder) viewHolder;
			HelpMsg helpMsg = (HelpMsg) chatMsg;
			// helpViewHolder.mHelpButton.setImageDrawable(helpMsg.icon);
			if (ConfigUtil.isShowHelpInfos()) {
				helpViewHolder.mTitleLayout.post(new Runnable() {
					@Override
					public void run() {
						// 扩大HelpButton可点击区域
						Rect delegateArea = new Rect();
						ImageButton delegate = helpViewHolder.mHelpButton;
						delegate.getHitRect(delegateArea);
						
						delegateArea.left -= 100;
						delegateArea.right += 100;
						delegateArea.top -= 20;
						delegateArea.bottom += 20;
						
						TouchDelegate expandedArea = new TouchDelegate(delegateArea, delegate);
						if (View.class.isInstance(delegate.getParent())) {
							((View) delegate.getParent()).setTouchDelegate(expandedArea);
						}
					}
				});
				helpViewHolder.mHelpButton.setVisibility(View.GONE);
				helpViewHolder.mHelpButton.setOnClickListener(helpMsg.iconCallback);
			} else {
				helpViewHolder.mHelpButton.setVisibility(View.GONE);
			}
			if(helpMsg.ifShowText){
				helpViewHolder.content.setText(LanguageConvertor.toLocale("您可以这样问我"));
				helpViewHolder.mTextDespOne.setVisibility(View.VISIBLE);
				helpViewHolder.mTextDespTwo.setVisibility(View.VISIBLE);
			}else {
				helpViewHolder.content.setText(LanguageConvertor.toLocale(""));
				helpViewHolder.mTextDespOne.setVisibility(View.GONE);
				helpViewHolder.mTextDespTwo.setVisibility(View.GONE);
			}
			if (ConfigUtil.isShowSettings()) {
				helpViewHolder.exIcon.setVisibility(View.VISIBLE);
				helpViewHolder.exIcon.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent startRecordSetting = new Intent(AppLogicBase.getApp(), MainActivity.class);
						startRecordSetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						AppLogicBase.getApp().startActivity(startRecordSetting);
						WinRecord.getInstance().dismiss();
					}
				});
			} else {
				helpViewHolder.exIcon.setVisibility(View.GONE);
			}
		} else	if (msgType == ChatMessage.TYPE_FROM_SYS_CONTACT) {
			if (viewHolder.list.getAdapter() == null) {
				BaseAdapter adapter = new ChatContactListAdapter(mContext, chatMsg.items);
				mListAdapterRef = new WeakReference<BaseAdapter>(adapter);
				viewHolder.list.setAdapter(adapter);
			} else {
				((ChatContactListAdapter) viewHolder.list.getAdapter()).setChatListItem(chatMsg.items);
				mListAdapterRef = new WeakReference<BaseAdapter>((BaseAdapter) viewHolder.list.getAdapter());
			}

			viewHolder.list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (mChatMsgListener != null) {
						mChatMsgListener.onListMsgItemClicked(ChatMessage.TYPE_FROM_SYS_CONTACT, position);
					}
				}
			});

		/*
		 * if (msgType == ChatMessage.TYPE_FROM_SYS_WX_PICKER) {
		 * viewHolder.title.setText(chatMsg.title); if
		 * (viewHolder.list.getAdapter() == null) { BaseAdapter adapter = new
		 * ChatWxContactListAdapter(mContext, chatMsg.wxItems);
		 * viewHolder.list.setAdapter(adapter); } else {
		 * ((ChatWxContactListAdapter)
		 * viewHolder.list.getAdapter()).setChatListItem(chatMsg.wxItems);
		 * mListAdapterRef = new WeakReference<BaseAdapter>((BaseAdapter)
		 * viewHolder.list.getAdapter()); }
		 * 
		 * // TODO 硬编码 ! // LayoutParams oralParams =
		 * viewHolder.list.getLayoutParams(); // oralParams.height =
		 * mContext.getResources().getDimensionPixelSize(R.dimen.y320);
		 * 
		 * LayoutParams oralParams = viewHolder.list.getLayoutParams(); if
		 * (chatMsg.wxItems.size() == 1) { oralParams.height =
		 * mContext.getResources().getDimensionPixelSize(R.dimen.y100); } else
		 * if (chatMsg.wxItems.size() == 2) { oralParams.height =
		 * mContext.getResources().getDimensionPixelSize(R.dimen.y200); } else {
		 * oralParams.height =
		 * mContext.getResources().getDimensionPixelSize(R.dimen.y300); }
		 * viewHolder.list.setLayoutParams(oralParams);
		 * 
		 * viewHolder.list.setOnItemClickListener(new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) { if (mChatMsgListener != null) {
		 * mChatMsgListener.onListMsgItemClicked(ChatMessage.
		 * TYPE_FROM_SYS_WX_PICKER, position); } } }); }
		 */

		} else if (msgType == WeatherMessage.TYPE_FROM_SYS_WEATHER) {
//			if (ScreenUtil.isLargeScreen(dialog)) {
//				WeatherViewHolderLarge wvhl = (WeatherViewHolderLarge) viewHolder;
//				WeatherMessage m = (WeatherMessage) chatMsg;
//				WeatherRefresher.getInstance().updateData(m.mWeatherInfos, wvhl);
//			} else {
				WeatherViewHolder v = (WeatherViewHolder) viewHolder;
				WeatherMessage m = (WeatherMessage) chatMsg;
				WeatherRefresher.getInstance().updateData(m.mWeatherInfos, v);
//			}
		} else if (msgType == StockMessage.TYPE_FROM_SYS_STOCK) {
			StockViewHolder v = (StockViewHolder) viewHolder;
			StockMessage m = (StockMessage) chatMsg;
			StockRefresher.getInstance().updateData(m.mStockInfo, v);
		} else if (msgType == ChatMessage.TYPE_FROM_SYS_BIND_SERVICE) {
			BindDeviceViewHolder bindDeviceViewHolder = (BindDeviceViewHolder) viewHolder;
			BindDeviceMsg bindDeviceMsg = ((BindDeviceMsg) chatMsg);
			int h = (int) LayouUtil.getDimen("m160");
			try {
				Bitmap qrCodeBitmap;
				Bitmap imageBitmap;
				if (BitmapCache.getInstance().getBitmap(mQrCodeKey) != null) {
					qrCodeBitmap = BitmapCache.getInstance().getBitmap(mQrCodeKey);
				} else {
					qrCodeBitmap = QRUtil.createQRCodeBitmap(bindDeviceMsg.qrCode, h);
					mQrCodeKey = bindDeviceMsg.qrCode + qrCodeBitmap.getWidth() + qrCodeBitmap.getHeight();
					BitmapCache.getInstance().putBitmap(mQrCodeKey, qrCodeBitmap);
				}
				if (BitmapCache.getInstance().getBitmap(mImageKey) != null) {
					imageBitmap = BitmapCache.getInstance().getBitmap(mImageKey);
				} else {
					imageBitmap = Bitmap.createScaledBitmap(ImageUtils.getBitmap(bindDeviceMsg.imageUrl, h * 2, h), h * 2, h, true);
					mImageKey = GlobalContext.get().getSharedPreferences("txz", Context.MODE_PRIVATE).getString("keyBindDeviceImageCRC32", "") + imageBitmap.getWidth() + imageBitmap.getHeight();
					LogUtil.d("skyward " + mImageKey);
					BitmapCache.getInstance().putBitmap(mImageKey, imageBitmap);
				}
				bindDeviceViewHolder.mIvQrCode.setImageBitmap(qrCodeBitmap);
				bindDeviceViewHolder.mImageView.setImageBitmap(imageBitmap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (msgType == ChatMessage.TYPE_FROM_SYS_QRCODE) {
			QrCodeViewHolder q = (QrCodeViewHolder) viewHolder;
			int h = (int) LayouUtil.getDimen("y150");
			try {
				Bitmap bitmap = QRUtil.createQRCodeBitmap(chatMsg.text, h);
				q.mImageView.setImageBitmap(bitmap);
			} catch (WriterException e) {
			}
		} else if (msgType == ChatMessage.TYPE_FROM_SYS_HELP_TIPS) {
			HelpTipViewHolder v = (HelpTipViewHolder) viewHolder;
			HelpTipMsg m = (HelpTipMsg) chatMsg;
			//v.content.setText(m.mTitle);
			v.llContent.removeAllViews();
			v.llContent.setGravity(Gravity.CENTER_HORIZONTAL);

			//int itemHeight = (int)(com.txznet.comm.ui.util.ConfigUtil.getDisplayLvItemH(false) * 0.8f);

			for (int i = 0 ; i < m.mHelpTipBeen.size() ; i++) {
				HelpTipBean bean = m.mHelpTipBeen.get(i);
				View view = LayoutInflater.from(mContext).inflate(R.layout.help_tip_view_item,null);
				ImageView icon = ((ImageView)view.findViewById(R.id.icon));
				LinearLayout.LayoutParams iconLayoutParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
				if(LayouUtil.getDrawable(bean.resId) == null){
					ImageLoader.getInstance().displayImage(
							"file://" + bean.resId, new ImageViewAware(icon));
				}else {
					icon.setImageDrawable(LayouUtil.getDrawable(bean.resId));
				}
				TextView title = ((TextView)view.findViewById(R.id.title));
				title.setText(LanguageConvertor.toLocale(bean.label.replaceAll("“","").replaceAll("”", "")));
//				((TextView)view.findViewById(R.id.intro)).setTextColor(Color.WHITE);
				int textSize = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_HELP_TEXT_SIZE,0);
				if(textSize == 0){
					textSize = (int)LayouUtil.getDimen("m12");
				}
				LinearLayout.LayoutParams params;
				if (i == 2 || m.mHelpTipBeen.size() == 2) {
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
					icon.setScaleX(8f/9f);
					icon.setScaleY(8f/9f);
					icon.setAlpha(alpha / 3);
					title.setAlpha(alpha / 3);
					TextViewUtil.setTextSize(title, textSize + (int)LayouUtil.getDimen("m12") *(alpha / 3));
					params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				}
				v.llContent.addView(view,params);
			}
		} else if (msgType == ChatMessage.TYPE_FROM_SYS_COMPETITION_DETAIL) {
			CompetitionDetailViewHolder detailViewHolder = (CompetitionDetailViewHolder) viewHolder;
			CompetitionMsg viewData = (CompetitionMsg) chatMsg;

			detailViewHolder.tvHomeTeam.setText(LanguageConvertor.toLocale(viewData.mCompetitionBean.mHomeTeam.mName));
			detailViewHolder.tvLabel.setText(LanguageConvertor.toLocale(viewData.mCompetitionBean.mRoundType + DateUtils.getDayQuantum(viewData.mCompetitionBean.mStartTimeStamp * 1000L)));
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日  E  HH:mm");
			detailViewHolder.tvTime.setText(LanguageConvertor.toLocale(formatter.format(new Date(viewData.mCompetitionBean.mStartTimeStamp * 1000L))));

			if (TextUtils.equals("未开始", viewData.mCompetitionBean.mPeriod)) {
				detailViewHolder.tvPeriod.setTextColor(Color.parseColor("#FFFFFFFF"));
				detailViewHolder.tvGoal.setText("VS");
				detailViewHolder.tvPeriod.setText(LanguageConvertor.toLocale(viewData.mCompetitionBean.mPeriod));
				detailViewHolder.tvPeriod.setBackground(LayouUtil.getDrawable("competition_not_started_detail"));
			} else {
				detailViewHolder.tvGoal.setText(LanguageConvertor.toLocale(viewData.mCompetitionBean.mHomeTeam.mGoal + " : " + viewData.mCompetitionBean.mAwayTeam.mGoal));
				detailViewHolder.tvPeriod.setText(LanguageConvertor.toLocale(viewData.mCompetitionBean.mPeriod));
				if (TextUtils.equals("进行中", viewData.mCompetitionBean.mPeriod)) {
					detailViewHolder.tvPeriod.setTextColor(Color.parseColor("#FFFFFFFF"));
					detailViewHolder.tvPeriod.setBackground(LayouUtil.getDrawable("competition_under_way_detail"));
				} else if (TextUtils.equals("已结束", viewData.mCompetitionBean.mPeriod)) {
					detailViewHolder.tvPeriod.setBackground(LayouUtil.getDrawable("competition_ended_detail"));
					detailViewHolder.tvPeriod.setTextColor(Color.parseColor("#80FFFFFF"));
				} else {
					detailViewHolder.tvPeriod.setTextColor(Color.parseColor("#80FFFFFF"));
					detailViewHolder.tvPeriod.setBackground(null);
				}
			}

			ImageLoader.getInstance().displayImage(viewData.mCompetitionBean.mHomeTeam.mLogo, detailViewHolder.ivHomeTeam);
			ImageLoader.getInstance().displayImage(viewData.mCompetitionBean.mAwayTeam.mLogo, detailViewHolder.ivAwayTeam);
			detailViewHolder.tvAwayTeam.setText(LanguageConvertor.toLocale(viewData.mCompetitionBean.mAwayTeam.mName));
		}

		if (chatMsg instanceof BaseDisplayMsg) {
			final DisplayListViewHolder ph = (DisplayListViewHolder) viewHolder;
			BaseDisplayMsg pm = (BaseDisplayMsg) chatMsg;
			List itemList = pm.mItemList;
			if (itemList == null || itemList.size() < 1) {
				if (pm instanceof PoiMsg) {
					ph.mNoResultTv.setText(LanguageConvertor.toLocale(getNoResultBuilderTxt(pm.mKeywords).toString()));
				} else if (pm instanceof MusicMsg) {
					String keywords = pm.mKeywords;
					if (TextUtils.isEmpty(keywords)) {
						keywords = "没有找到相关结果";
					}
					ph.mNoResultTv.setText(LanguageConvertor.toLocale(keywords));
				} else if (pm instanceof WxContactMsg) {
					ph.mNoResultTv.setText(LanguageConvertor.toLocale("未找到微信联系人"));
				}
				ph.mDisplayLv.setVisibility(View.GONE);
				ph.mNoResultTv.setVisibility(View.VISIBLE);
				if (chatMsg instanceof PoiMsg) {
					convertView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
									"txz.record.ui.event.display.tip", null, null);
						}
					});
				}
			}
		}
		startAnimation(position, convertView);
		if (chatMsg instanceof PluginMessage) {
			PluginData pData = ((PluginMessage) chatMsg).mPluginData;
			if (pData != null) {
				return pData.mView;
			}
		}

		return convertView;
	}

	public void notifyUpdateProgress(int val) {
		if (mChatMsgs.size() > 0) {
			ChatMessage msg = mChatMsgs.get(mChatMsgs.size() - 1);
			if (msg.type == ChatMessage.TYPE_FROM_SYS_CONTACT) {
				if (msg.items != null && msg.items.size() > 0) {
					if (val >= 0 && val <= 100) {
						msg.items.get(0).curPrg = val;
						msg.items.get(0).shouldWaiting = true;
					} else {
						msg.items.get(0).shouldWaiting = false;
					}
					if (mListAdapterRef != null) {
						BaseAdapter adapter = mListAdapterRef.get();
						if (adapter != null) {
							adapter.notifyDataSetChanged();
						}
					}
				}
			}

			if (msg instanceof BaseDisplayMsg) {
				BaseDisplayMsg bdm = (BaseDisplayMsg) msg;
				if (bdm.mItemList != null && bdm.mItemList.size() > 0) {
					if (val >= 0 && val <= 100) {
						((DisplayItem) bdm.mItemList.get(0)).curPrg = val;
						((DisplayItem) bdm.mItemList.get(0)).shouldWaiting = true;
					} else {
						((DisplayItem) bdm.mItemList.get(0)).shouldWaiting = false;
					}

					if (mDisplayAdapterRef != null) {
						ChatDisplayAdapter adapter = mDisplayAdapterRef.get();
						if (adapter != null) {
							adapter.notifyDataSetChanged();
						}
					}
				}
			}
		}
	}

	private ChatMsgListener mChatMsgListener;

	public void setChatMsgListener(ChatMsgListener listener) {
		this.mChatMsgListener = listener;
	}

	public interface ChatMsgListener {
		void onListMsgItemClicked(int msgType, int index);

		void onListMsgCancel(int msgType);
	}

	private SpannableStringBuilder getNoResultBuilderTxt(String keywords) {
		int length = keywords.length();
		keywords = "没有找到" + keywords + "相关结果，请再说一次或点击本消息手动修改";
		SpannableStringBuilder ssb = new SpannableStringBuilder(keywords);
		ForegroundColorSpan norfcs = new ForegroundColorSpan(Color.WHITE);
		ForegroundColorSpan keyfcs = new ForegroundColorSpan(Color.parseColor("#27d7fd"));
		ssb.setSpan(norfcs, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(keyfcs, 4, 4 + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(norfcs, 4 + length, keywords.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ssb;
	}

	/**
	 * 重置动画
	 */
	public void resetAnimation() {
		isFrist.clear();
	}
}