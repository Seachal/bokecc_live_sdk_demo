package com.bokecc.livemodule.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;

/**
 * 进度记忆管理类
 * Created by dds on 2020/9/14.
 */
public class ProRecordWorker {

    private static final String TAG = ProRecordWorker.class.getSimpleName();
    public static final String LAST_POSITION = "lastPosition";
    public static final String RECORD_ID = "recordId";

    private final int DELAY_HIDE_JUMP = 2;

    private HandlerThread handlerThread;
    private volatile Looper looper;
    private volatile RecordHandler handler;

    private final Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DELAY_HIDE_JUMP) {
                if (textView != null) {
                    textView.setVisibility(View.GONE);
                }
            }
        }
    };
    private DWReplayPlayer player;
    private String recordTag = ""; // 记忆标识 在线:视频的recordId，离线:播放的路径
    private TextView textView;

    private final Context context;
    private final UICallback uiCallback;

    // ui回调
    public interface UICallback {
        void proRecordSeek(long time);
    }

    public ProRecordWorker(Context context, UICallback uiCallback) {
        this.context = context;
        this.uiCallback = uiCallback;
    }

    /**
     * 设置需要显示的内容
     */
    public void setTextView(TextView textView) {
        this.textView = textView;

    }

    /**
     * 设置播放器
     *
     * @param player player
     */
    public void setPlayer(DWReplayPlayer player) {
        this.player = player;
    }

    /**
     * 设置进度记忆的唯一标识符
     *
     * @param recordTag 在线回放：recordId   离线回访：path
     */
    public void setRecordTag(String recordTag) {
        this.recordTag = recordTag;
    }

    /**
     * 开启进度记忆
     */
    public void start() {
        if (handlerThread != null && handlerThread.isAlive()) {
            handlerThread.quit();
        }
        handlerThread = new HandlerThread("progressRecord");
        handlerThread.start();
        looper = handlerThread.getLooper();
        handler = new RecordHandler(looper, player, recordTag);
        if (this.textView != null) {
            String spRecordId = SPUtil.getInstance().getString(RECORD_ID);
            final long spLastPosition = SPUtil.getInstance().getLong(LAST_POSITION);
            if (spRecordId.equals(recordTag) && spLastPosition > 0) {
                // 设置进度跳转显示内容
                SpannableStringBuilder spannableString = new SpannableStringBuilder("您上次观看到  ");
                long playSecond = Math.round((double) spLastPosition / 1000) * 1000;
                String formatTime = TimeUtil.getFormatTime(playSecond);
                spannableString.append(formatTime).append("  ");
                SpannableString string = new SpannableString("跳转");
                string.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        if (uiCallback != null) {
                            uiCallback.proRecordSeek(spLastPosition);
                        }
                        if (textView != null) {
                            textView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(context.getResources().getColor(R.color.colorTitleBg));
                        ds.setTextSize(DensityUtil.sp2px(context, 16));
                    }
                }, 0, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.append(string);
                textView.setText(spannableString);
                textView.setVisibility(View.VISIBLE);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }

        //  延迟5s开始记录进度
        handler.removeMessages(RecordHandler.START);
        handler.sendEmptyMessageDelayed(RecordHandler.START, 7000);

        // 延迟10s隐藏提示语
        handler.removeMessages(DELAY_HIDE_JUMP);
        uiHandler.sendEmptyMessageDelayed(DELAY_HIDE_JUMP, 10000);
    }

    /**
     * 停止进度记忆
     */
    public void stop() {
        if (handler != null && handlerThread != null && handlerThread.isAlive()) {
            handler.sendEmptyMessage(RecordHandler.STOP);
        }
        if (looper != null) {
            looper.quit();
        }
        if (uiHandler != null) {
            uiHandler.removeCallbacksAndMessages(null);
        }
        if (textView != null) {
            textView.setVisibility(View.GONE);
        }

    }


    //清除进度记忆
    public void clear() {
        SPUtil.getInstance().put(RECORD_ID, "");
    }

    private static class RecordHandler extends Handler {
        static final int START = 0x001;
        static final int STOP = 0x002;
        static final int RECORD = 0x003;
        private DWReplayPlayer player;
        private String recordTag = "";

        public RecordHandler(Looper looper, DWReplayPlayer player, String recordTag) {
            super(looper);
            this.player = player;
            this.recordTag = recordTag;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START: // 开始记录
                    sendEmptyMessage(RECORD);
                    sendEmptyMessageDelayed(START, 5000);
                    break;
                case RECORD:
                    // 记录播放进度
                    if (player != null && player.isPlaying()) {
                        //Log.i(TAG, "RecordHandler recordTag:" + recordTag + ",lastPosition:" + player.getCurrentPosition());
                        SPUtil.getInstance().put(RECORD_ID, recordTag);
                        SPUtil.getInstance().put(LAST_POSITION, player.getCurrentPosition());
                    }

                    break;

            }
        }
    }


}
