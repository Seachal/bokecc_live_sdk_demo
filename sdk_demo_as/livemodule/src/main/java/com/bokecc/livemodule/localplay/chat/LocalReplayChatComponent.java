package com.bokecc.livemodule.localplay.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.chat.OnChatComponentClickListener;
import com.bokecc.livemodule.live.chat.module.ChatEntity;
import com.bokecc.livemodule.localplay.DWLocalDWReplayChatListener;
import com.bokecc.livemodule.localplay.DWLocalReplayCoreHandler;
import com.bokecc.livemodule.localplay.chat.adapter.LocalReplayChatAdapter;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

/**
 * 回放聊天控件
 */
public class LocalReplayChatComponent extends RelativeLayout implements DWLocalDWReplayChatListener {

    // 回放聊天是否和视频播放时间同步展示
    private final static boolean REPLAY_CHAT_FOLLOW_TIME = true;

    private final Context mContext;
    private RecyclerView mChatList;
    private LocalReplayChatAdapter mChatAdapter;

    public LocalReplayChatComponent(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public LocalReplayChatComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTimerTask();
    }

    public void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.replay_portrait_chat_layout, this, true);
        mChatList = findViewById(R.id.chat_container);
        initChat();
    }

    public void initChat() {
        mChatList.setLayoutManager(new LinearLayoutManager(mContext));
        mChatAdapter = new LocalReplayChatAdapter(mContext);
        mChatList.setAdapter(mChatAdapter);

        mChatAdapter.setOnChatcomponentClickListener(new LocalReplayChatAdapter.OnChatComponentClickListener() {
            @Override
            public void OnChatComponentClickListener(View view, Bundle bundle) {
                if (mChatComponentClickListener != null) {
                    mChatComponentClickListener.onClickChatComponent(bundle);
                }
            }
        });

        // 设置监听
        DWLocalReplayCoreHandler dwReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler != null) {
            dwReplayCoreHandler.setLocalReplayChatListener(this);
        }

        if (REPLAY_CHAT_FOLLOW_TIME) {
            lastChatTime = 0;
            startTimerTask();
        }
    }

    /**
     * 清空聊天数据
     */
    public void clearChatEntities() {
        mChatAdapter.clearChatData();
    }

    /**
     * 回放的聊天添加
     */
    public void addChatEntities(ArrayList<ChatEntity> chatEntities) {
        mChatAdapter.add(chatEntities);
    }

    /**
     * 回放的聊天添加 (追加)
     */
    public void appendChatEntities(ArrayList<ChatEntity> chatEntities) {
        mChatAdapter.append(chatEntities);
        // 如果只是需要追加数据，而不滑动，可以注释掉下面的调用
        mChatList.smoothScrollToPosition(mChatAdapter.getChatListSize() - 1);
    }

    private ArrayList<ChatEntity> mChatEntities = new ArrayList<>();

    private ChatEntity getReplayChatEntity(ReplayChatMsg msg) {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setUserId(msg.getUserId());
        chatEntity.setUserName(msg.getUserName());
        chatEntity.setUserRole(msg.getUserRole());
        chatEntity.setPrivate(false);
        chatEntity.setPublisher(true);
        chatEntity.setMsg(msg.getContent());
        chatEntity.setTime(String.valueOf(msg.getTime()));
        chatEntity.setUserAvatar(msg.getAvatar());
        return chatEntity;
    }

    /**
     * 聊天信息
     *
     * @param replayChatMsgs 聊天信息
     */
    @Override
    public void onChatMessage(TreeSet<ReplayChatMsg> replayChatMsgs) {
        ArrayList<ChatEntity> chatEntities = new ArrayList<>();
        for (ReplayChatMsg msg : replayChatMsgs) {
            // 判断聊天信息的状态 0：显示  1：不显示
            if ("0".equals(msg.getStatus())) {
                chatEntities.add(getReplayChatEntity(msg));
            }
        }
        mChatEntities = chatEntities;

        // 如果不随时间轴展示数据，就将所有数据加载
        if (!REPLAY_CHAT_FOLLOW_TIME) {
            mChatList.post(new Runnable() {
                @Override
                public void run() {
                    addChatEntities(mChatEntities);
                }
            });
        }
    }

    private OnChatComponentClickListener mChatComponentClickListener;

    public void setOnChatComponentClickListener(OnChatComponentClickListener listener) {
        mChatComponentClickListener = listener;
    }
    // --------------------- Demo 随时间展示聊天 定时器  --------------------

    private final Timer timer = new Timer();
    private TimerTask timerTask;
    private int lastChatTime = 0;  //  单位：秒
    private int currentPos = 0;
    // 开始计时器，随时间展示聊天
    private void startTimerTask() {
        stopTimerTask();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // 回放的聊天内容随时间轴推进展示
                if (REPLAY_CHAT_FOLLOW_TIME) {
                    DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
                    // 判断是否为空
                    if (dwLocalReplayCoreHandler == null || dwLocalReplayCoreHandler.getPlayer() == null) {
                        return;
                    }
                    // 获取当前的player
                    final DWReplayPlayer player = dwLocalReplayCoreHandler.getPlayer();
                    if (player == null || !player.isPlaying()) {
                        return;
                    }
                    final ArrayList<ChatEntity> temp_chatEntities = new ArrayList<>();
                    int time = (int) (player.getCurrentPosition() / 1000f);
                    // -------------------往前拖动-----------------------
                    if (time < lastChatTime) {
                        for (ChatEntity entity : mChatEntities) {
                            if (!TextUtils.isEmpty(entity.getTime()) && time >= Integer.valueOf(entity.getTime())) {
                                temp_chatEntities.add(entity);
                            }
                        }
                        currentPos = temp_chatEntities.size();
                        lastChatTime = time;
                        if (mChatList != null) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    clearChatEntities();
                                    addChatEntities(temp_chatEntities);
                                    int chatDataSize = mChatAdapter.getChatListSize();
                                    if (chatDataSize > 0) {
                                        mChatList.smoothScrollToPosition(chatDataSize - 1);
                                    }
                                }
                            });
                        }
                    } else {
                        // -----------------------时间正常走动---------------------
                        temp_chatEntities.clear();
                        for (int i = currentPos; i < mChatEntities.size(); i++) {
                            ChatEntity entity = mChatEntities.get(i);
                            if (!TextUtils.isEmpty(entity.getTime()) && Integer.valueOf(entity.getTime()) <= time) {
                                temp_chatEntities.add(entity);
                            }
                        }

                        currentPos += temp_chatEntities.size();
                        lastChatTime = time;
                        if (mChatList != null && temp_chatEntities.size() > 0) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    appendChatEntities(temp_chatEntities);
                                }
                            });
                        }
                    }

                }
            }
        };
        // 推荐2秒刷新一次聊天数据
        timer.schedule(timerTask, 0, 2 * 1000);
    }

    public void stopTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }
}
