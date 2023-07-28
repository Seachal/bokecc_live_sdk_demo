package com.bokecc.livemodule.replay;

import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayDot;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLineInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPageInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPracticeInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQualityinfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 回放直播间信息监听
 */
public interface DWReplayRoomListener {

    /**
     * 回放播放初始化已经完成
     */
    void videoPrepared();

    //视频开始播放
    void startRending();

    /**
     * 开始缓冲
     */
    void bufferStart();

    /**
     * 更新缓冲进度
     *
     * @param percent 缓冲百分比
     */
    void updateBufferPercent(int percent);

    /**
     * 展示当前直播回放时长
     *
     * @param playerDuration 播放时长
     */
    void showVideoDuration(long playerDuration);

    /**
     * 播放完成
     */
    void onPlayComplete();

    /**
     * seek完成
     */
    void onSeekComplete();

    /**
     * 播放错误
     *
     * @param code code
     */
    void onPlayError(int code);

    /**
     * 房间内出现错误
     */
    void onException(DWLiveException exception);

    /**
     * 清晰度回调
     *
     * @param videoQuality   清晰度列表
     * @param currentQuality 当前清晰度
     */
    void onHDReceivedVideoQuality(List<ReplayQualityinfo> videoQuality, ReplayQualityinfo currentQuality);

    /**
     * 线路回调
     *
     * @param lines            线路列表
     * @param currentLineIndex 当前线路下标
     */
    void onHDReceivedVideoAudioLines(List<ReplayLineInfo> lines, int currentLineIndex);

    /**
     * 该回放是否存在音频
     *
     * @param hasAudio
     */
    void onHDAudioMode(DWLiveReplay.Audio hasAudio);

    /**
     * 获取到的随堂测信息
     *
     * @param list list
     */
    void onHDReceivePracticeList(List<ReplayPracticeInfo> list);

    /**
     * 获取到的回放打点列表
     *
     * @param dotList dotList
     */
    void onHDReplayDotList(List<ReplayDot> dotList);

    /**
     * 回放页面信息
     *
     * @param infoList infoList
     */
    public void onPageInfoList(ArrayList<ReplayPageInfo> infoList);
}
