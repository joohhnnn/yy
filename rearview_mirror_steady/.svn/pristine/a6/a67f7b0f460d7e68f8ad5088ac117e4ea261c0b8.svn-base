//package com.txznet.music.ui.adapter;
//
//import android.graphics.Color;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.txznet.music.R;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.baseModule.dao.DBManager;
//import com.txznet.music.baseModule.ui.BaseFragment;
//import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
//import com.txznet.music.utils.StringUtils;
//import com.txznet.music.utils.Utils;
//
//import java.util.List;
//
///**
// * Created by Terry on 2017/5/4.
// */
//
//public class ListGridViewAdapter extends BaseAdapter {
//
//    private BaseFragment mBaseFragment;
//    private List<Audio> mAudios;
//    LayoutInflater mInflater;
//
//
//    public ListGridViewAdapter(BaseFragment baseFragment, List<Audio> audios) {
//        this.mBaseFragment = baseFragment;
//        this.mAudios = audios;
//        this.mInflater = LayoutInflater.from(mBaseFragment.getActivity());
//    }
//
//    @Override
//    public int getCount() {
//        synchronized (mAudios) {
//            if (null != mAudios) {
//                return mAudios.size();
//            }
//        }
//        return 0;
//    }
//
//    public List<Audio> getAudios(){
//        return  mAudios;
//    }
//
//    @Override
//    public Object getItem(int position) {
//        synchronized (mAudios) {
//            if (null != mAudios) {
//                return mAudios.get(position);
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public long getItemId(int position) {
//        synchronized (mAudios) {
//            if (null != mAudios) {
//                return mAudios.get(position).getId();
//            }
//        }
//        return 0;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView == null) {
//            convertView = mInflater.inflate(R.layout.item_list_grid,null);
//        }
//        final Audio audio = (Audio) getItem(position);
//        ImageView icon = (ImageView) convertView.findViewById(R.id.iv_grid_item_icon);
//        TextView name = (TextView) convertView.findViewById(R.id.tv_grid_item_name);
//        TextView artist = (TextView) convertView.findViewById(R.id.tv_grid_item_artist);
//        ImageView delete = (ImageView) convertView.findViewById(R.id.iv_grid_item_delete);
//
//        if (audio.getLogo() != null) {
//            ImageLoader.getInstance().displayImage(audio.getLogo(), icon);
//        } else {
//            icon.setBackgroundResource(R.drawable.local_item_default);
//        }
//        name.setText(audio.getName());
//        if (Utils.isSong(audio.getSid())) {
//            artist.setText(StringUtils.toString(audio.getArrArtistName()));
//        } else {
//            artist.setText(audio.getAlbumName());
//        }
//
//        convertView.setTag(audio);
//        delete.setTag(audio);
//        delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Audio audio1 = (Audio) v.getTag();
//                DBManager.getInstance().removeHistoryAudio(audio);
//                mAudios.remove(audio1);
//                notifyDataSetChanged();
//            }
//        });
//
//        if (PlayEngineFactory.getEngine().getCurrentAudio() != null
//                && PlayEngineFactory.getEngine().getCurrentAudio().getSid() == audio.getSid()
//                && PlayEngineFactory.getEngine().getCurrentAudio().getId() == audio.getId()) {
//            name.setTextColor(Color.parseColor("#E82526"));
//            name.setMarqueeRepeatLimit(-1);
//        } else {
//            name.setTextColor(Color.parseColor("#FFFFFF"));
//            name.setMarqueeRepeatLimit(0);
//        }
//
////        convertView.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                try {
////                    Audio audio1 = (Audio) v.getTag();
////                    PlayEngineFactory.getEngine().playAudio();
////                } catch (Exception e) {
////                    LogUtil.loge("Play",e);
////                }
////            }
////        });
//        return convertView;
//    }
//}
