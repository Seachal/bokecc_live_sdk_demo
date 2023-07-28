package com.bokecc.dwlivedemo.popup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bokecc.dwlivedemo.R;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 悬浮弹出框（支持拖动）
 */
public class FloatingPopupWindow implements View.OnClickListener {
    private final Activity mContext;
    private final RelativeLayout mFloatingLayout;
    private View mNowView;              // 当前容器里面装载的View
    private final ImageView ivClose;    // 删除按钮

    private float lastX;
    private float lastY;
    private final AtomicBoolean isShow = new AtomicBoolean(false);
    private final Handler handler = new Handler(Looper.getMainLooper());

    // 取消小窗显示监听
    private FloatDismissListener floatDismissListener;

    /**
     * 构造方法
     *
     * @param context Activity
     */
    public FloatingPopupWindow(Activity context) {
        mContext = context;
        mFloatingLayout = new RelativeLayout(mContext);
        mFloatingLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.text_primary));
        ivClose = new ImageView(mContext);
        ivClose.setImageResource(R.drawable.live_screen_close);
    }

    /**
     * 设置取消小窗的监听
     *
     * @param floatDismissListener FloatDismissListener
     */
    public void setFloatDismissListener(FloatDismissListener floatDismissListener) {
        this.floatDismissListener = floatDismissListener;
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (floatDismissListener != null) {
            floatDismissListener.dismiss();
        }
    }

    // region -------------------- 对外提供方法-----------------------------------------------------

    /**
     * 添加新View
     */
    public void addView(View view) {
        mNowView = view;
        if (mFloatingLayout != null) {
            mFloatingLayout.addView(view, 0);
        }
    }

    /**
     * 移除包裹的布局
     */
    public void removeNowView() {
        if (mFloatingLayout != null) {
            mFloatingLayout.removeView(mNowView);
        }
    }

    /**
     * 移除所有子View，已做兼容
     */
    public void removeAllView() {
        // 兼容处理，防止顶部按钮也移除掉
        removeNowView();
    }

    /**
     * 显示弹出框
     */
    @SuppressLint("ClickableViewAccessibility")
    public void show(View view) {
        if (isShowing()) {
            return;
        }
        isShow.set(true);
        ((ViewGroup) mContext.getWindow().getDecorView().getRootView()).removeView(mFloatingLayout);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(dp2px(mContext, 120), dp2px(mContext, 90));
        ((ViewGroup) mContext.getWindow().getDecorView().getRootView()).addView(mFloatingLayout, layoutParams);
        mFloatingLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - lastX);
                        int deltaY = (int) (event.getRawY() - lastY);
                        float transX = mFloatingLayout.getX() + deltaX;
                        float transY = mFloatingLayout.getY() + deltaY;
                        //判断是否超过屏幕外
                        if (transX < 0) {
                            transX = 0;
                        }
                        if (transX > (getDeviceScreenWidth(mContext) - mFloatingLayout.getWidth())) {
                            transX = (getDeviceScreenWidth(mContext) - mFloatingLayout.getWidth());
                        }
                        if (transY < getStatusBarHeight(mContext)) {
                            transY = getStatusBarHeight(mContext);
                        }
                        if (transY > (getDeviceScreenHeight(mContext) - mFloatingLayout.getHeight())) {
                            transY = (getDeviceScreenHeight(mContext) - mFloatingLayout.getHeight());
                        }
                        mFloatingLayout.setTranslationX(transX);
                        mFloatingLayout.setTranslationY(transY);
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        if (ivClose.getVisibility() == View.GONE) {
                            ivClose.setVisibility(View.VISIBLE);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.postDelayed(runnable, 3000);
                        break;
                }
                return true;
            }
        });
        // 添加删除按钮
        mFloatingLayout.removeView(ivClose);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dp2px(mContext, 20), dp2px(mContext, 20));
        lp.addRule(RelativeLayout.ALIGN_PARENT_END, 1);
        mFloatingLayout.addView(ivClose, lp);
        ivClose.setVisibility(View.GONE);
        ivClose.setOnClickListener(this);
        //初始化位置
        Configuration mConfiguration = mContext.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscapeTranslation();
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitTranslation();
        }
    }

    /**
     * 隐藏弹出框
     */
    public void dismiss() {
        handler.removeCallbacks(runnable);
        if (mFloatingLayout != null) {
            if (isShowing()) {
                try {
                    ((ViewGroup) mContext.getWindow().getDecorView().getRootView()).removeView(mFloatingLayout);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isShow.set(false);
            }
        }
    }

    /**
     * 横竖屏切换  调整连麦窗口的位置
     *
     * @param orientation 屏幕方向 1:竖屏
     *                    {@link Configuration}
     */
    public void onConfigurationChanged(int orientation) {
        if (!isShowing()) return;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscapeTranslation();
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitTranslation();
        }
    }

    /**
     * 获取当前正展示的View
     */
    public View getNowView() {
        return mNowView;
    }

    /**
     * 是否显示
     */
    public boolean isShowing() {
        return isShow.get();
    }

    //endregion

    // region -------------------- 私有方法---------------------------------------------------------

    // 设置横屏初始化位置
    private void setLandscapeTranslation() {
        if (null != mFloatingLayout) {
            mFloatingLayout.setTranslationX(2);
            mFloatingLayout.setTranslationY(dp2px(mContext, 42));
        }

    }

    // 设置竖屏初始化位置
    private void setPortraitTranslation() {
        if (mFloatingLayout != null) {
            mFloatingLayout.setTranslationX(getDeviceScreenWidth(mContext) - dp2px(mContext, 121));
//            float y = (getDeviceScreenHeight(mContext));
            float y = (getDeviceScreenHeight(mContext)) / 3f
                    + getStatusBarHeight(mContext)
                    + dp2px(mContext, 42);
            if (y < 0) {
                y = 0;
            }
            if (y > (getDeviceScreenHeight(mContext) - mFloatingLayout.getHeight())) {
                y = (getDeviceScreenHeight(mContext) - mFloatingLayout.getHeight());
            }
            mFloatingLayout.setTranslationY(y);
        }
    }

    // dp-->px
    private int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }


    // 获取设备屏幕可见高度
    private int getDeviceScreenHeight(Activity activity) {
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        return dm.heightPixels;

    }


    // 获取设备屏幕实际高度
    private int getDeviceRealHeight(Activity activity) {

        // 获取可见高度
//        Rect rect = new Rect();
//        mContext.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);


        // 获取实际高度
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        return dm.heightPixels;


    }

    // 获取设备屏幕宽度
    public static int getDeviceScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    // 获取状态栏高度
    private int getStatusBarHeight(Activity activity) {
        int height = 0;
        try {
            int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                height = activity.getResources().getDimensionPixelSize(resourceId);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return height;
    }


    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (ivClose != null) {
                ivClose.setVisibility(View.GONE);
            }

        }
    };
    //endregion

    public interface FloatDismissListener {
        void dismiss();
    }

}
