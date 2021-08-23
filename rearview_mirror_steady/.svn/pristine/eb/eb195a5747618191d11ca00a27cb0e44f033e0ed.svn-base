package com.txznet.comm.ui.theme.test.winlayout.inner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.SkillfulReminding;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.VersionManager;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.view.ChatFromSysView;
import com.txznet.comm.ui.theme.test.view.ChatToSysView;
import com.txznet.comm.ui.theme.test.view.HelpButtonView;
import com.txznet.comm.ui.theme.test.view.SkilledRemaindView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.ChatFromSysViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.sdk.TXZResourceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 半屏界面的winlayout
 */
public class WinLayoutHalf extends IWinLayout {

	private static WinLayoutHalf sInstance = new WinLayoutHalf();


	private WinLayoutHalf() {
	}

	public static WinLayoutHalf getInstance() {
		return sInstance;
	}

	private RelativeLayout mRootLayout;
	private FrameLayout mFlFullContent;
	private LinearLayout mFlHalfContent;
	private FrameLayout robotCount;
	private FrameLayout textCount;
	private FrameLayout helpCount;
	private LinearLayout mLlHalfContent;
	private FrameLayout mFlBackground;
	private FrameLayout fullContent;
	private FrameLayout fullBackground;
	private View.OnClickListener mClickListener;
	private HelpButtonView mHelp;

	private FrameLayout lFRemind;
	private SkilledRemaindView skilledRemaindView;

	public static int cardViewHeight;    //竖屏天气、股票、电影、长文本四个界面，展示时的语音dialog高度

	//private mBroadcastReceiver mReceicver;

	private int countMargin;    //全屏内容左右外边距
	private int helpIconSize;    //语音中心图标大小
	private int helpRightMargin;    //语音中心图标右边距
	private int helpLeftMargin;    //语音中心图标右边距

	private int remindViewWidth;    //熟手模式提示框宽度
	private int remindViewHeight;    //熟手模式提示框高度

    private int unit;

    //帮助全部内容
    private List<String> helpDetailList = new ArrayList<>();

	/**
	 * 半屏模式
	 */
	public static final int LAYOUT_HALF_SCREEN = 1;

	/**
	 * 全屏模式
	 */
	public static final int LAYOUT_FULL_SCREEN = 2;


    /**
     * 伪全屏模式，竖屏天气、股票、电影、长文本
     */
    public static final int LAYOUT_FULL_SCREEN_CARD = 3;


    /**
     * 真·全屏模式，帮助及帮助详情界面完全中满屏幕，不显示语音动画
     */
    public static final int LAYOUT_FULL_SCREEN_HELP = 4;

	private void initSize(){
	    if (WinLayout.isVertScreen){
            unit = ViewParamsUtil.unit;
            countMargin = 2 * unit;
            helpIconSize = 5 * unit;
            helpRightMargin = 2 * unit;
            helpLeftMargin = unit;

            remindViewWidth = 80 * unit;
            remindViewHeight = 6 * unit;
            if (SizeConfig.screenWidth < 900 || remindViewWidth > SizeConfig.screenWidth){
                remindViewWidth = SizeConfig.screenWidth;
            }
        }else {
            unit = ViewParamsUtil.unit;
            countMargin = 5 * unit;
            helpIconSize = 5 * unit;
            helpRightMargin = 3 * unit;
            helpLeftMargin = unit;

            remindViewWidth = 80 * unit;
            remindViewHeight = 6 * unit;
			if (SizeConfig.screenWidth < 900 || remindViewWidth > SizeConfig.screenWidth){
				remindViewWidth = SizeConfig.screenWidth;
			}
        }

	}

	/**
	 * 添加录音图标
	 */
	@Override
	public void addRecordView(View recordView) {
	    if (recordView != null){
            // 这种形式的录音动画直接在聊天View里面的
            ViewGroup parent = (ViewGroup) recordView.getParent();
            if (parent!= null){
                parent.removeView(recordView);
            }

            //竖屏动画适当缩放
            if (WinLayout.isVertScreen){
                float scaling = WinLayout.getInstance().recordScaling(WinLayout.getInstance().halfHeight);
                int width = (int)(WinLayout.getInstance().halfHeight * scaling);
                FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(width,width,Gravity.CENTER);
                layoutParam.leftMargin = 2 * unit;
                layoutParam.rightMargin = 2 * unit;
                robotCount.addView(recordView,layoutParam);
            }else {
                int width = WinLayout.getInstance().halfHeight*160/140;
                FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(width,FrameLayout.LayoutParams.MATCH_PARENT,Gravity.CENTER);
                layoutParam.leftMargin = width/6;
                layoutParam.rightMargin = width/6;
                robotCount.addView(recordView,layoutParam);
            }

            showRecord();
        }
	}

	@Override
	public Object removeLastView() {
		return null;
	}

	/**
	 *
	 * @param type
	 */
	public void updateScreen(int type) {
		switch (type) {
		case LAYOUT_HALF_SCREEN:
            if (mFlBackground != null && mLlHalfContent != null) {
                fullContent.setVisibility(View.GONE);
                fullBackground.setVisibility(View.VISIBLE);
                mFlBackground.setVisibility(View.GONE);
            }
            WinLayout.getInstance().updateScreenType(StyleConfig.STYLE_ROBOT_HALF_SCREES);
            break;
		case LAYOUT_FULL_SCREEN:
            LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    SizeConfig.screenHeight-WinLayout.getInstance().halfHeight);
//			LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//					0,1);
            mFlBackground.setLayoutParams(lLayoutParams);
			if (mFlBackground != null && mLlHalfContent != null) {
				fullContent.setVisibility(View.VISIBLE);
				fullBackground.setVisibility(View.GONE);
				mFlBackground.setVisibility(View.VISIBLE);
			}
			WinLayout.getInstance().updateScreenType(StyleConfig.STYLE_ROBOT_FULL_SCREES);
			break;
		case LAYOUT_FULL_SCREEN_CARD:
            lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    cardViewHeight - WinLayout.getInstance().halfHeight);
            mFlBackground.setLayoutParams(lLayoutParams);
            if (mFlBackground != null && mLlHalfContent != null) {
                fullContent.setVisibility(View.VISIBLE);
                fullBackground.setVisibility(View.GONE);
                mFlBackground.setVisibility(View.VISIBLE);
            }
            if (!WinLayout.getInstance().isHalfBottom){    //对话框在上面
                RecordWin2Manager.getInstance().updateDisplayArea(0,0,
                        WindowManager.LayoutParams.MATCH_PARENT, cardViewHeight);
            }else {    //对话框在下面
                RecordWin2Manager.getInstance().updateDisplayArea(0,
                        SizeConfig.screenHeight - cardViewHeight,
                        WindowManager.LayoutParams.MATCH_PARENT, cardViewHeight);
            }
            break;
            case LAYOUT_FULL_SCREEN_HELP:
                lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                mFlBackground.setLayoutParams(lLayoutParams);
                if (mFlBackground != null && mLlHalfContent != null) {
                    fullContent.setVisibility(View.VISIBLE);
                    fullBackground.setVisibility(View.GONE);
                    mFlBackground.setVisibility(View.VISIBLE);
                }
                WinLayout.getInstance().updateScreenType(StyleConfig.STYLE_ROBOT_FULL_SCREES);
                break;
            default:
			break;
		}
	}


	@Override
	public View get() {
		return mRootLayout;
	}

	/**
	 * 添加View到对应的地方
	 */
	@Override
	public Object addView(int targetView, View view, ViewGroup.LayoutParams layoutParams) {

		Object tag = view.getTag();
        LogUtil.logd(WinLayout.logTag+ "addView: --tag:"+tag);
		if (tag instanceof Integer && (Integer)tag == ViewData.TYPE_FULL_LIST_COMPETITION){
			//赛事查询列表没有反馈语的展示
			WinLayout.isHideView = false;
		}
		switch (targetView) {
		case RecordWinController.TARGET_CONTENT_CHAT:
			if (tag instanceof Integer) {
				Integer type = (Integer) tag;
				switch (type) {
                    case ViewData.TYPE_CHAT_HELP_TIPS:
						//不展示的页面需要将vTips清空，防止显示在下一个界面上
						WinLayout.getInstance().vTips = null;
						return null;
					case ViewData.TYPE_CHAT_OFFLINE_PROMOTE:
					case ViewData.TYPE_CHAT_WEATHER:
					case ViewData.TYPE_CHAT_SHARE:
					case ViewData.TYPE_CHAT_BIND_DEVICE_QRCODE:
					case ViewData.TYPE_CHAT_CONSTELLATION_FORTUNE:
					case ViewData.TYPE_CHAT_CONSTELLATION_MATCHING:
					case ViewData.TYPE_CHAT_FEEDBACK:
					case ViewData.TYPE_CHAT_FROM_SYS_INTERRUPT:
					case ViewData.TYPE_CHAT_COMPETITION_DETAIL:
						if (skilledRemaindView != null) {
							//隐藏熟手模式提示框
							skilledRemaindView.hide();
						}
						setGuideText();
						mFlBackground.setOnClickListener(null);
						fullContent.removeAllViews();
						if (WinLayout.isVertScreen){
                            updateScreen(LAYOUT_FULL_SCREEN_CARD);
                        }else {
                            updateScreen(LAYOUT_FULL_SCREEN);
                        }
						FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
						if (type == ViewData.TYPE_CHAT_FROM_SYS_INTERRUPT){
							//ChatContentView mChatContentView = new ChatContentView(GlobalContext.get());
							//mChatContentView.addView(view);
							ScrollView mScrollView = new ScrollView(GlobalContext.get());
							mScrollView.setFillViewport(true);
							mScrollView.setVerticalScrollBarEnabled(false);
							mScrollView.addView(view);
							fullContent.addView(mScrollView, layoutParams3);

							mFlHalfContent.setBackgroundColor(Color.parseColor("#00000000"));
							mLlHalfContent.setBackground(LayouUtil.getDrawable("bg"));
							return null;
						}

						fullContent.addView(view, layoutParams3);

						mFlHalfContent.setBackgroundColor(Color.parseColor("#00000000"));
						mLlHalfContent.setBackground(LayouUtil.getDrawable("bg"));
						return null;
				}
			}
            //WinLayout.getInstance().halfHeight = SizeConfig.screenHeight / 5;
            if (skilledRemaindView != null) {
                skilledRemaindView.show(remindViewHeight);
            }
            showRecord();
            mFlBackground.setOnClickListener(mClickListener);
            fullContent.removeAllViews();
            textCount.removeAllViews();
            updateScreen(LAYOUT_HALF_SCREEN);
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            textCount.addView(view, layoutParams2);

            mFlHalfContent.setBackground(LayouUtil.getDrawable("bg"));
            mLlHalfContent.setBackgroundColor(Color.parseColor("#00000000"));
			break;
		case RecordWinController.TARGET_CONTENT_FULL:
			//帮助界面需要全屏
            boolean isShowHelp = false;
			if (tag instanceof Integer) {
				Integer type = (Integer) tag;
				switch (type) {
					case ViewData.TYPE_FULL_LIST_HELP:
					case ViewData.TYPE_FULL_LIST_HELP_DETAIL:
					case ViewData.TYPE_FULL_LIST_HELP_IMAGE_DETAIL:
                        isShowHelp = true;
						hideRecord();
						break;
				}
			}else {
				showRecord();
			}
            if (WinLayout.isVertScreen && tag instanceof Integer && (Integer)tag == ViewData.TYPE_FULL_LIST_CINEMA){
                updateScreen(LAYOUT_FULL_SCREEN_CARD);
            }else if (isShowHelp){
                updateScreen(LAYOUT_FULL_SCREEN_HELP);
            }else {
                updateScreen(LAYOUT_FULL_SCREEN);
            }
			if (skilledRemaindView != null) {
				//隐藏熟手模式提示框
				skilledRemaindView.hide();
			}
			//清除之前的提示语
			textCount.removeAllViews();
			//添加引导语
			setGuideText();

			mFlBackground.setOnClickListener(null);
			fullContent.removeAllViews();
			FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
			fullContent.addView(view, layoutParams3);

			mFlHalfContent.setBackgroundColor(Color.parseColor("#00000000"));
			mLlHalfContent.setBackground(LayouUtil.getDrawable("bg"));
			break;
		case RecordWinController.TARGET_VIEW_MIC:
			break;
		default:
			break;
		}

		return null;
	}

	/**
	 * 释放内存，界面关闭时会调用
	 */
	@Override
	public void release() {
        LogUtil.logd(WinLayout.logTag+ "release: half");

		if (mRootLayout != null){
			mRootLayout.removeAllViews();
			mRootLayout = null;
		}
		if (robotCount != null){
			robotCount.removeAllViews();
			robotCount = null;
		}
        //GlobalContext.get().unregisterReceiver(mReceicver);
	}

	/**
	 * 重置聊天记录，界面关闭时会调用
	 */
	@Override
	public void reset() {
		//WinLayout.getInstance().halfHeight = SizeConfig.screenHeight / 5;
		updateScreen(LAYOUT_HALF_SCREEN);
		if (skilledRemaindView != null && skilledRemaindView.getVisibility() == View.VISIBLE){
			SkillfulReminding.getInstance().reduceOnce();
			skilledRemaindView.hide();
		}
		if (textCount != null){
            textCount.removeAllViews();
        }
	}

	@Override
	public void init() {
        /*IntentFilter mFilter = new IntentFilter();
        mFilter.addAction("com.txznet.txz.record.show");
        mFilter.addAction("com.txznet.txz.record.dismiss");*/
        /*mReceicver = new mBroadcastReceiver();
        GlobalContext.get().registerReceiver(mReceicver,mFilter);*/

		//初始化页面列表数量
		SizeConfig.getInstance().init(4);
		super.init();
		initSize();
		if (mRootLayout == null) {

			LogUtil.logd(WinLayout.logTag+"WinLayoutHalf:"+"init weightRecord:"+WinLayout.getInstance().halfHeight);
			// 初始化一些配置等
			if (!WinLayout.getInstance().isHalfBottom){
				mRootLayout = (RelativeLayout) LayouUtil.getView("win_layout_half_top");
			}else {
				mRootLayout = (RelativeLayout) LayouUtil.getView("win_layout_half");
			}
			mLlHalfContent = (LinearLayout) LayouUtil.findViewByName("llHalfContent",mRootLayout);
			mFlHalfContent = (LinearLayout) LayouUtil.findViewByName("lHalfContent",mRootLayout);
			robotCount = (FrameLayout) LayouUtil.findViewByName("robotCount",mFlHalfContent);
			textCount = (FrameLayout) LayouUtil.findViewByName("textCount",mFlHalfContent);
			helpCount = (FrameLayout) LayouUtil.findViewByName("helpCount",mFlHalfContent);
			mFlBackground = (FrameLayout) LayouUtil.findViewByName("flBackground",mRootLayout);
			fullContent = (FrameLayout) LayouUtil.findViewByName("fullContent",mFlBackground);
			fullBackground = (FrameLayout) LayouUtil.findViewByName("fullBackground",mFlBackground);
			mFlFullContent = (FrameLayout) LayouUtil.findViewByName("flFullContent",mRootLayout);

			lFRemind = (FrameLayout)LayouUtil.findViewByName("lFRemind",mRootLayout);

			LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,WinLayout.getInstance().halfHeight);
			mFlHalfContent.setLayoutParams(lLayoutParams);
			mFlHalfContent.setBackground(LayouUtil.getDrawable("bg"));
			mLlHalfContent.setBackgroundColor(Color.parseColor("#FF000000"));
			mFlBackground.setPadding(countMargin, 0, countMargin, 0);

			lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.screenHeight-WinLayout.getInstance().halfHeight);
			mFlBackground.setLayoutParams(lLayoutParams);

			skilledRemaindView = new SkilledRemaindView(GlobalContext.get());
			skilledRemaindView.setBackground(LayouUtil.getDrawable(WinLayout.getInstance().isHalfBottom?"white_top_range_layout":"white_bottom_range_layout"));
			//FrameLayout.LayoutParams rlayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
			FrameLayout.LayoutParams rlayoutParams = new FrameLayout.LayoutParams(remindViewWidth,remindViewHeight);
			rlayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
			lFRemind.addView(skilledRemaindView,rlayoutParams);
			lFRemind.setVisibility(View.VISIBLE);

			lLayoutParams = (LinearLayout.LayoutParams) helpCount.getLayoutParams();
			lLayoutParams.leftMargin = helpLeftMargin;
            helpCount.setLayoutParams(lLayoutParams);

			mHelp = new HelpButtonView(GlobalContext.get(),helpIconSize,helpIconSize);
			FrameLayout.LayoutParams  layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL);
			layoutParams.rightMargin = helpRightMargin;
			helpCount.addView(mHelp, layoutParams);
			mClickListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v == mFlBackground) {
						RecordWin2Manager.getInstance().dismiss();
					}
				}
			};

			mFlBackground.setOnClickListener(mClickListener);

			mRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					mRootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
			});
            //更新帮助按钮状态
            mHelp.showByConfig();

			//注册帮助按钮状态监听，更新帮助按钮状态
			com.txznet.comm.remote.util.ConfigUtil.registerIconStateChangeListener(new com.txznet.comm.remote.util.ConfigUtil.IconStateChangeListener() {
				@Override
				public void onStateChanged(int i, boolean b) {
                    UI2Manager.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            mHelp.showByConfig();

                        }
                    },0);
				}
			});

		}
	}

	@Override
	public void setBackground(Drawable drawable) {
		get().setBackground(drawable);
	}


	//处理语音界面打开状态，将界面切换到对话界面
	public void showChatView(){
        //WinLayout.getInstance().halfHeight = SizeConfig.screenHeight / 5;
        if (skilledRemaindView != null) {
            skilledRemaindView.show(remindViewHeight);
        }
        showRecord();
        mFlBackground.setOnClickListener(mClickListener);
        fullContent.removeAllViews();
        textCount.removeAllViews();

        updateScreen(LAYOUT_HALF_SCREEN);

        mFlHalfContent.setBackground(LayouUtil.getDrawable("bg"));
        mLlHalfContent.setBackgroundColor(Color.parseColor("#00000000"));

        //问候语设置为空的时候，并且textCount内容为空是，显示帮助引导语
        if (!TXZConfigManager.getInstance().hasDefaultWelcomeMessage()){
            LogUtil.logd(WinLayout.logTag+ "isHasDefaultWelcomeMessage: "+TXZConfigManager.getInstance().hasDefaultWelcomeMessage());
            showHintWithoutGreeting();
        }

    }

	//全屏展示，去掉底部动画
	public void hideRecord(){
		if (mFlHalfContent.getVisibility() == View.VISIBLE) {
			mFlHalfContent.setVisibility(View.GONE);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
			mFlBackground.setLayoutParams(layoutParams);
			mFlBackground.setPadding(0, 0, 0, 0);
		}
	}

	//上面展示，显示底部动画
	public void showRecord(){
		if (mFlHalfContent.getVisibility() == View.GONE) {
			mFlHalfContent.setVisibility(View.VISIBLE);
			LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.screenHeight-WinLayout.getInstance().halfHeight);
			mFlBackground.setLayoutParams(lLayoutParams);
			mFlBackground.setPadding(countMargin, 0, countMargin, 0);
		}
	}

	//设置引导语
	public void setGuideText(){
		ChatFromSysViewData tipViewData = new ChatFromSysViewData();
        String  text = "";
		if (WinLayout.getInstance().vTips != null){
			text = WinLayout.getInstance().vTips;
            if (text.contains("；")){
                String[] texts = text.split("；");
                Random random = new Random();
                switch (texts.length){
                    case 3:
                        int i = random.nextInt(2);
                        text = "你可以说：“" + texts[i]+"”，“"+texts[i+1]+ "”";
                        break;
                    case 4:
                        i = random.nextInt(3);
                        text = "你可以说：“" + texts[i]+"”，“"+texts[i+1]+ "”";
                        break;
                    case 1:
                    case 2:
                        text = "你可以说：“" + text.replaceAll("；","”，“") + "”";
                        break;
                }
            }else {
				text = "你可以说：“" + text+ "”";
			}
			tipViewData.textContent = text;
			WinLayout.getInstance().vTips = null;
			ViewFactory.ViewAdapter viewAdapter = ChatFromSysView.getInstance().getView(tipViewData);
			LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			textCount.removeAllViews();
			textCount.addView(viewAdapter.view, llParams);
		}
	}

	//问候语为空的时候，显示一句引导文案
	private void showHintWithoutGreeting(){
	    if (helpDetailList != null && helpDetailList.size() == 0){
            //获取帮助界面所有内容
            TXZResourceManager.getInstance().getHelpDetailItems(new TXZResourceManager.OnGetHelpDetailCallback() {
                @Override
                public void onGetHelpDetail(String s) {
                    String content = s.replaceAll("\\\\","");
                    content = content.replace("[\"{","[{");
                    content = content.replace("\"}]","}]");
                    content = content.replace("\"{","{");
                    content = content.replace("}\"","}");
                    LogUtil.logd(WinLayout.logTag+ "onGetHelpDetail: "+content);

                    try {
                        JSONArray jsonArray = new JSONArray(content);
                        for (int i = 0;i < jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            JSONArray detailArray = jsonObject.getJSONArray("detailItems");
                            for (int j = 0;j < detailArray.length();j++){
                                JSONObject detailJObject = detailArray.getJSONObject(j);
                                //helpDetailArray[i][j] = detailJObject.getString("name");
                                String str = detailJObject.getString("name");
                                helpDetailList.add(str);
                                //LogUtil.logd(WinLayout.logTag+ "onGetHelpDetailItem: "+"--"+detailJObject.getString("name"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        LogUtil.loge(WinLayout.logTag+ "HelpDetail is not a JSON: "+content);
                    }
                    //语音界面第一次起来时会展示一句tts，此时不做显示(需要延时后判断)
					AppLogicBase.runOnUiGround(new Runnable() {
						@Override
						public void run() {
							if (textCount.getChildCount() == 0){
								showHelpHint();
							}
						}
					},100);
                }
            });
        }else {
            showHelpHint();
        }

    }

    //显示引导文案
    private void showHelpHint(){
        //生成展示文案
        Random random = new Random();
        String text = random.nextInt(2) == 0?"你可以说：":"试试说：";
        LogUtil.logd(WinLayout.logTag+ "showHintWithoutGreeting: "+helpDetailList.size());
        try {
            text += helpDetailList.get(random.nextInt(helpDetailList.size()));
        }catch (Exception e){
            LogUtil.loge(WinLayout.logTag+ "get helpDetailList error: "+e.getMessage());
        }

        //在对话框展示
        ChatFromSysViewData tipViewData = new ChatFromSysViewData();
        tipViewData.textContent = text;
        ViewFactory.ViewAdapter viewAdapter = ChatFromSysView.getInstance().getViewLine(tipViewData);
        FrameLayout.LayoutParams llParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        //textCount.removeAllViews();
        //updateScreenType(LAYOUT_HALF_SCREEN);
        textCount.addView(viewAdapter.view, llParams);

    }

}
