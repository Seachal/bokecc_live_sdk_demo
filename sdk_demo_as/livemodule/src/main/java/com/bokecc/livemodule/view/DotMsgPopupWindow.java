package com.bokecc.livemodule.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.DensityUtil;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.sdk.mobile.live.logging.ELog;

/**
 * 视频打点弹出View
 * Created by dds on 2021/3/2.
 */
public class DotMsgPopupWindow extends PopupWindow {

    protected Context mContext;
    private int mLayoutParamsX, mLayoutParamsY;
    private LinearLayout mDotViewMsgRootView;
    /**
     * 打点详细信息
     */
    private TextView mDotMsgTextView;
    /**
     * 下方箭头
     */
    private ImageView mArrowIndicator;
    protected View mPopContentView;

    /**
     * 打点View
     */
    private DotView mDotView;

    public DotMsgPopupWindow(Context context, DotView dotView, int x, int y) {
        mContext = context;
        this.mLayoutParamsX = x;
        this.mLayoutParamsY = y;
        this.mDotView = dotView;
        mPopContentView = LayoutInflater.from(context).inflate(R.layout.cc_dialogfragment_dot_msg, null);
        setContentView(mPopContentView);
        mDotViewMsgRootView = mPopContentView.findViewById(R.id.dot_view_msg_root);
        mDotMsgTextView = mPopContentView.findViewById(R.id.tv_dot_msg);
        mArrowIndicator = mPopContentView.findViewById(R.id.arrow_indicator);

        int time = mDotView.getDotTime();
        String timeStr = TimeUtil.getFormatTime(time * 1000);
        String msg = String.format("%s %s", timeStr, mDotView.getDotMsg());
        mDotMsgTextView.setText(msg);
        mDotViewMsgRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDotViewMsgClickListener != null) {
                    mDotViewMsgClickListener.onDotViewMsgClick();
                }
                dismiss();
            }
        });

        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));
        this.setOutsideTouchable(true);


    }

    public void show(View view) {
        ViewGroup.MarginLayoutParams seekBarLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int paddingStart = view.getPaddingStart();
        getContentView().measure(0, 0);
        int measureWidth = getContentView().getMeasuredWidth();
        int measureHeight = getContentView().getMeasuredHeight();
        int x_off = mLayoutParamsX - (measureWidth >> 1) - (mDotView.getRootWidth());
        int width = DensityUtil.getWidth(mContext);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mArrowIndicator.getLayoutParams();
        if (mLayoutParamsX + paddingStart + 10 < measureWidth / 2) {
            int padding = measureWidth / 2 - mLayoutParamsX - 10 - paddingStart;
            layoutParams.setMarginEnd(padding);
        } else if ((x_off + measureWidth) > width) {
            int padding = x_off + measureWidth - width;
            layoutParams.setMarginStart(padding);
        }
        int y_off = -(view.getHeight() + measureHeight);
        showAsDropDown(view, x_off, y_off);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    /**
     * 打点信息点击事件监听
     */
    private OnDotViewMsgClickListener mDotViewMsgClickListener;

    public interface OnDotViewMsgClickListener {
        void onDotViewMsgClick();
    }

    public void setOnDotViewMsgClickListener(OnDotViewMsgClickListener listener) {
        this.mDotViewMsgClickListener = listener;
    }


}
