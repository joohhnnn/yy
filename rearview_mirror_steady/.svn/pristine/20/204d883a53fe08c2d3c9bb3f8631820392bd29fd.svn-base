package com.txznet.music.report;

/**
 * 上报协议
 * 依据《数据报点整理.docx》- 2018/12/26
 *
 * @author zackzhou
 * @date 2018/12/26,19:32
 */

public interface ReportEventProtocol {

    // --- 系统报点 [100000, 200000)
    int SYS_OPEN_MUSIC = 100000; // 打开同听
    int SYS_EXIT_MUSIC = 100001; // 退出同听
    int SYS_PAGE_SLIDE = 100500; // Tab滑动报点
    int SYS_PAGE_RECOMMAND_CLICK = 100600; // 推荐tab点击报点
    int SYS_PAGE_MUSIC_CLICK = 100601; // 音乐tab点击报点
    int SYS_PAGE_RADIO_CLICK = 100602; // 电台tab点击报点

    // -- 全局声控报点(101000-102000)
    int VOICE_PREV_OR_NEXT = 101000; // 上一首/下一首
    int VOICE_FAVOUR_OR_UNFAVOUR = 101001; // 加入收藏/取消收藏
    int VOICE_SUBSCRIBE_OR_UNSUBSCRIBE = 101002; // 加入订阅/取消订阅

    // -- 本地音乐报点(300000-301000)
    int LOCAL_ENTER = 300000; // 进入本地音乐
    int LOCAL_SCAN = 300001; // 本地扫描
    int LOCAL_DELETE = 300002; // 删除管理
    int LOCAL_SORT_BY_TIME = 300003; // 按添加时间排序
    int LOCAL_SORT_BY_NAME = 300004; // 按歌曲首字母排序
    int LOCAL_FAVOUR_OR_UNFAVOUR = 300005; // 收藏/取消收藏本地歌曲
    int LOCAL_COVER_CLICK = 300006; // 点击封面播放本地音乐
    int LOCAL_LIST_ITEM_CLICK = 300007; // 点击列表播放本地音乐

    // --个人中心(301000-302000)
    int USER_CENTER_CLICK = 301000; // 点击个人中心
    // --个人中心-收藏(301100-301200)
    int USER_CENTER_ITEM_FAVOUR_ENTER = 301100; // 进入收藏
    int USER_CENTER_ITEM_FAVOUR_FAVOUR_OR_UNFAVOUR = 301101; // 收藏-收藏/取消收藏
    int USER_CENTER_ITEM_FAVOUR_LIST_ITEM_CLICK = 301102; // 收藏 - 点击列表
    int USER_CENTER_ITEM_FAVOUR_EXIT = 301103; // 退出收藏
    // --个人中心-订阅(301200-301300)
    int USER_CENTER_ITEM_SUBSCRIBE_ENTER = 301200; // 点击订阅
    int USER_CENTER_ITEM_SUBSCRIBE_LIST_ITEM_CLICK = 301201; // 订阅 - 点击列表
    int USER_CENTER_ITEM_SUBSCRIBE_EXIT = 301202; // 退出订阅
    int USER_CENTER_ITEM_SUBSCRIBE_LIST_ITEM_UNSUBSCRIBE = 301203; // 取消订阅

    // -- 历史播放(301300-301400)
    int USER_CENTER_ITEM_HISTORY_ENTER = 301301; // 进入历史
    int USER_CENTER_ITEM_HISTORY_EXIT = 301302; // 退出历史

    int USER_CENTER_ITEM_HISTORY_MUSIC_ENTER = 301310; // 进入历史音乐
    int USER_CENTER_ITEM_HISTORY_MUSIC_LIST_ITEM_CLICK = 301311; // 历史音乐 - 点击列表
    int USER_CENTER_ITEM_HISTORY_MUSIC_DELETE = 301312; // 历史音乐 - 删除管理
    int USER_CENTER_ITEM_HISTORY_MUSIC_EXIT = 301313; // 历史音乐 - 离开

    int USER_CENTER_ITEM_HISTORY_RADIO_ENTER = 301320; // 进入历史电台
    int USER_CENTER_ITEM_HISTORY_RADIO_LIST_ITEM_CLICK = 301321; // 历史电台 - 点击列表
    int USER_CENTER_ITEM_HISTORY_RADIO_DELETE = 301322; // 历史电台 - 删除管理
    int USER_CENTER_ITEM_HISTORY_RADIO_EXIT = 301323; // 历史电台 - 离开


    // -- 微信推送(301400-301500)
    int WX_PUSH_ENTER = 301400; // 进入微信推送
    int WX_PUSH_DELETE = 301401; // 删除管理
    int WX_PUSH_LIST_ITEM_CLICK = 301402; // 点击音频
    int WX_PUSH_EXIT = 301403; // 退出微信推送

    // -- 设置(301500-301600)
    int SETTINGS_ENTER = 301500; // 进入设置
    int SETTINGS_AI_PLAY = 301501; // 智能播放
    int SETTINGS_FLOAT_WIN = 301502; // 悬浮窗
    int SETTINGS_WAKEUP_CMD = 301503; // 免唤醒指令
    int SETTINGS_CLEAR_CACHE = 301504; // 清除缓存
    int SETTINGS_EXIT = 301505; // 退出设置

    // -- 付费收听榜(302000-303000)
    int PAY_LISTINGS_CLICK = 302000; // 点击榜单
    int PAY_LISTINGS_LIST_ITEM_CLICK = 302001; // 播放榜单内容

    // ------ 主页item点击事项
    // -- 常听音乐标签(303000-304000)
    int OFTEN_LISTEN_MUSIC_CLICK = 303000; // 点击我常听的音乐
    // -- 常听电台节目(304000-305000)
    int OFTEN_LISTEN_RADIO_CLICK = 304000; // 点击我常听的电台节目
    // -- 每日推荐20首(305000-306000)
    int DAILY_RECOMMEND_CLICK = 305000; // 每日推荐20首
    // -- AI电台(306000-307000)
    int AI_RADIO_CLICK = 306000; // AI电台

    // -- 音乐tab报点(400000-500000)
    int MUSIC_RECOMMEND_TAB_SWITCH = 401001; // 点击精选下换一批
    int MUSIC_CATEGORY_TAB_SWITCH = 402001; // 点击分类下换一批

    // ------ 电台精选(500000-501000)
    // -- 必听榜内容
    int NECE_LISTEN_LISTINGS_CLICK = 500002; // 点击必听榜
    int NECE_LISTEN_LISTINGS_LIST_ITEM_CLICK = 500003; // 播放榜单内容
    // -- 电台精选其他推荐位
    int RADIO_RECOMMEND_TAB_ITEM_CLICK = 500004; // 点击推荐位
    // -- 电台分类(502000-503000)
    // -- 亲子
    int RADIO_CATEGORY_PC_ENTER = 502000; // 进入亲子
    int RADIO_CATEGORY_PC_EXIT = 502001; // 退出亲子
    int RADIO_CATEGORY_PC_LIST_ITEM_CLICK = 502002; // 亲子一级界面下点击专辑
    int RADIO_CATEGORY_PC_MORE_CLICK = 503003;  // 亲子点击更多
    int RADIO_CATEGORY_PC_MORE_ITEM_CLICK = 503004; //  二级分类点击
    // -- 有声书
    int RADIO_CATEGORY_AUDIO_BOOK_ENTER = 502100; // 进入有声书
    int RADIO_CATEGORY_AUDIO_BOOK_EXIT = 502101; // 退出有声书
    int RADIO_CATEGORY_AUDIO_BOOK_LIST_ITEM_CLICK = 502102; // 有声书一级界面选择
    int RADIO_CATEGORY_AUDIO_BOOK_MORE_CLICK = 502103; // 有声书点击更多

    // -- 其他分类(502200-502300)
    int RADIO_CATEGORY_OTHER_ENTER = 502200; // 进入分类
    int RADIO_CATEGORY_OTHER_EXIT = 502201; // 退出分类
    int RADIO_CATEGORY_OTHER_LIST_ITEM_CLICK = 502202; // 选择分类中的专辑

    // ------ 播放器报点(200000-300000)
    int PLAYER_ENTER = 200000; // 进入播放页
    int PLAYER_FAVOUR_OR_UNFAVOUR = 200001; // 收藏/取消收藏歌曲
    int PLAYER_SUBSCRIBE_OR_UNSUBSCRIBE = 200002; // 订阅/取消订阅专辑

    int COMM_PLAYER_NEXT = 200003; // 下一首
    int COMM_PLAYER_PREV = 200013; // 上一首

    int PLAYER_SEEK_TO = 200004; // 进度条操作
    int PLAYER_PLAY_LIST_CLICK = 200005; // 查看播放列表
    int PLAYER_PLAY_LIST_PLAY_MODE_CLICK = 200006; // 切换播放模式
    int PLAYER_PLAY_LRC_CLICK = 200007; // 查看歌词
    int PLAYER_AI_MODE_CLICK = 200008; // 切换AI模式
    int PLAYER_AI_MODE_DELETE = 200009; // AI模式下删除歌曲
    int PLAYER_PLAY_START = 200010; // 音频开始播放
    int PLAYER_PLAY_COMPLETION = 200011; // 音频结束播放
    int PLAYER_PLAY_PAUSE = 200012; // 音频暂停播放

    int COMM_FAVOUR_OR_UNFAVOUR = 200014; // 收藏/取消收藏
    int COMM_SUBSCRIBE_OR_UNSUBSCRIBE = 200015; // 订阅/取消订阅

    int PLAYER_PLAY_LIST_ITEM_CLICK = 200016; // 播放列表元素点击
    int PLAYER_PLAY_COMPLETION_ERROR = 200017; // 音频结束播放错误引起的
}
