package com.bokecc.dwlivedemo.base;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.bokecc.dwlivedemo.utils.StatusBarUtil;
import com.bokecc.livemodule.view.CustomToast;

/**
 * Activity 基类
 */
public abstract class BaseActivity extends AppCompatActivity {


    // 请求全屏特性
    public void requestFullScreenFeature() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideActionBar();
    }

    // 请求状态栏黑色
    public void requestStatusBarBlack(FragmentActivity activity) {
        StatusBarUtil.setStatusBarColor(activity, Color.BLACK);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideActionBar();
    }

    // 隐藏ActionBar
    public void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    // 跳转到其他的Activity
    public void go(Class clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    // 跳转到其他的Activity
    public void go(Class clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    // 在UI线程上进行吐司提示
    public void toastOnUiThread(final String msg) {
        // 判断是否处在UI线程
        if (!checkOnMainThread()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    showToast(msg);
                }
            });
        } else {
            showToast(msg);
        }
    }

    // 进行吐司提示
    private void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        CustomToast.showToast(this, msg, Toast.LENGTH_SHORT);
    }

    // 判断当前的线程是否是UI线程
    protected boolean checkOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    // 判断当前屏幕朝向是否为竖屏
    public boolean isPortrait() {
        int mOrientation = getApplicationContext().getResources().getConfiguration().orientation;
        return mOrientation != Configuration.ORIENTATION_LANDSCAPE;
    }


}
