package com.bokecc.livemodule.live.video;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.DWLiveVideoListener;
import com.bokecc.livemodule.view.CustomToast;
import com.bokecc.livemodule.view.ResizeTextureView;
import com.bokecc.livemodule.view.VideoLoadingView;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.DWLivePlayer;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.bokecc.sdk.mobile.live.player.DWBasePlayer;
import com.bokecc.sdk.mobile.live.player.PlayerEvent;
import com.bokecc.sdk.mobile.live.util.NetworkUtils;
import com.bumptech.glide.Glide;

/**
 * CC 直播视频展示控件
 * 说明: 此处存在Surface动态初始化失败的问题，后续考虑怎么优化
 */
public class LiveVideoView extends RelativeLayout implements DWLiveVideoListener {
    private final String TAG = LiveVideoView.class.getSimpleName();

    private Context mContext;
    /**
     * 视频显示容器
     */
    private ResizeTextureView mVideoContainer;
    /**
     * 视频加载进度
     */
    private VideoLoadingView mVideoProgressBar;
    /**
     * 直播播放器
     */
    private DWLivePlayer player;
    /**
     * 音频模式界面
     */
    private View audioModeRoot;
    private ImageView audioUndulate;

    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;


    private boolean isPlayerNetErrorStatus = false;

    /**
     * 是否在开课状态
     */
    private boolean isStreamStart = true;

    public LiveVideoView(Context context) {
        super(context);
        initViews(context);
    }

    public LiveVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public LiveVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    public void initViews(Context context) {
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    /**
     * 初始化视图对象
     */
    private void inflateViews() {
        View mRootView = LayoutInflater.from(mContext).inflate(R.layout.live_video_view, null);
        mVideoContainer = mRootView.findViewById(R.id.live_video_container);
        mVideoProgressBar = mRootView.findViewById(R.id.video_progressBar);
        audioModeRoot = mRootView.findViewById(R.id.audio_root);
        audioUndulate = mRootView.findViewById(R.id.iv_audio_undulate);
        addView(mRootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        player = new DWLivePlayer(mContext);
        player.setPlayerEventListener(event);
        mVideoContainer.setSurfaceTextureListener(surfaceTextureListener);
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.setPlayer(player);
            dwLiveCoreHandler.setDwLiveVideoListener(this);
        }
    }

    /**
     * 视频播放控件进入连麦模式
     *
     * @param isVideoRtc : 是否显示连麦
     */
    public void enterRtcMode(boolean isVideoRtc) {
        if (isVideoRtc) {
            // 如果是视频连麦，则将播放器暂停
            DWLive.getInstance().pause();
            setVisibility(GONE);
        } else {
            // 如果是音频连麦，只需将播放器的音频关闭掉
            DWLive.getInstance().setVolume(0f, 0f);
        }

    }

    /**
     * 视频播放控件退出连麦模式
     */
    public void exitRtcMode() {
        // 重新开启播放器
        if (isStreamStart) {
            DWLive.getInstance().restartVideo();
        }
        setVisibility(VISIBLE);
    }

    /**
     * 开始播放
     */
    public void start() {
        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            Log.e(TAG,"LiveVideoView:Network UnAvailable!");
            return;
        }
        if (!DWLiveCoreHandler.getInstance().isRtcing()) {
            // 启动直播播放器
            DWLiveCoreHandler.getInstance().start();
            if (mVideoProgressBar != null) {
                mVideoProgressBar.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (!DWLiveCoreHandler.getInstance().isRtcing()) {
            DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
            if (dwLiveCoreHandler != null) {
                dwLiveCoreHandler.stop();
            }
        }
    }

    /**
     * 释放播放器
     */
    public void destroy() {
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.destroy();
        }
    }

    /**
     * 切换音视频模式布局
     *
     * @param playMode DWLive.PlayMode
     */
    private void changeModeLayout(DWLive.LivePlayMode playMode) {
        if (playMode == DWLive.LivePlayMode.PLAY_MODE_TYEP_AUDIO) {
            audioModeRoot.setVisibility(VISIBLE);
            Glide.with(mContext).load(R.drawable.gif_audio_undulate).asGif().thumbnail(0.1f).into(audioUndulate);
        } else {
            audioModeRoot.setVisibility(GONE);
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

    //------------------------------------- 监听方法 -------------------------------------------
    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    if (mSurfaceTexture != null) {
                        mVideoContainer.setSurfaceTexture(mSurfaceTexture);
                    } else {
                        mSurfaceTexture = surfaceTexture;
                        mSurface = new Surface(surfaceTexture);
                        player.setSurface(mSurface);
                    }

                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                }
            };

    private final PlayerEvent event = new PlayerEvent() {
        @Override
        public void onPrepared() {
            ELog.i(TAG, "LiveVideoView onPrepared...");
            isPlayerNetErrorStatus = false;
        }

        @Override
        public void onError(int code, DWLiveException exception) {
            mVideoProgressBar.setVisibility(GONE);
            isPlayerNetErrorStatus = false;
            CustomToast.showToast(mContext, exception.getMessage(), Toast.LENGTH_SHORT);
            String msg = exception.getMessage();
            if (code == DWLivePlayer.MEDIA_ERROR_NET_ERROR) {
                // 无网或者无流异常
                msg = "网络异常，请刷新重试";
                isPlayerNetErrorStatus = true;
            } else if (code == DWLivePlayer.MEDIA_ERROR_TIMEOUT) {
                msg = "请求超时，请刷新重试";
            } else {
                msg = "播放异常，请刷新重试";
            }
            DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
            if (dwLiveCoreHandler != null) {
                //需要停止直播
                dwLiveCoreHandler.stop();
                dwLiveCoreHandler.showError(msg);
            }
        }

        @Override
        public void onPlayStateChange(final DWBasePlayer.State state) {
            post(new Runnable() {
                @Override
                public void run() {
                    switch (state) {
                        case PLAYING:
                            mVideoProgressBar.setVisibility(GONE);
                            isPlayerNetErrorStatus = false;
                            break;
                        case PREPARING:
                        case BUFFERING:
                            mVideoProgressBar.setVisibility(VISIBLE);
                            isPlayerNetErrorStatus = false;
                            break;
                    }
                }
            });
        }

        @Override
        public void onVideoSizeChanged(final int width, final int height) {
            post(new Runnable() {
                @Override
                public void run() {
                    mVideoContainer.setVideoSize(width, height);
                }
            });


        }

        @Override
        public void onBufferSpeed(float speed) {
            if (mVideoProgressBar != null) {
                mVideoProgressBar.setSpeed(speed);
            }
        }
    };

    //------------------------------------- SDK 回调相关 ---------------------------------------
    @Override
    public void onStreamEnd(final boolean isNormal) {
        post(new Runnable() {
            @Override
            public void run() {
                isStreamStart = false;
                // 暂停播放
                DWLive.getInstance().pause();
                mVideoProgressBar.setVisibility(View.GONE);

            }
        });
    }

    @Override
    public void onStreamStart() {
        isStreamStart = true;
    }


    /**
     * 播放状态
     *
     * @param status : 包括PLAYING, PREPARING共2种状态
     */
    @Override
    public void onLiveStatus(final DWLive.PlayStatus status) {
        post(new Runnable() {
            @Override
            public void run() {
                switch (status) {
                    case PLAYING:
                        // 直播正在播放
                        mVideoProgressBar.setVisibility(VISIBLE);
                        break;
                    case PREPARING:
                        // 直播未开始
                        mVideoProgressBar.setVisibility(GONE);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 禁播
     *
     * @param reason : 禁播原因
     */
    @Override
    public void onBanStream(String reason) {
        post(new Runnable() {
            @Override
            public void run() {
                // 播放器停止播放
                DWLive.getInstance().pause();
                // 隐藏加载控件
                if (mVideoProgressBar != null) {
                    mVideoProgressBar.setVisibility(GONE);
                }
            }
        });
    }

    /**
     * 解禁
     */
    @Override
    public void onUnbanStream() {
        if (mSurface != null) {
            DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
            if (dwLiveCoreHandler != null) {
                dwLiveCoreHandler.start();
            }
        }
    }

    @Override
    public void onChangePlayMode(DWLive.LivePlayMode playMode) {
        changeModeLayout(playMode);
    }


    public void onNetWorkChanged(boolean hasNet) {
        if (hasNet){
            if (isPlayerNetErrorStatus){
                //直接恢复观看
                DWLiveCoreHandler.getInstance().retryVideoPlay();
                isPlayerNetErrorStatus = false;
            }
        }else {
        }
    }

}
