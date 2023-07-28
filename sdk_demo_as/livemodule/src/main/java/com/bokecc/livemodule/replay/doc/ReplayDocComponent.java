package com.bokecc.livemodule.replay.doc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.bokecc.sdk.mobile.live.widget.DocView;

/**
 * 回放直播间文档展示控件
 */
public class ReplayDocComponent extends LinearLayout implements DocView.DocViewEventListener {

    private static final String TAG = "sdk_DocView";
    private Context mContext;
    private DocView mDocView;

    public ReplayDocComponent(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public ReplayDocComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        mDocView = new DocView(mContext);
        mDocView.setScrollable(false);
        mDocView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mDocView);

        DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
        if (replayCoreHandler != null) {
            replayCoreHandler.setDocView(mDocView);
        }

        mDocView.setDocViewListener(this);
    }

    // 设置文档是否可滑动
    public void setDocScrollable(boolean isDocScrollable) {
        if (mDocView != null) {
            mDocView.setScrollable(isDocScrollable);
        }
    }

    // 设置文档的拉伸模式
    public void setScaleType(int type) {
        if (DocView.ScaleType.CENTER_INSIDE.ordinal() == type) {
            mDocView.setDocScaleType(DocView.ScaleType.CENTER_INSIDE);
        } else if (DocView.ScaleType.FIT_XY.ordinal() == type) {
            mDocView.setDocScaleType(DocView.ScaleType.FIT_XY);
        } else if (DocView.ScaleType.CROP_CENTER.ordinal() == type) {
            mDocView.setDocScaleType(DocView.ScaleType.CROP_CENTER);
        }
    }

    public DocView getmDocView() {
        return mDocView;
    }
    @Override
    public void docLoadCompleteFailedWithIndex(int index) {
        switch (index) {

            case 0:
                ELog.i(TAG, "文档组件加载完成");
                break;
            case 3:
                ELog.i(TAG, "文档组件加载失败");
                break;

            case 1:
                //文档类型（静态文档、动态文档、白板）之 动态文档 翻页成功
                ELog.i(TAG, "动态文档翻页成功");
                break;

            case 5:
                //文档类型（静态文档、动态文档、白板）之 动态文档 翻页超时
                ELog.i(TAG, "动态文档翻页超时");
                break;


            case 2:
                //文档类型（静态文档、动态文档、白板）之 静态文档 翻页成功
                ELog.i(TAG, "非动画文档(白板 图片)文档加载完成");
                break;

            case 4:
                //文档类型（静态文档、动态文档、白板）之 静态文档 翻页失败
                ELog.i(TAG, "静态文档翻页失败");
                break;

            case 6:
                //文档类型（静态文档、动态文档、白板）之 白板 翻页失败
                ELog.i(TAG, "白板加载失败");
                break;

            case 9:
                //文档类型（静态文档、动态文档、白板）之 静态文档、动态文档 翻页超时回调状态码（包含index为5和10状态）
                ELog.i(TAG, "文档翻页超时");
                break;



            case 10:
                //文档类型（静态文档、动态文档、白板）之 静态文档 翻页超时回调状态码
                ELog.i(TAG, "静态文档翻页超时");
                break;
            case 11:
                //文档类型（静态文档、动态文档、白板）之 动态文档 step动画执行成功回调状态码
                ELog.i(TAG, "动态文档动画执行成功");
                break;
            case 12:
                //文档类型（静态文档、动态文档、白板）之 动态文档 step动画执行超时回调状态码
                ELog.i(TAG, "动态文档动画执行超时");
                break;

            case 13:
                //文档类型（静态文档、动态文档、白板）之 动态文档 文档加载成功回调状态码
                ELog.i(TAG, "动态文档加载成功");
                break;
            case 14:
                //文档类型（静态文档、动态文档、白板）之 动态文档 文档加载失败回调状态码
                ELog.i(TAG, "动态文档加载失败");
                break;


            default:

        }
    }
}
