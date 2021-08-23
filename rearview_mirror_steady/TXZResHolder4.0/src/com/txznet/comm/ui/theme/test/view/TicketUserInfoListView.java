package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.data.TicketUserInfoListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITicketUserInfoListView;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 说明：
 *
 * @author xiaolin
 * create at 2020-08-26 14:51
 */
public class TicketUserInfoListView extends ITicketUserInfoListView {

    private static TicketUserInfoListView sInstance = new TicketUserInfoListView();

    public static TicketUserInfoListView getInstance() {
        return sInstance;
    }

    private TextView tvName;
    private TextView tvIdCard;
    private TextView tvPhone;
    private Button btnCommit;

    private TicketUserInfoListViewData mViewData;

    private boolean isName = false;
    private boolean isIdNum = false;
    private boolean isMoNum = false;

    public class ViewHolder {

        private TicketUserInfoListViewData.SeatBean seatBean;
        View wrapLayout;
        TextView tvSeatType;
        TextView tvTicketCount;
        TextView tvPrice;

        public void init(TicketUserInfoListViewData.SeatBean bean) {
            this.seatBean = bean;
            if (bean == null) {
                wrapLayout.setVisibility(View.INVISIBLE);
            } else {
                tvSeatType.setText(bean.name);
                tvTicketCount.setText(bean.number + "张");
                tvPrice.setText(bean.price);

                wrapLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (ViewHolder holder : viewHolders) {
                            holder.setSelect(false);
                        }
                        setSelect(true);
                    }
                });
            }
        }

        public void setSelect(boolean select) {
            if (select) {
                wrapLayout.setBackgroundResource(R.drawable.item_setlected);
                mSeatBean = seatBean;
            } else {
                wrapLayout.setBackgroundResource(R.drawable.xml_bg_form_ticket_seat);
            }
        }
    }

    private TicketUserInfoListViewData.SeatBean mSeatBean;// 选中的座位
    private List<ViewHolder> viewHolders = new ArrayList<>();

    @Override
    public void updateProgress(int i, int i1) {

    }

    @Override
    public void snapPage(boolean b) {

    }

    @Override
    public void updateItemSelect(int i) {

    }

    @Override
    public void init() {
        super.init();
        LogUtil.logd(WinLayout.logTag + "TicketUserInfoListView.init()");
    }

    @Override
    public ExtViewAdapter getView(ViewData viewData) {
        TicketUserInfoListViewData data = (TicketUserInfoListViewData) viewData;
        mViewData = data;

        WinLayout.getInstance().vTips = null;
        LogUtil.logd(WinLayout.logTag + "TicketUserInfoListView.getView() viewData:" + JSONObject.toJSONString(viewData));

        View view = createView(data);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = TrainTicketListView.getInstance();
        viewAdapter.cardHeightType = ExtViewAdapter.SIZE_TYPE.MATCH_PARENT;// 内容占卡片最大
        viewAdapter.showRecordView = false;// 不显示录音图标
        return viewAdapter;
    }

    public View createView(final TicketUserInfoListViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.ticket_user_info, (ViewGroup) null);

        tvName = view.findViewById(R.id.tvName);
        final TextView tvErrorName = view.findViewById(R.id.tvErrorName);
        tvIdCard = view.findViewById(R.id.tvIdCard);
        final TextView tvErrorIdCard = view.findViewById(R.id.tvErrorIdCard);
        tvPhone = view.findViewById(R.id.tvPhone);
        final TextView tvErrorPhone = view.findViewById(R.id.tvErrorPhone);

        btnCommit = view.findViewById(R.id.btnCommit);
        ImageView ivBack = view.findViewById(R.id.ivBack);
        ViewGroup container = view.findViewById(R.id.container);

        final List<TicketUserInfoListViewData.UseInfoBean> infoBeans = viewData.getUserInfoItems();

        tvName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextOutLen(s.toString(), 16)) {
                    String name = s.toString().substring(0, s.length() - 1);
                    s.replace(s.length() - 1, s.length(), "");
                }
                for (int i = 0; i < s.length(); i++) {
                    if (!checkName(s.charAt(i))) {
                        tvErrorName.setText("*姓名只能包含中文或英文");
                        tvName.setTextColor(Color.RED);
                        isName = false;
                        return;
                    }
                }
                boolean matchFlag = false;
                String currentId = "";
                String currentPhone = "";
                for (int i = 0; i < infoBeans.size(); i++) {
                    if (s.toString().equals(infoBeans.get(i).name)) {
                        matchFlag = true;
                        currentId = infoBeans.get(i).idNumber;
                        currentPhone = infoBeans.get(i).phone;
                        break;
                    }
                }
                if (matchFlag) {
                    tvIdCard.setText(currentId);
                    tvPhone.setText(currentPhone);
                }
                isName = s.length() > 0;
                tvErrorName.setText("");
                setTvSearch();
            }
        });

        tvIdCard.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    isIdNum = false;
                    tvErrorIdCard.setText("");
                    return;
                }
                if (!checkIdNum(s.toString(), false)) {
                    tvErrorIdCard.setText("请输入正确的身份证号码");
                    isIdNum = false;
                } else {
                    if (s.length() >= 18) {
                        isIdNum = true;
                    } else {
                        isIdNum = false;
                    }
                    tvErrorIdCard.setText("");
                }
                setTvSearch();
            }
        });

        tvPhone.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!checkNum(s.toString())) {
                    tvErrorPhone.setText("请输入正确的手机号码");
                    isMoNum = false;
                } else {
                    if (s.length() >= 11) {
                        isMoNum = true;
                    } else {
                        isMoNum = false;
                    }
                    tvErrorPhone.setText("");
                }
                setTvSearch();
            }
        });

        tvName.setText(viewData.getName());
        tvIdCard.setText(viewData.getIdNumber());
        tvPhone.setText(viewData.getPhone());

        tvName.setOnClickListener(editUserInfoListener);
        tvIdCard.setOnClickListener(editUserInfoListener);
        tvPhone.setOnClickListener(editUserInfoListener);

        ivBack.setOnClickListener(backListener);

        List<TicketUserInfoListViewData.SeatBean> seatBeans = viewData.getSeatItems();
        int len = seatBeans.size();
        viewHolders = new ArrayList<>();
        for (int i = 0; i < len; i += 2) {// 注意+2
            View itemView = LayoutInflater.from(context).inflate(R.layout.ticket_user_info_seat_item, (ViewGroup) null);

            View wrapLayoutStart = itemView.findViewById(R.id.wrapLayoutStart);
            TextView tvSeatTypeStart = itemView.findViewById(R.id.tvSeatTypeStart);
            TextView tvTicketCountStart = itemView.findViewById(R.id.tvTicketCountStart);
            TextView tvPriceStart = itemView.findViewById(R.id.tvPriceStart);

            View wrapLayoutEnd = itemView.findViewById(R.id.wrapLayoutEnd);
            TextView tvSeatTypeEnd = itemView.findViewById(R.id.tvSeatTypeEnd);
            TextView tvTicketCountEnd = itemView.findViewById(R.id.tvTicketCountEnd);
            TextView tvPriceEnd = itemView.findViewById(R.id.tvPriceEnd);

            ViewHolder holderStart = new ViewHolder();
            holderStart.wrapLayout = wrapLayoutStart;
            holderStart.tvSeatType = tvSeatTypeStart;
            holderStart.tvTicketCount = tvTicketCountStart;
            holderStart.tvPrice = tvPriceStart;

            ViewHolder holderEnd = new ViewHolder();
            holderEnd.wrapLayout = wrapLayoutEnd;
            holderEnd.tvSeatType = tvSeatTypeEnd;
            holderEnd.tvTicketCount = tvTicketCountEnd;
            holderEnd.tvPrice = tvPriceEnd;

            {
                TicketUserInfoListViewData.SeatBean bean = seatBeans.get(i);
                holderStart.init(bean);
                if (i == 0) {
                    holderStart.setSelect(true);
                }
                viewHolders.add(holderStart);
            }

            {
                TicketUserInfoListViewData.SeatBean bean = null;
                if (i + 1 < len) {
                    bean = seatBeans.get(i + 1);
                    viewHolders.add(holderEnd);
                }
                holderEnd.init(bean);
            }

            container.addView(itemView);
        }

        return view;
    }

    View.OnClickListener editUserInfoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String name = tvName.getText().toString();
            String idNumber = tvIdCard.getText().toString();
            String phone = tvPhone.getText().toString();
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("idNumber", idNumber);
            obj.put("phone", phone);
            RecordWin2Manager.getInstance().operateView(
                    TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                    TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_TICKET_USER_INFO_EDIT,
                    0,
                    0,
                    1,
                    obj.toJSONString());
        }
    };

    View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                    TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_TICKET_INFO_BACK, 0, 0);
        }
    };

    View.OnClickListener commitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtil.logd(WinLayout.logTag + "onClick commit");

            // flags: 0x11810542

//            int flags = v.getRootView().getWindogetWindow().getAttributes().flags;
//            LogUtil.logd("RecordWin2True show flags:"+String.format("0x%H", flags));
//
//            int key = 0x01;
//            for(int i=0; i<31; i++){
//                if((key & flags) > 0){
//                    LogUtil.logd("RecordWin2True show flag:"+String.format("0x%H\t\t\t\t", key));
//                }
//                key = key << 1;
//            }

            String name = tvName.getText().toString();
            String idCard = tvIdCard.getText().toString();
            String phone = tvPhone.getText().toString();

            com.alibaba.fastjson.JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", idCard);
                jsonObject.put("name", name);
                jsonObject.put("moNum", phone);
                if (mViewData.isTrain()) {
                    jsonObject.put("seatCode", mSeatBean.seatType);
                    jsonObject.put("seatName", mSeatBean.name);
                    jsonObject.put("seatPrice", mSeatBean.price);
                } else {
                    jsonObject.put("seatCode", mViewData.getFlightInfo().seatCode);
                    jsonObject.put("seatName", mViewData.getFlightInfo().seatName);
                    //jsonObject.put("seatPrice", mSeatBean.price);
                }
                if (mViewData.isTrain()) {
                    jsonObject.put("ticketInfoJson", mViewData.getTrainInfo());
                } else {
                    jsonObject.put("ticketInfoJson", mViewData.getFlightInfo());
                }
                // jsonObject.put("ticketType", ticketType.toString());
                jsonObject.put("isTrain", mViewData.isTrain());
                if (mViewData.getUserInfoItems() != null && mViewData.getUserInfoItems().size() > 0) {
                    for (int i = 0; i < mViewData.getUserInfoItems().size(); i++) {
                        if (idCard.equals(mViewData.getUserInfoItems().get(i).idNumber) && name.equals(mViewData.getUserInfoItems().get(i).name) && phone.equals(mViewData.getUserInfoItems().get(i).phone)) {
                            jsonObject.put("sonAccount", mViewData.getUserInfoItems().get(i).sonAccount);
                            break;
                        }
                    }
                }
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_TICKET_INFO_COMMIT, 0, 0,
                        1, jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                LogUtil.logd("userInfo commit exception");
            }
            btnCommit.setOnClickListener(null);

        }
    };

    //判断文本是否超出指定长度
    public static boolean TextOutLen(String str, int maxLen) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            char item = str.charAt(i);
            if (item < 128) {
                count = count + 1;
            } else {
                count = count + 2;
            }
        }
        return count > maxLen;
    }

    /**
     * 判定输入的是否是汉字
     *
     * @param c 被校验的字符
     * @return true代表是汉字
     *  
     */
    public static boolean checkName(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            String illegal = "￥`~！#%^&*=+\\|{}。；：'\"，<>/？○●★☆☉♀♂※——+++¤╬の〆（）…【】‘’、“”";
            return !illegal.contains((String.valueOf(c)));
        } else {
            return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
        }
    }


    public static boolean checkIdNum(String str, boolean flag) {
        Pattern pattern = Pattern.compile("[0-9]*");
        if (pattern.matcher(str).matches()) {
            return true;
        }
        if (flag) {
            return false;
        }
        return checkIdNum(str.substring(0, str.length() - 1), true) && (str.charAt(str.length() - 1) == 'X');
    }

    public static boolean checkNum(String str) {
        Pattern pattern;
        if (str.length() >= 11) {
            pattern = Pattern.compile("^1[3|4|5|7|8|9]\\d{9}$");
        } else {
            pattern = Pattern.compile("[0-9]*");
        }
        return pattern.matcher(str).matches();
    }

    public void setTvSearch() {
        if (isIdNum && isName && isMoNum) {
            btnCommit.setBackgroundResource(R.drawable.xml_bg_btn_commit_enable);
            btnCommit.setTextColor(Color.WHITE);
            btnCommit.setOnClickListener(commitListener);
        } else {
            btnCommit.setBackgroundResource(R.drawable.xml_bg_btn_commit_disable);
            btnCommit.setTextColor(0x40FFFFFF);
            btnCommit.setOnClickListener(null);
        }
    }

}
