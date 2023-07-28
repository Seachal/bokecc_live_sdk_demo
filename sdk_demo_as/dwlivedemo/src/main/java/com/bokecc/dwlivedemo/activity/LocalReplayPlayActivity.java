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
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.download.FileUtil;
import com.bokecc.dwlivedemo.popup.ExitPopupWindow;
import com.bokecc.dwlivedemo.popup.FloatingPopupWindow;
import com.bokecc.dwlivedemo.utils.NotificationUtil;
import com.bokecc.livemodule.live.chat.OnChatComponentClickListener;
import com.bokecc.livemodule.localplay.DWLocalReplayCoreHandler;
import com.bokecc.livemodule.localplay.chat.LocalReplayChatComponent;
import com.bokecc.livemodule.localplay.doc.LocalReplayDocComponent;
import com.bokecc.livemodule.localplay.intro.LocalReplayIntroComponent;
import com.bokecc.livemodule.localplay.qa.LocalReplayQAComponent;
import com.bokecc.livemodule.localplay.room.LocalReplayRoomLayout;
import com.bokecc.livemodule.localplay.video.LocalReplayVideoView;
import com.bokecc.livemodule.view.CustomToast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.bokecc.dwlivedemo.utils.NotificationUtil.ACTION_DESTROY;
import static com.bokecc.dwlivedemo.utils.NotificationUtil.ACTION_PLAY_PAUSE;
import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_IMAGE_COMPONENT;
import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_ULR_COMPONET;

/**
 * 离线回放播放页
 */
public class LocalReplayPlayActivity extends BaseActivity implements DWLocalReplayCoreHandler.LocalTemplateUpdateListener {

    public static String DOWNLOAD_DIR;
    private View mRoot;
    private String mPlayPath;  // CCR文件名
    private LocalReplayVideoView mReplayVideoView;
    private RelativeLayout mReplayVideoContainer;
    private LocalReplayRoomLayout mReplayRoomLayout;
    private LinearLayout mReplayMsgLayout;

    // 悬浮弹窗（用于展示文档和视频）
    private FloatingPopupWindow mLocalReplayFloatingView;

    // 广播接收者
    private BroadcastReceiver broadcastReceiver;

    private boolean isVideoMain;       // 是否是视频大屏

    /**
     * 启动回放界面
     *
     * @param activity    activity
     * @param isVideoMain 是否是以大屏为主
     * @param fileName    离线文件路径，用于记忆播放
     */
    public static void openActivity(Activity activity, boolean isVideoMain, String fileName) {
        Intent intent = new Intent(activity, LocalReplayPlayActivity.class);
        intent.putExtra("isVideoMain", isVideoMain);
        intent.putExtra("fileName", fileName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 请求全屏
//        requestFullScreenFeature();
        // 请求状态栏黑色
        requestStatusBarBlack(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_replay_play);
        DOWNLOAD_DIR = FileUtil.getCCDownLoadPath(this);
        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");
        isVideoMain = getIntent().getBooleanExtra("isVideoMain", true);
        if (TextUtils.isEmpty(fileName)) {
            CustomToast.showToast(this, "CCR文件名为空，播放失败！", Toast.LENGTH_LONG);
            return;
        }

        File oriFile = new File(DOWNLOAD_DIR, fileName);
        mPlayPath = getUnzipDir(oriFile);

        DWLocalReplayCoreHandler handler = DWLocalReplayCoreHandler.getInstance();
        handler.setLocalTemplateUpdateListener(this);

        // 设置进度记忆唯一标识
        DWLocalReplayCoreHandler replayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (replayCoreHandler != null) {
            replayCoreHandler.setRecordTag(mPlayPath);
        }

        initViews();
        initReceiver();

        if (mReplayVideoView != null) {
            mReplayVideoView.start();
        }
    }

    public static String getUnzipDir(File oriFile) {

        String fileName = oriFile.getName();

        StringBuilder sb = new StringBuilder();
        sb.append(oriFile.getParent());
        sb.append("/");
        int index = fileName.indexOf(".");
        if (index == -1) {
            sb.append(fileName);
        } else {
            sb.append(fileName.substring(0, index));
        }

        return sb.toString();
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

    PagerAdapter adapter;

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        adapter = new PagerAdapter() {
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

    /********************************* 重要组件相关 ***************************************/

    //因为息屏会重新触发一次onLocalTemplateUpdate回调
    private boolean isComponentsInit = false;

    /**
     * 根据直播间模版初始化相关组件
     */
    private void initComponents() {
        if (isComponentsInit) {
            return;
        }
        isComponentsInit = true;

        mLiveInfoList.clear();
        mIdList.clear();

        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler == null) {
            return;
        }
        // 初始化视频控件
        initVideoLayout();
        // 判断当前直播间模版是否有"文档"功能
        if (dwLocalReplayCoreHandler.hasDocView()) {
            initDocLayout();
        }
        // 判断当前直播间模版是否有"聊天"功能
        if (dwLocalReplayCoreHandler.hasChatView()) {
            initChatLayout();
        }
        // 判断当前直播间模版是否有"问答"功能
        if (dwLocalReplayCoreHandler.hasQaView()) {
            initQaLayout();
        }
        // 直播间简介
        initIntroLayout();
        // 模块初始化
        initViewPager();

    }

    // 简介组件
    LocalReplayIntroComponent mIntroComponent;
    // 问答组件
    LocalReplayQAComponent mQaLayout;
    // 聊天组件
    LocalReplayChatComponent mChatLayout;
    // 文档组件
    LocalReplayDocComponent mDocLayout;
    private FloatingPopupWindow.FloatDismissListener floatDismissListener = new FloatingPopupWindow.FloatDismissListener() {
        @Override
        public void dismiss() {
            mReplayRoomLayout.setSwitchText(true);
        }
    };

    // 初始化聊天布局区域
    private void initChatLayout() {
        mIdList.add(R.id.live_portrait_info_chat);
        mTagList.add(mChatTag);
        mChatTag.setVisibility(View.VISIBLE);
        mLiveInfoList.add(mChatLayout);
    }

    // 初始化问答布局区域
    private void initQaLayout() {
        mIdList.add(R.id.live_portrait_info_qa);
        mTagList.add(mQaTag);
        mQaTag.setVisibility(View.VISIBLE);
        mLiveInfoList.add(mQaLayout);
    }

    // 初始化简介布局区域
    private void initIntroLayout() {
        mIdList.add(R.id.live_portrait_info_intro);
        mTagList.add(mIntroTag);
        mIntroTag.setVisibility(View.VISIBLE);
        mLiveInfoList.add(mIntroComponent);
    }

    // 初始化文档布局区域
    private void initDocLayout() {
        mDocLayout = new LocalReplayDocComponent(this);
        if (isVideoMain) {
            mLocalReplayFloatingView.addView(mDocLayout);
        } else {
            if (DWLocalReplayCoreHandler.getInstance().hasDocView()) {
                mReplayVideoContainer.addView(mDocLayout);
                mDocLayout.setDocScrollable(true);
            }
        }
        // 显示小窗
        showFloatingDocLayout();
    }

    // 初始化视频布局区域
    private void initVideoLayout() {
        if (isVideoMain) {
            mReplayVideoContainer.addView(mReplayVideoView);
        } else {
            if (DWLocalReplayCoreHandler.getInstance().hasDocView()) {
                mLocalReplayFloatingView.addView(mReplayVideoView);
            } else {
                mReplayVideoContainer.addView(mReplayVideoView);
            }
        }
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
                    NotificationUtil.sendLocalReplayNotification(LocalReplayPlayActivity.this);
                } else if (NotificationUtil.ACTION_DESTROY.equals(action)) {
                    exit();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY_PAUSE);
        intentFilter.addAction(ACTION_DESTROY);
        registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        NotificationUtil.cancelNotification(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing()) {
            NotificationUtil.sendLocalReplayNotification(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (mRoot != null && mRoot.getHandler() != null) {
            mRoot.getHandler().removeCallbacksAndMessages(null);
        }
        super.onDestroy();
        mLocalReplayFloatingView.dismiss();
        mReplayVideoView.destroy();
        NotificationUtil.cancelNotification(this);
        unregisterReceiver(broadcastReceiver);
    }


    private void initViews() {
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        mReplayVideoContainer = findViewById(R.id.rl_video_container);
        mReplayRoomLayout = findViewById(R.id.replay_room_layout);
        mReplayRoomLayout.setReplayRoomStatusListener(roomStatusListener);
        mReplayMsgLayout = findViewById(R.id.ll_pc_replay_msg_layout);
        mViewPager = findViewById(R.id.live_portrait_container_viewpager);
        mRadioGroup = findViewById(R.id.rg_infos_tag);
        mIntroTag = findViewById(R.id.live_portrait_info_intro);
        mQaTag = findViewById(R.id.live_portrait_info_qa);
        mChatTag = findViewById(R.id.live_portrait_info_chat);
        mDocTag = findViewById(R.id.live_portrait_info_document);

        mLocalReplayFloatingView = new FloatingPopupWindow(this);
        mLocalReplayFloatingView.setFloatDismissListener(floatDismissListener);

        // 视频显示区域
        mReplayVideoView = new LocalReplayVideoView(this);
        mReplayVideoView.setPlayPath(mPlayPath);

        //退出确认弹框
        mExitPopupWindow = new ExitPopupWindow(this);

        mChatLayout = new LocalReplayChatComponent(this);
        mQaLayout = new LocalReplayQAComponent(this);
        mIntroComponent = new LocalReplayIntroComponent(this);
        mDocLayout = new LocalReplayDocComponent(this);

        mReplayRoomLayout.init(isVideoMain);
        mReplayRoomLayout.setActivity(this);
        mChatLayout.setOnChatComponentClickListener(new OnChatComponentClickListener() {
            @Override
            public void onClickChatComponent(Bundle bundle) {
                if (bundle == null) return;

                String type = bundle.getString("type");
                if (CONTENT_IMAGE_COMPONENT.equals(type)) {
                    Intent intent = new Intent(LocalReplayPlayActivity.this, ImageDetailsActivity.class);
                    intent.putExtra("imageUrl", bundle.getString("url"));
                    startActivity(intent);
                } else if (CONTENT_ULR_COMPONET.equals(type)) {
                    Uri uri = Uri.parse(bundle.getString("url"));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }

            }
        });
    }


    // 展示文档悬浮窗布局
    private void showFloatingDocLayout() {
        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler == null) {
            return;
        }
        // 判断当前直播间模版是否有"文档"功能，如果没文档，则小窗功能也不应该有
        if (dwLocalReplayCoreHandler.hasDocView()) {
            mLocalReplayFloatingView.show(mRoot);
        }
    }


    /**************************************  Room 状态回调监听 *************************************/


    private final LocalReplayRoomLayout.ReplayRoomStatusListener roomStatusListener =
            new LocalReplayRoomLayout.ReplayRoomStatusListener() {

                @Override
                public void switchVideoDoc(boolean isVideoMain) {
                    switchView(isVideoMain);
                }

                @Override
                public void openVideoDoc() {
                    if (mReplayRoomLayout.isVideoMain()) {
                        mLocalReplayFloatingView.show(mRoot);
                        if (mDocLayout.getParent() != null)
                            ((ViewGroup) mDocLayout.getParent()).removeView(mDocLayout);
                        mLocalReplayFloatingView.addView(mDocLayout);
                    } else {
                        mLocalReplayFloatingView.show(mRoot);
                        if (mReplayVideoView.getParent() != null)
                            ((ViewGroup) mReplayVideoView.getParent()).removeView(mReplayVideoView);
                        mLocalReplayFloatingView.addView(mReplayVideoView);
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
                public void onClickDocScaleType(int scaleType) {
                    if (mDocLayout != null) {
                        mDocLayout.setScaleType(scaleType);
                    }
                }
            };

    public void switchView(boolean isVideoMain) {
        if (mReplayVideoView.getParent() != null)
            ((ViewGroup) mReplayVideoView.getParent()).removeView(mReplayVideoView);
        if (mDocLayout.getParent() != null)
            ((ViewGroup) mDocLayout.getParent()).removeView(mDocLayout);
        if (isVideoMain) {
            // 缓存视频的切换前的画面
            mLocalReplayFloatingView.addView(mDocLayout);
            mReplayVideoContainer.addView(mReplayVideoView);
            mDocLayout.setDocScrollable(false);//webview不可切换
        } else {
            // 缓存视频的切换前的画面
            mLocalReplayFloatingView.addView(mReplayVideoView);
            mReplayVideoContainer.addView(mDocLayout);
            mDocLayout.setDocScrollable(true);//webview不可切换
        }
    }


    //---------------------------------- 全屏相关逻辑 --------------------------------------------/

    // 退出全屏
    private void quitFullScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mReplayMsgLayout.setVisibility(View.VISIBLE);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(false));
    }

    //---------------------------------- 退出相关逻辑 --------------------------------------------/
    ExitPopupWindow mExitPopupWindow;

    ExitPopupWindow.ConfirmExitRoomListener confirmExitRoomListener = new ExitPopupWindow.ConfirmExitRoomListener() {
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

    /**
     * 模板信息获取监听器，当本地回放模板信获取成功后该方法将会回调
     */
    @Override
    public void onLocalTemplateUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initComponents();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!isPortrait()) {
            quitFullScreen();
            return;
        }
        finish();
//        if (mExitPopupWindow != null) {
//            mExitPopupWindow.setConfirmExitRoomListener(confirmExitRoomListener);
//            mExitPopupWindow.show(mRoot);
//        }
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
        if (mLocalReplayFloatingView != null) {
            mLocalReplayFloatingView.onConfigurationChanged(newConfig.orientation);
        }
    }

    @TargetApi(19)
    private static int getSystemUiVisibility(boolean isFull) {
        if (isFull) {
            int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            return flags;
        } else {
            return View.SYSTEM_UI_FLAG_VISIBLE;
        }

    }

}
