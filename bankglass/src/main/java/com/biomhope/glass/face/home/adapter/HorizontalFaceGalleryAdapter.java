package com.biomhope.glass.face.home.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.biomhope.glass.face.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HorizontalFaceGalleryAdapter extends RecyclerView.Adapter<HorizontalFaceGalleryAdapter.FaceGalleryViewHolder> {

    private Context context;
    private List<String> imgs;
    private OnItemClickListener mListener;

    public HorizontalFaceGalleryAdapter(Context context, OnItemClickListener mListener) {
        this.context = context;
        this.mListener = mListener;
    }

    @Override
    public FaceGalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FaceGalleryViewHolder(LayoutInflater.from(context).inflate(R.layout.item_face_gallery, parent, false));
    }

    @Override
    public void onBindViewHolder(FaceGalleryViewHolder holder, final int position) {
        Glide.with(context)
                .load("file://" + imgs.get(position))
                .transition(new DrawableTransitionOptions().crossFade(250))
                .apply(new RequestOptions().placeholder(R.drawable.recordedfiles_thumbnail_mask))
                .into(holder.face_image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(imgs.get(position));
                }
            }
        });
    }

    public void update(List<String> imgs) {
        this.imgs = imgs;
        notifyDataSetChanged();
    }

    public void insertFaceData(String newFace) {
        if (imgs != null) {
            imgs.add(0, newFace);
            notifyItemInserted(0);
            notifyItemRangeChanged(0, imgs.size() - 0);
        }
    }

    @Override
    public int getItemCount() {
        return imgs == null ? 0 : imgs.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String path);
    }

    static class FaceGalleryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.face_image)
        ImageView face_image;

        public FaceGalleryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
