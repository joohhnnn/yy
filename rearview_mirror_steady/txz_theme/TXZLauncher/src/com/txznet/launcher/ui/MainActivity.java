package com.txznet.launcher.ui;

import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.view.KeyEvent;

import com.txznet.launcher.bean.AppIconInfo;
import com.txznet.launcher.bean.AppInfo;
import com.txznet.launcher.helper.ResidentApp;
import com.txznet.launcher.layout.GridLayoutModel;
import com.txznet.launcher.layout.LayoutModel;
import com.txznet.launcher.layout.LayoutModel.Plugin;
import com.txznet.launcher.layout.TriangleLayoutModel;
import com.txznet.launcher.ui.base.ProxyContext;
import com.txznet.launcher.ui.base.ThemeActivity;
import com.txznet.loader.AppLogic;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

public class MainActivity extends ThemeActivity {

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 解析资源中的布局配置来解析布局模型
    @Override
    protected LayoutModel createLayoutModel(ProxyContext proxyContext) {
        LayoutModel model = null;
        int conf = proxyContext.getIdentifier("xml", "layout_conf");
        XmlResourceParser parser = proxyContext.getResources().getXml(conf);
        try {
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlResourceParser.START_TAG) {
                    String name = parser.getName();
                    if (name.equals("layout-config")) { // 根节点跳过
                        parser.next();
                        continue;
                    }
                    if (name.equals("type")) {
                        parser.next();
                        int type = Integer.parseInt(parser.getText());
                        if (type == LayoutModel.LAYOUT_TYPE_GRID) {
                            model = new GridLayoutModel(this, proxyContext);
                        }
                        if (type == LayoutModel.LAYOUT_TYPE_TRIANGLE) {
                            model = new TriangleLayoutModel(this, proxyContext);
                        }
                    } else if (name.equals("plugins")) {
                        if (model.plugins == null) {
                            model.plugins = new ArrayList<Plugin>();
                        }
                    } else if (name.equals("plugin")) {
                        LayoutModel.Plugin plugin = new LayoutModel.Plugin();
                        plugin.name = parser.getAttributeValue(0);
                        plugin.start = parser.getAttributeIntValue(1, 0);
                        plugin.end = parser.getAttributeIntValue(2, 0);
                        model.plugins.add(plugin);
                    }else if(name.equals("icon-values")){
                    	if (model.iconInfo == null) {
                            model.iconInfo = new ArrayList<AppInfo>();
                        }
                    }else if(name.equals("icon")){
                    	AppInfo appInfo=new AppInfo();
                    	appInfo.setAppName(parser.getAttributeValue(2));
                    	appInfo.setAppNameId(parser.getAttributeValue(2));
                    	appInfo.setClassName(parser.getAttributeValue(4));
                    	appInfo.setIcon(proxyContext.getDrawable(parser.getAttributeValue(1)));
                    	appInfo.setPackageName(parser.getAttributeValue(3));
                    	appInfo.setSystemApp(true);
                    	model.iconInfo.add(appInfo);
                    }
                }
                parser.next();
            }
            ResidentApp.syncCustomResidentApp(model.iconInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        parser.close();
        return model;
    }
}
