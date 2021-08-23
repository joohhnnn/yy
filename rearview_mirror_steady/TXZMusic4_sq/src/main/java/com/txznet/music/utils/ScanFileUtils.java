//package com.txznet.music.utils;
//
//import android.media.MediaExtractor;
//
//import com.txznet.comm.remote.util.LogUtil;
//
//import java.io.File;
//import java.io.FilenameFilter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Pattern;
//
///**
// * @author telenewbie
// * @version 创建时间：2016年3月4日 下午7:09:16
// */
//public class ScanFileUtils {
//
//    // 合法的后缀名称
//    private static String[] postfixs = new String[]{".aac", ".m4a", ".tmd",
//            ".mp3"};
//
//    private static boolean isValid(String postfix) {
//        for (int i = 0; i < postfixs.length; i++) {
//            if (postfix.endsWith(postfixs[i])) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    private static boolean scanMp4Files(String url) {
//        MediaExtractor extractor = new MediaExtractor();
//        try {
//
//            extractor.setDataSource(url);
//            int numTracks = extractor.getTrackCount();
//            if (numTracks == 1) {
//                return true;
//            }
//            return false;
//        } catch (Exception e) {
//        } finally {
//            extractor.release();
//            extractor = null;
//        }
//        return false;
//    }
//
//    // 获取所有文件
//    public static List<File> getFiles(String filePath) {
//        dirFile.clear();
//        scan(filePath);
//        return dirFile;
//    }
//
//    static List<File> dirFile = new ArrayList<File>();
//
//    // private static long valueableLength = 500 * 1024;
//
//    public static class MyFileNameFilter implements FilenameFilter {
//
//        @Override
//        public boolean accept(File dir, String filename) {
//            //使用正则表达式
//            if (filename.contains(".")) {// 有后缀名
//                return Pattern.matches(".*\\.(tmd|aac|m4a|mp3|nomedia)", filename);
//            }
//            return true;
//        }
//    }
//
//    private static MyFileNameFilter filenameFilter = new MyFileNameFilter();
//
//    /**
//     * 只支持aac，MP3，m4a,tmd(txz media data)这几种格式
//     *
//     * @param path
//     */
//    private static void scan(String path) {
//        File file = new File(path);
//        File[] listFiles = file.listFiles(filenameFilter);
//        if (ArrayUtils.isEmpty(listFiles) || ".nomedia".equals(listFiles[0].getName())) {
//            LogUtil.logd("music:local:file:can't scan:" + path + ",maybe empty or contain \".nomedia\"");
//            return;
//        }
//        int length = listFiles.length;
//        for (int i = length - 1; i >= 0; i--) {
//            File childFile = listFiles[i];
//            // LogUtil.logd("File:" + childFile.getName());
//            if (childFile.exists()) {
//                if (childFile.isDirectory()) {
//                    scan(childFile.getAbsolutePath());
//                } else {
//                    // String name = childFile.getAbsolutePath();
//                    if (childFile.getName().contains(".")) {
//                        if (childFile.length() > SharedPreferencesUtils.getSearchSize()) {
//                            // if (scanMp4Files(name)) {
//                            dirFile.add(childFile);
//                            // }
//                        }
//                    }
//                }
//            }
//            listFiles[i] = null;
//        }
//    }
//
//}
