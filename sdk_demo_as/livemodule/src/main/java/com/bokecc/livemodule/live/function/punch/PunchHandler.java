package com.bokecc.livemodule.live.function.punch;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.bokecc.livemodule.live.function.punch.view.PunchDialog;
import com.bokecc.livemodule.live.function.punch.view.PunchResultDialog;
import com.bokecc.sdk.mobile.live.pojo.PunchAction;

/**
 * '投票' 处理机制
 */
public class PunchHandler {

    private PunchDialog mPunchPopup;

    private Context context;
    private PunchResultDialog punchResultDialog;
    private PunchDialog.PunchListener punchListener = new PunchDialog.PunchListener() {
        @Override
        public void onEnd() {
            showResult(true);
        }

        @Override
        public void onSuccess() {
            //打卡成功 进行提示
            showResult(false);
        }
    };
    private View view;

    private void showResult(boolean isEnd) {
        //打卡失败 进行提示
        if (punchResultDialog != null && punchResultDialog.isShowing()) {
            punchResultDialog.dismiss();
        }
        if (punchResultDialog != null) {
            punchResultDialog = null;
        }
        punchResultDialog = new PunchResultDialog(context);
        punchResultDialog.show(view, isEnd);
    }

    /**
     * 初始化投票功能
     */
    public void initPunch(Context context) {
        this.context = context;
    }

    /**
     * 显示打卡
     */
    public void startPunch(View root, PunchAction punchAction) {
        if (punchAction == null){
            return;
        }
        if (punchAction.getType()== PunchAction.Action.STOP_PUNCH){
            //如果收到停止打卡  并且当前打卡正在展示 需要提示打卡结束
            // 针对未收到开始打卡并且收到停止打卡的不处理
            if (mPunchPopup!=null&&mPunchPopup.isSame(punchAction.getId())){
                stopPunch("打卡结束");
            }
        }else{
            //如果当前的打卡正在展示  不处理
            if (mPunchPopup!=null&&mPunchPopup.isSame(punchAction.getId())
                &&mPunchPopup.isShowing()){
                return;
            }
            if (mPunchPopup!=null&&mPunchPopup.isShowing()){
                mPunchPopup.dismiss();
            }
            if (mPunchPopup!=null){
                mPunchPopup = null;
            }
            mPunchPopup = new PunchDialog(context,punchListener);
            if (punchAction!=null){
                if (TextUtils.isEmpty(punchAction.getTips())){
                    punchAction.setTips("各位同学开始签到");
                }else{
                    punchAction.setTips(punchAction.getTips());
                }
            }else{
                punchAction.setTips("各位同学开始签到");
            }
            int time = punchAction.getRemainDuration();
            if (time <= 0) {
                return;
            }
            view =root;
            mPunchPopup.show(root,punchAction,time);
        }
    }

    /**
     * 结束签到
     *
     * @param message message
     */
    public void stopPunch(String message) {
        if (mPunchPopup != null && mPunchPopup.isShowing()) {
            mPunchPopup.destroy(message);
        }
    }


}
