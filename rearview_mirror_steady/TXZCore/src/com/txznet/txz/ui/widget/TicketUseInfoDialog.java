package com.txznet.txz.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txz.ui.data.UiData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.util.TXZFileConfigUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class TicketUseInfoDialog extends WinDialog {


    private List<UseInfoBean> useInfoBeanList = new LinkedList<UseInfoBean>();

    private UseInfoBean lastUseInfo;

/*    private String defSonAccount = "-1";

    private String currentSonAccount = defSonAccount;*/

    public void setLastUseInfo(UseInfoBean lastUseInfo){
        this.lastUseInfo = lastUseInfo;
    }

    public void setUseInfoBeanList(List<UseInfoBean> useInfoBeanList){
        this.useInfoBeanList = useInfoBeanList;
    }

    public static class UseInfoBean {
        public String phone;
        public String name;
        public String idNumber;
        public String sonAccount;
    }

    private String currentSelectIndexId;

    private TextView tvCommit;

    private EditText edName;

    private  EditText edId;

    private EditText edNum;

    private LinearLayout seatLy;

    private LinearLayout seatSencondLy;

    private String ticketInfoJson = "";

    private String seatType = "";
    private String seatName = "";
    private String seatPrice = "";

    public static int mSpeechTaskId = TXZTtsManager.INVALID_TTS_TASK_ID;

    private TicketType ticketType;


    public enum TicketType{
        Train,
        Flight
    }

    public void setTicketType(TicketType ticketType){
        this.ticketType = ticketType;
    }

    public void setSeatLevel(String seatType){
        this.seatType = seatType;
    }
    public void setSeatName(String seatName){
        this.seatName = seatName;
    }

    public void  setTicketInfoJson(String ticketInfoJson){
        this.ticketInfoJson = ticketInfoJson;
    }

    public void setCurrentSelectIndexId(String currentSelectIndexId){
        this.currentSelectIndexId = currentSelectIndexId;
    }

    public String getCurrentSelectIndexId(){
        return this.currentSelectIndexId;
    }

    boolean isName = false;
    boolean isIdNum = false;
    boolean isMoNum = false;

    private AfterDissmiss afterDissmiss;

    public void setAfterDissmiss(AfterDissmiss afterDissmiss) {
        this.afterDissmiss = afterDissmiss;
    }

    public void removeAfterDissmiss(){
        this.afterDissmiss = null;
    }

    public interface AfterDissmiss{
        void afterDissmiss();
    }

    class ViewHolder {
        LinearLayout mBtnSearch;
        EditText mEtDest;
        RelativeLayout mRlBack;
        TextView mKeyBoard;
        View rootView;
    }

    private static TicketUseInfoDialog instance = new TicketUseInfoDialog();

    public static TicketUseInfoDialog getInstance() {
        return instance;
    }

    private TicketUseInfoDialog(){
        super(true);
        setCanceledOnTouchOutside(false);
        getWindow().setType(TXZFileConfigUtil
                .getSingleConfig(TXZFileConfigUtil.TICKET_USE_INFO_DIALOG_TYPE,
                        Integer.class, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        setOnDismissListener(new OnDismissListener(){
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(afterDissmiss != null){
                    afterDissmiss.afterDissmiss();
                }
                useInfoBeanList = null;
                lastUseInfo = null;
                TXZTtsManager.getInstance().cancelSpeak(mSpeechTaskId);
                AppLogicBase.removeBackGroundCallback(ttsCommit);
            }
        });
        Window theWindow = getWindow();
        WindowManager.LayoutParams lp = theWindow.getAttributes();
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
        theWindow.setAttributes(lp);

        /*
         * 不使用WindowManager里的fullscreen，这个flag和输入法的SOFT_INPUT_ADJUST_PAN有冲突。会导致界面被顶上去后就不回来了。
         * 改成使用View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION等的方法。这样子就可以了。
         * 不过我也不知道原理是什么。搞不明白。
         */
        mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View createView() {
        if(ScreenUtil.getScreenWidth() > ScreenUtil.getScreenHeight()){
            return createContentView();
        }else {
            return createPotraitr();
        }
    }

    private TicketUseInfoDialog.ViewHolder mViewHolder;

    private LinkedList<View> seatViewList = new LinkedList<View>();

    private View createPotraitr(){
        mViewHolder = new TicketUseInfoDialog.ViewHolder();
        LinearLayout layout = new LinearLayout(GlobalContext.get());
        mViewHolder.rootView = layout;
        layout.setBackgroundColor(Color.BLACK);
        layout.setOrientation(LinearLayout.VERTICAL);
        /*
         * 点击空白处关闭输入法。本来是想着输入法的逻辑我们不处理的，但是有的输入法没有关闭按钮，
         * 并且我们的界面在界面出来的时候会上移导致返回按钮不见，这时候用户就只能通过back按钮来
         * 关闭输入法。所以添加空白处关闭输入法的逻辑，帮助用户关闭输入法。
         */
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                hideSoftInput();
            }
        });

        RelativeLayout rlTitle = new RelativeLayout(GlobalContext.get());
        rlTitle.setPadding((int) LayouUtil.getDimen("x30"), 0, (int) LayouUtil.getDimen("x60"), 0);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) LayouUtil.getDimen("y80"));
        rlTitle.setLayoutParams(llLayoutParams);
        layout.addView(rlTitle);

        RelativeLayout rlBack = new RelativeLayout(GlobalContext.get());
        mViewHolder.mRlBack = rlBack;
        rlBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlBack.setLayoutParams(rlLayoutParams);
        rlTitle.addView(rlBack);

        ImageView ivBack = new ImageView(GlobalContext.get());
        ivBack.setScaleType(ImageView.ScaleType.FIT_END);
        ivBack.setImageDrawable(LayouUtil.getDrawable("button_back"));
        rlLayoutParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m10"), (int) LayouUtil.getDimen("m18"));
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlLayoutParams.rightMargin = (int) LayouUtil.getDimen("x8");
        ivBack.setId(ViewUtils.generateViewId());
        ivBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(ivBack);

        TextView tvBack = new TextView(GlobalContext.get());
        tvBack.setTextColor(Color.parseColor("#FFFFFF"));
        tvBack.setHintTextColor(Color.parseColor("#40454b"));
        TextViewUtil.setTextSize(tvBack, LayouUtil.getDimen("m19"));
       /* tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });*/
        tvBack.setText("返回");
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.RIGHT_OF, ivBack.getId());
        tvBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(tvBack);

        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
        tvTitle.setHintTextColor(Color.parseColor("#40454b"));
        TextViewUtil.setTextSize(tvTitle, LayouUtil.getDimen("m22"));
        tvTitle.setText("乘客信息");
        tvTitle.setGravity(Gravity.CENTER);
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        tvTitle.setLayoutParams(rlLayoutParams);
        rlTitle.addView(tvTitle);

        tvCommit = new TextView(GlobalContext.get());

        RelativeLayout contentRy = new RelativeLayout(GlobalContext.get());
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(contentRy, rlLayoutParams);

        final LinearLayout llEdit = new LinearLayout(GlobalContext.get());
        llEdit.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        //llEdit.setPadding((int) LayouUtil.getDimen("x60"), 0, (int) LayouUtil.getDimen("x60"), 0);
        llEdit.setOrientation(LinearLayout.VERTICAL);
        rlLayoutParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x717"), RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rlLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlLayoutParams.topMargin = (int) LayouUtil.getDimen("y27");
        llEdit.setId(ViewUtils.generateViewId());
        llEdit.setLayoutParams(rlLayoutParams);
        contentRy.addView(llEdit);

        LinearLayout nameEditLy = new LinearLayout(GlobalContext.get());
        nameEditLy.setOrientation(LinearLayout.HORIZONTAL);
        nameEditLy.setPadding((int) LayouUtil.getDimen("x11"),(int) LayouUtil.getDimen("y16"),(int) LayouUtil.getDimen("x29"),(int) LayouUtil.getDimen("x11"));
        llEdit.addView(nameEditLy);
        final TextView tvName = new TextView(GlobalContext.get());
        tvName.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvName,(int) LayouUtil.getDimen("m19"));
        tvName.setText("姓       名：");
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        llLayoutParams.gravity = Gravity.CENTER;
        tvName.setLayoutParams(llLayoutParams);
        nameEditLy.addView(tvName);

        edName = new EditText(GlobalContext.get());
        final TextView errorHintTv = new TextView(GlobalContext.get());
        errorHintTv.setSingleLine();
        TextViewUtil.setTextSize(errorHintTv, (int) LayouUtil.getDimen("m17"));
        mViewHolder.mEtDest = edName;
//        edName.setBackgroundColor(Color.parseColor("#202326"));
        //edName.setBackgroundDrawable(LayouUtil.getDrawable("search_edit_bg"));
        edName.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edName.setHint("请填写身份证的姓名");
        edName.setPadding((int) LayouUtil.getDimen("x11"), 0, 0, 0);
        edName.setSingleLine();
        edName.setTextColor(Color.parseColor("#FFFFFF"));
        edName.setHintTextColor(Color.parseColor("#646464"));
        edName.setBackground(null);

        TextViewUtil.setTextSize(edName, (int) LayouUtil.getDimen("m19"));
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        edName.setLayoutParams(llLayoutParams);
        edName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(TextOutLen(s.toString(), 16)){
                    String name = s.toString().substring(0, s.length() - 1);
                    s.replace(s.length() -  1, s.length(), "");
                }
                for(int i = 0; i < s.length(); i++){
                    if(!checkName(s.charAt(i))){
                        errorHintTv.setText("*姓名只能包含中文或英文");
                        edName.setTextColor(Color.RED);
                        isName = false;
                        return;
                    }
                }
                boolean matchFlag = false;
                String currentId = "";
                String currentPhone = "";
                for(int i = 0; useInfoBeanList != null && i < useInfoBeanList.size(); i++){
                    if(s.toString().equals(useInfoBeanList.get(i).name)){
                        matchFlag = true;
                        currentId = useInfoBeanList.get(i).idNumber;
                        currentPhone = useInfoBeanList.get(i).phone;
                        break;
                    }
                }
                if(matchFlag){
                    edId.setText(currentId);
                    edNum.setText(currentPhone);
                }
                isName = s.length() > 0;
                errorHintTv.setText("");
                edName.setTextColor(Color.parseColor("#FFFFFF"));
                setTvSearch();
            }
        });
        nameEditLy.addView(edName);
        errorHintTv.setTextColor(Color.RED);
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        errorHintTv.setLayoutParams(llLayoutParams);
        nameEditLy.addView(errorHintTv);

        View dividerOne = new View(GlobalContext.get());
        dividerOne.setBackground(LayouUtil.getDrawable("line"));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                (int) Math.max(1, LayouUtil.getDimen("y1")));
        llEdit.addView(dividerOne,layoutParams);

        LinearLayout idEditLy = new LinearLayout(GlobalContext.get());
        idEditLy.setOrientation(LinearLayout.HORIZONTAL);
        idEditLy.setPadding((int) LayouUtil.getDimen("x11"),(int) LayouUtil.getDimen("y16"),(int) LayouUtil.getDimen("x29"),(int) LayouUtil.getDimen("x11"));
        llEdit.addView(idEditLy);
        final TextView tvId = new TextView(GlobalContext.get());
        tvId.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvId,(int) LayouUtil.getDimen("m19"));
        tvId.setText("身份证号：");
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        llLayoutParams.gravity = Gravity.CENTER;
        tvId.setLayoutParams(llLayoutParams);
        idEditLy.addView(tvId);

        final TextView errorIdHintTv = new TextView(GlobalContext.get());
        edId = new EditText(GlobalContext.get());
        mViewHolder.mEtDest = edId;
//        edName.setBackgroundColor(Color.parseColor("#202326"));
        //edId.setBackgroundDrawable(LayouUtil.getDrawable("search_edit_bg"));
        edId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
        edId.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edId.setHint("请填写您的身份证号码");
        edId.setBackground(null);
        edId.setPadding((int) LayouUtil.getDimen("x11"), 0, 0, 0);
        edId.setSingleLine();
        edId.setTextColor(Color.parseColor("#FFFFFF"));
        edId.setHintTextColor(Color.parseColor("#646464"));
        edId.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0){
                    isIdNum = false;
                    errorIdHintTv.setText("");
                    edId.setTextColor(Color.parseColor("#FFFFFF"));
                    return;
                }
                if(!checkIdNum(s.toString(),false)){
                    errorIdHintTv.setText("请输入正确的身份证号码");
                    edId.setTextColor(Color.RED);
                    isIdNum = false;
                } else{
                    if(s.length() >= 18){
                        isIdNum = true;
                    }else {
                        isIdNum = false;
                    }
                    errorIdHintTv.setText("");
                    edId.setTextColor(Color.parseColor("#FFFFFF"));
                }
                setTvSearch();
            }
        });
        TextViewUtil.setTextSize(edId, (int) LayouUtil.getDimen("m19"));
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        edId.setLayoutParams(llLayoutParams);
        idEditLy.addView(edId);
        errorIdHintTv.setSingleLine();
        TextViewUtil.setTextSize(errorIdHintTv, (int) LayouUtil.getDimen("m17"));
        errorIdHintTv.setTextColor(Color.RED);
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        errorIdHintTv.setLayoutParams(llLayoutParams);
        idEditLy.addView(errorIdHintTv);

        View dividerTwo = new View(GlobalContext.get());
        dividerTwo.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,(int) Math.max(1, LayouUtil.getDimen("y1")));
        llEdit.addView(dividerTwo,layoutParams);

        LinearLayout numEditLy = new LinearLayout(GlobalContext.get());
        numEditLy.setOrientation(LinearLayout.HORIZONTAL);
        numEditLy.setPadding((int) LayouUtil.getDimen("x11"),(int) LayouUtil.getDimen("y16"),(int) LayouUtil.getDimen("x29"),(int) LayouUtil.getDimen("x11"));
        llEdit.addView(numEditLy);
        final TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvNum,(int) LayouUtil.getDimen("m19"));
        tvNum.setText("手机号码：");
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        llLayoutParams.gravity = Gravity.CENTER;
        tvNum.setLayoutParams(llLayoutParams);
        numEditLy.addView(tvNum);

        final TextView errorNumHintTv = new TextView(GlobalContext.get());
        edNum = new EditText(GlobalContext.get());
        mViewHolder.mEtDest = edNum;
//        edName.setBackgroundColor(Color.parseColor("#202326"));
        //edId.setBackgroundDrawable(LayouUtil.getDrawable("search_edit_bg"));
        edNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        edNum.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edNum.setHint("用于接受订单信息");
        edNum.setBackground(null);
        edNum.setPadding((int) LayouUtil.getDimen("x11"), 0, 0, 0);
        edNum.setSingleLine();
        edNum.setTextColor(Color.parseColor("#FFFFFF"));
        edNum.setHintTextColor(Color.parseColor("#646464"));
        TextViewUtil.setTextSize(edNum, (int) LayouUtil.getDimen("m19"));
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        edNum.setLayoutParams(llLayoutParams);
        numEditLy.addView(edNum);
        edNum.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!checkNum(s.toString())){
                    errorNumHintTv.setText("请输入正确的手机号码");
                    edNum.setTextColor(Color.RED);
                    isMoNum = false;
                } else{
                    if(s.length() >= 11){
                        isMoNum = true;
                    }else {
                        isMoNum = false;
                    }
                    errorNumHintTv.setText("");
                    edNum.setTextColor(Color.parseColor("#FFFFFF"));
                }
                setTvSearch();
            }
        });
        errorNumHintTv.setSingleLine();
        TextViewUtil.setTextSize(errorNumHintTv, (int) LayouUtil.getDimen("m17"));
        errorNumHintTv.setTextColor(Color.RED);
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        errorNumHintTv.setLayoutParams(llLayoutParams);
        numEditLy.addView(errorNumHintTv);

        seatLy = new LinearLayout(GlobalContext.get());
        seatLy.setOrientation(LinearLayout.HORIZONTAL);
        seatLy.setId(ViewUtils.generateViewId());
        rlLayoutParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x717"), ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.BELOW, llEdit.getId());
        rlLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlLayoutParams.topMargin = (int) LayouUtil.getDimen("y14");

        contentRy.addView(seatLy, rlLayoutParams);

        seatSencondLy = new LinearLayout(GlobalContext.get());
        seatSencondLy.setOrientation(LinearLayout.HORIZONTAL);
        rlLayoutParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x717"), ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.BELOW, seatLy.getId());
        rlLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlLayoutParams.topMargin = (int) LayouUtil.getDimen("y9");

        contentRy.addView(seatSencondLy, rlLayoutParams);

        tvCommit.setGravity(Gravity.CENTER);
        tvCommit.setPadding(0, (int) LayouUtil.getDimen("y12"), 0, (int) LayouUtil.getDimen("y12"));
        rlLayoutParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x417"), ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rlLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        // rlLayoutParams.topMargin = (int)LayouUtil.getDimen("y16");
        rlLayoutParams.bottomMargin = (int) LayouUtil.getDimen("y7");
        contentRy.addView(tvCommit, rlLayoutParams);
        TextViewUtil.setTextSize(tvCommit, (int) LayouUtil.getDimen("m22"));
        tvCommit.setBackgroundColor(Color.BLUE);
        tvCommit.setTextColor(createColorStateList(
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#8ce3fd"),
                Color.parseColor("#8ce3fd"),
                0xfff));
        tvCommit.setText("提交");
        tvCommit.setBackgroundDrawable(LayouUtil.getDrawable("movie_list_rang_bg"));
        edName.requestFocus();
        return layout;
    }

    @SuppressLint("NewApi")
    private View createContentView() {
        mViewHolder = new TicketUseInfoDialog.ViewHolder();
        LinearLayout layout = new LinearLayout(GlobalContext.get());
        mViewHolder.rootView = layout;
        layout.setBackgroundColor(Color.BLACK);
        layout.setOrientation(LinearLayout.VERTICAL);
        /*
         * 点击空白处关闭输入法。本来是想着输入法的逻辑我们不处理的，但是有的输入法没有关闭按钮，
         * 并且我们的界面在界面出来的时候会上移导致返回按钮不见，这时候用户就只能通过back按钮来
         * 关闭输入法。所以添加空白处关闭输入法的逻辑，帮助用户关闭输入法。
         */
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                hideSoftInput();
            }
        });

        RelativeLayout rlTitle = new RelativeLayout(GlobalContext.get());
        rlTitle.setPadding((int) LayouUtil.getDimen("x30"), 0, (int) LayouUtil.getDimen("x60"), 0);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) LayouUtil.getDimen("y80"));
        rlTitle.setLayoutParams(llLayoutParams);
        layout.addView(rlTitle);

        RelativeLayout rlBack = new RelativeLayout(GlobalContext.get());
        mViewHolder.mRlBack = rlBack;
        rlBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlBack.setLayoutParams(rlLayoutParams);
        rlTitle.addView(rlBack);

        ImageView ivBack = new ImageView(GlobalContext.get());
        ivBack.setScaleType(ImageView.ScaleType.FIT_END);
        ivBack.setImageDrawable(LayouUtil.getDrawable("button_back"));
        rlLayoutParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m10"), (int) LayouUtil.getDimen("m18"));
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlLayoutParams.rightMargin = (int) LayouUtil.getDimen("x8");
        ivBack.setId(ViewUtils.generateViewId());
        ivBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(ivBack);

        TextView tvBack = new TextView(GlobalContext.get());
        tvBack.setTextColor(Color.parseColor("#FFFFFF"));
        tvBack.setHintTextColor(Color.parseColor("#40454b"));
        TextViewUtil.setTextSize(tvBack, LayouUtil.getDimen("m19"));
       /* tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });*/
        tvBack.setText("返回");
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.RIGHT_OF, ivBack.getId());
        tvBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(tvBack);

        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
        tvTitle.setHintTextColor(Color.parseColor("#40454b"));
        TextViewUtil.setTextSize(tvTitle, LayouUtil.getDimen("m22"));
        tvTitle.setText("乘客信息");
        tvTitle.setGravity(Gravity.CENTER);
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        tvTitle.setLayoutParams(rlLayoutParams);
        rlTitle.addView(tvTitle);

        tvCommit = new TextView(GlobalContext.get());

        RelativeLayout contentRy = new RelativeLayout(GlobalContext.get());
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(contentRy, rlLayoutParams);

        final LinearLayout llEdit = new LinearLayout(GlobalContext.get());
        llEdit.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        //llEdit.setPadding((int) LayouUtil.getDimen("x60"), 0, (int) LayouUtil.getDimen("x60"), 0);
        llEdit.setOrientation(LinearLayout.VERTICAL);
        rlLayoutParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x644"), RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rlLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlLayoutParams.topMargin = (int) LayouUtil.getDimen("y16");
        llEdit.setId(ViewUtils.generateViewId());
        llEdit.setLayoutParams(rlLayoutParams);
        contentRy.addView(llEdit);

        LinearLayout nameEditLy = new LinearLayout(GlobalContext.get());
        nameEditLy.setOrientation(LinearLayout.HORIZONTAL);
        nameEditLy.setPadding((int) LayouUtil.getDimen("x16"),(int) LayouUtil.getDimen("y20"),(int) LayouUtil.getDimen("x29"),(int) LayouUtil.getDimen("y19"));
        llEdit.addView(nameEditLy);
        final TextView tvName = new TextView(GlobalContext.get());
        tvName.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvName,(int) LayouUtil.getDimen("m19"));
        tvName.setText("姓       名：");
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        llLayoutParams.gravity = Gravity.CENTER;
        tvName.setLayoutParams(llLayoutParams);
        nameEditLy.addView(tvName);

        edName = new EditText(GlobalContext.get());
        final TextView errorHintTv = new TextView(GlobalContext.get());
        errorHintTv.setSingleLine();
        mViewHolder.mEtDest = edName;
//        edName.setBackgroundColor(Color.parseColor("#202326"));
        //edName.setBackgroundDrawable(LayouUtil.getDrawable("search_edit_bg"));
        edName.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edName.setHint("请填写身份证的姓名");
        edName.setPadding((int) LayouUtil.getDimen("x24"), 0, 0, 0);
        edName.setSingleLine();
        edName.setTextColor(Color.parseColor("#FFFFFF"));
        edName.setHintTextColor(Color.parseColor("#646464"));
        edName.setBackground(null);
        TextViewUtil.setTextSize(edName, (int) LayouUtil.getDimen("m19"));
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        edName.setLayoutParams(llLayoutParams);
        edName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(TextOutLen(s.toString(), 16)){
                    String name = s.toString().substring(0, s.length() - 1);
                    s.replace(s.length() -  1, s.length(), "");
                }
                for(int i = 0; i < s.length(); i++){
                    if(!checkName(s.charAt(i))){
                        errorHintTv.setText("*姓名只能包含中文或英文");
                        edName.setTextColor(Color.RED);
                        isName = false;
                        return;
                    }
                }
                boolean matchFlag = false;
                String currentId = "";
                String currentPhone = "";
                for(int i = 0; useInfoBeanList != null && i < useInfoBeanList.size(); i++){
                    if(s.toString().equals(useInfoBeanList.get(i).name)){
                        matchFlag = true;
                        currentId = useInfoBeanList.get(i).idNumber;
                        currentPhone = useInfoBeanList.get(i).phone;
                        break;
                    }
                }
                if(matchFlag){
                    edId.setText(currentId);
                    edNum.setText(currentPhone);
                }
                isName = s.length() > 0;
                errorHintTv.setText("");
                edName.setTextColor(Color.parseColor("#FFFFFF"));
                setTvSearch();
            }
        });
        nameEditLy.addView(edName);
        TextViewUtil.setTextSize(errorHintTv, (int) LayouUtil.getDimen("m19"));
        errorHintTv.setTextColor(Color.RED);
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        errorHintTv.setLayoutParams(llLayoutParams);
        nameEditLy.addView(errorHintTv);

        View dividerOne = new View(GlobalContext.get());
        dividerOne.setBackground(LayouUtil.getDrawable("line"));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                (int) Math.max(1, LayouUtil.getDimen("y1")));
        llEdit.addView(dividerOne,layoutParams);

        LinearLayout idEditLy = new LinearLayout(GlobalContext.get());
        idEditLy.setOrientation(LinearLayout.HORIZONTAL);
        idEditLy.setPadding((int) LayouUtil.getDimen("x16"),(int) LayouUtil.getDimen("y20"),(int) LayouUtil.getDimen("x29"),(int) LayouUtil.getDimen("y19"));
        llEdit.addView(idEditLy);
        final TextView tvId = new TextView(GlobalContext.get());
        tvId.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvId,(int) LayouUtil.getDimen("m19"));
        tvId.setText("身份证号：");
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        llLayoutParams.gravity = Gravity.CENTER;
        tvId.setLayoutParams(llLayoutParams);
        idEditLy.addView(tvId);

        final TextView errorIdHintTv = new TextView(GlobalContext.get());
        edId = new EditText(GlobalContext.get());
        mViewHolder.mEtDest = edId;
//        edName.setBackgroundColor(Color.parseColor("#202326"));
        //edId.setBackgroundDrawable(LayouUtil.getDrawable("search_edit_bg"));
        edId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
        edId.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edId.setHint("请填写您的身份证号码");
        edId.setBackground(null);
        edId.setPadding((int) LayouUtil.getDimen("x24"), 0, 0, 0);
        edId.setSingleLine();
        edId.setTextColor(Color.parseColor("#FFFFFF"));
        edId.setHintTextColor(Color.parseColor("#646464"));
        edId.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0){
                    isIdNum = false;
                    errorIdHintTv.setText("");
                    edId.setTextColor(Color.parseColor("#FFFFFF"));
                    return;
                }
                if(!checkIdNum(s.toString(),false)){
                    errorIdHintTv.setText("请输入正确的身份证号码");
                    edId.setTextColor(Color.RED);
                    isIdNum = false;
                } else{
                    if(s.length() >= 18){
                        isIdNum = true;
                    }else {
                        isIdNum = false;
                    }
                    errorIdHintTv.setText("");
                    edId.setTextColor(Color.parseColor("#FFFFFF"));
                }
                setTvSearch();
            }
        });
        TextViewUtil.setTextSize(edId, (int) LayouUtil.getDimen("m19"));
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        edId.setLayoutParams(llLayoutParams);
        idEditLy.addView(edId);
        errorIdHintTv.setSingleLine();
        TextViewUtil.setTextSize(errorIdHintTv, (int) LayouUtil.getDimen("m19"));
        errorIdHintTv.setTextColor(Color.RED);
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        errorIdHintTv.setLayoutParams(llLayoutParams);
        idEditLy.addView(errorIdHintTv);

        View dividerTwo = new View(GlobalContext.get());
        dividerTwo.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,(int) Math.max(1, LayouUtil.getDimen("y1")));
        llEdit.addView(dividerTwo,layoutParams);

        LinearLayout numEditLy = new LinearLayout(GlobalContext.get());
        numEditLy.setOrientation(LinearLayout.HORIZONTAL);
        numEditLy.setPadding((int) LayouUtil.getDimen("x16"),(int) LayouUtil.getDimen("y20"),(int) LayouUtil.getDimen("x29"),(int) LayouUtil.getDimen("y19"));
        llEdit.addView(numEditLy);
        final TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvNum,(int) LayouUtil.getDimen("m19"));
        tvNum.setText("手机号码：");
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        llLayoutParams.gravity = Gravity.CENTER;
        tvNum.setLayoutParams(llLayoutParams);
        numEditLy.addView(tvNum);

        final TextView errorNumHintTv = new TextView(GlobalContext.get());
        edNum = new EditText(GlobalContext.get());
        mViewHolder.mEtDest = edNum;
//        edName.setBackgroundColor(Color.parseColor("#202326"));
        //edId.setBackgroundDrawable(LayouUtil.getDrawable("search_edit_bg"));
        edNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        edNum.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edNum.setHint("用于接受订单信息");
        edNum.setBackground(null);
        edNum.setPadding((int) LayouUtil.getDimen("x24"), 0, 0, 0);
        edNum.setSingleLine();
        edNum.setTextColor(Color.parseColor("#FFFFFF"));
        edNum.setHintTextColor(Color.parseColor("#646464"));
        TextViewUtil.setTextSize(edNum, (int) LayouUtil.getDimen("m19"));
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        edNum.setLayoutParams(llLayoutParams);
        numEditLy.addView(edNum);
        edNum.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!checkNum(s.toString())){
                    errorNumHintTv.setText("请输入正确的手机号码");
                    edNum.setTextColor(Color.RED);
                    isMoNum = false;
                } else{
                    if(s.length() >= 11){
                        isMoNum = true;
                    }else {
                        isMoNum = false;
                    }
                    errorNumHintTv.setText("");
                    edNum.setTextColor(Color.parseColor("#FFFFFF"));
                }
                setTvSearch();
            }
        });
        errorNumHintTv.setSingleLine();
        TextViewUtil.setTextSize(errorNumHintTv, (int) LayouUtil.getDimen("m19"));
        errorNumHintTv.setTextColor(Color.RED);
        llLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        errorNumHintTv.setLayoutParams(llLayoutParams);
        numEditLy.addView(errorNumHintTv);

        seatLy = new LinearLayout(GlobalContext.get());
        seatLy.setOrientation(LinearLayout.HORIZONTAL);
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.BELOW, llEdit.getId());
        rlLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlLayoutParams.topMargin = (int) LayouUtil.getDimen("y16");
        rlLayoutParams.bottomMargin = (int) LayouUtil.getDimen("y16");

        contentRy.addView(seatLy, rlLayoutParams);

        tvCommit.setGravity(Gravity.CENTER);
        tvCommit.setPadding(0, (int) LayouUtil.getDimen("y12"), 0, (int) LayouUtil.getDimen("y12"));
        rlLayoutParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x313"), ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rlLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
       // rlLayoutParams.topMargin = (int)LayouUtil.getDimen("y16");
        rlLayoutParams.bottomMargin = (int) LayouUtil.getDimen("y20");
        contentRy.addView(tvCommit, rlLayoutParams);
        TextViewUtil.setTextSize(tvCommit, (int) LayouUtil.getDimen("m22"));
        tvCommit.setBackgroundColor(Color.BLUE);
        tvCommit.setTextColor(createColorStateList(
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#8ce3fd"),
                Color.parseColor("#8ce3fd"),
                0xfff));
        tvCommit.setText("提交");
        tvCommit.setBackgroundDrawable(LayouUtil.getDrawable("movie_list_rang_bg"));
        edName.requestFocus();
        return layout;

    }

    /**
     * 对TextView设置不同状态时其文字颜色。
     */
    private ColorStateList createColorStateList(int normal, int pressed, int focused, int unable) {
        int[] colors = new int[]{pressed, focused, normal, focused, unable, normal};
        int[][] states = new int[6][];
        states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};
        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused};
        states[2] = new int[]{android.R.attr.state_enabled};
        states[3] = new int[]{android.R.attr.state_focused};
        states[4] = new int[]{android.R.attr.state_window_focused};
        states[5] = new int[]{};
        ColorStateList colorList = new ColorStateList(states, colors);
        return colorList;
    }

    @Override
    public void show() {
        super.show();
        useInfoWake();
        refreSeatView();
        edId.setText("");
        edName.setText("");
        edNum.setText("");
        edName.requestFocus();
       mSpeechTaskId = TXZTtsManager.getInstance().speakText("好的，正在预订，请填写乘客信息。");
       AppLogicBase.runOnBackGround(ttsCommit,180000);
        GlobalObservableSupport.getHomeObservable().registerObserver(mHomeReceiver);
    }


    public void useInfoWake(){
        TXZAsrManager.AsrComplexSelectCallback wakeUpCallback = new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public String getTaskId() {
                return getWakeUpTaskId();
            }

            @Override
            public boolean needAsrState() {
                return false;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                onCommand(type, command);
                super.onCommandSelected(type, command);
            }
        };

        addCommand(wakeUpCallback, "CANCEL_DIGLOG", "取消","返回");
        WakeupManager.getInstance().useWakeupAsAsr(wakeUpCallback);
    }

    @SuppressLint("NewApi")
    private void refreSeatView(){
        if(ScreenUtil.getScreenWidth() > ScreenUtil.getScreenHeight()){
            landscapeSeatView();
        }else {
            potraitrSeatView();
        }
    }

    private void potraitrSeatView(){
        seatLy.removeAllViews();
        seatViewList.clear();
        seatSencondLy.removeAllViews();
        if(ticketType == TicketType.Train){
            try {
                JSONObject seatJson = new JSONObject(ticketInfoJson);
                JSONArray allSeatJSONArray = seatJson.getJSONArray("allSeatJSONArray");
                for(int i = 0, j = 0; i < allSeatJSONArray.length(); i++){
                    if(allSeatJSONArray.getJSONObject(i).getInt("number") <= 0){
                        continue;
                    }

                    RelativeLayout seatRL = new RelativeLayout(GlobalContext.get());
                    seatViewList.add(seatRL);
                    String strSeatType = allSeatJSONArray.getJSONObject(i).getString("seatClass");
                    String strName =  allSeatJSONArray.getJSONObject(i).getString("name");
                    String strPrice =  allSeatJSONArray.getJSONObject(i).getString("price");
                    if(seatViewList.size() == 1){
                        this.seatType = strSeatType;
                        this.seatName = strName;
                        this.seatPrice = strPrice;
                        seatRL.setBackground(LayouUtil.getDrawable("view_selected_blue"));
                    }else {
                        seatRL.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
                    }
                    JSONObject tag = new JSONObject();
                    tag.put("seatType", strSeatType);
                    tag.put("strName", strName);
                    tag.put("strPrice", strPrice);
                    LinearLayout.LayoutParams lyParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("x219"), (int) LayouUtil.getDimen("y47"));
                    if(j < 3){
                        if(j != 2){
                            lyParams.rightMargin = (int) LayouUtil.getDimen("x31");
                        }
                        seatLy.addView(seatRL, lyParams);
                        seatRL.setTag(tag);
                        seatRL.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for(int i = 0; i < seatViewList.size(); i++){
                                    seatViewList.get(i).setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
                                }
                                v.setBackground(LayouUtil.getDrawable("view_selected_blue"));
                                JSONObject currentTag = (JSONObject) v.getTag();
                                try {
                                    seatType = currentTag.getString("seatType");
                                    seatName = currentTag.getString("strName");
                                    seatPrice = currentTag.getString("strPrice");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }else {
                        if(j != 5){
                            lyParams.rightMargin = (int) LayouUtil.getDimen("x31");
                        }
                        seatSencondLy.addView(seatRL, lyParams);
                        seatRL.setTag(tag);
                        seatRL.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for(int i = 0; i < seatViewList.size(); i++){
                                    seatViewList.get(i).setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
                                }
                                v.setBackground(LayouUtil.getDrawable("view_selected_blue"));
                                JSONObject currentTag = (JSONObject) v.getTag();
                                try {
                                    seatType = currentTag.getString("seatType");
                                    seatName = currentTag.getString("strName");
                                    seatPrice = currentTag.getString("strPrice");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    TextView seatName = new TextView(GlobalContext.get());
                    seatName.setId(ViewUtils.generateViewId());
                    seatName.setText(allSeatJSONArray.getJSONObject(i).getString("name"));
                    TextViewUtil.setTextSize(seatName, (int) LayouUtil.getDimen("m19"));
                    RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rlParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rlParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    rlParams.leftMargin = 2;
                    seatRL.addView(seatName, rlParams);

                    TextView seatCount = new TextView(GlobalContext.get());
                    seatCount.setText(String.format("%s张", allSeatJSONArray.getJSONObject(i).getString("number")));
                    TextViewUtil.setTextSize(seatCount, (int) LayouUtil.getDimen("m16"));
                    TextViewUtil.setTextColor(seatCount, Color.parseColor("#646464"));
                    rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    rlParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    rlParams.addRule(RelativeLayout.ALIGN_BASELINE, seatName.getId());
                    rlParams.rightMargin = 2;
                    seatRL.addView(seatCount, rlParams);

                    TextView seatPrice = new TextView(GlobalContext.get());
                    seatPrice.setText(String.format("￥%s", allSeatJSONArray.getJSONObject(i).getString("price")));
                    TextViewUtil.setTextSize(seatPrice, (int) LayouUtil.getDimen("m22"));
                    rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rlParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    rlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    TextViewUtil.setTextColor(seatPrice, Color.parseColor("#F98006"));
                    seatRL.addView(seatPrice, rlParams);
                    j++;
                }
            } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
    }

    private void landscapeSeatView(){
        seatLy.removeAllViews();
        seatViewList.clear();
        if(ticketType == TicketType.Train){
            try {
                JSONObject seatJson = new JSONObject(ticketInfoJson);
                JSONArray allSeatJSONArray = seatJson.getJSONArray("allSeatJSONArray");
                for(int i = 0; i < allSeatJSONArray.length(); i++){
                    if(allSeatJSONArray.getJSONObject(i).getInt("number") <= 0){
                        continue;
                    }

                    RelativeLayout seatRL = new RelativeLayout(GlobalContext.get());
                    seatViewList.add(seatRL);
                    String strSeatType = allSeatJSONArray.getJSONObject(i).getString("seatClass");
                    String strName =  allSeatJSONArray.getJSONObject(i).getString("name");
                    String strPrice =  allSeatJSONArray.getJSONObject(i).getString("price");
                    if(seatViewList.size() == 1){
                        this.seatType = strSeatType;
                        this.seatName = strName;
                        this.seatPrice = strPrice;
                        seatRL.setBackground(LayouUtil.getDrawable("view_selected_blue"));
                    }else {
                        seatRL.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
                    }
                    JSONObject tag = new JSONObject();
                    tag.put("seatType", strSeatType);
                    tag.put("strName", strName);
                    tag.put("strPrice", strPrice);
                    LinearLayout.LayoutParams lyParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("x120"), (int) LayouUtil.getDimen("y80"));
                    lyParams.leftMargin = (int) LayouUtil.getDimen("x8");
                    lyParams.rightMargin = (int) LayouUtil.getDimen("x8");
                    seatLy.addView(seatRL, lyParams);
                    seatRL.setTag(tag);
                    seatRL.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            for(int i = 0; i < seatViewList.size(); i++){
                                seatViewList.get(i).setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
                            }
                            v.setBackground(LayouUtil.getDrawable("view_selected_blue"));
                            JSONObject currentTag = (JSONObject) v.getTag();
                            try {
                                seatType = currentTag.getString("seatType");
                                seatName = currentTag.getString("strName");
                                seatPrice = currentTag.getString("strPrice");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    TextView seatName = new TextView(GlobalContext.get());
                    seatName.setId(ViewUtils.generateViewId());
                    seatName.setText(allSeatJSONArray.getJSONObject(i).getString("name"));
                    TextViewUtil.setTextSize(seatName, (int) LayouUtil.getDimen("m24"));
                    RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rlParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rlParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    rlParams.leftMargin = 2;
                    seatRL.addView(seatName, rlParams);

                    TextView seatCount = new TextView(GlobalContext.get());
                    seatCount.setText(String.format("%s张", allSeatJSONArray.getJSONObject(i).getString("number")));
                    TextViewUtil.setTextSize(seatCount, (int) LayouUtil.getDimen("m20"));
                    TextViewUtil.setTextColor(seatCount, Color.parseColor("#646464"));
                    rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    rlParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    rlParams.addRule(RelativeLayout.ALIGN_BASELINE, seatName.getId());
                    rlParams.rightMargin = 2;
                    seatRL.addView(seatCount, rlParams);

                    TextView seatPrice = new TextView(GlobalContext.get());
                    seatPrice.setText(String.format("￥%s", allSeatJSONArray.getJSONObject(i).getString("price")));
                    TextViewUtil.setTextSize(seatPrice, (int) LayouUtil.getDimen("m28"));
                    rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rlParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    rlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    TextViewUtil.setTextColor(seatPrice, Color.parseColor("#F98006"));
                    seatRL.addView(seatPrice, rlParams);
                }
            } catch (JSONException e) {
                return;
            }
        }
    }

    public  void onCommand(String type, String command){
        if("CANCEL_DIGLOG".equals(type)){
            if(TicketWaitingDialog.getInstance().isShowing()){
                return;
            }
            dismiss();
            WakeupManager.getInstance().recoverWakeupFromAsr(getWakeUpTaskId());
        }
    }

    public static TXZAsrManager.AsrComplexSelectCallback addCommand(TXZAsrManager.AsrComplexSelectCallback asc, String type, String... cmds) {
        asc.addCommand(type, cmds);
        return asc;
    }

    private String getWakeUpTaskId() {
        return "TicketUseInfoDialog";
    }

    /**
      * 判定输入的是否是汉字
      *
      * @param c
      *  被校验的字符
      * @return true代表是汉字
      */
    public static boolean checkName(char c) {
       Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if(ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                ||ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS){
            String illegal = "￥`~！#%^&*=+\\|{}。；：'\"，<>/？○●★☆☉♀♂※——+++¤╬の〆（）…【】‘’、“”";
            return !illegal.contains((String.valueOf(c)));
        }else{
            return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
        }
    }

    public static boolean checkNum(String str){
        Pattern pattern;
        if(str.length() >= 11){
            pattern = Pattern.compile("^1[3|4|5|7|8|9]\\d{9}$");
        }else {
            pattern = Pattern.compile("[0-9]*");
        }
        return pattern.matcher(str).matches();
    }

    public static boolean checkIdNum(String str, boolean flag){
        Pattern pattern = Pattern.compile("[0-9]*");
        if(pattern.matcher(str).matches()){
            return true;
        }
        if(flag){
            return false;
        }
        return checkIdNum(str.substring(0, str.length() - 1), true) && (str.charAt(str.length() - 1) == 'X');
    }

    View.OnClickListener commitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TXZTtsManager.getInstance().cancelSpeak(mSpeechTaskId);
            if(NetworkManager.getInstance().getNetType() == UiData.NETWORK_STATUS_NONE || NetworkManager.getInstance().getNetType() == UiData.NETWORK_STATUS_FLY){
                mSpeechTaskId = TtsManager.getInstance().speakText("网络异常，请重试");
                return;
            }
            AppLogicBase.removeBackGroundCallback(ttsCommit);
                String id = edId.getText().toString();
                String name = edName.getText().toString();
                String moNum = edNum.getText().toString();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", id);
                    jsonObject.put("name", name);
                    jsonObject.put("moNum", moNum);
                    jsonObject.put("seatCode", seatType);
                    jsonObject.put("seatName", seatName);
                    jsonObject.put("seatPrice", seatPrice);
                    jsonObject.put("ticketInfoJson",new JSONObject(ticketInfoJson));
                    jsonObject.put("ticketType",ticketType.toString());
                    if(useInfoBeanList !=null && useInfoBeanList.size() > 0){
                        for(int i = 0; i < useInfoBeanList.size(); i++){
                            if(id.equals(useInfoBeanList.get(i).idNumber) && name.equals(useInfoBeanList.get(i).name) && moNum.equals(useInfoBeanList.get(i).phone)){
                                jsonObject.put("sonAccount",useInfoBeanList.get(i).sonAccount);
                                break;
                            }
                        }
                    }
                    RecordWin2Manager.getInstance().operateView(
                            TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                            TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_TICKET_INFO_COMMIT, 0, 0,
                            1,jsonObject.toString());
                } catch (JSONException e) {
                    LogUtil.logd("userInfo commit exception");
                }
            tvCommit.setOnClickListener(null);
            }

    };

    Runnable ttsCommit = new Runnable(){

        @Override
        public void run() {
            String ttsType = "";
            if(ticketType != null){
                switch (ticketType){
                    case Train:ttsType = "火车票";break;
                    case Flight:ttsType = "机票";break;
                }
                mSpeechTaskId = TXZTtsManager.getInstance().speakText("正在为您预订"+ttsType+"，请及时填写乘客信息.");
            }
        }
    };

    @SuppressLint("NewApi")
    public void setTvSearch(){
        if(isIdNum && isName && isMoNum){
            tvCommit.setBackground(LayouUtil.getDrawable("btn_blue_bg"));
            tvCommit.setTextColor(createColorStateList(
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#8ce3fd"),
                    Color.parseColor("#8ce3fd"),
                    0xfff));
            tvCommit.setOnClickListener(commitListener);
        }else{
            tvCommit.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
            tvCommit.setTextColor(Color.parseColor("#646464"));
            tvCommit.setOnClickListener(null);
        }
    }

    @Override
    public void dismiss(){
        if(!isShowing()){
            return;
        }
        try {
            GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeReceiver);
            WakeupManager.getInstance().recoverWakeupFromAsr(getWakeUpTaskId());
        }catch (Exception ignored){}
        // 要在super的dismiss之前执行，不然会导致没法通过view获取到windowToken，也就没法关闭键盘了。
        hideSoftInput();

        super.dismiss();
    }

    private HomeObservable.HomeObserver mHomeReceiver = new HomeObservable.HomeObserver() {
        @Override
        public void onHomePressed() {
            // 短按Home键
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 0);
        }
    };

    private void hideSoftInput() {
        if (mView != null) {
            InputMethodManager imm = (InputMethodManager) GlobalContext.get()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
        }
    }

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


}
