package com.bokecc.dwlivedemo.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.dwlivedemo.BuildConfig;
import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.popup.ExitPopupWindow;
import com.bokecc.dwlivedemo.popup.FloatingPopupWindow;
import com.bokecc.dwlivedemo.utils.NotificationUtil;
import com.bokecc.dwlivedemo.utils.Permissions;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.DWLiveRTCListener;
import com.bokecc.livemodule.live.chat.KeyboardHeightProvider;
import com.bokecc.livemodule.live.chat.LiveChatComponent;
import com.bokecc.livemodule.live.chat.OnChatComponentClickListener;
import com.bokecc.livemodule.live.doc.LiveDocComponent;
import com.bokecc.livemodule.live.function.FunctionCallBack;
import com.bokecc.livemodule.live.function.FunctionHandler;
import com.bokecc.livemodule.live.intro.LiveIntroComponent;
import com.bokecc.livemodule.live.morefunction.MoreFunctionLayout;
import com.bokecc.livemodule.live.morefunction.rtc.RTCVideoLayout;
import com.bokecc.livemodule.live.qa.LiveQAComponent;
import com.bokecc.livemodule.live.room.LiveRoomLayout;
import com.bokecc.livemodule.live.video.LiveVideoView;
import com.bokecc.livemodule.utils.AppPhoneStateListener;
import com.bokecc.livemodule.view.CustomToast;
import com.bokecc.sdk.mobile.live.DWLive;

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
 * 直播播放页（默认视频大屏，文档小屏，可手动切换）
 */
public class LivePlayActivity extends BaseActivity implements DWLiveRTCListener {

    private static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    View mRoot;
    // 顶部布局
    RelativeLayout mLiveTopLayout;
    // 底部布局
    RelativeLayout mLiveMsgLayout;
    // 大屏:视频 连麦 文档
    RelativeLayout rl_video_container;
    // 直播间状态布局
    LiveRoomLayout mLiveRoomLayout;
    // 小屏:悬浮弹窗（用于展示文档和视频 连麦）
    FloatingPopupWindow mLiveFloatingView;
    // 直播功能处理机制（签到、答题卡/投票、问卷、抽奖）
    FunctionHandler mFunctionHandler;
    // 更多功能控件（私聊、连麦、公告）
    MoreFunctionLayout mMoreFunctionLayout;
    // 软键盘控制
    private KeyboardHeightProvider keyboardHeightProvider;

    // 随堂测和答题收起功能小图标
    private TextView mLandVote;
    private TextView mPortraitVote;
    // 广播接收者
    private BroadcastReceiver broadcastReceiver;

    // intent 是否是默认视频大屏
    private boolean isVideoMain;

    /**
     * 启动直播界面
     *
     * @param activity    activity
     * @param isVideoMain 是否是以大屏为主
     */
    public static void openActivity(Activity activity, boolean isVideoMain) {
        Intent intent = new Intent(activity, LivePlayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("isVideoMain", isVideoMain);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 请求全屏
//        requestFullScreenFeature();
        // 请求状态栏黑色
        requestStatusBarBlack(this);
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_play);

        isVideoMain = getIntent().getBooleanExtra("isVideoMain", true);

        initViews();
        initViewPager();
        initRoomStatusListener();
        mFunctionHandler = new FunctionHandler();
        mFunctionHandler.initFunctionHandler(this, functionCallBack);
        keyboardHeightProvider = new KeyboardHeightProvider(this);
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                keyboardHeightProvider.start();
            }
        });
        // 初始化广播接收器
        initReceiver();
        // 开始播放
        mLiveVideoView.start();

        // 设置防录屏
        mLiveVideoView.setAntiRecordScreen(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        keyboardHeightProvider.addKeyboardHeightObserver(mChatLayout);
        keyboardHeightProvider.addKeyboardHeightObserver(mLiveRoomLayout);
        keyboardHeightProvider.addKeyboardHeightObserver(mQaLayout);
        keyboardHeightProvider.addKeyboardHeightObserver(mMoreFunctionLayout);
        mFunctionHandler.setRootView(mRoot);
        // 开启弹幕
        mLiveRoomLayout.openLiveBarrage();
        // 取消通知
        NotificationUtil.cancelNotification(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        keyboardHeightProvider.clearObserver();
        mFunctionHandler.removeRootView();
        // 停止弹幕
        mLiveRoomLayout.closeLiveBarrage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing()) {
            NotificationUtil.sendLiveNotification(this);
        }
    }

    @Override
    protected void onDestroy() {
        keyboardHeightProvider.close();
        mLiveFloatingView.dismiss();
        mFunctionHandler.onDestroy(this);
        mLiveVideoView.stop();
        mLiveRoomLayout.destroy();
        if (mLiveRtcView != null) {
            mLiveRtcView.destroy();
        }
        mLiveVideoView.destroy();
        // 取消通知显示
        NotificationUtil.cancelNotification(this);
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!isPortrait()) {
            if (!mLiveRoomLayout.onBackPressed()) {
                quitFullScreen();
            }
            return;
        } else {
            if (mChatLayout != null && mChatLayout.onBackPressed()) {
                return;
            }
        }
        // 弹出退出提示
        showExitTips();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 横屏隐藏状态栏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(true));
            //如果随堂测 答题卡的缩小按钮存在
            if (mPortraitVote.getVisibility() == VISIBLE) {
                mPortraitVote.setVisibility(View.GONE);
                mLandVote.setVisibility(VISIBLE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(false));
            if (mLandVote.getVisibility() == VISIBLE) {
                mLandVote.setVisibility(View.GONE);
                mPortraitVote.setVisibility(VISIBLE);
            }
        }
        // 调整窗口的位置
        if (mLiveFloatingView != null) {
            mLiveFloatingView.onConfigurationChanged(newConfig.orientation);
        }
        // 开启或者关闭弹幕
        mLiveRoomLayout.openLiveBarrage();
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

    private void initViews() {
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        mLiveTopLayout = findViewById(R.id.rl_pc_live_top_layout);

        rl_video_container = findViewById(R.id.rl_video_container);
        mLiveRoomLayout = findViewById(R.id.live_room_layout);
        mLiveRoomLayout.setActivity(this);
        // 视频下方界面
        mLiveMsgLayout = findViewById(R.id.ll_pc_live_msg_layout);
        mViewPager = findViewById(R.id.live_portrait_container_viewpager);
        mRadioGroup = findViewById(R.id.rg_infos_tag);
        mIntroTag = findViewById(R.id.live_portrait_info_intro);
        mQaTag = findViewById(R.id.live_portrait_info_qa);
        mChatTag = findViewById(R.id.live_portrait_info_chat);
        mDocTag = findViewById(R.id.live_portrait_info_document);
        mMoreFunctionLayout = findViewById(R.id.more_function_layout);

        //随堂测 答题卡缩小的按钮
        mLandVote = findViewById(R.id.tv_land_vote);
        mLandVote.setOnClickListener(voteClickListener);
        mPortraitVote = findViewById(R.id.tv_portrait_vote);
        mPortraitVote.setOnClickListener(voteClickListener);

        // 弹出框界面
        mExitPopupWindow = new ExitPopupWindow(this);
        mLiveFloatingView = new FloatingPopupWindow(LivePlayActivity.this);
        mLiveFloatingView.setFloatDismissListener(new FloatingPopupWindow.FloatDismissListener() {
            @Override
            public void dismiss() {
                if (mLiveRoomLayout != null) {
                    mLiveRoomLayout.setSwitchText(true);
                }

            }
        });

        // 初始化控制布局，需登录成功之后调用
        mLiveRoomLayout.init(isVideoMain);
        // 设置连麦监听
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.setDwLiveRTCListener(this);
        }
        // 检测权限（用于连麦）
        doPermissionCheck();


    }


    private void initReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (NotificationUtil.ACTION_PLAY_PAUSE.equals(action)) {
                    NotificationUtil.sendLiveNotification(LivePlayActivity.this);
                } else if (NotificationUtil.ACTION_DESTROY.equals(action)) {
                    exit();
                } else if (ACTION_PAUSE.equals(action)) {
                    // 取消连麦
                    DWLive.getInstance().disConnectSpeak();
                    // 取消连麦申请
                    DWLiveCoreHandler.getInstance().cancelRTCConnect();


                } else if (ACTION_RESUME.equals(action)) {
                    // 重新播放video
                    if (mLiveVideoView != null) {
                        mLiveVideoView.exitRtcMode();
                    }
                }else if (ACTION_CONNECTIVITY_CHANGE.equals(action)){
                    ConnectivityManager connectionManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  //得到系统服务类
                    NetworkInfo networkInfo=connectionManager.getActiveNetworkInfo();
                    if(networkInfo!=null&&networkInfo.isAvailable()){
                        if (mLiveVideoView!=null){
                            mLiveVideoView.onNetWorkChanged(true);
                        }
                    }else{
                        if (mLiveVideoView!=null){
                            mLiveVideoView.onNetWorkChanged(false);
                        }
                    }


                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY_PAUSE);
        intentFilter.addAction(ACTION_DESTROY);
        intentFilter.addAction(ACTION_PAUSE);
        intentFilter.addAction(ACTION_RESUME);
        intentFilter.addAction(ACTION_CONNECTIVITY_CHANGE);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 进行权限检测
     */
    private void doPermissionCheck() {
        Permissions.request(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new Permissions.Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        if (integer == PackageManager.PERMISSION_GRANTED) {
                            //Toast.makeText(LivePlayActivity.this, "Permission Allow", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LivePlayActivity.this, "请开启相关权限", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    //*************************************** 直播间状态监听 ***************************************/
    LiveRoomLayout.LiveRoomStatusListener roomStatusListener = new LiveRoomLayout.LiveRoomStatusListener() {
        @Override
        public void switchVideoDoc(boolean isVideoMain) {
            switchView(isVideoMain);  // 切换视频
        }

        @Override
        public void openVideoDoc() {
            if (mLiveFloatingView == null) return;
            if (mLiveRoomLayout.isVideoMain()) {
                mLiveFloatingView.show(mRoot);
                if (mDocLayout.getParent() != null)
                    ((ViewGroup) mDocLayout.getParent()).removeView(mDocLayout);
                mLiveFloatingView.addView(mDocLayout);
            } else {
                mLiveFloatingView.show(mRoot);
                if (mLiveVideoView.getParent() != null)
                    ((ViewGroup) mLiveVideoView.getParent()).removeView(mLiveVideoView);
                mLiveFloatingView.addView(mLiveVideoView);
            }
        }

        // 退出直播间
        @Override
        public void closeRoom() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 如果当前状态是竖屏，则弹出退出确认框，否则切换为竖屏
                    if (isPortrait()) {
                        showExitTips();
                    } else {
                        quitFullScreen();
                    }
                }
            });
        }

        // 全屏
        @Override
        public void fullScreen() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mLiveMsgLayout.setVisibility(View.GONE);
                }
            });
        }

        // 踢出直播间
        @Override
        public void kickOut() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CustomToast.showToast(LivePlayActivity.this, "您已经被踢出直播间", Toast.LENGTH_SHORT);
                    finish();
                }
            });
        }

        @Override
        public void onClickDocScaleType(int scaleType) {
            if (mDocLayout != null) {
                mDocLayout.setScaleType(scaleType);
            }
        }

        @Override
        public void onStreamEnd() {
            //初始化视频界面并隐藏悬浮框
            if (mLiveFloatingView != null) {
                mLiveFloatingView.dismiss();
            }
        }

        @Override
        public void onStreamStart() {
            if (!mLiveFloatingView.isShowing()) {
                //显示小窗
                showFloatingLayout();
                switchView(mLiveRoomLayout.isVideoMain());  // 开课回调
            }

            //尝试获取一下当前正在进行的随堂测，需要考虑用户重新登录及Home桌面的问题
            DWLive.getInstance().getPracticeInformation();
        }
    };

    // 初始化房间状态监听
    private void initRoomStatusListener() {
        if (mLiveRoomLayout == null) {
            return;
        }
        mLiveRoomLayout.setLiveRoomStatusListener(roomStatusListener);
    }

    // 退出全屏
    private void quitFullScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mLiveMsgLayout.setVisibility(VISIBLE);
    }

    // 切换大小屏
    private void switchView(boolean isVideoMain) {
        if (mLiveVideoView.getParent() != null) {
            ((ViewGroup) (mLiveVideoView.getParent())).removeView(mLiveVideoView);
        }
        if (mDocLayout == null) {
            mDocLayout = new LiveDocComponent(this);
        }
        if (DWLiveCoreHandler.getInstance().hasPdfView()) {
            if (mDocLayout.getParent() != null) {
                ((ViewGroup) (mDocLayout.getParent())).removeView(mDocLayout);
            }
        }
        if (DWLiveCoreHandler.getInstance().isRtcing() && DWLiveCoreHandler.getInstance().isVideoRtc()) {
            //----------------------------------------------连麦中切换窗口
            if (mLiveRtcView.getParent() != null) {
                ((ViewGroup) (mLiveRtcView.getParent())).removeView(mLiveRtcView);
            }
            if (isVideoMain) {
                if (DWLiveCoreHandler.getInstance().hasPdfView()) {
                    mLiveFloatingView.addView(mDocLayout);
                    mDocLayout.setDocScrollable(false);
                }
                rl_video_container.addView(mLiveRtcView);
            } else {
                if (DWLiveCoreHandler.getInstance().hasPdfView()) {
                    rl_video_container.addView(mDocLayout, 0);
                    mDocLayout.setDocScrollable(true);
                }
                mLiveFloatingView.addView(mLiveRtcView);
            }
        } else {
            //---------------------------------------------player拉流切换窗口
            if (isVideoMain) {
                if (DWLiveCoreHandler.getInstance().hasPdfView()) {
                    mLiveFloatingView.addView(mDocLayout);
                    mDocLayout.setDocScrollable(false);
                }
                // 处理连麦
                if (mLiveRtcView.getParent() != null) {
                    ((ViewGroup) (mLiveRtcView.getParent())).removeView(mLiveRtcView);
                }
                rl_video_container.addView(mLiveRtcView);
                rl_video_container.addView(mLiveVideoView);


            } else {
                // 处理连麦
                if (mLiveRtcView.getParent() != null) {
                    ((ViewGroup) (mLiveRtcView.getParent())).removeView(mLiveRtcView);
                }
                mLiveFloatingView.addView(mLiveRtcView);
                mLiveFloatingView.addView(mLiveVideoView);
                if (DWLiveCoreHandler.getInstance().hasPdfView()) {
                    rl_video_container.addView(mDocLayout, 0);
                    mDocLayout.setDocScrollable(true);
                }

            }
        }
    }


    //*************************************** 下方布局 ***************************************/

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
                hideKeyboard();
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

    //********************************* 直播 问答、聊天、文档、简介 组件相关 ******************/

    // 简介组件
    LiveIntroComponent mIntroComponent;
    // 问答组件
    LiveQAComponent mQaLayout;
    // 聊天组件
    LiveChatComponent mChatLayout;
    // 文档组件
    LiveDocComponent mDocLayout;
    // 视频组件
    LiveVideoView mLiveVideoView;
    // 连麦组件
    RTCVideoLayout mLiveRtcView;

    //根据直播间模版初始化相关组件
    private void initComponents() {
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler == null) {
            return;
        }
        // 显示文档
        if (dwLiveCoreHandler.hasPdfView()) {
            initDocLayout();
        }
        //初始化连麦
        initRtcLayout();
        // 显示视频
        initVideoLayout();
        // 显示悬浮窗
        showFloatingLayout();

        // 显示聊天
        if (dwLiveCoreHandler.hasChatView()) {
            initChatLayout();
        }
        // 显示文档
        if (dwLiveCoreHandler.hasQaView()) {
            initQaLayout();
        }
        // 直播间简介
        initIntroLayout();

        // 判断当前直播间模版是否是仅视频大屏模式，如果是，隐藏更多功能
        if (dwLiveCoreHandler.isOnlyVideoTemplate()) {
            mMoreFunctionLayout.setVisibility(View.GONE);
        }

    }

    // 初始化简介布局区域
    private void initIntroLayout() {
        mIdList.add(R.id.live_portrait_info_intro);
        mTagList.add(mIntroTag);
        mIntroTag.setVisibility(VISIBLE);
        mIntroComponent = new LiveIntroComponent(this);
        mLiveInfoList.add(mIntroComponent);
    }

    // 初始化问答布局区域
    private void initQaLayout() {
        mIdList.add(R.id.live_portrait_info_qa);
        mTagList.add(mQaTag);
        mQaTag.setVisibility(VISIBLE);
        mQaLayout = new LiveQAComponent(this);
        mLiveInfoList.add(mQaLayout);
    }

    // 初始化聊天布局区域
    private void initChatLayout() {
        mIdList.add(R.id.live_portrait_info_chat);
        mTagList.add(mChatTag);
        mChatTag.setVisibility(VISIBLE);
        mChatLayout = new LiveChatComponent(this);
        mChatLayout.setPopView(mRoot);
        //initChatView();
        mLiveInfoList.add(mChatLayout);
        mChatLayout.setBarrageLayout(mLiveRoomLayout.getLiveBarrageLayout());
        mChatLayout.setOnChatComponentClickListener(new OnChatComponentClickListener() {
            @Override
            public void onClickChatComponent(Bundle bundle) {
                if (bundle == null) return;
                String type = bundle.getString("type");
                if (CONTENT_IMAGE_COMPONENT.equals(type)) {
                    Intent intent = new Intent(LivePlayActivity.this, ImageDetailsActivity.class);
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

    // 初始化文档布局区域
    private void initDocLayout() {
        mDocLayout = new LiveDocComponent(this);
        if (isVideoMain) {
            mLiveFloatingView.removeNowView();
            mLiveFloatingView.addView(mDocLayout);
        } else {
            rl_video_container.addView(mDocLayout);
            mDocLayout.setDocScrollable(true);
        }

    }

    //初始化连麦布局区域
    private void initRtcLayout() {
        mLiveRtcView = new RTCVideoLayout(this);
        if (mLiveRtcView.getParent() != null) {
            ((ViewGroup) mLiveRtcView.getParent()).removeView(mLiveRtcView);
        }
        if (isVideoMain) {
            rl_video_container.addView(mLiveRtcView);
        } else {
            DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
            if (dwLiveCoreHandler != null && dwLiveCoreHandler.hasPdfView()) {
                mLiveFloatingView.addView(mLiveRtcView);
            } else {
                rl_video_container.addView(mLiveRtcView);
            }
        }
    }

    // 初始化视频布局区域
    private void initVideoLayout() {
        mLiveVideoView = new LiveVideoView(this);
        if (mLiveVideoView.getParent() != null) {
            ((ViewGroup) mLiveVideoView.getParent()).removeView(mLiveVideoView);
        }
        if (isVideoMain) {
            rl_video_container.addView(mLiveVideoView);
        } else {
            DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
            if (dwLiveCoreHandler != null && dwLiveCoreHandler.hasPdfView()) {
                mLiveFloatingView.addView(mLiveVideoView);
            } else {
                rl_video_container.addView(mLiveVideoView);
            }

        }
    }


    // 展示文档悬浮窗布局
    private void showFloatingLayout() {
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler == null) {
            return;
        }
        // 判断当前直播间模版是否有"文档"功能，如果没文档，则小窗功能也不应该有
        if (dwLiveCoreHandler.hasPdfView()) {
            mLiveFloatingView.show(mRoot);
        }
    }

    //******************************** 连麦状态监听  ********************************************/


    /**
     * isVideoRtc always is true
     *
     * @param isVideoRtc 当前连麦是否是视频连麦
     * @param videoSize  视频的宽高，值为"600x400"
     */
    @Override
    public void onEnterSpeak(final boolean isVideoRtc, final boolean needAdjust, final String videoSize) {
        //-------------显示连麦界面------------------------
        if (mLiveRtcView.getParent() != null) {
            ((ViewGroup) (mLiveRtcView.getParent())).removeView(mLiveRtcView);
        }
        if (mLiveRoomLayout.isVideoMain()) {
            rl_video_container.addView(mLiveRtcView);
        } else {
            mLiveFloatingView.addView(mLiveRtcView);
        }
        // ----------- 处理界面逻辑-----------------------
        if (mLiveVideoView != null) {
            mLiveVideoView.enterRtcMode(isVideoRtc);
        }
        if (mLiveRtcView != null) {
            mLiveRtcView.enterSpeak(isVideoRtc, needAdjust, videoSize);
        }
    }


    @Override
    public void onDisconnectSpeak() {
        if (mLiveRtcView != null) {
            mLiveRtcView.disconnectSpeak();
        }
        if (mLiveVideoView != null) {
            if (DWLiveCoreHandler.getInstance().isVideoRtc()) {
                // 隐藏连麦组件
                if (mLiveVideoView.getParent() != null) {
                    ((ViewGroup) (mLiveVideoView.getParent())).removeView(mLiveVideoView);
                }
                if (mLiveRoomLayout.isVideoMain()) {
                    rl_video_container.addView(mLiveVideoView);
                } else {
                    mLiveFloatingView.addView(mLiveVideoView);
                }
            }
            // 通话中不恢复播放
            if (AppPhoneStateListener.isPhoneInCall) {
                return;
            }
            // 重新播放video
            mLiveVideoView.exitRtcMode();

        }
    }

    @Override
    public void onSpeakError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLiveRtcView != null) {
                    mLiveRtcView.speakError(e);
                }
                if (mLiveVideoView != null) {
                    mLiveVideoView.exitRtcMode();
                }
            }
        });
    }

    //********************************** 工具方法 *******************************************/

    // 隐藏输入法
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mLiveTopLayout.getWindowToken(), 0);
        }
    }


    //*********************************  退出相关逻辑 ***************************************/
    private ExitPopupWindow mExitPopupWindow;

    // 弹出退出提示
    private void showExitTips() {
        if (mExitPopupWindow != null) {
            mExitPopupWindow.setConfirmExitRoomListener(new ExitPopupWindow.ConfirmExitRoomListener() {
                @Override
                public void onConfirmExitRoom() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            exit();
                        }
                    });
                }
            });
            mExitPopupWindow.show(mRoot);
        }
    }

    // 退出房间
    private void exit() {
        mExitPopupWindow.dismiss();
        finish();
    }


    //*********************************  答题卡和随堂测收起功能*********************************/
    private boolean isVote;
    //添加答题卡收起监听
    private final FunctionCallBack functionCallBack = new FunctionCallBack() {
        @Override
        public void onMinimize(boolean isVote) {
            super.onMinimize(isVote);
            LivePlayActivity.this.isVote = isVote;
            if (isVote) {
                mPortraitVote.setBackgroundResource(R.drawable.float_answer2);
                mLandVote.setBackgroundResource(R.drawable.float_answer2);
            } else {
                mPortraitVote.setBackgroundResource(R.drawable.float_answer);
                mLandVote.setBackgroundResource(R.drawable.float_answer);
            }
            //显示按钮
            if (isPortrait()) {
                mPortraitVote.setVisibility(VISIBLE);
                mLandVote.setVisibility(View.GONE);
            } else {
                mLandVote.setVisibility(VISIBLE);
                mPortraitVote.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClose() {
            super.onClose();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //隐藏按钮
                    mPortraitVote.setVisibility(View.GONE);
                    mLandVote.setVisibility(View.GONE);
                }
            });
        }
    };
    //随堂测 答题卡缩小按钮的点击时间
    View.OnClickListener voteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mFunctionHandler != null) {
                if (isVote) {
                    mFunctionHandler.onVoteStart();
                } else {
                    mFunctionHandler.onPractice();
                }
            }
            mPortraitVote.setVisibility(View.GONE);
            mLandVote.setVisibility(View.GONE);
        }
    };

}
