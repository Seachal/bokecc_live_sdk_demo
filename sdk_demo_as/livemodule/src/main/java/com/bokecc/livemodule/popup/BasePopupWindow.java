package com.bokecc.livemodule.popup;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.PopupWindowCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bokecc.livemodule.view.CustomToast;

public abstract class BasePopupWindow {
    protected Context mContext;

    protected CCPopupWindow mPopupWindow;

    protected View mPopContentView;

    /**
     * @param context 上下文
     */
    public BasePopupWindow(Context context) {
        this(context, 0, 0);
    }
    /**
     * @param context 上下文
     * @param width   可见区域的宽度 单位dp
     * @param height  可见区域的高度
     */
    public BasePopupWindow(Context context, int width, int height) {
        mContext = context;
        mPopContentView = LayoutInflater.from(mContext).inflate(getContentView(), null);
        mPopupWindow = new CCPopupWindow(mPopContentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        init();
    }
    public <T extends View> T findviewById(@IdRes int id){
        return mPopContentView.findViewById(id);
    }
    // 拦截返回键
    protected void setFocusableInTouchMode(boolean focusable){
        mPopContentView.setFocusableInTouchMode(focusable);
    }
    // 点击空白区域
    protected void setOutsideTouchable(boolean outsideTouchable){
        mPopupWindow.setOutsideTouchable(outsideTouchable);
    }
    //设置动画
    protected void setAnimationStyle(int style){
        mPopupWindow.setAnimationStyle(style);
    }
    //设置焦点
    protected void setFocusable(boolean focusable){
        mPopContentView.setFocusable(focusable);
    }
    //展示
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void show(Activity activity,View view, int gravity, int x, int y) {
        if (mPopupWindow.isShowing()) {
            return;
        }
        if (activity != null && !activity.isFinishing()) {
            Window dialogWindow = activity.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.alpha = 0.7f;
            dialogWindow.setAttributes(lp);
        }
        mPopupWindow.showAtLocation(view, gravity, x, y);
    }

    /**
     * 隐藏弹出框
     */
    public void dismiss() {
        if (mPopupWindow != null) {
            if (mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
        }
    }
    // 在Ui线程上进行吐司提示
    public void toastOnUiThread(final String msg) {
        // 判断是否处在UI线程
        if (checkOnMainThread()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    showToast(msg);
                }
            });
        } else {
            showToast(msg);
        }
    }
    // 进行吐司提示
    private void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        CustomToast.showToast(mContext, msg, Toast.LENGTH_SHORT);
    }
    // 判断当前的线程是否是UI线程
    protected boolean checkOnMainThread() {
        return Looper.myLooper() != Looper.getMainLooper();
    }
    public abstract void init();
    public abstract @LayoutRes int getContentView();

    public void setActivity(Activity activity) {
        mPopupWindow.setActivity(activity);
    }
}
