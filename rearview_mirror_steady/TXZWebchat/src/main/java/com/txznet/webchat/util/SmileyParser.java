package com.txznet.webchat.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;

import com.txznet.webchat.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ASUS User on 2015/8/10.
 */
public class SmileyParser {
    private static final String[][] TRANSLATE_TEXTS = new String[][]{
            {
                    "Smile", "Grimace", "Drool", "Scowl", "Chill", "Sob", "Shy",
                    "Shutup", "Sleep", "Cry", "Awkward", "Pout", "Wink", "Grin",
                    "Surprised", "Frown", "Cool", "Tension", "Scream", "Puke", "Chuckle",
                    "Joyful", "Slight", "Smug", "Hungry", "Drowsy", "Panic", "Sweat",
                    "Laugh", "Loafer", "Strive", "Scold", "Doubt", "Shhh", "Dizzy",
                    "Crazy", "BadLuck", "Skull", "Hammer", "Wave", "Relief", "DigNose",
                    "Clap", "Shame", "Trick", "Bah！L", "Bah！R", "Yawn", "Lookdown",
                    "Wronged", "Puling", "Sly", "Kiss", "Uh-oh", "Whimper", "Cleaver",
                    "Melon", "Beer", "Basketball", "PingPong", "Coffee", "Rice", "Pig",
                    "Rose", "Wilt", "Lip", "Heart", "BrokenHeart", "Cake", "Lightning",
                    "Bomb", "Dagger", "Soccer", "Ladybug", "Poop", "Moon", "Sun",
                    "Gift", "Hug", "Strong", "Weak", "Shake", "Victory", "Admire",
                    "Beckon", "Fist", "Pinky", "Love", "No", "OK", "InLove",
                    "Blowkiss", "Waddle", "Tremble", "Aaagh!", "Twirl", "Kotow", "Lookback",
                    "Jump", "Give-in", "Hooray", "HeyHey", "Smooch", "TaiJi L", "TaiJi R",
                    "囧", "Hey", "Facepalm", "Smirk", "Smart", "Concerned", "Yeah!",
                    "Packet", "Chick"
            },
            {
                    "微笑", "撇嘴", "色", "发呆", "得意", "流泪", "害羞",
                    "闭嘴", "睡", "大哭", "尴尬", "发怒", "调皮", "呲牙",
                    "惊讶", "难过", "酷", "冷汗", "抓狂", "吐", "偷笑",
                    "愉快", "白眼", "傲慢", "饥饿", "困", "惊恐", "流汗",
                    "憨笑", "悠闲", "奋斗", "咒骂", "疑问", "嘘", "晕",
                    "疯了", "衰", "骷髅", "敲打", "再见", "擦汗", "抠鼻",
                    "鼓掌", "糗大了", "坏笑", "左哼哼", "右哼哼", "哈欠", "鄙视",
                    "委屈", "快哭了", "阴险", "亲亲", "吓", "可怜", "菜刀",
                    "西瓜", "啤酒", "篮球", "乒乓", "咖啡", "饭", "猪头",
                    "玫瑰", "凋谢", "嘴唇", "爱心", "心碎", "蛋糕", "闪电",
                    "炸弹", "刀", "足球", "瓢虫", "便便", "月亮", "太阳",
                    "礼物", "拥抱", "强", "弱", "握手", "胜利", "抱拳",
                    "勾引", "拳头", "差劲", "爱你", "NO", "OK", "爱情",
                    "飞吻", "跳跳", "发抖", "怄火", "转圈", "磕头", "回头",
                    "跳绳", "投降", "激动", "乱舞", "献吻", "左太极", "右太极",
                    "囧", "嘿哈", "捂脸", "奸笑", "机智", "皱眉", "耶",
                    "红包", "鸡"
            }
    };
    private static final String[][] EMOTION_TEXTS = new String[][]{
            {
                    "[微笑]", "[撇嘴]", "[色]", "[发呆]", "[得意]", "[流泪]", "[害羞]",
                    "[闭嘴]", "[睡]", "[大哭]", "[尴尬]", "[发怒]", "[调皮]", "[呲牙]",
                    "[惊讶]", "[难过]", "[酷]", "[冷汗]", "[抓狂]", "[吐]", "[偷笑]",
                    "[愉快]", "[白眼]", "[傲慢]", "[饥饿]", "[困]", "[惊恐]", "[流汗]",
                    "[憨笑]", "[悠闲]", "[奋斗]", "[咒骂]", "[疑问]", "[嘘]", "[晕]",
                    "[疯了]", "[衰]", "[骷髅]", "[敲打]", "[再见]", "[擦汗]", "[抠鼻]",
                    "[鼓掌]", "[糗大了]", "[坏笑]", "[左哼哼]", "[右哼哼]", "[哈欠]", "[鄙视]",
                    "[委屈]", "[快哭了]", "[阴险]", "[亲亲]", "[吓]", "[可怜]", "[菜刀]",
                    "[西瓜]", "[啤酒]", "[篮球]", "[乒乓]", "[咖啡]", "[饭]", "[猪头]",
                    "[玫瑰]", "[凋谢]", "[嘴唇]", "[爱心]", "[心碎]", "[蛋糕]", "[闪电]",
                    "[炸弹]", "[刀]", "[足球]", "[瓢虫]", "[便便]", "[月亮]", "[太阳]",
                    "[礼物]", "[拥抱]", "[强]", "[弱]", "[握手]", "[胜利]", "[抱拳]",
                    "[勾引]", "[拳头]", "[差劲]", "[爱你]", "[NO]", "[OK]", "[爱情]",
                    "[飞吻]", "[跳跳]", "[发抖]", "[怄火]", "[转圈]", "[磕头]", "[回头]",
                    "[跳绳]", "[投降]", "[激动]", "[乱舞]", "[献吻]", "[左太极]", "[右太极]",
                    "[囧]", "[嘿哈]", "[捂脸]", "[奸笑]", "[机智]", "[皱眉]", "[耶]",
                    "[红包]", "[鸡]"
            },
            {
                    "[Smile]", "[Grimace]", "[Drool]", "[Scowl]", "[Chill]", "[Sob]", "[Shy]",
                    "[Shutup]", "[Sleep]", "[Cry]", "[Awkward]", "[Pout]", "[Wink]", "[Grin]",
                    "[Surprised]", "[Frown]", "[Cool]", "[Tension]", "[Scream]", "[Puke]", "[Chuckle]",
                    "[Joyful]", "[Slight]", "[Smug]", "[Hungry]", "[Drowsy]", "[Panic]", "[Sweat]",
                    "[Laugh]", "[Loafer]", "[Strive]", "[Scold]", "[Doubt]", "[Shhh]", "[Dizzy]",
                    "[Crazy]", "[BadLuck]", "[Skull]", "[Hammer]", "[Wave]", "[Relief]", "[DigNose]",
                    "[Clap]", "[Shame]", "[Trick]", "[Bah！L]", "[Bah！R]", "[Yawn]", "[Lookdown]",
                    "[Wronged]", "[Puling]", "[Sly]", "[Kiss]", "[Uh-oh]", "[Whimper]", "[Cleaver]",
                    "[Melon]", "[Beer]", "[Basketball]", "[PingPong]", "[Coffee]", "[Rice]", "[Pig]",
                    "[Rose]", "[Wilt]", "[Lip]", "[Heart]", "[BrokenHeart]", "[Cake]", "[Lightning]",
                    "[Bomb]", "[Dagger]", "[Soccer]", "[Ladybug]", "[Poop]", "[Moon]", "[Sun]",
                    "[Gift]", "[Hug]", "[Strong]", "[Weak]", "[Shake]", "[Victory]", "[Admire]",
                    "[Beckon]", "[Fist]", "[Pinky]", "[Love]", "[No]", "[OK]", "[InLove]",
                    "[Blowkiss]", "[Waddle]", "[Tremble]", "[Aaagh!]", "[Twirl]", "[Kotow]", "[Lookback]",
                    "[Jump]", "[Give-in]", "[Hooray]", "[HeyHey]", "[Smooch]", "[TaiJi L]", "[TaiJi R]",
                    "[囧]", "[Hey]", "[Facepalm]", "[Smirk]", "[Smart]", "[Concerned]", "[Yeah!]",
                    "[Packet]", "[Chick]"
            },
            {
                    "[emoji1f604]", "[emoji1f60a]", "[emoji1f63a]", "[emoji263a]", "[emoji1f609]", "[emoji1f63b]", "[emoji1f63d]",
                    "[emoji1f61a]", "[emoji1f633]", "[emoji1f63c]", "[emoji1f60c]", "[emoji1f61c]", "[emoji1f61d]", "[emoji1f612]",
                    "[emoji1f60f]", "[emoji1f613]", "[emoji1f64d]", "[emoji1f61e]", "[emoji1f4ab]", "[emoji1f625]", "[emoji1f630]",
                    "[emoji1f628]", "[emoji1f62b]", "[emoji1f63f]", "[emoji1f62d]", "[emoji1f639]", "[emoji1f632]", "[emoji1f631]",
                    "[emoji1f620]", "[emoji1f64e]", "[emoji1f62a]", "[emoji1f637]", "[emoji1f47f]", "[emoji1f47d]", "[emoji2764]",
                    "[emoji1f494]", "[emoji1f498]", "[emoji2747]", "[emoji1f31f]", "[emoji2755]", "[emoji2754]", "[emoji1f4a4]",
                    "[emoji1f4a7]", "[emoji1f3b5]", "[emoji1f525]", "[emoji1f4a9]", "[emoji1f44d]", "[emoji1f44e]", "[emoji1f44a]",
                    "[emoji270c]", "[emoji1f446]", "[emoji1f447]", "[emoji1f449]", "[emoji1f448]", "[emoji261d]", "[emoji1f4aa]",
                    "[emoji1f48f]", "[emoji1f491]", "[emoji1f466]", "[emoji1f467]", "[emoji1f469]", "[emoji1f468]", "[emoji1f47c]",
                    "[emoji1f480]", "[emoji1f48b]", "[emoji2600]", "[emoji2614]", "[emoji2601]", "[emoji26c4]", "[emoji1f31b]",
                    "[emoji26a1]", "[emoji1f30a]", "[emoji1f431]", "[emoji1f436]", "[emoji1f42d]", "[emoji1f439]", "[emoji1f430]",
                    "[emoji1f43a]", "[emoji1f438]", "[emoji1f42f]", "[emoji1f428]", "[emoji1f43b]", "[emoji1f43d]", "[emoji1f42e]",
                    "[emoji1f417]", "[emoji1f435]", "[emoji1f434]", "[emoji1f40d]", "[emoji1f426]", "[emoji1f414]", "[emoji1f427]",
                    "[emoji1f41b]", "[emoji1f419]", "[emoji1f420]", "[emoji1f433]", "[emoji1f42c]", "[emoji1f339]", "[emoji1f33a]",
                    "[emoji1f334]", "[emoji1f335]", "[emoji1f49d]", "[emoji1f383]", "[emoji1f47b]", "[emoji1f385]", "[emoji1f384]",
                    "[emoji1f4e6]", "[emoji1f514]", "[emoji1f389]", "[emoji1f388]", "[emoji1f4bf]", "[emoji1f4f7]", "[emoji1f4f9]",
                    "[emoji1f4bb]", "[emoji1f4fa]", "[emoji1f4de]", "[emoji1f513]", "[emoji1f510]", "[emoji1f511]", "[emoji1f528]",
                    "[emoji1f4a1]", "[emoji1f4eb]", "[emoji1f6c0]", "[emoji1f4b5]", "[emoji1f4a3]", "[emoji1f52b]", "[emoji1f48a]",
                    "[emoji1f3c8]", "[emoji1f3c0]", "[emoji26bd]", "[emoji26be]", "[emoji26f3]", "[emoji1f3c6]", "[emoji1f47e]",
                    "[emoji1f3a4]", "[emoji1f3b8]", "[emoji1f459]", "[emoji1f451]", "[emoji1f302]", "[emoji1f45c]", "[emoji1f484]",
                    "[emoji1f48d]", "[emoji1f48e]", "[emoji2615]", "[emoji1f37a]", "[emoji1f37b]", "[emoji1f379]", "[emoji1f354]",
                    "[emoji1f35f]", "[emoji1f35d]", "[emoji1f363]", "[emoji1f35c]", "[emoji1f373]", "[emoji1f366]", "[emoji1f382]",
                    "[emoji1f34f]", "[emoji2708]", "[emoji1f680]", "[emoji1f6b2]", "[emoji1f684]", "[emoji26a0]", "[emoji1f3c1]",
                    "[emoji1f6b9]", "[emoji1f6ba]", "[emoji2b55]", "[emoji2716]", "[emojia9]", "[emojiae]", "[emoji2122]",
                    "[emoji1f64f]"
            }
    };
    private static final int[][] EMOTION_RESOURCES = new int[][]{
            {
                    R.drawable.smiley_01, R.drawable.smiley_02, R.drawable.smiley_03, R.drawable.smiley_04, R.drawable.smiley_05, R.drawable.smiley_06, R.drawable.smiley_07,
                    R.drawable.smiley_08, R.drawable.smiley_09, R.drawable.smiley_10, R.drawable.smiley_11, R.drawable.smiley_12, R.drawable.smiley_13, R.drawable.smiley_14,
                    R.drawable.smiley_15, R.drawable.smiley_16, R.drawable.smiley_17, R.drawable.smiley_18, R.drawable.smiley_19, R.drawable.smiley_20, R.drawable.smiley_21,
                    R.drawable.smiley_22, R.drawable.smiley_23, R.drawable.smiley_24, R.drawable.smiley_25, R.drawable.smiley_26, R.drawable.smiley_27, R.drawable.smiley_28,
                    R.drawable.smiley_29, R.drawable.smiley_30, R.drawable.smiley_31, R.drawable.smiley_32, R.drawable.smiley_33, R.drawable.smiley_34, R.drawable.smiley_35,
                    R.drawable.smiley_36, R.drawable.smiley_37, R.drawable.smiley_38, R.drawable.smiley_39, R.drawable.smiley_40, R.drawable.smiley_41, R.drawable.smiley_42,
                    R.drawable.smiley_43, R.drawable.smiley_44, R.drawable.smiley_45, R.drawable.smiley_46, R.drawable.smiley_47, R.drawable.smiley_48, R.drawable.smiley_49,
                    R.drawable.smiley_50, R.drawable.smiley_51, R.drawable.smiley_52, R.drawable.smiley_53, R.drawable.smiley_54, R.drawable.smiley_55, R.drawable.smiley_56,
                    R.drawable.smiley_57, R.drawable.smiley_58, R.drawable.smiley_59, R.drawable.smiley_60, R.drawable.smiley_61, R.drawable.smiley_62, R.drawable.smiley_63,
                    R.drawable.smiley_64, R.drawable.smiley_65, R.drawable.smiley_66, R.drawable.smiley_67, R.drawable.smiley_68, R.drawable.smiley_69, R.drawable.smiley_70,
                    R.drawable.smiley_71, R.drawable.smiley_72, R.drawable.smiley_73, R.drawable.smiley_74, R.drawable.smiley_75, R.drawable.smiley_76, R.drawable.smiley_77,
                    R.drawable.smiley_78, R.drawable.smiley_79, R.drawable.smiley_80, R.drawable.smiley_81, R.drawable.smiley_82, R.drawable.smiley_83, R.drawable.smiley_84,
                    R.drawable.smiley_85, R.drawable.smiley_86, R.drawable.smiley_87, R.drawable.smiley_88, R.drawable.smiley_89, R.drawable.smiley_90, R.drawable.smiley_91,
                    R.drawable.smiley_92, R.drawable.smiley_93, R.drawable.smiley_94, R.drawable.smiley_95, R.drawable.smiley_96, R.drawable.smiley_97, R.drawable.smiley_98,
                    R.drawable.smiley_99, R.drawable.smiley_100, R.drawable.smiley_101, R.drawable.smiley_102, R.drawable.smiley_103, R.drawable.smiley_104, R.drawable.smiley_105,
                    R.drawable.smiley_106, R.drawable.smiley_107, R.drawable.smiley_108, R.drawable.smiley_109, R.drawable.smiley_110, R.drawable.smiley_111, R.drawable.smiley_112,
                    R.drawable.smiley_113, R.drawable.smiley_114
            },
            {
                    R.drawable.smiley_01, R.drawable.smiley_02, R.drawable.smiley_03, R.drawable.smiley_04, R.drawable.smiley_05, R.drawable.smiley_06, R.drawable.smiley_07,
                    R.drawable.smiley_08, R.drawable.smiley_09, R.drawable.smiley_10, R.drawable.smiley_11, R.drawable.smiley_12, R.drawable.smiley_13, R.drawable.smiley_14,
                    R.drawable.smiley_15, R.drawable.smiley_16, R.drawable.smiley_17, R.drawable.smiley_18, R.drawable.smiley_19, R.drawable.smiley_20, R.drawable.smiley_21,
                    R.drawable.smiley_22, R.drawable.smiley_23, R.drawable.smiley_24, R.drawable.smiley_25, R.drawable.smiley_26, R.drawable.smiley_27, R.drawable.smiley_28,
                    R.drawable.smiley_29, R.drawable.smiley_30, R.drawable.smiley_31, R.drawable.smiley_32, R.drawable.smiley_33, R.drawable.smiley_34, R.drawable.smiley_35,
                    R.drawable.smiley_36, R.drawable.smiley_37, R.drawable.smiley_38, R.drawable.smiley_39, R.drawable.smiley_40, R.drawable.smiley_41, R.drawable.smiley_42,
                    R.drawable.smiley_43, R.drawable.smiley_44, R.drawable.smiley_45, R.drawable.smiley_46, R.drawable.smiley_47, R.drawable.smiley_48, R.drawable.smiley_49,
                    R.drawable.smiley_50, R.drawable.smiley_51, R.drawable.smiley_52, R.drawable.smiley_53, R.drawable.smiley_54, R.drawable.smiley_55, R.drawable.smiley_56,
                    R.drawable.smiley_57, R.drawable.smiley_58, R.drawable.smiley_59, R.drawable.smiley_60, R.drawable.smiley_61, R.drawable.smiley_62, R.drawable.smiley_63,
                    R.drawable.smiley_64, R.drawable.smiley_65, R.drawable.smiley_66, R.drawable.smiley_67, R.drawable.smiley_68, R.drawable.smiley_69, R.drawable.smiley_70,
                    R.drawable.smiley_71, R.drawable.smiley_72, R.drawable.smiley_73, R.drawable.smiley_74, R.drawable.smiley_75, R.drawable.smiley_76, R.drawable.smiley_77,
                    R.drawable.smiley_78, R.drawable.smiley_79, R.drawable.smiley_80, R.drawable.smiley_81, R.drawable.smiley_82, R.drawable.smiley_83, R.drawable.smiley_84,
                    R.drawable.smiley_85, R.drawable.smiley_86, R.drawable.smiley_87, R.drawable.smiley_88, R.drawable.smiley_89, R.drawable.smiley_90, R.drawable.smiley_91,
                    R.drawable.smiley_92, R.drawable.smiley_93, R.drawable.smiley_94, R.drawable.smiley_95, R.drawable.smiley_96, R.drawable.smiley_97, R.drawable.smiley_98,
                    R.drawable.smiley_99, R.drawable.smiley_100, R.drawable.smiley_101, R.drawable.smiley_102, R.drawable.smiley_103, R.drawable.smiley_104, R.drawable.smiley_105,
                    R.drawable.smiley_106, R.drawable.smiley_107, R.drawable.smiley_108, R.drawable.smiley_109, R.drawable.smiley_110, R.drawable.smiley_111, R.drawable.smiley_112,
                    R.drawable.smiley_113, R.drawable.smiley_114
            },
            {
                    R.drawable.emoji_01, R.drawable.emoji_02, R.drawable.emoji_03, R.drawable.emoji_04, R.drawable.emoji_05, R.drawable.emoji_06, R.drawable.emoji_07,
                    R.drawable.emoji_08, R.drawable.emoji_09, R.drawable.emoji_10, R.drawable.emoji_11, R.drawable.emoji_12, R.drawable.emoji_13, R.drawable.emoji_14,
                    R.drawable.emoji_15, R.drawable.emoji_16, R.drawable.emoji_17, R.drawable.emoji_18, R.drawable.emoji_19, R.drawable.emoji_20, R.drawable.emoji_21,
                    R.drawable.emoji_22, R.drawable.emoji_23, R.drawable.emoji_24, R.drawable.emoji_25, R.drawable.emoji_26, R.drawable.emoji_27, R.drawable.emoji_28,
                    R.drawable.emoji_29, R.drawable.emoji_30, R.drawable.emoji_31, R.drawable.emoji_32, R.drawable.emoji_33, R.drawable.emoji_34, R.drawable.emoji_35,
                    R.drawable.emoji_36, R.drawable.emoji_37, R.drawable.emoji_38, R.drawable.emoji_39, R.drawable.emoji_40, R.drawable.emoji_41, R.drawable.emoji_42,
                    R.drawable.emoji_43, R.drawable.emoji_44, R.drawable.emoji_45, R.drawable.emoji_46, R.drawable.emoji_47, R.drawable.emoji_48, R.drawable.emoji_49,
                    R.drawable.emoji_50, R.drawable.emoji_51, R.drawable.emoji_52, R.drawable.emoji_53, R.drawable.emoji_54, R.drawable.emoji_55, R.drawable.emoji_56,
                    R.drawable.emoji_57, R.drawable.emoji_58, R.drawable.emoji_59, R.drawable.emoji_60, R.drawable.emoji_61, R.drawable.emoji_62, R.drawable.emoji_63,
                    R.drawable.emoji_64, R.drawable.emoji_65, R.drawable.emoji_66, R.drawable.emoji_67, R.drawable.emoji_68, R.drawable.emoji_69, R.drawable.emoji_70,
                    R.drawable.emoji_71, R.drawable.emoji_72, R.drawable.emoji_73, R.drawable.emoji_74, R.drawable.emoji_75, R.drawable.emoji_76, R.drawable.emoji_77,
                    R.drawable.emoji_78, R.drawable.emoji_79, R.drawable.emoji_80, R.drawable.emoji_81, R.drawable.emoji_82, R.drawable.emoji_83, R.drawable.emoji_84,
                    R.drawable.emoji_85, R.drawable.emoji_86, R.drawable.emoji_87, R.drawable.emoji_88, R.drawable.emoji_89, R.drawable.emoji_90, R.drawable.emoji_91,
                    R.drawable.emoji_92, R.drawable.emoji_93, R.drawable.emoji_94, R.drawable.emoji_95, R.drawable.emoji_96, R.drawable.emoji_97, R.drawable.emoji_98,
                    R.drawable.emoji_99, R.drawable.emoji_100, R.drawable.emoji_101, R.drawable.emoji_102, R.drawable.emoji_103, R.drawable.emoji_104, R.drawable.emoji_105,
                    R.drawable.emoji_106, R.drawable.emoji_107, R.drawable.emoji_108, R.drawable.emoji_109, R.drawable.emoji_110, R.drawable.emoji_111, R.drawable.emoji_112,
                    R.drawable.emoji_113, R.drawable.emoji_114, R.drawable.emoji_115, R.drawable.emoji_116, R.drawable.emoji_117, R.drawable.emoji_118, R.drawable.emoji_119,
                    R.drawable.emoji_120, R.drawable.emoji_121, R.drawable.emoji_122, R.drawable.emoji_123, R.drawable.emoji_124, R.drawable.emoji_125, R.drawable.emoji_126,
                    R.drawable.emoji_127, R.drawable.emoji_128, R.drawable.emoji_129, R.drawable.emoji_130, R.drawable.emoji_131, R.drawable.emoji_132, R.drawable.emoji_133,
                    R.drawable.emoji_134, R.drawable.emoji_135, R.drawable.emoji_136, R.drawable.emoji_137, R.drawable.emoji_138, R.drawable.emoji_139, R.drawable.emoji_140,
                    R.drawable.emoji_141, R.drawable.emoji_142, R.drawable.emoji_143, R.drawable.emoji_144, R.drawable.emoji_145, R.drawable.emoji_146, R.drawable.emoji_147,
                    R.drawable.emoji_148, R.drawable.emoji_149, R.drawable.emoji_150, R.drawable.emoji_151, R.drawable.emoji_152, R.drawable.emoji_153, R.drawable.emoji_154,
                    R.drawable.emoji_155, R.drawable.emoji_156, R.drawable.emoji_157, R.drawable.emoji_158, R.drawable.emoji_159, R.drawable.emoji_160, R.drawable.emoji_161,
                    R.drawable.emoji_162, R.drawable.emoji_163, R.drawable.emoji_164, R.drawable.emoji_165, R.drawable.emoji_166, R.drawable.emoji_167, R.drawable.emoji_168,
                    R.drawable.emoji_169

            }
    };
    private static Map<String, Integer> mEmotionResMap = new HashMap<String, Integer>(EMOTION_TEXTS[0].length * 2);
    private static Map<String, String> mEmotionTranslateMap = new HashMap<>(TRANSLATE_TEXTS[0].length);

    static {
        for (int i = 0; i < EMOTION_TEXTS.length; i++) {
            for (int j = 0; j < EMOTION_TEXTS[i].length; j++) {
                mEmotionResMap.put(EMOTION_TEXTS[i][j], EMOTION_RESOURCES[i][j]);
            }
        }

        for (int i = 0; i < TRANSLATE_TEXTS[0].length; i++) {
            mEmotionTranslateMap.put(TRANSLATE_TEXTS[0][i], TRANSLATE_TEXTS[1][i]);
        }
    }

    private static SmileyParser sInstance;

    private Context mContext;
    private Pattern mPattern; // 白名单的匹配

    private SmileyParser(Context context) {
        mContext = context;
        mPattern = buildPattern();
    }

    public static SmileyParser getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SmileyParser.class) {
                if (sInstance == null) {
                    sInstance = new SmileyParser(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private Pattern buildPattern() {
        StringBuilder patternString = new StringBuilder();
        patternString.append('(');
        for (int i = 0; i < EMOTION_TEXTS.length; i++) {
            for (int j = 0; j < EMOTION_TEXTS[i].length; j++) {
                patternString.append(Pattern.quote(EMOTION_TEXTS[i][j]));
                patternString.append('|');
            }
        }
        patternString.replace(patternString.length() - 1, patternString.length(), ")");
        return Pattern.compile(patternString.toString());
    }

    public CharSequence parser(String text, final View.OnClickListener listener) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }

        // 将未被转为span的emoji先转换
        //text = filterEmoji(text);

        // 先把不能处理的过滤掉
        List<Integer> starts = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();
        Matcher emojiMatcher = Pattern.compile("\\[emoji\\w+\\]").matcher(text);
        while (emojiMatcher.find()) {
            if (mEmotionResMap.containsKey(emojiMatcher.group())) {
                continue;
            }
            starts.add(emojiMatcher.start());
            ends.add(emojiMatcher.end());
        }
        StringBuilder sb = new StringBuilder(text);
        int length = 0;
        for (int i = 0; i < starts.size(); i++) {
            int start = starts.get(i);
            int end = ends.get(i);
            sb.replace(start - length, end - length, "");
            length += end - start;
        }

        SpannableStringBuilder builder = new SpannableStringBuilder(sb.toString());
        Matcher matcher = mPattern.matcher(sb.toString());
        while (matcher.find()) {
            int resId = mEmotionResMap.get(matcher.group());
            if (resId != -1) {
                builder.setSpan(new MyImageSpan(mContext, resId, MyImageSpan.ALIGN_FONTCENTER), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        if (listener != null) {
            builder.setSpan(new NoLinkClickableSpan() {
                @Override
                public void onClick(View widget) {
                    listener.onClick(widget);
                }
            }, 0, builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    public CharSequence parser(String text) {
        return parser(text, null);
    }

    /**
     * 获取可以用来播报的表情名称, 用来应付英文表情
     *
     * @param emotion
     * @return
     */
    public static String getEmotionTts(String emotion) {
        String result = mEmotionTranslateMap.get(emotion);

        return TextUtils.isEmpty(result) ? emotion : result;
    }

    // 是否存在可以匹配替换的字符段
    public boolean hasMatch(String text) {
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            return true;
        }
        return false;
    }

    public static String parseEmoji(String text) {
        return text.replaceAll("<span class=\"emoji ", "[").replaceAll("\"></span(>)?", "]").replaceAll("<br/>", "");
    }

    public static String removeEmoji(String text) {
        return text.replaceAll("\\[emoji\\w+\\]", "");
    }





    // -----------------------------------------------------------------------------------------





    public static boolean containsEmoji(String source) {
        if (TextUtils.isEmpty(source)) {
            return false;
        }

        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (isEmojiCharacter(codePoint)) {
                //do nothing，判断到了这里表明，确认有表情字符
                return true;
            }
        }
        return false;
    }

    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) ||
                (codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
    }

    /**
     * 过滤emoji 或者 其他非文字类型的字符
     *
     * @param source
     * @return
     */
    public static String filterEmoji(String source) {
        source = source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "[emoji]");
        if (!containsEmoji(source)) {
            return source;//如果不包含，直接返回
        }
        //到这里铁定包含
        StringBuilder buf = null;

        int len = source.length();

        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);

            if (isEmojiCharacter(codePoint)) {
                if (buf == null) {
                    buf = new StringBuilder(source.length());
                }

                buf.append(codePoint);
            } else {
                buf.append("*");
            }
        }

        if (buf == null) {
            return source;//如果没有找到 emoji表情，则返回源字符串
        } else {
            if (buf.length() == len) {//这里的意义在于尽可能少的toString，因为会重新生成字符串
                buf = null;
                return source;
            } else {
                return buf.toString();
            }
        }

    }
}
