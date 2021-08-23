
package com.txznet.txz.component.nav.baidu.info;

public class StringUtils
{

	public final static String[] unitDistArray = new String[] { "m", "km", "米", "公里" };
	
    /**
     * 距离中英文单位枚举
     */
    public enum UnitLangEnum {

        EN(0), ZH(1);

        private int nUnit;

        private UnitLangEnum(int nUnit) {
            this.nUnit = nUnit;
        }

        public int getnUnit() {
            return nUnit;
        }

        public void setnUnit(int nUnit) {
            this.nUnit = nUnit;
        }

    }
	
	/**
     * 转换剩余距离为分档显示距离
     * 
     * @param nDist ，剩余距离，单位：米（m）
     * @param nUnit ，单位类型，0-英文：M/KM，1-中文：米/公里
     * @param formatDist ，格式化之后的字符串输出，包含单位后缀
     * @return 返回转换之后的距离值，为整零值
     */
    public static void formatDistance(int nDist, UnitLangEnum langEnum, StringBuffer formatDist) {

        int offset = 0;
        boolean bNoZero = false;
        int nUnit = langEnum.getnUnit();

        // 偏移到中文单位索引
        if (nUnit != 0) {
            nUnit++;
        }
        
        if (nDist >= 1000) {
            offset = 1;
            // Log.d(TAG, "++ formatDistance   nDist="+nDist);

            if (nDist % 1000 == 0) {
                bNoZero = true;
            }

            String distFormat = "";
            if (bNoZero) {
                distFormat = "%.0f%s";
            } else {
                distFormat = "%.1f%s";
            }

			if (formatDist != null) {
				int km = nDist / 1000;
				if (km >= 100) {
					// 大于等于100km
					distFormat = "%d%s";
					formatDist.append(String.format(distFormat, km, unitDistArray[nUnit + offset]));
				} else {
					formatDist.append(String.format(distFormat, (double) nDist / 1000, unitDistArray[nUnit + offset]));
				}
			}
		} else {
			offset = 0;
			if (formatDist != null) {
				formatDist.append(String.format("%d%s", nDist, unitDistArray[nUnit + offset]));
			}
		}
	}
	
    public static boolean isNotEmpty(String s) {
        if (s == null || s.length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isEmpty(String s) {
        if (s == null || s.length() == 0) {
            return true;
        } else {
            return false;
        }
    }
}
