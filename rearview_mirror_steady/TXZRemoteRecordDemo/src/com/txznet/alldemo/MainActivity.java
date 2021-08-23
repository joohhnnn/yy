package com.txznet.alldemo;

import java.util.ArrayList;
import java.util.List;

import com.txznet.alldemo.ui.ActionListAdapter;
import com.txznet.alldemo.ui.ActionManager;
import com.txznet.alldemo.ui.AutoAction;
import com.txznet.rmtrecorddemo.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class MainActivity extends Activity {
     ListView mListView;
     List<AutoAction> mActionList = new ArrayList<AutoAction>();
     ActionListAdapter mActionListAdapter;
     
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initData();
		initView();
	}

	private void initView(){
		mListView = (ListView) findViewById(R.id.actionList);
		mActionListAdapter = new ActionListAdapter(this, mActionList);
		mListView.setAdapter(mActionListAdapter);
	}
	
	private void initData(){
		ActionManager.fillAction(mActionList);
	}
}
