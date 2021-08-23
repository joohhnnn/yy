package com.txznet.webchat.util;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.webchat.R;
import com.txznet.webchat.log.L;
import com.txznet.webchat.stores.WxConfigStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

/**
 * 文件相关工具类
 * Created by J on 2017/6/13.
 */

public class FileUtil {
    private static final int SIZE_THRESHOLD_MB = 1024 * 1024;

    private static final double SIZE_EXCHANGE_UNIT_MB = 1024 * 1024.0;
    private static final double SIZE_EXCHANGE_UNIT_KB = 1024.0;

    public static final String SIZE_SUFFIX_UI_KB = "KB";
    public static final String SIZE_SUFFIX_UI_MB = "MB";
    public static final String SIZE_SUFFIX_BROAD_KB = "KB";
    public static final String SIZE_SUFFIX_BROAD_MB = "兆";

    public static final int FILE_TYPE_WORD = 1;
    public static final int FILE_TYPE_EXCEL = 2;
    public static final int FILE_TYPE_PPT = 3;
    public static final int FILE_TYPE_PDF = 4;
    public static final int FILE_TYPE_DEFAULT = 0;

    private static DecimalFormat sDecimalFormatter = new DecimalFormat("#.0");

    /**
     * 将Assets目录中的指定文件释放到指定目录
     *
     * @param assetsPath
     * @param dst
     * @return
     */
    public static boolean releaseFileFromAssets(String assetsPath, String dst) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = AppLogic.getInstance().getAssets().open(assetsPath);
            out = new FileOutputStream(dst);
            byte[] buf = new byte[2048];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            in = null;
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 获取用于消息播报的文件大小
     *
     * @param size 原始大小(字节为单位)
     * @return 可直接用于tts的大小描述
     */
    public static String getBroadFileSize(long size) {
        if (size > SIZE_THRESHOLD_MB) {
            return sDecimalFormatter.format(size / SIZE_EXCHANGE_UNIT_MB) + SIZE_SUFFIX_BROAD_MB;
        }

        return sDecimalFormatter.format(size / SIZE_EXCHANGE_UNIT_KB) + SIZE_SUFFIX_BROAD_KB;
    }

    /**
     * 获取用于界面展示的文件大小
     *
     * @param size 原始大小(字节为单位)
     * @return 可直接用于界面展示的大小描述
     */
    public static String getUIFileSize(long size) {
        if (size > SIZE_THRESHOLD_MB) {
            return sDecimalFormatter.format(size / SIZE_EXCHANGE_UNIT_MB) + SIZE_SUFFIX_UI_MB;
        }

        return sDecimalFormatter.format(size / SIZE_EXCHANGE_UNIT_KB) + SIZE_SUFFIX_UI_KB;
    }

    /**
     * 获取用于tts播报的文件名
     *
     * @param fileName 文件名
     * @return 可直接用于tts播报的文件名
     */
    public static String getFileNameForTts(String fileName) {
        return fileName.replaceAll("\\.", "点");
    }

    /**
     * 获取文件后后缀
     *
     * @param fileName 文件名
     * @return 后缀, e.g. hello.pdf会返回pdf
     */
    public static String getFileSuffix(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }

        int pointPosition = fileName.lastIndexOf(".");
        if (-1 == pointPosition || pointPosition == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(pointPosition + 1);
    }

    /**
     * 获取文件名前缀(即去掉了后缀的文件名)
     *
     * @param fileName 文件名
     * @return 前缀, e.g. hello.pdf会返回hello
     */
    public static String getFilePrefix(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }

        int pointPosition = fileName.lastIndexOf(".");
        if (-1 == pointPosition || pointPosition == fileName.length() - 1) {
            return fileName;
        }

        return fileName.substring(0, pointPosition);
    }

    private static int ID_ICON_WORD = R.drawable.ic_file_icon_word;
    private static int ID_ICON_EXCEL = R.drawable.ic_file_icon_excel;
    private static int ID_ICON_PPT = R.drawable.ic_file_icon_ppt;
    private static int ID_ICON_PDF = R.drawable.ic_file_icon_pdf;
    private static int ID_ICON_DEFAULT = R.drawable.ic_file_icon_default;
    private static int ID_ICON_UNSUPPORTED = R.drawable.ic_file_icon_unsupported;

    /**
     * 获取用于展示的文件图标
     *
     * @param fileName 文件名
     * @return 用于界面展示的图标资源id
     */
    public static int getFileIcon(String fileName) {
        if (!WxConfigStore.getInstance().isFileSuffixSupported(getFileSuffix(fileName))) {
            return ID_ICON_UNSUPPORTED;
        }

        int fileType = getFileType(fileName);

        switch (fileType) {
            case FILE_TYPE_WORD:
                return ID_ICON_WORD;

            case FILE_TYPE_PPT:
                return ID_ICON_PPT;

            case FILE_TYPE_EXCEL:
                return ID_ICON_EXCEL;

            case FILE_TYPE_PDF:
                return ID_ICON_PDF;

            default:
                return ID_ICON_DEFAULT;
        }
    }

    /**
     * 采取合适的方式打开文件
     *
     * @param fileUrl 完整的文件路径(包含文件名及后缀)
     */
    public static void openFile(String fileUrl) {
        File file = new File(fileUrl);

        if (!file.exists()) {
            L.e("open file failed: file not exist: " + fileUrl);
            return;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setAction(Intent.ACTION_VIEW);
        //获取文件file的MIME类型
        String type = getMIMEType(fileUrl);
        //设置intent的data和Type属性。
        intent.setDataAndType(Uri.fromFile(file), type);
        //跳转

        try {
            GlobalContext.get().startActivity(intent);
        } catch (Exception e) {
            L.e("open file failed: cannot find activity to open file: " + fileUrl);
        }

    }

    private static String getMIMEType(String fileUrl) {
        int type = getFileType(fileUrl);

        switch (type) {
            case FILE_TYPE_WORD:
                return "application/msword";

            case FILE_TYPE_EXCEL:
                return "application/vnd.ms-excel";

            case FILE_TYPE_PDF:
                return "application/pdf";

            case FILE_TYPE_PPT:
                return "application/vnd.ms-powerpoint";
        }

        return "*/*";
    }

    private static int getFileType(String fileName) {
        String suffix = getFileSuffix(fileName);

        if (TextUtils.isEmpty(suffix)) {
            return FILE_TYPE_DEFAULT;
        } else if ("doc".equalsIgnoreCase(suffix) || "docx".equalsIgnoreCase(suffix)) {
            return FILE_TYPE_WORD;
        } else if ("xls".equalsIgnoreCase(suffix) || "xlsx".equalsIgnoreCase(suffix)) {
            return FILE_TYPE_EXCEL;
        } else if ("ppt".equalsIgnoreCase(suffix) || "pptx".equalsIgnoreCase(suffix)) {
            return FILE_TYPE_PPT;
        } else if ("pdf".equalsIgnoreCase(suffix)) {
            return FILE_TYPE_PDF;
        } else {
            return FILE_TYPE_DEFAULT;
        }
    }

    public static void initFileDir(String fileDir, boolean generateNoMediaFile) {
        File file = new File(fileDir);
        if (!file.exists()) {
            boolean success = file.mkdirs();

            if (!success) {
                L.e("initDir::init app dir failed: " + fileDir);
                return;
            }
        }

        if (generateNoMediaFile) {
            initNoMediaFile(fileDir);
        }
    }

    public static void initNoMediaFile(String fileDir) {
        String noMediaFilePath = fileDir + "/.nomedia";
        File noMediaFile = new File(noMediaFilePath);

        if (!noMediaFile.exists()) {
            boolean success = false;
            try {
                success = noMediaFile.createNewFile();
            } catch (Exception e) {
                L.e("initDir::generate nomedia file encountered error: " + e.toString());
            }

            if (!success) {
                L.e("initDir::generate nomedia file failed");
            }
        }
    }
}
