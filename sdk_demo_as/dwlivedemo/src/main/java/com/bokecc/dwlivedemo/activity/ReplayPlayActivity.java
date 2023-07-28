package com.bokecc.dwlivedemo.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.bokecc.dwlivedemo.BuildConfig;
import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.popup.ExitPopupWindow;
import com.bokecc.dwlivedemo.popup.FloatingPopupWindow;
import com.bokecc.dwlivedemo.utils.NotificationUtil;
import com.bokecc.livemodule.live.chat.OnChatComponentClickListener;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.chat.ReplayChatComponent;
import com.bokecc.livemodule.replay.doc.ReplayDocComponent;
import com.bokecc.livemodule.replay.intro.ReplayIntroComponent;
import com.bokecc.livemodule.replay.qa.ReplayQAComponent;
import com.bokecc.livemodule.replay.room.ReplayRoomLayout;
import com.bokecc.livemodule.replay.video.ReplayVideoView;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;
import static com.bokecc.dwlivedemo.utils.NotificationUtil.ACTION_DESTROY;
import static com.bokecc.dwlivedemo.utils.NotificationUtil.ACTION_PLAY_PAUSE;
import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_IMAGE_COMPONENT;
import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_ULR_COMPONET;
import static com.bokecc.livemodule.utils.AppPhoneStateListener.ACTION_PAUSE;
import static com.bokecc.livemodule.utils.AppPhoneStateListener.ACTION_RESUME;

/**
 * 回放播放页（默认文档大屏，视频小屏，可手动切换）
 */
public class ReplayPlayActivity extends BaseActivity {
    private static final String TAG = "ReplayPlayActivity";

    View mRoot;
    LinearLayout mReplayMsgLayout;
    RelativeLayout mReplayVideoContainer;
    // 回放视频View
    ReplayVideoView mReplayVideoView;
    // 悬浮弹窗（用于展示文档和视频）
    FloatingPopupWindow mReplayFloatingView;
    // 回放房间组件
    ReplayRoomLayout mReplayRoomLayout;
    // 广播接收者
    BroadcastReceiver broadcastReceiver;

    // 是否默认视频大屏
    private boolean isVideoMain;

    /**
     * 启动回放界面
     *
     * @param activity    activity
     * @param isVideoMain 是否是以大屏为主
     * @param recordId    回放id，用于记忆播放
     */
    public static void openActivity(Activity activity, boolean isVideoMain, String recordId) {
        Intent intent = new Intent(activity, ReplayPlayActivity.class);
        intent.putExtra("isVideoMain", isVideoMain);
        intent.putExtra("recordId", recordId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 请求全屏
//        requestFullScreenFeature();
        // 请求状态栏黑色
        requestStatusBarBlack(this);
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_play);

        // 回放id
        String recordId = getIntent().getStringExtra("recordId");
        isVideoMain = getIntent().getBooleanExtra("isVideoMain", true);

        initViews();
        initViewPager();
        initReceiver();


        // 设置进度记忆唯一标识
        DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
        if (replayCoreHandler != null) {
            replayCoreHandler.setRecordTag(recordId);
        }

//        DWLiveReplay.getInstance().setLastPosition(5 * 60 * 1000);
        // 开始播放
        mReplayVideoView.start();

        // 设置防录屏
        mReplayVideoView.setAntiRecordScreen(this);
    }

    // 注册广播接收者
    private void initReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (NotificationUtil.ACTION_PLAY_PAUSE.equals(action)) {
                    if (mReplayRoomLayout != null) {
                        mReplayRoomLayout.changePlayerStatus();
                    }
                    NotificationUtil.sendReplayNotification(ReplayPlayActivity.this);
                } else if (NotificationUtil.ACTION_DESTROY.equals(action)) {
                    exit();
                } else if (ACTION_PAUSE.equals(action)) {
                    DWLiveReplay.getInstance().pause();


                } else if (ACTION_RESUME.equals(action)) {
                    DWLiveReplay.getInstance().resume();
                }

            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY_PAUSE);
        intentFilter.addAction(ACTION_DESTROY);
        intentFilter.addAction(ACTION_PAUSE);
        intentFilter.addAction(ACTION_RESUME);
        registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    protected void onDestroy() {
        destroy();
        super.onDestroy();
    }
    private boolean isDestroyed = false;
    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            destroy();
        }
    }
    private void destroy()  {
        if (isDestroyed) {
            return;
        }
        NotificationUtil.cancelNotification(this);
        unregisterReceiver(broadcastReceiver);
        mReplayFloatingView.dismiss();
        mReplayVideoView.destroy();
        isDestroyed = true;
    }

    @Override
    public void onBackPressed() {
        if (!isPortrait()) {
            quitFullScreen();
            return;
        }
        if (mExitPopupWindow != null) {
            mExitPopupWindow.setConfirmExitRoomListener(confirmExitRoomListener);
            mExitPopupWindow.show(mRoot);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 横屏隐藏状态栏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(true));
        } else {
            getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(false));
        }
        //调整窗口的位置
        if (mReplayFloatingView != null) {
            mReplayFloatingView.onConfigurationChanged(newConfig.orientation);
        }
    }

    @TargetApi(19)
    private static int getSystemUiVisibility(boolean isFull) {
        if (isFull) {
            int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            return flags;
        } else {
            return View.SYSTEM_UI_FLAG_VISIBLE;
        }

    }

    private void initViews() {
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        mReplayVideoContainer = findViewById(R.id.rl_video_container);
        mReplayRoomLayout = findViewById(R.id.replay_room_layout);
        mReplayMsgLayout = findViewById(R.id.ll_pc_replay_msg_layout);
        mViewPager = findViewById(R.id.live_portrait_container_viewpager);
        mRadioGroup = findViewById(R.id.rg_infos_tag);
        mIntroTag = findViewById(R.id.live_portrait_info_intro);
        mQaTag = findViewById(R.id.live_portrait_info_qa);
        mChatTag = findViewById(R.id.live_portrait_info_chat);
        mDocTag = findViewById(R.id.live_portrait_info_document);


        // 弹出框界面
        mExitPopupWindow = new ExitPopupWindow(this);
        mReplayFloatingView = new FloatingPopupWindow(this);
        mReplayFloatingView.setFloatDismissListener(floatDismissListener);
        mReplayRoomLayout.setReplayRoomStatusListener(roomStatusListener);

        // 初始化房间信息
        mReplayRoomLayout.init(isVideoMain);
        mReplayRoomLayout.setActivity(this);
    }


    /**
     * 根据直播间模版初始化相关组件
     */
    private void initComponents() {
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler == null) {
            return;
        }

        // 初始化视频控件
        initVideoLayout();

        // 判断当前直播间模版是否有"文档"功能
        if (dwReplayCoreHandler.hasDocView()) {
            initDocLayout();
        }
        // 判断当前直播间模版是否有"聊天"功能
        if (dwReplayCoreHandler.hasChatView()) {
            initChatLayout();
            ELog.d(TAG, "initChatLayout");
        }
        // 判断当前直播间模版是否有"问答"功能
        if (dwReplayCoreHandler.hasQaView()) {
            initQaLayout();
            ELog.d(TAG, "initQaLayout");
        }
        // 直播间简介
        initIntroLayout();


    }

    /********************************* 重要组件相关 ***************************************/

    // 简介组件
    ReplayIntroComponent mIntroComponent;

    // 问答组件
    ReplayQAComponent mQaLayout;

    // 聊天组件
    ReplayChatComponent mChatLayout;

    // 文档组件
    ReplayDocComponent mDocLayout;

    // 初始化聊天布局区域
    private void initChatLayout() {
        mIdList.add(R.id.live_portrait_info_chat);
        mTagList.add(mChatTag);
        mChatTag.setVisibility(VISIBLE);
        mChatLayout = new ReplayChatComponent(this);
        mChatLayout.setOnChatComponentClickListener(new OnChatComponentClickListener() {
            @Override
            public void onClickChatComponent(Bundle bundle) {
                if (bundle == null) return;
                String type = bundle.getString("type");
                if (CONTENT_IMAGE_COMPONENT.equals(type)) {
                    Intent intent = new Intent(ReplayPlayActivity.this, ImageDetailsActivity.class);
                    intent.putExtra("imageUrl", bundle.getString("url"));
                    startActivity(intent);
                } else if (CONTENT_ULR_COMPONET.equals(type)) {
                    Uri uri = Uri.parse(bundle.getString("url"));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });
        mLiveInfoList.add(mChatLayout);
        if (mChatLayout != null) {
            mReplayRoomLayout.setSeekListener(mChatLayout);
        }
    }

    // 初始化问答布局区域
    private void initQaLayout() {
        mIdList.add(R.id.live_portrait_info_qa);
        mTagList.add(mQaTag);
        mQaTag.setVisibility(VISIBLE);
        mQaLayout = new ReplayQAComponent(this);
        mLiveInfoList.add(mQaLayout);
    }

    // 初始化简介布局区域
    private void initIntroLayout() {
        ELog.d(TAG, "initIntroLayout");
        mIdList.add(R.id.live_portrait_info_intro);
        mTagList.add(mIntroTag);
        mIntroTag.setVisibility(VISIBLE);
        mIntroComponent = new ReplayIntroComponent(this);
        mLiveInfoList.add(mIntroComponent);
    }

    // 初始化文档布局区域
    private void initDocLayout() {
        mDocLayout = new ReplayDocComponent(this);
        if (isVideoMain) {
            mReplayFloatingView.addView(mDocLayout);
        } else {
            if (DWReplayCoreHandler.getInstance().hasDocView()) {
                mReplayVideoContainer.addView(mDocLayout);
                mDocLayout.setDocScrollable(true);
            }
        }
        // 显示小窗
        showFloatingDocLayout();

    }

    // 初始化视频布局区域
    private void initVideoLayout() {
        mReplayVideoView = new ReplayVideoView(this);
        if (isVideoMain) {
            mReplayVideoContainer.addView(mReplayVideoView);
        } else {
            if (DWReplayCoreHandler.getInstance().hasDocView()) {
                mReplayFloatingView.addView(mReplayVideoView);
            } else {
                mReplayVideoContainer.addView(mReplayVideoView);
            }
        }

    }

    // 展示文档悬浮窗布局
    private void showFloatingDocLayout() {
        // 判断当前直播间模版是否有"文档"功能，如果没文档，则小窗功能也不应该有
        if (DWReplayCoreHandler.getInstance().hasDocView()) {
            mReplayFloatingView.show(mRoot);
        }
    }

    /*************************************** 下方布局 ***************************************/

    List<View> mLiveInfoList = new ArrayList<>();
    List<Integer> mIdList = new ArrayList<>();
    List<RadioButton> mTagList = new ArrayList<>();

    ViewPager mViewPager;

    RadioGroup mRadioGroup;
    RadioButton mIntroTag;
    RadioButton mQaTag;
    RadioButton mChatTag;
    RadioButton mDocTag;

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        initComponents();
        PagerAdapter adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return mLiveInfoList.size();
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mLiveInfoList.get(position));
                return mLiveInfoList.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mLiveInfoList.get(position));
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        };
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mTagList.get(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                mViewPager.setCurrentItem(mIdList.indexOf(i), true);
            }
        });
        if (mTagList != null && mTagList.size() > 0) {
            mTagList.get(0).performClick();
        }
    }

    // 悬浮窗消失监听
    private final FloatingPopupWindow.FloatDismissListener floatDismissListener = new FloatingPopupWindow.FloatDismissListener() {
        @Override
        public void dismiss() {
            mReplayRoomLayout.setSwitchText(true);
        }
    };

    //*************************************  Room 状态回调监听 *************************************/

    private final ReplayRoomLayout.ReplayRoomStatusListener roomStatusListener =
            new ReplayRoomLayout.ReplayRoomStatusListener() {

                @Override
                public void switchVideoDoc(boolean isVideoMain) {
                    switchView(isVideoMain);
                }

                @Override
                public void openVideoDoc() {
                    if (mReplayRoomLayout.isVideoMain()) {
                        mReplayFloatingView.show(mRoot);
                        if (mDocLayout.getParent() != null)
                            ((ViewGroup) mDocLayout.getParent()).removeView(mDocLayout);
                        mReplayFloatingView.addView(mDocLayout);
                    } else {
                        mReplayFloatingView.show(mRoot);
                        if (mReplayVideoView.getParent() != null)
                            ((ViewGroup) mReplayVideoView.getParent()).removeView(mReplayVideoView);
                        mReplayFloatingView.addView(mReplayVideoView);
                    }
                }

                @Override
                public void closeRoom() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 如果当前状态是竖屏，则弹出退出确认框，否则切换为竖屏
                            if (isPortrait()) {
                                if (mExitPopupWindow != null) {
                                    mExitPopupWindow.setConfirmExitRoomListener(confirmExitRoomListener);
                                    mExitPopupWindow.show(mRoot);
                                }
                            } else {
                                quitFullScreen();
                            }
                        }
                    });
                }

                @Override
                public void fullScreen() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            mReplayMsgLayout.setVisibility(View.GONE);
                            getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(true));
                        }
                    });
                }

                @Override
                public void onClickDocScaleType(int type) {
                    if (mDocLayout != null) {
                        mDocLayout.setScaleType(type);
                    }
                }

                @Override
                public void onChangePlayMode(DWLiveReplay.PlayMode playMode) {
                    mReplayVideoView.changeModeLayout(playMode);

                }


            };

    public void switchView(boolean isVideoMain) {
        if (mReplayVideoView.getParent() != null)
            ((ViewGroup) mReplayVideoView.getParent()).removeView(mReplayVideoView);
        if (mDocLayout.getParent() != null)
            ((ViewGroup) mDocLayout.getParent()).removeView(mDocLayout);
        if (isVideoMain) {
            // 缓存视频的切换前的画面
            mReplayFloatingView.addView(mDocLayout);
            mReplayVideoContainer.addView(mReplayVideoView);
            mDocLayout.setDocScrollable(false);//webview不可滑动
        } else {
            // 缓存视频的切换前的画面
            mReplayFloatingView.addView(mReplayVideoView);
            mReplayVideoContainer.addView(mDocLayout);
            mDocLayout.setDocScrollable(true);//webview可滑动
        }
    }

    //---------------------------------- 全屏相关逻辑 --------------------------------------------/

    // 退出全屏
    private void quitFullScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mReplayMsgLayout.setVisibility(VISIBLE);

        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(false));
    }

    //---------------------------------- 退出相关逻辑 --------------------------------------------/

    private ExitPopupWindow mExitPopupWindow;

    private final ExitPopupWindow.ConfirmExitRoomListener confirmExitRoomListener = new ExitPopupWindow.ConfirmExitRoomListener() {
        @Override
        public void onConfirmExitRoom() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    exit();
                }
            });
        }
    };

    // 退出房间
    private void exit() {
        mExitPopupWindow.dismiss();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 取消通知
        NotificationUtil.cancelNotification(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        // 开启通知
        if (!isFinishing()) {
            NotificationUtil.sendReplayNotification(this);
        }

    }
}
