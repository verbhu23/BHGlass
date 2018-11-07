package com.biomhope.glass.face.home.master;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.bean.FaceSimilarResult;
import com.biomhope.glass.face.bean.GroupUserInfo;
import com.biomhope.glass.face.bean.UserInfo;
import com.biomhope.glass.face.utils.CommonUtil;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author:BH
 * create at:2018-10-11
 * description: 识别相似结果列表adapter
 */
public class FaceSimilarityAdapter extends RecyclerView.Adapter<FaceSimilarityAdapter.SimFaceHolder> {

    private List<UserInfo> simUsers;
    private Context context;
    private OnItemClickListener mListener;
    private final Typeface fromAssetTypeFace;

    public FaceSimilarityAdapter(List<UserInfo> simUsers, Context context, OnItemClickListener mListener) {
        this.simUsers = simUsers;
        this.context = context;
        this.mListener = mListener;
        String FONT_LCDD = "fonts" + File.separator + "Lcdd.ttf";
        fromAssetTypeFace = Typeface.createFromAsset(context.getAssets(), FONT_LCDD);
    }

    @Override
    public SimFaceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimFaceHolder(LayoutInflater.from(context).inflate(R.layout.item_face_similarity, parent, false));
    }

    @Override
    public void onBindViewHolder(SimFaceHolder holder, int position) {
        final UserInfo userInfo = simUsers.get(position);
        int simFormat = 0;
        if (!TextUtils.isEmpty(userInfo.sim)) {
            simFormat = Math.round(Float.valueOf(userInfo.sim));
        }
        CommonUtil.loadOnlinePic(context, userInfo.faceDefine, holder.iv_thumbnail_similarity);
        holder.face_result_item_name.setText(userInfo.name);
        holder.face_result_item_idcard.setText(userInfo.idCard);
        holder.tv_face_similarity.setTypeface(fromAssetTypeFace);
        holder.tv_face_similarity.setText(String.valueOf(simFormat) + "%");
        holder.pb_similarity_progress.setProgress(simFormat);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(userInfo);
                }
            }
        });

        if (GroupUserInfo.USER_TYPE_VIP.equals(userInfo.family)) {
            holder.similarity_face_root.setBackgroundResource(R.drawable.bg_face_sim_vip);
            holder.face_result_item_type.setTextColor(context.getResources().getColor(R.color.sim_face_vip_text));
            holder.face_result_item_type.setText("VIP");
            holder.face_result_item_type.setBackgroundResource(R.drawable.bg_face_sim_vip_txt);
        } else if (GroupUserInfo.USER_TYPE_COMMON.equals(userInfo.family)) {
            holder.similarity_face_root.setBackgroundResource(R.drawable.bg_face_sim_normal);
            holder.face_result_item_type.setText(context.getResources().getString(R.string.user_type_com));
            holder.face_result_item_type.setTextColor(context.getResources().getColor(R.color.sim_face_normal_text));
            holder.face_result_item_type.setBackgroundResource(R.drawable.bg_face_sim_normal_txt);
        } else if (GroupUserInfo.USER_TYPE_BLACK.equals(userInfo.family)) {
            holder.similarity_face_root.setBackgroundResource(R.drawable.bg_face_sim_normal);
            holder.face_result_item_type.setText(context.getResources().getString(R.string.user_type_black));
            holder.face_result_item_type.setTextColor(context.getResources().getColor(R.color.clr_black));
            holder.face_result_item_type.setBackgroundResource(R.drawable.bg_face_sim_black_text);

        }
    }

    public void notifyData(List<UserInfo> simUsers) {
        this.simUsers = simUsers;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return simUsers == null ? 0 : simUsers.size();
    }

    public interface OnItemClickListener {
        void onItemClick(UserInfo userInfo);
    }

    static class SimFaceHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.similarity_face_root)
        RelativeLayout similarity_face_root;

        @BindView(R.id.iv_thumbnail_similarity)
        ImageView iv_thumbnail_similarity;

        @BindView(R.id.face_result_item_name)
        TextView face_result_item_name;

        @BindView(R.id.face_result_item_idcard)
        TextView face_result_item_idcard;

        @BindView(R.id.face_result_item_type)
        TextView face_result_item_type; // 显示是否为VIP或者普通用户

        @BindView(R.id.tv_face_similarity)
        TextView tv_face_similarity;

        @BindView(R.id.pg_similarity_progress)
        ProgressBar pb_similarity_progress;

        SimFaceHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
