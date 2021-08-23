package com.example.resholder;

import com.txznet.resholder.R;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getResources().getDrawable(R.drawable.gradient_bg);
	}
}