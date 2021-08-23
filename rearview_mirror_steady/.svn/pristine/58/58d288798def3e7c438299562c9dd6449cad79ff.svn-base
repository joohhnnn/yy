package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.LouHolder;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ConstellationFortuneData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IConstellationFortuneView;
import com.txznet.resholder.R;

import java.util.HashMap;

/**
 * 星座运势
 * <p>
 * 2020-08-10 17:42
 *
 * @author xiaolin
 */
public class ConstellationFortuneView extends IConstellationFortuneView {

    private static ConstellationFortuneView sInstance = new ConstellationFortuneView();

    public static HashMap<String, Integer> constellationPicture = new HashMap<String, Integer>(){{
        put("白羊座", R.drawable.constellation_baiyang);
        put("金牛座", R.drawable.constellation_jinniu);
        put("双子座", R.drawable.constellation_shuagnzi);
        put("巨蟹座", R.drawable.constellation_juxie);
        put("狮子座", R.drawable.constellation_shizi);
        put("处女座", R.drawable.constellation_chunv);
        put("天秤座", R.drawable.constellation_tianping);
        put("天蝎座", R.drawable.constellation_tianxie);
        put("射手座", R.drawable.constellation_sheshou);
        put("摩羯座", R.drawable.constellation_mojie);
        put("水瓶座", R.drawable.constellation_shuiping);
        put("双鱼座", R.drawable.constellation_shuangyu);
    }};
    public static int getConstellationDrawableRes(String name){
        Integer res = constellationPicture.get(name);
        if(res != null){
            return res;
        }
        return 0;
    }


    private ConstellationFortuneView() { }
    public static ConstellationFortuneView getInstance() {
        return sInstance;
    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        ConstellationFortuneData constellationFortuneData = (ConstellationFortuneData) data;
        WinLayout.getInstance().vTips = constellationFortuneData.vTips;

        View view = createViewNone(constellationFortuneData);

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = BindDeviceView.getInstance();
        return adapter;
    }


    private View createViewNone(ConstellationFortuneData data) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.constellation_fortune_view, (ViewGroup)null);
        LouHolder holder = LouHolder.createInstance(view);

        holder.putText(R.id.tvName, data.name + data.fortuneType);
        Integer constellationPictureId = constellationPicture.get(data.name);
        if(constellationPictureId != null){
            holder.putImg(R.id.ivName, constellationPictureId);
        }
        holder.putText(R.id.tvDesc, data.desc);

        int[] ids = {
            R.id.ivStar1,
            R.id.ivStar2,
            R.id.ivStar3,
            R.id.ivStar4,
            R.id.ivStar5,
        };
        for (int i = 0; i < 5; i++) {
            holder.putImg(ids[i], i < data.level ? R.drawable.star_enable : R.drawable.star_disable);
        }

        return view;
    }

    @Override
    public void init() {
        super.init();
    }

    /**
     * 切换模式修改布局参数
     *
     * @param styleIndex
     */
    public void onUpdateParams(int styleIndex) {

    }


}
