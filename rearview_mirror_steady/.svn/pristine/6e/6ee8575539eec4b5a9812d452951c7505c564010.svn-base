package com.txznet.music.ui.user;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.music.R;
import com.txznet.music.action.WxPushActionCreator;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.store.WxPushStore;
import com.txznet.music.ui.base.BaseFragment;
import com.txznet.music.ui.favour.FavourFragment;
import com.txznet.music.ui.history.HistoryFragment;
import com.txznet.music.ui.setting.SettingFragment;
import com.txznet.music.ui.subscribe.SubscribeFragment;
import com.txznet.music.ui.webchatpush.WxPushFragment;
import com.txznet.music.util.adapter.RecyclerAdapter;
import com.txznet.music.widget.GridDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 个人中心
 *
 * @author zackzhou
 * @date 2018/12/4,16:34
 */
public class UserFragment extends BaseFragment {
    @Bind(R.id.rv_data)
    RecyclerView mRecyclerView;
//    @Bind(R.id.btn_favour)
//    Button btnFavour;
//    @Bind(R.id.btn_subscribe)
//    Button btnSubscribe;
//    @Bind(R.id.btn_history)
//    Button btnHistory;
//    @Bind(R.id.btn_wx_push)
//    Button btnWxPush;
//    @Bind(R.id.btn_settings)
//    Button btnSettings;

    List<UserItem> mUserItems = new ArrayList<>();

    {
        mUserItems.add(new UserItem(R.drawable.user_center_favour_icon, "我的收藏", v -> {
            new FavourFragment().show(getChildFragmentManager(), "favour");
        }));
        mUserItems.add(new UserItem(R.drawable.user_center_subscribe_icon, "我的订阅", v -> {
            new SubscribeFragment().show(getChildFragmentManager(), "subscribe");
        }));
        mUserItems.add(new UserItem(R.drawable.user_center_history_icon, "历史播放", v -> {
            new HistoryFragment().show(getChildFragmentManager(), "historyMusic");
        }));
        mUserItems.add(new UserItem(R.drawable.user_center_wx_push_icon, "微信推送", v -> {
            new WxPushFragment().show(getChildFragmentManager(), "wxpush");
        }));
        mUserItems.add(new UserItem(R.drawable.user_center_setting_icon, "设置", v -> {
            new SettingFragment().show(getChildFragmentManager(), "setting");
        }));
    }

    @Override
    protected int getLayout() {
        return R.layout.user_fragment;
    }

    @Override
    protected void initView(View view) {


        mRecyclerView.setLayoutManager(new GridLayoutManager(GlobalContext.get(), 3));
//        GridOffsetsItemDecoration decor = new GridOffsetsItemDecoration(GridOffsetsItemDecoration.GRID_OFFSETS_VERTICAL);
//        decor.setHorizontalItemOffsets(getResources().getDimensionPixelOffset(R.dimen.m24));
//        decor.setVerticalItemOffsets(getResources().getDimensionPixelOffset(R.dimen.m24));
//        decor.setOffsetEdge(false);
//        decor.setOffsetLast(false);
//        mRecyclerView.addItemDecoration(decor);
        GridDividerItemDecoration gridDividerItemDecoration = new GridDividerItemDecoration(getResources().getDimensionPixelOffset(R.dimen.m24), getResources().getColor(R.color.transparent));
        mRecyclerView.addItemDecoration(gridDividerItemDecoration);


        mRecyclerView.setAdapter(new RecyclerAdapter<UserItem>(GlobalContext.get(), mUserItems, R.layout.user_item_layout) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder holder, int position, UserItem item) {
                TextView textView = (TextView) holder.getView(R.id.tv_title);

                final Drawable drawable = getResources().getDrawable(item.iconID);
                //设置drawable的位置,宽高
                drawable.setBounds(0, 0, getResources().getDimensionPixelOffset(R.dimen.m64), getResources().getDimensionPixelOffset(R.dimen.m64));
                textView.setCompoundDrawables(null, drawable, null, null);
                textView.setCompoundDrawablePadding(getResources().getDimensionPixelOffset(R.dimen.m24));
                textView.setText(item.name);
                holder.itemView.setOnClickListener(item.mOnClickListener);
            }
        });

        mRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        if (SharedPreferencesUtils.getQrCodeInfoCache() == null) {
            ViewModelProviders.of(this).get(WxPushStore.class).getQrCodeInfo().observe(this, qrCodeInfo -> {

            });
            if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                //有网，获取是否绑定过的状态，如果没有则展示二维码界面，如果有则展示empty界面
                WxPushActionCreator.getInstance().getWxPushQRCode();
            }
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        tvTitle.setText("个人中心");
    }


}
