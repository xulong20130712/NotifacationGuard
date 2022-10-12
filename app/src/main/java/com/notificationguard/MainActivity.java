package com.notificationguard;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.notificationguard.utils.NetUtils;
import com.notificationguard.utils.RequestType;
import com.notificationguard.utils.SettingUtils;
import com.notificationguard.utils.StringUtils;
import com.notificationguard.vmq.VMQUtils;
import com.notificationguard.vmq.util.Constant;
import com.notificationguard.zxing.activity.CaptureActivity;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "+-->";

    private String host;
    private String key;

    private TextView txthost;
    private TextView txtkey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String string = Settings.Secure.getString(getContentResolver(),
                SettingUtils.NOTIFICATION_FLAG);
        if (!isValid(string)) {
            startActivity(new Intent(
                    SettingUtils.ACTION_NOTIFICATION_FLAG));
        }

        initService();
        txthost = findViewById(R.id.txt_host);
        txtkey = findViewById(R.id.txt_key);
    }

    private void initService() {

        if (!NotificationListenerServiceimpl.isValidNotificationListener()) {

            toggleNotificationListenerService(this);
        }
    }

    private void toggleNotificationListenerService(Context context) {

        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, NotificationListenerServiceimpl.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(context, NotificationListenerServiceimpl.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        // 不要每次打开都显示
        // Toast.makeText(MainActivity.this, "监听服务启动中...", Toast.LENGTH_SHORT).show();
    }

    public void startQrCode(View v) {
        // 申请相机权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 申请文件读写权限（部分朋友遇到相册选图需要读写权限的情况，这里一并写一下）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    //手动配置
    public void doInput(View v) {

        final EditText inputServer = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入配置数据").setView(inputServer)
                .setNegativeButton("取消", null);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String scanResult = inputServer.getText().toString();

                String[] tmp = scanResult.split("/");
                if (tmp.length != 2) {
                    Toast.makeText(MainActivity.this, "数据错误，请您输入网站上显示的配置数据!", Toast.LENGTH_SHORT).show();
                    return;
                }


                //将扫描出的信息显示出来
                txthost.setText(" 通知地址：" + tmp[0]);
                txtkey.setText(" 通讯密钥：" + tmp[1]);
                host = tmp[0];
                key = tmp[1];

                SharedPreferences.Editor editor = getSharedPreferences("vone", MODE_PRIVATE).edit();
                editor.putString("host", host);
                editor.putString("key", key);
                editor.commit();

            }
        });
        builder.show();

    }

    private boolean isValid(String flat) {

        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");

            Log.e("+-->", "---isValid---" + flat);
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(getPackageName(), cn.getPackageName())) {

                        Log.e("+-->", "---isValid true---");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);

            Log.e("+-->", "---onActivityResult---" + scanResult);
            String[] tmp = scanResult.split("/");
            if (tmp.length != 2) {
                Toast.makeText(MainActivity.this, "二维码错误，请您扫描网站上显示的二维码!", Toast.LENGTH_SHORT).show();
                return;
            }
//            String t = String.valueOf(new Date().getTime());
//            String sign = StringUtils.md5(t + tmp[1]);
//            String url = NetUtils.URLSCHEMA_HTTPS + tmp[0] + "&sign=" + sign;
//            Request request = new Request.Builder().url("http://" + tmp[0] + "/appHeart?t=" + t + "&sign=" + sign).method("GET", null).build();
//            Call call = Utils.getOkHttpClient().newCall(request);

            requestData(tmp[0], tmp[1], null);
            //将扫描出的信息显示出来
            txthost.setText(" 通知地址：" + tmp[0]);
            txtkey.setText(" 通讯密钥：" + tmp[1]);
            host = tmp[0];
            key = tmp[1];

            SharedPreferences.Editor editor = getSharedPreferences("vone", MODE_PRIVATE).edit();
            editor.putString("host", host);
            editor.putString("key", key);
            editor.commit();
        }
    }

    private void requestData(final String host, final String signKey, final HashMap<String, String> body) {

        String t = String.valueOf(new Date().getTime());
        String signStr = StringUtils.md5(t + signKey);
        String url = NetUtils.URLSCHEMA_HTTPS + host+ "/commit&sign=" + signStr;
        Callback callback = new Callback() {
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
        String json= "{ \"name\":\"runoob\", \"alexa\":10000, \"site\":null }";
        NetUtils.getInstance().requestData(url, RequestType.POST, callback, getRequestBody(json));
    }

    private FormBody getFormBody(HashMap<String, String> body) {

        if(body== null|| body.isEmpty()) {

            return null;
        }
        FormBody formBody= new FormBody.Builder()
                .build();

        return formBody;
    }

    private RequestBody getRequestBody(final String json) {

        if(StringUtils.isEmptyString(json)) {

            return null;
        }
        RequestBody requestBody= RequestBody.create(MediaType.parse("application/json"), json);
        return requestBody;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode(null);
                } else {
                    // 被禁止授权
                    Toast.makeText(MainActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            case Constant.REQ_PERM_EXTERNAL_STORAGE:
                // 文件读写权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode(null);
                } else {
                    // 被禁止授权
                    Toast.makeText(MainActivity.this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}