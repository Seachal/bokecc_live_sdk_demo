package com.bokecc.livemodule.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.DensityUtil;

import java.util.Locale;

/**
 * 视频加载中View
 */
public class VideoLoadingView extends RelativeLayout {

    //大屏状态的跟布局
    RelativeLayout mLargeRoot;
    private TextView mNet;
    //小屏状态的跟布局
    RelativeLayout mSmallRoot;
    private TextView mNetSmall;

    public VideoLoadingView(Context context) {
        super(context);
        initView(context);
    }

    public VideoLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VideoLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.video_loading_view, this);
        //大屏
        mLargeRoot = findViewById(R.id.rl_large_root);
        mNet = findViewById(R.id.tv_net);
        mNet.setVisibility(GONE);
        //小屏
        mSmallRoot = findViewById(R.id.rl_small_root);
        mNetSmall = findViewById(R.id.tv_net_small);
        mNetSmall.setVisibility(GONE);

    }

    /**
     * 设置网速
     *
     * @param speed speed
     */
    public void setSpeed(final float speed) {
        post(new Runnable() {
            @Override
            public void run() {
                mNet.setVisibility(VISIBLE);
                mNetSmall.setVisibility(VISIBLE);
                mNet.setText(parseSpeed(speed));
                mNetSmall.setText(parseSpeed(speed));
            }
        });

    }

    // 转换tcp网速单位
    private String parseSpeed(float speed) {
        if (speed >= 1000 * 1000) {
            return String.format(Locale.US, "%.2f MB/s", ((float) speed) / 1000 / 1000);
        } else if (speed >= 1000) {
            return String.format(Locale.US, "%.1f KB/s", ((float) speed) / 1000);
        } else {
            return String.format(Locale.US, "%d B/s", (long) speed);
        }
    }

    // 当前视频窗的宽度
    private int currentWidth;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        // 屏幕大小改变
        if (currentWidth != parentWidth) {
            if (currentWidth > parentWidth && parentWidth < DensityUtil.getWidth(getContext())) {
                showSmallTips();
            } else {
                showBigTips();
            }
            currentWidth = parentWidth;
        } else {
            if (parentWidth < DensityUtil.getWidth(getContext())) {
                showSmallTips();
            } else {
                showBigTips();
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    // 显示大窗提示
    private void showBigTips() {
        mLargeRoot.setVisibility(VISIBLE);
        mSmallRoot.setVisibility(GONE);
    }

    // 显示小窗提示
    private void showSmallTips() {
        mSmallRoot.setVisibility(VISIBLE);
        mLargeRoot.setVisibility(GONE);
    }

}
