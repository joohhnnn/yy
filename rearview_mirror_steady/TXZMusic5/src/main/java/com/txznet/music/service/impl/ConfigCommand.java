package com.txznet.music.service.impl;

import com.txznet.music.action.SettingActionCreator;
import com.txznet.rxflux.Operation;

/**
 * 通过TXZMusicManager来配置同听的一些参数
 * 全部改成配置文件来进行配置吧.不用这种方式了
 *
 * @author telen
 * @date 2018/12/28,16:59
 * @deprecated use {@link com.txznet.txz.util.TXZFileConfigUtil} instead
 */
@Deprecated
public class ConfigCommand extends BaseCommand {
    public ConfigCommand() {

        //以下来源于TXZMusicManager里面直接传递给同听的命令字
        /////
     /*   "startappplay"//废弃//设置App是否一启动就继续播放上次未关闭的音频
        "fullscreen"//废弃//置App是否全屏
        "backVisible"//废弃//设置App是否有返回按钮
        "param.tips.show"//废弃//设置App是否隐藏"已切换到xxx(歌曲名)"的提示[每当歌曲切换的时候的提示]
        "tips.gravity"//废弃//设置App提示框显示的位置

        "enableFloatingPlayer"//废弃//设置是否开启悬浮播放器
        "enableSplash"//废弃//设置是否开启闪屏页
        "autoJumpPlayerPage"//废弃//设置是否自动打开播放页面
        "closeVolume"//废弃//设置是否关闭电台之家对声音的控制

        "setExitWithPlay"//废弃//设置点击返回按键是否退出播放
        "setExtraTypeface"//废弃//设置是否适用额外的字体库，支持韩文日文

        "setShortPlayNeedTrigger"//废弃//设置快报是否需要手动触发
        "triggerShortPlay"//废弃//手动触发快报
        "showExitDialog"//废弃//设置点击返回退出同听时是否弹出对话框
        "wakeup_value"//接口的形式,设置同听是否注册免唤醒词，与设置界面同步


        "notOpenAppPName"//声控后是否打开播放器界面
        "releaseAudioFocus"//电台之家丢失音频焦点后是否释放焦点
        "wakeupPlay"//设置电台之家在休眠唤醒后是否继续播放
        "resume_auto_play"//设置打开同听后是否自动开始播放
        "shortPlayEnable"//设置默认是否开启快报推送，
        "searchSize"//设置本地扫描最小扫描文件大小,默认为500K
        "searchPath"//设置本地扫描路径
        "needAsr"//设置App是否启用全局唤醒词
        "wakeup_default"//设置同听是否注册免唤醒词的默认值，用户修改后以用户设置为准l
        */

//        addCmd("startappplay", (pkgName, cmd, data) -> {
//            // TODO: 2018/12/28 是否一启动就继续播放上次未关闭的音频
//            return new byte[0];
//        });
//        addCmd("fullscreen", (pkgName, cmd, data) -> {
//            // TODO: 2018/12/28 设置App是否全屏
//            return new byte[0];
//        });
//        addCmd("backVisible", (pkgName, cmd, data) -> {
//            // TODO: 2018/12/28 设置App是否有返回按钮
//            return new byte[0];
//        });
        addCmd("wakeup_value", (pkgName, cmd, data) -> {
            SettingActionCreator.getInstance().clickAsr(Operation.SDK);
            return new byte[0];
        });

    }
}
