package com.bokecc.livemodule.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.sdk.mobile.live.pojo.LotteryCollectTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LotteryMessageItemView extends LinearLayout {

    private TextView key;
    private EditText value;
    private LotteryCollectTemplate lotteryCollectTemplate;

    public LotteryMessageItemView(Context context) {
        super(context);
        init(context);
    }

    public LotteryMessageItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LotteryMessageItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_lottery_message, this);
        key = inflate.findViewById(R.id.tv_lottery_message_key);
        value = inflate.findViewById(R.id.et_lottery_message_value);
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void setContent(LotteryCollectTemplate lotteryCollectTemplate) {
        this.lotteryCollectTemplate = lotteryCollectTemplate;
        if (lotteryCollectTemplate != null) {
            if (!TextUtils.isEmpty(lotteryCollectTemplate.getTitle())) {
                key.setText(lotteryCollectTemplate.getTitle());
                if (key.getText().equals("手机号")) {
                    value.setInputType(InputType.TYPE_CLASS_NUMBER); //输入类型
                    value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)}); //最大输入长度
                }
            }
            if (!TextUtils.isEmpty(lotteryCollectTemplate.getTips())) {
                value.setHint(lotteryCollectTemplate.getTips());
            }
        }
    }

    String regEx = "^(\\d{11})$";

    /**
     * @return 0 是没有问题
     * 1 手机号输入不正确
     * 2 字段为空了
     */
    public int checkContent() {
        if (TextUtils.isEmpty(value.getText().toString())) {
            return 2;
        } else if (key.getText().equals("手机号")) {
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(value.getText().toString());
            boolean rs = matcher.matches();
            if (rs) {
                return 0;
            } else {
                return 1;
            }
        }
        return 0;
    }

    public LotteryCollectTemplate getLotteryCollectTemplate() {
        return lotteryCollectTemplate;
    }

    public String getContent() {
        return value.getText().toString();
    }

    public void setEdittextListenr(TextWatcher textWatcher) {
        value.addTextChangedListener(textWatcher);
    }
}
