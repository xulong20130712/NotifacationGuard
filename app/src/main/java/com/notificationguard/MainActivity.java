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

import com.alibaba.fastjson.JSON;
import com.notificationguard.utils.NetUtils;
import com.notificationguard.utils.RSAUtils;
import com.notificationguard.utils.RequestType;
import com.notificationguard.utils.SettingUtils;
import com.notificationguard.utils.StringUtils;
import com.notificationguard.vmq.util.Constant;
import com.notificationguard.zxing.activity.CaptureActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
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

        // ???????????????????????????
        // Toast.makeText(MainActivity.this, "?????????????????????...", Toast.LENGTH_SHORT).show();
    }

    public void startQrCode(View v) {
        // ??????????????????
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // ????????????
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // ????????????
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
            return;
        }
        // ???????????????
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    //????????????
    public void doInput(View v) {

        final EditText inputServer = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("?????????????????????").setView(inputServer)
                .setNegativeButton("??????", null);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String scanResult = inputServer.getText().toString();

                String[] tmp = scanResult.split("/");
                if (tmp.length != 2) {
                    Toast.makeText(MainActivity.this, "?????????????????????????????????????????????????????????!", Toast.LENGTH_SHORT).show();
                    return;
                }


                //?????????????????????????????????
                txthost.setText(" ???????????????" + tmp[0]);
                txtkey.setText(" ???????????????" + tmp[1]);
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
        //??????????????????
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);

            Log.e("+-->", "---onActivityResult---" + scanResult);
            String[] tmp = scanResult.split("/");
            if (tmp.length != 2) {
                Toast.makeText(MainActivity.this, "?????????????????????????????????????????????????????????!", Toast.LENGTH_SHORT).show();
                return;
            }
//            String t = String.valueOf(new Date().getTime());
//            String sign = StringUtils.md5(t + tmp[1]);
//            String url = NetUtils.URLSCHEMA_HTTPS + tmp[0] + "&sign=" + sign;
//            Request request = new Request.Builder().url("http://" + tmp[0] + "/appHeart?t=" + t + "&sign=" + sign).method("GET", null).build();
//            Call call = Utils.getOkHttpClient().newCall(request);

            requestData(tmp[0], tmp[1], null);
            //?????????????????????????????????
            txthost.setText(" ???????????????" + tmp[0]);
            txtkey.setText(" ???????????????" + tmp[1]);
            host = tmp[0];
            key = tmp[1];

            SharedPreferences.Editor editor = getSharedPreferences("vone", MODE_PRIVATE).edit();
            editor.putString("host", host);
            editor.putString("key", key);
            editor.commit();
        }
    }

    private void requestData(final String host, final String signKey, final HashMap<String, String> body) {

        String url= "https://api.spointyc.com/edge/channel/v1/updateInfo";
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
        String dataJson= "";
        String values= "";
        DataBean dataBean= new DataBean();
        dataBean.setExtra("this is ex");
        dataBean.setPrice(100);
        dataBean.setNickName("this is pro");
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
                // ?????????????????????
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ????????????
                    startQrCode(null);
                } else {
                    // ???????????????
                    Toast.makeText(MainActivity.this, "??????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                }
                break;
            case Constant.REQ_PERM_EXTERNAL_STORAGE:
                // ????????????????????????
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ????????????
                    startQrCode(null);
                } else {
                    // ???????????????
                    Toast.makeText(MainActivity.this, "??????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}