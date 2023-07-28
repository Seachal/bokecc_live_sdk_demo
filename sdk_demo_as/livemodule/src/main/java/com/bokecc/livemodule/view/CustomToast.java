package com.bokecc.livemodule.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.livemodule.R;

/**
 * 自定义Toast
 * 防止重复弹出
 * Created by dds on 2021/1/18.
 */
public class CustomToast {

    /**
     * 自定义防止重复弹出Toast
     */
    public static void showToast(Context context, CharSequence text, int duration) {
        LayoutInflater inflate = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflate.inflate(R.layout.tips_status_view, null);
        TextView tv = view.findViewById(R.id.tips_status_text);
        tv.setText(text);
        Toast toast = new Toast(context.getApplicationContext());
        toast.setView(view);
        toast.setDuration(duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }


}
