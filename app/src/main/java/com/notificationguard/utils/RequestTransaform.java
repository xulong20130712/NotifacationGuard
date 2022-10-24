package com.notificationguard.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.notificationguard.DataBean;
import com.notificationguard.RequestBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestTransaform {

    private static String TAG= "+-->";



    public static void requestData(DataBean dataBean, Callback callback) {

        String url= "https://api.spointyc.com/edge/channel/v1/updateInfo";
        if(callback== null) {

            callback = new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    Log.e(TAG, "---onFailure---");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    try {
                        Log.e(TAG, "---onResponse: " + response.body().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    isOk = true;
                }
            };
        }
        String dataJson= "";
        String values= "";
        dataJson= JSON.toJSONString(dataBean);
        try {
            values = RSAUtils.encrypt(dataJson, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Pattern pattern = Pattern.compile("\\s*|\\t|\\r|\\n");
        Matcher matcher= pattern.matcher(values);
        values= matcher.replaceAll("");
        Log.e("+-->", "values---"+ values);
        RequestBean requestBean= new RequestBean();
        requestBean.setData(values);
        String requestJson= JSON.toJSONString(requestBean);
        Log.e("+-->", "---dataJson rsa---"+ requestJson);
        NetUtils.getInstance().requestData(url, RequestType.POST, callback, getRequestBody(requestJson));
    }

    private static RequestBody getRequestBody(final String json) {

        if(StringUtils.isEmptyString(json)) {

            return null;
        }
        RequestBody requestBody= RequestBody.create(MediaType.parse("application/json"), json);
        return requestBody;
    }
}
