package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.DimenUtils;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.widget.PrinterTextView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatFromSysViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatFromSysView;
import com.txznet.resholder.R;
import com.txznet.txz.util.LanguageConvertor;

/**
 * 系统播报文字
 * <p>
 * 2020-08-19
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class ChatFromSysView extends IChatFromSysView {

    private static ChatFromSysView sInstance = new ChatFromSysView();


    private TextView tvContent;

    private ChatFromSysView() {
    }

    public static ChatFromSysView getInstance() {
        return sInstance;
    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        ChatFromSysViewData viewData = (ChatFromSysViewData) data;
        LogUtil.logd(WinLayout.logTag + "ChatFromSysView.getView(ViewData)" + viewData.textContent);

        return createViewNone(viewData);
    }

    public ExtViewAdapter getView(ViewData data, SpannableString spannableString) {
        ChatFromSysViewData viewData = (ChatFromSysViewData) data;
        LogUtil.logd(WinLayout.logTag + "ChatFromSysView.getView(ViewData)" + viewData.textContent + spannableString);

        return createViewNone(viewData, spannableString);
    }

    /**
     * 每个item文字用圈圈起来
     *
     * @param strings
     * @return
     */
    public ExtViewAdapter getView(String[] strings) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.chat_tips_container, (ViewGroup) null);
        ViewGroup container = view.findViewById(R.id.container);

        int paddingLeft = (int) DimenUtils.dp2px(context, 10F);

        for (String line : strings) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.chat_tips_container_item, (ViewGroup) null);
            TextView tvItem = itemView.findViewById(R.id.tvItem);
            tvItem.setText(line);

            if (container.getChildCount() > 0) {
                itemView.setPadding(paddingLeft, 0, 0, 0);
            }
            container.addView(itemView);
        }

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = ViewData.TYPE_CHAT_FROM_SYS;
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = ChatFromSysView.getInstance();
        return adapter;
    }

    private ExtViewAdapter createViewNone(ChatFromSysViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.chat_from_sys_text, (ViewGroup) null);
        tvContent = view.findViewById(R.id.tvContent);
        tvContent.setText(LanguageConvertor.toLocale(viewData.textContent));

        view.setTag(viewData.getType());

        if (viewData.onClickListener != null) {
            view.setOnClickListener(viewData.onClickListener);
        }

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = viewData.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        if (viewData.textContent != null
                && viewData.textContent.length() > 20
                && !isNotInterrupt(viewData.textContent)
                && !viewData.textContent.startsWith("你可以说")) {
            adapter.type = ViewData.TYPE_CHAT_FROM_SYS_INTERRUPT;
            tvContent.setSingleLine(false);
            tvContent.setLineSpacing(0, 1.2F);
        } else {
            tvContent.setSingleLine(true);
            tvContent.setEllipsize(TextUtils.TruncateAt.END);
        }
        adapter.object = ChatFromSysView.getInstance();
        return adapter;
    }

    private ExtViewAdapter createViewNone(ChatFromSysViewData viewData, SpannableString spannableString) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.chat_from_sys_text, (ViewGroup) null);
        tvContent = view.findViewById(R.id.tvContent);
        tvContent.setText(spannableString);

        if (viewData.onClickListener != null) {
            view.setOnClickListener(viewData.onClickListener);
        }

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = viewData.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = ChatFromSysView.getInstance();
        return adapter;
    }

    @Override
    public void init() {
        super.init();
    }

    //切换模式是更新布局参数
    public void onUpdateParams(int styleIndex) {

    }

    private boolean isNotInterrupt(String text) {
        String[] strArr = {"没有找到", "信息不完整", "点击本消息手动修改", "暂时不支持", "我还没有名字", "有新名字了", "我先去学习一下", "我的名字叫"
                , "正在为您搜索", "请检查网络连接是否正常", "的价格是", "空气质量指数", "将为您"};
        for (String str : strArr) {
            if (text.contains(str)) {
                return true;
            }
        }

        LogUtil.logd(WinLayout.logTag + "isNotInterrupt: fasle--" + text);
        return false;
    }

}
