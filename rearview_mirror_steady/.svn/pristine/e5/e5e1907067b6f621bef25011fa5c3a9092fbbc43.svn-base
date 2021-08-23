package com.txznet.music.util;

import android.text.TextUtils;

import com.txznet.music.Constant;
import com.txznet.music.data.entity.LocalAudio;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.mp3.MP3FileReader;
import org.jaudiotagger.audio.mp4.Mp4FileReader;
import org.jaudiotagger.audio.wav.WavFileReader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * @author zackzhou
 * @date 2019/3/2,16:01
 */

public class MediaMetadataUtils {
    private static final String TAG = Constant.LOG_TAG_UTILS + ":MediaMetadataUtils";

    public static final String SPEC_CHAR = "~$\\/:,;*?|～&";
    public static final String URL_PATTERN = "^(http(s?)://)*[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";

    private MediaMetadataUtils() {
    }

    public static String changeEncode(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        try {
            if (StringCodec.isEncodable(str, "GBK")) {
                return str;
            }
            String str2 = new String(str.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            if (!isGarbled(str2) && !isMessyCode(str2)) {
                return str2;
            }
            str2 = new String(str.getBytes(StandardCharsets.ISO_8859_1), "GBK");
            if (TextUtils.isEmpty(str2) || isMessyCode(str2)) {
                str2 = "";
            }
            return str2;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str;
        }
    }

    public static boolean isGarbled(String str) {
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (SPEC_CHAR.indexOf(str.charAt(i)) == -1) {
                return false;
            }
        }
        return true;
    }

    public static boolean isMessyCode(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        String charSequence = null;
        try {
            charSequence = stringFilter(str);
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(charSequence)) {
            return false;
        }
        char[] toCharArray = charSequence.trim().toCharArray();
        int i = 0;
        int i2 = 0;
        for (char c : toCharArray) {
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
                    i++;
                }
                i2++;
            }
        }
        return i2 != 0 && ((double) (((float) i) / ((float) i2))) > 0.4d;
    }

    private static final Pattern STRING_FILTER_PATTERN_1 = Pattern.compile("[`~!@#$%^&*()+=|{}':;,\\[\\].<>/?！￥…（）—【】‘；：”“’。，、？]");
    private static final Pattern STRING_FILTER_PATTERN_2 = Pattern.compile("\\s*|\t*|\r*|\n*");

    public static String stringFilter(String str) {
        return STRING_FILTER_PATTERN_1.matcher(STRING_FILTER_PATTERN_2.matcher(str).replaceAll("").replaceAll("\\p{P}", "")).replaceAll("");
    }


    private static boolean isChinese(char c) {
        Character.UnicodeBlock of = Character.UnicodeBlock.of(c);
        return of == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || of == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || of == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || of == Character.UnicodeBlock.GENERAL_PUNCTUATION || of == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || of == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    public static boolean isValidSongName(String str) {
        if (TextUtils.isEmpty(str) || Pattern.compile(URL_PATTERN).matcher(str).matches()) {
            return false;
        }
        int lastIndexOf = str.lastIndexOf(46);
        if (lastIndexOf > 0) {
            str = str.substring(0, lastIndexOf);
        }
        String toLowerCase = str.toLowerCase();
        int length = toLowerCase.length();
        for (lastIndexOf = 0; lastIndexOf < length; lastIndexOf++) {
            char charAt = toLowerCase.charAt(lastIndexOf);
            if ((charAt < '0' || charAt > '9') && SPEC_CHAR.indexOf(charAt) == -1) {
                return true;
            }
        }
        return false;
    }

    public static void getMediaMetadata(LocalAudio audio, File file) {
        if (!FileUtils.isExist(file)) {
            return;
        }
        try {
            AudioFile read;
            // mp3,m4a,aac,wav,flac
            if (file.getName().endsWith("mp3")) {
                // FIXME: 2019/5/28 修正jaudiotagger对没有歌曲信息的歌曲解析慢，2秒才抛出异常的问题
                if (checkId3Tag(file)) {
                    read = new MP3FileReader().read(file);
                } else {
                    getSimpleMusic(audio, null);
                    return;
                }
            } else if (file.getName().endsWith("flac")) {
                read = new FlacFileReader().read(file);
            } else if (file.getName().endsWith("wav")) {
                read = new WavFileReader().read(file);
            } else if (file.getName().endsWith("m4a")) {
                read = new Mp4FileReader().read(file);
            } else {
                getSimpleMusic(audio, null);
                return;
            }
            Tag tag = read.getTag();
            if (tag == null) {
                Logger.d(TAG, "tag is null: " + file);
                getSimpleMusic(audio, read);
                return;
            }
            String album = tag.getFirst(FieldKey.ALBUM);
            String artist = tag.getFirst(FieldKey.ARTIST);
            String title = tag.getFirst(FieldKey.TITLE);
            if (TextUtils.isEmpty(album) && TextUtils.isEmpty(artist) && TextUtils.isEmpty(title)) {
                Logger.d(TAG, "tag is empty: " + file);
                getSimpleMusic(audio, read);
                return;
            }
            String fileExtension = null;
            if (!TextUtils.isEmpty(album)) {
                fileExtension = changeEncode(album);
                if (!isGarbled(fileExtension)) {
                    audio.albumName = fileExtension;
                }
            }
            audio.artist = new String[]{Constant.UNKNOWN};
            if (!TextUtils.isEmpty(artist)) {
                fileExtension = changeEncode(artist);
                if (!isGarbled(fileExtension)) {
                    audio.artist = new String[]{fileExtension};
                }
            }
            if (TextUtils.isEmpty(title)) {
                audio.name = null;
            } else {
                audio.name = changeEncode(title);
                if (!(isValidSongName(audio.name) || TextUtils.isEmpty(fileExtension))) {
                    audio.name = null;
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            getSimpleMusic(audio, null);
        }
    }

    private static boolean checkId3Tag(File mp3file) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(mp3file, "r");
            byte[] headerbuf = new byte[3];
            file.read(headerbuf);
            // Parse it quickly
            if (headerbuf[0] == 'I' && headerbuf[1] == 'D' && headerbuf[2] == '3') {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private static void getSimpleMusic(LocalAudio audio, AudioFile audioFile) {
        audio.artist = new String[]{Constant.UNKNOWN};
    }
}
