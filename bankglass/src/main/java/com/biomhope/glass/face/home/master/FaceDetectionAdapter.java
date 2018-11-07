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
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.bean.FaceRecogListVO;
import com.biomhope.glass.face.bean.GroupUserInfo;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主列表人脸检测adapter
 */
public class FaceDetectionAdapter extends RecyclerView.Adapter<FaceDetectionAdapter.FaceDetectionViewHolder> {

    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private List<FaceRecogListVO> userInfos;
    private OnItemClickListener mListener;
    private final Typeface fromAssetTypeFace;

    public FaceDetectionAdapter(Context context, List<FaceRecogListVO> userInfos, OnItemClickListener mListener) {
        this.context = context;
        this.userInfos = userInfos;
        this.mListener = mListener;
        String FONT_LCDD = "fonts" + File.separator + "Lcdd.ttf";
        fromAssetTypeFace = Typeface.createFromAsset(context.getAssets(), FONT_LCDD);
    }

    @Override
    public FaceDetectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FaceDetectionViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recognition_result, parent, false));
    }

    @Override
    public void onBindViewHolder(FaceDetectionViewHolder holder, final int position) {
        final FaceRecogListVO userInfo = userInfos.get(position);
        LogUtil.i(TAG, "onBindViewHolder position = " + position + " ,userInfo = " + userInfo.toString());
        if (userInfo.isServerPic) {
            // 来自服务器的图片需要进行手动缓存
            CommonUtil.loadOnlinePic(context, userInfo.img1, holder.record_iv_thumbnail);
        } else {
            CommonUtil.loadLocalPic(context, userInfo.imgCropPath, holder.record_iv_thumbnail);
        }
        if (TextUtils.isEmpty(userInfo.clientName)) {
            holder.recognition_result_name.setText(context.getResources().getString(R.string.user_name_not_match));
        } else {
            holder.recognition_result_name.setText(userInfo.clientName);
        }
        if (TextUtils.isEmpty(userInfo.idCard)) {
            holder.recognition_result_id_card.setText(context.getResources().getString(R.string.user_idcard_not_match));
        } else {
            holder.recognition_result_id_card.setText(userInfo.idCard);
        }
        if (!"0".equals(userInfo.sim) && !TextUtils.isEmpty(userInfo.sim)) {
            int formatSim = Math.round(Float.valueOf(userInfo.sim));
            holder.tv_face_sim_size.setText(formatSim + "%");
            holder.tv_face_sim_size.setTypeface(fromAssetTypeFace);
            holder.pg_sim_progress.setProgress(formatSim);
        } else {
            // 无识别相似度数据
            holder.tv_face_sim_size.setText("");
            holder.pg_sim_progress.setProgress(0);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // position总是0 userinfo正常
                LogUtil.i(TAG, "onClick:点击第[" + position + "]个 当前userInfo: " + userInfo.toString());
                if (mListener != null) {
                    mListener.onItemClick(userInfo);
                }
            }
        });
        if (GroupUserInfo.USER_TYPE_VIP.equals(userInfo.family)) {
            holder.face_result_item_type.setVisibility(View.GONE);
            holder.iv_show_is_vip.setVisibility(View.VISIBLE);
        } else if (GroupUserInfo.USER_TYPE_COMMON.equals(userInfo.family)) {
            holder.face_result_item_type.setVisibility(View.VISIBLE);
            holder.iv_show_is_vip.setVisibility(View.GONE);
            holder.face_result_item_type.setText(context.getResources().getString(R.string.user_type_com));
            holder.face_result_item_type.setTextColor(context.getResources().getColor(R.color.glass_common_text_color));
            holder.face_result_item_type.setBackgroundResource(R.drawable.bg_face_sim_normal_txt);
        } else if (GroupUserInfo.USER_TYPE_BLACK.equals(userInfo.family)) {
            holder.face_result_item_type.setVisibility(View.VISIBLE);
            holder.iv_show_is_vip.setVisibility(View.GONE);
            holder.face_result_item_type.setText(context.getResources().getString(R.string.user_type_black));
            holder.face_result_item_type.setTextColor(context.getResources().getColor(R.color.clr_black));
            holder.face_result_item_type.setBackgroundResource(R.drawable.bg_face_sim_black_text);
        } else {
            // 无数据时隐藏所有
            holder.face_result_item_type.setVisibility(View.GONE);
            holder.iv_show_is_vip.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return userInfos == null ? 0 : userInfos.size();
    }

    public void insertHeaderData(FaceRecogListVO userInfo) {
        if (userInfos != null) {
            userInfos.add(0, userInfo);
            LogUtil.i(TAG, "新增一条识别结果数据: " + userInfo.toString());
            notifyItemInserted(0);
            // 通知数据与界面重新绑定 否则索引position总是为0 取数据正常
            // 会刷新所有item UI
            notifyItemRangeChanged(0, userInfos.size() - 0);
        }
    }

    public void notifyData(List<FaceRecogListVO> userInfos) {
        this.userInfos = userInfos;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(FaceRecogListVO userInfo);
    }

    static class FaceDetectionViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.record_iv_thumbnail)
        ImageView record_iv_thumbnail;

        @BindView(R.id.iv_show_is_vip)
        ImageView iv_show_is_vip;

        @BindView(R.id.recognition_result_name)
        TextView recognition_result_name;

        @BindView(R.id.face_result_item_type)
        TextView face_result_item_type;

        @BindView(R.id.recognition_result_id_card)
        TextView recognition_result_id_card;

        @BindView(R.id.tv_face_sim_size)
        TextView tv_face_sim_size;

        @BindView(R.id.pg_sim_progress)
        ProgressBar pg_sim_progress;

        public FaceDetectionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
