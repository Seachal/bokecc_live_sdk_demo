package com.bokecc.livemodule.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.base.BaseDialogFragment;
import com.bokecc.livemodule.utils.TimeUtil;


/**
 * 打点信息DialogFragment
 */
public class DotMsgDialogFragment extends BaseDialogFragment {

    /**
     * DialogFragment展示的x和y点的坐标
     */
    private LinearLayout mDotViewMsgRootView;
    private RelativeLayout dot_layout;
    private int mLayoutParamsX, mLayoutParamsY;
    /**
     * 打点详细信息
     */
    private TextView mDotMsgTextView;
    /**
     * 打点View
     */
    private DotView mDotView;
    private ImageView arrow_indicator;

    /**
     * 打点信息点击事件监听
     */
    private OnDotViewMsgClickListener mDotViewMsgClickListener;

    public DotMsgDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.cc_dialogfragment_dot_msg;
    }

    @Override
    protected void bindView(final View view) {
        mDotViewMsgRootView = view.findViewById(R.id.dot_view_msg_root);
        dot_layout = view.findViewById(R.id.dot_layout);
        mDotMsgTextView = view.findViewById(R.id.tv_dot_msg);
        int time = mDotView.getDotTime();
        String timeStr = TimeUtil.getFormatTime(time * 1000);
        String msg = String.format("%s %s", timeStr, mDotView.getDotMsg());
        mDotMsgTextView.setText(msg);
        arrow_indicator = view.findViewById(R.id.arrow_indicator);



        mDotViewMsgRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDotViewMsgClickListener != null) {
                    mDotViewMsgClickListener.onDotViewMsgClick();
                }
                if (getDialog() != null) {
                    getDialog().dismiss();
                }
            }
        });


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected boolean isCancelableOutside() {
        return true;
    }

    @Override
    protected int getDialogAnimationRes() {
        return super.getDialogAnimationRes();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Window window = getDialog().getWindow();
        if (window != null) {
            //设置窗体背景色透明
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //设置宽高
            final WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            //透明度
            layoutParams.dimAmount = getDimAmount();
            //位置
            layoutParams.gravity = Gravity.START | Gravity.BOTTOM;

            final ViewTreeObserver viewTreeObserver = dot_layout.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (getContext() != null) {
                        ViewTreeObserver obs = dot_layout.getViewTreeObserver();
                        obs.removeOnGlobalLayoutListener(this);
                        int measuredWidth = dot_layout.getMeasuredWidth();
                        layoutParams.x = mLayoutParamsX - measuredWidth / 2;
                        layoutParams.y = getScreenHeight(getContext()) - mLayoutParamsY;
                        window.setAttributes(layoutParams);

                    }


                }
            });
        }
    }


    public void setX(int x) {
        this.mLayoutParamsX = x;
    }

    public void setY(int y) {
        this.mLayoutParamsY = y;
    }

    public void setDotView(DotView dotView) {
        this.mDotView = dotView;
    }

    public DotView getDotView() {
        return mDotView;
    }

    public interface OnDotViewMsgClickListener {
        void onDotViewMsgClick();
    }

    public void setOnDotViewMsgClickListener(OnDotViewMsgClickListener listener) {
        this.mDotViewMsgClickListener = listener;
    }
}
