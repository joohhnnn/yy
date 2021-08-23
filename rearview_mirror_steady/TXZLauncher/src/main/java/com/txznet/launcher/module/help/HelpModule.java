package com.txznet.launcher.module.help;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.help.HelpPresenter;
import com.txznet.launcher.domain.help.bean.HelpCommand;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.launcher.utils.RecyclerAdapter;
import com.txznet.launcher.widget.CornerFrameLayout;
import com.txznet.launcher.widget.ItemDecorationAlbumColumns;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrManager;

import java.util.Arrays;

import butterknife.Bind;

/**
 * Created by daviddai on 2018/8/25
 * 帮助界面
 */
public class HelpModule extends BaseModule implements IHelpView {

    private static final String TASK_ID_HELP = "TASK_ID_HELP";

    @Bind(R.id.help_tv_list_title)
    TextView listTitle;

    @Bind(R.id.help_tv_list_pre)
    TextView listPre;

    @Bind(R.id.help_tv_list_page)
    TextView listPage;

    @Bind(R.id.help_tv_list_next)
    TextView listNext;

    @Bind(R.id.help_list_command)
    RecyclerView commandList;

    @Bind(R.id.help_tv_command_tips)
    TextView commandTipsTv;

    @Bind(R.id.help_corner_ly)
    CornerFrameLayout cornerLy;


    private Context mContext;
    private RecyclerAdapter<HelpCommand> commandListAdapter;
    private HelpPresenter mHelpPresenter;

    private int mLastTtsId = -1;
    private String mLastTtsType;

    private TtsUtil.ITtsCallback mTtsCallback = new TtsUtil.ITtsCallback() {
        @Override
        public void onEnd() {
            mLastTtsId = -1;
        }
    };
    private TXZAsrManager.AsrComplexSelectCallback mAsrComplexSelectCallback;

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        mHelpPresenter = new HelpPresenter(this);
        mHelpPresenter.attach();

        mAsrComplexSelectCallback = new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public String getTaskId() {
                return TASK_ID_HELP;
            }

            @Override
            public boolean needAsrState() {
                return true;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                // 触发指令后，刷新退出机制的时间。
                performTimeoutTask(true);

                // 指令的处理
                LogUtil.e("onCommandSelected: type=" + type + ", command=" + command);
                super.onCommandSelected(type, command);
                switch (type) {
                    case "PRE_PAGE":
                        if (mHelpPresenter.isFirstPage()) {
                            /*
                             * 判断用户是否是在第一页中重复喊上一页。
                             * 首先类型要一致，都是第一页。但是只判断这个是不够的，会出现用户不断喊第一次，我们只提示一次"已经是第一页"，然后后面的都不处理了。
                             */
                            if ("FIRST_PAGE".equals(mLastTtsType) && mLastTtsId != -1) {
                                // 当用户在第一页中重复喊上一页的时候，不重复执行。
                                break;
                            }
                            mLastTtsType = "FIRST_PAGE";
                            TtsUtil.cancelSpeak(mLastTtsId);
                            mLastTtsId = TtsUtil.speakText("已经是第一页", mTtsCallback);
                        } else {
                            mLastTtsType = type;
                            mHelpPresenter.prePage();
                            TtsUtil.cancelSpeak(mLastTtsId);
                            mLastTtsId = TtsUtil.speakText("已切换为上一页");
                        }
                        break;
                    case "NEXT_PAGE":
                        if (mHelpPresenter.isLastPage()) {
                            if ("LAST_PAGE".equals(mLastTtsType) && mLastTtsId != -1) {
                                break;
                            }
                            mLastTtsType = "LAST_PAGE";
                            TtsUtil.cancelSpeak(mLastTtsId);
                            mLastTtsId = TtsUtil.speakText("已经是最后一页", mTtsCallback);
                        } else {
                            mLastTtsType = type;
                            mHelpPresenter.nextPage();
                            TtsUtil.cancelSpeak(mLastTtsId);
                            mLastTtsId = TtsUtil.speakText("已切换为下一页");
                        }
                        break;
                    case "FINISH_HELP":
                        LaunchManager.getInstance().launchDesktop();
                        break;
                    case "PAGE_ONE":
                        if (type.equals(mLastTtsType) && mLastTtsId != -1) {
                            break;
                        }
                        mLastTtsType = type;
                        TtsUtil.cancelSpeak(mLastTtsId);
                        if (mHelpPresenter.getCurrentPage() == 1) {
                            mLastTtsId = TtsUtil.speakText("已经是第1页",mTtsCallback);
                        }else {
                            mHelpPresenter.showSpecifiedPage(1);
                            mLastTtsId = TtsUtil.speakText("已切换为第1页", mTtsCallback);
                        }
                        break;
                    case "PAGE_TWO":
                        if (type.equals(mLastTtsType) && mLastTtsId != -1) {
                            break;
                        }
                        mLastTtsType = type;
                        TtsUtil.cancelSpeak(mLastTtsId);
                        if (mHelpPresenter.getCurrentPage() == 2) {
                            mLastTtsId = TtsUtil.speakText("已经是第2页",mTtsCallback);
                        }else {
                            mHelpPresenter.showSpecifiedPage(2);
                            mLastTtsId = TtsUtil.speakText("已切换为第2页", mTtsCallback);
                        }
                        break;
                    case "PAGE_THREE":
                        if (type.equals(mLastTtsType) && mLastTtsId != -1) {
                            break;
                        }
                        mLastTtsType = type;
                        TtsUtil.cancelSpeak(mLastTtsId);
                        if (mHelpPresenter.getCurrentPage() == 3) {
                            mLastTtsId = TtsUtil.speakText("已经是第3页",mTtsCallback);
                        }else {
                            mHelpPresenter.showSpecifiedPage(3);
                            mLastTtsId = TtsUtil.speakText("已切换为第3页", mTtsCallback);
                        }
                        break;
                    case "PAGE_FOUR":
                        if (type.equals(mLastTtsType) && mLastTtsId != -1) {
                            break;
                        }
                        mLastTtsType = type;
                        TtsUtil.cancelSpeak(mLastTtsId);
                        if (mHelpPresenter.getCurrentPage() == 4) {
                            mLastTtsId = TtsUtil.speakText("已经是第4页",mTtsCallback);
                        }else {
                            mHelpPresenter.showSpecifiedPage(4);
                            mLastTtsId = TtsUtil.speakText("已切换为第4页", mTtsCallback);
                        }
                        break;
                    case "PAGE_FIVE":
                        if (type.equals(mLastTtsType) && mLastTtsId != -1) {
                            break;
                        }
                        mLastTtsType = type;
                        TtsUtil.cancelSpeak(mLastTtsId);
                        if (mHelpPresenter.getCurrentPage() == 5) {
                            mLastTtsId = TtsUtil.speakText("已经是第5页",mTtsCallback);
                        }else {
                            mHelpPresenter.showSpecifiedPage(5);
                            mLastTtsId = TtsUtil.speakText("已切换为第5页", mTtsCallback);
                        }
                        break;
                }
            }
        }.addCommand("PRE_PAGE", "上一页")
                .addCommand("NEXT_PAGE", "下一页")
                .addCommand("FINISH_HELP", "返回桌面");

        // 添加指定某一页的command
        mAsrComplexSelectCallback.addCommand("PAGE_ONE", "第一页", "第1页")
                .addCommand("PAGE_TWO", "第二页", "第2页")
                .addCommand("PAGE_THREE", "第三页", "第3页")
                .addCommand("PAGE_FOUR", "第四页", "第4页")
                .addCommand("PAGE_FIVE", "第五页", "第5页");
    }

    // 退出界面的任务
    private Runnable closeRunnable = new Runnable() {
        @Override
        public void run() {
            LaunchManager.getInstance().showAtImageBottom("");
            LaunchManager.getInstance().launchDesktop();
        }
    };


    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        // 保存context
        mContext = context;

        // 加载布局，绑定view
        View content = LayoutInflater.from(context).inflate(R.layout.module_help, parent, false);
        listTitle = content.findViewById(R.id.help_tv_list_title);
        listPre = content.findViewById(R.id.help_tv_list_pre);
        listPage = content.findViewById(R.id.help_tv_list_page);
        listNext = content.findViewById(R.id.help_tv_list_next);
        commandList = content.findViewById(R.id.help_list_command);
        commandTipsTv = content.findViewById(R.id.help_tv_command_tips);
        cornerLy = content.findViewById(R.id.help_corner_ly);

        // 初始化界面
        initView();

        // 展示第一页
        mHelpPresenter.showSpecifiedPage(1);
        return content;
    }

    /**
     * module第一次显示后会被LaunchManager加入到cache中，之后显示module都是执行refreshView方法。
     */
    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        // 展示第一页
        mHelpPresenter.showSpecifiedPage(1);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 注册帮助界面才有的指令
        regAsrCmd();

        // 在一个时间点之后关闭帮助界面。现在的是2分钟
        performTimeoutTask(true);
    }

    /**
     * module被隐藏的时候会执行该方法。
     */
    @Override
    public void onPreRemove() {
        super.onPreRemove();
        // 反注册帮助界面的指令
        unRegAsrCmd();

        // 关闭退出任务。
        performTimeoutTask(false);
    }

    private void performTimeoutTask(boolean run) {
        AppLogic.removeUiGroundCallback(closeRunnable);
        if (run) {
            AppLogic.runOnUiGround(closeRunnable, PreferenceUtil.getInstance().getLong(PreferenceUtil.KEY_WECHAT_QR_TIMEOUT, PreferenceUtil.DEFAULT_WECHAT_QR_TIMEOUT));
        }
    }

    /**
     * 注册免唤醒词
     */
    private void regAsrCmd() {
        TXZAsrManager.getInstance().useWakeupAsAsr(mAsrComplexSelectCallback);
    }

    /**
     * 反注册免唤醒词
     */
    private void unRegAsrCmd() {
        TXZAsrManager.getInstance().recoverWakeupFromAsr(TASK_ID_HELP);
    }

    @Override
    @UiThread
    public void showPage(HelpCommand[] commands, int currentPage, int firstPage, int lastPage) {
        setPage(currentPage, firstPage, lastPage);
        setCommandList(commands);
    }

    // 私有方法

    /**
     * view 的初始化
     */
    private void initView() {
        // title的设置
        // 没办法获取到设置的称呼，固定成小O小O.如果以后有办法获取到设置的昵称，记得这个方法要在refreshView的时候也执行。不让昵称是不会刷新的。
        setTitle("小O小O");

        // 底部提示的设置
        String commandTips = "你可以说 “上一页”、“下一页”、“返回桌面”";
        SpannableString commandTipsSpannable = new SpannableString(commandTips);
        commandTipsSpannable.setSpan(new ForegroundColorSpan(0xFFFFFFFF), 6, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        commandTipsSpannable.setSpan(new ForegroundColorSpan(0xFFFFFFFF), 12, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        commandTipsSpannable.setSpan(new ForegroundColorSpan(0xFFFFFFFF), 18, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        commandTipsTv.setText(commandTipsSpannable);

        // list的设置
        commandList.setLayoutManager(new LinearLayoutManager(mContext));
        commandList.addItemDecoration(new ItemDecorationAlbumColumns(2, 1));
        commandListAdapter = new RecyclerAdapter<HelpCommand>(mContext, null, R.layout.layout_help_command_item) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder helper, int position, HelpCommand item) {
                ((ImageView) helper.getView(R.id.iv_icon)).setImageResource(item.getIcon());
                ((TextView) helper.getView(R.id.tv_command_type)).setText(item.getType());
                ((TextView) helper.getView(R.id.tv_command_detail)).setText(item.getCommands());
            }
        };
        commandList.setAdapter(commandListAdapter);

        // 圆角布局的设置
        cornerLy.setCorner(10);
    }

    /**
     * 设置title的文本，包括动态的昵称设置和昵称设置特殊颜色
     *
     * @param nick 昵称
     */
    private void setTitle(@NonNull String nick) {
        String title = "请先说“" + nick + "”唤醒语音";
        SpannableString titleSpannable = new SpannableString(title);
        titleSpannable.setSpan(new ForegroundColorSpan(0xFFFFFFFF), 4, 4 + nick.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        listTitle.setText(titleSpannable);
    }

    /**
     * 页数相关view的设置
     *
     * @param currentPage 当前页数
     * @param firstPage   第一页
     * @param lastPage    最后一页
     */
    @UiThread
    private void setPage(int currentPage, int firstPage, int lastPage) {
        // 设置页数
        listPage.setText(currentPage + "/" + lastPage);

        // 根据是否是第一页，最后一页。来设置"上一页"和"下一页"的颜色。
        if (currentPage == firstPage) {
            listPre.setTextColor(0x7FFFFFFF);
            listNext.setTextColor(0xFFFFFFFF);
        } else if (currentPage == lastPage) {
            listPre.setTextColor(0xFFFFFFFF);
            listNext.setTextColor(0x7FFFFFFF);
        } else {
            listPre.setTextColor(0xFFFFFFFF);
            listNext.setTextColor(0xFFFFFFFF);
        }
    }

    /**
     * 列表相关view的设置
     *
     * @param commands 要显示的资源列表
     */
    private void setCommandList(HelpCommand[] commands) {
        commandListAdapter.refresh(Arrays.asList(commands));
    }
}
