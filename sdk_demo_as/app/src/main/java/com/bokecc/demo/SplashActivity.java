package com.bokecc.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.bokecc.dwlivedemo.activity.PilotActivity;

/**
 * 闪屏界面
 *
 * @author wy
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //第一次安装直接打开推到后台再进入重新走splash的问题处理
        if(!isTaskRoot()){
            finish();
            return;
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        //延迟3s进入
        long SPLASH_DELAY = 3 * 1000L;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, PilotActivity.class));
                finish();
            }
        }, SPLASH_DELAY);
    }

}
