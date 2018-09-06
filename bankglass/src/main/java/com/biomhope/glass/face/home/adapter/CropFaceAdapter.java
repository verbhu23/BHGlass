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
import android.widget.RatingBar;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.bean.FaceDecetListBean;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CropFaceAdapter extends RecyclerView.Adapter<CropFaceAdapter.CropFaceViewHolder> {

    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private List<FaceDecetListBean> faceDecetListBeans;
    private OnItemClickListener mListener;

    public CropFaceAdapter(Context context, List<FaceDecetListBean> faceDecetListBeans, OnItemClickListener mListener) {
        this.context = context;
        this.faceDecetListBeans = faceDecetListBeans;
        this.mListener = mListener;
    }

    @Override
    public CropFaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CropFaceViewHolder(LayoutInflater.from(context).inflate(R.layout.item_glass_detection, parent, false));
    }

    @Override
    public void onBindViewHolder(CropFaceViewHolder holder, final int position) {
        final FaceDecetListBean faceDecetListBean = faceDecetListBeans.get(position);
        Glide.with(context)
                .load("file://" + faceDecetListBean.imgCropPath)
                .transition(new DrawableTransitionOptions().crossFade(250))
                .apply(new RequestOptions().placeholder(R.drawable.pictures_no).error(R.drawable.remote_refresh))
                .into(holder.record_iv_thumbnail);
        holder.record_face_name.setText(faceDecetListBean.name);
        holder.tv_face_sim_size.setText(faceDecetListBean.sim + "%");
        holder.pg_sim_progress.setProgress(Integer.valueOf(faceDecetListBean.sim));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: click position = " + position);
                if (mListener != null) {
                    mListener.onItemClick(faceDecetListBean);
                }
            }
        });
        if ("vip".equals(faceDecetListBean.userType)) {
            holder.iv_user_type.setVisibility(View.VISIBLE);
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

    static class CropFaceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.record_iv_thumbnail)
        ImageView record_iv_thumbnail;

        @BindView(R.id.record_face_name)
        TextView record_face_name;

        @BindView(R.id.iv_user_type)
        ImageView iv_user_type;

        @BindView(R.id.tv_face_sim_size)
        TextView tv_face_sim_size;

        @BindView(R.id.pg_sim_progress)
        ProgressBar pg_sim_progress;


        public CropFaceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
