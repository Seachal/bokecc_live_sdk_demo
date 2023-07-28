package com.bokecc.livemodule.replay.room.rightview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bokecc.livemodule.live.room.rightview.LiveQualityView;
import com.bokecc.livemodule.utils.DensityUtil;
import com.bokecc.livemodule.view.CustomToast;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLineInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQualityinfo;

import java.util.List;

/**
 * 回放右侧弹出界面
 */
public class ReplayRightView extends LinearLayout {
    public static final int RIGHT_SHOW_LINE = 1;       // 展示线路
    public static final int RIGHT_SHOW_QUALITY = 2;    // 展示清晰度
    public static final int RIGHT_SHOW_SPEED = 3;      // 展示倍速
    public static final int RIGHT_HIDE = 4;            // 隐藏右侧布局

    private ReplayLineView rightLineView;        // 右侧线路切换布局
    private ReplaySpeedView rightSpeedView;      // 右侧倍速布局
    private ReplayQualityView rightQualityView;  // 右侧清晰度布局

    // 右侧布局弹出偏移量
    private final static int translationX = 150;
    // 倍速播放
    private final float[] speeds = new float[]{0.5f, 1.0f, 1.25f, 1.5f};
    // 各种切换的回调
    private RightCallBack rightCallBack;

    private final Animator.AnimatorListener hideListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            removeAllViews();
            setVisibility(GONE);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };


    public ReplayRightView(Context context) {
        super(context);
        initView(context);
    }

    public ReplayRightView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ReplayRightView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        // 点击外部隐藏
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRight(ReplayRightView.RIGHT_HIDE);
            }
        });
        // 初始化线路View
        rightLineView = new ReplayLineView(getContext());
        // 初始化清晰度View
        rightQualityView = new ReplayQualityView(getContext());
        // 初始化倍速View
        rightSpeedView = new ReplaySpeedView(getContext());
        rightSpeedView.setData(speeds);
        rightLineView.setLineCallBack(new ReplayLineView.LineCallBack() {
            @Override
            public void onLineChange(int line) {
                if (rightCallBack != null) {
                    rightCallBack.onChangeLine(line);
                }
                showRight(RIGHT_HIDE);
                CustomToast.showToast(getContext(), "切换成功", Toast.LENGTH_SHORT);
            }
        });
        rightLineView.setPlayModeCallBack(new ReplayLineView.PlayModeCallBack() {
            @Override
            public void onPlayModeChange(DWLiveReplay.PlayMode playMode) {
                if (rightCallBack != null) {
                    rightCallBack.onChangePlayMode(playMode);
                }
                showRight(RIGHT_HIDE);
                CustomToast.showToast(getContext(), "切换成功", Toast.LENGTH_SHORT);
            }
        });

        rightQualityView.setQualityCallBack(new LiveQualityView.QualityCallBack() {
            @Override
            public void qualityChange(int quality,String qualityDesc) {
                showRight(RIGHT_HIDE);
                CustomToast.showToast(getContext(), "切换成功", Toast.LENGTH_SHORT);
                if (rightCallBack != null) {
                    rightCallBack.onChangeQuality(quality,qualityDesc);
                }
            }
        });

        rightSpeedView.setQualityCallBack(new ReplaySpeedView.SpeedCallBack() {
            @Override
            public void speedChange(float speed) {
                if (rightCallBack != null) {
                    rightCallBack.onSpeedChange(speed);
                }
                showRight(ReplayRightView.RIGHT_HIDE);
            }
        });
    }

    /**
     * 设置切换成功和失败的回调
     *
     * @param rightCallBack LiveRightCallBack
     */
    public void setRightCallBack(RightCallBack rightCallBack) {
        this.rightCallBack = rightCallBack;
    }

    /**
     * 展示右边布局
     *
     * @param type 1 线路  2清晰度  3倍速  4 隐藏
     */
    public void showRight(final int type) {
        //动画
        AnimatorSet animatorSet;
        if (type == RIGHT_HIDE) {
            if (getVisibility() == VISIBLE) {
                ObjectAnimator oa = ObjectAnimator.ofFloat(this, "translationX", 0, getWidth());
                //隐藏动画
                oa.setDuration(500);
                animatorSet = new AnimatorSet();
                animatorSet.playTogether(oa);
                animatorSet.addListener(hideListener);
                animatorSet.start();
                if (rightCallBack != null) {
                    rightCallBack.onClose();
                }
            }
        } else {
            removeAllViews();
            if (type == RIGHT_SHOW_LINE) {
                addView(rightLineView);
            } else if (type == RIGHT_SHOW_QUALITY) {
                addView(rightQualityView);
            } else if (type == RIGHT_SHOW_SPEED) {
                addView(rightSpeedView);
            }
            if (getVisibility() == GONE) {
                setVisibility(VISIBLE);
            }
            animatorSet = new AnimatorSet();
            ObjectAnimator show = ObjectAnimator.ofFloat(this, "translationX",
                    DensityUtil.getWidth(getContext()) - DensityUtil.dp2px(getContext(), translationX), 0);
            show.setDuration(500);
            animatorSet.playSequentially(show);
            animatorSet.start();
        }
    }

    /**
     * 设置是否显示音频切换
     */
    public void showAudio(DWLiveReplay.Audio hasAudio) {
        //如果没有音频线路  隐藏音频切换开关
        if (hasAudio == DWLiveReplay.Audio.HAVE_AUDIO_LINE_TURE) {
            rightLineView.showSwitch();
        } else {
            rightLineView.hideSwitch();
        }
    }

    /**
     * 展示清晰度
     *
     * @param videoQuality   清晰度列表
     * @param currentQuality 当前清晰度
     */
    public void showQuality(List<ReplayQualityinfo> videoQuality, ReplayQualityinfo currentQuality) {
        rightQualityView.setData(videoQuality, currentQuality);
    }

    /**
     * 显示线路数
     *
     * @param lines    所有线路
     * @param indexNum 被选中线路 默认0
     */
    public void showVideoAudioLines(List<ReplayLineInfo> lines, int indexNum) {
        rightLineView.setData(lines, indexNum);
    }

    /**
     * 设置倍速
     */
    public void setSpeed(float speed){
        rightSpeedView.setCurrentSpeed(speed);
    }

    public void setQuality(int quality) {
        rightQualityView.setQuality(quality);
    }

    public void setLine(int line) {
        rightLineView.setLine(line);
    }

    public void setMode(boolean isAudio) {
        rightLineView.setMode(isAudio);
    }

    /**
     * 切换的回调
     */
    public interface RightCallBack {
        void onChangePlayMode(DWLiveReplay.PlayMode playMode);

        void onChangeQuality(int quality,String qualityDesc);

        void onChangeLine(int line);

        void onSpeedChange(float speed);

        void onClose();
    }

}
