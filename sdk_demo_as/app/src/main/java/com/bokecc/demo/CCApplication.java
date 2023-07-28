package com.bokecc.demo;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.bokecc.dwlivedemo.DWApplication;

/**
 * Application
 */
public class CCApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        //公共库初始化
        DWApplication.getInstance().init(this);

    }

    /**
     * 程序终止的时候执行
     */
    @Override
    public void onTerminate() {
        Log.i("CCApplication", "sdk_bokecc CCApplication onTerminate");
        DWApplication.getInstance().onTerminate();
        super.onTerminate();
    }

}
