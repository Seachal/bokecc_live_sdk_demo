package com.bokecc.livemodule.live.room.rightview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.bokecc.livemodule.utils.DensityUtil;
import com.bokecc.livemodule.view.CustomToast;
import com.bokecc.livemodule.view.RightBaseView;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.LiveLineInfo;
import com.bokecc.sdk.mobile.live.pojo.LiveQualityInfo;

import java.util.List;

public class LiveRightView extends RightBaseView {

    public static final int RIGHT_SHOW_LINE = 1;     // 展示线路
    public static final int RIGHT_SHOW_QUALITY = 2;  // 展示清晰度
    public static final int RIGHT_SHOW_BARRAGE = 3;  // 展示弹幕设置
    public static final int RIGHT_HIDE = 4;          // 隐藏右侧布局

    private LiveLineView rightLineView;         // 右侧线路切换布局
    private LiveQualityView rightQualityView;   // 右侧清晰度布局
    private LiveBarrageView rightBarrageView;   // 右侧弹幕设置布局


    private LiveRightCallBack rightCallBack;

    private final static int translationX = 150;       // 右侧布局弹出偏移量

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

    public LiveRightView(Context context) {
        super(context);
    }

    public LiveRightView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LiveRightView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void initViews() {
        // 点击外部隐藏
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRight(RIGHT_HIDE);
            }
        });
        // 初始化线路View
        rightLineView = new LiveLineView(getContext());
        // 初始化清晰度View
        rightQualityView = new LiveQualityView(getContext());
        //初始化弹幕设置view
        rightBarrageView = new LiveBarrageView(getContext());

        rightLineView.setLineCallBack(new LiveLineView.LineCallBack() {
            @Override
            public void onLineChange(int line) {
                showRight(RIGHT_HIDE);
                CustomToast.showToast(getContext(), "切换成功", Toast.LENGTH_SHORT);
                if (rightCallBack != null) {
                    rightCallBack.onChangeLine(line);
                }
            }

        });
        rightLineView.setPlayModeCallBack(new LiveLineView.PlayModeCallBack() {
            @Override
            public void onPlayModeChange(DWLive.LivePlayMode playMode) {
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
                if (rightCallBack != null) {
                    rightCallBack.onChangeQuality(quality,qualityDesc);
                }
                showRight(RIGHT_HIDE);
                CustomToast.showToast(getContext(), "切换成功", Toast.LENGTH_SHORT);
            }
        });
        rightBarrageView.setBarrageCallBack(new LiveBarrageView.BarrageCallBack() {
            @Override
            public void barrageChange(int type) {
                if (rightCallBack != null) {
                    rightCallBack.onChangeBarrage(type);
                }
                showRight(RIGHT_HIDE);
            }
        });
    }


    /**
     * 设置切换成功和失败的回调
     *
     * @param rightCallBack LiveRightCallBack
     */
    public void setRightCallBack(LiveRightCallBack rightCallBack) {
        this.rightCallBack = rightCallBack;
    }

    /**
     * 展示右边布局
     *
     * @param type RIGHT_SHOW_LINE：线路  RIGHT_SHOW_QUALITY：清晰度    RIGHT_HIDE：隐藏
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
                if (rightCallBack!=null){
                    rightCallBack.onClose();
                }
            }
        } else {
            removeAllViews();
            if (type == RIGHT_SHOW_LINE) {
                addView(rightLineView);
            } else if (type == RIGHT_SHOW_QUALITY) {
                addView(rightQualityView);
            }else if (type == RIGHT_SHOW_BARRAGE){
                addView(rightBarrageView);
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
    public void showAudio(boolean isShowAudio) {
        if (isShowAudio) {
            rightLineView.showSwitch();
        } else {
            rightLineView.hideSwitch();
        }

    }
    public void setQuality(int quality){
        rightQualityView.setQuality(quality);
    }
    /**
     * 显示清晰度列表，默认值
     */
    public void showVideoQuality(List<LiveQualityInfo> videoQuality, LiveQualityInfo currentQuality) {
        rightQualityView.setData(videoQuality, currentQuality);
    }

    /**
     * 显示线路数
     *
     * @param lines    所有线路
     * @param indexNum 被选中线路 默认0
     */
    public void showVideoAudioLines(List<LiveLineInfo> lines, int indexNum) {
        rightLineView.setData(lines, indexNum);
    }
    //设置线路
    public void setLine(int line){
        rightLineView.setLine(line);
    }
    //设置播放模式
    public void setMode(boolean isAudio){
        rightLineView.setMode(isAudio);
    }
    /**
     * 切换的回调
     */
    public interface LiveRightCallBack {

        void onChangePlayMode(DWLive.LivePlayMode playMode);

        void onChangeQuality(int quality,String qualityDesc);
        void onChangeBarrage(int type);
        void onChangeLine(int line);
        void onClose();
    }


}
