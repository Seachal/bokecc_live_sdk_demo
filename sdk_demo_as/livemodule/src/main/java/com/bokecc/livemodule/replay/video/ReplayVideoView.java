package com.bokecc.livemodule.replay.video;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.utils.ProRecordWorker;
import com.bokecc.livemodule.view.ResizeTextureView;
import com.bokecc.livemodule.view.VideoLoadingView;
import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.player.DWBasePlayer;
import com.bokecc.sdk.mobile.live.player.PlayerEvent;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bumptech.glide.Glide;


/**
 * CC 回放视频展示控件
 */
public class ReplayVideoView extends RelativeLayout {
    private static final String TAG = "ReplayVideoView";
    private final Context mContext;
    private ResizeTextureView mTextureView;
    private VideoLoadingView mVideoProgressBar;
    private DWReplayPlayer player;
    private SurfaceTexture mSurfaceTexture;

    /**
     * 音频模式界面
     */
    private View audioModeRoot;
    private ImageView audioUndulate;

    public ReplayVideoView(Context context) {
        super(context);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public ReplayVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public ReplayVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    private void inflateViews() {
        View mRootView = LayoutInflater.from(mContext).inflate(R.layout.live_video_view, this);
        mTextureView = mRootView.findViewById(R.id.live_video_container);
        mVideoProgressBar = mRootView.findViewById(R.id.video_progressBar);
        audioModeRoot = mRootView.findViewById(R.id.audio_root);
        audioUndulate = mRootView.findViewById(R.id.iv_audio_undulate);
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);
        player = new DWReplayPlayer(mContext);
        player.setPlayerEvent(playerEvent);
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler != null) {
            dwReplayCoreHandler.setPlayer(player);
        }
    }

    /**
     * 开始播放
     */
    public void start() {
        final DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler != null) {
            dwReplayCoreHandler.start();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        DWReplayCoreHandler handler = DWReplayCoreHandler.getInstance();
        if (handler != null) {
            handler.pause();
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        //防止内存泄漏
        if (getHandler()!=null){
            getHandler().removeCallbacksAndMessages(null);
        }

        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler != null) {
            dwReplayCoreHandler.destroy();
            dwReplayCoreHandler.stopProRecord();
        }
    }

    public void showProgress() {
        if (mVideoProgressBar != null) {
            mVideoProgressBar.setVisibility(VISIBLE);
        }
    }


    public void dismissProgress() {
        if (mVideoProgressBar != null) {
            mVideoProgressBar.setVisibility(GONE);
        }
    }

    /**
     * 设置防录屏
     * 该方法需要在Activity的OnCreate方法里调用
     *
     * @param activity activity
     */
    public void setAntiRecordScreen(Activity activity) {
        if (player != null) {
            player.setAntiRecordScreen(activity);
        }
    }

    /**
     * 切换音视频模式布局
     *
     * @param playMode DWLive.PlayMode
     */
    public void changeModeLayout(DWLiveReplay.PlayMode playMode) {
        if (playMode == DWLiveReplay.PlayMode.PLAY_MODE_TYEP_AUDIO) {
            audioModeRoot.setVisibility(VISIBLE);
            Glide.with(mContext).load(R.drawable.gif_audio_undulate).asGif().thumbnail(0.1f).into(audioUndulate);
        } else {
            audioModeRoot.setVisibility(GONE);
        }
    }


    /*******************************************  TextureView 监听*********************************/

    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                                      int width, int height) {
                    Surface mSurface;
                    if (mSurfaceTexture != null) {
                        mTextureView.setSurfaceTexture(mSurfaceTexture);
                    } else {
                        mSurfaceTexture = surfaceTexture;
                        mSurface = new Surface(surfaceTexture);
                        player.setSurface(mSurface);

                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                                        int height) {
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
            DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
            dismissProgress();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.playError(code);
            }
        }

        @Override
        public void onPrepared() {
            final DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.replayVideoPrepared();
            }
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (dwReplayCoreHandler == null) {
                        return;
                    }
                    dwReplayCoreHandler.startProRecord(DWLiveEngine.getInstance().getContext(), new ProRecordWorker.UICallback() {
                        @Override
                        public void proRecordSeek(long time) {
                            //如果是播放中 直接seek即可
                            if (DWReplayCoreHandler.getInstance().getPlayer().isInPlaybackState()) {
                                DWReplayPlayer player = DWReplayCoreHandler.getInstance().getPlayer();
                                player.seekTo(time);
                            } else {
                                //如果不是播放中  为了安全起见 重新播放
                                DWReplayCoreHandler.getInstance().retryReplay(time, false);
                            }
                        }
                    });
                }
            }, 3000);
        }

        @Override
        public void onVideoSizeChanged(int width, int height) {
            if (width != 0 && height != 0) {
                mTextureView.setVideoSize(width, height);
            }
        }

        @Override
        public void onCompletion() {
            DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.onPlayComplete();
                // 清除记忆播放
                dwReplayCoreHandler.clearProRecord();
            }

        }

        @Override
        public void onBufferUpdate(int percent) {
            DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.updateBufferPercent(percent);
            }
        }

        @Override
        public void onPlayStateChange(DWReplayPlayer.State state) {
            DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
            if (state == DWBasePlayer.State.BUFFERING || state == DWBasePlayer.State.PREPARING) {
                showProgress();
                if (dwReplayCoreHandler != null) {
                    dwReplayCoreHandler.bufferStart();
                }
            } else if (state == DWBasePlayer.State.PLAYING) {
                dismissProgress();
                if (dwReplayCoreHandler != null) {
                    dwReplayCoreHandler.onRenderStart();
                }
            }
        }

        @Override
        public void onBufferSpeed(float speed) {
            if (mVideoProgressBar != null) {
                mVideoProgressBar.setSpeed(speed);
            }
        }

        @Override
        public void onSeekComplete() {
            DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.onSeekComplete();
            }
        }
    };

}
