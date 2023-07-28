package com.bokecc.livemodule.base;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.chat.util.DensityUtil;
import com.bokecc.livemodule.popup.SettingPopupWindow;
import com.bokecc.livemodule.popup.callback.SettingPopupInterface;
import com.bokecc.livemodule.popup.callback.SettingPopupWindowCallBack;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.room.ReplayRoomLayout;
import com.bokecc.livemodule.replay.room.rightview.ReplayRightView;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.livemodule.view.BaseRelativeLayout;
import com.bokecc.livemodule.view.DotMsgPopupWindow;
import com.bokecc.livemodule.view.DotView;
import com.bokecc.livemodule.view.RePlaySeekBar;
import com.bokecc.sdk.mobile.live.OnMarqueeImgFailListener;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.bokecc.sdk.mobile.live.pojo.Marquee;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayDot;
import com.bokecc.sdk.mobile.live.widget.MarqueeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 回放/离线回放
 * 播放控制界面封装
 */
public abstract class BaseReplayRoomLayout extends BaseRelativeLayout implements SettingPopupInterface {
    private static final String TAG = "BaseReplayRoomLayout";

    protected Activity activity;
    // ----------顶部布局---------
    protected RelativeLayout mTopLayout;
    protected TextView mTitle;             // 标题
    protected View mTopRightLayout;        // 顶部右侧布局
    protected ImageView mVideoDocSwitch;    // 切换文档/视频按钮
    protected ImageView mClose;            // 退出回放按钮
    protected ImageView mVideoDocMore;     // 横屏 配置按钮
//

    // ----------底部布局----------
    protected RelativeLayout mBottomLayout;
    protected View mPortBottomLayout;
    protected View mLandBottomLayout;
    protected RePlaySeekBar mPlaySeekBar;     // 竖屏进度条
    protected RePlaySeekBar mLandPlaySeekBar; // 横屏进度条
    protected TextView mDurationView;         // 播放时总时长
    protected TextView mLandDurationView;     // 播放时总时长
    protected TextView mCurrentTime;          // 当前播放时间
    protected TextView mLandCurrentTime;      // 当前播放时间
    protected ImageView mLiveFullScreen;      // 全屏按钮
    protected ImageView mPlayIcon;            // 播放/暂停 按钮
    protected TextView mLandSpeed;            // 横屏 倍速按钮
    protected TextView mLandQuality;          // 横屏 切换清晰度
    protected ImageView mLandPlayIcon;        // 横屏 播放/暂停 按钮
    protected ImageView mLandVideoDocSwitch;   // 横屏 切换大小屏按钮
    protected ReplayRightView rightView;      // 横屏右侧 进度 清晰度 线路布局
    // 底部弹出设置界面
    protected SettingPopupWindow settingPopupWindow;

    // ----------错误布局----------
    protected LinearLayout mTipsLayout;   // 错误界面
    protected TextView mTipsView;         // 错误提示
    protected TextView mTryBtn;           // 播放错误，重试按钮

    // ----------手势拖动布局----------
    protected RelativeLayout mSeekRoot;   // 手势拖动界面
    protected TextView mSeekTime;         // seek time
    protected TextView mSumTime;          // sum time

    protected MarqueeView mMarqueeView;   // 跑马灯控件
    protected TextView progress_record;   // 进度记忆展示

    protected boolean isTouch;            // 是否正在拖动进度
    protected boolean isVideoMain;        // 是否是视频大屏
    protected int start;                  // 拖动进度开始时间
    private boolean isFloatDismiss;       // 当前是否显示悬浮窗
    protected List<ReplayDot> dotBeans;   // 打点数据列表
    private DotMsgPopupWindow popupWindow;// 打点弹出界面


    protected static final int HIDE_TIME = 5000;
    //针对隐藏标题栏和聊天布局的延迟
    protected Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public BaseReplayRoomLayout(Context context) {
        super(context);
    }

    public BaseReplayRoomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseReplayRoomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.replay_room_layout, this, true);
        mTitle = findViewById(R.id.tv_portrait_live_title);
        mTopLayout = findViewById(R.id.rl_portrait_live_top_layout);
        mBottomLayout = findViewById(R.id.bottom_layout);
        mLiveFullScreen = findViewById(R.id.iv_portrait_live_full);
        mClose = findViewById(R.id.iv_portrait_live_close);
        mTipsLayout = findViewById(R.id.id_error_layout);
        mTryBtn = findViewById(R.id.id_try);
        mTipsView = findViewById(R.id.id_msg_tips);
        mSeekRoot = findViewById(R.id.seek_root);
        mSeekTime = findViewById(R.id.tv_seek_time);
        mSumTime = findViewById(R.id.tv_sum_time);
        progress_record = findViewById(R.id.progress_record);
        // 竖屏显示
        mPortBottomLayout = findViewById(R.id.replay_port_bottom_layout);
        mCurrentTime = findViewById(R.id.replay_current_time);
        mDurationView = findViewById(R.id.replay_duration);
        mTopRightLayout = findViewById(R.id.id_port_video_doc_switch);
        mVideoDocSwitch = findViewById(R.id.video_doc_switch);
        mPlayIcon = findViewById(R.id.replay_play_icon);
        // 横屏显示
        mLandBottomLayout = findViewById(R.id.replay_land_bottom_layout);
        mLandCurrentTime = findViewById(R.id.replay_land_current_time);
        mLandDurationView = findViewById(R.id.replay_land_duration);
        mLandSpeed = findViewById(R.id.replay_land_speed);
        mLandQuality = findViewById(R.id.replay_land_quality);
        mLandVideoDocSwitch = findViewById(R.id.video_doc_land_switch);
        mLandPlayIcon = findViewById(R.id.replay_land_play_icon);
        mVideoDocMore = findViewById(R.id.video_doc_more);
        rightView = findViewById(R.id.right_root);

        mPlaySeekBar = findViewById(R.id.replay_progressbar);
        mLandPlaySeekBar = findViewById(R.id.replay_land_progressbar);

    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //  横屏
            mLiveFullScreen.setVisibility(GONE);
            mLandBottomLayout.setVisibility(VISIBLE);
            mPortBottomLayout.setVisibility(GONE);
            mVideoDocSwitch.setVisibility(GONE);
            // 初始化打点View
            initDotView();


        } else {
            // 竖屏
            mLiveFullScreen.setVisibility(VISIBLE);
            mLandBottomLayout.setVisibility(GONE);
            mPortBottomLayout.setVisibility(VISIBLE);
            if (isHasDoc()) {
                mVideoDocSwitch.setVisibility(VISIBLE);
            }

        }


    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initListener();
        // 延时隐藏上下bar
        removeCallbacks(hideRunnable);
        postDelayed(hideRunnable, HIDE_TIME);

        if (getPlayer() != null && getPlayer().isPlaying()) {
            startTimerTask();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTimerTask();
    }


    private void initListener() {
        // 点击整个控件，隐藏上下bar
        setOnClickListener(mRoomAnimatorListener);
        // 进度条设置监听
        // mPlaySeekBar2 = getPlaySeekBar(null);
        // 点击播放暂停
        mPlayIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayerStatus();
            }
        });
        mLandPlayIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayerStatus();
            }
        });


        // 切换文档/视频大小屏
        mVideoDocSwitch.setOnClickListener(onSwitchClickListener);
        mLandVideoDocSwitch.setOnClickListener(onSwitchClickListener);

        // 全屏
        mLiveFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 回调给activity修改ui
                if (replayRoomStatusListener != null) {
                    replayRoomStatusListener.fullScreen();
                }
            }
        });

        // 点击关闭
        mClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (replayRoomStatusListener != null) {
                    replayRoomStatusListener.closeRoom();
                }
            }
        });


        // 重试按钮
        mTryBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTipsLayout.setVisibility(GONE);
                doRetry(false);
            }
        });

        // 倍速切换
        mLandSpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rightView.showRight(ReplayRightView.RIGHT_SHOW_SPEED);
            }
        });

        // 进度条监听
        mPlaySeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mLandPlaySeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }


    // region ---------------------------------内部方法---------------------------------------------

    // 拖拽进度条监听
    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    String formatTime = TimeUtil.getFormatTime(seekBar.getProgress());
                    mCurrentTime.setText(formatTime);
                    mLandCurrentTime.setText(formatTime);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    isTouch = true;
                    start = seekBar.getProgress();
                    removeCallbacks(hideRunnable);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isTouch = false;
                    DWReplayPlayer player = getPlayer();
                    if (player != null) {
                        if (player.isPlaying()) {
                            setPlaySeekBarCanSeek(false);
                        }
                        player.seekTo(seekBar.getProgress());
                    }
                    if (mSeekListener != null && seekBar.getProgress() - start < 0) {
                        mSeekListener.onBackSeek(seekBar.getProgress());
                    }
                    removeCallbacks(hideRunnable);
                    postDelayed(hideRunnable, HIDE_TIME);
                }
            };

    // 切换文档和视频监听
    private final OnClickListener onSwitchClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (replayRoomStatusListener != null) {
                if (isFloatDismiss) {
                    replayRoomStatusListener.openVideoDoc();
                    setSwitchText(false);
                } else {
                    isVideoMain = !isVideoMain;
                    replayRoomStatusListener.switchVideoDoc(isVideoMain);
                    setSwitchText(false);
                }

            }
        }
    };


    // endregion

    // region ---------------------------------对外方法---------------------------------------------

    /**
     * 初始化房间状态
     * <p>
     * ** 需要登录之后调用,包含文档和视频的内部监听
     *
     * @param isVideoMain 是否是视频大窗
     */
    public void init(boolean isVideoMain) {
        this.isVideoMain = isVideoMain;
        //竖屏设置弹框
        settingPopupWindow = new SettingPopupWindow(mContext);
        settingPopupWindow.setSettingPopupInterface(this);
        settingPopupWindow.addSpeed(new float[]{0.5f, 1.0f, 1.25f, 1.5f});
        playInit();
    }

    /**
     * 重试播放
     *
     * @param updateStream 是否更新流
     */
    public void doRetry(boolean updateStream) {
        playRetry(updateStream);
    }

    /**
     * 播放/暂停
     */
    public void changePlayerStatus() {
        playChangeState(getPlaySeekBar().getProgress());
    }

    /**
     * 切换倍速
     */
    public void changePlaySpeed() {
        playChangeSpeed();
    }

    /**
     * 设置文档/视频切换的按钮的文案
     *
     * @param isFloatDismiss 小窗是否关闭
     */
    public void setSwitchText(boolean isFloatDismiss) {
        this.isFloatDismiss = isFloatDismiss;
        if (isFloatDismiss) {
            mVideoDocSwitch.setImageResource(isVideoMain ? R.drawable.open_doc : R.drawable.open_video);
            mLandVideoDocSwitch.setImageResource(isVideoMain ? R.drawable.open_doc : R.drawable.open_video);
        } else {
            mVideoDocSwitch.setImageResource(R.drawable.video_doc_switch);
            mLandVideoDocSwitch.setImageResource(R.drawable.video_doc_switch);
        }
    }

    /**
     * 设置跑马灯数据
     *
     * @param marquee Marquee
     */
    public void setMarquee(final Marquee marquee) {
        final ViewGroup parent = (ViewGroup) mMarqueeView.getParent();
        if (parent.getWidth() != 0 && parent.getHeight() != 0) {
            if (marquee != null && marquee.getAction() != null) {
                if (marquee.getType().equals("text")) {
                    mMarqueeView.setTextContent(marquee.getText().getContent());
                    mMarqueeView.setTextColor(marquee.getText().getColor().replace("0x", "#"));
                    mMarqueeView.setTextFontSize((int) DensityUtil.sp2px(mContext, marquee.getText().getFont_size()));
                    mMarqueeView.setType(1);
                } else {
                    mMarqueeView.setMarqueeImage(mContext, marquee.getImage().getImage_url(), marquee.getImage().getWidth(), marquee.getImage().getHeight());
                    mMarqueeView.setType(2);
                }
                mMarqueeView.setMarquee(marquee, parent.getHeight(), parent.getWidth());
                mMarqueeView.setOnMarqueeImgFailListener(new OnMarqueeImgFailListener() {
                    @Override
                    public void onLoadMarqueeImgFail() {
                        //跑马灯加载失败
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
                                    mMarqueeView.setMarqueeImage(mContext, marquee.getImage().getImage_url(), marquee.getImage().getWidth(), marquee.getImage().getHeight());
                                    mMarqueeView.setType(2);
                                }
                                mMarqueeView.setMarquee(marquee, height, width);
                                mMarqueeView.setOnMarqueeImgFailListener(new OnMarqueeImgFailListener() {
                                    @Override
                                    public void onLoadMarqueeImgFail() {
                                        //跑马灯加载失败
                                    }
                                });
                                mMarqueeView.start();
                            }
                        }
                    });
        }

    }

    /**
     * 是否是視頻大窗/
     */
    public boolean isVideoMain() {
        return isVideoMain;
    }

    /**
     * 设置进度条是否可seek
     */
    protected void setPlaySeekBarCanSeek(boolean isCan) {
        RePlaySeekBar landPlaySeekBar = findViewById(R.id.replay_land_progressbar);
        landPlaySeekBar.setCanSeek(isCan);
        RePlaySeekBar playSeekBar = findViewById(R.id.replay_progressbar);
        playSeekBar.setCanSeek(isCan);
    }

    /**
     * 设置进度条
     *
     * @param max max
     */
    protected void setSeekBarMax(int max) {
        RePlaySeekBar landPlaySeekBar = findViewById(R.id.replay_land_progressbar);
        landPlaySeekBar.setMax(max);
        RePlaySeekBar playSeekBar = findViewById(R.id.replay_progressbar);
        playSeekBar.setMax(max);
    }

    /**
     * seek 相应时间
     *
     * @param progress progress
     */
    public void seekTo(int progress) {
        DWReplayPlayer player = getPlayer();
        if (player != null) {
            if (!player.isPlaying()) {
                playChangeState(progress);
            } else {
                player.seekTo(progress);
                if (mSeekListener != null && progress - getPlaySeekBar().getProgress() < 0) {
                    mSeekListener.onBackSeek(getPlaySeekBar().getProgress());
                }
            }
            setPlayBarProgress(progress);
            removeCallbacks(hideRunnable);
            postDelayed(hideRunnable, HIDE_TIME);
        }


    }

    public void onVideoPrepared() {
        mTipsLayout.setVisibility(GONE);
        mPlayIcon.setSelected(true);
        mLandPlayIcon.setSelected(true);
    }

    /**
     * 设置横屏倍速按钮文本
     *
     * @param speed
     */
    protected void setSpeedText(float speed) {
        String s = "";
        if (speed == 0.5f) {
            s = "0.5x";
        } else if (speed == 1.0f) {
            s = "倍速";
        } else if (speed == 1.25f) {
            s = "1.25x";
        } else if (speed == 1.5f) {
            s = "1.5x";
        }
        mLandSpeed.setText(s);
    }

    // endregion


    //******************************* 定时任务 用于更新进度条等 UI ********************************/

    /**
     * 开启定时器
     */
    protected void startTimerTask() {
        removeCallbacks(runnable);
        post(runnable);
    }

    /**
     * 停止计时器（播放状态、播放时间）
     */
    protected void stopTimerTask() {
        removeCallbacks(runnable);
    }

    public void setTimeText() {
        DWReplayPlayer player = getPlayer();
        if (player == null) return;
        //更新播放器的播放时间
        if (!player.isPlaying() && (player.getDuration() - player.getCurrentPosition() < 500)) {
            setCurrentTime(player.getDuration());
        } else {
            setCurrentTime(player.getCurrentPosition());
        }
        mPlayIcon.setSelected(player.isPlaying());
        mLandPlayIcon.setSelected(player.isPlaying());
    }

    // 设置进度时间显示
    private void setCurrentTime(final long time) {
        long playSecond = Math.round((double) time / 1000) * 1000;
        if (!isTouch && !isSeek) {
            String formatTime = TimeUtil.getFormatTime(getPlaySeekBar().getProgress());
            mCurrentTime.setText(formatTime);
            mLandCurrentTime.setText(formatTime);
            setPlayBarProgress((int) playSecond);
        }
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (getPlayer()!=null&&getPlayer().isInPlaybackState()){
                setTimeText();
            }
            postDelayed(runnable, 1000);
        }
    };


    //********************************* 动画相关方法 ****************************************

    protected OnClickListener mRoomAnimatorListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            removeCallbacks(hideRunnable);
            postDelayed(hideRunnable, HIDE_TIME);
            toggleTopAndButtom();
        }
    };

    protected void hide() {
        mTopLayout.clearAnimation();
        mBottomLayout.clearAnimation();
        ObjectAnimator bottom_y = ObjectAnimator.ofFloat(mBottomLayout, "translationY", mBottomLayout.getHeight());
        ObjectAnimator top_y = ObjectAnimator.ofFloat(mTopLayout, "translationY", -1 * mTopLayout.getHeight());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(top_y).with(bottom_y);
        //播放动画的持续时间
        animatorSet.setDuration(500);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mBottomLayout.setVisibility(GONE);
                mTopLayout.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

    protected void show() {
        mTopLayout.clearAnimation();
        mBottomLayout.clearAnimation();
        mTopLayout.setVisibility(VISIBLE);
        mBottomLayout.setVisibility(VISIBLE);
        ObjectAnimator bottom_y = ObjectAnimator.ofFloat(mBottomLayout, "translationY", 0);
        ObjectAnimator top_y = ObjectAnimator.ofFloat(mTopLayout, "translationY", 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(top_y).with(bottom_y);
        //播放动画的持续时间
        animatorSet.setDuration(500);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {


            }

            @Override
            public void onAnimationRepeat(Animator animator) {


            }
        });
        postDelayed(hideRunnable, HIDE_TIME);
    }

    protected void toggleTopAndButtom() {
        if (mTopLayout.isShown()) {
            hide();
        } else {
            show();
        }
    }

    //********************************* 进度条seek相关 ****************************************

    // 获取当前界面显示的seekbar
    protected RePlaySeekBar getPlaySeekBar() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏
            return mPlaySeekBar;
        } else {
            return mLandPlaySeekBar;
        }
    }

    /**
     * 设置进度条位置
     *
     * @param progress progress
     */
    protected void setPlayBarProgress(int progress) {
        mPlaySeekBar.setProgress(progress);
        mLandPlaySeekBar.setProgress(progress);
    }

    /**
     * 设置二级进度条位置
     *
     * @param progress progress
     */
    protected void setPlayBarSecondaryProgress(int progress) {
        mPlaySeekBar.setSecondaryProgress(progress);
        mLandPlaySeekBar.setSecondaryProgress(progress);
    }

    protected float downX;
    protected float downY;
    protected float moveX;
    protected VelocityTracker mVelocityTracker = null;
    protected boolean isSeek = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((getDocumentDisplayMode() == 1||getDocumentDisplayMode() == 0) && mTipsLayout.getVisibility() != VISIBLE && getPlaySeekBar().isCanSeek()) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    moveX = event.getX();
                    downY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getX();
                    float moveY = event.getY();
                    if (Math.abs(downY - moveY) + 10 < Math.abs(moveX - this.downX)) {
                        if (getPlayer() == null) {
                            break;
                        }
                        if (Math.abs(moveX - this.downX) > 10 && getPlayer().isInPlaybackState()) {
                            stopTimerTask();
                            mVelocityTracker.computeCurrentVelocity(1000);
                            seek((moveX - this.moveX) * 1000, false, mVelocityTracker.getXVelocity(0));

                            if (mSeekRoot.getVisibility() != VISIBLE)
                                mSeekRoot.setVisibility(VISIBLE);
                            mSeekTime.setText(TimeUtil.getFormatTime(getPlaySeekBar().getProgress()));
                            if (TextUtils.isEmpty(mSumTime.getText()) || mSumTime.getText().equals("00:00")) {
                                mSumTime.setText(TimeUtil.getFormatTime(getPlaySeekBar().getMax()));
                            }
                            this.moveX = moveX;
                            //手势拖拽显示进度条
                            if (mTopLayout.getVisibility() != VISIBLE) {
                                show();
                            }
                            removeCallbacks(hideRunnable);
                            isSeek = true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    float upX = event.getX();
                    if (isSeek) {
                        if (Math.abs(upX - downX) > 10) {
                            seek((upX - downX), true, mVelocityTracker.getXVelocity(0));
                            startTimerTask();
                        }
                        postDelayed(hideRunnable, HIDE_TIME);
                        isSeek = false;
                        if (mSeekRoot.getVisibility() != GONE)
                            mSeekRoot.setVisibility(GONE);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if (mSeekRoot.getVisibility() != GONE)
                        mSeekRoot.setVisibility(GONE);
                    break;
            }
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    performClick();
                    return false;

            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }


    protected void seek(float move, boolean isSeek, float xVelocity) {
        int max = getPlaySeekBar().getMax();
        int progress = getPlaySeekBar().getProgress();
        DWReplayPlayer player = getPlayer();
        if (progress + move < 0) {
            setPlayBarProgress(0);
        } else if (progress + move >= max) {
            setPlayBarProgress(max);
        } else if (progress + move < max) {
            setPlayBarProgress((int) (progress + ((xVelocity * 10))));
        }
        if (isSeek && player != null) {
            player.seekTo(getPlaySeekBar().getProgress());
        }
    }

    //********************************* 设置进度回调方法 ****************************************

    protected ReplayRoomLayout.SeekListener mSeekListener;

    /**
     * 设置进度监听
     *
     * @param listener listener
     */
    public void setSeekListener(ReplayRoomLayout.SeekListener listener) {
        mSeekListener = listener;
    }

    public interface SeekListener {
        void onBackSeek(long progress);
    }


    //****************************** 回放直播间状态监听 用于Activity更新UI *************************

    public abstract static class ReplayRoomStatusListener {

        /**
         * 视频/文档 切换
         */
        public abstract void switchVideoDoc(boolean isVideoMain);

        /**
         * 打开小窗
         */
        public abstract void openVideoDoc();

        /**
         * 退出直播间
         */
        public abstract void closeRoom();

        /**
         * 进入全屏
         */
        public abstract void fullScreen();

        /**
         * 点击文档类型
         */
        public void onClickDocScaleType(int scaleType) {
        }

        /**
         * 切换音视频模式
         *
         * @param playMode playMode
         */
        public void onChangePlayMode(DWLiveReplay.PlayMode playMode) {
        }


    }

    // 回放直播间状态监听
    protected ReplayRoomLayout.ReplayRoomStatusListener replayRoomStatusListener;

    /**
     * 设置回放直播间状态监听
     *
     * @param listener 回放直播间状态监听
     */
    public void setReplayRoomStatusListener(ReplayRoomStatusListener listener) {
        this.replayRoomStatusListener = listener;
    }


    // ****************************** 视频打点功能 ********************************************
    public void initDotView() {
        if (dotBeans == null) {
            // ELog.i(TAG, "dotBeans is null");
            return;
        }
        if (!getPlaySeekBar().isCanSeek()) {
            // ELog.i(TAG, "mLandPlaySeekBar is not CanSeek");
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                if (DWReplayCoreHandler.getInstance().getPlayer()==null){
                    return;
                }
                final long duration = DWReplayCoreHandler.getInstance().getPlayer().getDuration();
                if (duration == 0) {
                    ELog.i(TAG, "player duration = 0");
                    return;
                }
                int measuredWidth = mLandPlaySeekBar.getMeasuredWidth();
                if (measuredWidth == 0) {
                    ELog.i(TAG, "measuredWidth=" + measuredWidth + ",mLandPlaySeekBar.isShown:" + mLandPlaySeekBar.isShown());
                    return;
                }
                FrameLayout seekBarGrandParent = (FrameLayout) mLandPlaySeekBar.getParent();
                List<DotView> dotViewList = new ArrayList<>();
                for (ReplayDot dotBean : dotBeans) {
                    if (dotBean.getTime() * 1000 > duration) {
                        continue;
                    }
                    DotView mDotView = new DotView(getContext());
                    mDotView.setDotTime(dotBean.getTime());
                    mDotView.setDotMsg(dotBean.getDesc());
                    dotViewList.add(mDotView);
                    seekBarGrandParent.removeView(mDotView);
                    seekBarGrandParent.addView(mDotView);
                }
                for (final DotView dotView : dotViewList) {
                    reLayoutDotView(duration, dotView);
                }
            }

        });

    }

    private void reLayoutDotView(long mDuration, final DotView dotView) {
        int measuredWidth = mLandPlaySeekBar.getWidth();
        if (mDuration <= 0 || measuredWidth <= 0) {
            return;
        }
        int currentPosition = dotView.getDotTime();
        int seekBarWidth = mLandPlaySeekBar.getProgressDrawable().getBounds().width();
        int thumbWidth = mLandPlaySeekBar.getThumb().getBounds().width();
        final double x_position = (seekBarWidth * 1.00) / mDuration * currentPosition * 1000;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) dotView.getLayoutParams();
        layoutParams.leftMargin = (int) Math.round(mLandPlaySeekBar.getPaddingLeft() + (thumbWidth >> 1) + x_position - (dotView.getRootWidth() >> 1));
        layoutParams.height = mLandPlaySeekBar.getHeight();
        dotView.setLayoutParams(layoutParams);
        dotView.setOnDotViewClickListener(new DotView.OnDotViewClickListener() {
            @Override
            public void onDotViewClick() {
                int[] position = new int[2];
                dotView.getLocationInWindow(position);
                removeCallbacks(hideRunnable);
                // 打点弹出框
                popupWindow = new DotMsgPopupWindow(getContext(), dotView, position[0], position[1]);
                popupWindow.show(mLandPlaySeekBar);
                popupWindow.setOnDotViewMsgClickListener(new DotMsgPopupWindow.OnDotViewMsgClickListener() {
                    @Override
                    public void onDotViewMsgClick() {
                        seekTo(dotView.getDotTime() * 1000);

                    }
                });

            }
        });
    }


    //********************************* 需要实现的方法 ****************************************

    /**
     * 初始化
     */
    protected abstract void playInit();

    /**
     * 重试播放
     *
     * @param updateStream 是否更新流
     */
    protected abstract void playRetry(boolean updateStream);

    /**
     * 修改播放状态
     */
    protected abstract void playChangeState(long progress);

    /**
     * 修改播放倍速
     */
    protected abstract void playChangeSpeed();

    /**
     * 获取播放器对象
     */
    protected abstract DWReplayPlayer getPlayer();

    /**
     * 获取文档展示模式
     *
     * @return 文档窗口状态：1——适合窗口，2——适合宽度
     */
    protected abstract int getDocumentDisplayMode();

    protected abstract boolean isHasDoc();

    @Override
    public void onSpeed(float speed) {

    }

    @Override
    public void onPlayMode(boolean isAudio, SettingPopupWindowCallBack callBack) {

    }

    @Override
    public void onQuality(int quality, String desc, SettingPopupWindowCallBack callBack) {

    }

    @Override
    public void onLine(int line, SettingPopupWindowCallBack callBack) {

    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        settingPopupWindow.setActivity(activity);
    }
}
