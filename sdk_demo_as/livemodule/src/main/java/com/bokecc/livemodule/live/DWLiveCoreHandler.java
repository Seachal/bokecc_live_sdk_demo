package com.bokecc.livemodule.live;

import android.util.Log;

import com.bokecc.livemodule.BuildConfig;
import com.bokecc.livemodule.live.chat.module.ChatEntity;
import com.bokecc.livemodule.utils.ThreadUtils;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.bokecc.sdk.mobile.live.DWLiveListener;
import com.bokecc.sdk.mobile.live.DWLivePlayer;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.Exception.ErrorCode;
import com.bokecc.sdk.mobile.live.listener.LiveChangeSourceListener;
import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.BanChatBroadcast;
import com.bokecc.sdk.mobile.live.pojo.BroadCastAction;
import com.bokecc.sdk.mobile.live.pojo.BroadCastMsg;
import com.bokecc.sdk.mobile.live.pojo.ChatMessage;
import com.bokecc.sdk.mobile.live.pojo.LiveInfo;
import com.bokecc.sdk.mobile.live.pojo.LiveLineInfo;
import com.bokecc.sdk.mobile.live.pojo.LiveQualityInfo;
import com.bokecc.sdk.mobile.live.pojo.LotteryAction;
import com.bokecc.sdk.mobile.live.pojo.PracticeInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeRankInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeStatisInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeSubmitResultInfo;
import com.bokecc.sdk.mobile.live.pojo.PrivateChatInfo;
import com.bokecc.sdk.mobile.live.pojo.Question;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireInfo;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireStatisInfo;
import com.bokecc.sdk.mobile.live.pojo.RoomDocInfo;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;
import com.bokecc.sdk.mobile.live.pojo.SettingInfo;
import com.bokecc.sdk.mobile.live.pojo.TeacherInfo;
import com.bokecc.sdk.mobile.live.pojo.UserRedminAction;
import com.bokecc.sdk.mobile.live.pojo.Viewer;
import com.bokecc.sdk.mobile.live.rtc.CCRTCRender;
import com.bokecc.sdk.mobile.live.rtc.RtcClient;
import com.bokecc.sdk.mobile.live.widget.DocView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 直播相关逻辑核心处理机制
 */
public class DWLiveCoreHandler {

    private static final String TAG = DWLiveCoreHandler.class.getSimpleName();
    private static final DWLiveCoreHandler dwLiveCoreHandler = new DWLiveCoreHandler();

    private int retryCount = 0;


    /**
     * 获取DWLiveCoreHandler单例的实例
     *
     * @return dwLiveCoreHandler
     */
    public static DWLiveCoreHandler getInstance() {
        return dwLiveCoreHandler;
    }

    /**
     * 私有构造函数
     */
    private DWLiveCoreHandler() {

    }

    /******************************* 各类功能模块监听相关 ***************************************/

    private DWLiveQAListener dwLiveQAListener;

    /**
     * 设置问答监听
     */
    public void setDwLiveQAListener(DWLiveQAListener listener) {
        dwLiveQAListener = listener;
    }

    private DWLiveChatListener dwLiveChatListener;

    /**
     * 设置聊天监听
     */
    public void setDwLiveChatListener(DWLiveChatListener listener) {
        dwLiveChatListener = listener;
    }

    private DWLiveRoomListener dwLiveRoomListener;

    /**
     * 设置直播间信息监听
     */
    public void setDwLiveRoomListener(DWLiveRoomListener dwLiveRoomListener) {
        this.dwLiveRoomListener = dwLiveRoomListener;
    }

    private DWLiveVideoListener dwLiveVideoListener;

    /**
     * 设置直播视频监听
     */
    public void setDwLiveVideoListener(DWLiveVideoListener dwLiveVideoListener) {
        this.dwLiveVideoListener = dwLiveVideoListener;
    }

    private DWLiveFunctionListener dwLiveFunctionListener;

    /**
     * 设置直播功能监听（签到、答题卡/投票、问卷、抽奖）
     */
    public void setDwLiveFunctionListener(DWLiveFunctionListener dwLiveFunctionListener) {
        this.dwLiveFunctionListener = dwLiveFunctionListener;
    }

    private DWLiveMoreFunctionListener dwLiveMoreFunctionListener;

    /**
     * 设置直播更多功能监听（公告、连麦、私聊）
     */
    public void setDwLiveMoreFunctionListener(DWLiveMoreFunctionListener dwLiveMoreFunctionListener) {
        this.dwLiveMoreFunctionListener = dwLiveMoreFunctionListener;
    }

    private DWLiveRTCListener dwLiveRTCListener;

    /**
     * 设置直播连麦监听 -- 用于视频和连麦画面展示
     */
    public void setDwLiveRTCListener(DWLiveRTCListener dwLiveRTCListener) {
        this.dwLiveRTCListener = dwLiveRTCListener;
    }

    private DWLiveRTCStatusListener dwLiveRTCStatusListener;

    /**
     * 设置直播连麦状态监听 -- 用于连麦控制控件展示
     */
    public void setDwLiveRTCStatusListener(DWLiveRTCStatusListener dwLiveRTCStatusListener) {
        this.dwLiveRTCStatusListener = dwLiveRTCStatusListener;
    }

    private UserListener userListener;

    /**
     * 防止房间内学员退出和进入监听
     *
     * @param userListener
     */
    public void setUserListener(UserListener userListener) {
        this.userListener = userListener;
    }

    /******************************* 设置"播放"组件/控件相关 ***************************************/


    private DWLivePlayer dwLivePlayer;

    private DocView docView;

    /**
     * 设置播放器
     *
     * @param player 播放器
     */
    public void setPlayer(DWLivePlayer player) {
        this.dwLivePlayer = player;
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.setDWLivePlayer(this.dwLivePlayer);
        }
    }

    /**
     * 设置文档展示控件
     *
     * @param docView 文档展示控件
     */
    public void setDocView(DocView docView) {
        this.docView = docView;
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.setDWLivePlayDocView(this.docView);
        }
    }

    public DWLivePlayer getPlayer() {
        return dwLivePlayer;
    }

    /******************************* 直播间模版状态相关 ***************************************/

    private final static String ONLY_VIDEO_TEMPLATE_TYPE = "1";

    /**
     * 当前直播间是否有'文档'
     */
    public boolean hasPdfView() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getTemplateInfo() != null) {
            return dwLive.getTemplateInfo().hasDoc();
        }
        return false;
    }

    /**
     * 当前直播间是否有'聊天'
     */
    public boolean hasChatView() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getTemplateInfo() != null) {
            return dwLive.getTemplateInfo().hasChat();
        }
        return false;
    }

    /**
     * 当前直播间是否有'问答'
     */
    public boolean hasQaView() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getTemplateInfo() != null) {
            return dwLive.getTemplateInfo().hasQa();
        }
        return false;
    }

    /**
     * 当前模版是否只有视频(大屏模式-->视频)
     * <p>
     * 注：<大屏模式-->视频>的TemplateInfo.type == 1
     *
     * @return true 为是，false为否
     */
    public boolean isOnlyVideoTemplate() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getTemplateInfo() != null) {
            return ONLY_VIDEO_TEMPLATE_TYPE.equals(dwLive.getTemplateInfo().getType());
        }
        return false;
    }

    /**
     * 是否显示用户人数
     *
     * @return true 显示 false 不显示
     */
    public boolean isShowUserCount() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getRoomInfo() != null) {
            return dwLive.getRoomInfo().getShowUserCount() == 1;
        }
        return false;
    }

    /**
     * 是否显示清晰度
     *
     * @return true 显示 false 不显示
     */
    public boolean isShowQuality() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getRoomInfo() != null) {
            return (dwLive.getRoomInfo().getMultiQuality() == 1);
        }
        return false;
    }

    /**
     * 是否开启跑马灯
     *
     * @return true 显示 false 不显示
     */
    public boolean isOpenMarquee() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getRoomInfo() != null) {
            return dwLive.getRoomInfo().getOpenMarquee() == 1;
        }
        return false;
    }

    /**
     * 是否开启弹幕
     *
     * @return true 显示 false 不显示
     */
    public boolean isOpenBarrage() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getRoomInfo() != null) {
            return dwLive.getRoomInfo().getBarrage() == 1;
        }
        return false;
    }


    //******************************* 直播SDK视频生命周期控制相关 ***************************************/

    /**
     * 开始播放
     */
    public void start() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            // 设置播放参数 （回调 及 上下文）
            dwLive.setDWLivePlayParams(dwLiveListener, DWLiveEngine.getInstance().getContext());
            // 调用开始播放逻辑
            dwLive.start();
            // 获取随堂测统计信息
            DWLive.getInstance().getPracticeStatis("");
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.stop();
        }
    }

    /**
     * 释放资源
     */
    public void destroy() {
        dwLiveQAListener = null;
        dwLiveChatListener = null;
        dwLiveVideoListener = null;
        dwLiveFunctionListener = null;
        dwLiveMoreFunctionListener = null;
        dwLiveRTCListener = null;
        dwLiveRTCStatusListener = null;
        dwLiveRoomListener = null;
        userListener = null;
        isRtcing = false;
        if (docView != null) {
            docView = null;
        }
        if (dwLivePlayer != null) {
            dwLivePlayer = null;
        }
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.onDestroy();
        }
    }

    /**
     * 切换清晰度
     *
     * @param quality quality
     */
    public void changeQuality(int quality, LiveChangeSourceListener changeLineCallback) {
        DWLive.getInstance().changeQuality(quality, changeLineCallback);
    }

    /**
     * 切换线路
     *
     * @param lineIndex 线路索引
     */
    public void changeLine(int lineIndex, LiveChangeSourceListener changeLineCallback) {
        DWLive.getInstance().changeLine(lineIndex, changeLineCallback);
    }

    /**
     * 切换播放模式
     *
     * @param playMode DWLive.PlayMode
     */
    public void changePlayMode(DWLive.LivePlayMode playMode, LiveChangeSourceListener changeCallBack) {
        DWLive.getInstance().changePlayMode(playMode, changeCallBack);
    }

    // 更新视频展示界面UI
    public void updatePlayModeUI(DWLive.LivePlayMode playMode) {
        if (dwLiveVideoListener != null) {
            dwLiveVideoListener.onChangePlayMode(playMode);
        }

    }

    //----------------------------------- 直播SDK(DWLive)提供的的主动调用方法 -----------------------/


    /**
     * 获取直播信息
     *
     * @return 直播信息 其中：LiveInfo.liveStartTime 为直播开始时间，LiveInfo.liveDuration "直播持续时间，单位（s），直播未开始返回-1"
     */
    public LiveInfo getLiveInfo() {
        return DWLive.getInstance().getLiveInfo();
    }

    public Viewer getViewer() {
        return DWLive.getInstance().getViewer();
    }

    public RoomInfo getRoomInfo() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            return dwLive.getRoomInfo();
        }
        return null;
    }

    /**
     * 发送签到信息
     */
    public void sendRollCall() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.sendRollCall();
        }
    }

    private Map<String, ArrayList<Integer>> practiceResultIndexs;  // 随堂测答题结果列表

    /**
     * 发送随堂测答题结果
     *
     * @param practiceId    随堂测ID
     * @param answerOptions 随堂测回答的结果信息
     */
    public void sendPracticeAnswer(String practiceId, ArrayList<String> answerOptions) {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.sendPracticeAnswer(practiceId, answerOptions);
        }
    }

    // 缓存随堂测答题记录
    public void cachePracticeResult(String practiceId, ArrayList<Integer> resultIndexs) {
        if (practiceResultIndexs == null) {
            practiceResultIndexs = new HashMap<>();
        }
        practiceResultIndexs.put(practiceId, resultIndexs);
    }

    /**
     * 获取随堂测答题记录（根据随堂测ID）
     *
     * @param practiceId 随堂测ID
     */
    public ArrayList<Integer> getPracticeResult(String practiceId) {
        if (practiceResultIndexs == null) {
            return null;
        }
        return practiceResultIndexs.get(practiceId);
    }


    //--------------------------------- 模块间UI事件处理方法(中转事件) -----------------------------------/

    /**
     * 跳转到私聊列表页
     *
     * @param chatEntity 要跳转到的私聊的内容指引
     */
    public void jump2PrivateChat(ChatEntity chatEntity) {
        if (dwLiveMoreFunctionListener != null) {
            dwLiveMoreFunctionListener.jump2PrivateChat(chatEntity);
        }
    }

    /**
     * 显示播放错误界面
     *
     * @param msg 播放错误信息
     */
    public void showError(String msg) {
        if (dwLiveRoomListener != null) {
            dwLiveRoomListener.showError(msg);
        }

    }

    public void retryVideoPlay(){
        if (dwLiveRoomListener!=null){
            dwLiveRoomListener.callRetryAndHideErrorView();
        }
    }

    /******************************* 实现直播相关功能事件监听 ***************************************/

    private final DWLiveListener dwLiveListener = new DWLiveListener() {

        /**
         * 收到房间配置信息
         * @param info SettingInfo
         */
        @Override
        public void onRoomSettingInfo(SettingInfo info) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onRoomSettingInfo:" + Thread.currentThread().getName() + "," + info.toString());
                ThreadUtils.checkIsOnMainThread();
            }

        }

        /**
         * 后台配置切换视频文档区域
         *
         * @param isVideoMain 视频是否为主区域
         */
        @Override
        public void onSwitchVideoDoc(boolean isVideoMain) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onSwitchVideoDoc:" + Thread.currentThread().getName() + ",isVideoMain = " + isVideoMain);
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.onSwitchVideoDoc(isVideoMain);
            }
        }


        /**
         * 回调老师列表
         * @param infoList List<TeacherInfo>
         */
        @Override
        public void onOnlineTeachers(List<TeacherInfo> infoList) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onOnlineTeachers:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
        }

        @Override
        public void onHistoryQuestionAnswer(List<Question> questions, List<Answer> answers) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onHistoryQuestionAnswer:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveQAListener != null) {
                dwLiveQAListener.onHistoryQuestionAnswer(questions, answers);
            }
        }

        /**
         * 提问
         *
         * @param question 问题信息
         */
        @Override
        public void onQuestion(Question question) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onQuestion:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveQAListener != null) {
                dwLiveQAListener.onQuestion(question);
            }
        }

        /**
         * 收到客户端发布的问题的编号
         *
         * @param questionId 问题id
         */
        @Override
        public void onPublishQuestion(String questionId) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPublishQuestion:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveQAListener != null) {
                dwLiveQAListener.onPublishQuestion(questionId);
            }
        }

        /**
         * 回答
         *
         * @param answer 回答信息
         */
        @Override
        public void onAnswer(Answer answer) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onAnswer:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveQAListener != null) {
                dwLiveQAListener.onAnswer(answer);
            }
        }

        /**
         * 直播的播放状态
         *
         * @param status 包括PLAYING, PREPARING共2种状态
         */
        @Override
        public void onLiveStatus(DWLive.PlayStatus status) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLiveStatus:" + Thread.currentThread().getName() + "," + status.toString());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveVideoListener != null) {
                dwLiveVideoListener.onLiveStatus(status);
            }
        }

        /**
         * 流结束
         *
         * @param isNormal 流是否正常结束
         */
        @Override
        public void onStreamEnd(boolean isNormal) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onStreamEnd:" + Thread.currentThread().getName() + ",isNormal:" + isNormal);
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveVideoListener != null) {
                dwLiveVideoListener.onStreamEnd(isNormal);
            }
            if (dwLiveRoomListener != null) {
                String reason = isNormal ? "直播已结束" : "直播未开始";
                dwLiveRoomListener.onStreamEnd(isNormal, reason);
            }
        }

        @Override
        public void onStreamStart() {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onStreamStart:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveVideoListener != null) {
                dwLiveVideoListener.onStreamStart();
            }
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.onStreamStart();
            }
        }

        /**
         * 收到直播历史公聊
         *
         * @param chatLogs 补推的历史聊天信息
         */
        @Override
        public void onHistoryChatMessage(ArrayList<ChatMessage> chatLogs) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onHistoryChatMessage:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onHistoryChatMessage(chatLogs);
            }
        }

        /**
         * 公共聊天
         *
         * @param msg 聊天信息
         */
        @Override
        public void onPublicChatMessage(ChatMessage msg) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPublicChatMessage:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onPublicChatMessage(msg);
            }
        }

        /**
         * 收到聊天信息状态管理事件
         *
         * @param msgStatusJson 聊天信息状态管理事件json
         */
        @Override
        public void onChatMessageStatus(String msgStatusJson) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onChatMessageStatus:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onChatMessageStatus(msgStatusJson);
            }
        }

        /**
         * 禁言消息，该消息是单个用户被禁言情况下发送消息的回调
         *
         * @param msg 聊天信息
         */
        @Override
        public void onSilenceUserChatMessage(ChatMessage msg) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onSilenceUserChatMessage:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onSilenceUserChatMessage(msg);
            }
        }

        /**
         * 收到禁言事件
         *
         * @param mode 禁言类型 1：个人禁言  2：全员禁言
         */
        @Override
        public void onBanChat(int mode) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onBanChat:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
                Log.d(TAG, "onBanChat:mode" + mode);
            }
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onBanChat(mode);
            }
        }

        /**
         * 收到解除禁言事件
         *
         * @param mode 禁言类型 1：个人禁言  2：全员禁言
         */
        @Override
        public void onUnBanChat(int mode) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onUnBanChat:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
                Log.d(TAG, "onUnBanChat:mode" + mode);
            }
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onUnBanChat(mode);
            }
        }

        @Override
        public void onBanDeleteChat(String userId) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onBanDeleteChat:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
                Log.d(TAG, "onBanDeleteChat:userId" + userId);
            }
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onBanDeleteChat(userId);
            }
        }

        /**
         * 别人私聊我
         *
         */
        @Override
        public void onPrivateChat(PrivateChatInfo info) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPrivateChat:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveMoreFunctionListener != null) {
                dwLiveMoreFunctionListener.onPrivateChat(info);
            }
        }

        /**
         * 我发出的私聊
         *
         */
        @Override
        public void onPrivateChatSelf(PrivateChatInfo info) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPrivateChatSelf:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveMoreFunctionListener != null) {
                dwLiveMoreFunctionListener.onPrivateChatSelf(info);
            }
        }

        /**
         * 在线人数<br/>
         * 刷新频率：15秒
         *
         * @param count 人数统计
         */
        @Override
        public void onUserCountMessage(int count) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onUserCountMessage:" + Thread.currentThread().getName() + ",userCount:" + count);
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.showRoomUserNum(count);
            }
        }


        /**
         * 回调当前翻页的信息<br/>
         * 注意：<br/>
         * 白板docTotalPage一直为0，pageNum从1开始<br/>
         * 其他文档docTotalPage为正常页数，pageNum从0开始
         *
         * @param docId        文档Id
         * @param docName      文档名称
         * @param pageNum      当前页码
         * @param docTotalPage 当前文档总共的页数
         */
        @Override
        public void onPageChange(String docId, String docName, int width, int height, int pageNum, int docTotalPage) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPageChange:" + Thread.currentThread().getName() + ",docId:" + docId + ",docName:" + docName + ",pageNum:" + pageNum + ",docTotalPage:" + docTotalPage);
                ThreadUtils.checkIsOnMainThread();
            }

            currentDocId = docId;
            currentPageNum = pageNum;
            totalPageNum = docTotalPage;
        }

        /**
         * 通知
         *
         */
        @Override
        public void onNotification(String msg) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onNotification:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }

        }

        /**
         * 切换数据源
         *
         * @param switchInfo 切换数据源信息 <br>
         * 注：<br>
         * 1. 返回数据格式为：{"source_type":"10","source_type_desc":"数据源类型：摄像头打开"} <br>
         * 2. 目前此回调只会在有文档的直播间模版下才会触发
         */
        @Override
        public void onSwitchSource(String switchInfo) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onSwitchSource:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
        }


        /**
         * 收到历史广播信息(目前服务端只返回最后一条历史广播)
         *
         * @param msgs 广播消息列表
         */
        @Override
        public void onHistoryBroadcastMsg(ArrayList<BroadCastMsg> msgs) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onHistoryBroadcastMsg:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveChatListener != null) {
                // 判断空
                if (msgs == null) {
                    return;
                }
                // 展示历史广播信息,这里以前是单条处理，这里修改为多条处理
                dwLiveChatListener.onHistoryBroadcastMsg(msgs);

            }
        }


        /**
         * 收到广播信息（实时）
         *
         * @param msg 广播消息 包含id
         */
        @Override
        public void onBroadcastMsg(BroadCastMsg msg) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onBroadcastMsg2:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onBroadcastMsg(msg);
            }

        }

        /**
         * 收到广播信息操作信息
         *
         * @param action 广播消息 根据id操作广播消息
         */
        @Override
        public void onBroadcastMsgAction(BroadCastAction action) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onBroadcastMsgAction:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveChatListener != null) {
                // 执行删除操作
                if (action.getAction() == 1) {
                    dwLiveChatListener.onBroadcastMsgDel(action.getId());
                }

            }
        }

        /**
         * 信息，一般包括被禁言等
         *
         */
        @Override
        public void onInformation(String msg) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onInformation:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.onInformation(msg);
            }
        }

        /**
         * 异常的回调
         *
         * @param exception  DWLiveException
         */
        @Override
        public void onException(DWLiveException exception) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onException:" + Thread.currentThread().getName() + "," + exception.toString());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                if (exception.getErrorCode() == ErrorCode.GET_PLAY_URL_FAILED) {
                    dwLiveFunctionListener.onException("获取播放地址失败:" + exception.getMessage());
                    showError("网络异常，请重试");
                } else if (exception.getErrorCode() == ErrorCode.GET_HISTORY_FAILED) {
                    dwLiveFunctionListener.onException("获取历史信息失败:" + exception.getMessage());
                    //展示播放失败tips

                    //获取历史信息失败，重试播放
                    //重试3次
//                    if (retryCount<3){
//                        retryCount++;
//                        DWLive.getInstance().start();
//                    }
                } else if (exception.getErrorCode() == ErrorCode.DOC_PAGE_INFO_FAILED) {
                    dwLiveFunctionListener.onException("文档加载失败");
                } else if (exception.getErrorCode() == ErrorCode.CONNECT_SERVICE_FAILED) {
                    dwLiveFunctionListener.onException("连接socket服务器异常:" + exception.getMessage());
                } else {
                    dwLiveFunctionListener.onException("unknown error:" + exception.getMessage());
                }
            }
        }

        /**
         * 用户被踢出房间的回调
         *
         * @param type 踢出房间的类型<br>
         * 10:在允许重复登录前提下，后进入者会登录会踢出先前登录者<br>
         * 20:讲师、助教、主持人通过页面踢出按钮踢出用户
         */
        @Override
        public void onKickOut(int type) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onKickOut:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.onKickOut();
            }
        }

        /**
         * 回调已播放时长
         *
         * @param playedTime 已播放时长，如果未开始，则时间为-1
         */
        @Override
        public void onLivePlayedTime(int playedTime) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLivePlayedTime:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
        }

        /**
         * 获取已播放时长请求异常
         *
         * @param exception 获取已播放时长异常
         */
        @Override
        public void onLivePlayedTimeException(Exception exception) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLivePlayedTimeException:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
        }

        /**
         * 是否是时移播放
         *
         */
        @Override
        public void isPlayedBack(boolean isPlayedBack) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "isPlayedBack:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
        }


        /**
         * 定制聊天
         *
         * @param customMessage customMessage
         */
        @Override
        public void onCustomMessage(String customMessage) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onCustomMessage:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
                Log.d(TAG, "onCustomMessage:" + customMessage);
            }
        }

        /**
         * 禁播
         *
         * @param reason 禁播原因
         */
        @Override
        public void onBanStream(String reason) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onBanStream:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
                Log.d(TAG, "onBanStream:reason" + reason);
            }
            if (dwLiveVideoListener != null) {
                dwLiveVideoListener.onBanStream(reason);
            }
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.onStreamEnd(true, reason);
            }
        }

        /**
         * 解禁
         */
        @Override
        public void onUnbanStream() {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onUnbanStream:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveVideoListener != null) {
                dwLiveVideoListener.onUnbanStream();
            }
        }

        @Override
        public void onInitFinished() {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onInitFinished:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveRoomListener != null && DWLive.getInstance().getRoomInfo() != null) {
                dwLiveRoomListener.showRoomTitle(DWLive.getInstance().getRoomInfo().getName());
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onInitFinished();
            }

        }

        @Override
        public void onHDAudioMode(DWLive.LiveAudio hasAudio) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onHDAudioMode:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.onHDAudioMode(hasAudio);
            }
        }

        @Override
        public void onHDReceivedVideoQuality(List<LiveQualityInfo> videoQuality, LiveQualityInfo currentQuality) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onHDReceivedVideoQuality:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.onHDReceivedVideoQuality(videoQuality, currentQuality);
            }
        }

        @Override
        public void onHDReceivedVideoAudioLines(List<LiveLineInfo> lines, int indexNum) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onHDReceivedVideoAudioLines:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.onHDReceivedVideoAudioLines(lines, indexNum);
            }
        }

        /**
         * 公告
         *
         * @param isRemove     是否是公告删除，如果为true，表示公告删除且announcement参数为null
         * @param announcement 公告内容
         */
        @Override
        public void onAnnouncement(boolean isRemove, String announcement) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onAnnouncement:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveMoreFunctionListener != null) {
                dwLiveMoreFunctionListener.onAnnouncement(isRemove, announcement);
            }
        }

        /**
         * 签到回调
         *
         * @param duration 签到持续时间，单位为秒
         */
        @Override
        public void onRollCall(int duration) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onRollCall:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onRollCall(duration);
            }
        }

        /**
         * 开始抽奖
         *
         * @param lotteryId 本次抽奖的id
         */
        @Override
        public void onStartLottery(String lotteryId) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onStartLottery:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onStartLottery(lotteryId);
            }
        }

        /**
         * 抽奖结果
         *
         * @param isWin       是否中奖，true表示中奖了
         * @param lotteryCode 中奖码
         * @param lotteryId   本次抽奖的id
         * @param winnerName  中奖者的名字
         */
        @Override
        public void onLotteryResult(boolean isWin, String lotteryCode, String lotteryId, String winnerName) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLotteryResult:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onLotteryResult(isWin, lotteryCode, lotteryId, winnerName);
            }
        }

        /**
         * 结束抽奖
         *
         * @param lotteryId 本次抽奖的id
         */
        @Override
        public void onStopLottery(String lotteryId) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onStopLottery:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onStopLottery(lotteryId);
            }
        }

        /**
         * 抽奖2.0
         */
        @Override
        public void onLottery(LotteryAction lotteryAction) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLottery:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onLottery(lotteryAction);
            }
        }

        /**
         * 开始投票
         *
         * @param voteCount 总共的选项个数2-5
         * @param VoteType  0表示单选，1表示多选，目前只有单选
         */
        @Override
        public void onVoteStart(int voteCount, int VoteType) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onVoteStart:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onVoteStart(voteCount, VoteType);
            }
        }

        /**
         * 结束投票
         */
        @Override
        public void onVoteStop() {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onVoteStop:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onVoteStop();
            }
        }

        /**
         * 投票结果统计
         *
         * @param jsonObject 投票结果数据
         */
        @Override
        public void onVoteResult(JSONObject jsonObject) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onVoteResult:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onVoteResult(jsonObject);
            }
        }

        /**
         * 收到奖品发送事件
         *
         * @param type       奖品类型: 1 奖杯 2 其他(后续扩展使用)
         * @param viewerId   观看者的id
         * @param viewerName 观看者的昵称
         */
        @Override
        public void onPrizeSend(int type, String viewerId, String viewerName) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPrizeSend:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPrizeSend(type, viewerId, viewerName);
            }
        }

        /**
         * 发布问卷
         *
         * @param info 问卷内容
         */
        @Override
        public void onQuestionnairePublish(QuestionnaireInfo info) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onQuestionnairePublish:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onQuestionnairePublish(info);
            }
        }

        /**
         * 停止问卷
         *
         * @param questionnaireId 问卷Id
         */
        @Override
        public void onQuestionnaireStop(String questionnaireId) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onQuestionnaireStop:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onQuestionnaireStop(questionnaireId);
            }
        }

        /**
         * 问卷统计信息
         *
         */
        @Override
        public void onQuestionnaireStatis(QuestionnaireStatisInfo info) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onQuestionnaireStatis:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onQuestionnaireStatis(info);
            }
        }

        /**
         * 发布第三方问卷
         *
         * @param title       问卷标题
         * @param externalUrl 第三方问卷链接
         */
        @Override
        public void onExeternalQuestionnairePublish(String title, String externalUrl) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onExeternalQuestionnairePublish:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onExeternalQuestionnairePublish(title, externalUrl);
            }
        }

        /**
         * 发布随堂测
         *
         * @param info 随堂测内容
         */
        @Override
        public void onPracticePublish(PracticeInfo info) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPracticePublish:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticePublish(info);
            }
        }

        /**
         * 收到随堂测提交结果
         *
         * @param info 随堂测结果
         */
        @Override
        public void onPracticeSubmitResult(PracticeSubmitResultInfo info) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPracticeSubmitResult:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticeSubmitResult(info);
            }
        }

        /**
         * 收到随堂测统计信息
         *
         * @param info 随堂测排名信息
         */
        @Override
        public void onPracticStatis(PracticeStatisInfo info) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPracticStatis:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticStatis(info);
            }
        }

        @Override
        public void onPracticRanking(PracticeRankInfo info) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPracticRanking:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticRanking(info);
            }
        }

        /**
         * 收到停止随堂测
         *
         * @param practiceId 随堂测ID
         */
        @Override
        public void onPracticeStop(String practiceId) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPracticeStop:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticeStop(practiceId);
            }
        }

        /**
         * 收到关闭随堂测
         *
         * @param practiceId 随堂测ID
         */
        @Override
        public void onPracticeClose(String practiceId) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPracticeClose:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticeClose(practiceId);
            }
        }

        /**
         *    禁言通知,通知给直播间所有人某用户被禁言
         * @param banChatBroadcast
         *    userId 用户id
         *    userName 用户名
         *    userRole 用户角色
         *    userAvatar 用户头像
         *    groupId 分组id
         */
        @Override
        public void HDBanChatBroadcastWithData(BanChatBroadcast banChatBroadcast) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "HDBanChatBroadcastWithData:" + Thread.currentThread().getName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (dwLiveChatListener != null) {
                dwLiveChatListener.HDBanChatBroadcastWithData(banChatBroadcast);
            }
        }

        /**
         *  用户进出通知
         * {@link UserRedminAction.ActionType#HDUSER_IN_REMIND} 进入直播间
         * {@link UserRedminAction.ActionType#HDUSER_OUT_REMIND}   退出直播间
         *
         * @param userJoinExitAction
         *    userId 用户id
         *    userName 用户名
         *    userRole 用户角色
         *    userAvatar 用户头像
         *    groupId 分组id
         *    role 接收端列表  1-讲师；2-助教；3-主持人；4-观看端
         *    content 自定义内容
         */
        @Override
        public void HDUserRemindWithAction(UserRedminAction userJoinExitAction) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "HDUserRemindWithAction:" + Thread.currentThread().getName() + ",userId = " + userJoinExitAction.getUserId() + ",userName=" + userJoinExitAction.getUserName());
                ThreadUtils.checkIsOnMainThread();
            }
            if (userListener != null) {
                userListener.HDUserRemindWithAction(userJoinExitAction);
            }
        }
    };


    //************************************ 直播连麦模块 ***************************************/


    public void initRtc(CCRTCRender localRender, CCRTCRender remoteRender) {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.setRtcClientParameters(rtcClientListener, localRender, remoteRender);
        }
    }


    private boolean isAllowRtc = false;

    /**
     * 当前是否允许连麦
     */
    public boolean isAllowRtc() {
        return isAllowRtc;
    }


    private boolean isRtcing = false;
    private boolean mIsVideoRtc = false;

    /**
     * 当前是否正在连麦中
     */
    public boolean isRtcing() {
        return isRtcing;
    }

    /**
     * 当前连麦是否是视频连麦
     *
     * @return true 是 false 否
     */
    public boolean isVideoRtc() {
        return mIsVideoRtc;
    }

    /**
     * 申请连麦
     *
     * @param videoRtc 是否为视频连麦
     */
    public void startRTCConnect(boolean videoRtc) {
        // 判断当前是否允许连麦，如果不允许，则不继续做任何操作
        if (!isAllowRtc) {
            return;
        }
        if (videoRtc) {
            DWLive.getInstance().startRtcConnect();
        } else {
            DWLive.getInstance().startVoiceRTCConnect();
        }
    }

    /**
     * 取消连麦申请
     */
    public void cancelRTCConnect() {
        DWLive.getInstance().disConnectApplySpeak();
        isRtcing = false;
        // 关闭
        if (dwLiveRTCStatusListener != null) {
            dwLiveRTCStatusListener.onCloseSpeak();
        }
    }

    //------------------------------ 实现直播连麦功能事件监听 --------------------------------/

    private final RtcClient.RtcClientListener rtcClientListener = new RtcClient.RtcClientListener() {

        @Override
        public void onAllowSpeakStatus(final boolean isAllowSpeak) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onAllowSpeakStatus:" + Thread.currentThread().getName() + ",isAllowSpeak = " + isAllowSpeak);
                ThreadUtils.checkIsOnMainThread();
            }
            isAllowRtc = isAllowSpeak;

            if (!isAllowSpeak) {
                if (dwLiveRTCStatusListener != null) {
                    dwLiveRTCStatusListener.onCloseSpeak();
                }
            }
        }

        /**
         * 主播端接通连麦,开始
         * @param videoSize 视频的宽高，值为"600x400"
         */
        @Override
        public void onEnterSpeak(boolean isVideoRtc, boolean needAdjust, String videoSize) {
            mIsVideoRtc = isVideoRtc;
            isRtcing = true;
            if (dwLiveRTCListener != null) {
                dwLiveRTCListener.onEnterSpeak(isVideoRtc, needAdjust, videoSize);
            }
            if (dwLiveRTCStatusListener != null) {
                dwLiveRTCStatusListener.onEnterRTC(isVideoRtc);
            }

        }

        @Override
        public void onDisconnectSpeak() {
            if (dwLiveRTCListener != null && isRtcing) {
                dwLiveRTCListener.onDisconnectSpeak();
            }
            if (dwLiveRTCStatusListener != null) {
                dwLiveRTCStatusListener.onExitRTC();
            }
            isRtcing = false;
        }

        @Override
        public void onSpeakError(final Exception e) {
            if (dwLiveRTCListener != null) {
                dwLiveRTCListener.onSpeakError(e);
            }
            if (dwLiveRTCStatusListener != null) {
                dwLiveRTCStatusListener.onExitRTC();
            }
            isRtcing = false;
        }

        @Override
        public void onCameraOpen(final int width, final int height) {

        }
    };

    //------------------------------ 自由翻页相关 --------------------------------/
    private String currentDocId;
    private int currentPageNum;
    private int totalPageNum;

    // 获取当前DocId
    public String getCurrentDocId() {
        return currentDocId;
    }

    // 获取当前翻页
    public int getCurrentPageNum() {
        return currentPageNum;
    }

    // 获取翻页总数
    public int getTotalPageNum() {
        return totalPageNum;
    }
    public int getDocTotalPageNumByDocId(String docId) {
        HashMap<String, RoomDocInfo> map = DWLive.getInstance().getRoomDocInfos();
        if (map!=null&&map.size()>0){
            if (map.containsKey(docId)){
                RoomDocInfo roomDocInfo = map.get(docId);
                if (roomDocInfo!=null){
                    return roomDocInfo.getDocTotalPage();
                }
            }
        }
        return 0;
    }
}
