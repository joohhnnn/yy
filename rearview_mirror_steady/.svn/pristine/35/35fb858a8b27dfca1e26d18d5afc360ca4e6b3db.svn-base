package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ISearchEditView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * Created by ASUS User on 2018/7/19.
 */

public class SearchEditView extends ISearchEditView {

    class ViewHolder {
        LinearLayout mBtnSearch;
        EditText mEtDest;
        RelativeLayout mRlBack;
        TextView mKeyBoard;
        View rootView;
    }

    private ViewHolder mViewHolder = null;

    private int bgColor;    //整个背景颜色
    private int titleHeight;    //标题内容高度
    private int titleMarginHorizontal;    //标题内容左右边距
    private int iconBackSide;    //返回图标大小
    private int tvBackSize;    //返回字体大小
    private int tvBackColor;    //返回字体颜色
    private int tvTitleSize;    //标题字体大小
    private int tvTitleColor;    //标题字体颜色
    private int editHeight;    //编辑内容高度
    private int editHorizontalMargin;    //编辑内容左右边距
    private int editTopMargin;    //编辑内容左右边距
    private int editViewSize;    //编辑框输入文字大小
    private int editViewColor;    //编辑框输入文字颜色
    private int tvSearchWidth;    //搜索按钮宽度
    private int tvSearchSize;    //搜索按钮字体大小

    private static SearchEditView instance = new SearchEditView();

    public static SearchEditView getInstance() {
        return instance;
    }

    private SearchEditView() {

    }

    public void onStart(String mKey) {
        if (mViewHolder != null) {
            if (!TextUtils.isEmpty(mKey)) {
                mViewHolder.mEtDest.setText(mKey);
                mViewHolder.mEtDest.setSelection(mViewHolder.mEtDest.getText().toString().length());
            }
            mViewHolder.mEtDest.setFocusable(true);
            mViewHolder.mEtDest.setFocusableInTouchMode(true);
            mViewHolder.mEtDest.requestFocus();
            showSoftInput();
        }
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        ViewFactory.ViewAdapter adapter = new ViewFactory.ViewAdapter();
        LogUtil.logd(WinLayout.logTag+ "getView: SearchEditView");

        adapter.view = createContentView();
        adapter.type = ViewData.TYPE_SEARCH_EDIT_VIEW;
        return adapter;
    }

    private View createContentView() {
        mViewHolder = new ViewHolder();
        LinearLayout layout = new LinearLayout(GlobalContext.get());
        mViewHolder.rootView = layout;
        //layout.setBackground(LayouUtil.getDrawable("widget_color"));
        //layout.setBackgroundColor(bgColor);
        layout.setBackground(LayouUtil.getDrawable("bg"));
        layout.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout rlTitle = new RelativeLayout(GlobalContext.get());
        //rlTitle.setPadding((int) LayouUtil.getDimen("x30"), 0, (int) LayouUtil.getDimen("x60"), 0);
        rlTitle.setPadding(titleMarginHorizontal, 0, titleMarginHorizontal, 0);
        //LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, titleHeight);
        rlTitle.setLayoutParams(llLayoutParams);
        layout.addView(rlTitle);

        RelativeLayout rlBack = new RelativeLayout(GlobalContext.get());
        mViewHolder.mRlBack = rlBack;
        rlBack.setClickable(true);
        RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlBack.setLayoutParams(rlLayoutParams);
        rlTitle.addView(rlBack);

        ImageView ivBack = new ImageView(GlobalContext.get());
        //ivBack.setScaleType(ImageView.ScaleType.FIT_END);
        ivBack.setImageDrawable(LayouUtil.getDrawable("back"));
        rlLayoutParams = new RelativeLayout.LayoutParams(iconBackSide, iconBackSide);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        //rlLayoutParams.rightMargin = (int) LayouUtil.getDimen("x20");
        ivBack.setId(ViewUtils.generateViewId());
        ivBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(ivBack);

        TextView tvBack = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvBack,tvBackSize);
        TextViewUtil.setTextColor(tvBack,tvBackColor);
        //tvBack.setTextColor(Color.parseColor("#FFFFFF"));
        //tvBack.setHintTextColor(Color.parseColor("#40454b"));
        //tvBack.setTextSize(31);
        tvBack.setText("返回");
        tvBack.setGravity(Gravity.CENTER);
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.RIGHT_OF, ivBack.getId());
        tvBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(tvBack);

        TextView tvTitle = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvTitle,tvTitleSize);
        TextViewUtil.setTextColor(tvTitle,tvTitleColor);
        /*tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
        tvTitle.setHintTextColor(Color.parseColor("#40454b"));
        tvTitle.setTextSize(37);*/
        tvTitle.setText("修改关键字");
        tvTitle.setGravity(Gravity.CENTER);
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        tvTitle.setLayoutParams(rlLayoutParams);
        rlTitle.addView(tvTitle);

        final LinearLayout llEdit = new LinearLayout(GlobalContext.get());
        //llEdit.setPadding((int) LayouUtil.getDimen("x60"), 0, (int) LayouUtil.getDimen("x60"), 0);
        llEdit.setPadding(editHorizontalMargin, 0, editHorizontalMargin, 0);
        llEdit.setOrientation(LinearLayout.HORIZONTAL);
        //llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, editHeight);
        llLayoutParams.topMargin = editTopMargin;
        llEdit.setLayoutParams(llLayoutParams);
        layout.addView(llEdit);

        final EditText edDest = new EditText(GlobalContext.get());
        mViewHolder.mEtDest = edDest;
//        edDest.setBackgroundColor(Color.parseColor("#202326"));
        //edDest.setBackgroundDrawable(LayouUtil.getDrawable("search_edit_bg"));
        edDest.setBackground(LayouUtil.getDrawable("search_edit_bg"));
        edDest.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        //设置不显示全屏的输入窗口
        edDest.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edDest.setHint("请输入您的目的地");
        //edDest.setPadding((int) LayouUtil.getDimen("x24"), 0, 0, 0);
        edDest.setSingleLine();
        //edDest.setTextColor(Color.parseColor("#FFFFFF"));
        edDest.setHintTextColor(Color.parseColor("#40454b"));
        edDest.setCursorVisible(false);
        //edDest.setTextSize(38);
        TextViewUtil.setTextSize(edDest,editViewSize);
        TextViewUtil.setTextColor(edDest,editViewColor);
        //llLayoutParams = new LinearLayout.LayoutParams(0, (int) LayouUtil.getDimen("y90"), 1);
        llLayoutParams = new LinearLayout.LayoutParams(0, editHeight, 1);
        llLayoutParams.setMargins(0, 0, 0, 0);
        edDest.setLayoutParams(llLayoutParams);
        llEdit.addView(edDest);

        final LinearLayout llSearch = new LinearLayout(GlobalContext.get());
        mViewHolder.mBtnSearch = llSearch;
        //llSearch.setBackground(LayouUtil.getDrawable("activity_home_search_bg"));
        llSearch.setGravity(Gravity.CENTER);
        llSearch.setOrientation(LinearLayout.VERTICAL);
        llSearch.setClickable(true);
        llSearch.setFocusable(true);
        llSearch.setBackground(LayouUtil.getDrawable("search_btn_bg"));
        //llLayoutParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("x150"), (int) LayouUtil.getDimen("y90"));
        llLayoutParams = new LinearLayout.LayoutParams(tvSearchWidth, editHeight);
        llLayoutParams.gravity = Gravity.CENTER;
        llSearch.setLayoutParams(llLayoutParams);
        llEdit.addView(llSearch);

//        ImageView ivSearch = new ImageView(GlobalContext.get());
//        ivSearch.setPadding(0,0,0,0);
//        ivSearch.setScaleType(ImageView.ScaleType.CENTER);
//        ivSearch.setImageDrawable(LayouUtil.getDrawable("activity_home_search_draw"));
//        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//        ivSearch.setLayoutParams(llLayoutParams);
//        llSearch.addView(ivSearch);

        final TextView tvSearch = new TextView(GlobalContext.get());
        tvSearch.setGravity(Gravity.CENTER);
        tvSearch.setPadding(0, 0, 0, 0);
        //tvSearch.setTextSize(36);
        //tvSearch.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) LayouUtil.getDimen("y60"));
        TextViewUtil.setTextSize(tvSearch,tvSearchSize);
        tvSearch.setIncludeFontPadding(false);
        tvSearch.setBackgroundColor(Color.TRANSPARENT);
        tvSearch.setTextColor(createColorStateList(
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#8ce3fd"),
                Color.parseColor("#8ce3fd"),
                0xfff));
        tvSearch.setText("搜索");
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.CENTER;
        tvSearch.setLayoutParams(llLayoutParams);
        llSearch.addView(tvSearch);


        rlBack.setOnClickListener(onClickListener);
        llSearch.setOnClickListener(onClickListener);

        edDest.setOnFocusChangeListener(onFocusChangeListener);

        // 增加点击回车进行搜索
        edDest.setOnEditorActionListener(onEditorActionListener);

        edDest.addTextChangedListener(textWatcher);


        edDest.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                edDest.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Drawable edDrawable = LayouUtil.getDrawable("search_edit_bg");
                Drawable btDrawable = LayouUtil.getDrawable("search_btn_bg");

                if (edDrawable != null) {
                    edDest.setBackgroundDrawable(edDrawable);
                }
                if (btDrawable != null) {
                    llSearch.setBackgroundDrawable(btDrawable);
                    tvSearch.setText("搜索");
                    tvSearch.setTextColor(createColorStateList(
                            Color.parseColor("#FFFFFF"),
                            Color.parseColor("#8ce3fd"),
                            Color.parseColor("#8ce3fd"),
                            0xfff));
                }
            }
        });

        if (TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_KEYBOARD_FULL_SCREEN, false)) {
            int imeOptions = edDest.getImeOptions();
            imeOptions &= ~EditorInfo.IME_FLAG_NO_EXTRACT_UI;
            imeOptions &= ~EditorInfo.IME_FLAG_NO_FULLSCREEN;
            edDest.setImeOptions(imeOptions);
        }


        return layout;

    }

    @Override
    public void init() {
        bgColor = Color.parseColor(LayouUtil.getString("color_search_bg"));
        tvBackColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvTitleColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        editViewColor =  Color.parseColor(LayouUtil.getString("color_main_title"));

        int unit = ViewParamsUtil.unit;
        titleHeight = 10 * unit;
        titleMarginHorizontal = 3 * unit;
        iconBackSide = 3 * unit;
        tvBackSize = ViewParamsUtil.h5;
        tvTitleSize = ViewParamsUtil.h1;
        editHeight = 8 * unit;
        editHorizontalMargin = 5 * unit;
        editTopMargin = 4 * unit;
        editViewSize = ViewParamsUtil.h3;
        tvSearchWidth = 14 * unit;
        tvSearchSize = ViewParamsUtil.h3;

    }

    @Override
    public void onShow() {

    }

    @Override
    public void onDismiss() {
        hideSoftInput();
    }


    private void hideSoftInput() {
        if (mViewHolder != null && mViewHolder.mKeyBoard != null) {
            InputMethodManager imm = (InputMethodManager) GlobalContext.get()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mViewHolder.mKeyBoard.getWindowToken(), 0);
        }
    }

    private void showSoftInput() {
        InputMethodManager imm = (InputMethodManager) GlobalContext.get()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mViewHolder != null) {
            imm.showSoftInput(mViewHolder.mEtDest, InputMethodManager.SHOW_FORCED);
        }
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


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mViewHolder != null) {
                // 隐藏软键盘
                hideSoftInput();
                if (v == mViewHolder.mRlBack) {
                    RecordWin2Manager.getInstance().doEditSearchClickCancel();
                } else if (v == mViewHolder.mBtnSearch) {
                    RecordWin2Manager.getInstance().doEditSearchResult(mViewHolder.mEtDest.getText().toString());
                    WinLayout.isSearch = WinLayout.targetView != TXZRecordWinManager.RecordWin2.RecordWinController.TARGET_CONTENT_CHAT;
                }
            }
        }
    };

    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            mViewHolder.mKeyBoard = (TextView) v;
        }
    };


    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // 点击回车，触发搜索点击事件
            if (event != null) {
                LogUtil.loge("KeyEvent:" + event.getAction());
            }
            // 隐藏软键盘
            hideSoftInput();
            RecordWin2Manager.getInstance().doEditSearchResult(mViewHolder.mEtDest.getText().toString());
            WinLayout.isSearch = WinLayout.targetView != TXZRecordWinManager.RecordWin2.RecordWinController.TARGET_CONTENT_CHAT;
            return true;
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(mViewHolder.mEtDest.getEditableText().toString())) {
                mViewHolder.mBtnSearch.setSelected(false);
            } else {
                mViewHolder.mBtnSearch.setSelected(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
}
