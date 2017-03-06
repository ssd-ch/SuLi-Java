package com.example.ssd.shimaneuniversitybrowser;

public class StringUtil {

    /**
     * 文字列に含まれる全角数字を半角数字に変換します。
     *
     * @param str 変換前文字列(null不可)
     * @return 変換後文字列
     */
    public static String fullWidthNumberToHalfWidthNumber(String str) {
        if (str == null){
            throw new IllegalArgumentException();
        }
        StringBuffer sb = new StringBuffer(str);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ('０' <= c && c <= '９') {
                sb.setCharAt(i, (char) (c - '０' + '0'));
            }
        }
        return sb.toString();
    }

    /**
     * 文字列に含まれる半角数字を全角数字に変換します。
     *
     * @param str 変換前文字列(null不可)
     * @return 変換後文字列
     */
    public static String halfWidthNumberToFullWidthNumber(String str) {
        if (str == null){
            throw new IllegalArgumentException();
        }
        StringBuffer sb = new StringBuffer(str);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ('0' <= c && c <= '9') {
                sb.setCharAt(i, (char) (c - '0' + '０'));
            }
        }
        return sb.toString();
    }
}