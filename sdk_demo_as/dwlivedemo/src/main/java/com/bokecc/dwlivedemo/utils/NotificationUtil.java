package com.bokecc.dwlivedemo.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.activity.LivePlayActivity;
import com.bokecc.dwlivedemo.activity.LocalReplayPlayActivity;
import com.bokecc.dwlivedemo.activity.ReplayPlayActivity;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.localplay.DWLocalReplayCoreHandler;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.sdk.mobile.live.player.DWBasePlayer;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * 后台播放通知工具类
 * Created by dds on 2020/9/21.
 */
public class NotificationUtil {

    // 通知属性
    private static final String CHANNEL_ID = "HD_SDK_NOTIFICATION";
    private static final String CHANNEL_NAME = "Huode Scene";
    private static final String CHANNEL_DESCRIPTION = "Huode Scene notification";

    // 暂停和关闭通知的广播事件
    public static final String ACTION_PLAY_PAUSE = "hd_notification_play_pause";
    public static final String ACTION_DESTROY = "hd_notification_destroy";


    /**
     * 发送通知 - 直播退到后台播放
     *
     * @param context Activity
     */
    public static void sendLiveNotification(Activity context) {
        DWLiveCoreHandler instance = DWLiveCoreHandler.getInstance();
        RoomInfo roomInfo = instance.getRoomInfo();
        if (roomInfo == null) {
            return;
        }
        ComponentName componentName = new ComponentName(context, LivePlayActivity.class);
        String name = roomInfo.getName();
        sendNotification(context, true, name, componentName);
    }

    /**
     * 发送通知 - 回放退到后台播放
     *
     * @param context Activity
     */
    public static void sendReplayNotification(Activity context) {
        DWReplayCoreHandler instance = DWReplayCoreHandler.getInstance();
        DWReplayPlayer player = instance.getPlayer();
        RoomInfo roomInfo = DWLiveReplay.getInstance().getRoomInfo();
        if (player == null) {
            return;
        }
        boolean playing = player.isPlaying();
        ComponentName componentName = new ComponentName(context, ReplayPlayActivity.class);
        String name = roomInfo == null ? "回放" : roomInfo.getName();
        sendNotification(context, playing, name, componentName);
    }

    /**
     * 发送通知 - 离线回放退到后台播放
     *
     * @param context Activity
     */
    public static void sendLocalReplayNotification(Activity context) {
        DWLocalReplayCoreHandler instance = DWLocalReplayCoreHandler.getInstance();
        DWReplayPlayer player = instance.getPlayer();
        RoomInfo roomInfo = DWLiveLocalReplay.getInstance().getRoomInfo();
        if (player == null) {
            return;
        }
        boolean playing = player.isPlaying() && player.getPlayerState() != DWBasePlayer.State.PLAYING;
        ComponentName componentName = new ComponentName(context, LocalReplayPlayActivity.class);
        String name = roomInfo == null ? "离线回放" : roomInfo.getName();
        sendNotification(context, playing, name, componentName);
    }

    /**
     * 发送通知
     *
     * @param context Activity
     * @param isPlay  是否正在播放
     * @param content 显示内容
     */
    public static void sendNotification(Activity context, boolean isPlay, String content, ComponentName componentName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setComponent(componentName);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        // 设置通知界面
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.item_notification);
        remoteViews.setTextViewText(R.id.id_content, content);
        // 设置图标
        remoteViews.setImageViewResource(R.id.id_play_btn, isPlay ? R.drawable.icon_pause : R.drawable.icon_play);
        // 暂停播放
        Intent pauseAction = new Intent();
        pauseAction.setAction(ACTION_PLAY_PAUSE);
        PendingIntent pendingPauseAction = PendingIntent.getBroadcast(context, -1,
                pauseAction, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.id_play_btn, pendingPauseAction);
        // 结束播放
        Intent destroyAction = new Intent();
        destroyAction.setAction(ACTION_DESTROY);
        PendingIntent pendingDestroyAction = PendingIntent.getBroadcast(context, -1,
                destroyAction, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.id_close_play, pendingDestroyAction);

        builder.setCustomContentView(remoteViews);
        createNotificationChannel(context);
        Notification build = builder.build();
        build.flags = Notification.FLAG_NO_CLEAR;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, build);
    }

    /**
     * 取消通知事件
     *
     * @param context Activity
     */
    public static void cancelNotification(Activity context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }

    // 兼容android 8.0
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);
            //锁屏显示通知
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
