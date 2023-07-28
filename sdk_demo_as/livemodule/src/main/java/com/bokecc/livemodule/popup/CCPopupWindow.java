package com.bokecc.livemodule.popup;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class CCPopupWindow extends PopupWindow {

    protected Activity activity;

    public CCPopupWindow(Context context) {
        super(context);
    }

    public CCPopupWindow(View mPopContentView, int matchParent, int wrapContent) {
        super(mPopContentView,matchParent,wrapContent);
    }

    @Override
    public void dismiss() {
        if (activity != null && !activity.isFinishing()) {
            Window dialogWindow = activity.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.alpha = 1.0f;
            dialogWindow.setAttributes(lp);
        }
        super.dismiss();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
