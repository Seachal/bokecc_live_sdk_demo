package com.bokecc.livemodule.live.morefunction;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;

public class RemindItem extends LinearLayout {

    private TextView mTvRemind;
    private ImageView mclose;
    private RemindType type;
    private RemindListener remindListener;
    public enum RemindType{
        PRIVATE_CHAT,//私聊
        ANNOUNCE//公告
    }
    public RemindItem(Context context) {
        super(context);
        initView(context);
    }

    public RemindItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RemindItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.remind_item_view, this);
        mTvRemind = findViewById(R.id.tv_remind);
        mclose = findViewById(R.id.iv_remind_close);
    }
    public void setContent(RemindType type){
        this.type=type;
        if (type == RemindType.PRIVATE_CHAT){
            mTvRemind.setText("您有新私聊");
        }else if (type == RemindType.ANNOUNCE){
            mTvRemind.setText("您有新公告");
        }
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remindListener!=null){
                    remindListener.onClickListener(RemindItem.this, RemindItem.this.type);
                }
            }
        });
        mclose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remindListener!=null){
                    remindListener.onClose(RemindItem.this);
                }
            }
        });
    }

    public void setRemindListener(RemindListener remindListener) {
        this.remindListener = remindListener;
    }

    public RemindType getType() {
        return type;
    }

    public interface RemindListener{
        void onClickListener(RemindItem remindItem, RemindType type);
        void onClose(RemindItem remindItem);
    }
}
