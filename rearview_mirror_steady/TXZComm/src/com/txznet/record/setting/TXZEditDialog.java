package com.txznet.record.setting;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.txznet.txz.comm.R;

public class TXZEditDialog extends Dialog {

	Context mContext;
	protected TXZEditDialog(Context context) {
		super(context, R.style.TXZ_Dialog_Style);
		this.mContext=context;
		setCustomDialog();
	}
	private EditText editText;
    private Button positiveButton, negativeButton;
    private TextView title;
	private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.layout_edit, null);
        title = (TextView) mView.findViewById(R.id.txt_dialogTitle);
        editText = (EditText) mView.findViewById(R.id.et_command);
        positiveButton = (Button) mView.findViewById(R.id.commit_editCommand);
        negativeButton = (Button) mView.findViewById(R.id.cancel_editCommand);
        
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        mView.measure(w, h);
        int width = mView.getMeasuredWidth();
        int height = mView.getMeasuredHeight();
        getWindow().setLayout(width, height);
        editText.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int editStart;
			private int editEnd;
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				temp=s;
				if(temp.length()==8){
					Toast.makeText(mContext, "亲，您已输入"+temp.length()+"个字符", Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				editStart=editText.getSelectionStart();
				editEnd=editText.getSelectionEnd();
				 if(temp.length()>8){
					s.delete(editStart-1, editEnd);
					int tempSelection=editStart;
					editText.setText(s.toString());
					editText.setSelection(tempSelection);
				}
			}
		});
        super.setContentView(mView);
    }
     
    public View getEditText(){
        return editText;
    }
 
    public View getTitleTextView(){
    	return title;
    }
    @Override
	public void setContentView(int layoutResID) {
	}

	@Override
	public void setContentView(View view) {
	}

	@Override
	public void setContentView(View view,
			LayoutParams params) {
	}

	/**
     * 确定键监听器
     * @param listener
     */ 
    public void setOnPositiveListener(View.OnClickListener listener){ 
        positiveButton.setOnClickListener(listener); 
    } 
    /**
     * 取消键监听器
     * @param listener
     */ 
    public void setOnNegativeListener(View.OnClickListener listener){ 
        negativeButton.setOnClickListener(listener); 
    }
    
}
