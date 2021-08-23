package com.txznet.txz.ui.win.help;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.zxing.WriterException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.loader.AppLogic;
import com.txznet.record.helper.ChatMsgFactory;
import com.txznet.record.ui.WinRecord;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.txz.R;
import com.txznet.txz.component.choice.IChoice;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.help.HelpDetail.HelpDetailItem;
import com.txznet.txz.ui.win.help.WinHelpDetailTops.PageHelper;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.StatusObervable.StatusObserver;
import com.txznet.txz.util.QRUtil;
import com.txznet.txz.util.runnables.Runnable2;

import android.graphics.Color;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 帮助第三级界面控制器
 *
 */
public class WinHelpDetailSelector implements IChoice<Void>{
//	PageHelper mPageHelper;
	MixPageHlper mixPageHlper;
	private boolean mIsSelecting;
	static boolean mHasWakeup;
	static int mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
	private static WinHelpDetailSelector instance = new WinHelpDetailSelector();

	//默认三级列表界面
	public static final int DETAIL_TYPE_LIST_NORMAL = 0;
	//显示图文详情界面
	public static final int DETAIL_TYPE_IMAGE_TEXT = 1;
	//显示更新的三级列表界面
	public static final int DETAIL_TYPE_LIST_NEW = 2;
	//显示图文和默认的三级列表模式
	public static final int DETAIL_TYPE_MIX = 3;

	private int mCurrentDetailType = DETAIL_TYPE_LIST_NORMAL;

	private boolean isFromFile = false;

	public static WinHelpDetailSelector getInstance() {
		return instance;
	}
	
	private WinHelpDetailSelector() {
		RecorderWin.OBSERVABLE.registerObserver(new StatusObserver() {

			@Override
			public void onShow() {
			}

			@Override
			public void onDismiss() {
				AppLogic.removeUiGroundCallback(mDismissRun);
				AppLogic.runOnUiGround(mDismissRun, 10);
			}
		});
	}
	
	Runnable mDismissRun = new Runnable() {
		
		@Override
		public void run() {
			dismiss();
		}
	};
	
	private List<HelpDetailItem> helpDetailItems;
	private List<HelpDetail.HelpDetailImg> helpDetailImgs;
	private String helpMsg="";
	private int curHelpPage = 0;
	private String mHelpLabel = "";
	private HelpDetail mHelpDetail;
	private boolean isHelpDetailNew = false;
	private boolean hasNet = true;
	
	/**
	 * 显示帮助三级界面
	 * @param helpDetail 帮助二级的详情
	 * @param isNew 帮助二级是否是新增的
	 * @param helpMsg 帮助二级类目
	 * @param curPage 当前帮助二级的页面
	 * @param isFromFile
	 */
	public void showHelpList(HelpDetail helpDetail, boolean isNew, String helpMsg, int curPage, boolean isFromFile){
		mCurrentDetailType = DETAIL_TYPE_LIST_NORMAL;
		this.hasNet = helpDetail.hasNet;
		showMixHelpInner(helpDetail,isNew,helpMsg,curPage,isFromFile);
	}

	/**
	 * 显示更新的指令，数据去除操作在获取的时候执行了
	 * @param helpDetail
	 * @param isNew
	 * @param helpMsg
	 * @param curPage
	 * @param isFromFile
	 */
	public void showHelpNewsList(HelpDetail helpDetail, boolean isNew, String helpMsg, int curPage, boolean isFromFile){
		mCurrentDetailType = DETAIL_TYPE_LIST_NEW;
		showMixHelpInner(helpDetail,isNew,helpMsg,curPage,isFromFile);
	}

	/**
	 * 显示图文的指令
	 * @param helpDetail
	 * @param isNew
	 * @param helpMsg
	 * @param curPage
	 * @param isFromFile
	 */
	public void showHelpImageText(HelpDetail helpDetail, boolean isNew, String helpMsg, int curPage, boolean isFromFile){
		mCurrentDetailType = DETAIL_TYPE_IMAGE_TEXT;
		showMixHelpInner(helpDetail,isNew,helpMsg,curPage,isFromFile);
	}

	/**
	 * 显示图片和指令
	 * @param helpDetail
	 * @param isNew
	 * @param helpMsg
	 * @param curPage
	 * @param isFromFile
	 */
	public void showMixHelp(HelpDetail helpDetail, boolean isNew, String helpMsg, int curPage, boolean isFromFile){
		mCurrentDetailType = DETAIL_TYPE_MIX;
		showMixHelpInner(helpDetail,isNew,helpMsg,curPage,isFromFile);
	}

	private void showMixHelpInner(HelpDetail helpDetail, boolean isNew, String helpMsg, int curPage, boolean isFromFile) {
		WinHelpManager.getInstance().updateCloseIconState(true);
		this.isFromFile = isFromFile;
		this.mHelpDetail = helpDetail;
		this.helpMsg = helpMsg;
		this.curHelpPage = curPage;
		this.isHelpDetailNew = isNew;
		if (this.mHelpDetail.detailItems == null) {
			this.helpDetailItems = new ArrayList<HelpDetailItem>();
		} else {
			this.helpDetailItems = this.mHelpDetail.detailItems;
		}
		if (this.mHelpDetail.detailImgs == null) {
			this.helpDetailImgs = new ArrayList<HelpDetail.HelpDetailImg>();
		} else {
			this.helpDetailImgs = this.mHelpDetail.detailImgs;
		}
		AsrManager.getInstance().cancel();
		TtsManager.getInstance().pause();
		ChoiceManager.getInstance().clearIsSelecting();
		mIsSelecting = true;
		this.mHelpLabel = this.mHelpDetail.name;
		if (!TextUtils.isEmpty(this.mHelpLabel)) {
			speakText(NativeData.getResPlaceholderString("RS_VOICE_HELP_DETAIL_ITEM_SPK","%ITEM%",this.mHelpLabel));
		}
		PageHelper mPageHelper = new PageHelper();
		if (mCurrentDetailType != DETAIL_TYPE_IMAGE_TEXT) {
			Integer pageSize = ChoiceManager.getInstance().getNumPageSize(TXZConfigManager.PageType.PAGE_TYPE_HELP_DETAIL_LIST.name());
			if (pageSize == null) {
				//留出一条显示label,显示的条目数量是正常列表的1.25倍
				pageSize = (int) (ChoiceManager.getInstance().getNumPageSize() * 1.25) - 1;
			}
			mPageHelper.reset(helpDetailItems.size(), pageSize);
		} else {
			mPageHelper.reset(0, 1);
		}

		PageHelper imgPageHelper = new PageHelper();
		if (mCurrentDetailType == DETAIL_TYPE_IMAGE_TEXT || mCurrentDetailType == DETAIL_TYPE_MIX) {
			imgPageHelper.reset(helpDetailImgs.size(), 1);
		} else {
			imgPageHelper.reset(0, 1);
		}

		mixPageHlper = new MixPageHlper();
		mixPageHlper.reset(mPageHelper, imgPageHelper);

		showMixHelp(0);

		beginWakeup();
	}

	private void showMixHelp(int page){
		int type = mixPageHlper.getPageType(page);
		if (type == MixPageHlper.TYPE_IMG) {
			showHelpImage();
		} else if (type == MixPageHlper.TYPE_LIST) {
			showHelpList();
		}
		int curPage = mixPageHlper.getCurPage(MixPageHlper.TYPE_MIX) + 1;
		int maxPage = mixPageHlper.getMaxPage(MixPageHlper.TYPE_MIX);
		LogUtil.d("QRCode：curPage:" + curPage + ",maxPage:" + maxPage + ",mHelpLabel:" + mHelpLabel);
		ReportUtil.doReport(new ReportUtil.Report.Builder()
				.setType("helplist")
				.setAction("changePage")
				.putExtra("label",mHelpLabel)
				.putExtra("curPage",curPage)
				.putExtra("maxPage",maxPage)
				.buildCommReport());
	}

	private void showHelpList() {

		int curPage = mixPageHlper.getCurPage(MixPageHlper.TYPE_LIST);
		int pageSize = mixPageHlper.getPageSize(MixPageHlper.TYPE_LIST);
		int sIndex = curPage * pageSize;
		if (helpDetailItems == null) {
			return;
		}

		final int c = helpDetailItems.size();
		if (sIndex >= c) {
			return;
		}

		JSONBuilder jBuilder = new JSONBuilder();
		jBuilder.put("type", 9);
		jBuilder.put("keywords", "帮助");
		jBuilder.put("label", mHelpLabel);
		jBuilder.put("prefix", helpMsg);
		jBuilder.put("curPage", mixPageHlper.getCurPage(MixPageHlper.TYPE_MIX));//只有1页时不显示翻页的
		int maxPage = mixPageHlper.getMaxPage(MixPageHlper.TYPE_MIX);
		jBuilder.put("maxPage", maxPage == 1 ? 0 : maxPage);

		ArrayList<HelpDetailItem> tmpList = new ArrayList<HelpDetailItem>();
		JSONArray jsonArray = new JSONArray();
		//插入列表条目
		JSONObject obj = new JSONBuilder()
		.put("title", mHelpDetail.name)
		.put("time", mHelpDetail.time)
		.put("isNew", false)
		.getJSONObject();
		jsonArray.put(obj);
		HelpDetailItem mHelpDetailItem = new HelpDetailItem();
		mHelpDetailItem.name = mHelpDetail.name;
		mHelpDetailItem.time = mHelpDetail.time;
		mHelpDetailItem.isNew = isHelpDetailNew;
		tmpList.add(mHelpDetailItem);
		for (int i = 0; i < pageSize; i++) {
			if (sIndex >= helpDetailItems.size()) {
				break;
			}

			mHelpDetailItem = helpDetailItems.get(sIndex);
			tmpList.add(mHelpDetailItem);
			obj = new JSONBuilder()
				.put("title", mHelpDetailItem.name)
				.put("time", mHelpDetailItem.time)
				.put("isNew", false)
				.put("netType",mHelpDetailItem.netType)
				.getJSONObject();
			jsonArray.put(obj);
			sIndex++;
		}
		jBuilder.put("helpDetails", jsonArray);
		jBuilder.put("hasNet",hasNet);
		jBuilder.put("count", jsonArray.length());

		 if (WinManager.getInstance().isRecordWin2()) {
		 // ui2.0框架直接发送数据,将type转换成9
			 jBuilder.put("type", 9);
			 RecorderWin.sendSelectorList(jBuilder.toString());
			 return;
		 }
		 
		 jBuilder.put("type", 7);
		 AppLogic.runOnUiGround(new Runnable2<JSONBuilder,ArrayList<HelpDetailItem>>(jBuilder,tmpList) {
				@Override
				public void run() {
					// 直接发送给本地界面
					WinRecord.getInstance().addMsg(ChatMsgFactory.createContainMsg(mP1.toString(), createView(mP2)));
				}
			}, 0);
		 
	}

	protected View createView(ArrayList<HelpDetailItem> tmpList) {
		int contentHeight = ScreenUtil.getDisplayLvItemH(false) * ScreenUtil.getVisbileCount();
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackgroundResource(R.drawable.white_range_layout);
		LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
		llLayout.addView(llContent,layoutParams);
		int itemHeight = (int)(contentHeight/(ScreenUtil.getVisbileCount()*1.25f));
		for (int i = 0; i < tmpList.size(); i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight);
			View itemView = null;
			if (i == 0) {
				itemView = createItemView(i,tmpList.get(i),true);
			}else {
				itemView = createItemView(i,tmpList.get(i),false);
			}
			llContent.addView(itemView,layoutParams);
		}
		return llLayout;
	}
	
	private View createItemView(int position,HelpDetailItem helpBean,boolean isLabel){
		RelativeLayout itemView = new RelativeLayout(GlobalContext.get());
		itemView.setTag(position);

		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.topMargin = (int)LayouUtil.getDimen("y2");
		layoutParams.bottomMargin = (int)LayouUtil.getDimen("y2");
		itemView.addView(flContent,layoutParams);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		flContent.addView(llContent, mFLayoutParams);
		
		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		llDetail.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		mLLayoutParams.leftMargin = (int) LayouUtil.getDimen("y15");
		llContent.addView(llDetail,mLLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.BOTTOM);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.CENTER;
		mLLayoutParams.rightMargin = (int) LayouUtil.getDimen("y6");
		llDetail.addView(tvContent,mLLayoutParams);
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,(int) Math.ceil(LayouUtil.getDimen("y1")));
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);

		if (isLabel) {
			TextViewUtil.setTextSize(tvContent, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_LABEL_ITEM_SIZE1));
			TextViewUtil.setTextColor(tvContent, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
		}else {
			TextViewUtil.setTextSize(tvContent, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE1));
			if (!hasNet && helpBean.netType == WinHelpDetailTops.CMD_NET_TYPE_NET){
				TextViewUtil.setTextColor(tvContent, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR3));
			} else {
				TextViewUtil.setTextColor(tvContent, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
			}
		}

		tvContent.setText(StringUtils.isEmpty(helpBean.name) ?"" : helpBean.name);

		divider.setVisibility(isLabel?View.VISIBLE:View.INVISIBLE);
		
		return itemView;
	}

	private void showHelpImage(){
		int curPage = mixPageHlper.getCurPage(MixPageHlper.TYPE_IMG);
		int pageSize = mixPageHlper.getPageSize(MixPageHlper.TYPE_IMG);
		int sIndex = curPage * pageSize;

		if (helpDetailImgs == null) {
			return;
		}

		final int c = helpDetailImgs.size();
		if (sIndex >= c) {
			return;
		}

		JSONBuilder jBuilder = new JSONBuilder();
		jBuilder.put("type", 10);
		jBuilder.put("keywords", "帮助");
		jBuilder.put("label", mHelpLabel);
		jBuilder.put("prefix", helpMsg);
		jBuilder.put("curPage", mixPageHlper.getCurPage(MixPageHlper.TYPE_MIX));
		//只有1页时不显示翻页的
		int maxPage = mixPageHlper.getMaxPage(MixPageHlper.TYPE_MIX);
		jBuilder.put("maxPage", maxPage == 1 ? 0 : maxPage);

		jBuilder.put("title",mHelpDetail.name);
		jBuilder.put("isFromFile",isFromFile);

		HelpDetail tmpHelpDetail = mHelpDetail.clone();
		tmpHelpDetail.detailImgs = new ArrayList<HelpDetail.HelpDetailImg>();
		JSONArray jsonArray = new JSONArray();
		JSONObject obj;
		HelpDetail.HelpDetailImg helpDetailImg;
		for (int i = 0; i < pageSize; i++) {
			if (sIndex >= helpDetailImgs.size()) {
				break;
			}
			helpDetailImg = helpDetailImgs.get(sIndex);
			tmpHelpDetail.detailImgs.add(helpDetailImg);
			obj = new JSONBuilder()
					.put("text", helpDetailImg.text)
					.put("time", helpDetailImg.time)
					.put("img", helpDetailImg.img)
					.put("id",helpDetailImg.id)
					.getJSONObject();
			jsonArray.put(obj);
			sIndex++;
		}
		jBuilder.put("helpDetails", jsonArray);
		jBuilder.put("count", jsonArray.length());

		if (WinManager.getInstance().isRecordWin2()) {
			RecorderWin.sendSelectorList(jBuilder.toString());
			return;
		}
		jBuilder.put("type", 7);
		AppLogic.runOnUiGround(new Runnable2<JSONBuilder,HelpDetail>(jBuilder,tmpHelpDetail) {
			@Override
			public void run() {
				// 直接发送给本地界面
				WinRecord.getInstance().addMsg(ChatMsgFactory.createContainMsg(mP1.toString(), createImageTextView(mP2)));
			}
		}, 0);

	}

	protected View createImageTextView(final HelpDetail helpDetail) {
		int contentHeight = ScreenUtil.getDisplayLvItemH(false) * ScreenUtil.getVisbileCount();
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.setLayoutParams(layoutParams);
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackgroundResource(R.drawable.white_range_layout);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
		llLayout.addView(llContent,layoutParams);
		int itemHeight = (int)(contentHeight/(ScreenUtil.getVisbileCount()*1.25f));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight);

		HelpDetailItem mHelpDetailItem = new HelpDetailItem();
		mHelpDetailItem.name = helpDetail.name;
		mHelpDetailItem.time = helpDetail.time;
		mHelpDetailItem.isNew = isHelpDetailNew;

		View itemView = createItemView(0, mHelpDetailItem, true);
		llContent.addView(itemView,layoutParams);
		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setOrientation(LinearLayout.HORIZONTAL);
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		llContent.addView(llDetail,layoutParams);
		View view = new View(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
		llDetail.addView(view,layoutParams);

		final HelpDetail.HelpDetailImg helpDetailImg = helpDetail.detailImgs.get(0);

		if (!TextUtils.isEmpty(helpDetailImg.img)) {
			ImageView iv = new ImageView(GlobalContext.get());
			iv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					JSONBuilder data = new JSONBuilder();
					data.put("title",helpDetail.title);
					data.put("url",helpDetailImg.img);
					data.put("desc",helpDetailImg.text);
					data.put("isFromFile",isFromFile);
					data.put("from","detail");
					WinHelpManager.getInstance().invokeWinHelp("", "txz.help.ui.detail.qrcode", data.toBytes());
				}
			});
			iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			if (helpDetailImg.img.startsWith("qrcode:")){
				try {
					iv.setImageBitmap(QRUtil.createQRCodeBitmap(helpDetailImg.img.replace("qrcode:",""), (int) LayouUtil.getDimen("y200")));
				} catch (WriterException e) {
					e.printStackTrace();
				}
			} else if (isFromFile) {
				ImageLoader.getInstance().displayImage("file://" + helpDetailImg.img, new ImageViewAware(iv));
			} else {
				iv.setImageDrawable(LayouUtil.getDrawable(helpDetailImg.img));
			}
			layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5);
			llDetail.addView(iv, layoutParams);
		}

		if (!TextUtils.isEmpty(helpDetailImg.text) && !TextUtils.isEmpty(helpDetailImg.img)) {
			view = new View(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			llDetail.addView(view, layoutParams);
		}

		if (!TextUtils.isEmpty(helpDetailImg.text)) {
			TextView tv = new TextView(GlobalContext.get());
			tv.setGravity(Gravity.CENTER_VERTICAL);
			TextViewUtil.setTextSize(tv, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE1));
			TextViewUtil.setTextColor(tv, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
			tv.setText(helpDetailImg.text);
			layoutParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 10);
			llDetail.addView(tv, layoutParams);
		}

		view = new View(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
		llDetail.addView(view,layoutParams);

		return llLayout;
	}

	public void snapPage(boolean isNext) {
		if (!mIsSelecting) {
			return;
		}
		boolean bSucc = isNext?mixPageHlper.nextPage():mixPageHlper.prePage();
		if (bSucc) {
			showMixHelp(mixPageHlper.getCurPage(MixPageHlper.TYPE_MIX));
		}
	}

	private void snapPager(boolean isNext, boolean bSucc, String command) {
		if (bSucc) {
			showMixHelp(mixPageHlper.getCurPage(MixPageHlper.TYPE_MIX));
		}

		String endSpk = "";
		String pager = command;
		if (command.contains("翻")) {
			pager = NativeData.getResString("RS_SELECTOR_SELECT_PAGE").replace(
					"%CMD%", command);
		} else {
			pager = NativeData.getResPlaceholderString("RS_SELECTOR_SELECT",
					"%CMD%", command);
		}

		if (!bSucc) {
			String slot = "";
			if (isNext) {
				slot = NativeData.getResString("RS_SELECTOR_THE_LAST");
			} else {
				slot = NativeData.getResString("RS_SELECTOR_THE_FIRST");
			}

			endSpk = NativeData.getResString("RS_SELECTOR_PAGE_COMMAND")
					.replace("%NUM%", slot);
		}

		speakText(bSucc ? pager : endSpk);
	}

	private void speakText(String spk) {
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		mSpeechTaskId = TtsManager.getInstance().speakText(spk);
	}

	public void onBackPress(boolean fromVoice){
		dismiss();
		WinHelpManager.getInstance().updateCloseIconState(true);
		ChoiceManager.getInstance().clearIsSelecting();
		if (curHelpPage >= 0) {
//						WinHelpDetailTops.getInstance().updateCurPage(curHelpPage);
			WinHelpManager.getInstance().show(new JSONBuilder().put("type", WinHelpManager.TYPE_OPEN_FROM_BACK)
					.put("selectPage",curHelpPage).toString());
			ReportUtil.doReport(new ReportUtil.Report.Builder()
					.setType("helpDetail")
					.setAction("back")
					.putExtra("closetype", fromVoice?WinHelpManager.TYPE_CLOSE_FROM_VOICE:WinHelpManager.TYPE_CLOSE_FROM_CLICK)
					.setSessionId()
					.buildCommReport());
		}else {
			RecorderWin.open(NativeData.getResString("RS_SELECTOR_HELP"));
			ReportUtil.doReport(new ReportUtil.Report.Builder()
					.setType("helpDetail")
					.setAction("close")
					.putExtra("closetype", fromVoice?WinHelpManager.TYPE_CLOSE_FROM_VOICE:WinHelpManager.TYPE_CLOSE_FROM_CLICK)
					.setSessionId()
					.buildCommReport());
		}
	}

	private void beginWakeup() {
		AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {
			
			@Override
			public boolean needAsrState() {
				return true;
			}

			@Override
			public String getTaskId() {
				return TASK_HELP_ITEM;
			}

			@Override
			public void onCommandSelected(String type, String command) {
				if ("HELP$CANCEL".equals(type)) {
					onBackPress(true);
				} else if ("HELP$QUIT".equals(type)) {
					dismiss();
					RecorderWin.open(NativeData.getResString("RS_SELECTOR_HELP"));
					ReportUtil.doReport(new ReportUtil.Report.Builder()
							.setType("helpDetail")
							.setAction("back")
							.putExtra("closetype", WinHelpManager.TYPE_CLOSE_FROM_VOICE)
							.setSessionId()
							.buildCommReport());
				}else if ("HELP$NEXTPAGE".equals(type)) {
					snapPager(true, mixPageHlper.nextPage(), command);
					ReportUtil.doReport(
							new ReportUtil.Report.Builder()
									.setAction("helpDetail")
									.setType("nextPage")
									.putExtra("snaptype",WinHelpManager.TYPE_SNAP_PAGE_FROM_VOICE)
									.setSessionId()
									.buildCommReport());
				} else if ("HELP$PREPAGE".equals(type)) {
					snapPager(false, mixPageHlper.prePage(), command);
					ReportUtil.doReport(
							new ReportUtil.Report.Builder()
									.setAction("helpDetail")
									.setType("prePage")
									.putExtra("snaptype",WinHelpManager.TYPE_SNAP_PAGE_FROM_VOICE)
									.setSessionId()
									.buildCommReport());
				} else if (type.startsWith("HELP_PAGE_INDEX_")) {
					int index = Integer.parseInt(type
							.substring("HELP_PAGE_INDEX_".length()));
					speakText(NativeData.getResPlaceholderString(
							"RS_SELECTOR_SELECT", "%CMD%", command));
					mixPageHlper.selectPage(index);

					showMixHelp(mixPageHlper.getCurPage(MixPageHlper.TYPE_MIX));
				} 
			}
		}.addCommand("HELP$CANCEL",
				NativeData.getResStringArray("RS_HELP_DETAIL_ITEM_WAKEUP_CANCEL"));
		acsc.addCommand("HELP$QUIT",
				NativeData.getResStringArray("RS_HELP_DETAIL_ITEM_WAKEUP_QUIT"));

		if (mixPageHlper != null) {
			if (mixPageHlper.getMaxPage(MixPageHlper.TYPE_MIX) > 1) {
				acsc.addCommand("HELP$NEXTPAGE",
						NativeData.getResStringArray("RS_SELECT_WAKEUP_NEXTPAGE"))
						.addCommand(
								"HELP$PREPAGE",
								NativeData
										.getResStringArray("RS_SELECT_WAKEUP_PREPAGE"));

				int pageSize = mixPageHlper.getMaxPage(MixPageHlper.TYPE_MIX);
				int i = 1;
				for (; i <= pageSize; i++) {
					String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i);
					acsc.addCommand("HELP_PAGE_INDEX_" + i, "第" + strIndex + "页");
				}
				acsc.addCommand("HELP_PAGE_INDEX_" + (i - 1), "最后一页");
			}
		}

		mHasWakeup = true;
		WakeupManager.getInstance().useWakeupAsAsr(acsc);
	}

	public static final String TASK_HELP_ITEM = "WinHelpItemControl";

	@Override
	public void showChoices(Void data) {}

	@Override
	public boolean isSelecting() {
		return mIsSelecting;
	}

	@Override
	public void clearIsSelecting() {
		clearIsSelecting_Inner();
	}

	protected void dismiss() {
		WinHelpManager.getInstance().resetCloseIconState();
		clearIsSelecting();
	}

	void clearIsSelecting_Inner(){
		if (mHasWakeup) {
			mHasWakeup = false;
			mIsSelecting = false;
			TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			WakeupManager.getInstance().recoverWakeupFromAsr(TASK_HELP_ITEM);
		}
	}

	class MixPageHlper {
		PageHelper listPageHelper;
		PageHelper imgPageHelper;
		public int curPage;
		static final int TYPE_MIX = 0;
		static final int TYPE_LIST = 1;
		static final int TYPE_IMG = 2;

		public boolean nextPage() {
			if (curPage < (getMaxPage(TYPE_MIX) - 1)) {
				curPage++;
				int type = getPageType(curPage);
				if (type == TYPE_IMG) {
					if (getPageType(curPage - 1) == TYPE_LIST) {
						imgPageHelper.selectPage(0);
					} else {
						imgPageHelper.nextPage();
					}
				} else if (type == TYPE_LIST) {
					listPageHelper.nextPage();
				}
				return true;
			}
			return false;
		}

		public boolean selectPage(int page) {
			if (page <= getMaxPage(TYPE_MIX) && page > 0) {
				curPage = page - 1;
				int type = getPageType(curPage);
				if (type == TYPE_IMG) {
					if (listPageHelper.totalCount == 0) {
						imgPageHelper.selectPage(page);
					} else {
						imgPageHelper.selectPage(page - getMaxPage(TYPE_LIST));
					}
				} else if (type == TYPE_LIST) {
					listPageHelper.selectPage(page);
				}
				return true;
			}
			return false;
		}

		public boolean prePage() {
			if (curPage > 0) {
				curPage--;
				int type = getPageType(curPage);
				if (type == TYPE_IMG) {
					imgPageHelper.prePage();
				} else if (type == TYPE_LIST) {
					if (getPageType(curPage + 1) == TYPE_IMG) {
						listPageHelper.selectPage(getMaxPage(TYPE_LIST));
					} else {
						listPageHelper.prePage();
					}
				}
				return true;
			}
			return false;
		}

		public int getMaxPage(int type) {
			if (type == TYPE_MIX) {
				return  getMaxPage(TYPE_LIST) + getMaxPage(TYPE_IMG);
			} else if (type == TYPE_LIST) {
				return (listPageHelper.getTotalCount() > 0 && listPageHelper.getMaxPage() == 0) ? 1 : listPageHelper.getMaxPage();
			} else if (type == TYPE_IMG) {
				return (imgPageHelper.getTotalCount() > 0 && imgPageHelper.getMaxPage() == 0) ? 1 : imgPageHelper.getMaxPage();
			}else {
				return 0;
			}
		}

		public int getPageSize(int type) {
			if (type == TYPE_LIST) {
				return listPageHelper.getPageSize();
			} else if (type == TYPE_IMG) {
				return imgPageHelper.getPageSize();
			} else {
				return 0;
			}
		}

		public void reset(PageHelper listPageHelper,PageHelper imgPageHelper) {
			this.listPageHelper = listPageHelper;
			this.imgPageHelper = imgPageHelper;
			this.curPage = 0;
		}

		public int getCurPage(int type) {
			if (type == TYPE_LIST) {
				return listPageHelper.getCurPage();
			} else if (type == TYPE_IMG) {
				return imgPageHelper.getCurPage();
			} else {
				return curPage;
			}
		}

		public int getPageType(int curPage){
			int type = TYPE_LIST;
			if (curPage >= getMaxPage(TYPE_LIST) && curPage <= getMaxPage(TYPE_MIX)) {
				type = TYPE_IMG;
			}
			return type;
		}

	}
}
