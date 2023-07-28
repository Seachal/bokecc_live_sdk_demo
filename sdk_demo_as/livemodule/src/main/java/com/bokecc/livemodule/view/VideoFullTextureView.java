package com.bokecc.livemodule.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

/**
 * 用于全屏显示video的，存在裁剪
 */

public class VideoFullTextureView extends TextureView {
    private static final String TAG = "ResizeTextureView";
    private int mVideoWidth;
    private int mVideoHeight;
    public VideoFullTextureView(Context context) {
        super(context);
    }


    public VideoFullTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        Log.i(TAG, "setVideoSize: width:"+width+" height:"+height);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mVideoWidth == 0 || mVideoHeight == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        if (getRotation() == 90 || getRotation() == 270) { // 软解码时处理旋转信息，交换宽高
            widthMeasureSpec = widthMeasureSpec + heightMeasureSpec;
            heightMeasureSpec = widthMeasureSpec - heightMeasureSpec;
            widthMeasureSpec = widthMeasureSpec - heightMeasureSpec;
        }
        int width = getDefaultSize(widthMeasureSpec, widthMeasureSpec);
        int height = getDefaultSize(heightMeasureSpec, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            width = widthSpecSize;
            height = heightSpecSize;
            if ((float)mVideoWidth/(float)mVideoHeight>(float)width/(float)height){
                width = (int) ((float)height/((float)mVideoWidth/(float)mVideoHeight));
            }else{
                height = (int) ((float)width/((float)mVideoWidth/(float)mVideoHeight));
            }
        }
        setMeasuredDimension(width, height);
    }
}