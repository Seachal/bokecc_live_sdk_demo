package com.bokecc.livemodule;

import android.app.Application;
import android.util.Log;

import com.bokecc.sdk.mobile.live.DWLiveEngine;

/**
 * 直播 SDK 帮助类
 */
public class LiveSDKHelper {

    private static final String TAG = "CCLive";


    /**
     * 初始化SDK
     *
     * @param app 应用上下文
     */
    public static void initSDK(Application app) {
        // 只需要初始化一次
        if (DWLiveEngine.getInstance() == null) {
            // enableLog:是否开启日志
            // enableX5: 是否开启x5内核
            DWLiveEngine.init(app, BuildConfig.DEBUG, false);

        } else {
            Log.i(TAG, "DWLiveEngine has init");
        }
    }

    /**
     * 销毁SDK
     */
    public static void onTerminate() {
        DWLiveEngine.getInstance().onTerminate();
    }

}
