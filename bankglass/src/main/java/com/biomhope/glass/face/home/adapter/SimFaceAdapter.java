package com.biomhope.glass.face.home.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.bean.SearchPersonsResult;
import com.biomhope.glass.face.bean.SearchPersonsResultTemp;
import com.biomhope.glass.face.global.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SimFaceAdapter extends RecyclerView.Adapter<SimFaceAdapter.SimFaceHolder> {

    private List<SearchPersonsResultTemp.UserInfo> simUsers;
    private Context context;
    private OnItemClickListener mListener;

    public SimFaceAdapter(List<SearchPersonsResultTemp.UserInfo> simUsers, Context context, OnItemClickListener mListener) {
        this.simUsers = simUsers;
        this.context = context;
        this.mListener = mListener;
    }

    @Override
    public SimFaceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimFaceHolder(LayoutInflater.from(context).inflate(R.layout.item_face_sim, parent, false));
    }

    @Override
    public void onBindViewHolder(SimFaceHolder holder, int position) {
        final SearchPersonsResultTemp.UserInfo userInfo = simUsers.get(position);
        int simFormat = Math.round(userInfo.sim);
        final String imgUrl = Constants.REQUEST_IMAGE_URL + userInfo.faceId + File.separator + userInfo.userId;
        Glide.with(context)
                .load(imgUrl)
                .transition(new DrawableTransitionOptions().crossFade(250))
                .apply(new RequestOptions().bitmapTransform(new CircleCrop()).error(R.drawable.remote_refresh))
                .into(holder.iv_thumbnail_sim);

        holder.tv_face_sim.setText(String.valueOf(simFormat) + "%");
        holder.pb_sim_progress.setProgress(simFormat);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(imgUrl);
                }
            }
        });
    }

    public void notifyData(List<SearchPersonsResultTemp.UserInfo> simUsers) {
        this.simUsers = simUsers;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return simUsers == null ? 0 : simUsers.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String imgUrl);
    }

    static class SimFaceHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_thumbnail_sim)
        ImageView iv_thumbnail_sim;

        @BindView(R.id.tv_face_sim)
        TextView tv_face_sim;

        @BindView(R.id.pb_sim_progress)
        ProgressBar pb_sim_progress;

        public SimFaceHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
