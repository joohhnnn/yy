package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.google.zxing.WriterException;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.VersionManager;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.HelpDetailListViewData;
import com.txznet.comm.ui.viewfactory.data.HelpDetailListViewData.HelpDetailBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IHelpDetailListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.QRUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 帮助二级列表
 * <p>
 * 2020-08-17 18:00
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class HelpDetailListView extends IHelpDetailListView {

    private static HelpDetailListView sInstance = new HelpDetailListView();

    private List<View> mFocusViews;


    private HelpDetailListView() { }
    public static HelpDetailListView getInstance() {
        return sInstance;
    }

    @Override
    public void updateProgress(int progress, int selection) {
    }

    @Override
    public void release() {
        super.release();
        if (mFocusViews != null) {
            mFocusViews.clear();
        }
    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        HelpDetailListViewData helpListViewData = (HelpDetailListViewData) data;
        WinLayout.getInstance().vTips = helpListViewData.mTitleInfo.titlefix;
        LogUtil.logd(WinLayout.logTag + "HelpDetailListViewData.getView:" + JSONObject.toJSONString(data));

        View view = createHelpDetailListViewNone(helpListViewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = HelpDetailListView.getInstance();
        viewAdapter.cardHeightType = ExtViewAdapter.SIZE_TYPE.MATCH_PARENT;// 内容占卡片最大
        return viewAdapter;
    }


    View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (VersionManager.getInstance().isUseHelpNewTag()) {
                com.txznet.comm.remote.util.ConfigUtil.setShowHelpNewTag(false);
            }
            RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                    RecordWinController.VIEW_HELP_BACK, 0, 0);
        }
    };


    private View createHelpDetailListViewNone(final HelpDetailListViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();

        ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.help_tip_view_detail, (ViewGroup) null);

        ImageView ivBack = view.findViewById(R.id.ivBack);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        ViewGroup container = view.findViewById(R.id.container);

        ViewGroup imgWrap = view.findViewById(R.id.imgWrap);
        ImageView ivQrCode = view.findViewById(R.id.ivQrCode);
        TextView tvQrCodeDesc = view.findViewById(R.id.tvQrCodeDesc);


        ivBack.setOnClickListener(backListener);
        tvTitle.setText(LanguageConvertor.toLocale(viewData.getData().get(0).title));

        view.setLayoutAnimation(ListViewItemAnim.getAnimationController());
        view.setLayoutAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_START);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_REPEAT);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_END);
                }
            }
        });

        mFocusViews = new ArrayList<>();
        for (int i = 1; i < viewData.count; i++) {
            View itemView = createItemView(context, i, viewData.getData().get(i), i != (SizeConfig.pageHelpDetailCount));
            mFocusViews.add(itemView);
            container.addView(itemView);
        }

        if(viewData.getHelpDetailImgBeans().size() > 0){
            final HelpDetailListViewData.HelpDetailImgBean row = viewData.getHelpDetailImgBeans().get(0);
            if(row.img.startsWith("qrcode:")){
                try {
                    ivQrCode.setImageBitmap(QRUtil.createQRCodeBitmap(row.img.replace("qrcode:", ""), (int) context.getResources().getDimension(R.dimen.m200)));
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                ivQrCode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        JSONBuilder data = new JSONBuilder();
                        data.put("title", viewData.getHelpLabel());
                        data.put("url", row.img);
                        data.put("desc", row.text);
                        data.put("isFromFile", false);
                        data.put("from", "detail");
                        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.detail.qrcode", data.toBytes(), null);
                    }
                });
            }
            tvQrCodeDesc.setText(row.text);
        } else {
            imgWrap.setVisibility(View.GONE);
        }

        return view;
    }

	private View createItemView(Context context, int position, HelpDetailBean helpBean, boolean showDivider) {
		View view = LayoutInflater.from(context).inflate(R.layout.help_tip_view_detail_item, (ViewGroup)null);
		TextView tvDesc = view.findViewById(R.id.tvDesc);
		tvDesc.setText(helpBean.title);
		return view;
	}

    @Override
    public void init() {
        super.init();

    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }

    @Override
    public void snapPage(boolean next) {

    }

    @Override
    public List<View> getFocusViews() {
        return mFocusViews;
    }

    /**
     * 是否含有动画
     */
    @Override
    public boolean hasViewAnimation() {
        return true;
    }

    @Override
    public void updateItemSelect(int arg0) {

    }


}
