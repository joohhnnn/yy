package com.txznet.txz.module.help;

public interface IHelpTips {
    void registerTipsInfo(String msg);
    void registerTemporaryTipsInfo(String msg);
    void unRegisterTipsInfo(String msg);
}
