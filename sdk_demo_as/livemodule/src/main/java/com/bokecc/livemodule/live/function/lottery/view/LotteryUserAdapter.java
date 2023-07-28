package com.bokecc.livemodule.live.function.lottery.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.view.HeadView;
import com.bokecc.sdk.mobile.live.pojo.LotteryUserInfo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class LotteryUserAdapter extends RecyclerView.Adapter<LotteryUserAdapter.UserHolder>  {
    private final LayoutInflater mInflater;
    private List<LotteryUserInfo> users;
    private final Context mContext;

    public LotteryUserAdapter(Context context, List<LotteryUserInfo> users) {
        this.users = users;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setUsers(List<LotteryUserInfo> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new UserHolder(mInflater.inflate(R.layout.item_lottery_user, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder userHolder, int i) {
        Glide.with(mContext)
                .load(users.get(i).getUserAvatar())
                .asBitmap()
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                //占位图
                .placeholder(R.drawable.head_view_publisher)
                //错误图
                .error(R.drawable.head_view_publisher).
                into(userHolder.mUserHeadView);
        if (TextUtils.isEmpty(users.get(i).getUserName())){
            userHolder.userName.setText("未知");
        }else{
            userHolder.userName.setText(users.get(i).getUserName());
        }
    }

    @Override
    public int getItemCount() {
        return users == null?0:users.size();
    }

    public class UserHolder extends RecyclerView.ViewHolder {
        public TextView userName ;
        public HeadView mUserHeadView;
        public UserHolder(@NonNull View itemView) {
            super(itemView);
            mUserHeadView = itemView.findViewById(R.id.hv_user_portrait);
            userName = itemView.findViewById(R.id.tv_username);
        }
    }
}
