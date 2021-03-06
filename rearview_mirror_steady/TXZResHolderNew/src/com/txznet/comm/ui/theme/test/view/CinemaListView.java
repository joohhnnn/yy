package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutFull;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutHalf;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutNone;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.view.ScaleImageView;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CinemaListViewData;
import com.txznet.comm.ui.viewfactory.data.CinemaListViewData.CinemaBean;
import com.txznet.comm.ui.viewfactory.data.PoiListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICinemaListView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize.ImageLoaderImpl;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.runnables.Runnable1;

@SuppressLint("NewApi")
public class CinemaListView extends ICinemaListView {

	private static CinemaListView sInstance = new CinemaListView();

	private LruCache<String, Bitmap> mCachePost = new LruCache<String, Bitmap>(ConfigUtil.getCinemaItemCount());
	
	private List<View> mItemViews;

	private int contentHeight;    //????????????
	private int ivBillWidth;    //??????????????????
	private int ivBillHeight;    //??????????????????
    private int itemHeight;    //????????????
	private int tvNameSize;    //??????????????????
	private int tvNameHeight;    //??????????????????
	private int tvNameColor;    //????????????????????????
	private int tvNameTopMargin;    //?????????????????????
	private int tvNameBottomMargin;    //?????????????????????
    private int ivStarBgColor;    //????????????????????????
	private int ivStarWidth;    //??????????????????
	private int ivStarHeight;    //??????????????????
	private int ivStarRightMargin;    //?????????????????????
	private int tvScorePreSize;    //????????????????????????
	private int tvScorePreHeight;    //????????????????????????
	private int tvScoreColor;    //??????????????????
    private int tvScoreAftSize;    //????????????????????????

    private int dividerHeight;

	private CinemaListView() {
	}

	public static CinemaListView getInstance(){
		return sInstance;
	}
	
	@Override
	public void updateProgress(int progress, int selection) {
		LogUtil.logd("updateProgress " + progress + "," + selection);
	}
	
	@Override
	public void release() {
		super.release();
		if (mItemViews != null) {
			mItemViews.clear();
		}
	}

	@Override
	public ViewAdapter getView(ViewData data) {
		CinemaListViewData cinemaData = (CinemaListViewData)data;
		WinLayout.getInstance().vTips = cinemaData.vTips;
		LogUtil.logd(WinLayout.logTag+ "cinemaData.vTips: "+cinemaData.vTips);

		View view = null;
		//onUpdateSize();
		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                view = createViewFull(cinemaData);
                break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                view = createViewHalf(cinemaData);
                break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = createViewNone(cinemaData);
				break;
		}
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.view.setTag(data.getType());
		viewAdapter.isListView = true;
		viewAdapter.object = CinemaListView.getInstance();
		return viewAdapter;
	}

	private View createViewFull(CinemaListViewData cinemaData){

		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(cinemaData,"movie","??????");

		FrameLayout fLayout = new FrameLayout(GlobalContext.get());

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		FrameLayout.LayoutParams fLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
		//???????????????????????????
		if (WinLayout.isVertScreen){
			if(WinLayout.getInstance().isVerticalFullBottom){
				fLayoutParams.gravity = Gravity.BOTTOM;
			}else {
				fLayoutParams.gravity = Gravity.TOP;
			}
		}else {
			fLayoutParams.gravity = Gravity.CENTER;
		}
		fLayout.addView(llLayout,fLayoutParams);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("white_range_layout_movie"));

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0, itemHeight,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContents.addView(llContent,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),cinemaData.mTitleInfo.curPage,cinemaData.mTitleInfo.maxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		//layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,itemHeight);
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LayoutParams.MATCH_PARENT);
		llContents.addView(llPager,layoutParams);

		//layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		//layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,SizeConfig.pageCount * SizeConfig.itemHeight);
		layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,contentHeight);
		layoutParams.gravity = Gravity.CENTER;
		llLayout.addView(llContents,layoutParams);

		mItemViews = new ArrayList<View>();
		for (int i = 0; i < SizeConfig.pageMovieCount; i++) {
			int emptyCount = SizeConfig.pageMovieCount - cinemaData.count;
			View itemView ;
			if (emptyCount > 0 && i >= cinemaData.count ){
				itemView = createEmptyView();
			}else {
				itemView = createItemViewFull(i,cinemaData.getData().get(i));
			}
			layoutParams = new LinearLayout.LayoutParams(ivBillWidth,LayoutParams.MATCH_PARENT);
            llContent.addView(itemView,layoutParams);
			if (i != SizeConfig.pageMovieCount - 1) {
				//layoutParams.setMargins(0, 0, (int) LayouUtil.getDimen("x4"), 0);
                FrameLayout fItemView = new FrameLayout(GlobalContext.get());
                layoutParams = new LinearLayout.LayoutParams(0,0,1);
                llContent.addView(fItemView,layoutParams);
			}
			mItemViews.add(itemView);
		}
		//int emptyCount = ConfigUtil.getCinemaItemCount() - cinemaData.count;
		/*int emptyCount = 3 - cinemaData.count;
		if (emptyCount > 0) {
			for (int i = cinemaData.count - 1; i < 3; i++) {
				//View itemView = createItemViewFull(i,cinemaData.getData().get(i));
				View itemView = createEmptyView();
				layoutParams = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
				if (i != 3 - 1) {
					layoutParams.setMargins(0, 0, (int) LayouUtil.getDimen("y12"), 0);
				}
				llContent.addView(itemView,layoutParams);
				mItemViews.add(itemView);
			}
		}*/

		/*LinearLayout layout = new LinearLayout(GlobalContext.get());
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		layout.addView(llLayout,layoutParams);*/
		return fLayout;
	}

    private View createViewHalf(CinemaListViewData cinemaData){

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(cinemaData,"movie","??????");

        FrameLayout fLayout = new FrameLayout(GlobalContext.get());

        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams fLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        fLayoutParams.gravity = Gravity.CENTER;
        fLayout.addView(llLayout,fLayoutParams);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("white_range_layout_movie"));

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.HORIZONTAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(0, itemHeight,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llContents.addView(llContent,layoutParams);

        LinearLayout llPager = new PageView(GlobalContext.get(),cinemaData.mTitleInfo.curPage,cinemaData.mTitleInfo.maxPage);
        //llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
        //layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,itemHeight);
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LayoutParams.MATCH_PARENT);
        llContents.addView(llPager,layoutParams);

        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,contentHeight);
        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,SizeConfig.pageCount * SizeConfig.itemHeight);
        layoutParams.gravity = Gravity.CENTER;
        llLayout.addView(llContents,layoutParams);

        mItemViews = new ArrayList<View>();
        for (int i = 0; i < SizeConfig.pageMovieCount; i++) {
            int emptyCount = SizeConfig.pageMovieCount - cinemaData.count;
            View itemView ;
            if (emptyCount > 0 && i >= cinemaData.count ){
                itemView = createEmptyView();
            }else {
                itemView = createItemViewHalf(i,cinemaData.getData().get(i));
            }
            layoutParams = new LinearLayout.LayoutParams(ivBillWidth,LayoutParams.MATCH_PARENT);
            llContent.addView(itemView,layoutParams);
            if (i != SizeConfig.pageMovieCount - 1) {
                //layoutParams.setMargins(0, 0, (int) LayouUtil.getDimen("x4"), 0);
                FrameLayout fItemView = new FrameLayout(GlobalContext.get());
                layoutParams = new LinearLayout.LayoutParams(0,0,1);
                llContent.addView(fItemView,layoutParams);
            }
            mItemViews.add(itemView);
        }
        //int emptyCount = ConfigUtil.getCinemaItemCount() - cinemaData.count;
		/*int emptyCount = 3 - cinemaData.count;
		if (emptyCount > 0) {
			for (int i = cinemaData.count - 1; i < 3; i++) {
				//View itemView = createItemViewFull(i,cinemaData.getData().get(i));
				View itemView = createEmptyView();
				layoutParams = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
				if (i != 3 - 1) {
					layoutParams.setMargins(0, 0, (int) LayouUtil.getDimen("y12"), 0);
				}
				llContent.addView(itemView,layoutParams);
				mItemViews.add(itemView);
			}
		}*/

		/*LinearLayout layout = new LinearLayout(GlobalContext.get());
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		layout.addView(llLayout,layoutParams);*/
        return fLayout;
    }

	private View createViewNone(CinemaListViewData cinemaData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(cinemaData,"movie","??????");

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),cinemaData.mTitleInfo.curPage,cinemaData.mTitleInfo.maxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llLayout.addView(llPager,layoutParams);

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContents.addView(titleViewAdapter.view,layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        llContents.addView(divider, layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,0,1);
		llContents.addView(llContent,layoutParams);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
		//llLayout.addView(llContent,layoutParams);

		mItemViews = new ArrayList<View>();

        FrameLayout fItemView = new FrameLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(0,0,1);
        llContent.addView(fItemView,layoutParams);
		for (int i = 0; i < SizeConfig.pageMovieCount; i++) {
			int emptyCount = SizeConfig.pageMovieCount - cinemaData.count;
			View itemView ;
			if (emptyCount > 0 && i >= cinemaData.count ){
				itemView = createEmptyView();
			}else {
				itemView = createItemViewNone(i,cinemaData.getData().get(i));
			}
			//layoutParams = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,1);
			layoutParams = new LinearLayout.LayoutParams(ivBillWidth,itemHeight);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
			llContent.addView(itemView,layoutParams);

            FrameLayout fItemView1 = new FrameLayout(GlobalContext.get());
            layoutParams = new LinearLayout.LayoutParams(0,0,1);
            llContent.addView(fItemView1,layoutParams);

			/*FrameLayout.LayoutParams fLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
			fLayoutParams.gravity = Gravity.CENTER;
			fItemView.addView(itemView,fLayoutParams);*/
			mItemViews.add(itemView);
		}

		return llLayout;
	}
	
	private void addChildView(LinearLayout parent,View view,int pos){
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
		/*if (pos == 0) {
			layoutParams.setMargins(0, 0, (int)LayouUtil.getDimen("y6"), 0);
		} elseif (pos == 3 - 1) {
			layoutParams.setMargins((int)LayouUtil.getDimen("y6"), 0, 0, 0);
		} else {
			layoutParams.setMargins((int)LayouUtil.getDimen("y6"), 0, (int)LayouUtil.getDimen("y6"), 0);
		} */
		if (pos != 3 - 1) {
			layoutParams.setMargins(0, 0, (int)LayouUtil.getDimen("y12"), 0);
		}
		parent.addView(view,layoutParams);
	}

	@Override
	public void init() {
		super.init();
		// ???????????????????????????????????????
        //dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
		dividerHeight = (int) LayouUtil.getDimen("y1");
		if (dividerHeight == 0){
			dividerHeight = 1;
		}
        ivStarBgColor = Color.parseColor(LayouUtil.getString("color_movie_score_bg"));
		tvScoreColor = Color.parseColor(LayouUtil.getString("color_flight_price"));
        tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));
	}

	//??????????????????????????????
	public void onUpdateParams(int styleIndex){
        switch (styleIndex) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                initFull();
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                initHalf();
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                initNone();
                break;
            default:
                break;
        }
    }

	//??????????????????
	private void initFull(){
        // ???????????????????????????????????????
		if (WinLayout.isVertScreen){
            int unit = (int) LayouUtil.getDimen("vertical_unit");
            ivBillWidth = (SizeConfig.screenWidth - 6 * unit)/3;
            ivBillHeight = ivBillWidth * 3 / 2;
            tvNameSize = (int) LayouUtil.getDimen("vertical_h3");
            tvNameHeight = (int) LayouUtil.getDimen("vertical_h3_height");
            tvNameTopMargin = 2 * unit;
            tvNameBottomMargin = unit;
            ivStarWidth = (int) (ivBillWidth * 0.7);
            ivStarHeight = (int) LayouUtil.getDimen("vertical_h1");
            ivStarRightMargin = unit / 3;
            tvScorePreSize = (int) LayouUtil.getDimen("vertical_h1");
            tvScorePreHeight = (int) LayouUtil.getDimen("vertical_h1_height");
            tvScoreAftSize = (int) LayouUtil.getDimen("vertical_h3");
            itemHeight = ivBillHeight + tvNameTopMargin + tvNameHeight + tvNameBottomMargin + tvScorePreHeight + tvNameTopMargin;
            //itemHeight = SizeConfig.itemHeight * SizeConfig.pageCount - unit;
            contentHeight = itemHeight + unit;
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            ivBillWidth = (int) LayouUtil.getDimen("billWidth");
            ivBillHeight = (int) LayouUtil.getDimen("billHeight");
            tvNameSize = (int) LayouUtil.getDimen("h3");
            tvNameHeight = (int) LayouUtil.getDimen("h3_height");
            tvNameTopMargin = 2 * unit;
            tvNameBottomMargin = unit;
            ivStarWidth = (int) (ivBillWidth * 0.7);
            ivStarHeight = (int) LayouUtil.getDimen("h1");
            ivStarRightMargin = unit / 3;
            tvScorePreSize = (int) LayouUtil.getDimen("h1");
            tvScorePreHeight = (int) LayouUtil.getDimen("h1_height");
            tvScoreAftSize = (int) LayouUtil.getDimen("h3");
            if (SizeConfig.pageCount < 4){
                tvNameBottomMargin = 0;    //???????????????????????????,????????????????????????
                itemHeight = ivBillHeight + tvNameTopMargin + tvNameHeight + tvScorePreHeight + tvNameTopMargin;
			}else {
                itemHeight = SizeConfig.itemHeight * SizeConfig.pageCount - unit;
            }
            //contentHeight = SizeConfig.itemHeight * SizeConfig.pageCount;
            contentHeight = itemHeight + unit;
        }
    }

    //??????????????????
    private void initHalf(){
        // ???????????????????????????????????????
	    if (WinLayout.isVertScreen){
            int unit = (int) LayouUtil.getDimen("vertical_unit");
            ivBillWidth = (SizeConfig.screenWidth - 6 * unit)/3;
            ivBillHeight = ivBillWidth * 3 / 2;
            tvNameSize = (int) LayouUtil.getDimen("vertical_h3");
            tvNameHeight = (int) LayouUtil.getDimen("vertical_h3_height");
            tvNameTopMargin = 2 * unit;
            tvNameBottomMargin = unit;
            ivStarWidth = (int) (ivBillWidth * 0.7);
            ivStarHeight = (int) LayouUtil.getDimen("vertical_h1");
            ivStarRightMargin = unit / 3;
            tvScorePreSize = (int) LayouUtil.getDimen("vertical_h1");
            tvScorePreHeight = (int) LayouUtil.getDimen("vertical_h1_height");
            tvScoreAftSize = (int) LayouUtil.getDimen("vertical_h3");
            itemHeight = ivBillHeight + tvNameTopMargin + tvNameHeight + tvNameBottomMargin + tvScorePreHeight + tvNameTopMargin;
            //itemHeight = SizeConfig.itemHeight * SizeConfig.pageCount - unit;
            //contentHeight = SizeConfig.itemHeight * SizeConfig.pageCount;
            contentHeight = itemHeight + unit;
            WinLayoutHalf.cardViewHeight = SizeConfig.screenHeight / 5 + contentHeight + SizeConfig.titleHeight;
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            ivBillWidth = (int) LayouUtil.getDimen("billWidthHalf");
            ivBillHeight = (int) LayouUtil.getDimen("billHeightHalf");
            tvNameSize = (int) LayouUtil.getDimen("h3");
            tvNameHeight = (int) LayouUtil.getDimen("h3_height");
            tvNameTopMargin = unit;
            tvNameBottomMargin = unit;
            ivStarWidth = (int) (ivBillWidth * 0.7);
            ivStarHeight = (int) LayouUtil.getDimen("h1");
            ivStarRightMargin = unit / 3;
            tvScorePreSize = (int) LayouUtil.getDimen("h3");
            tvScorePreHeight = (int) LayouUtil.getDimen("h3_height");
            tvScoreAftSize = (int) LayouUtil.getDimen("h7");
            //itemHeight = ivBillHeight + tvNameTopMargin + tvNameHeight + tvNameBottomMargin + tvScorePreHeight + tvNameTopMargin;
            itemHeight = ivBillHeight  + tvNameHeight + tvNameBottomMargin + tvScorePreHeight ;
            //itemHeight = SizeConfig.itemHeight * SizeConfig.pageCount - unit;
            contentHeight = itemHeight + unit;
        }

    }

    //??????????????????
    private void initNone(){
        if (WinLayout.isVertScreen){
            //int unit = (int) LayouUtil.getDimen("unit");
			ivBillWidth = (int) LayouUtil.getDimen("x175");
			ivBillHeight = ivBillWidth * 3 / 2;
			tvNameSize = (int) LayouUtil.getDimen("x26");
			tvNameHeight = tvNameSize + 10;
			tvNameTopMargin = (int) LayouUtil.getDimen("x20");
			tvNameBottomMargin = (int) LayouUtil.getDimen("x10");
			ivStarWidth = (int) (ivBillWidth * 0.7);
			ivStarHeight = (int) LayouUtil.getDimen("x31");
			ivStarRightMargin = (int) LayouUtil.getDimen("x3");
			tvScorePreSize = (int) LayouUtil.getDimen("x31");
			tvScorePreHeight = tvScorePreSize + 10;
			tvScoreAftSize = (int) LayouUtil.getDimen("x26");
			//itemHeight = ivBillHeight + tvNameTopMargin + tvNameHeight + tvNameBottomMargin;
			itemHeight = ivBillHeight + tvNameHeight;
			//itemHeight = SizeConfig.itemHeight * SizeConfig.pageCount - unit;
		}else {
            int unit = (int) LayouUtil.getDimen("unit");
            ivBillWidth = (int) LayouUtil.getDimen("billWidthNone");
            ivBillHeight = (int) LayouUtil.getDimen("billHeightNone");
            tvNameSize = (int) LayouUtil.getDimen("h3_none");
            tvNameHeight = (int) LayouUtil.getDimen("h3_height_none");
            tvNameTopMargin = 2 * unit;
            tvNameBottomMargin = unit;
            ivStarWidth = (int) (ivBillWidth * 0.7);
            ivStarHeight = (int) LayouUtil.getDimen("h1_none");
            ivStarRightMargin = unit / 3;
            tvScorePreSize = (int) LayouUtil.getDimen("h1_none");
            tvScorePreHeight = (int) LayouUtil.getDimen("h1_height_none");
            tvScoreAftSize = (int) LayouUtil.getDimen("h3_none");
            //itemHeight = ivBillHeight + tvNameTopMargin + tvNameHeight + tvNameBottomMargin;
            itemHeight = ivBillHeight + tvNameHeight;
            //itemHeight = SizeConfig.itemHeight * SizeConfig.pageCount - unit;
        }
    }

	/*//??????????????????????????????????????????
	private void onUpdateSize(){
		ivBillWidth = SizeConfig.getInstance().getBillWidth();
		ivBillHeight = SizeConfig.getInstance().getBillHeight();
		itemHeight = SizeConfig.getInstance().getItemMovieHeight();
	}*/

	@Override
	public void snapPage(boolean next) {
		LogUtil.logd("update snap "+next);
	}
	
	@Override
	public List<View> getFocusViews() {
		return mItemViews;
	}
	
	@Override
	public boolean supportKeyEvent() {
		return true;
	}
	
	
	private View createItemViewFull(int position,CinemaBean cinemaBean){
		LinearLayout llItem = new LinearLayout(GlobalContext.get());
		llItem.setOrientation(LinearLayout.VERTICAL);
		llItem.setBackground(LayouUtil.getDrawable("movie_bg"));
		// ??????2px???padding?????????????????????
		//llItem.setPadding(2, 2, 2, 2);
		//llItem.setBackground(LayouUtil.getDrawable("white_bottom_range_layout"));

		//ScaleImageView ivBill = new ScaleImageView(GlobalContext.get());
		ImageView ivBill = new ImageView(GlobalContext.get());
		//ivBill.setMode(ScaleImageView.MODE_AUTO_HEIGHT);
		//ivBill.setScale(1.5f);
		ivBill.setScaleType(ScaleType.CENTER_CROP);
		//ivBill.setAdjustViewBounds(true);
		//LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
		//LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) LayouUtil.getDimen("y258"));
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ivBillWidth,ivBillHeight);
		llItem.addView(ivBill,layoutParams);

		/*LinearLayout lBottom = new LinearLayout(GlobalContext.get());
		lBottom.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,0,1);
		llItem.addView(lBottom,layoutParams);*/

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(ivBillWidth,itemHeight - ivBillHeight);
        llItem.addView(llContent,layoutParams);

		TextView tvTitle = new TextView(GlobalContext.get());
		tvTitle.setEllipsize(TruncateAt.END);
		tvTitle.setGravity(Gravity.CENTER);
		tvTitle.setSingleLine();
		TextViewUtil.setTextSize(tvTitle, tvNameSize);
		TextViewUtil.setTextColor(tvTitle, tvNameColor);
		tvTitle.setText(LanguageConvertor.toLocale(cinemaBean.title));
		loadDrawableByUrl(ivBill, cinemaBean.post);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,tvNameHeight);
        //layoutParams.topMargin = tvNameTopMargin;
        layoutParams.bottomMargin = tvNameBottomMargin;
        llContent.addView(tvTitle,layoutParams);

		FrameLayout fLScore = new FrameLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;
        //layoutParams.bottomMargin = tvNameBottomMargin;
        llContent.addView(fLScore,layoutParams);
		
		LinearLayout llScore = new LinearLayout(GlobalContext.get());
		llScore.setGravity(Gravity.CENTER);
		llScore.setOrientation(LinearLayout.HORIZONTAL);
		FrameLayout.LayoutParams fLayoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		fLayoutParams.gravity = Gravity.CENTER;
		fLScore.addView(llScore,fLayoutParams);
		if(cinemaBean.score > 0){
			ImageView ivScore = new ImageView(GlobalContext.get());
			ivScore.setScaleType(ScaleType.FIT_CENTER);
			layoutParams = new LinearLayout.LayoutParams(ivStarWidth,ivStarHeight);
			layoutParams.rightMargin = ivStarRightMargin;
			layoutParams.gravity = Gravity.CENTER_VERTICAL;
			llScore.addView(ivScore,layoutParams);

			TextView tvScorePre = new TextView(GlobalContext.get());
			tvScorePre.setGravity(Gravity.BOTTOM);
			tvScorePre.setIncludeFontPadding(false);
			tvScorePre.setSingleLine();
			tvScorePre.setId(ViewUtils.generateViewId());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvScorePreHeight);
			layoutParams.gravity = Gravity.BOTTOM;
			llScore.addView(tvScorePre,layoutParams);

			TextView tvScoreAft = new TextView(GlobalContext.get());
			tvScoreAft.setGravity(Gravity.BOTTOM);
			tvScoreAft.setSingleLine();
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.BOTTOM;
			llScore.addView(tvScoreAft,layoutParams);

			TextViewUtil.setTextSize(tvScorePre, tvScorePreSize);
			TextViewUtil.setTextColor(tvScorePre, tvScoreColor);
			TextViewUtil.setTextSize(tvScoreAft, tvScoreAftSize);
			TextViewUtil.setTextColor(tvScoreAft, tvScoreColor);
			String sc = String.format("%.1f", cinemaBean.score);
			if (sc.contains(".")) {
				String[] scoreArray = sc.split("\\.");
				if (scoreArray != null && scoreArray.length > 1) {
					tvScorePre.setText(scoreArray[0]);
					tvScoreAft.setText("." + scoreArray[1]);
				}
			} else {
				tvScorePre.setText(LanguageConvertor.toLocale(sc));
				tvScoreAft.setText("");
			}

			ivScore.setImageDrawable(getSoreMark(cinemaBean.score));
			if (ivScore.getVisibility() != View.VISIBLE) {
				ivScore.setVisibility(View.VISIBLE);
			}
		}else {
			TextView tvNoScore = new TextView(GlobalContext.get());
			tvNoScore.setText("????????????");
			TextViewUtil.setTextSize(tvNoScore, tvScoreAftSize);
			TextViewUtil.setTextColor(tvNoScore, Color.parseColor("#FFFFFF"));
			llScore.addView(tvNoScore, layoutParams);
		}
		
		/*llItem.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				LinearLayout itemView = (LinearLayout) v;
				if (hasFocus) {
					itemView.setBackgroundColor(Color.parseColor("#4AA5FA"));
				} else {
					itemView.setBackground(LayouUtil.getDrawable("white_range_layout"));
				}
			}
		});*/
		
		return llItem;
	}

    private View createItemViewHalf(int position,CinemaBean cinemaBean){
        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOrientation(LinearLayout.VERTICAL);
        llItem.setBackground(LayouUtil.getDrawable("movie_bg"));
        // ??????2px???padding?????????????????????
        //llItem.setPadding(2, 2, 2, 2);
        //llItem.setBackground(LayouUtil.getDrawable("white_bottom_range_layout"));

        //ScaleImageView ivBill = new ScaleImageView(GlobalContext.get());
        ImageView ivBill = new ImageView(GlobalContext.get());
        //ivBill.setMode(ScaleImageView.MODE_AUTO_HEIGHT);
        //ivBill.setScale(1.5f);
        ivBill.setScaleType(ScaleType.CENTER_CROP);
        //ivBill.setAdjustViewBounds(true);
        //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) LayouUtil.getDimen("y258"));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ivBillWidth,ivBillHeight);
        llItem.addView(ivBill,layoutParams);

		/*LinearLayout lBottom = new LinearLayout(GlobalContext.get());
		lBottom.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,0,1);
		llItem.addView(lBottom,layoutParams);*/

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(ivBillWidth,itemHeight - ivBillHeight);
        llItem.addView(llContent,layoutParams);
		TextView tvTitle = new TextView(GlobalContext.get());
		tvTitle.setEllipsize(TruncateAt.END);
		tvTitle.setGravity(Gravity.CENTER);
		tvTitle.setSingleLine();
		tvTitle.setText(LanguageConvertor.toLocale(cinemaBean.title));
		loadDrawableByUrl(ivBill, cinemaBean.post);
		TextViewUtil.setTextSize(tvTitle, tvNameSize);
		TextViewUtil.setTextColor(tvTitle, tvNameColor);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,tvNameHeight);
		//layoutParams.topMargin = tvNameTopMargin;
		//layoutParams.bottomMargin = tvNameBottomMargin;
		llContent.addView(tvTitle,layoutParams);

		FrameLayout fLScore = new FrameLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;
		layoutParams.bottomMargin = tvNameBottomMargin;
		llContent.addView(fLScore,layoutParams);

		LinearLayout llScore = new LinearLayout(GlobalContext.get());
		llScore.setGravity(Gravity.CENTER);
		llScore.setOrientation(LinearLayout.HORIZONTAL);
		FrameLayout.LayoutParams fLayoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		fLayoutParams.gravity = Gravity.CENTER;
		fLScore.addView(llScore,fLayoutParams);
		if(cinemaBean.score > 0){
			ImageView ivScore = new ImageView(GlobalContext.get());
			ivScore.setScaleType(ScaleType.FIT_CENTER);
			layoutParams = new LinearLayout.LayoutParams(ivStarWidth,ivStarHeight);
			layoutParams.rightMargin = ivStarRightMargin;
			layoutParams.gravity = Gravity.CENTER_VERTICAL;
			llScore.addView(ivScore,layoutParams);

			TextView tvScorePre = new TextView(GlobalContext.get());
			tvScorePre.setGravity(Gravity.BOTTOM);
			tvScorePre.setIncludeFontPadding(false);
			tvScorePre.setSingleLine();
			tvScorePre.setId(ViewUtils.generateViewId());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvScorePreHeight);
			layoutParams.gravity = Gravity.BOTTOM;
			llScore.addView(tvScorePre,layoutParams);

			TextView tvScoreAft = new TextView(GlobalContext.get());
			tvScoreAft.setGravity(Gravity.BOTTOM);
			tvScoreAft.setSingleLine();
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.BOTTOM;
			llScore.addView(tvScoreAft,layoutParams);

			TextViewUtil.setTextSize(tvScorePre, tvScorePreSize);
			TextViewUtil.setTextColor(tvScorePre, tvScoreColor);
			TextViewUtil.setTextSize(tvScoreAft, tvScoreAftSize);
			TextViewUtil.setTextColor(tvScoreAft, tvScoreColor);

			String sc = String.format("%.1f", cinemaBean.score);
			if (sc.contains(".")) {
				String[] scoreArray = sc.split("\\.");
				if (scoreArray != null && scoreArray.length > 1) {
					tvScorePre.setText(scoreArray[0]);
					tvScoreAft.setText("." + scoreArray[1]);
				}
			} else {
				tvScorePre.setText(LanguageConvertor.toLocale(sc));
				tvScoreAft.setText("");
			}

			ivScore.setImageDrawable(getSoreMark(cinemaBean.score));
			if (ivScore.getVisibility() != View.VISIBLE) {
				ivScore.setVisibility(View.VISIBLE);
			}
		}else {
			TextView tvNoScore = new TextView(GlobalContext.get());
			tvNoScore.setText("????????????");
			TextViewUtil.setTextSize(tvNoScore, tvScoreAftSize);
			TextViewUtil.setTextColor(tvNoScore, Color.parseColor("#FFFFFF"));
			llScore.addView(tvNoScore, layoutParams);
		}

		/*llItem.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				LinearLayout itemView = (LinearLayout) v;
				if (hasFocus) {
					itemView.setBackgroundColor(Color.parseColor("#4AA5FA"));
				} else {
					itemView.setBackground(LayouUtil.getDrawable("white_range_layout"));
				}
			}
		});*/

        return llItem;
    }

	private View createItemViewNone(int position,CinemaBean cinemaBean){
		LinearLayout llItem = new LinearLayout(GlobalContext.get());
		llItem.setOrientation(LinearLayout.VERTICAL);

		RelativeLayout lBillRoot = new RelativeLayout(GlobalContext.get());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ivBillWidth,ivBillHeight);
		llItem.addView(lBillRoot,layoutParams);

		//ScaleImageView ivBill = new ScaleImageView(GlobalContext.get());
		ImageView ivBill = new ImageView(GlobalContext.get());
		//ivBill.setMode(ScaleImageView.MODE_AUTO_HEIGHT);
		//ivBill.setScale(1.5f);
		ivBill.setScaleType(ScaleType.CENTER_CROP);
		RelativeLayout.LayoutParams rlayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		lBillRoot.addView(ivBill,rlayoutParams);

		TextView tvTitle = new TextView(GlobalContext.get());
		tvTitle.setEllipsize(TruncateAt.END);
		tvTitle.setGravity(Gravity.CENTER);
		tvTitle.setSingleLine();
		TextViewUtil.setTextSize(tvTitle, tvNameSize);
		TextViewUtil.setTextColor(tvTitle, tvNameColor);
		tvTitle.setText(LanguageConvertor.toLocale(cinemaBean.title));
		loadDrawableByUrl(ivBill, cinemaBean.post);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,tvNameHeight);
		llItem.addView(tvTitle,layoutParams);

		FrameLayout fLScore = new FrameLayout(GlobalContext.get());
		fLScore.setBackgroundColor(ivStarBgColor);
		rlayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		rlayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lBillRoot.addView(fLScore,rlayoutParams);

		LinearLayout llScore = new LinearLayout(GlobalContext.get());
		llScore.setGravity(Gravity.CENTER);
		llScore.setOrientation(LinearLayout.HORIZONTAL);
		FrameLayout.LayoutParams fLayoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		fLayoutParams.gravity = Gravity.CENTER;
		fLScore.addView(llScore,fLayoutParams);

		/*ImageView ivScore = new ImageView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(ivStarWidth,ivStarHeight);
		layoutParams.rightMargin = ivStarRightMargin;
		ivScore.setScaleType(ScaleType.FIT_CENTER);
		llScore.addView(ivScore,layoutParams);*/
		if(cinemaBean.score > 0) {
			TextView tvScorePre = new TextView(GlobalContext.get());
			tvScorePre.setGravity(Gravity.BOTTOM);
			tvScorePre.setIncludeFontPadding(false);
			tvScorePre.setSingleLine();
			tvScorePre.setId(ViewUtils.generateViewId());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.BOTTOM;
			llScore.addView(tvScorePre, layoutParams);

			TextView tvScoreAft = new TextView(GlobalContext.get());
			tvScoreAft.setGravity(Gravity.BOTTOM);
			tvScoreAft.setSingleLine();
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.BOTTOM;
			llScore.addView(tvScoreAft, layoutParams);


			TextViewUtil.setTextSize(tvScorePre, tvScorePreSize);
			TextViewUtil.setTextColor(tvScorePre, tvScoreColor);
			TextViewUtil.setTextSize(tvScoreAft, tvScoreAftSize);
			TextViewUtil.setTextColor(tvScoreAft, tvScoreColor);




			String sc = String.format("%.1f", cinemaBean.score);
			if (sc.contains(".")) {
				String[] scoreArray = sc.split("\\.");
				if (scoreArray != null && scoreArray.length > 1) {
					tvScorePre.setText(scoreArray[0]);
					tvScoreAft.setText("." + scoreArray[1]);
				}
			} else {
				tvScorePre.setText(LanguageConvertor.toLocale(sc));
				tvScoreAft.setText("");
			}

		}else {
			TextView tvNoScore = new TextView(GlobalContext.get());
			tvNoScore.setText("????????????");
			TextViewUtil.setTextSize(tvNoScore, tvScoreAftSize);
			TextViewUtil.setTextColor(tvNoScore, Color.parseColor("#FFFFFF"));
			llScore.addView(tvNoScore, layoutParams);
		}

		/*ivScore.setImageDrawable(getSoreMark(cinemaBean.score));
		if (ivScore.getVisibility() != View.VISIBLE) {
			ivScore.setVisibility(View.VISIBLE);
		}*/

		return llItem;
	}
	
	protected View createEmptyView() {
		ScaleImageView ivBill = new ScaleImageView(GlobalContext.get());
		ivBill.setMode(ScaleImageView.MODE_AUTO_HEIGHT);
		ivBill.setScale(1.5f);
		ivBill.setScaleType(ScaleType.CENTER_CROP);
		return ivBill;
	}
	
	private void loadDrawableByUrl(final ImageView ivHead, String uri) {
		Bitmap bitmap = null;
		synchronized (mCachePost) {
			bitmap = mCachePost.get(uri);
		}

		if (bitmap != null) {
			UI2Manager.runOnUIThread(new Runnable1<Bitmap>(bitmap) {

				@Override
				public void run() {
					ivHead.setImageBitmap(mP1);
					ivHead.setVisibility(View.VISIBLE);
				}
			}, 0);
			return;
		}

		ImageLoaderImpl.getInstance().displayImage(uri,ivHead, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				super.onLoadingStarted(imageUri, view);
				((ImageView) view).setImageDrawable(LayouUtil.getDrawable("def_moive"));
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				super.onLoadingFailed(imageUri, view, failReason);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				super.onLoadingComplete(imageUri, view, loadedImage);
				if (loadedImage != null) {
					((ImageView) view).setImageBitmap(loadedImage);
					view.setVisibility(View.VISIBLE);
					synchronized (mCachePost) {
						mCachePost.put(imageUri, loadedImage);
					}
				}
			}
		});
	}
	
	private Drawable getSoreMark(double score) {
		if (score < 1.0f) {
			return LayouUtil.getDrawable("dz_icon_star0");
		} else if (score < 2.0f) {
			return LayouUtil.getDrawable("dz_icon_star1");
		} else if (score < 3.0f) {
			return LayouUtil.getDrawable("dz_icon_star2");
		} else if (score < 4.0f) {
			return LayouUtil.getDrawable("dz_icon_star3");
		} else if (score < 5.0f) {
			return LayouUtil.getDrawable("dz_icon_star4");
		} else if (score < 6.0f) {
			return LayouUtil.getDrawable("dz_icon_star5");
		} else if (score < 7.0f) {
			return LayouUtil.getDrawable("dz_icon_star6");
		} else if (score < 8.0f) {
			return LayouUtil.getDrawable("dz_icon_star7");
		} else if (score < 9.0f) {
			return LayouUtil.getDrawable("dz_icon_star8");
		} else if (score < 10.0f) {
			return LayouUtil.getDrawable("dz_icon_star9");
		} else {
			return LayouUtil.getDrawable("dz_icon_star10");
		}
	}

	@Override
	public void updateItemSelect(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
