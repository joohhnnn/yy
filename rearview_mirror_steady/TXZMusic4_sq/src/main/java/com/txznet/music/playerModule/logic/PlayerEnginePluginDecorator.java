//package com.txznet.music.playerModule.logic;
//
//import com.txznet.audio.player.TXZAudioPlayer;
//import com.txznet.music.albumModule.bean.Album;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.baseModule.Constant;
//import com.txznet.music.baseModule.bean.EnumState.AudioType;
//import com.txznet.music.baseModule.bean.EnumState.OperState;
//import com.txznet.music.baseModule.bean.PlayerInfo;
//import com.txznet.music.baseModule.plugin.CommandString;
//import com.txznet.music.playerModule.bean.IPlayerState;
//import com.txznet.music.util.TimeUtils;
//import com.txznet.txz.plugin.PluginManager;
//
//import java.util.List;
//
///**
// * 通知插件
// * 参数  interceptValue 是否拦截
// */
//public class PlayerEnginePluginDecorator implements IPlayerEngine {
//    IPlayerEngine engine;
//    TXZAudioPlayer mPlayer;
//
//    public PlayerEnginePluginDecorator(IPlayerEngine engine) {
//        this.engine = engine;
//    }
//
//    @Override
//    public void setState(IPlayerState state) {
//        engine.setState(state);
//    }
//
//    @Override
//    public IPlayerState getState() {
//        return engine.getState();
//    }
//
//    @Override
//    public TXZAudioPlayer getAudioPlayer() {
//        if (mPlayer == null) {
//            Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.GETAUDIOPLAYER);
//            boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//            if (!interceptValue) {
//                mPlayer = engine.getAudioPlayer();
//            } else {
//                mPlayer = (TXZAudioPlayer) PluginManager.invoke(CommandString.PLUGIN_PLAYER_SINGLE + CommandString.GETAUDIOPLAYER, PlayInfoManager.getInstance().getCurrentAudio());
////            PluginBean mBean = new PluginBean();
////            mBean.setCmd(CommandString.CREATEPLAYER);
////            mBean.setObj(TESTRecode.getmCurrentInfo().getmCurrentAudio());
////            return AudioPluginUtil.getObjFromPlugin(Environment.getExternalStorageDirectory() + "/txz/audio/output.dex", "com.txznet.audio.player.TestAudioPlugin", mBean, TXZAudioPlayer.class);
//            }
//        }
//        return mPlayer;
//    }
//
//    @Override
//    public void init() {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.INIT);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.init();
//        }
//    }
//
//    @Override
//    public void play(OperState operation) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.PLAY);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.play(operation);
//        }
//    }
//
//    @Override
//    public void pause(OperState operation) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.PAUSE);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.pause(operation);
//        }
//    }
//
//    @Override
//    public void playOrPause(OperState operation) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.PLAYORPAUSE);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.playOrPause(operation);
//
//        }
//    }
//
//    @Override
//    public void playAudio(OperState operation, Audio willPlayAudio) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.PLAYAUDIO);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.playAudio(operation, willPlayAudio);
//        }
//    }
//
//    @Override
//    public void release(OperState operation) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.RELEASE);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            //注销掉
//            engine.release(operation);
//        }
//        mPlayer=null;
//    }
//
//    @Override
//    public void next(OperState operation) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.NEXT);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.next(operation);
//        }
//    }
//
//    @Override
//    public void last(OperState operation) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.LAST);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.last(operation);
//        }
//    }
//
//    @Override
//    public void changeMode(OperState operation) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.CHANGEMODE);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.changeMode(operation);
//        }
//    }
//
//    @Override
//    public void changeMode(OperState operation, @PlayerInfo.PlayerMode int mode) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.CHANGEMODE);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.changeMode(operation, mode);
//        }
//    }
//
//
//    @Override
//    public void seekTo(OperState operation, long position) {
//        TimeUtils.endTime(Constant.SPEND_TAG+"quick:prepare:seekto");
//        TimeUtils.startTime(Constant.SPEND_TAG+"quick:seekto");
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.SEEKTO);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.seekTo(operation, position);
//        }
//    }
//
//    @Override
//    public void setVolume(OperState operState, float volume) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.SETVOLUME);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.setVolume(operState, volume);
//        }
//    }
//
//    @Override
//    public void searchListData(OperState operation, boolean isDown) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.SEARCHLISTDATA);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.searchListData(operation, isDown);
//        }
//    }
//
//    @Override
//    public Album getCurrentAlbum() {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.GETCURRENTALBUM);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            return engine.getCurrentAlbum();
//        } else {
//            return null;//TODO:拦截之后的处理？
//        }
//
//    }
//
//    @Override
//    public Audio getCurrentAudio() {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.GETCURRENTAUDIO);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            return engine.getCurrentAudio();
//        } else {
//            return null;
//        }
//    }
//
//    @Override
//    public void setAudios(OperState operation, List<Audio> audios, Album album, int index,int ori) {
//        //TODO:混淆是否有问题 Audio album
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.SETAUDIOS, audios, album, index);//这里往插件上传递数据
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.setAudios(operation, audios, album, index,ori);
//        }
//    }
//
//    @Override
//    public void addAudios(OperState operation, final List<Audio> audios, boolean isAddLast) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.ADDAUDIOS, audios, isAddLast);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.addAudios(operation, audios, isAddLast);
//
//        }
//    }
//
//    @Override
//    public boolean isPlaying() {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.ISPLAYING);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            return engine.isPlaying();
//        } else {
//            //TODO:
//            return false;
//        }
//    }
//
//	@Override
//	public IPlayerState getReleaseState() {
//		return engine.getReleaseState();
//	}
//
//	@Override
//	public IPlayerState getBufferState() {
//		return engine.getBufferState();
//	}
//
//	@Override
//	public IPlayerState getPauseState() {
//		return engine.getPauseState();
//	}
//
//	@Override
//	public IPlayerState getPlayState() {
//		return engine.getPlayState();
//	}
//
//	@Override
//    public IPlayerState getTempPauseState(IPlayerState currentPlayState) {
//		return engine.getTempPauseState(currentPlayState);
//	}
//
//    @Override
//    public void playAudioList(OperState sound, List<Audio> localAudios, int index, Album album) {
//        engine.playAudioList(sound,localAudios,index,album);
//    }
//
//
//    @Override
//    public void prepareAsync(OperState operation) {
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_ENGINE + CommandString.PREPAREASYNC);
//        boolean interceptValue = (obj != null && obj instanceof Boolean) && Boolean.parseBoolean(obj.toString());
//        if (!interceptValue) {
//            engine.prepareAsync(operation);
//        }
//    }
//
//}
