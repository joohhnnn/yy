package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.AudioListViewData;
import com.txznet.comm.ui.viewfactory.data.AudioListViewData.AudioBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IAudioListView;
import com.txznet.resholder.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * 音频列表：音乐、小说
 * <p>
 * 唤起：
 * 1. 我想听青花瓷
 * 2. 我想听小说
 * <p>
 * 2020-08-13 10:00
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class AudioListView extends IAudioListView {

    private static AudioListView sInstance = new AudioListView();

    private List<View> mItemViews;

    private AudioListView() {
    }

    public static AudioListView getInstance() {
        return sInstance;
    }

    @Override
    public void updateProgress(int progress, int selection) {

    }

    @Override
    public void release() {
        super.release();
        if (mItemViews != null) {
            mItemViews.clear();
        }

    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        AudioListViewData audioListViewData = (AudioListViewData) data;
        WinLayout.getInstance().vTips = audioListViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "audioListViewData.isMusic: " + audioListViewData.isMusic + "audioListViewData.vTips: " + audioListViewData.vTips);

        View view = createViewNone(audioListViewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = AudioListView.getInstance();
        return viewAdapter;
    }

    //当前列表是否有有声书标签
    private boolean isHasTag(AudioListViewData audioListViewData) {
        for (int i = 0; i < audioListViewData.count; i++) {
            AudioBean audioBean = audioListViewData.getData().get(i);
            if (audioBean.novelStatus != AudioBean.NOVEL_STATUS_INVALID ||
                    audioBean.paid ||
                    audioBean.lastPlay ||
                    audioBean.latest) {
                return true;
            }
        }
        return false;
    }

    private View createViewNone(AudioListViewData viewData) {
        ArrayList<AudioBean> competitionBeans = viewData.getData();
        Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = viewData.mTitleInfo.maxPage;
        int curPage = viewData.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        mItemViews = new ArrayList<>();

        /*音乐列表，没有标签*/
        if (viewData.isMusic) {
            for (int i = 0; i < viewData.count; i++) {
                AudioBean row = competitionBeans.get(i);
                View itemView = createItemView(context, i, row, i != viewData.count - 1);
                container.addView(itemView);
                mItemViews.add(itemView);
            }

            // 添加空视图填充空间
            int re = SizeConfig.pageAudioCount - viewData.count;
            for (int i = 0; i < re; i++) {
                View itemView = createItemView(context, i, null, false);
                container.addView(itemView);
            }
        }
        /*电台，可能有标签*/
        else {
            for (int i = 0; i < viewData.count; i++) {
                AudioBean row = competitionBeans.get(i);
                View itemView = createItemViewTag(context, i, row, i != viewData.count - 1);
                container.addView(itemView);
                mItemViews.add(itemView);
            }

            // 添加空视图填充空间
            int re = SizeConfig.pageAudioTagCount - viewData.count;
            for (int i = 0; i < re; i++) {
                View itemView = createItemViewTag(context, i, null, false);
                container.addView(itemView);
            }
        }

        return view;
    }

    private View createItemView(Context context, int position, AudioBean row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_list_item, (ViewGroup) null);

        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDesc = view.findViewById(R.id.tvDesc);
        View divider = view.findViewById(R.id.divider);

        String title = String.format(Locale.getDefault(),
                "%d. %s", position + 1, row.title);
        tvTitle.setText(title);
        tvDesc.setText(row.name);

        // 列表分隔线
        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        // 设置列表点击
        ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, position);

        return view;
    }

    private View createItemViewTag(Context context, int position, AudioBean row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_list_item_tag, (ViewGroup) null);

        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDesc = view.findViewById(R.id.tvDesc);
        ImageView ivTagLast = view.findViewById(R.id.ivTagLast);
        ImageView ivTagLatest = view.findViewById(R.id.ivTagLatest);
        ImageView ivTagPaid = view.findViewById(R.id.ivTagPaid);
        ImageView ivTagStatus = view.findViewById(R.id.ivTagStatus);
        View divider = view.findViewById(R.id.divider);

        String title = String.format(Locale.getDefault(),
                "%d. %s", position + 1, row.title);
        tvTitle.setText(title);
        tvDesc.setText(row.name);

        // tag 听
        if (!row.lastPlay) {
            ivTagLast.setVisibility(View.GONE);
        }

        // tag 新
        if (!row.latest) {
            ivTagLatest.setVisibility(View.GONE);
        }

        // tag 付
        if (!row.paid) {
            ivTagPaid.setVisibility(View.GONE);
        }

        // tag 连、完
        switch (row.novelStatus) {
            case AudioBean.NOVEL_STATUS_SERILIZE:
                ivTagStatus.setImageResource(R.drawable.music_tag_status1);
                break;
            case AudioBean.NOVEL_STATUS_END:
                ivTagStatus.setImageResource(R.drawable.music_tag_status2);
                break;
            case AudioBean.NOVEL_STATUS_INVALID:
            default:
                ivTagStatus.setVisibility(View.GONE);
        }

        // 列表分隔线
        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        // 设置列表点击
        ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, position);
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
        LogUtil.logd("update snap " + next);
    }

    @Override
    public List<View> getFocusViews() {
        return mItemViews;
    }

    /**
     * 是否含有动画
     *
     * @return
     */
    @Override
    public boolean hasViewAnimation() {
        return true;
    }

    @Override
    public void updateItemSelect(int index) {
        LogUtil.logd(WinLayout.logTag + "train updateItemSelect " + index);
        showSelectItem(index);
    }

    private void showSelectItem(int index) {
        for (int i = 0; i < mItemViews.size(); i++) {
            if (i == index) {
                mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
            } else {
                mItemViews.get(i).setBackground(null);
            }
        }
    }


}
