package com.biomhope.glass.face.home.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.bean.FaceDecetListBean;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VipUserAdapter extends RecyclerView.Adapter<VipUserAdapter.VipListViewHolder> {

    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private List<FaceDecetListBean> faceDecetListBeans;
    private OnItemClickListener mListener;

    public VipUserAdapter(Context context, List<FaceDecetListBean> faceDecetListBeans, OnItemClickListener mListener) {
        this.context = context;
        this.faceDecetListBeans = faceDecetListBeans;
        this.mListener = mListener;
    }

    @Override
    public VipListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VipListViewHolder(LayoutInflater.from(context).inflate(R.layout.item_vip_level, parent, false));
    }

    @Override
    public void onBindViewHolder(VipListViewHolder holder, final int position) {
        final FaceDecetListBean faceDecetListBean = faceDecetListBeans.get(position);
        Glide.with(context)
                .load("file://" + faceDecetListBean.imgCropPath)
                .transition(new DrawableTransitionOptions().crossFade(500))
                .apply(new RequestOptions().bitmapTransform(new CircleCrop()).placeholder(R.drawable.userimg).error(R.drawable.remote_refresh))
                .into(holder.record_iv_thumbnail);
        holder.tv_vip_name.setText(faceDecetListBean.name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: click position = " + position);
                if (mListener != null) {
                    mListener.onItemClick(faceDecetListBean);
                }
            }
        });

        // VIP LIST
        if ("vip".equals(faceDecetListBean.userType)) {
            switch (faceDecetListBean.level) {
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
            holder.tv_vip_level.setText("VIP" + faceDecetListBean.level);
            holder.rat_vip_level.setRating(Float.valueOf(faceDecetListBean.level));
        }

    }

    @Override
    public int getItemCount() {
        return faceDecetListBeans == null ? 0 : faceDecetListBeans.size();
    }

    public void insertHeaderData(FaceDecetListBean faceDecetListBean) {
        if (faceDecetListBeans != null) {
            faceDecetListBeans.add(0, faceDecetListBean);
            notifyItemInserted(0);
            // 通知数据与界面重新绑定
            notifyItemRangeChanged(0, faceDecetListBeans.size() - 0);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(FaceDecetListBean faceDecetListBean);
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
