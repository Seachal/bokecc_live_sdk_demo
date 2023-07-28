package com.bokecc.livemodule.live.function.lottery.view;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;

/**
 * 抽奖开始弹出框
 */
public class NewLotteryCancelPopup extends BasePopupWindow {
    public NewLotteryCancelPopup(Context context) {
        super(context);
    }
    Button mConfirm;

    @Override
    protected void onViewCreated() {
        mConfirm = findViewById(R.id.bt_confirm);
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.new_lottery_cancel_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }
}
