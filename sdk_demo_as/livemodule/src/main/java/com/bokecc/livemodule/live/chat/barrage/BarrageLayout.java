package com.bokecc.livemodule.live.chat.barrage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bokecc.livemodule.live.chat.util.DensityUtil;
import com.bokecc.livemodule.live.chat.util.EmojiUtil;
import com.bokecc.sdk.mobile.live.pojo.ChatMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 弹幕布局类
 *
 * @author liufh
 */
public class BarrageLayout extends RelativeLayout {

    private List<ChatMessage> infos = new ArrayList<>(); // 存储收到的弹幕信息

    private boolean isStart = false;

    private long duration = 7000L;

    private Context context;

    private int width; //屏幕宽度
    private int height;//需要展示的高度

    private List<BarrageParentView> bvs = new ArrayList<>();

    private Timer timer = new Timer();

    private TimerTask timerTask;

    private Long barrageInterval = 2000L;

    private int maxBrragePerShow = 4;
    private int space = 30;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (infos.size() > maxBrragePerShow) {
                insertBarrageView(maxBrragePerShow);
            } else if (infos.size() > 0) {
                insertBarrageView(infos.size());
            }

            Iterator<BarrageParentView> iterator = bvs.iterator();
            while (iterator.hasNext()) {
                BarrageParentView bv = iterator.next();
                if (bv.getFinish()) {
                    iterator.remove();
                    BarrageLayout.this.removeView(bv);
                }
            }
        }
    };

    private int offset = 100;
    private int barrageHeight;

    private void insertBarrageView(int size) {

        Iterator<ChatMessage> iterator = infos.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            addBarrageView((float) (barrageHeight+space)*i + offset, duration,  iterator.next());
            iterator.remove();
            if (i == size - 1) {
                break;
            }
            i++;
        }
    }

    @SuppressLint("NewApi")
    private void addBarrageView(float heightPosition, long duration, ChatMessage chatMessage) {
        BarrageParentView bv = new BarrageParentView(context);
        bv.setRole(chatMessage.getUserRole(),chatMessage.getUserId());
        bv.setText(EmojiUtil.parseFaceMsg(context, new SpannableString(chatMessage.getMessage())));
        bv.setTextSize(14);
        bv.setShadowLayer(1.0f, 1.0f, 1.0f, Color.argb(147, 0, 0, 0)); // 设置字体阴影
        bv.setTextColor(Color.WHITE);
        bv.setSingleLine(true);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Rect bounds = new Rect();
        TextPaint paint = bv.getPaint();
        paint.getTextBounds(chatMessage.getMessage(), 0, chatMessage.getMessage().length(), bounds);

        int paintWidth = bounds.width();
        int marginLeft = chatMessage.getUserRole().equals("student")?paintWidth * -1:paintWidth * -1-DensityUtil.dp2px(context,38);
        lp.setMargins(marginLeft, 0, 0, 0);
        bv.setLayoutParams(lp);

        bvs.add(bv);
        this.addView(bv);
        bv.move(width + paintWidth+DensityUtil.dp2px(context,30), width * -1, heightPosition, heightPosition, duration);
    }

    public BarrageLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    public BarrageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public BarrageLayout(Context context) {
        super(context);
        this.context = context;
        init();
    }

    /**
     * 初始化，注册监听获取布局的宽和高
     */
    public void init() {
        space = com.bokecc.livemodule.utils.DensityUtil.dp2px(getContext(),16 );
        width = getDeviceScreenHeight(context);
        //距离顶部的偏移量
        offset = DensityUtil.dp2px(context, 42);
        barrageHeight = com.bokecc.livemodule.utils.DensityUtil.dp2px(getContext(), 22);
    }

    /**
     * 设置弹幕显示的高度 和偏移量
     * @param height  弹幕显示的高度
     * @param topOffSet  距离顶部的偏移量
     * @param bottomOffSet  距离底部的偏移量
     */
    public void setHeight(int height,int topOffSet,int bottomOffSet){
        this.height = height;
        offset = topOffSet;
        //总的可展示高度 除以  弹幕的高度和间距
        maxBrragePerShow = (height-offset-bottomOffSet+space)/(barrageHeight+space);
    }
    /**
     * 添加新信息
     */
    public void addNewInfo(ChatMessage info) {
        if (isStart) {
            if (height != 0 && width != 0) {
                infos.add(info);
            }
        }
    }

    /**
     * 弹幕开始
     */
    public void start() {

        if (isStart) {
            return;
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };

        timer.schedule(timerTask, 0, barrageInterval);
        this.setVisibility(View.VISIBLE);
        isStart = true;
    }

    /**
     * 停止弹幕
     */
    public void stop() {
        isStart = false;
        if (timerTask != null) {
            timerTask.cancel();
        }
        infos.clear();

    }

    /**
     * 设置屏幕刷新弹幕的间隔，最小2s
     *
     * @param interval 毫秒
     */
    public void setInterval(long interval) {
        if (interval > 2000) {
            this.barrageInterval = interval;
        }
    }

    /**
     * 设置弹幕在屏幕的动画时间
     *
     * @param duration
     */
    public void setBarrageDuration(long duration) {
        this.duration = duration;
    }

    /**
     * 设置每次获取的最大的弹幕个数
     *
     * @param maxBarragePerShow
     */
    public void setMaxBarragePerShow(int maxBarragePerShow) {
        if (maxBarragePerShow > 0) {
            this.maxBrragePerShow = maxBarragePerShow;
        }

    }

    // 获取设备屏幕高度
    private int getDeviceScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

}
