package com.bokecc.dwlivedemo.activity.extra;

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.download.FileUtil;
import com.bokecc.dwlivedemo.popup.ExitPopupWindow;
import com.bokecc.dwlivedemo.popup.FloatingPopupWindow;
import com.bokecc.livemodule.replaymix.DWReplayMixCoreHandler;
import com.bokecc.livemodule.replaymix.chat.ReplayMixChatComponent;
import com.bokecc.livemodule.replaymix.doc.ReplayMixDocComponent;
import com.bokecc.livemodule.replaymix.intro.ReplayMixIntroComponent;
import com.bokecc.livemodule.replaymix.qa.ReplayMixQAComponent;
import com.bokecc.livemodule.replaymix.room.ReplayMixRoomLayout;
import com.bokecc.livemodule.replaymix.video.ReplayMixVideoView;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLoginInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 回放播放页（支持在线回放和离线回放同页面列表切换）
 * <p>
 * 注意事项：
 * <p>
 * 1. 离线回放CCR文件必须已经下载解压好了。
 * 2. 播放离线回放必须已经授权'存储'权限
 */
public class ReplayMixPlayActivity extends BaseActivity implements View.OnClickListener {

    View mRoot;

    FrameLayout mReplayMsgLayout;

    // 大屏布局
    RelativeLayout mReplayVideoContainer;
    // 悬浮弹窗（用于展示文档和视频）
    FloatingPopupWindow mReplayFloatingView;
    // 回放视频View
    ReplayMixVideoView mReplayVideoView;
    // 文档组件
    ReplayMixDocComponent mDocLayout;

    ReplayMixRoomLayout mReplayRoomLayout;

    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestFullScreenFeature();
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix_replay_play);
        initViews();
        initViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 展示悬浮窗
        mRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                showFloatingDocLayout();
                mReplayVideoView.start();
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReplayVideoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReplayFloatingView.dismiss();
        mReplayVideoView.destroy();
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

    private void initViews() {
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);

        mReplayVideoContainer = findViewById(R.id.rl_video_container);
        mReplayVideoView = findViewById(R.id.replay_video_view);

        mReplayRoomLayout = findViewById(R.id.replay_room_layout);
        mReplayRoomLayout.setReplayRoomStatusListener(roomStatusListener);

        mReplayMsgLayout = findViewById(R.id.ll_pc_replay_msg_layout);
        mViewPager = findViewById(R.id.live_portrait_container_viewpager);
        mRadioGroup = findViewById(R.id.rg_infos_tag);
        mIntroTag = findViewById(R.id.live_portrait_info_intro);
        mQaTag = findViewById(R.id.live_portrait_info_qa);
        mChatTag = findViewById(R.id.live_portrait_info_chat);
        mDocTag = findViewById(R.id.live_portrait_info_document);

        mReplayFloatingView = new FloatingPopupWindow(this);

        mExitPopupWindow = new ExitPopupWindow(this);

        // 在线回放1 按钮点击事件
        findViewById(R.id.replay_one).setOnClickListener(this);
        // 在线回放2 按钮点击事件
        findViewById(R.id.replay_two).setOnClickListener(this);
        // 在线回放3 按钮点击事件
        findViewById(R.id.replay_three).setOnClickListener(this);
    }

    /**
     * 根据直播间模版初始化相关组件
     */
    private void initComponents() {
        initDocLayout();
        initChatLayout();
        initQaLayout();
        initIntroLayout();
    }

    /********************************* 直播重要组件相关 ***************************************/

    // 简介组件
    ReplayMixIntroComponent mIntroComponent;

    // 问答组件
    ReplayMixQAComponent mQaLayout;

    // 聊天组件
    ReplayMixChatComponent mChatLayout;


    // 初始化聊天布局区域
    private void initChatLayout() {
        mIdList.add(R.id.live_portrait_info_chat);
        mTagList.add(mChatTag);
        mChatTag.setVisibility(View.VISIBLE);
        mChatLayout = new ReplayMixChatComponent(this);
        mLiveInfoList.add(mChatLayout);
    }

    // 初始化问答布局区域
    private void initQaLayout() {
        mIdList.add(R.id.live_portrait_info_qa);
        mTagList.add(mQaTag);
        mQaTag.setVisibility(View.VISIBLE);
        mQaLayout = new ReplayMixQAComponent(this);
        mLiveInfoList.add(mQaLayout);
    }

    // 初始化简介布局区域
    private void initIntroLayout() {
        mIdList.add(R.id.live_portrait_info_intro);
        mTagList.add(mIntroTag);
        mIntroTag.setVisibility(View.VISIBLE);
        mIntroComponent = new ReplayMixIntroComponent(this);
        mLiveInfoList.add(mIntroComponent);
    }

    // 初始化文档布局区域
    private void initDocLayout() {
        mDocLayout = new ReplayMixDocComponent(this);
        mReplayFloatingView.addView(mDocLayout);
    }

    // 展示文档悬浮窗布局
    private void showFloatingDocLayout() {
        mReplayFloatingView.show(mRoot);
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
        pagerAdapter = new PagerAdapter() {
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
        mViewPager.setAdapter(pagerAdapter);
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

    /**************************************  Room 状态回调监听 *************************************/

    boolean isVideoMain = true;

    private final ReplayMixRoomLayout.ReplayRoomStatusListener roomStatusListener = new ReplayMixRoomLayout.ReplayRoomStatusListener() {

        @Override
        public void switchVideoDoc() {
            if (isVideoMain) {
                // 缓存视频的切换前的画面
                mReplayVideoView.cacheScreenBitmap();
                mReplayVideoContainer.removeAllViews();
                mReplayFloatingView.removeAllView();
                ViewGroup.LayoutParams lp = mDocLayout.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mDocLayout.setLayoutParams(lp);
                mDocLayout.setLayoutParams(lp);
                mReplayFloatingView.addView(mReplayVideoView);
                mReplayVideoContainer.addView(mDocLayout);
                isVideoMain = false;
                mReplayRoomLayout.setVideoDocSwitchText("切换视频");

            } else {
                // 缓存视频的切换前的画面
                mReplayVideoView.cacheScreenBitmap();
                mReplayVideoContainer.removeAllViews();
                mReplayFloatingView.removeAllView();
                ViewGroup.LayoutParams lp = mDocLayout.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mDocLayout.setLayoutParams(lp);
                mReplayFloatingView.addView(mDocLayout);
                mReplayVideoContainer.addView(mReplayVideoView);
                isVideoMain = true;
                mReplayRoomLayout.setVideoDocSwitchText("切换文档");
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
                }
            });
        }

    };

    //---------------------------------- 全屏相关逻辑 --------------------------------------------/

    // 退出全屏
    private void quitFullScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mReplayMsgLayout.setVisibility(View.VISIBLE);
        mReplayRoomLayout.quitFullScreen();
    }

    //---------------------------------- 退出相关逻辑 --------------------------------------------/

    ExitPopupWindow mExitPopupWindow;  // 退出确认弹出框界面

    ExitPopupWindow.ConfirmExitRoomListener confirmExitRoomListener = new ExitPopupWindow.ConfirmExitRoomListener() {
        @Override
        public void onConfirmExitRoom() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChatLayout.stopTimerTask();
                    mReplayRoomLayout.stopTimerTask();
                    mExitPopupWindow.dismiss();

                    finish();
                }
            });
        }
    };

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
            int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            return flags;
        } else {
            return View.SYSTEM_UI_FLAG_VISIBLE;
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();// 回放切换
        if (id == R.id.replay_one) {
            ReplayLoginInfo replayLoginInfoOne = new ReplayLoginInfo();
            replayLoginInfoOne.setRoomId("341CC70CE320768C9C33DC5901307461");
            replayLoginInfoOne.setUserId("920022FE264A70C1");
            replayLoginInfoOne.setRecordId("1D707B324473BCC8");
            replayLoginInfoOne.setViewerName("123");
            replayLoginInfoOne.setViewerToken("123");
            startLiveReplay(replayLoginInfoOne);
        } else if (id == R.id.replay_two) {
            ReplayLoginInfo replayLoginInfoTwo = new ReplayLoginInfo();
            replayLoginInfoTwo.setRoomId("9E351AD80BB8BD9B9C33DC5901307461");
            replayLoginInfoTwo.setUserId("35BF2C2678E055D3");
            replayLoginInfoTwo.setRecordId("46963C06E5BD1F1E");
            replayLoginInfoTwo.setViewerName("123");
            replayLoginInfoTwo.setViewerToken("123");
            startLiveReplay(replayLoginInfoTwo);
        } else if (id == R.id.replay_three) {// 需要 "存储" 权限
            startLocalReplay("0FD9C56FD11B4B29.ccr");
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

    /**
     * 开始在线回放
     *
     * @param info 在线回放登录信息
     */
    public void startLiveReplay(ReplayLoginInfo info) {
        DWReplayMixCoreHandler handler = DWReplayMixCoreHandler.getInstance();
        handler.startLiveReplay(info);
    }

    /**
     * 开始离线回放
     *
     * @param ccrName CCR文件名称（此部分逻辑可以参考完整的离线回放Demo：localreplaydemo）
     */
    public void startLocalReplay(String ccrName) {
        //离线资源文件路径
        String DOWNLOAD_DIR = FileUtil.getCCDownLoadPath(this);
        File oriFile = new File(DOWNLOAD_DIR, ccrName);

        String unzipDir = getUnzipDir(oriFile);
        DWReplayMixCoreHandler handler = DWReplayMixCoreHandler.getInstance();
        handler.startLocalReplay(unzipDir);
    }

}
