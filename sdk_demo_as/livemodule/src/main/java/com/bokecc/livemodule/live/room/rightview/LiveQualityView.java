package com.bokecc.livemodule.live.room.rightview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.utils.DensityUtil;
import com.bokecc.livemodule.view.ChangeQualityTextView;
import com.bokecc.livemodule.view.RightBaseView;
import com.bokecc.sdk.mobile.live.listener.LiveChangeSourceListener;
import com.bokecc.sdk.mobile.live.pojo.LiveQualityInfo;

import java.util.List;

/**
 * 清晰度列表View
 */
public class LiveQualityView extends RightBaseView {
    private final static String TAG = LiveQualityView.class.getSimpleName();

    private LinearLayout llQuality;
    private QualityCallBack qualityCallBack;

    /**
     * 选中的ItemView
     */
    private ChangeQualityTextView checkedView;

    public LiveQualityView(Context context) {
        super(context);
    }

    @Override
    public void initViews() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.right_quality_view, null);
        llQuality = inflate.findViewById(R.id.ll_quality);
        this.addView(inflate);
    }

    /**
     * 设置回调类
     *
     * @param qualityCallBack qualityCallBack
     */
    public void setQualityCallBack(QualityCallBack qualityCallBack) {
        this.qualityCallBack = qualityCallBack;
    }

    /**
     * 设置数据，展示清晰度列表
     */
    public void setData(List<LiveQualityInfo> videoQuality, LiveQualityInfo currentQuality) {
        if (videoQuality == null || currentQuality == null) {
            Log.e(TAG, "LiveQualityView setData param is null");
            return;
        }
        // 移除所有View
        llQuality.removeAllViews();
        int size = videoQuality.size();
        for (int i = 0; i < size; i++) {
            LiveQualityInfo liveLineParams = videoQuality.get(i);
            ChangeQualityTextView qualityTextView = new ChangeQualityTextView(getContext(),
                    liveLineParams.getQuality(), liveLineParams.getDesc());
            // 默认显示第一条数据：原画
            if (checkedView == null) {
                if (i == 0) {
                    qualityTextView.setChecked(true);
                    checkedView = qualityTextView;
                }
            } else {
                if (checkedView.getQuality() == liveLineParams.getQuality()) {
                    qualityTextView.setChecked(true);
                }
            }
            LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(getContext(), 80));
            params.weight = 1;
            llQuality.addView(qualityTextView, params);

            // 设置Item点击监听
            qualityTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleItemClick(v);

                }
            });
        }

    }

    // 处理每项的点击事件
    private void handleItemClick(View v) {
        final ChangeQualityTextView qualityTextView = (ChangeQualityTextView) v;
        if (qualityTextView.isChecked()) {
            return;
        }
        setCheckView(qualityTextView);
        DWLiveCoreHandler instance = DWLiveCoreHandler.getInstance();
        if (instance != null) {
            instance.changeQuality(qualityTextView.getQuality(), new LiveChangeSourceListener() {
                @Override
                public void onChange(int success) {
                    if (success == 0) {
                        checkedView = qualityTextView;
                        if (qualityCallBack != null) {
                            qualityCallBack.qualityChange(checkedView.getQuality(), qualityTextView.getDesp());
                        }
                    } else {
                        setCheckView(checkedView);
                        sendErrorToast(success);
                    }
                }
            });
        }


    }

    // 设置默认选项
    private void setCheckView(ChangeQualityTextView qualityTextView) {
        for (int i = 0; i < llQuality.getChildCount(); i++) {
            ChangeQualityTextView childAt = (ChangeQualityTextView) llQuality.getChildAt(i);
            childAt.setChecked(childAt.getQuality() == qualityTextView.getQuality());
        }
    }

    public void setQuality(int quality) {
        for (int i = 0; i < llQuality.getChildCount(); i++) {
            ChangeQualityTextView liveLineParams = (ChangeQualityTextView) llQuality.getChildAt(i);
            if (liveLineParams.getQuality() == quality) {
                checkedView = liveLineParams;
                liveLineParams.setChecked(true);
            } else {
                liveLineParams.setChecked(false);
            }
        }
    }

    // 弹出错误信息
    private void sendErrorToast(int errorCode) {
        if (errorCode == LiveChangeSourceListener.INTERVAL) {
            toastOnUiThread("您切换的太频繁了");
        } else if (errorCode == LiveChangeSourceListener.FAIL) {
            toastOnUiThread("切换失败");
        }
    }


    /**
     * 切换成功回调
     */
    public interface QualityCallBack {
        void qualityChange(int quality, String qualityDesc);
    }

}
