package com.txznet.sdkdemo.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.TXZNavManager.NavTool;
import com.txznet.sdk.TXZNavManager.NavToolStatusHighListener;
import com.txznet.sdk.TXZNavManager.NavToolStatusListener;
import com.txznet.sdk.TXZNavManager.NavToolType;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class NavActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "设置导航工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().setNavTool(mNavTool);
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}), new DemoButton(this, "取消导航工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().setNavTool((NavTool) null);
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}));

		addDemoButtons(new DemoButton(this, "同行者导航", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance()
						.setNavTool(NavToolType.NAV_TOOL_TXZ);
				
				DebugUtil.showTips("已将默认声控导航设置为：" + ((Button)v).getText());
			}
		}), new DemoButton(this, "百度地图", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().setNavTool(
						NavToolType.NAV_TOOL_BAIDU_MAP);
				
				DebugUtil.showTips("已将默认声控导航设置为：" + ((Button)v).getText());
			}
		}), new DemoButton(this, "百度导航", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().setNavTool(
						NavToolType.NAV_TOOL_BAIDU_NAV);
				
				DebugUtil.showTips("已将默认声控导航设置为：" + ((Button)v).getText());
			}
		}), new DemoButton(this, "百度导航HD", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().setNavTool(
						NavToolType.NAV_TOOL_BAIDU_NAV_HD);
				
				DebugUtil.showTips("已将默认声控导航设置为：" + ((Button)v).getText());
			}
		}));
		addDemoButtons(new DemoButton(this, "高德地图", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().setNavTool(
						NavToolType.NAV_TOOL_GAODE_MAP);
				
				DebugUtil.showTips("已将默认声控导航设置为：" + ((Button)v).getText());
			}
		}), new DemoButton(this, "高德导航", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().setNavTool(
						NavToolType.NAV_TOOL_GAODE_NAV);
				
				DebugUtil.showTips("已将默认声控导航设置为：" + ((Button)v).getText());
			}
		}), new DemoButton(this, "高德车机版", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().setNavTool(
						NavToolType.NAV_TOOL_GAODE_MAP_CAR);
				
				DebugUtil.showTips("已将默认声控导航设置为：" + ((Button)v).getText());
			}
		}), new DemoButton(this, "凯立德导航", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().setNavTool(
						NavToolType.NAV_TOOL_KAILIDE_NAV);
				
				DebugUtil.showTips("已将默认声控导航设置为：" + ((Button)v).getText());
			}
		}));

		addDemoButtons(new DemoButton(this, "回到导航", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().enterNav();
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}), new DemoButton(this, "退出导航", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().exitNav();
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}));

		addDemoButtons(new DemoButton(this, "回家", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().navHome();
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}), new DemoButton(this, "去公司", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZNavManager.getInstance().navCompany();
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}), new DemoButton(this, "导航到指定位置", new OnClickListener() {
			@Override
			public void onClick(View v) {
				Poi poi = new Poi();
				poi.setLat(22.541544);
				poi.setLng(114.059624);
				poi.setName("测试地点");
				poi.setCity("深圳");
				poi.setGeoinfo("深圳的测试地点");
				TXZNavManager.getInstance().navToLoc(poi);
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}));

	}

	NavTool mNavTool = new NavTool() {
		@Override
		public void setStatusListener(NavToolStatusListener listener) {
			// TODO 请记录下导航状态监听器，通过监听器来通知状态给同行者
		}

		@Override
		public void navToLoc(Poi poi) {
			DebugUtil.showTips("导航到指定POI点：经度" + poi.getLng() + "，纬度"
					+ poi.getLat());
		}

		@Override
		public void navHome() {
			// 已废弃
			DebugUtil.showTips("回家");
		}

		@Override
		public void navCompany() {
			// 已废弃
			DebugUtil.showTips("去公司");
		}

		@Override
		public boolean isInNav() {
			//TODO 返回真实的导航状态
			return false;
		}

		@Override
		public void exitNav() {
			DebugUtil.showTips("退出导航");
		}

		@Override
		public void enterNav() {
			// TODO 返回导航，在isNav返回true时回到原来的导航界面
			DebugUtil.showTips("回到导航");
		}

		@Override
		public void setCompanyLoc(Poi arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setHomeLoc(Poi arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setStatusListener(NavToolStatusHighListener arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void speakLimitSpeed() {
			// TODO Auto-generated method stub
			
		}
	};

}
