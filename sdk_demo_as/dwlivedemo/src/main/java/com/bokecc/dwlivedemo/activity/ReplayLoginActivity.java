package com.bokecc.dwlivedemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.popup.LoginPopupWindow;
import com.bokecc.dwlivedemo.scan.qr_codescan.MipcaActivityCapture;
import com.bokecc.dwlivedemo.utils.StatusBarUtil;
import com.bokecc.dwlivedemo.view.LoginLineLayout;
import com.bokecc.livemodule.view.CustomToast;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.Marquee;
import com.bokecc.sdk.mobile.live.pojo.TemplateInfo;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplayLoginListener;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLoginInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 在线回放登录页面
 */
public class ReplayLoginActivity extends BaseActivity implements View.OnClickListener {

    private static final int MAX_NAME = 20;  // 用户昵称最多20字符
    private LoginLineLayout lllLoginReplayUid;       // CC 账号ID
    private LoginLineLayout lllLoginReplayRoomid;    // 直播间ID
    private LoginLineLayout lllLoginReplayRecordid;  // 回放ID
    private LoginLineLayout lllLoginReplayName;      // 用户昵称
    private LoginLineLayout lllLoginReplayPassword;  // 用户密码

    private Button btnLoginLive;  // 点击

    private LoginPopupWindow loginPopupWindow;   // 登录Loading控件
    private View mRoot;
    private long currentTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 全屏
//        requestFullScreenFeature();
        // 沉浸式
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.setLightStatusBar(this, true, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_login);
        initViews();

        preferences = getSharedPreferences("live_login_info", Activity.MODE_PRIVATE);
        getSharePrefernce();
        if (map != null) {
            initEditTextInfo();
        }

    }

    private void initViews() {
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_scan).setOnClickListener(this);
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        btnLoginLive = findViewById(R.id.btn_login_replay);
        lllLoginReplayUid = findViewById(R.id.lll_login_replay_uid);
        lllLoginReplayRoomid = findViewById(R.id.lll_login_replay_roomid);
        lllLoginReplayRecordid = findViewById(R.id.lll_login_replay_recordid);
        lllLoginReplayName = findViewById(R.id.lll_login_replay_name);
        lllLoginReplayPassword = findViewById(R.id.lll_login_replay_password);

        lllLoginReplayUid.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_uid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayRoomid.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_roomid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayRecordid.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_recordid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayName.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_name_hint)).addOnTextChangeListener(myTextWatcherLength);
        lllLoginReplayName.maxEditTextLength = MAX_NAME;
        lllLoginReplayPassword.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_s_password_hint)).addOnTextChangeListener(myTextWatcher)
                .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        preferences = getSharedPreferences("live_login_info", Activity.MODE_PRIVATE);

        loginPopupWindow = new LoginPopupWindow(this);

        btnLoginLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginPopupWindow != null && loginPopupWindow.isShowing()) {
                    return;
                }
                //防止频繁点击
                if (currentTime == 0 || System.currentTimeMillis() - currentTime > 2000) {
                    doLiveLogin();
                    currentTime = System.currentTimeMillis();
                }
            }
        });
    }

    /**
     * 隐藏弹窗
     */
    private void dismissPopupWindow() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loginPopupWindow != null && loginPopupWindow.isShowing()) {
                    loginPopupWindow.dismiss();
                }
            }
        });
    }

    //————————————-- 登录相关方法（核心方法）  ————————————————————-

    /**
     * 执行回放登录操作
     */
    private void doLiveLogin() {
        loginPopupWindow.show(mRoot);
        // 创建登录信息
        final ReplayLoginInfo replayLoginInfo = new ReplayLoginInfo();
        replayLoginInfo.setUserId(lllLoginReplayUid.getText().trim());
        replayLoginInfo.setRoomId(lllLoginReplayRoomid.getText().trim());
        replayLoginInfo.setRecordId(lllLoginReplayRecordid.getText().trim());
        replayLoginInfo.setViewerName(lllLoginReplayName.getText().trim());
        replayLoginInfo.setViewerToken(lllLoginReplayPassword.getText().trim());
//        replayLoginInfo.setViewerToken("XjbetHY9Wn84uEkUpNauVq");

        DWLiveReplay.getInstance().startLogin(replayLoginInfo, new DWLiveReplayLoginListener() {

            @Override
            public void onLogin(TemplateInfo templateInfo, Marquee marquee) {
                writeSharePreference();
                toastOnUiThread("登录成功");
                // 跳转回放界面
                ReplayPlayActivity.openActivity(ReplayLoginActivity.this, false, replayLoginInfo.getRecordId());

                dismissPopupWindow();
            }

            @Override
            public void onException(DWLiveException exception) {
                dismissPopupWindow();
                toastOnUiThread("登录失败:" + exception.getMessage());
            }
        });
    }

    //------------------------------- 缓存数据相关方法----------------------------------------------

    private SharedPreferences preferences;

    private void writeSharePreference() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("replayuid", lllLoginReplayUid.getText().trim());
        editor.putString("replayroomid", lllLoginReplayRoomid.getText().trim());
        editor.putString("replayrecordid", lllLoginReplayRecordid.getText().trim());
        editor.putString("replayusername", lllLoginReplayName.getText().trim());
        editor.putString("replaypassword", lllLoginReplayPassword.getText().trim());
        editor.commit();
    }

    private void getSharePrefernce() {
        lllLoginReplayUid.setText(preferences.getString("replayuid", "6B1744F4ABCBC599"));
        lllLoginReplayRoomid.setText(preferences.getString("replayroomid", "243BEE8FF66F7FB89C33DC5901307461"));
        lllLoginReplayRecordid.setText(preferences.getString("replayrecordid", "83495400F03564C0"));
        lllLoginReplayName.setText(preferences.getString("replayusername", "1"));
        lllLoginReplayPassword.setText(preferences.getString("replaypassword", "{\"nickname\":\"学员\",\"secret\":\"chaoge569912xdxx\"}"));
    }

    //———————————————— 扫码相关逻辑 ————————————————————————

    private static final int QR_REQUEST_CODE = 222;

    private String userIdStr = "userid";  // 用户id
    private String roomIdStr = "roomid";  // 房间id
    private String recordIdStr = "recordid";  // 回放id

    private Map<String, String> map;

    // 跳转到扫码页面
    private void showScan() {
        Intent intent = new Intent(this, MipcaActivityCapture.class);
        startActivityForResult(intent, QR_REQUEST_CODE);
    }

    // 接收并处理扫码页返回的数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case QR_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");

                    if (!result.contains("userid=")) {
                        CustomToast.showToast(getApplicationContext(), "扫描失败，请扫描正确的播放二维码", Toast.LENGTH_SHORT);
                        return;
                    }
                    map = parseUrl(result);
                    initEditTextInfo();
                }
                break;
            default:
                break;
        }
    }

    private void initEditTextInfo() {
        if (map.containsKey(roomIdStr)) {
            lllLoginReplayRoomid.setText(map.get(roomIdStr));
        }

        if (map.containsKey(userIdStr)) {
            lllLoginReplayUid.setText(map.get(userIdStr));
        }

        if (map.containsKey(recordIdStr)) {
            lllLoginReplayRecordid.setText(map.get(recordIdStr));
        }
    }

    //------------------------------------- 工具方法 -----------------------------------------------

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.iv_scan) {
            showScan();
        }
    }

    private final TextWatcher myTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean isLoginEnabled = isNewLoginButtonEnabled(lllLoginReplayUid, lllLoginReplayRoomid, lllLoginReplayName);
            btnLoginLive.setEnabled(isLoginEnabled);
            btnLoginLive.setTextColor(isLoginEnabled ? Color.parseColor("#ffffff") : Color.parseColor("#f7d8c8"));
        }
    };
    private final TextWatcher myTextWatcherLength = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.length() > 20) {
                toastOnUiThread(getResources().getString(R.string.username_max_length));
            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean isLoginEnabled = isNewLoginButtonEnabled(lllLoginReplayUid, lllLoginReplayRoomid, lllLoginReplayName);
            btnLoginLive.setEnabled(isLoginEnabled);
            btnLoginLive.setTextColor(isLoginEnabled ? Color.parseColor("#ffffff") : Color.parseColor("#f7d8c8"));
        }
    };

    // 检测登录按钮是否应该可用
    public static boolean isNewLoginButtonEnabled(LoginLineLayout... views) {
        for (LoginLineLayout view : views) {
            if ("".equals(view.getText().trim())) {
                return false;
            }
        }
        return true;
    }

    // 解析扫码获取到的URL
    private Map<String, String> parseUrl(String url) {
        Map<String, String> map = new HashMap<String, String>();
        String param = url.substring(url.indexOf("?") + 1, url.length());
        String[] params = param.split("&");

        if (params.length < 2) {
            return null;
        }

        for (String p : params) {
            String[] en = p.split("=");
            if (en.length < 2) {
                map.put(en[0], "");
            } else {
                map.put(en[0], en[1]);
            }
        }
        return map;
    }
}
