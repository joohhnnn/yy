package com.txznet.comm.ui.viewfactory.view.defaults;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ISearchEditView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * Created by ASUS User on 2018/7/19.
 */

public class DefaultSearchEditView extends ISearchEditView {

    class ViewHolder {
        LinearLayout mBtnSearch;
        EditText mEtDest;
        RelativeLayout mRlBack;
        TextView mKeyBoard;
        View rootView;
    }

    private ViewHolder mViewHolder = null;

    private static DefaultSearchEditView instance = new DefaultSearchEditView();

    public static DefaultSearchEditView getInstance() {
        return instance;
    }

    private DefaultSearchEditView() {

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
        adapter.view = createContentView();
        adapter.type = ViewData.TYPE_SEARCH_EDIT_VIEW;
        return adapter;
    }

    private View createContentView() {
        mViewHolder = new ViewHolder();
        LinearLayout layout = new LinearLayout(GlobalContext.get());
        mViewHolder.rootView = layout;
        layout.setBackgroundDrawable(LayouUtil.getDrawable("widget_color"));
        layout.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout rlTitle = new RelativeLayout(GlobalContext.get());
        rlTitle.setPadding((int) LayouUtil.getDimen("x30"), 0, (int) LayouUtil.getDimen("x60"), 0);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        ivBack.setScaleType(ImageView.ScaleType.FIT_END);
        ivBack.setImageDrawable(LayouUtil.getDrawable("button_back"));
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) LayouUtil.getDimen("y16"));
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlLayoutParams.rightMargin = (int) LayouUtil.getDimen("x20");
        ivBack.setId(ViewUtils.generateViewId());
        ivBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(ivBack);

        TextView tvBack = new TextView(GlobalContext.get());
        tvBack.setTextColor(Color.parseColor("#FFFFFF"));
        tvBack.setHintTextColor(Color.parseColor("#40454b"));
        TextViewUtil.setTextSize(tvBack, 31);
        tvBack.setText("??????");
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.RIGHT_OF, ivBack.getId());
        tvBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(tvBack);

        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
        tvTitle.setHintTextColor(Color.parseColor("#40454b"));
        TextViewUtil.setTextSize(tvTitle, 37);
        tvTitle.setText("???????????????");
        tvTitle.setGravity(Gravity.CENTER);
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        tvTitle.setLayoutParams(rlLayoutParams);
        rlTitle.addView(tvTitle);

        final LinearLayout llEdit = new LinearLayout(GlobalContext.get());
        llEdit.setPadding((int) LayouUtil.getDimen("x60"), 0, (int) LayouUtil.getDimen("x60"), 0);
        llEdit.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llEdit.setLayoutParams(llLayoutParams);
        layout.addView(llEdit);

        final EditText edDest = new EditText(GlobalContext.get());
        mViewHolder.mEtDest = edDest;
//        edDest.setBackgroundColor(Color.parseColor("#202326"));
        edDest.setBackgroundDrawable(LayouUtil.getDrawable("search_edit_bg"));
        edDest.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        edDest.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edDest.setHint("????????????????????????");
        edDest.setPadding((int) LayouUtil.getDimen("x24"), 0, 0, 0);
        edDest.setSingleLine();
        edDest.setTextColor(Color.parseColor("#FFFFFF"));
        edDest.setHintTextColor(Color.parseColor("#40454b"));
        edDest.setCursorVisible(false);
        TextViewUtil.setTextSize(edDest, 38);
        llLayoutParams = new LinearLayout.LayoutParams(0, (int) LayouUtil.getDimen("y90"), 1);
        llLayoutParams.setMargins(0, 0, 0, 0);
        edDest.setLayoutParams(llLayoutParams);
        llEdit.addView(edDest);

        final LinearLayout llSearch = new LinearLayout(GlobalContext.get());
        mViewHolder.mBtnSearch = llSearch;
        llSearch.setBackgroundDrawable(LayouUtil.getDrawable("activity_home_search_bg"));
        llSearch.setGravity(Gravity.CENTER);
        llSearch.setOrientation(LinearLayout.VERTICAL);
        llSearch.setClickable(true);
        llSearch.setFocusable(true);
        llSearch.setBackgroundDrawable(LayouUtil.getDrawable("search_btn_bg"));
        llLayoutParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("x150"), (int) LayouUtil.getDimen("y90"));
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

        TextViewUtil.setTextSize(tvSearch,36);
        tvSearch.setBackgroundColor(Color.TRANSPARENT);
        tvSearch.setTextColor(createColorStateList(
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#8ce3fd"),
                Color.parseColor("#8ce3fd"),
                0xfff));
        tvSearch.setText("??????");


        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.CENTER;
        tvSearch.setLayoutParams(llLayoutParams);
        llSearch.addView(tvSearch);


        rlBack.setOnClickListener(onClickListener);
        llSearch.setOnClickListener(onClickListener);

        edDest.setOnFocusChangeListener(onFocusChangeListener);

        // ??????????????????????????????
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
                    tvSearch.setText("??????");
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
     * ???TextView???????????????????????????????????????
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
                if (v == mViewHolder.mRlBack) {
                    // ???????????????
                    hideSoftInput();
                    RecordWin2Manager.getInstance().doEditSearchClickCancel();
                } else if (v == mViewHolder.mBtnSearch) {
                    // ???????????????
                    hideSoftInput();
                    RecordWin2Manager.getInstance().doEditSearchResult(mViewHolder.mEtDest.getText().toString());
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
            // ???????????????????????????????????????
            if (event != null) {
                LogUtil.loge("KeyEvent:" + event.getAction());
            }
            // ???????????????
            hideSoftInput();
            RecordWin2Manager.getInstance().doEditSearchResult(mViewHolder.mEtDest.getText().toString());
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
