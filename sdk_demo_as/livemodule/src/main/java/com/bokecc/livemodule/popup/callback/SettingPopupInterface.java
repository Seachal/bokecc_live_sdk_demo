package com.bokecc.livemodule.popup.callback;

import com.bokecc.sdk.mobile.live.pojo.LiveQualityInfo;

public interface SettingPopupInterface {
    //当前的倍速
    void onSpeed(float speed);
    //当前的模式
    void onPlayMode(boolean isAudio,SettingPopupWindowCallBack callBack);
    //当前的清晰度
    void onQuality(int quality,String desc,SettingPopupWindowCallBack callBack);
    //当前的线路
    void onLine(int line,SettingPopupWindowCallBack callBack);
}
