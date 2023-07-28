package com.bokecc.livemodule.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.bokecc.livemodule.R;

public abstract class RightBaseView extends BaseLinearLayout {

    protected RightBaseView(Context context) {
        super(context);
        shieldClick();
    }

    protected RightBaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        shieldClick();
    }

    protected RightBaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        shieldClick();
    }

    //屏蔽点击暗色区域不消失
    private void shieldClick() {
        View viewById = findViewById(R.id.root);
        if (viewById != null) {
            viewById.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 空实现，防止点击灰色区域隐藏右侧边栏

                }
            });
        }
    }
}
