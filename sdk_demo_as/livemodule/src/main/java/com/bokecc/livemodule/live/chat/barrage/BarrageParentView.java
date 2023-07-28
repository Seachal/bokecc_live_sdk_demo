package com.bokecc.livemodule.live.chat.barrage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.DensityUtil;
import com.bokecc.sdk.mobile.live.DWLive;

/**
 * 弹幕组件
 *
 * @author cc视频
 */
public class BarrageParentView extends LinearLayout {

    private boolean isFinish = false;
    private ImageView ivTag;
    private BarrageView barrageTextView;
    private View root;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarrageParentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context,attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarrageParentView(Context context) {
        super(context);
        initView(context,null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarrageParentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context,attrs);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void initView(Context context, AttributeSet attrs){
        root = LayoutInflater.from(context).inflate(R.layout.barrage_view, this);
        ivTag = root.findViewById(R.id.iv_barrage_tag);
        barrageTextView = root.findViewById(R.id.tv_barrage_content);

    }
    @SuppressLint("NewApi")
    public void move(float fromX, float toX, float fromY, float toY, long duration) { // toX参数没有使用

        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        this.measure(w, h);
        int width = this.getMeasuredWidth();

        Animation anim = new TranslateAnimation(fromX, width * -1 - 50, fromY, toY);
        anim.setDuration(duration);
        anim.setFillAfter(true);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isFinish = true;
            }
        });

        this.startAnimation(anim);
    }

    public boolean getFinish() {
        return isFinish;
    }

    public void setText(CharSequence text) {
        if (barrageTextView != null) {
            barrageTextView.setText(text);
        }
    }

    public void setTextSize(float size) {
        if (barrageTextView != null) {
            barrageTextView.setTextSize(size);
        }
    }

    public void setShadowLayer(float v, float v1, float v2, int argb) {
        barrageTextView.setShadowLayer(v,v1,v2,argb);
    }

    public void setTextColor(int color) {
        barrageTextView.setTextColor(color);
    }

    public void setSingleLine(boolean b) {
        barrageTextView.setSingleLine(b);
    }

    public TextPaint getPaint() {
        return barrageTextView.getPaint();
    }

    public void setRole(String userRole,String id) {
        if (userRole.equals("publisher")){//主播
            ivTag.setImageResource(R.drawable.barrage_teacher);
            root.setBackgroundResource(R.drawable.barrage_teacher_bg);
        }else if (userRole.equals("teacher")){//助教
            ivTag.setImageResource(R.drawable.barrage_assistant);
            root.setBackgroundResource(R.drawable.barrage_assistant_bg);
        }else if (userRole.equals("host")){//主持人
            ivTag.setImageResource(R.drawable.barrage_presenter);
            LinearLayout.LayoutParams layoutParams = (LayoutParams) ivTag.getLayoutParams();
            layoutParams.width = DensityUtil.dp2px(getContext(),38);
            ivTag.setLayoutParams(layoutParams);
            root.setBackgroundResource(R.drawable.barrage_presenter_bg);
        }else {
            if (DWLive.getInstance()!=null
                    &&DWLive.getInstance().getViewer()!=null
                    &&!TextUtils.isEmpty(DWLive.getInstance().getViewer().getId())
                    &&!TextUtils.isEmpty(id)
                    &&DWLive.getInstance().getViewer().getId().equals(id)){
                ivTag.setVisibility(GONE);
                root.setBackgroundResource(R.drawable.barrage_teacher_bg);
            }else{
                ivTag.setVisibility(GONE);
            }
        }
    }
}