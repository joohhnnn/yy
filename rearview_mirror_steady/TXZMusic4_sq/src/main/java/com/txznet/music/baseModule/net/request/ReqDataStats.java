//package com.txznet.music.baseModule.net.request;
//
//import java.util.List;
//
///**
// * 做数据统计
// *
// * @author telenewbie
// * @version 创建时间：2016年3月28日 下午8:34:04
// */
//public class ReqDataStats {
//
//    public enum Action {
//        PREVIOUS(1),/*上一首*/
//        NEXT(2),/*下一首*/
//        PLAY(3),/*播放*/
//        PAUSE(4),/*暂停*/
//        PREVIOUS_SOUND(5),/*声控上一首*/
//        NEXT_SOUND(6),/*声控下一首*/
//        PLAY_SOUND(7),/*声控播放*/
//        PAUSE_SOUND(8), /*  声控暂停 */
//        NEXT_AUTO(9), /* 自动切换 */
//
//        FOUND_SOUND(11), /* 触发搜索逻辑 */
//        INDEX_SOUND(12), /* 搜索之后，自动选择 */
////        SHOW_LIST(13), /* 点击列表 */
////        ACT_LOCAL(14), /* 本地音乐 */
////        ACT_HISTORY(15), /* 历史列表 */
////        ACT_ALBUM(16), /* 专辑 */
////        ACT_LOGIN(17), /* 登录 */
////        NEXT_ERROR(18),/*因为出错导致切换下一首*/
////        PREVIOUS_ERROR(19),/*因为出错导致切换上一首*/
//        ACT_PREPARED(20),/*用于上报当前播放的音频，实时，对应接口：/report/abnormal,,非行为上报*/
//        ACT_ERROR_URL(21),/*用于上报当前URL播放异常，实时,对应接口：/report/report，非行为上报*/
////        ACT_EXIT(22),/*退出播放器*/
////        ACT_LOGIN_ERROR(23),/*进入播放器，上次闪退*/
////        PAUSE_EXTERA(24),/*方案商外部调用暂停*/
////        PLAY_EXTERA(25),/*方案商外部调用播放*/
////        NEXT_EXTERA(26),/*方案商外部调用下一首*/
////        PREVIOUS_EXTERA(27),/*方案商外部调用上一首*/
//        ACT_QUICK_RECEIVE(28),/*接收到快报推送*/
//        ACT_QUICK_CLOSE_AUTO(29),/*自动关闭快报*/
//        ACT_QUICK_CONTINUE(30),/*免唤醒触发收听快报*/
//        ACT_QUICK_CONTINUE_WAKEUP(31),/*唤醒触发收听快报*/
//        ACT_QUICK_CLOSE_MANUAL(32),/*手动点击关闭快报*/
//        ACT_QUICK_CLOSE_WAKEUP(33),/*唤醒关闭快报*/
//        ACT_QUICK_CLOSE(34),/*免唤醒关闭快报*/
//        ACT_QUICK_CONTINUE_MANUAL(35),/*手动触发收听快报*/
//
////        PLAY_AUTO(36); /* 真正开始播放 ，先暂时删除*/
////        ACT_SEEK_START(37),/* 手动触发拖动事件*/
////        ACT_SEEK_COMPLETE(38),/* 拖动事件完成，可以播放*/
//        ACT_ClICK_OTHER(40),/*手动触发收听快报*/
//        ;
//        private int id;
//
//        Action(int id) {
//            this.id = id;
//        }
//
//        public int getId() {
//            return id;
//        }
//
//    }
//
//    private List<ReportInfo> infos;
//
//    private ReportInfo info;
//    private long time;
//    private int actionName;// 上/下/播放/暂停/声控/自动切换/结束
//
//    public static class ReportInfo {
//        private long id;// 音频ID
//        private int sid;// 来源ID
//        private long duration;// 时长
//        private float currentPercent;// 当前播放进度
//        private String artists;// 艺术家
//        private String title;// 音频名称
//
//        // public ReportInfo(long id, int sid, String atrits, String title) {
//        // super();
//        // this.id = id;
//        // this.sid = sid;
//        // this.atrits = atrits;
//        // this.title = title;
//        // }
//
//        public ReportInfo(long id, int sid, long duration, float currentPercent) {
//            super();
//            this.id = id;
//            this.sid = sid;
//            this.duration = duration;
//            this.currentPercent = currentPercent;
//        }
//
//        public void setAtrits(String atrits) {
//            this.artists = atrits;
//        }
//
//        public ReportInfo(long id, int sid, long duration, float currentPercent, String artists, String title) {
//            super();
//            this.id = id;
//            this.sid = sid;
//            this.duration = duration;
//            this.currentPercent = currentPercent;
//            this.artists = artists;
//            this.title = title;
//        }
//
//        public void setTitle(String title) {
//            this.title = title;
//        }
//
//        @Override
//        public String toString() {
//            return "ReportInfo{" +
//                    "id=" + id +
//                    ", sid=" + sid +
//                    ", duration=" + duration +
//                    ", currentPercent=" + currentPercent +
//                    ", artists='" + artists + '\'' +
//                    ", title='" + title + '\'' +
//                    '}';
//        }
//    }
//
//    public ReqDataStats(List<ReportInfo> infos, long time, Action actionName) {
//        super();
//        this.infos = infos;
//        this.time = time;
//        this.actionName = actionName.ordinal();
//    }
//
//    /**
//     * 行为操作上报BEAN
//     *
//     * @param info
//     * @param time
//     * @param actionName
//     */
//    public ReqDataStats(ReportInfo info, long time, Action actionName) {
//        super();
//        this.info = info;
//        this.time = time;
//        this.actionName = actionName.getId();
//    }
//
//    // /**
//    // *
//    // * @param time
//    // * @param actionName
//    // * @param id
//    // * @param sid
//    // */
//    // public ReqDataStats(long time, Action actionName, long id, int sid) {
//    // super();
//    // this.time = time;
//    // this.actionName = actionName;
//    // infos = new ArrayList<ReqDataStats.ReportInfo>();
//    // infos.add(new ReportInfo(id, sid));
//    // }
//
//
//    @Override
//    public String toString() {
//        return "ReqDataStats{" +
//                "infos=" + infos +
//                ", info=" + info +
//                ", time=" + time +
//                ", actionName=" + actionName +
//                '}';
//    }
//}
