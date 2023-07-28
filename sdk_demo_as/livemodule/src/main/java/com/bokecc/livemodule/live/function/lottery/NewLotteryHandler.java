package com.bokecc.livemodule.live.function.lottery;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.bokecc.livemodule.live.function.lottery.view.NewLotteryCancelPopup;
import com.bokecc.livemodule.live.function.lottery.view.NewLotteryResultPopup;
import com.bokecc.livemodule.live.function.lottery.view.NewLotteryStartPopup;
import com.bokecc.livemodule.view.CustomToast;
import com.bokecc.sdk.mobile.live.pojo.LotteryAction;

/**
 * '抽奖' 处理机制
 */
public class NewLotteryHandler {


    private NewLotteryStartPopup mNewLotteryStartPopup;
    private NewLotteryResultPopup mNewLotteryResultPopup;
    private NewLotteryCancelPopup mNewLotteryCancelPopup;

    /**
     * 初始化抽奖功能
     */
    public void initLottery(Context context) {
        mNewLotteryStartPopup = new NewLotteryStartPopup(context);
        mNewLotteryStartPopup.setKeyBackCancel(true);
        mNewLotteryResultPopup = new NewLotteryResultPopup(context);
        mNewLotteryResultPopup.setKeyBackCancel(true);
        mNewLotteryCancelPopup = new NewLotteryCancelPopup(context);
        mNewLotteryCancelPopup.setKeyBackCancel(true);
    }

    /**
     * 开始抽奖
     */
    private void startLottery(View root, String lotteryId) {
        if (mNewLotteryResultPopup.isShowing()) {
            mNewLotteryResultPopup.dismiss();
        }
        mNewLotteryStartPopup.setLotteryId(lotteryId);
        mNewLotteryStartPopup.show(root);
    }

    /**
     * 展示抽奖结果
     */
    private void showLotteryResult(View root, final LotteryAction lotteryAction) {
        if (mNewLotteryStartPopup.isShowing()) {
            mNewLotteryStartPopup.dismiss();
        }
        if (!mNewLotteryResultPopup.isShowing()) {
            mNewLotteryResultPopup.setLotteryResult(lotteryAction);
            mNewLotteryResultPopup.show(root);
        } else {
            //如果重复收到相同的数据  不进行处理
            if (mNewLotteryResultPopup.getLotteryAction().getLotteryId().equals(lotteryAction.getLotteryId())
                    && mNewLotteryResultPopup.getLotteryAction().getLotteryStatus() == lotteryAction.getLotteryStatus()) {
                return;
            } else {
                mNewLotteryResultPopup.dismiss();
                mNewLotteryResultPopup.setLotteryResult(lotteryAction);
                mNewLotteryResultPopup.show(root);
            }
        }
    }

    /**
     * 取消抽奖
     */
    private void cancelLottery(View root) {
        if (mNewLotteryStartPopup.isShowing()) {
            mNewLotteryStartPopup.dismiss();
        }
        if (mNewLotteryResultPopup.isShowing()) {
            mNewLotteryResultPopup.dismiss();
        }
        if (!mNewLotteryCancelPopup.isShowing()){
            mNewLotteryCancelPopup.show(root);
        }
    }

    public void onLottery(View root, LotteryAction lotteryAction) {
        if (!lotteryAction.isHaveLottery()){
            if (mNewLotteryStartPopup.isShowing()) {
                mNewLotteryStartPopup.dismiss();
            }
            return;
        }
        if (lotteryAction.getLotteryStatus() == 0) {
            //展示正在抽奖
            startLottery(root, lotteryAction.getLotteryId());
        } else if (lotteryAction.getLotteryStatus() == 1) {
            //取消抽奖
            cancelLottery(root);
        } else if (lotteryAction.getLotteryStatus() == 2) {
            //展示抽奖结果
            showLotteryResult(root, lotteryAction);
        } else if (lotteryAction.getLotteryStatus() == 3) {
            //抽奖异常结束
            CustomToast.showToast(root.getContext(), "抽奖异常结束", Toast.LENGTH_SHORT);
        }
    }
}
