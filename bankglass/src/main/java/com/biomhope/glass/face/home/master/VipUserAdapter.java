package com.biomhope.glass.face.home.master;

import android.content.Context;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.bean.GroupUserInfo;
import com.biomhope.glass.face.bean.UserInfo;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VipUserAdapter extends RecyclerView.Adapter<VipUserAdapter.VipListViewHolder> {

    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private List<UserInfo> userInfos;
    private OnItemClickListener mListener;

    public VipUserAdapter(Context context, List<UserInfo> userInfos, OnItemClickListener mListener) {
        this.context = context;
        this.userInfos = userInfos;
        this.mListener = mListener;
    }

    @Override
    public VipListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VipListViewHolder(LayoutInflater.from(context).inflate(R.layout.item_vip_level, parent, false));
    }

    @Override
    public void onBindViewHolder(VipListViewHolder holder, final int position) {
        final UserInfo userInfo = userInfos.get(position);

        CommonUtil.loadCircleBitmap(context, "file://" + userInfo.imgCropPath, holder.record_iv_thumbnail);
        holder.tv_vip_name.setText(userInfo.name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i(TAG, "onClick: click position = " + position);
                if (mListener != null) {
                    mListener.onItemClick(userInfo);
                }
            }
        });

        // VIP LIST
        if (GroupUserInfo.USER_TYPE_VIP.equals(userInfo.family)) {
            switch (userInfo.viplevel) {
                case "1":
                case "2":
                case "3":
                    holder.tv_vip_level.setTextColor(context.getResources().getColor(R.color.vip_level_normal));
                    break;
                case "4":
                case "5":
                    holder.tv_vip_level.setTextColor(context.getResources().getColor(R.color.vip_level_height));
                    break;
            }
            holder.tv_vip_level.setText("VIP" + userInfo.viplevel);
            holder.rat_vip_level.setRating(Float.valueOf(userInfo.viplevel));
        }

    }

    @Override
    public int getItemCount() {
        return userInfos == null ? 0 : userInfos.size();
    }

    public void insertHeaderData(UserInfo userInfo) {
        if (userInfos != null) {
            userInfos.add(0, userInfo);
            notifyItemInserted(0);
            // 通知数据与界面重新绑定
            notifyItemRangeChanged(0, userInfos.size() - 0);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(UserInfo userInfo);
    }

    static class VipListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.record_iv_thumbnail)
        ImageView record_iv_thumbnail;

        @BindView(R.id.tv_vip_name)
        TextView tv_vip_name;

        @BindView(R.id.tv_vip_level)
        TextView tv_vip_level;

        @BindView(R.id.rat_vip_level)
        AppCompatRatingBar rat_vip_level;

        public VipListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
