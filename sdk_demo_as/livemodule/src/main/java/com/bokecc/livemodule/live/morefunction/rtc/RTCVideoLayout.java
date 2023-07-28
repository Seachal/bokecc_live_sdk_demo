package com.bokecc.livemodule.live.morefunction.rtc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.utils.AppRTCAudioManager;
import com.bokecc.livemodule.view.BaseRelativeLayout;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.rtc.CCRTCRender;


/**
 * CC 直播连麦视频展示控件
 */
public class RTCVideoLayout extends BaseRelativeLayout {


    private final int[] mVideoSizes = new int[2]; // 远程视频的宽高

    private CCRTCRender mLocalRender;
    private CCRTCRender mRemoteRender;
    private AppRTCAudioManager mAudioManager;

    // 是否重新计算大小
    private boolean needAdjust = false;

    public RTCVideoLayout(Context context) {
        super(context);
    }

    @Override
    public void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.live_portrait_rtc_video, this, true);
        mLocalRender = findViewById(R.id.svr_local_render);
        mRemoteRender = findViewById(R.id.svr_remote_render);
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.initRtc(mLocalRender, mRemoteRender);
        }
    }

    // 进入连麦
    public void enterSpeak(final boolean isVideoRtc, final boolean needAdjust, String videoSize) {
        this.needAdjust = needAdjust;
        processRemoteVideoSize(videoSize);
        if (isVideoRtc) {
            setVisibility(VISIBLE);
            mLocalRender.setVisibility(View.INVISIBLE);
            mRemoteRender.setVisibility(View.VISIBLE);
            DWLive.getInstance().removeLocalRender();
        } else {
            setVisibility(GONE);
            mLocalRender.setVisibility(View.INVISIBLE);
            mRemoteRender.setVisibility(View.VISIBLE);
        }
        // 由于rtc是走的通话音频，所以需要做处理
        mAudioManager = AppRTCAudioManager.create(mContext);
        mAudioManager.start(null);
    }

    // 连麦断开
    public void disconnectSpeak() {
        post(new Runnable() {
            @Override
            public void run() {
                toastOnUiThread("连麦中断");
                setVisibility(GONE);
                if (mAudioManager != null) {
                    mAudioManager.stop();
                }
                destroy();
            }
        });
    }

    // 连麦错误
    public void speakError(final Exception e) {
        post(new Runnable() {
            @Override
            public void run() {
                toastOnUiThread("连麦错误 ：" + e.getMessage());
                setVisibility(GONE);
                if (mAudioManager != null) {
                    mAudioManager.stop();
                }
                destroy();
            }
        });
    }

    public void destroy() {
        if (mAudioManager != null) {
            mAudioManager.stop();
        }
    }

    private void processRemoteVideoSize(String videoSize) {
        String[] sizes = videoSize.split("x");
        int width = Integer.parseInt(sizes[0]);
        int height = Integer.parseInt(sizes[1]);
        double ratio = (double) width / (double) height;
        // 对于分辨率为16：9的，更改默认分辨率为16：10
        if (ratio > 1.76 && ratio < 1.79) {
            mVideoSizes[0] = 1600;
            mVideoSizes[1] = 1000;
        }
        requestLayout();
    }


}
