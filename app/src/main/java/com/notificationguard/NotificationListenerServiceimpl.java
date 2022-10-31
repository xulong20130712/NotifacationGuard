package com.notificationguard;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.notificationguard.utils.RequestTransaform;
import com.notificationguard.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class NotificationListenerServiceimpl extends NotificationListenerService {


    private static boolean isValid= false;

    private static String WECHATTAG= "com.tencent";
    private static String ALITAG= "com.eg.android.AlipayGphone";//aa徐龙通过扫码向你付款0.01元
    private static String ALISPLIT= "通过扫码向你付款";
    private static String WECHATSPLIT= "通过扫码向你付款";
    private boolean isAliPay= false;

    private DataBean dataBean= new DataBean();


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        if(sbn== null) {

            return;
        }
        Log.e("+-->", "---onNotificationPosted---"+ sbn.getPackageName());
        Notification notification = sbn.getNotification();
        String packageName= sbn.getPackageName();
        Log.e("+-->", "---packageName---"+ packageName);
        if(!StringUtils.isEmptyString(packageName)) {

            isAliPay= packageName.contains(ALITAG);
        }
        Log.e("+-->", "---isAliPay---"+ isAliPay);
        if (notification == null) {
            return;
        }
        PendingIntent pendingIntent = null;
        // 当 API > 18 时，使用 extras 获取通知的详细信息
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Bundle extras = notification.extras;
            if (extras != null) {
                // 获取通知标题
                String title = extras.getString(Notification.EXTRA_TITLE, "");
                // 获取通知内容
                String content = extras.getString(Notification.EXTRA_TEXT, "");
                Log.e("+-->", "---intent title---"+ title+ "-content"+ content);
                String[] values= null;
                if(isAliPay&& !StringUtils.isEmptyString(content)) {
                    values= content.split(ALISPLIT);
                    if(values!= null&& values.length>1) {

                        values[1]= values[1].replace("元", "");
                        dataBean.setNickName(values[0]);
                        dataBean.setPrice(Double.parseDouble(values[1]));
                        dataBean.setExtra("extra");
                        Log.e("+-->", "---dataBean---"+ dataBean);
                        RequestTransaform.requestData(dataBean, null);
                    }
                }else {

                }
                Log.e("+-->", "---values---"+ values);
                if (!TextUtils.isEmpty(content) && content.contains("[微信红包]")) {
                    pendingIntent = notification.contentIntent;
                }
            }
        } else {
            // 当 API = 18 时，利用反射获取内容字段
            List<String> textList = getText(notification);
            if (textList != null && textList.size() > 0) {
                for (String text : textList) {
                    if (!TextUtils.isEmpty(text) && text.contains("[微信红包]")) {
                        pendingIntent = notification.contentIntent;
                        break;
                    }
                }
            }
        }
        // send pendingIntent to open wechat
        try {
            if (pendingIntent != null) {
                pendingIntent.send();
            }
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        Log.e("+-->", "---onNotificationRemoved---");
    }

    public List<String> getText(Notification notification) {
        if (null == notification) {
            return null;
        }
        RemoteViews views = notification.bigContentView;
        if (views == null) {
            views = notification.contentView;
        }
        if (views == null) {
            return null;
        }
        // Use reflection to examine the m_actions member of the given RemoteViews object.
        // It's not pretty, but it works.
        List<String> text = new ArrayList<>();
        try {
            Field field = views.getClass().getDeclaredField("mActions");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);
            // Find the setText() and setTime() reflection actions
            for (Parcelable p : actions) {
                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);
                // The tag tells which type of action it is (2 is ReflectionAction, from the source)
                int tag = parcel.readInt();
                if (tag != 2) continue;
                // View ID
                parcel.readInt();
                String methodName = parcel.readString();
                if (null == methodName) {
                    continue;
                } else if (methodName.equals("setText")) {
                    // Parameter type (10 = Character Sequence)
                    parcel.readInt();
                    // Store the actual string
                    String t = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
                    text.add(t);
                }
                parcel.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

        isValid= true;
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();

        isValid= false;
    }

    public static boolean isValidNotificationListener() {

        return isValid;
    }
}
