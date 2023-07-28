package com.bokecc.livemodule.live;

import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.LiveLineInfo;
import com.bokecc.sdk.mobile.live.pojo.LiveQualityInfo;

import java.util.List;

/**
 * 直播间信息回调监听
 */
public interface DWLiveRoomListener {

    /**
     * 切换视频文档区域
     *
     * @param isVideoMain 视频是否为主区域
     */
    void onSwitchVideoDoc(boolean isVideoMain);

    /**
     * 展示直播间标题
     */
    void showRoomTitle(String title);

    /**
     * 展示直播间人数
     */
    void showRoomUserNum(int number);

    /**
     * 踢出用户
     */
    void onKickOut();

    /**
     * 信息，一般包括被禁言、禁止提问等
     *
     * @param msg msg
     */
    void onInformation(String msg);

    /**
     * 下课：流结束
     *
     * @param isNormal 是否正常结束
     */
    void onStreamEnd(boolean isNormal, String reason);

    /**
     * 开始上课：流开始
     */
    void onStreamStart();

    /**
     * 显示错误
     * 1. 网络断开：
     * 2. 连接超时
     * 3. 其他错误
     */
    void showError(String errMsg);

    /**
     * 展示是否可以切换到语音
     */
    void onHDAudioMode(DWLive.LiveAudio hasAudio);

    // 展示清晰度
    void onHDReceivedVideoQuality(List<LiveQualityInfo> videoQuality, LiveQualityInfo currentQuality);

    // 展示线路
    void onHDReceivedVideoAudioLines(List<LiveLineInfo> lines, int indexNum);

    /**
     * 网络断开恢复时，自动执行重试
     */
    void callRetryAndHideErrorView();
}
