package com.bokecc.livemodule.live.room.rightview;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.utils.DensityUtil;
import com.bokecc.livemodule.view.ChangeQualityTextView;
import com.bokecc.livemodule.view.RightBaseView;
import com.bokecc.sdk.mobile.live.listener.LiveChangeSourceListener;
import com.bokecc.sdk.mobile.live.pojo.LiveQualityInfo;

import java.util.List;

/**
 * 弹幕设置View
 */
public class LiveBarrageView extends RightBaseView {
    private final static String TAG = LiveBarrageView.class.getSimpleName();
    public static final int BARRAGE_FULL = 0;
    public static final int BARRAGE_HALF = 1;
    private int type = BARRAGE_HALF;
    private LinearLayout mBarrageFullRoot,mBarrageHalfRoot;
    private ImageView mIvBarrageFull,mIvBarrageHalf;
    private TextView mTvBarrageFull,mTvBarrageHalf;
    private BarrageCallBack barrageCallBack;


    public LiveBarrageView(Context context) {
        super(context);
    }

    @Override
    public void initViews() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.right_barrage_view, null);
        mBarrageFullRoot = inflate.findViewById(R.id.barrage_full_root);
        mBarrageHalfRoot = inflate.findViewById(R.id.barrage_half_root);

        mIvBarrageFull = inflate.findViewById(R.id.iv_barrage_full);
        mIvBarrageHalf = inflate.findViewById(R.id.iv_barrage_half);

        mTvBarrageFull = inflate.findViewById(R.id.tv_barrage_full);
        mTvBarrageHalf = inflate.findViewById(R.id.tv_barrage_half);

        mBarrageFullRoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setBarrageType(BARRAGE_FULL);
            }
        });
        mBarrageHalfRoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setBarrageType(BARRAGE_HALF);
            }
        });
        this.addView(inflate);
    }

    /**
     * 设置回调类
     *
     * @param barrageCallBack barrageCallBack
     */
    public void setBarrageCallBack(BarrageCallBack barrageCallBack) {
        this.barrageCallBack = barrageCallBack;
    }

    /**
     * 设置数据，展示对应的状态
     */
    public void setBarrageType(int type) {
        if (this.type == type) {
            Log.e(TAG, "LiveBarrageView setData type is same");
            return;
        }
        this.type = type;
        if (type == BARRAGE_FULL){
            mIvBarrageFull.setImageResource(R.drawable.barrage_full_check);
            mTvBarrageFull.setTextColor(Color.parseColor("#F89E0F"));
            mIvBarrageHalf.setImageResource(R.drawable.barrage_half_nor);
            mTvBarrageHalf.setTextColor(Color.WHITE);
        }else if (type == BARRAGE_HALF){
            mIvBarrageFull.setImageResource(R.drawable.barrage_full_nor);
            mTvBarrageFull.setTextColor(Color.WHITE);
            mIvBarrageHalf.setImageResource(R.drawable.barrage_half_check);
            mTvBarrageHalf.setTextColor(Color.parseColor("#F89E0F"));
        }
        if (barrageCallBack!=null){
            barrageCallBack.barrageChange(type);
        }
    }

    /**
     * 切换成功回调
     */
    public interface BarrageCallBack {
        void barrageChange(int type);
    }

}
