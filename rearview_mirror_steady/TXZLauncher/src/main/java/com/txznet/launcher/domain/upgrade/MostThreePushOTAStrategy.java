package com.txznet.launcher.domain.upgrade;


import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.cfg.DebugCfg;
import com.txznet.launcher.utils.PreferenceUtil;

/**
 * Created by daviddai on 2018/9/7
 * 每一个版本收到后，有两种情况会停止通知用户升级。
 * 1.不论是用户取消升级还是升级过程中失败了，都计一次数。当累计3次的时候，就不给用户推送这个版本的升级
 * 2.当安装过程中
 */
public class MostThreePushOTAStrategy implements IPushOTAStrategy {

    @Override
    public void onReceiveOTAPush(String version) {
        String localVersion = PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_OTA_UPGRADE_VERSION, "");

        // 不是相同的version，将保存的数据重置，然后保存最新的version
        if (!localVersion.equals(version)) {
            PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_OTA_UPGRADE_VERSION, version);
            updateCount(true);
            // 标志当前版本没有下载过。
            setHadDownload(false);
        }
        // 相同的version 不处理
    }

    @Override
    public int isNotifyOTAUpgrade() {
        // 判断是否下载过
        if (hadDownload()) {
            // 判断当前类型提示的次数，少于3次才提示
            int count = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_OTA_UPGRADE_TIP_CONTINUE_COUNT, 0);
            if (count >= 0 && count < 3) {// 只提示三次，超过了就不提示
                return IPushOTAStrategy.RESUME_UPDATE;
            } else {
                return IPushOTAStrategy.NO_UPDATE;
            }
        } else {
            // 判断当前类型提示的次数，少于3次才提示
            int count = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_OTA_UPGRADE_TIP_NEW_COUNT, 0);
            if (count >= 0 && count < 3) {// 只提示三次，超过了就不提示
                return IPushOTAStrategy.FIRST_UPDATE;
            } else {
                return IPushOTAStrategy.NO_UPDATE;
            }
        }
    }

    @Override
    public void onShow() {
        updateCount(false);
    }

    @Override
    public void onSelectDownload() {
        setHadDownload(true);
    }

    @Override
    public void onCancel() {
        updateCount(false);
    }

    @Override
    public void onFailure() {
        updateCount(false);
    }

    private boolean hadDownload() {
        return PreferenceUtil.getInstance().getBoolean(PreferenceUtil.KEY_OTA_UPGRADE_DOWNLOAD, false);
    }

    /**
     * 是否下载过
     * @param had 是否下载过
     */
    public void setHadDownload(boolean had){
        PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_OTA_UPGRADE_DOWNLOAD,had);
    }


    /**
     * 当是新版本的时候，重置计数；相同版本累计数。
     *
     * @param isNew 是新版本
     */
    private void updateCount(boolean isNew) {
        if (isNew || DebugCfg.OTA_UPGRADE_DEBUG) {
            PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_OTA_UPGRADE_TIP_NEW_COUNT, 0);
            PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_OTA_UPGRADE_TIP_CONTINUE_COUNT, 0);
        } else {
            // 计数加一
            if (hadDownload()) {
                PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_OTA_UPGRADE_TIP_CONTINUE_COUNT, PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_OTA_UPGRADE_TIP_CONTINUE_COUNT, 0) + 1);
            }else {
                PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_OTA_UPGRADE_TIP_NEW_COUNT, PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_OTA_UPGRADE_TIP_NEW_COUNT, 0) + 1);
            }
        }
        LogUtil.e("updateCount: upgrade tip new count=" + PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_OTA_UPGRADE_TIP_NEW_COUNT, 0));
        LogUtil.e("updateCount: upgrade tip continue count=" + PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_OTA_UPGRADE_TIP_CONTINUE_COUNT, 0));
    }
}
