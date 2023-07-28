package com.bokecc.livemodule.live.room;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.DWLiveRoomListener;
import com.bokecc.livemodule.live.chat.KeyboardHeightObserver;
import com.bokecc.livemodule.live.chat.adapter.EmojiAdapter;
import com.bokecc.livemodule.live.chat.barrage.BarrageLayout;
import com.bokecc.livemodule.live.chat.util.DensityUtil;
import com.bokecc.livemodule.live.chat.util.EmojiUtil;
import com.bokecc.livemodule.live.chat.window.BanChatPopup;
import com.bokecc.livemodule.live.room.rightview.LiveRightView;
import com.bokecc.livemodule.popup.SettingPopupWindow;
import com.bokecc.livemodule.popup.callback.SettingPopupInterface;
import com.bokecc.livemodule.popup.callback.SettingPopupWindowCallBack;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.livemodule.view.BaseRelativeLayout;
import com.bokecc.livemodule.view.CustomToast;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.OnMarqueeImgFailListener;
import com.bokecc.sdk.mobile.live.listener.LiveChangeSourceListener;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.bokecc.sdk.mobile.live.pojo.LiveLineInfo;
import com.bokecc.sdk.mobile.live.pojo.LiveQualityInfo;
import com.bokecc.sdk.mobile.live.pojo.Marquee;
import com.bokecc.sdk.mobile.live.util.NetworkUtils;
import com.bokecc.sdk.mobile.live.widget.MarqueeView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.view.animation.Animation.RESTART;
import static com.bokecc.livemodule.live.room.rightview.LiveBarrageView.BARRAGE_FULL;
import static com.bokecc.livemodule.live.room.rightview.LiveRightView.RIGHT_SHOW_BARRAGE;

/**
 * 直播间控制
 */
public class LiveRoomLayout extends BaseRelativeLayout implements DWLiveRoomListener, KeyboardHeightObserver, SettingPopupInterface {

    private final static String TAG = LiveRoomLayout.class.getSimpleName();
    private Activity activity;
    // ----------顶部布局---------
    private RelativeLayout mTopLayout;
    private TextView mLiveTitle;           // 标题
    private TextView mLiveUserNumberTop;   // 顶部人数显示
    private ImageView mLiveClose;          // 退出直播间按钮
    private ImageView mVideoDocMore;       // 配置按钮

    // ----------底部布局----------
    private RelativeLayout mBottomLayout;
    private RelativeLayout mPortraitLiveBottom;   // 下方直播控制
    private LinearLayout mBarrageControlLayout;   // 弹幕开关布局
    private ImageView mBarrageControl;            // 弹幕开关
    private ImageView mBarrageSetting;            // 弹幕设置
    private ImageView mLiveFullScreen;            // 全屏按钮
    private ImageView mLiveVideoDocSwitch;         // 切换视频和文档大小屏按钮
    private TextView mQualitySwitch;              // 切换清晰度


    private RelativeLayout mBottomChatLayout; // 下方输入聊天信息框
    private GridView mEmojiGrid;            // 表情列表
    private Button mChatSend;               // 发送按钮
    private ImageView mEmoji;               // 切换表情按钮
    private EditText mInput;                // 表情输入框
    private TextView mChatLabel;            // 弹幕输入框占位

    // ----------直播状态显示---------
    private RelativeLayout mNoStreamRoot;
    private TextView mNoStreamText;           // 直播开始和结束的提示
    private TextView mCountDownTimeText;      // 直播开始倒计时
    private long downTime;
    private Timer timer;
    private TimerTask timerTask;

    // ----------错误布局----------
    public LinearLayout mTipsLayout;    // 错误界面
    private TextView mTipsView;         // 错误提示
    private TextView mTryBtn;           // 播放错误，重试按钮

    // 底部弹出设置界面
    private SettingPopupWindow settingPopupWindow;
    //右侧弹出布局
    private LiveRightView rightRoot;

    // 跑马灯组件
    private MarqueeView mMarqueeView;
    private boolean isOpenMarquee;          // 是否显示跑马灯

    // 弹幕组件
    private BarrageLayout mLiveBarrage;
    private boolean isOpenBarrage;          // 后台是否开启弹幕
    private boolean isBarrageOn = true;     // 是否显示弹幕

    private InputMethodManager mImm;
    // 防止输入框出来的时候，隐藏上下布局
    private final AtomicBoolean isShowInput = new AtomicBoolean(false);

    boolean isEmojiShow = false;                  // 表情是否显示
    boolean isSoftInput = false;                  // 键盘是否显示
    private int softKeyHeight;                    // 软键盘的高度
    private boolean showEmojiAction = false;      // 是否点击表情按钮
    private boolean isShowUserCount;              // 是否显示房间人数
    private boolean isShowQuality;                // 是否显示清晰度
    private boolean isVideoMain;                  // 当前是否是视频大窗
    private boolean isFloatDismiss;               // 当前是否显示悬浮窗

    private BanChatPopup banChatPopup;      // 禁言弹出框
    private DWLiveCoreHandler dwLiveCoreHandler;


    protected static final int HIDE_TIME = 5000;

    //针对隐藏标题栏和聊天布局的延迟
    private final Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            if (isShowInput.get()) {
                return;
            }
            hide();
        }
    };


    public LiveRoomLayout(Context context) {
        super(context);
    }

    public LiveRoomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LiveRoomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void initViews() {
        mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        LayoutInflater.from(mContext).inflate(R.layout.live_room_layout, this, true);
        mLiveTitle = findViewById(R.id.tv_portrait_live_title);
        mLiveUserNumberTop = findViewById(R.id.tv_portrait_live_user_count_top);
        mBarrageControlLayout = findViewById(R.id.ll_barrage_control);
        mBarrageControl = findViewById(R.id.iv_barrage_control);
        mBarrageSetting = findViewById(R.id.iv_barrage_setting);
        mTopLayout = findViewById(R.id.rl_portrait_live_top_layout);
        mBottomLayout = findViewById(R.id.rl_portrait_live_bottom_layout);
        mLiveVideoDocSwitch = findViewById(R.id.video_doc_switch);
        mLiveFullScreen = findViewById(R.id.iv_portrait_live_full);
        mPortraitLiveBottom = findViewById(R.id.portrait_live_bottom);
        mLiveClose = findViewById(R.id.iv_portrait_live_close);
        mBottomChatLayout = findViewById(R.id.id_chat_bottom);
        mEmoji = findViewById(R.id.id_push_chat_emoji);
        mEmojiGrid = findViewById(R.id.id_push_emoji_grid);
        mChatSend = findViewById(R.id.id_push_chat_send);
        mInput = findViewById(R.id.id_push_chat_input);
        mChatLabel = findViewById(R.id.tv_chat_label);
        rightRoot = findViewById(R.id.right_root);
        mQualitySwitch = findViewById(R.id.tv_quality_switch);
        mNoStreamRoot = findViewById(R.id.no_stream_root);
        mNoStreamText = findViewById(R.id.tv_no_stream);
        mCountDownTimeText = findViewById(R.id.id_count_down_time);

        mTipsLayout = findViewById(R.id.id_error_layout);
        mTryBtn = findViewById(R.id.id_try);
        mTipsView = findViewById(R.id.id_msg_tips);

        mMarqueeView = findViewById(R.id.marquee_view);
        mLiveBarrage = findViewById(R.id.live_barrage);
        mLiveBarrage.setHeight(getContext().getResources().getDisplayMetrics().widthPixels / 2, 0, 0);
        mVideoDocMore = findViewById(R.id.video_doc_more);

        // 显示动画
        mShowAction = new AlphaAnimation(0f, 1.0f);
        mShowAction.setRepeatMode(RESTART);
        mShowAction.setDuration(300);
        // 隐藏动画
        mHiddenAction = new AlphaAnimation(1.0f, 0f);
        mHiddenAction.setRepeatMode(RESTART);
        mHiddenAction.setDuration(300);

        //竖屏设置弹框
        settingPopupWindow = new SettingPopupWindow(mContext);
        settingPopupWindow.setSettingPopupInterface(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 设置点击事件
        initListener();
        // 初始化表情和聊天
        initEmojiAndChat();
        // 打开设置按钮
        openSetting();
        // 延时隐藏上下bar
        removeCallbacks(hideRunnable);
        postDelayed(hideRunnable, HIDE_TIME);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mNoStreamRoot.getVisibility() != VISIBLE) {
            mVideoDocMore.setVisibility(VISIBLE);
        }
        if (newConfig.orientation == ORIENTATION_LANDSCAPE) {
            //  横屏
            mLiveFullScreen.setVisibility(GONE);
            mBottomChatLayout.setVisibility(GONE);
            //如果是上课状态再显示更多按钮 显示横竖屏切换按钮

            if (DWLiveCoreHandler.getInstance().hasChatView()){
                mBarrageControlLayout.setVisibility(VISIBLE);
            }
            if (isBarrageOn) {
                mBarrageSetting.setVisibility(VISIBLE);
            }
            if (isShowQuality) {
                mQualitySwitch.setVisibility(View.VISIBLE);
            }

            ViewGroup.LayoutParams layoutParams = mPortraitLiveBottom.getLayoutParams();
            layoutParams.height = DensityUtil.dp2px(mContext, 55);
            mPortraitLiveBottom.setLayoutParams(layoutParams);
            DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
            if (dwLiveCoreHandler != null) {
                // 判断当前直播间模版是否有"聊天"功能，如果没有，就不展示下方聊天布局
                if (!dwLiveCoreHandler.hasChatView()) {
                    mBottomChatLayout.setVisibility(GONE);
                }
            }
        } else {
            // 竖屏
            mBottomChatLayout.setVisibility(GONE);
            mBarrageControlLayout.setVisibility(GONE);
            mQualitySwitch.setVisibility(View.GONE);
            //如果是上课状态再 显示横竖屏切换按钮
            if (mNoStreamRoot.getVisibility() != VISIBLE) {
                mLiveFullScreen.setVisibility(VISIBLE);
            }
            // 显示下方控制布局
            mPortraitLiveBottom.setVisibility(VISIBLE);
            // 隐藏聊天布局
            mBottomChatLayout.setVisibility(GONE);

            ViewGroup.LayoutParams layoutParams = mPortraitLiveBottom.getLayoutParams();
            layoutParams.height = DensityUtil.dp2px(mContext, 40);
            mPortraitLiveBottom.setLayoutParams(layoutParams);
        }

        removeCallbacks(hideRunnable);
        postDelayed(hideRunnable, HIDE_TIME);
    }


    /**
     * 初始化房间状态
     * <p>
     * ** 需要登录之后调用,包含文档和视频的内部监听
     *
     * @param isVideoMain 是否是视频大窗
     */
    public void init(boolean isVideoMain) {
        this.isVideoMain = isVideoMain;
        dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler == null) {
            return;
        }
        // 设置监听
        dwLiveCoreHandler.setDwLiveRoomListener(this);

        // 是否显示人数
        isShowUserCount = dwLiveCoreHandler.isShowUserCount();
        mLiveUserNumberTop.setVisibility(isShowUserCount ? VISIBLE : GONE);

        // 是否显示清晰度
        isShowQuality = dwLiveCoreHandler.isShowQuality();
        if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
            mQualitySwitch.setVisibility(isShowQuality ? VISIBLE : GONE);
        } else {
            mQualitySwitch.setVisibility(GONE);
        }

        // 是否显示切换文档按钮
        if (!dwLiveCoreHandler.hasPdfView()) {
            mLiveVideoDocSwitch.setVisibility(GONE);
        }
        // 是否开启跑马灯
        isOpenMarquee = dwLiveCoreHandler.isOpenMarquee();
        openMarquee();
        // 是否开启弹幕
        isOpenBarrage = dwLiveCoreHandler.isOpenBarrage();
        openLiveBarrage();


    }

    /**
     * 进入全屏
     */
    private void intoFullScreen() {
        if (liveRoomStatusListener != null) {
            liveRoomStatusListener.fullScreen();
        }

    }

    /**
     * 点击监听方法
     */
    private void initListener() {
        // 点击整个控件，隐藏上下bar
        setOnClickListener(mRoomAnimatorListener);
        // 点击切换大小窗
        mLiveVideoDocSwitch.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(View v) {
                if (liveRoomStatusListener != null) {
                    if (isFloatDismiss) {
                        liveRoomStatusListener.openVideoDoc();
                        setSwitchText(false);
                    } else {
                        isVideoMain = !isVideoMain;
                        liveRoomStatusListener.switchVideoDoc(isVideoMain);
                        setSwitchText(false);
                    }

                }
            }
        });

        //点击全屏切换
        mLiveFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                intoFullScreen();
            }
        });

        // 点击左上角返回按钮
        mLiveClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if (liveRoomStatusListener != null) {
                    liveRoomStatusListener.closeRoom();
                }
            }
        });

        // 点击刷新重试
        mTryBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isNetworkAvailable(getContext())) {
                    mTipsLayout.setVisibility(GONE);
                    DWLive.getInstance().start();
                } else {
                    CustomToast.showToast(mContext, "网络异常，请检查网络", Toast.LENGTH_SHORT);
                }
            }
        });

        // 输入弹幕
        mChatLabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowInput.set(true);
                isSoftInput = true;
                removeCallbacks(hideRunnable);

                mInput.requestFocus();
                mImm.showSoftInput(mInput, 0);

                mPortraitLiveBottom.setVisibility(GONE);
                mBottomChatLayout.setVisibility(VISIBLE);


            }
        });

        // 弹出切换清晰度
        mQualitySwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rightRoot.showRight(LiveRightView.RIGHT_SHOW_QUALITY);
            }
        });

        // 切换清晰度和播放模式 -->更新UI
        rightRoot.setRightCallBack(new LiveRightView.LiveRightCallBack() {


            @Override
            public void onChangePlayMode(DWLive.LivePlayMode playMode) {
                mQualitySwitch.setVisibility(playMode == DWLive.LivePlayMode.PLAY_MODE_TYEP_VIDEO ? VISIBLE : GONE);
                DWLiveCoreHandler.getInstance().updatePlayModeUI(playMode);
                setMode(2, playMode == DWLive.LivePlayMode.PLAY_MODE_TYEP_AUDIO);
            }

            @Override
            public void onChangeQuality(int quality, String qualityDesc) {
                mQualitySwitch.setText(qualityDesc);
                setQuality(2, quality, qualityDesc);
            }

            @Override
            public void onChangeBarrage(int type) {
                if (type == BARRAGE_FULL) {
                    mLiveBarrage.setHeight(getContext().getResources().getDisplayMetrics().heightPixels, 0, 0);
                } else {
                    mLiveBarrage.setHeight((getContext().getResources().getDisplayMetrics().heightPixels) / 2, 0, 0);
                }
            }

            @Override
            public void onChangeLine(int line) {
                setLine(2, line);
            }

            @Override
            public void onClose() {
                removeCallbacks(hideRunnable);
                if (!mTopLayout.isShown()) {
                    show();
                }
                postDelayed(hideRunnable, HIDE_TIME);
            }
        });

    }

    // 设置切换大小窗文字显示
    public void setSwitchText(boolean isFloatDismiss) {
        this.isFloatDismiss = isFloatDismiss;
        if (isFloatDismiss) {
            mLiveVideoDocSwitch.setImageResource(isVideoMain ? R.drawable.open_doc : R.drawable.open_video);
        } else {
            mLiveVideoDocSwitch.setImageResource(R.drawable.video_doc_switch);
        }
    }

    /**
     * 获取小窗显示状态
     */
    public boolean isVideoMain() {
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null && !dwLiveCoreHandler.hasPdfView()) {
            return true;
        }
        return isVideoMain;
    }

    /**
     * 是否是右边栏消费了返回按钮
     *
     * @return true 消费
     */
    public boolean onBackPressed() {
        if (rightRoot.getVisibility() == VISIBLE) {
            rightRoot.setVisibility(GONE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置清晰度的界面
     *
     * @param type    1为竖屏切换清晰度成功之后   2为横屏切换清晰度之后
     * @param quality 清晰度
     */
    private void setQuality(int type, int quality, String desc) {
        if (type == 1) {
            rightRoot.setQuality(quality);
        } else if (type == 2) {
            settingPopupWindow.setQuality(quality);
        }
        mQualitySwitch.setText(desc);
    }

    /**
     * 设置线路的界面
     *
     * @param type 1为竖屏切换线路成功之后   2为横屏切换线路之后
     * @param line 线路
     */
    private void setLine(int type, int line) {
        if (type == 1) {
            rightRoot.setLine(line);
        } else if (type == 2) {
            settingPopupWindow.setLine(line);
        }
    }

    /**
     * 设置播放模式的界面
     *
     * @param type    1为竖屏切换模式成功之后   2为横屏切换模式之后
     * @param isAudio 是否是音频
     */
    private void setMode(int type, boolean isAudio) {
        if (type == 1) {
            rightRoot.setMode(isAudio);
        } else if (type == 2) {
            settingPopupWindow.setMode(isAudio);
        }
    }

    /**
     * 开启功能配置用例
     */
    private void openSetting() {
        mVideoDocMore.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(View v) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    rightRoot.showRight(LiveRightView.RIGHT_SHOW_LINE);
                } else {
                    settingPopupWindow.show(activity, getRootView(), Gravity.BOTTOM, 0, 0);
                }

            }
        });

    }


    //**************************************** 回调相关方法 **************************************

    /**
     * 接收到服务器配置：切换视频文档区域
     *
     * @param isVideoMain 视频是否为主区域
     */
    @Override
    public void onSwitchVideoDoc(final boolean isVideoMain) {
        // 判断是否相同，相同没必要触发
        if (this.isVideoMain == isVideoMain) {
            return;
        }
        this.isVideoMain = isVideoMain;
        if (liveRoomStatusListener != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    liveRoomStatusListener.switchVideoDoc(isVideoMain);   // 服务器返回配置信息
                    setSwitchText(false);
                }
            });
        }
    }

    /**
     * 展示直播间标题
     */
    @Override
    public void showRoomTitle(final String title) {
        post(new Runnable() {
            @Override
            public void run() {
                mLiveTitle.setText(title);
            }
        });
    }

    /**
     * 展示直播间人数
     */
    @Override
    public void showRoomUserNum(final int number) {
        post(new Runnable() {
            @Override
            public void run() {
                mLiveUserNumberTop.setText(String.valueOf(number));
            }
        });
    }

    /**
     * 踢出用户
     */
    @Override
    public void onKickOut() {
        if (liveRoomStatusListener != null) {
            liveRoomStatusListener.kickOut();
        }
        stopTimer();
    }

    @Override
    public void onInformation(final String msg) {
        post(new Runnable() {
            @Override
            public void run() {
                if (banChatPopup == null) {
                    banChatPopup = new BanChatPopup(getContext());
                }
                if (banChatPopup.isShowing()) {
                    banChatPopup.onDestroy();
                }
                banChatPopup.banChat(msg);
                banChatPopup.show(LiveRoomLayout.this);
            }
        });
    }

    @Override
    public void onStreamEnd(boolean isNormal, String reason) {
        mNoStreamRoot.setVisibility(VISIBLE);
        mLiveFullScreen.setVisibility(View.GONE);
        mLiveVideoDocSwitch.setVisibility(GONE);
        mQualitySwitch.setVisibility(GONE);
        mNoStreamText.setText(reason);
        mVideoDocMore.setVisibility(GONE);
        //隐藏全凭按钮
        if (!isNormal) {
            // 如果开启了倒计时，则显示
            if (dwLiveCoreHandler != null && dwLiveCoreHandler.getRoomInfo().getOpenLiveCountdown() == 1) {
                downTime = dwLiveCoreHandler.getRoomInfo().getLiveCountdown();
                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                mCountDownTimeText.setText(TimeUtil.getYearMonthDayHourMinuteSecond(
                                        mContext, downTime));
                                downTime -= 1;
                                if (downTime <= 0) {
                                    mCountDownTimeText.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                };
                timer.schedule(timerTask, 0, 1000);
            }
        }
        if (liveRoomStatusListener != null) {
            liveRoomStatusListener.onStreamEnd();
        }
        // 关闭跑马灯
        closeMarquee();
    }

    @Override
    public void onStreamStart() {
        stopTimer();
        setSwitchText(false);
        mVideoDocMore.setVisibility(VISIBLE);
        mTipsLayout.setVisibility(GONE);
        mNoStreamRoot.setVisibility(View.GONE);
        //显示全屏按钮
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLiveFullScreen.setVisibility(VISIBLE);
            mQualitySwitch.setVisibility(GONE);
        } else {
            mQualitySwitch.setVisibility(isShowQuality ? VISIBLE : GONE);
            mLiveFullScreen.setVisibility(GONE);
        }
        if (DWLiveCoreHandler.getInstance().hasPdfView()) {
            mLiveVideoDocSwitch.setVisibility(VISIBLE);
        } else {
            mLiveVideoDocSwitch.setVisibility(GONE);
        }

        if (liveRoomStatusListener != null) {
            liveRoomStatusListener.onStreamStart();
        }
        // 开启跑马灯
        openMarquee();


    }

    @Override
    public void showError(final String errMsg) {
        post(new Runnable() {
            @Override
            public void run() {
                mTipsLayout.setVisibility(VISIBLE);
                mTipsView.setText(errMsg);
            }
        });

    }

    @Override
    public void onHDAudioMode(DWLive.LiveAudio hasAudio) {
        rightRoot.showAudio(hasAudio == DWLive.LiveAudio.HAVE_AUDIO_LINE_TRUE);
        if (hasAudio == DWLive.LiveAudio.HAVE_AUDIO_LINE_TRUE) {
            settingPopupWindow.addModeChange();
        }
    }

    @Override
    public void onHDReceivedVideoQuality(List<LiveQualityInfo> videoQuality, LiveQualityInfo currentQuality) {
        rightRoot.showVideoQuality(videoQuality, currentQuality);
        settingPopupWindow.addQuality(videoQuality, currentQuality);
    }

    @Override
    public void onHDReceivedVideoAudioLines(List<LiveLineInfo> lines, int indexNum) {
        rightRoot.showVideoAudioLines(lines, indexNum);
        settingPopupWindow.addLines(lines, indexNum);
    }

    @Override
    public void callRetryAndHideErrorView() {
        if (mContext!=null&&mTipsLayout!=null){
            if (NetworkUtils.isNetworkAvailable(getContext())) {
                mTipsLayout.setVisibility(GONE);
                DWLive.getInstance().start();
            } else {
                CustomToast.showToast(mContext, "网络异常，请检查网络", Toast.LENGTH_SHORT);
            }
        }

    }


    // 监听软键盘高度
    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        if (height > 10) {
            isSoftInput = true;
            softKeyHeight = height;
            mBottomChatLayout.setTranslationY(-softKeyHeight);
            mEmoji.setImageResource(R.drawable.push_chat_emoji_normal);
            isEmojiShow = false;

            removeCallbacks(hideRunnable);
        } else {
            if (!showEmojiAction) {
                hideEmoji();
                mBottomChatLayout.setTranslationY(0);
            }
            isSoftInput = false;

        }
        //结束动作指令
        showEmojiAction = false;
    }


    //***************************************** 聊天相关方法 **************************************

    // 最大输入字符数
    private short maxInput = 300;

    // 初始化表情和聊天相关
    private void initEmojiAndChat() {
        // 限制输入字符数为300
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String inputText = mInput.getText().toString();
                if (inputText.length() > maxInput) {
                    CustomToast.showToast(mContext, "字数超过300字", Toast.LENGTH_SHORT);
                    mInput.setText(inputText.substring(0, maxInput));
                    mInput.setSelection(maxInput);
                }
                if (TextUtils.isEmpty(inputText)) {
                    mChatSend.setEnabled(false);
                } else {
                    mChatSend.setEnabled(true);
                }
            }
        });
        mEmoji.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCallbacks(hideRunnable);
                //如果当前软件盘处于显示状态
                if (isSoftInput) {
                    showEmojiAction = true;
                    //1显示表情键盘
                    showEmoji();
                    //2隐藏软键盘
                    mImm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
                } else if (isEmojiShow) {  //表情键盘显示，软键盘没有显示，则直接显示软键盘
                    boolean b = mImm.showSoftInput(mInput, 0);
                    if (b) {
                        hideEmoji();
                    }
                } else {
                    showEmoji();
                }
            }
        });

        initEmojiAdapter();

        mChatSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mInput.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    CustomToast.showToast(mContext, "聊天内容不能为空", Toast.LENGTH_SHORT);
                    return;
                }
                DWLive.getInstance().sendPublicChatMsg(msg);
                clearChatInput();
                //判断是否是软键盘谈起  如果不是软键盘  需要单独去将输入框滑动到下方
                if (isSoftInput) {
                    hideKeyboard();
                } else {
                    hideEmoji();
                    mBottomChatLayout.setTranslationY(0);
                }
                isShowInput.set(false);
                removeCallbacks(hideRunnable);
                postDelayed(hideRunnable, HIDE_TIME);

            }
        });

        mBarrageControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                controlBarrageControl(!isBarrageOn);
            }
        });
        mBarrageSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rightRoot.showRight(RIGHT_SHOW_BARRAGE);
            }
        });
    }

    private void initEmojiAdapter() {
        EmojiAdapter emojiAdapter = new EmojiAdapter(mContext);
        emojiAdapter.bindData(EmojiUtil.imgs);
        mEmojiGrid.setAdapter(emojiAdapter);
        mEmojiGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mInput == null) {
                    return;
                }
                // 一个表情span占位8个字符
                if (mInput.getText().length() + 8 > maxInput) {
                    CustomToast.showToast(mContext, "字符数超过300字", Toast.LENGTH_SHORT);
                    return;
                }
                if (position == EmojiUtil.imgs.length - 1) {
                    EmojiUtil.deleteInputOne(mInput);
                } else {
                    EmojiUtil.addEmoji(mContext, mInput, position);
                }
            }
        });
    }

    public void clearChatInput() {
        mInput.setText("");
    }

    public void hideKeyboard() {
        hideEmoji();
        mImm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
    }

    // 显示emoji
    public void showEmoji() {
        if (mEmojiGrid.getHeight() != softKeyHeight && softKeyHeight != 0) {
            ViewGroup.LayoutParams lp = mEmojiGrid.getLayoutParams();
            lp.height = softKeyHeight;
            mEmojiGrid.setLayoutParams(lp);
        }
        mEmojiGrid.setVisibility(View.VISIBLE);
        mEmoji.setImageResource(R.drawable.push_chat_emoji);
        isEmojiShow = true;
        float transY;
        if (softKeyHeight == 0) {
            transY = -mEmojiGrid.getHeight();
        } else {
            transY = -softKeyHeight;
        }
        mBottomChatLayout.setTranslationY(transY);
    }

    // 隐藏emoji
    public void hideEmoji() {
        mEmojiGrid.setVisibility(View.GONE);
        mEmoji.setImageResource(R.drawable.push_chat_emoji_normal);
        isEmojiShow = false;
    }

    //***************************************** 控制布局动画相关方法 ******************************
    private final OnClickListener mRoomAnimatorListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            mBottomChatLayout.setTranslationY(0);
            removeCallbacks(hideRunnable);
            hideKeyboard();
            toggleTopAndButtom();
        }
    };

    //显示动画
    private AlphaAnimation mShowAction;
    //隐藏动画
    private AlphaAnimation mHiddenAction;

    private void hide() {
        mTopLayout.clearAnimation();
        mBottomLayout.clearAnimation();
        mBottomLayout.startAnimation(mHiddenAction);
        mTopLayout.startAnimation(mHiddenAction);
        mBottomLayout.setVisibility(GONE);
        mTopLayout.setVisibility(GONE);
        isShowInput.set(false);
    }

    private void show() {
        mBottomLayout.clearAnimation();
        mTopLayout.clearAnimation();
        mBottomLayout.startAnimation(mShowAction);
        mTopLayout.startAnimation(mShowAction);
        mTopLayout.setVisibility(VISIBLE);
        mBottomLayout.setVisibility(VISIBLE);

        // 显示下方控制布局
        mPortraitLiveBottom.setVisibility(VISIBLE);
        // 隐藏聊天布局
        mBottomChatLayout.setVisibility(GONE);

        removeCallbacks(hideRunnable);
        postDelayed(hideRunnable, HIDE_TIME);
    }

    private void toggleTopAndButtom() {
        if (mTopLayout.isShown()) {
            hide();
        } else {
            show();
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    //***************************************** 跑马灯控制相关 ***********************************

    /**
     * 开启跑马灯
     */
    public void openMarquee() {
        if (isOpenMarquee) {
            mMarqueeView.setVisibility(VISIBLE);
            if (dwLiveCoreHandler.getViewer() != null) {
                setMarquee(dwLiveCoreHandler.getViewer().getMarquee());
            }
        }
    }

    /**
     * 关闭跑马灯
     */
    public void closeMarquee() {
        if (mMarqueeView != null) {
            mMarqueeView.stop();
            mMarqueeView.setVisibility(View.GONE);
        }
    }

    private void setMarquee(final Marquee marquee) {
        final ViewGroup parent = (ViewGroup) mMarqueeView.getParent();
        if (parent.getWidth() != 0 && parent.getHeight() != 0) {
            if (marquee != null && marquee.getAction() != null) {
                if (marquee.getType().equals("text")) {
                    mMarqueeView.setTextContent(marquee.getText().getContent());
                    mMarqueeView.setTextColor(marquee.getText().getColor().replace("0x", "#"));
                    mMarqueeView.setTextFontSize((int) DensityUtil.sp2px(mContext, marquee.getText().getFont_size()));
                    // 1代表文字
                    mMarqueeView.setType(1);
                } else {
                    mMarqueeView.setMarqueeImage(mContext, marquee.getImage().getImage_url(), marquee.getImage().getWidth(), marquee.getImage().getHeight());
                    // 2代表图片
                    mMarqueeView.setType(2);
                }
                mMarqueeView.setMarquee(marquee, parent.getHeight(), parent.getWidth());
                mMarqueeView.setOnMarqueeImgFailListener(new OnMarqueeImgFailListener() {
                    @Override
                    public void onLoadMarqueeImgFail() {
                        ELog.e(TAG, "");

                    }
                });
                mMarqueeView.start();
            }
        } else {
            parent.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {

                        @Override
                        public void onGlobalLayout() {
                            parent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            int width = parent.getWidth();// 获取宽度
                            int height = parent.getHeight();// 获取高度
                            if (marquee != null && marquee.getAction() != null) {
                                if (marquee.getType().equals("text")) {
                                    mMarqueeView.setTextContent(marquee.getText().getContent());
                                    mMarqueeView.setTextColor(marquee.getText().getColor().replace("0x", "#"));
                                    mMarqueeView.setTextFontSize((int) DensityUtil.sp2px(mContext, marquee.getText().getFont_size()));
                                    mMarqueeView.setType(1);
                                } else {
                                    mMarqueeView.setMarqueeImage(mContext,
                                            marquee.getImage().getImage_url(), marquee.getImage().getWidth(), marquee.getImage().getHeight());
                                    mMarqueeView.setType(2);
                                }
                                mMarqueeView.setMarquee(marquee, height, width);
                                mMarqueeView.setOnMarqueeImgFailListener(new OnMarqueeImgFailListener() {
                                    @Override
                                    public void onLoadMarqueeImgFail() {

                                    }
                                });
                                mMarqueeView.start();
                            }
                        }
                    });
        }

    }


    //***************************************** 弹幕控制相关 ***********************************

    /**
     * 开启弹幕
     */
    public void openLiveBarrage() {
        controlBarrageControl(isOpenBarrage);
    }

    /**
     * 停止弹幕
     */
    public void closeLiveBarrage() {
        controlBarrageControl(false);

    }

    /**
     * 开关弹幕 目前只针对首次进入直播间 服务器返回的弹幕开关数据的时候调用
     *
     * @param isBarrageOn 是否开启弹幕
     */
    public void controlBarrageControl(boolean isBarrageOn) {
        this.isBarrageOn = isBarrageOn;
        if (mBarrageControl != null) {
            if (isBarrageOn) {
                mBarrageControl.setImageResource(R.drawable.barrage_on);
                mChatLabel.setVisibility(VISIBLE);
                mLiveBarrage.start();
                mBarrageSetting.setVisibility(VISIBLE);
            } else {
                mBarrageControl.setImageResource(R.drawable.barrage_off);
                mChatLabel.setVisibility(GONE);
                mLiveBarrage.stop();
                mBarrageSetting.setVisibility(GONE);
            }
        }
    }

    /**
     * 获取弹幕控件
     *
     * @return BarrageLayout
     */
    public BarrageLayout getLiveBarrageLayout() {
        return mLiveBarrage;
    }

    //***************************************** 工具方法 *****************************************
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                performClick();
                return false;

        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }


    //***************************************** 设置监听方法 *************************************
    private LiveRoomStatusListener liveRoomStatusListener;

    public void setLiveRoomStatusListener(LiveRoomStatusListener listener) {
        this.liveRoomStatusListener = listener;
    }

    @Override
    public void onSpeed(float speed) {

    }

    @Override
    public void onPlayMode(final boolean isAudio, final SettingPopupWindowCallBack callBack) {
        DWLiveCoreHandler instance = DWLiveCoreHandler.getInstance();
        if (instance != null) {
            // 切换到视频模式
            instance.changePlayMode(isAudio ? DWLive.LivePlayMode.PLAY_MODE_TYEP_AUDIO : DWLive.LivePlayMode.PLAY_MODE_TYEP_VIDEO, new LiveChangeSourceListener() {

                @Override
                public void onChange(int success) {
                    if (success == 0) {
                        DWLiveCoreHandler.getInstance().updatePlayModeUI(isAudio ? DWLive.LivePlayMode.PLAY_MODE_TYEP_AUDIO : DWLive.LivePlayMode.PLAY_MODE_TYEP_VIDEO);
                        setMode(1, isAudio);
                    }
                    if (callBack != null) {
                        callBack.onResult(success);
                    }
                }
            });
        }
    }

    @Override
    public void onQuality(final int quality, final String desc, final SettingPopupWindowCallBack callBack) {
        DWLiveCoreHandler instance = DWLiveCoreHandler.getInstance();
        if (instance != null) {
            instance.changeQuality(quality, new LiveChangeSourceListener() {
                @Override
                public void onChange(int success) {
                    if (success == 0) {
                        setQuality(1, quality, desc);
                    }
                    if (callBack != null) {
                        callBack.onResult(success);
                    }
                }
            });
        }
    }

    @Override
    public void onLine(final int line, final SettingPopupWindowCallBack callBack) {
        DWLiveCoreHandler instance = DWLiveCoreHandler.getInstance();
        if (instance != null) {
            instance.changeLine(line, new LiveChangeSourceListener() {
                @Override
                public void onChange(int success) {
                    if (success == 0) {
                        setLine(1, line);
                    }
                    if (callBack != null) {
                        callBack.onResult(success);
                    }
                }
            });
        }
    }

    public void destroy() {
        stopTimer();
    }

    public interface LiveRoomStatusListener {

        /**
         * 切换 视频/文档 区域回调
         */
        void switchVideoDoc(boolean isVideoMain);

        /**
         * 打开 视频/文档 小窗
         */
        void openVideoDoc();

        /**
         * 点击 Back 按钮 退出直播间回调
         */
        void closeRoom();

        /**
         * 点击 全屏 按钮 进入全屏回调
         */
        void fullScreen();

        /**
         * 用户踢出 事件回调 #Called From DWLiveCoreHandler
         */
        void kickOut();

        /**
         * 点击文档类型
         */
        void onClickDocScaleType(int scaleType);

        /**
         * 直播结束
         */
        void onStreamEnd();

        /**
         * 直播开始
         */
        void onStreamStart();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        settingPopupWindow.setActivity(activity);
    }
}
