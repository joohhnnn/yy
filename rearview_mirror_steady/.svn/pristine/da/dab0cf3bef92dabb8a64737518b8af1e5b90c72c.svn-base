package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.dialog.LoadingDialog;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.data.QiWuPhoneRechargeAmountViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IQiWuPhoneRechargeAmountView;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 说明：电话费充值
 *
 * @author xiaolin
 * create at 2020-09-07 10:11
 */
public class QiWuPhoneRechargeAmountView extends IQiWuPhoneRechargeAmountView {

    private static QiWuPhoneRechargeAmountView sInstance = new QiWuPhoneRechargeAmountView();

    public static QiWuPhoneRechargeAmountView getInstance() {
        return sInstance;
    }

    public class ViewHolder {

        private QiWuPhoneRechargeAmountViewData.Amount amount;
        View wrapLayout;
        TextView tvPrice;
        TextView tvUserPrice;

        public void init(final QiWuPhoneRechargeAmountViewData.Amount amount) {
            this.amount = amount;
            if (amount == null) {
                wrapLayout.setVisibility(View.INVISIBLE);
            } else {
                tvPrice.setText(amount.amount + "元");
                tvUserPrice.setText(String.format(Locale.CHINA,
                        "售价￥%.2f",
                        Float.valueOf(amount.userPrice)));

                wrapLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!checkPhoneNum(mPhoneNumber)){
                            return;
                        }

                        mLoadingDialog = new LoadingDialog(UIResLoader.getInstance().getModifyContext(),
                                QiWuPhoneRechargeAmountView.getInstance());
                        mLoadingDialog.setMessage("正在加载...");
                        mLoadingDialog.show();

                        wrapLayout.setBackgroundResource(R.drawable.item_setlected);
                        for(ViewHolder holder : viewHolders){
                            holder.wrapLayout.setOnClickListener(null);
                        }

                        JSONObject jObj = new JSONObject();
                        try {
                            jObj.put("select", true);
                            jObj.put("company", amount.company);
                            jObj.put("price", amount.price);
                            jObj.put("user_price", amount.userPrice);
                            jObj.put("amount", amount.amount);
                            jObj.put("province", amount.province);
                            jObj.put("phone", mPhoneNumber);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        RecordWin2Manager.getInstance().operateView(
                                TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                                TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_QIWU_PHONE_RECHARGE_COMMIT,
                                0, 0,
                                TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_SOURCE_TOUCH,
                                jObj.toString()
                        );

                        LogUtil.logd(WinLayout.logTag + "QiWuPhoneRechargeAmountView commit:" + jObj.toString());
                    }
                });
            }
        }
    }

    private View mView;
    private View mExtView;
    private TextView mTvPhoneNumber;
    private TextView mTvOperator;
    private String mPhoneNumber;
    private Button mBtnDone;
    private LoadingDialog mLoadingDialog;

    private List<ViewHolder> viewHolders = new ArrayList<>();

    @Override
    public ExtViewAdapter getView(ViewData viewData) {
        QiWuPhoneRechargeAmountViewData data = (QiWuPhoneRechargeAmountViewData) viewData;
        LogUtil.logd(WinLayout.logTag + "QiWuPhoneRechargeAmountView.getView() viewData:" + com.alibaba.fastjson.JSONObject.toJSONString(viewData));

        mPhoneNumber = String.valueOf(data.phoneNumber);

        if(mLoadingDialog != null && mLoadingDialog.isShow()){
            mLoadingDialog.dismiss();
        }

        View view = getView(data);
        mExtView = getExtView(data);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.extView = mExtView;
        viewAdapter.extView.setTag(viewAdapter);
        viewAdapter.object = getInstance();
        viewAdapter.cardHeightType = ExtViewAdapter.SIZE_TYPE.MATCH_PARENT;// 内容占卡片最大
        return viewAdapter;
    }

    @Override
    public void modifyNumber() {
        if(mExtView != null) {
            mExtView.setVisibility(View.VISIBLE);
            checkPhoneNum();
        }
    }

    @Override
    public boolean isShowingModifyNumberDialog() {
        return mExtView != null && mExtView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void updateItemSelect(int selection) {
        mLoadingDialog = new LoadingDialog(UIResLoader.getInstance().getModifyContext(),
                QiWuPhoneRechargeAmountView.getInstance());
        mLoadingDialog.setMessage("正在加载...");
        mLoadingDialog.show();

        for(ViewHolder holder : viewHolders){
            holder.wrapLayout.setOnClickListener(null);
        }
        if(selection < viewHolders.size()) {
            viewHolders.get(selection).wrapLayout.setBackgroundResource(R.drawable.item_setlected);
        }
    }

    private View getView(QiWuPhoneRechargeAmountViewData viewData) {
        List<QiWuPhoneRechargeAmountViewData.Amount> amounts = viewData.amountList;

        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.sim_view, (ViewGroup) null);
        mTvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        mTvOperator = view.findViewById(R.id.tvOperator);
        ViewGroup container = view.findViewById(R.id.container);

        setPhoneNumber(mPhoneNumber);
        mTvOperator.setText(amounts.get(0).province + amounts.get(0).company);

        mTvPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExtView.setVisibility(View.VISIBLE);
                checkPhoneNum();
            }
        });

        if(viewData.isLoading){
            mLoadingDialog = new LoadingDialog(context, getInstance());
            mLoadingDialog.setMessage("正在加载...");
            mLoadingDialog.show();
            return view;
        }

        int len = amounts.size();
        viewHolders = new ArrayList<>();
        for (int i = 0; i < len; i += 2) {// 注意+2
            View itemView = LayoutInflater.from(context).inflate(R.layout.sim_price_item, (ViewGroup) null);

            View wrapLayoutStart = itemView.findViewById(R.id.wrapLayoutStart);
            TextView tvPriceStart = itemView.findViewById(R.id.tvPriceStart);
            TextView tvUserPriceStart = itemView.findViewById(R.id.tvUserPriceStart);

            View wrapLayoutEnd = itemView.findViewById(R.id.wrapLayoutEnd);
            TextView tvPriceEnd = itemView.findViewById(R.id.tvPriceEnd);
            TextView tvUserPriceEnd = itemView.findViewById(R.id.tvUserPriceEnd);


            ViewHolder holderStart = new ViewHolder();
            holderStart.wrapLayout = wrapLayoutStart;
            holderStart.tvPrice = tvPriceStart;
            holderStart.tvUserPrice = tvUserPriceStart;

            ViewHolder holderEnd = new ViewHolder();
            holderEnd.wrapLayout = wrapLayoutEnd;
            holderEnd.tvPrice = tvPriceEnd;
            holderEnd.tvUserPrice = tvUserPriceEnd;

            {
                QiWuPhoneRechargeAmountViewData.Amount amount = amounts.get(i);
                holderStart.init(amount);
                viewHolders.add(holderStart);
            }

            {
                QiWuPhoneRechargeAmountViewData.Amount amount = null;
                if (i + 1 < len) {
                    amount = amounts.get(i + 1);
                    viewHolders.add(holderEnd);
                }
                holderEnd.init(amount);
            }

            container.addView(itemView);
        }

        return view;
    }

    private View getExtView(QiWuPhoneRechargeAmountViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.sim_keyboard, (ViewGroup) null);
        // 点击第二个卡片不消失
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        int[] ids = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        Button[] buttons = new Button[10];
        for (int i = 0; i < ids.length; i++) {
            buttons[i] = view.findViewById(ids[i]);
            final int finalI = i;
            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTvOperator.setText("");
                    addPhoneNumberOne(finalI);
                    checkPhoneNum();
                }
            });
        }
        View btnDelete = view.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvOperator.setText("");
                if (mPhoneNumber.length() > 0) {
                    mPhoneNumber = mPhoneNumber.substring(0, mPhoneNumber.length() - 1);
                    setPhoneNumber(mPhoneNumber);
                    checkPhoneNum();
                }
            }
        });
        btnDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mTvOperator.setText("");
                mPhoneNumber = "";
                setPhoneNumber(mPhoneNumber);
                checkPhoneNum();
                return true;
            }
        });
        mBtnDone = view.findViewById(R.id.btnDone);

        view.setVisibility(View.INVISIBLE);
        return view;
    }

    /**
     * 点击确定，完成电话号码修改
     */
    private void checkPhoneNum() {
        if (checkPhoneNum(mPhoneNumber)) {
            mBtnDone.setBackgroundResource(R.drawable.xml_bg_btn_sim_commit_enable);
            mBtnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBtnDone.setOnClickListener(null);
                    RecordWin2Manager.getInstance().operateView(
                            TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                            TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_QIWU_PHONE_RECHARGE_NUMBER_DIALOG_COMMIT,
                            0, 0,
                            TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_SOURCE_TOUCH,
                            mPhoneNumber
                    );

                    mExtView.setVisibility(View.INVISIBLE);
                    mLoadingDialog = new LoadingDialog(UIResLoader.getInstance().getModifyContext(),
                            QiWuPhoneRechargeAmountView.getInstance());
                    mLoadingDialog.setMessage("正在加载...");
                    mLoadingDialog.show();
                }
            });
        } else {
            mBtnDone.setOnClickListener(null);
            mBtnDone.setBackgroundResource(R.drawable.xml_bg_btn_sim_commit_disable);
        }
    }

    public static boolean checkPhoneNum(String str) {
        Pattern pattern = Pattern.compile("^1[3|4|5|7|8|9]\\d{9}$");
        return pattern.matcher(str).matches();
    }

    private void setPhoneNumber(String phoneNumber) {
        int len = phoneNumber.length();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<len; i++){
            sb.append(phoneNumber.charAt(i));
            if(i == 2 || i==6){
                sb.append(" ");
            }
        }
        mTvPhoneNumber.setText(sb.toString());
    }

    private void addPhoneNumberOne(int n) {
        if (mPhoneNumber.length() == 0 && n != 1) {// 不是1开头
            return;
        }
        if (mPhoneNumber.length() == 11) {
            return;
        }
        if (mPhoneNumber.length() == 1) {
            if (n != 3 && n != 4 && n != 5 && n != 7 && n != 8 && n != 9) {
                return;
            }
        }
        mPhoneNumber = mPhoneNumber + n;
        setPhoneNumber(mPhoneNumber);
    }
}
