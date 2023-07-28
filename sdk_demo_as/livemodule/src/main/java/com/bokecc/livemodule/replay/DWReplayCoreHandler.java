package com.bokecc.livemodule.replay;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.bokecc.livemodule.BuildConfig;
import com.bokecc.livemodule.utils.ProRecordWorker;
import com.bokecc.livemodule.utils.ThreadUtils;
import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.BaseRecordInfo;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplayListener;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.replay.ReplayChangeSourceListener;
import com.bokecc.sdk.mobile.live.replay.entity.ReplayLineParams;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayBroadCastMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayDot;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLineInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLiveInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPageInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPracticeInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQAMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQualityinfo;
import com.bokecc.sdk.mobile.live.widget.DocView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * 回放相关逻辑核心处理机制
 */
public class DWReplayCoreHandler {

    private static final String TAG = "DWReplayCoreHandler";

    private static final DWReplayCoreHandler dwReplayCoreHandler = new DWReplayCoreHandler();
    // 进度记忆功能
    private ProRecordWorker proRecordWorker;

    /**
     * 私有构造函数
     */
    private DWReplayCoreHandler() {
    }

    /**
     * 获取DWReplayCoreHandler单例的实例
     */
    public static DWReplayCoreHandler getInstance() {
        return dwReplayCoreHandler;
    }

    //******************************* 各类监听相关 ***************************************/

    /**
     * 回放聊天监听
     */
    private DWReplayChatListener replayChatListener;

    /**
     * 设置回放聊天监听
     *
     * @param replayChatListener 回放聊天监听
     */
    public void setReplayChatListener(DWReplayChatListener replayChatListener) {
        this.replayChatListener = replayChatListener;
    }

    /**
     * 回放问答监听
     */
    private DWReplayQAListener replayQAListener;

    /**
     * 设置回放问答监听
     *
     * @param replayQAListener 回放问答监听
     */
    public void setReplayQAListener(DWReplayQAListener replayQAListener) {
        this.replayQAListener = replayQAListener;
    }

    // 直播间信息监听
    private DWReplayRoomListener replayRoomListener;

    /**
     * 设置直播间信息监听
     *
     * @param listener 直播间信息监听
     */
    public void setReplayRoomListener(DWReplayRoomListener listener) {
        this.replayRoomListener = listener;
    }

    //******************************* 设置"播放"组件/控件相关 ***************************************/

    private DWReplayPlayer player;

    private DocView docView;

    // 进度记忆控件和唯一id
    private TextView proRecordJump;
    private String recordTag;

    /**
     * 设置播放器
     *
     * @param player 播放器
     */
    public void setPlayer(DWReplayPlayer player) {
        this.player = player;
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.setReplayPlayer(this.player);
        }
    }

    /**
     * 获取当前的播放器
     */
    public DWReplayPlayer getPlayer() {
        return this.player;
    }

    /**
     * 设置文档展示控件
     *
     * @param docView 文档展示控件
     */
    public void setDocView(DocView docView) {
        this.docView = docView;
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.setReplayDocView(this.docView);
        }
    }

    /**
     * 设置进度记忆控件
     *
     * @param textView textView
     */
    public void setProRecordJumpTextView(TextView textView) {
        proRecordJump = textView;
    }

    /**
     * 设置进度记忆唯一标识
     *
     * @param recordTag recordTag
     */
    public void setRecordTag(String recordTag) {
        this.recordTag = recordTag;
    }

    //******************************* 直播间模版相关 ***************************************/

    private final static String ViEW_VISIBLE_TAG = "1";

    /**
     * 当前直播间是否有'文档'
     */
    public boolean hasDocView() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null && dwLiveReplay.getTemplateInfo() != null) {
            return dwLiveReplay.getTemplateInfo().hasDoc();
        }
        return false;
    }

    /**
     * 当前直播间是否有'聊天'
     */
    public boolean hasChatView() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null && dwLiveReplay.getTemplateInfo() != null) {
            return dwLiveReplay.getTemplateInfo().hasChat();
        }
        return false;
    }

    /**
     * 当前直播间是否有'问答'
     */
    public boolean hasQaView() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null && dwLiveReplay.getTemplateInfo() != null) {
            return dwLiveReplay.getTemplateInfo().hasQa();
        }
        return false;
    }

    /**
     * 当前直播间是否显示跑马灯
     *
     * @return true 显示 false 不显示
     */
    public boolean isOpenMarquee() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null && dwLiveReplay.getRoomInfo() != null) {
            if (dwLiveReplay.getRoomInfo().getOpenMarquee() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文档显示模式
     *
     * @return 文档窗口状态：1——适合窗口，2——适合宽度
     */
    public int getDocumentDisplayMode() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null && dwLiveReplay.getRoomInfo() != null) {
            return dwLiveReplay.getRoomInfo().getDocumentDisplayMode();
        }
        return 1;
    }

    /**
     * 获取基本信息
     *
     * @return BaseRecordInfo
     */
    public BaseRecordInfo getBaseRecordInfo() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null && dwLiveReplay.getRoomInfo() != null) {
            return dwLiveReplay.getRoomInfo().getBaseRecordInfo();
        }
        return null;
    }

    /**
     * 获取直播信息
     *
     * @return ReplayLiveInfo
     */
    public ReplayLiveInfo getReplayLiveInfo() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            return dwLiveReplay.getReplayLiveInfo();
        }
        return null;
    }


    //***************************** 视频播放相关 ***************************************/

    /**
     * 开始播放
     */
    public void start() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        dwLiveReplay.setReplayParams(dwLiveReplayListener, DWLiveEngine.getInstance().getContext());
        dwLiveReplay.start();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.pause();
        }
    }

    /**
     * 释放资源
     */
    public void destroy() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.onDestroy();
        }
        replayChatListener = null;
        replayQAListener = null;
        replayRoomListener = null;
        if (docView != null) {
            docView = null;
        }
        if (player != null) {
            player = null;
        }
        if (proRecordJump != null) {
            proRecordJump = null;
        }
        if (proRecordWorker != null) {
            proRecordWorker = null;
        }
    }

    /**
     * 重试播放
     *
     * @param time:时间点，是否强制更新流地址
     */
    public void retryReplay(long time, boolean updateStream) {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.retryReplay(time, updateStream);
        }
    }

    /**
     * 更新当前缓冲进度
     */
    public void updateBufferPercent(int percent) {
        if (replayRoomListener != null) {
            replayRoomListener.updateBufferPercent(percent);
        }
    }

    public void playError(int code) {
        if (replayRoomListener != null) {
            replayRoomListener.onPlayError(code);
        }
    }

    public void bufferStart() {
        if (replayRoomListener != null) {
            replayRoomListener.bufferStart();
        }
    }

    public void onPlayComplete() {
        if (replayRoomListener != null) {
            replayRoomListener.onPlayComplete();
        }
    }

    public void onSeekComplete() {
        if (replayRoomListener != null) {
            replayRoomListener.onSeekComplete();
        }
    }

    public void onRenderStart() {
        if (replayRoomListener != null) {
            replayRoomListener.startRending();
        }
    }

    /**
     * 回放视频准备好了
     */
    public void replayVideoPrepared() {
        replayRoomListener.showVideoDuration(player.getDuration());
        replayRoomListener.videoPrepared();
    }

    /**
     * 切换清晰度
     *
     * @param quality quality
     */
    public void changeQuality(int quality, ReplayChangeSourceListener changeLineCallback) {
        DWLiveReplay.getInstance().changeQuality(quality, changeLineCallback);
    }

    /**
     * 切换线路
     *
     * @param lineIndex 线路索引
     */
    public void changeLine(int lineIndex, ReplayChangeSourceListener changeLineCallback) {
        DWLiveReplay.getInstance().changeLine(lineIndex, changeLineCallback);
    }

    /**
     * 切换播放模式
     *
     * @param playMode DWLive.PlayMode
     */
    public void changePlayMode(DWLiveReplay.PlayMode playMode, ReplayChangeSourceListener changeCallBack) {
        DWLiveReplay.getInstance().changePlayMode(playMode, changeCallBack);
    }


    //--------------------------------------记忆播放相关--------------------------------------------

    // 开启进度记忆
    public void startProRecord(Context context, ProRecordWorker.UICallback callback) {
        if (proRecordWorker != null) {
            proRecordWorker.stop();
        }
        proRecordWorker = new ProRecordWorker(context, callback);
        proRecordWorker.setPlayer(player);
        proRecordWorker.setRecordTag(recordTag);
        proRecordWorker.setTextView(proRecordJump);
        proRecordWorker.start();

    }

    public void stopProRecord() {
        if (proRecordWorker != null) {
            proRecordWorker.stop();
            proRecordWorker = null;
        }

    }

    public void clearProRecord() {
        if (proRecordWorker != null) {
            proRecordWorker.clear();
        }
    }


    //******************************* 实现 DWLiveListener 定义的方法 *********************************/

    private final DWLiveReplayListener dwLiveReplayListener = new DWLiveReplayListener() {
        @Override
        public void onPageDataError(int type, String msg) {
            DWLiveReplay.getInstance().reloadPageData();
        }

        @Override
        public void onException(DWLiveException exception) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onException:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
            if (replayRoomListener != null) {
                replayRoomListener.onException(exception);
            }
        }

        // 回放打点功能
        @Override
        public void onHDReplayDotList(List<ReplayDot> dotList) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onHDReplayDotList:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
            if (replayRoomListener != null) {
                replayRoomListener.onHDReplayDotList(dotList);
            }
        }

        @Override
        public void onMediaInfoPrepared() {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onMediaInfoPrepared:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
        }

        @Override
        public void onPageInfoList(ArrayList<ReplayPageInfo> infoList) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onPageInfoList:" + infoList.size()+",thread:"+Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
            if (replayRoomListener != null) {
                replayRoomListener.onPageInfoList(infoList);
            }

        }

        @Override
        public void onPageChange(String docId, String docName, int width, int height, int pageNum, int docTotalPage) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onPageChange:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
            Log.d(TAG, "onPageChange: pageNum:" + pageNum + " docTotalPage:" + docTotalPage + " docId=" + docId);
        }

        @Override
        public void onDataPrepared() {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onDataPrepared:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
        }

        @Override
        public void numberOfReceivedLines(int lines) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "numberOfReceivedLines:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
        }

        @Override
        public void numberOfReceivedLinesWithVideoAndAudio(List<ReplayLineParams> videoLines, List<ReplayLineParams> audioLines) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "numberOfReceivedLinesWithVideoAndAudio:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
        }

        @Override
        public void onHDAudioMode(DWLiveReplay.Audio hasAudio) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onHDAudioMode:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
            if (replayRoomListener != null) {
                replayRoomListener.onHDAudioMode(hasAudio);
            }
        }

        @Override
        public void onHDReceivedVideoQuality(List<ReplayQualityinfo> videoQuality, ReplayQualityinfo currentQuality) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onHDReceivedVideoQuality:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
            if (replayRoomListener != null) {
                replayRoomListener.onHDReceivedVideoQuality(videoQuality, currentQuality);
            }
        }

        @Override
        public void onHDReceivedVideoAudioLines(List<ReplayLineInfo> lines, int currentLineIndex) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onHDReceivedVideoAudioLines:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
            if (replayRoomListener != null) {
                replayRoomListener.onHDReceivedVideoAudioLines(lines, currentLineIndex);
            }
        }


        @Override
        public void onInitFinished() {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onInitFinished:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }


        }

        @Override
        public void onBroadCastMessage(ArrayList<ReplayBroadCastMsg> broadCastMsgList) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onBroadCastMessage:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
        }

        /**
         * 聊天信息
         *
         * @param replayChatMsgs 聊天信息
         */
        @Override
        public void onChatMessage(TreeSet<ReplayChatMsg> replayChatMsgs) {
//            if (BuildConfig.DEBUG) {
//                Log.i(TAG, "onChatMessage:" + Thread.currentThread().getName());
//                // 检查是否在主线程
//                ThreadUtils.checkIsOnMainThread();
//            }
//            if (replayChatListener != null) {
//                replayChatListener.onChatMessage(replayChatMsgs);
//            }
        }

        @Override
        public void onChatMessagePart(TreeSet<ReplayChatMsg> chatMsgs) {
            super.onChatMessagePart(chatMsgs);
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onChatMessage:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
            if (replayChatListener != null) {
                replayChatListener.onChatMessage(chatMsgs);
            }
        }


        /**
         * 提问回答信息
         *
         * @param qaMsgs 问答信息
         */
        @Override
        public void onQuestionAnswer(TreeSet<ReplayQAMsg> qaMsgs) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onQuestionAnswer:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
            if (replayQAListener != null) {
                replayQAListener.onQuestionAnswer(qaMsgs);
            }
        }

        @Override
        public void onHDReceivePracticeList(List<ReplayPracticeInfo> list) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onHDReceivePracticeList:" + Thread.currentThread().getName());
                // 检查是否在主线程
                ThreadUtils.checkIsOnMainThread();
            }
            if (replayRoomListener != null) {
                replayRoomListener.onHDReceivePracticeList(list);
            }
        }
    };


    /**
     * 当翻页失败时，调用此方法可以刷新翻页，方法调用间隔需大于3s
     */
    public void reloadPageChange() {
        Log.i(TAG,"reloadPageChange");
        try{
            DWLiveReplay.getInstance().reloadPageChange();
        }catch (Exception e){
             e.printStackTrace();
        }
    }
}
