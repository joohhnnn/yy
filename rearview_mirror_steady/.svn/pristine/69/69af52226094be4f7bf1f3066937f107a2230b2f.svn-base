package com.txznet.tts.ui;

import com.txznet.tts.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class DialogInput {
	    public interface IResultCallBack{
	    	public void onResult(String strText);
	    }
	    
	    public static void showDialog(final IResultCallBack cb) {  
	    	LayoutInflater inflater = LayoutInflater.from(ActivityManager.top());  
	    	final View inputView = inflater.inflate(R.layout.input_dialog, null);
	    	final EditText editView = (EditText) inputView.findViewById(R.id.dialog_input_edittext);
	    	
	    	AlertDialog.Builder builder = new AlertDialog.Builder(ActivityManager.top());
	    	builder.setIcon(android.R.drawable.ic_dialog_info);
	    	builder.setTitle("输入文本"); 
	    	builder.setView(inputView);
	    	builder.setPositiveButton("确定", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String strText = editView.getText().toString();
					if (cb != null){
						cb.onResult(strText);
					}
				}
			});
	    	builder.show();
	    }

}
