package com.bokecc.livemodule.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;

/**
 * 状态显示封装 - 和布局绑定
 */
public class TipsView extends RelativeLayout {
    private Context mContext;
    private AnimationDrawable animationDrawable;

    public TipsView(Context context) {
        super(context);
        init(context);
    }

    public TipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setGravity(Gravity.CENTER);
    }

    /**
     * 显示加载中提示
     *
     * @param content content显示文字
     */
    public void showLoadingView(String content) {
        removeAllViews();
        LayoutInflater.from(mContext).inflate(R.layout.tips_loading_view, this, true);
        TextView loadingText = findViewById(R.id.tips_loading_text);
        ImageView imageView = findViewById(R.id.tips_loading_image);
        animationDrawable = (AnimationDrawable) imageView.getBackground();
        animationDrawable.setOneShot(false);
        loadingText.setText(content);
    }

    /**
     * 显示常驻状态提示
     *
     * @param content 上方显示
     * @param btnStr  下方按钮显示
     */
    public void showStateView(String content, String btnStr, OnClickListener listener) {
        removeAllViews();
        LayoutInflater.from(mContext).inflate(R.layout.tips_video_retry_view, this, true);
        TextView tips_video_retry_msg = findViewById(R.id.tips_video_retry_msg);
        tips_video_retry_msg.setText(content);
        TextView tips_video_retry_btn = findViewById(R.id.tips_video_retry_btn);
        tips_video_retry_btn.setText(btnStr);
        tips_video_retry_btn.setOnClickListener(listener);
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        animationDrawable = null;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            if (animationDrawable != null) {
                animationDrawable.start();
            }
        } else if (visibility == GONE) {
            if (animationDrawable != null) {
                animationDrawable.stop();
            }
        }


    }
}
