package com.notificationguard.utils;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {

    /**
     * 判断是不是空字符串，如果是null也会认为是空字符串
     * @param str
     * @return
     */
    public static boolean isEmptyString(final String str) {

        boolean flag= false;
        if(str== null|| str.length()<= 0|| str.equalsIgnoreCase("null")) {
            flag= true;
        }
        return flag;
    }

    public static String byte2String(byte[] bytes) {

        if(bytes== null|| bytes.length<= 0) {

            return "";
        }
        return new String(bytes);
    }

    public static String md5(String string) {

        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
