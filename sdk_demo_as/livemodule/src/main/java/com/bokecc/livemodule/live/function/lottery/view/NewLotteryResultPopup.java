package com.bokecc.livemodule.live.function.lottery.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.function.ClickCallBack;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bokecc.livemodule.view.LotteryMessageItemView;
import com.bokecc.sdk.mobile.live.BaseCallback;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.LotteryAction;
import com.bokecc.sdk.mobile.live.pojo.LotteryCollectTemplate;
import com.bokecc.sdk.mobile.live.pojo.LotteryCommitInfo;
import com.bokecc.sdk.mobile.live.util.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 中奖弹出框
 */
public class NewLotteryResultPopup extends BasePopupWindow {
    //中奖展示的跟布局
    private LinearLayout mWinRoot;
    //未中奖展示的跟布局
    private LinearLayout mNotWinRoot;
    //中奖信息
    private LotteryAction lotteryAction;
    //中奖奖品名称
    private TextView mTvGift;
    private TextView mNotWinGift;
    //中奖码
    private TextView mLotteryCode;
    //中奖展示的数据跟布局
    private LinearLayout mContentRoot;
    //提交提示错误
    private TextView mError;
    //提交
    private Button mSubmit;
    private final String NET_ERROR = "网络异常，请稍后再试";
    private final String TEXT_NOT_CORRECT = "请输入正确的手机号";
    private final String NOT_WIN_LEFT = "很遗憾，您没有获得【";
    private final String LEFT = "恭喜您获得了【";
    private final String NOT_WIN_RIGHT = "】";
    private final String RIGHT = "】,请牢记您的中奖码";
    private RelativeLayout mRemindRoot;
    private LinearLayout mRemindSuccess;
    private LinearLayout mRemindFail;
    private Handler handler = new Handler(Looper.getMainLooper());
    private DelayAction delayAction;
    private long showTime;
    private ImageView mArrows;
    private LotteryUserAdapter lotteryUserAdapter;
    private RecyclerView lotteryList;
    private ImageView mClose;
    private LinearLayout mUserRoot;
    private LinearLayout mArrowRoot;
    //0是默认状态    1是正在提交    2是提交完成
    private int status = 0;
    private TextView mTips;

    public NewLotteryResultPopup(Context context) {
        super(context);
    }

    private boolean isOpen = false;

    private BaseCallback<String> commitCallBack = new BaseCallback<String>() {
        @Override
        public void onError(final String error) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mSubmit.setClickable(true);
                    mError.setVisibility(View.VISIBLE);
                    mError.setText(error);
                    status = 0;
                }
            });
        }

        @Override
        public void onSuccess(String msg) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mSubmit.setClickable(false);
                    mRemindRoot.setVisibility(View.VISIBLE);
                    mRemindSuccess.setVisibility(View.VISIBLE);
                    if (delayAction != null) {
                        delayAction.cancel();
                        handler.removeCallbacks(delayAction);
                    }
                    delayAction = new DelayAction();
                    delayAction.setSuccess(true);
                    status = 2;
                    handler.postDelayed(delayAction, 3000);
                }
            });
        }
    };

    @Override
    protected void onViewCreated() {
        mWinRoot = findViewById(R.id.ll_lottery_winning);
        mNotWinRoot = findViewById(R.id.ll_lottery_notwin);
        mTvGift = findViewById(R.id.tv_lottery_gift);
        mNotWinGift = findViewById(R.id.tv_lottery_nowin);
        mLotteryCode = findViewById(R.id.lottery_code);
        mTips = findViewById(R.id.tv_lottery_tip);
        mContentRoot = findViewById(R.id.ll_lottery_content);
        mError = findViewById(R.id.tv_lottery_error);
        mSubmit = findViewById(R.id.btn_Lottery_commit);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - showTime > 1000 * 60 * 30) {
                    mRemindRoot.setVisibility(View.VISIBLE);
                    mRemindFail.setVisibility(View.VISIBLE);
                    if (delayAction != null) {
                        delayAction.cancel();
                        handler.removeCallbacks(delayAction);
                    }
                    delayAction = new DelayAction();
                    delayAction.setSuccess(false);
                    handler.postDelayed(delayAction, 3000);
                    return;
                }
                if (NetworkUtils.isNetworkAvailable(mContext)) {
                    List<LotteryCommitInfo> commitInfos = new ArrayList<>();
                    if (mContentRoot.getChildCount() > 0) {
                        for (int i = 0; i < mContentRoot.getChildCount(); i++) {
                            View childAt = mContentRoot.getChildAt(i);
                            if (childAt instanceof LotteryMessageItemView) {
                                LotteryMessageItemView child = (LotteryMessageItemView) childAt;
                                if (child.checkContent() == 1) {
                                    mError.setVisibility(View.VISIBLE);
                                    mError.setText(TEXT_NOT_CORRECT);
                                    return;
                                } else if (child.checkContent() == 2) {
                                    mError.setVisibility(View.VISIBLE);
                                    mError.setText(String.format("%s不能为空", child.getLotteryCollectTemplate().getTitle()));
                                    return;
                                }
                                LotteryCommitInfo commitInfo = new LotteryCommitInfo();
                                commitInfo.setIndex(child.getLotteryCollectTemplate().getIndex());
                                commitInfo.setValue(child.getContent());
                                commitInfos.add(commitInfo);
                            }
                        }
                    }
                    status = 1;
                    mSubmit.setClickable(false);
                    DWLive.getInstance().commitLottery(lotteryAction.getLotteryId(), commitInfos, commitCallBack);
                } else {
                    mError.setVisibility(View.VISIBLE);
                    mError.setText(NET_ERROR);
                }
            }
        });
        mRemindRoot = findViewById(R.id.rl_lottery_remind_root);
        mRemindSuccess = findViewById(R.id.ll_lottery_success);
        mRemindFail = findViewById(R.id.ll_lottery_error);
        mUserRoot = findViewById(R.id.ll_user_root);
        mArrowRoot = findViewById(R.id.ll_arrow_root);
        mArrows = findViewById(R.id.iv_lottery_arrows);
        mClose = findViewById(R.id.iv_lottery_close);
        lotteryList = findViewById(R.id.rv_lottery_users);
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 4, LinearLayoutManager.VERTICAL, false);
        lotteryList.setLayoutManager(layoutManager);
        lotteryUserAdapter = new LotteryUserAdapter(mContext, null);
        lotteryList.setAdapter(lotteryUserAdapter);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - showTime > 1000 * 60 * 30) {
                    dismiss();
                    return;
                }
                if (status == 2) {
                    dismiss();
                } else {
                    NewLotteryRemindPopup newLotteryRemindPopup = new NewLotteryRemindPopup(mContext);
                    newLotteryRemindPopup.setClickCallBack(new ClickCallBack() {
                        @Override
                        public void onConfirm() {
                            if (delayAction != null) {
                                delayAction.cancel();
                                handler.removeCallbacks(delayAction);
                            }
                            status = 0;
                            NewLotteryResultPopup.this.dismiss();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    newLotteryRemindPopup.show(rootView);
                }

            }
        });
        mArrowRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    lotteryList.setVisibility(View.GONE);
                    mArrows.setImageResource(R.drawable.lottery_down);
                } else {
                    lotteryList.setVisibility(View.VISIBLE);
                    mArrows.setImageResource(R.drawable.lottery_up);
                }
                isOpen = !isOpen;
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.newlottery_result_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }

    public void setLotteryResult(LotteryAction lotteryAction) {
        this.lotteryAction = lotteryAction;
        showTime = System.currentTimeMillis();
        mSubmit.setClickable(true);
        mContentRoot.removeAllViews();
        mError.setVisibility(View.GONE);
        if (lotteryAction.getLotteryWinInfo() != null) {
            //这个位置赋值是为了容错 如果没有数据 默认的状态就是已提交
            status = 2;
            if (lotteryAction.getLotteryWinInfo().isWinner()) {
                mWinRoot.setVisibility(View.VISIBLE);
                mNotWinRoot.setVisibility(View.GONE);
                //中奖设置
                //礼物名称
                if (lotteryAction.getLotteryWinInfo().getPrize() != null) {
                    String s = LEFT + lotteryAction.getLotteryWinInfo().getPrize().getName() + RIGHT;
                    SpannableStringBuilder style = new SpannableStringBuilder(s);
                    style.setSpan(new ForegroundColorSpan(Color.parseColor("#38404B")), 0, LEFT.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    style.setSpan(new ForegroundColorSpan(Color.parseColor("#FF412E")), LEFT.length() - 1, LEFT.length() + lotteryAction.getLotteryWinInfo().getPrize().getName().length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    style.setSpan(new ForegroundColorSpan(Color.parseColor("#38404B")), LEFT.length() + lotteryAction.getLotteryWinInfo().getPrize().getName().length() + 1, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mTvGift.setText(style);
                }
                //中奖码
                if (lotteryAction.getLotteryWinInfo().getOwnUserInfo() != null && !TextUtils.isEmpty(lotteryAction.getLotteryWinInfo().getOwnUserInfo().getPrizeCode())) {
                    mLotteryCode.setText(lotteryAction.getLotteryWinInfo().getOwnUserInfo().getPrizeCode());
                }
                //填写的中奖信息
                if (lotteryAction.getLotteryWinInfo().getCollectTemplate() != null && lotteryAction.getLotteryWinInfo().getCollectTemplate().size() > 0) {
                    mTips.setVisibility(View.VISIBLE);
                    mSubmit.setVisibility(View.VISIBLE);
                    mContentRoot.setVisibility(View.VISIBLE);
                    for (int i = 0; i < lotteryAction.getLotteryWinInfo().getCollectTemplate().size(); i++) {
                        LotteryCollectTemplate lotteryCollectTemplate = lotteryAction.getLotteryWinInfo().getCollectTemplate().get(i);
                        LotteryMessageItemView lotteryMessageItemView = new LotteryMessageItemView(mContext);
                        lotteryMessageItemView.setContent(lotteryCollectTemplate);
                        lotteryMessageItemView.setEdittextListenr(new TextWatcher() {

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                mError.setVisibility(View.GONE);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        mContentRoot.addView(lotteryMessageItemView);
                        if (i == lotteryAction.getLotteryWinInfo().getCollectTemplate().size() - 1) {
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) lotteryMessageItemView.getLayoutParams();
                            layoutParams.bottomMargin = 0;//将默认的距离底部20dp，改为0，这样底部区域全被listview填满。
                            lotteryMessageItemView.setLayoutParams(layoutParams);
                        }
                    }
                    status = 0;
                } else {
                    mTips.setVisibility(View.GONE);
                    mContentRoot.setVisibility(View.GONE);
                    mSubmit.setVisibility(View.GONE);
                    status = 2;
                }
            } else {
                //礼物名称
                if (lotteryAction.getLotteryWinInfo().getPrize() != null) {
                    String s = NOT_WIN_LEFT + lotteryAction.getLotteryWinInfo().getPrize().getName() + NOT_WIN_RIGHT;
                    SpannableStringBuilder style = new SpannableStringBuilder(s);
                    style.setSpan(new ForegroundColorSpan(Color.parseColor("#38404B")), 0, NOT_WIN_LEFT.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    style.setSpan(new ForegroundColorSpan(Color.parseColor("#FF412E")), NOT_WIN_LEFT.length() - 1, NOT_WIN_LEFT.length() + lotteryAction.getLotteryWinInfo().getPrize().getName().length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    style.setSpan(new ForegroundColorSpan(Color.parseColor("#38404B")), NOT_WIN_LEFT.length() + lotteryAction.getLotteryWinInfo().getPrize().getName().length() + 1, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mNotWinGift.setText(style);
                }
                mWinRoot.setVisibility(View.GONE);
                mNotWinRoot.setVisibility(View.VISIBLE);
            }
            //设置中奖人员名单
            isOpen = false;
            mArrows.setImageResource(R.drawable.lottery_down);
            lotteryList.setVisibility(View.VISIBLE);
            if (lotteryAction.getLotteryWinInfo().getUserInfos() != null && lotteryAction.getLotteryWinInfo().getUserInfos().size() > 0) {
                mUserRoot.setVisibility(View.VISIBLE);
                lotteryList.setVisibility(View.GONE);
                lotteryUserAdapter.setUsers(lotteryAction.getLotteryWinInfo().getUserInfos());
            } else {
                mUserRoot.setVisibility(View.GONE);
            }
        }
    }

    public LotteryAction getLotteryAction() {
        return lotteryAction;
    }

    @Override
    public void show(View view) {
        super.show(view);
    }

    public void dismiss() {
        super.dismiss();
        if (delayAction != null) {
            delayAction.cancel();
            handler.removeCallbacks(delayAction);
        }
        showTime = 0;
        isOpen = false;
    }

    private class DelayAction implements Runnable {
        boolean hasCancel = false;

        public void cancel() {
            hasCancel = true;
        }

        boolean isSuccess;

        public void setSuccess(boolean success) {
            isSuccess = success;
        }

        @Override
        public void run() {
            if (hasCancel) return;
            mRemindRoot.setVisibility(View.GONE);
            if (isSuccess) {
                mRemindSuccess.setVisibility(View.GONE);
                dismiss();
            } else {
                mRemindFail.setVisibility(View.GONE);
            }

        }
    }

}
