package com.txznet.webchat.ui.base.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.bean.Poi;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.ResourceActionCreator;
import com.txznet.webchat.actions.TtsActionCreator;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.log.L;
import com.txznet.webchat.stores.TXZTtsStore;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.ui.common.WxImageLoader;
import com.txznet.webchat.ui.rearview_mirror.widget.BubbleRelativeLayout;
import com.txznet.webchat.util.FileUtil;
import com.txznet.webchat.util.SmileyParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 聊天信息Adapter基类
 * Created by J on 2018/4/21.
 */

public abstract class BaseChatMessageAdapter
        extends BaseRecyclerViewAdapter<BaseChatMessageAdapter.MsgViewHolder> {
    public static final String LOG_TAG = "BaseChatMessageAdapter";

    /**
     * 消息类型
     */
    protected enum ITEM_TYPE {
        // 普通文本消息
        MSG_TYPE_TEXT_LEFT,
        MSG_TYPE_TEXT_RIGHT,
        // 语音消息
        MSG_TYPE_VOICE_LEFT,
        MSG_TYPE_VOICE_RIGHT,
        // 位置消息
        MSG_TYPE_LOC_LEFT,
        MSG_TYPE_LOC_RIGHT,
        // 文件消息
        MSG_TYPE_FILE_LEFT,
        MSG_TYPE_FILE_RIGHT
    }

    private String mCurrentSession = "";

    private List<WxMessage> mMsgList = new ArrayList<>();

    private Context mContext;

    /**
     * 返回指定item类型的layout
     *
     * @param viewType item类型
     * @return 对应的layout id
     * @see BaseChatMessageAdapter.ITEM_TYPE
     */
    protected abstract @LayoutRes
    int getLayout(ITEM_TYPE viewType);

    public BaseChatMessageAdapter(Context context) {
        mContext = context;
    }

    public void setMsgList(List<WxMessage> list) {
        this.mMsgList = list;
    }

    public List<WxMessage> getMsgList() {
        return mMsgList;
    }

    public void setSession(String sessionID) {
        mCurrentSession = sessionID;
        mMsgList = WxMessageStore.getInstance().getMessageList(sessionID);

        notifyDataSetChanged();
    }

    public String getCurrentSession() {
        return mCurrentSession;
    }


    @Override
    public int getItemViewType(int position) {
        WxMessage msg = mMsgList.get(position);

        boolean isSelfMsg = isSelfMsg(msg);
        ITEM_TYPE msgType = isSelfMsg ? ITEM_TYPE.MSG_TYPE_TEXT_RIGHT
                : ITEM_TYPE.MSG_TYPE_TEXT_LEFT;

        switch (msg.mMsgType) {
            case WxMessage.MSG_TYPE_VOICE:
                msgType = isSelfMsg ? ITEM_TYPE.MSG_TYPE_VOICE_RIGHT
                        : ITEM_TYPE.MSG_TYPE_VOICE_LEFT;
                break;

            case WxMessage.MSG_TYPE_LOCATION:
                msgType = isSelfMsg ? ITEM_TYPE.MSG_TYPE_LOC_RIGHT
                        : ITEM_TYPE.MSG_TYPE_LOC_LEFT;
                break;

            case WxMessage.MSG_TYPE_FILE:
                // 只有开启了文件消息支持才显示文件样式, 否则按普通文本样式显示
                if (WxConfigStore.getInstance().isFileMsgEnabled()) {
                    msgType = isSelfMsg ? ITEM_TYPE.MSG_TYPE_FILE_RIGHT
                            : ITEM_TYPE.MSG_TYPE_FILE_LEFT;
                }
                break;
        }

        return msgType.ordinal();
    }

    @Override
    public MsgViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ITEM_TYPE itemType = ITEM_TYPE.values()[viewType];
        View v = LayoutInflater.from(mContext).inflate(getLayout(itemType), null);

        switch (itemType) {
            case MSG_TYPE_TEXT_LEFT:
                return new TextMsgViewHolder(v, ITEM_TYPE.MSG_TYPE_TEXT_LEFT);

            case MSG_TYPE_TEXT_RIGHT:
                return new TextMsgViewHolder(v, ITEM_TYPE.MSG_TYPE_TEXT_RIGHT);

            case MSG_TYPE_VOICE_LEFT:
                return new VoiceMsgViewHolder(v, ITEM_TYPE.MSG_TYPE_VOICE_LEFT);

            case MSG_TYPE_VOICE_RIGHT:
                return new VoiceMsgViewHolder(v, ITEM_TYPE.MSG_TYPE_VOICE_RIGHT);

            case MSG_TYPE_LOC_LEFT:
                return new LocMsgViewHolder(v, ITEM_TYPE.MSG_TYPE_LOC_LEFT);

            case MSG_TYPE_LOC_RIGHT:
                return new LocMsgViewHolder(v, ITEM_TYPE.MSG_TYPE_LOC_RIGHT);

            case MSG_TYPE_FILE_LEFT:
                return new FileMsgViewHolder(v, ITEM_TYPE.MSG_TYPE_FILE_LEFT);

            case MSG_TYPE_FILE_RIGHT:
                return new FileMsgViewHolder(v, ITEM_TYPE.MSG_TYPE_FILE_RIGHT);

            default:
                return new TextMsgViewHolder(v, ITEM_TYPE.MSG_TYPE_TEXT_LEFT);
        }
    }

    private boolean isSelfMsg(WxMessage msg) {
        return WxContactStore.getInstance().getLoginUser().mUserOpenId.equals(msg.mSenderUserId);
    }

    @Override
    public void onBindViewHolder(MsgViewHolder holder, int position) {
        final WxMessage msg = mMsgList.get(holder.getAdapterPosition());
        holder.bindMessage(msg);
    }

    @Override
    public int getItemCount() {
        return mMsgList == null ? 0 : mMsgList.size();
    }

    @Override
    protected void onItemClick(int position) {
        if (System.currentTimeMillis() - mLastRepeatMsgTime > 1000) {
            WxMessage msg = mMsgList.get(position);
            TtsActionCreator.get().repeatMessage(msg);
            mLastRepeatMsgTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onItemLongClick(int position) {

    }

    class MsgViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_car_chat_msg_avatar)
        ImageView mIvAvatar;
        @Bind(R.id.view_car_chat_msg_bubble)
        BubbleRelativeLayout mViewContainer;

        ITEM_TYPE mItemType = ITEM_TYPE.MSG_TYPE_TEXT_LEFT;
        WxMessage msg;

        public MsgViewHolder(View itemView, ITEM_TYPE itemType) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mItemType = itemType;
            initListener();
        }

        public void bindMessage(WxMessage msg) {
            this.msg = msg;

            // 加载用户头像
            WxContact con = WxContactStore.getInstance().getContact(msg.mSenderUserId);
            if (con != null) {
                WxImageLoader.loadHead(mContext, con, mIvAvatar);
            }

            // 设置焦点状态
            if (isOnFocus(getAdapterPosition())) {
                mViewContainer.setEnablePushEffect(false);
                if (isSelfMsg(msg)) {
                    if (WxMessage.MSG_TYPE_FILE == msg.mMsgType) {
                        // 自己发送的文件消息用的是白色气泡
                        mViewContainer.setBubbleResource(
                                R.drawable.src_bubble_right_white_top_focus,
                                R.drawable.src_bubble_right_white_arrow_focus,
                                R.drawable.src_bubble_right_white_bottom_focus
                        );
                    } else {
                        mViewContainer.setBubbleResource(
                                R.drawable.src_bubble_right_top_focus,
                                R.drawable.src_bubble_right_arrow_focus,
                                R.drawable.src_bubble_right_bottom_focus
                        );
                    }
                } else {
                    mViewContainer.setBubbleResource(
                            R.drawable.src_bubble_left_top_focus,
                            R.drawable.src_bubble_left_arrow_focus,
                            R.drawable.src_bubble_left_bottom_focus
                    );
                }
            } else {
                mViewContainer.setEnablePushEffect(true);
                if (isSelfMsg(msg)) {
                    if (WxMessage.MSG_TYPE_FILE == msg.mMsgType) {
                        // 自己发送的文件消息用的是白色气泡
                        mViewContainer.setBubbleResource(
                                R.drawable.src_bubble_right_white_top,
                                R.drawable.src_bubble_right_white_arrow,
                                R.drawable.src_bubble_right_white_bottom
                        );
                    } else {
                        mViewContainer.setBubbleResource(
                                R.drawable.src_bubble_right_top,
                                R.drawable.src_bubble_right_arrow,
                                R.drawable.src_bubble_right_bottom
                        );
                    }
                } else {
                    mViewContainer.setBubbleResource(
                            R.drawable.src_bubble_left_top,
                            R.drawable.src_bubble_left_arrow,
                            R.drawable.src_bubble_left_bottom
                    );
                }
            }
        }

        protected void initListener() {
            mViewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != msg && judgeItemClickInterval()) {
                        TtsActionCreator.get().repeatMessage(msg);
                    }
                }
            });
        }
    }

    class TextMsgViewHolder extends MsgViewHolder {
        @Bind(R.id.tv_car_chat_msg_content)
        TextView mTvContent;


        public TextMsgViewHolder(View itemView, ITEM_TYPE type) {
            super(itemView, type);
            mItemType = type;
        }

        @Override
        public void bindMessage(final WxMessage msg) {
            super.bindMessage(msg);
            mTvContent.setText(SmileyParser.getInstance(mContext).parser(msg.mContent));
        }
    }

    class VoiceMsgViewHolder extends MsgViewHolder {
        @Bind(R.id.iv_car_chat_voice_indicator)
        ImageView mIvVoice;

        AnimationDrawable mVoiceDrawable;

        public VoiceMsgViewHolder(View itemView, ITEM_TYPE type) {
            super(itemView, type);

            mVoiceDrawable = (AnimationDrawable) mIvVoice.getBackground();
        }

        @Override
        public void bindMessage(final WxMessage msg) {
            super.bindMessage(msg);

            if (msg.mMsgId == TXZTtsStore.getInstance().getBroadcastingMessage()) {
                startVoiceAnimation();
            } else {
                stopVoiceAnimation();
            }
        }

        public void startVoiceAnimation() {
            AppLogic.runOnUiGround(mStartAnimTask);
        }

        public void stopVoiceAnimation() {
            AppLogic.runOnUiGround(mStopAnimTask);
        }

        private Runnable mStartAnimTask = new Runnable() {
            @Override
            public void run() {
                mVoiceDrawable.start();
            }
        };

        private Runnable mStopAnimTask = new Runnable() {
            @Override
            public void run() {
                if (mVoiceDrawable.isRunning()) {
                    int drawbleId = ITEM_TYPE.MSG_TYPE_VOICE_LEFT == mItemType ?
                            R.drawable.src_voice_left : R.drawable.src_voice_right;
                    mVoiceDrawable = (AnimationDrawable) mContext.getResources()
                            .getDrawable(drawbleId);

                    mIvVoice.setBackground(mVoiceDrawable);
                }
            }
        };
    }

    class LocMsgViewHolder extends MsgViewHolder {
        @Bind(R.id.tv_car_chat_msg_content)
        TextView mTvContent;
        @Bind(R.id.fl_car_chat_msg_action)
        FrameLayout mFlAction;

        public LocMsgViewHolder(View itemView, ITEM_TYPE type) {
            super(itemView, type);
        }

        @Override
        protected void initListener() {
            super.initListener();

            mFlAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (null != msg && judgeItemClickInterval()) {
                        final Poi poi = new Poi();
                        poi.setName("地图选点");
                        poi.setGeoinfo(msg.mAddress);
                        poi.setLat(msg.mLatitude);
                        poi.setLng(msg.mLongtitude);
                        TXZNavManager.getInstance().navToLoc(poi);
                    }
                }
            });
        }

        @Override
        public void bindMessage(final WxMessage msg) {
            super.bindMessage(msg);

            mTvContent.setText(SmileyParser.getInstance(mContext).parser(msg.mContent));
        }
    }

    class FileMsgViewHolder extends MsgViewHolder {
        @Bind(R.id.iv_car_chat_msg_file_icon)
        ImageView mIvFileIcon;
        @Bind(R.id.tv_car_chat_msg_file_name)
        TextView mTvFileName;
        @Bind(R.id.tv_car_chat_msg_file_size)
        TextView mTvFileSize;
        @Bind(R.id.tv_car_chat_msg_file_size_indicator)
        TextView mTvFileUnsupportIndicator;
        @Bind(R.id.rl_car_chat_msg_file_stat_container)
        RelativeLayout mRlStatContainer;
        @Bind(R.id.iv_car_chat_msg_file_stat_icon)
        ImageView mIvStatIcon;

        public FileMsgViewHolder(View itemView, ITEM_TYPE type) {
            super(itemView, type);
        }

        @Override
        protected void initListener() {
            mViewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (WxConfigStore.getInstance()
                            .isFileSuffixSupported(FileUtil.getFileSuffix(msg.mFileName))
                            && judgeItemClickInterval()) {
                        repeatFileMessage(msg);
                    }
                }
            });
        }

        @Override
        public void bindMessage(final WxMessage msg) {
            super.bindMessage(msg);

            // 设置文件图标
            mIvFileIcon.setImageResource(FileUtil.getFileIcon(msg.mFileName));

            mTvFileName.setText(msg.mFileName);
            mTvFileSize.setText(FileUtil.getUIFileSize(msg.mFileSize));

            // 设置文件状态
            if (ResourceActionCreator.get().isFileDownloaded(msg)) {
                mRlStatContainer.setVisibility(View.GONE);
                mTvFileUnsupportIndicator.setText("");
            } else {
                mRlStatContainer.setVisibility(View.VISIBLE);


                if (!WxConfigStore.getInstance().isFileSuffixSupported(FileUtil.getFileSuffix(msg
                        .mFileName))) {
                    // 不支持打开的文件类型
                    mTvFileUnsupportIndicator.setText(R.string.lb_file_msg_indicator_maltype);
                    mIvStatIcon.setImageResource(R.drawable.transparent_background);
                } else if (!WxConfigStore.getInstance().isFileSizeSupported(msg.mFileSize)) {
                    // 文件大小超出限制
                    mTvFileUnsupportIndicator.setText(R.string.lb_file_msg_indicator_oversize);
                    mIvStatIcon.setImageResource(R.drawable.transparent_background);
                } else {
                    mTvFileUnsupportIndicator.setText("");

                    if (ResourceActionCreator.get().isFileDownloading(msg)) {
                        mIvStatIcon.setImageResource(R.drawable.ic_file_download_processing);
                    } else if (ResourceActionCreator.get().isFileDownloadFailed(msg)) {
                        mIvStatIcon.setImageResource(R.drawable.ic_file_download_error);
                    } else {
                        mIvStatIcon.setImageResource(R.drawable.ic_file_download_waiting);
                    }
                }
            }
        }

        private void repeatFileMessage(final WxMessage msg) {
            // 若文件已下载成功, 直接提示打开
            if (ResourceActionCreator.get().isFileDownloaded(msg) && new File(msg.mFilePath)
                    .exists()) {
                FileUtil.openFile(msg.mFilePath);
            } else if (isFileDownloadSupported(msg)) {
                if (!ResourceActionCreator.get().isFileDownloading(msg)) {
                    ResourceActionCreator.get().downloadFile(msg);
                }
            }
        }

        private boolean isFileDownloadSupported(final WxMessage msg) {
            String suffix = FileUtil.getFileSuffix(msg.mFileName);
            if (!WxConfigStore.getInstance().isFileSuffixSupported(suffix)) {
                L.d("CarChatMessageAdapter", "file suffix not supported: " + suffix);
                return false;
            }

            if (!WxConfigStore.getInstance().isFileSizeSupported(msg.mFileSize)) {
                L.d("CarChatMessageAdapter", "file size too large: " + msg.mFileSize);
                return false;
            }

            return true;
        }
    }

    private long mLastRepeatMsgTime = 0;

    private boolean judgeItemClickInterval() {
        if (System.currentTimeMillis() - mLastRepeatMsgTime > 1000) {
            mLastRepeatMsgTime = System.currentTimeMillis();
            return true;
        }

        return false;
    }

    @Override
    public void onNavGainFocus(Object rawFocus, int operation) {
        super.onNavGainFocus(rawFocus, operation);

        // 获取方控焦点时，总是将焦点设置到最后一条消息上
        setCurrentFocusPosition(getItemCount() - 1);
    }
}
