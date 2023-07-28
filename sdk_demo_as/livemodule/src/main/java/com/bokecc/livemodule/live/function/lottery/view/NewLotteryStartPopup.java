package com.bokecc.livemodule.live.function.lottery.view;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bumptech.glide.Glide;

/**
 * 抽奖开始弹出框
 */
public class NewLotteryStartPopup extends BasePopupWindow {
    private String lotteryId;
    public NewLotteryStartPopup(Context context) {
        super(context);
    }

    ImageView closeBtn;

    ImageView loadingIcon;


    @Override
    protected void onViewCreated() {
        closeBtn = findViewById(R.id.iv_lottery_close);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        loadingIcon = findViewById(R.id.iv_lottery_loading);

        Glide.with(mContext).load(R.drawable.lottery_loading_gif).asGif().thumbnail(0.1f).into(loadingIcon);

    }

    @Override
    protected int getContentView() {
        return R.layout.new_lottery_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }

    public void setLotteryId(String lotteryId) {
        this.lotteryId=lotteryId;
    }
}
