package com.notificationguard.utils;

import com.notificationguard.vmq.VMQUtils;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NetUtils {


    public static final String URLSCHEMA_HTTPS= "https://";
    private static final String DEFAULT_REQ_METHOD = "POST";
    private String requestUrl= "";
    private String secretKey= "";
    private static NetUtils netUtils;

    private NetUtils() {

    }

    public static NetUtils getInstance() {

        return SingleHolder.getInstance();
    }

    private static class SingleHolder {

        public static NetUtils getInstance() {

            if (netUtils== null) {
                netUtils= new NetUtils();
            }
            return netUtils;
        }
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

//    public void requestData(final String requestUrl, String method, Callback callback, FormBody body) {
//
//        if(StringUtils.isEmptyString(method)) {
//
//            method= REQ_METHOD;
//        }
//        Request request = new Request.Builder()
//                .url(requestUrl)
//                .method(method, null)
//                .post(body)
//                .build();
////        Request request = new Request.Builder().url("http://" + tmp[0] + "/appHeart?t=" + t + "&sign=" + sign).method("GET", null).build();
//        Call call = VMQUtils.getOkHttpClient().newCall(request);
//        call.enqueue(callback);
//    }

    public void requestData(final String requestUrl, String method, Callback callback, RequestBody body) {

        try {
            if (StringUtils.isEmptyString(method)) {

                method = DEFAULT_REQ_METHOD;
            }
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .method(method, body)
                    .build();
//        Request request = new Request.Builder().url("http://" + tmp[0] + "/appHeart?t=" + t + "&sign=" + sign).method("GET", null).build();
            Call call = VMQUtils.getOkHttpClient().newCall(request);
            call.enqueue(callback);
        }catch (Throwable throwable) {

            throwable.printStackTrace();
        }
    }

    public void requestData(final String requestUrl, String method, Callback callback) {

        requestData(requestUrl, method, callback, null);
    }
}