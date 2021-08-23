package com.txznet.webchat.ui.rearview_mirror.adapter;

import android.content.Context;

import com.txznet.webchat.R;
import com.txznet.webchat.ui.base.adapter.BaseChatMessageAdapter;

/**
 * 后视镜主题聊天界面MessageAdapter
 * Created by J on 2018/4/21.
 */

public class MirrorChatMessageAdapter extends BaseChatMessageAdapter {

    public MirrorChatMessageAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getLayout(final ITEM_TYPE viewType) {
        switch (viewType) {
            case MSG_TYPE_TEXT_LEFT:
                return R.layout.item_mirror_chat_list_left;

            case MSG_TYPE_TEXT_RIGHT:
                return R.layout.item_mirror_chat_list_right;

            case MSG_TYPE_VOICE_LEFT:
                return R.layout.item_mirror_chat_list_left_voice;

            case MSG_TYPE_VOICE_RIGHT:
                return R.layout.item_mirror_chat_list_right_voice;

            case MSG_TYPE_LOC_LEFT:
                return R.layout.item_mirror_chat_list_left_loc;

            case MSG_TYPE_LOC_RIGHT:
                return R.layout.item_mirror_chat_list_right_loc;

            case MSG_TYPE_FILE_LEFT:
                return R.layout.item_mirror_chat_list_left_file;

            case MSG_TYPE_FILE_RIGHT:
                return R.layout.item_mirror_chat_list_right_file;

            default:
                return R.layout.item_mirror_chat_list_left;
        }
    }
}
