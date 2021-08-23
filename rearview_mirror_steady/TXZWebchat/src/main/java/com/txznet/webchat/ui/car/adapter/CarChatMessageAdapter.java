package com.txznet.webchat.ui.car.adapter;

import android.content.Context;

import com.txznet.webchat.R;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.adapter.BaseChatMessageAdapter;

/**
 * 车机主题聊天界面MessageAdapter
 * Created by ASUS User on 2016/3/25.
 */
public class CarChatMessageAdapter extends BaseChatMessageAdapter {
    public static final String LOG_TAG = "CarChatMessageAdapter";

    private boolean bPortraitTheme;

    public CarChatMessageAdapter(Context context) {
        super(context);
        bPortraitTheme = WxThemeStore.get().isPortraitTheme();

    }

    @Override
    protected int getLayout(final ITEM_TYPE viewType) {
        if (bPortraitTheme) {
            switch (viewType) {
                case MSG_TYPE_TEXT_LEFT:
                    return R.layout.item_car_portrait_chat_list_left;

                case MSG_TYPE_TEXT_RIGHT:
                    return R.layout.item_car_portrait_chat_list_right;

                case MSG_TYPE_VOICE_LEFT:
                    return R.layout.item_car_portrait_chat_list_left_voice;

                case MSG_TYPE_VOICE_RIGHT:
                    return R.layout.item_car_portrait_chat_list_right_voice;

                case MSG_TYPE_LOC_LEFT:
                    return R.layout.item_car_portrait_chat_list_left_loc;

                case MSG_TYPE_LOC_RIGHT:
                    return R.layout.item_car_portrait_chat_list_right_loc;

                case MSG_TYPE_FILE_LEFT:
                    return R.layout.item_car_portrait_chat_list_left_file;

                case MSG_TYPE_FILE_RIGHT:
                    return R.layout.item_car_portrait_chat_list_right_file;

                default:
                    return R.layout.item_car_portrait_chat_list_left;
            }
        } else {
            switch (viewType) {
                case MSG_TYPE_TEXT_LEFT:
                    return R.layout.item_car_chat_list_left;

                case MSG_TYPE_TEXT_RIGHT:
                    return R.layout.item_car_chat_list_right;

                case MSG_TYPE_VOICE_LEFT:
                    return R.layout.item_car_chat_list_left_voice;

                case MSG_TYPE_VOICE_RIGHT:
                    return R.layout.item_car_chat_list_right_voice;

                case MSG_TYPE_LOC_LEFT:
                    return R.layout.item_car_chat_list_left_loc;

                case MSG_TYPE_LOC_RIGHT:
                    return R.layout.item_car_chat_list_right_loc;

                case MSG_TYPE_FILE_LEFT:
                    return R.layout.item_car_chat_list_left_file;

                case MSG_TYPE_FILE_RIGHT:
                    return R.layout.item_car_chat_list_right_file;

                default:
                    return R.layout.item_car_chat_list_left;
            }
        }
    }
}