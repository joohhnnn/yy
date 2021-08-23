package com.txznet.music.albumModule.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.InterestTag;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.widget.flowlayout.FlowLayout;
import com.txznet.music.widget.flowlayout.TagAdapter;
import com.txznet.music.widget.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by 58295 on 2018/4/20.
 */

public class InterestTagFragment extends BaseFragment {

    private static final String TAG = InterestTagFragment.class.getSimpleName();
    @Bind(R.id.flowlayout)
    TagFlowLayout mFlowLayout;
    @Bind(R.id.tag_title)
    TextView mTvTitle;
    @Bind(R.id.select_btn)
    Button mSelectBtn;
    @Bind(R.id.next_tags)
    Button nextTags;
    private TagAdapter<String> mAdapter;
    private LayoutInflater mInflater;
    //网络获取到的兴趣标签
    private List<InterestTag> interestTags;
    //当前兴趣标签的内容
    private List<String> tags = new ArrayList<>();
    //选中的兴趣标签id
    private List<Integer> tagIds = new ArrayList<>();
    private String mUrl;


    public String getFragmentId() {
        return "InterestTagFragment";
    }


    @Override
    public void reqData() {

    }

    @Override
    public void bindViews() {
        ObserverManage.getObserver().addObserver(this);
        mInflater = LayoutInflater.from(getActivity());
        mAdapter = new TagAdapter<String>(tags) {

            @Override
            public View getView(FlowLayout parent, int position, String tag) {
                TextView tvTag = (TextView) mInflater.inflate(R.layout.item_interest_tag, mFlowLayout, false);
                tvTag.setText(tag);
                return tvTag;
            }

            @Override
            public void onSelected(int position, View view) {
                super.onSelected(position, view);
                tagIds.add(interestTags.get(position).getId());
                if (tagIds.size() > 0) {
                    mSelectBtn.setBackgroundResource(R.drawable.shap_interest_select);
                }
                LogUtil.d("InterestTag", "this is onSelected tag" + interestTags.get(position).getId() + "|" + interestTags.get(position).getName());
            }

            @Override
            public void unSelected(int position, View view) {
                super.unSelected(position, view);
                tagIds.remove((Integer) interestTags.get(position).getId());
                if (tagIds.size() == 0) {
                    mSelectBtn.setBackgroundResource(R.drawable.shap_interest_normal);
                }
                LogUtil.d("InterestTag", "this is unSelected tag" + interestTags.get(position).getId() + "|" + interestTags.get(position).getName());
            }

            @Override
            public int getCount() {


                return super.getCount();
            }

            @Override
            public String getItem(int position) {
                return super.getItem(position);
            }
        };
        mFlowLayout.setAdapter(mAdapter);
        mFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                LogUtil.d("InterestTag", "this is onTagClick tag" + position + "|" + interestTags.get(position).getName());
                return true;
            }
        });
        mFlowLayout.setMaxHeight((int) getResources().getDimension(R.dimen.m270));
        mFlowLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFlowLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                nextTags.setVisibility(mFlowLayout.getPageSize() > 1 ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void initListener() {

    }


    @Override
    public int getLayout() {
        return R.layout.fragment_interest_tag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }


    @OnClick({R.id.select_btn, R.id.next_tags, R.id.skip})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_btn:
                if (tagIds.size() == 0) {
                    ToastUtils.showShortOnUI("你还没有选好你的兴趣标签哦！");
                    return;
                }
                AlbumEngine.getInstance().setUserInterestTag(InterestTagActivity.url, tagIds);
                ReportEvent.reportAddInterestTag(tagIds, Constant.POPUP, Constant.COMMIT_SUCCESS);
                LogUtil.d("InterestTag", "you  select this");
                getActivity().finish();
                ToastUtils.showShortOnUI("收到你的兴趣标签了！");
                break;
            case R.id.next_tags:
//                AlbumEngine.getInstance().queryInterestTag(InterestTagActivity.url);

                //换一页
                mFlowLayout.changeToNextPage();
                break;
            case R.id.skip:
                AlbumEngine.getInstance().skipInterestTag(InterestTagActivity.url);
                ReportEvent.reportAddInterestTag(null, Constant.POPUP, Constant.SKIP);
                LogUtil.d("skipInterestTag", "you will skip");
                getActivity().finish();
                break;
        }
    }

    public void initData(Bundle savedInstanceState) {
        mUrl = InterestTagActivity.url;
        interestTags = InterestTagActivity.data;
        tags.clear();
        if (mUrl != null && mUrl.equals(Constant.GET_INTEREST_TAG)) {
            mTvTitle.setText(R.string.music_interest_title);
        } else {
            mTvTitle.setText(R.string.radio_interest_title);
        }
        LogUtil.d("InterestTag", "this is update" + interestTags.size());
        for (int i = 0; i < interestTags.size(); i++) {
            tags.add(interestTags.get(i).getName());
        }
        mAdapter.notifyDataChanged();
    }

    @Override
    public void update(Observable observable, Object data) {
//        LogUtil.d("InterestTag","this is update");
//        if (data instanceof InfoMessage) {
//            InfoMessage info = (InfoMessage) data;
//            switch (info.getType()){
//                case InfoMessage.RESP_INTEREST_TAG:
//                    if(CollectionUtils.isNotEmpty(tags)){
//                        tags.clear();
//                    }
//                    ReqInterestTag req = (ReqInterestTag)info.getObj();
//                    interestTags = req.getData();
//                    LogUtil.d("InterestTag","this is update"+interestTags.size());
//                    for(int i = 0; i<interestTags.size(); i++){
//                        tags.add(interestTags.get(i).getName());
//                    }
//                    mAdapter.notifyDataChanged();
//                    break;
//            }
//        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ObserverManage.getObserver().deleteObserver(this);
        interestTags = null;
        tags = null;
        tagIds = null;
        mAdapter = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean onBackPressed() {
        AlbumEngine.getInstance().skipInterestTag(InterestTagActivity.url);
        return true;
    }

}
