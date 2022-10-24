package com.notificationguard.utils;


import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAUtils {




    //公钥
    private static String default_publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx2q4d0CwcrY28OKRVR0B\n" +
            "QbN8e80/q1Cs6sbKLAvs8io/7VMiZdcwg8/8NV/dX+DrGlliYRPVGrXvLRki3q0e\n" +
            "+8of4Yi7BX499eyoZiZqJGzru4DuNQPiOB6C6HPZXeOySlP1yDxwJ5xTYcuq3Ikr\n" +
            "on4xJEu9C5+aRZtvKXTHwNyqPGYtjhFJW4aH8W0Xw22QP7ZwWwCCeL0cHhIuIAp+\n" +
            "lxIJ3rbxcqFrZiZziSrHblPr0I7bMcqplGANrvCZaBbeSIjVLBXCL/nshA1axS3z\n" +
            "W9x9ifNy9vaflYJ1ME6AtJMwYXgekMNaVGrAXGw2hKFmpu6z6P5zlXAxZUDmerV8\n" +
            "0QIDAQAB";
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;


    /**
     * 获取公钥
     *
     * @param publicKey 公钥字符串
     * @return
     */
    public static PublicKey getPublicKey(String publicKey) throws Exception {

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodedKey = Base64.decode(publicKey.getBytes(), Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * RSA加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return
     */

    public static String encrypt(String data, String publicKey) throws Exception {

        if(StringUtils.isEmptyString(publicKey)) {

            publicKey= default_publicKey;
        }
        return encrypt(data, getPublicKey(publicKey));
    }

    public static String encrypt(String data, PublicKey publicKey) throws Exception {

        Cipher cipher;
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        int inputLen = data.getBytes().length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data.getBytes(), offset, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data.getBytes(), offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        // 获取加密内容使用base64进行编码,并以UTF-8为标准转化成字符串
        // 加密后的字符串
        String str= new String(Base64.encode(encryptedData, Base64.DEFAULT));
        Log.e("+-->", "---str---"+ str);
        return str;
    }
}
