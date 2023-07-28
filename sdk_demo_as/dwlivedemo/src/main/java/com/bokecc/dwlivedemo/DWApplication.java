package com.bokecc.dwlivedemo;

import android.app.Application;
import android.content.Context;

import com.bokecc.livemodule.LiveSDKHelper;

/**
 * 应用的 Application
 */
public class DWApplication {

    private static Application context;

    private DWApplication() {
    }

    public static DWApplication getInstance() {
        return DWApplication.SingletonHolder.instance;
    }

    private static class SingletonHolder {
        public static DWApplication instance = new DWApplication();
    }

    /**
     * 初始化
     *
     * @param application application
     */
    public void init(Application application) {
        context = application;
        //初始化SDK
        LiveSDKHelper.initSDK(application);
    }

    /**
     * 销毁
     */
    public void onTerminate() {
        LiveSDKHelper.onTerminate();
    }

    /**
     * 获取上下文
     * @return context
     */
    public static Context getContext() {
        return context;
    }

}

