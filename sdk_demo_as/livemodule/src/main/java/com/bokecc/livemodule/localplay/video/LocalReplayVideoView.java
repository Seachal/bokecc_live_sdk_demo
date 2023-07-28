package com.bokecc.livemodule.localplay.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.localplay.DWLocalReplayCoreHandler;
import com.bokecc.livemodule.utils.ProRecordWorker;
import com.bokecc.livemodule.view.ResizeTextureView;
import com.bokecc.livemodule.view.VideoLoadingView;
import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.player.PlayerEvent;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplay;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;

/**
 * CC 回放视频展示控件
 */
public class LocalReplayVideoView extends RelativeLayout {

    Context mContext;

    View mRootView;

    ResizeTextureView mTextureView;

    VideoLoadingView mVideoProgressBar;

    DWReplayPlayer player;

    String playPath;

    public LocalReplayVideoView(Context context) {
        super(context);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public LocalReplayVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public LocalReplayVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    private void inflateViews() {
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.live_video_view, this);
        mTextureView = mRootView.findViewById(R.id.live_video_container);
        mVideoProgressBar = mRootView.findViewById(R.id.video_progressBar);
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);
        player = new DWReplayPlayer(getContext());
        player.setPlayerEvent(playerEvent);
        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler != null) {
            dwLocalReplayCoreHandler.setPlayer(player);
        }
    }

    /**
     * 开始播放
     */
    public void start() {
        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler != null) {
            dwLocalReplayCoreHandler.start();
//
            dwLocalReplayCoreHandler.startProRecord(DWLiveEngine.getInstance().getContext(), new ProRecordWorker.UICallback() {
                @Override
                public void proRecordSeek(long time) {
                    //如果是播放中 直接seek即可
                    if (DWLocalReplayCoreHandler.getInstance().getPlayer().isInPlaybackState()) {
                        DWReplayPlayer player = DWLocalReplayCoreHandler.getInstance().getPlayer();
                        player.seekTo(time);
                    } else {
                        //如果不是播放中  为了安全起见 重新播放
                        DWLocalReplayCoreHandler.getInstance().retryReplay(time);
                    }
                }
            });
        }

    }

    /**
     * 暂停播放
     */
    public void pause() {
        // 暂停播放
        DWLiveLocalReplay.getInstance().pause();
    }

    /**
     * 停止播放
     */
    public void stop() {
        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler != null) {
            dwLocalReplayCoreHandler.stop();
        }
        if (dwLocalReplayCoreHandler != null) {
            // 停止记忆进度
            dwLocalReplayCoreHandler.stopProRecord();
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler != null) {
            dwLocalReplayCoreHandler.destroy();
        }
        if (dwLocalReplayCoreHandler != null) {
            dwLocalReplayCoreHandler.destroy();
            // 停止记忆进度
            dwLocalReplayCoreHandler.stopProRecord();
        }
    }

    private SurfaceTexture mSurfaceTexture;
    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (mSurfaceTexture != null) {
                mTextureView.setSurfaceTexture(mSurfaceTexture);
            } else {
                mSurfaceTexture = surfaceTexture;
                Surface mSurface = new Surface(surfaceTexture);
                player.updateSurface(mSurface);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };


    /******************************************* 播放器相关监听 ***********************************/
    private final PlayerEvent playerEvent = new PlayerEvent() {
        @Override
        public void onError(int code, DWLiveException exception) {
            DWLocalReplayCoreHandler dwReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.playError(code);
            }
        }

        @Override
        public void onPrepared() {
            post(new Runnable() {
                @Override
                public void run() {
                    DWLocalReplayCoreHandler coreHandler = DWLocalReplayCoreHandler.getInstance();
                    if (coreHandler != null) {
                        coreHandler.replayVideoPrepared();
                    }
                }
            });
        }

        @Override
        public void onVideoSizeChanged(int width, int height) {
            if (width != 0 && height != 0) {
                mTextureView.setVideoSize(width, height);
            }
        }

        @Override
        public void onCompletion() {
            DWLocalReplayCoreHandler dwReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.onPlayComplete();
                // 清除记忆播放
                dwReplayCoreHandler.clearProRecord();
            }
        }

        @Override
        public void onBufferUpdate(int percent) {
            super.onBufferUpdate(percent);
        }

        @Override
        public void onInfo(int what, int extra) {
            super.onInfo(what, extra);
        }

        @Override
        public void onPlayStateChange(DWReplayPlayer.State state) {
            if (state == DWReplayPlayer.State.PLAYING || state == DWReplayPlayer.State.PREPARED) {
                mVideoProgressBar.setVisibility(GONE);
            } else if (state == DWReplayPlayer.State.BUFFERING || state == DWReplayPlayer.State.PREPARING) {
                mVideoProgressBar.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onSeekComplete() {
            DWLocalReplayCoreHandler dwReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.onSeekComplete();
            }
        }
    };


    /**
     * 设置播放路径（即文件名：*****.ccr）
     *
     * @param fileName 播放路径
     */
    public void setPlayPath(String fileName) {
        this.playPath = fileName;
        DWLocalReplayCoreHandler.getInstance().setPlayPath(playPath);
    }
}
