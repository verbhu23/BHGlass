package com.biomhope.glass.face.home.settings;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.bean.GroupUserInfo;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FaceLibGroupAdapter extends RecyclerView.Adapter<FaceLibGroupAdapter.FaceDetectionViewHolder> {

    private static final String TAG = FaceLibGroupAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<GroupUserInfo> sysPushBeanArrayList;
    private OnItemClickListener mListener;

    FaceLibGroupAdapter(Context context, ArrayList<GroupUserInfo> sysPushBeanArrayList, OnItemClickListener mListener) {
        this.context = context;
        this.sysPushBeanArrayList = sysPushBeanArrayList;
        this.mListener = mListener;
    }

    @Override
    public FaceDetectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FaceDetectionViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recognition_result, parent, false));
    }

    @Override
    public void onBindViewHolder(FaceDetectionViewHolder holder, final int position) {
        final GroupUserInfo sysPushBean = sysPushBeanArrayList.get(position);
        // 头像来自注册或者在编辑中修改过
        if (!TextUtils.isEmpty(sysPushBean.img2)) {
            LogUtil.i(TAG, "FaceLibGroupAdapter onBindViewHolder第[" + position + "]行数据: " + sysPushBean.toString1());
            CommonUtil.loadLocalPic(context, sysPushBean.img2, holder.record_iv_thumbnail);
        } else {
            LogUtil.i(TAG, "FaceLibGroupAdapter onBindViewHolder第[" + position + "]行数据: " + sysPushBean.toString());
            // 列表抓取就是来自网络
            CommonUtil.loadOnlinePic(context, sysPushBean.img1, holder.record_iv_thumbnail);
        }
        holder.recognition_result_name.setText(sysPushBean.name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(sysPushBean);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mListener != null) {
                    mListener.onLongItemClick(position, sysPushBean);
                    return true;
                }
                return false;
            }
        });

        holder.recognition_result_id_card.setText(sysPushBean.idCard);
        String group_name = sysPushBean.family;
        if (GroupUserInfo.USER_TYPE_VIP.equals(group_name)) {
            holder.face_result_item_type.setVisibility(View.GONE);
            holder.iv_show_is_vip.setVisibility(View.VISIBLE);
        } else if (GroupUserInfo.USER_TYPE_COMMON.equals(group_name)) {
            holder.face_result_item_type.setVisibility(View.VISIBLE);
            holder.iv_show_is_vip.setVisibility(View.GONE);
            holder.face_result_item_type.setText(context.getResources().getString(R.string.user_type_com));
            holder.face_result_item_type.setTextColor(context.getResources().getColor(R.color.glass_common_text_color));
            holder.face_result_item_type.setBackgroundResource(R.drawable.bg_face_sim_normal_txt);
        } else if (GroupUserInfo.USER_TYPE_BLACK.equals(group_name)) {
            holder.face_result_item_type.setVisibility(View.VISIBLE);
            holder.iv_show_is_vip.setVisibility(View.GONE);
            holder.face_result_item_type.setText(context.getResources().getString(R.string.user_type_black));
            holder.face_result_item_type.setTextColor(context.getResources().getColor(R.color.clr_black));
            holder.face_result_item_type.setBackgroundResource(R.drawable.bg_face_sim_black_text);
        }

    }

    @Override
    public int getItemCount() {
        return sysPushBeanArrayList == null ? 0 : sysPushBeanArrayList.size();
    }

    public void insertHeaderData(GroupUserInfo sysPushBean) {
        if (sysPushBeanArrayList != null) {
            sysPushBeanArrayList.add(0, sysPushBean);
            LogUtil.i(TAG, "FaceLibGroupAdapter添加到第[0]行后共有" + sysPushBeanArrayList.size() + "个数据");
            notifyItemInserted(0);
            // 通知数据与界面重新绑定
            notifyItemRangeChanged(0, sysPushBeanArrayList.size());
        }
    }

    public void updateItemData(int position, GroupUserInfo registerBean) {
        if (sysPushBeanArrayList != null && position < sysPushBeanArrayList.size()) {
//            sysPushBeanArrayList.remove(position);
            sysPushBeanArrayList.set(position, registerBean);
            LogUtil.e(TAG, "FaceLibGroupAdapter更新第[" + position + "]行后数据为: " + sysPushBeanArrayList.get(position).toString());
            notifyItemChanged(position);
        }
    }

    public void removeItemData(int position) {
        if (sysPushBeanArrayList != null && position < sysPushBeanArrayList.size()) {
            sysPushBeanArrayList.remove(position);
            LogUtil.i(TAG, "FaceLibGroupAdapter删除第[" + position + "]行");
            notifyItemRemoved(position);
            LogUtil.i(TAG, "FaceLibGroupAdapter删除第[" + position + "]行后 List大小为: " + sysPushBeanArrayList.size());
            notifyItemRangeChanged(0, sysPushBeanArrayList.size());
        }
    }

    public void notifySingleData(GroupUserInfo newRegisterBean) {
        this.sysPushBeanArrayList.add(newRegisterBean);
        notifyDataSetChanged();
    }

    public void notifyData(ArrayList<GroupUserInfo> sysPushBeanArrayList) {
        this.sysPushBeanArrayList = sysPushBeanArrayList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(GroupUserInfo sysPushBean);

        void onLongItemClick(int position, GroupUserInfo sysPushBean);
    }

    static class FaceDetectionViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.record_iv_thumbnail)
        ImageView record_iv_thumbnail;

        @BindView(R.id.recognition_result_name)
        TextView recognition_result_name;

        @BindView(R.id.iv_show_is_vip)
        ImageView iv_show_is_vip;

        @BindView(R.id.face_result_item_type)
        TextView face_result_item_type;

        @BindView(R.id.recognition_result_id_card)
        TextView recognition_result_id_card;

        FaceDetectionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
