package com.biomhope.glass.face.home.session;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.home.session.audio.MediaManager;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.FastBlur;
import com.biomhope.glass.face.utils.LogUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author $USER_NAME
 * create at : 2018-10-22
 * description :
 */
public class ChatRecycleViewAdapter extends RecyclerView.Adapter<ChatRecycleViewAdapter.RecycleChatViewHolder> {

    private static final String TAG = ChatRecycleViewAdapter.class.getSimpleName();

    private List<ChatItemBean> chatItemBeans;
    private LayoutInflater mInflater;
    private Context context;
    private boolean isVoicePlaying = false; // 当前音频播放状态
    private int playVoicePosition = -1; // 继续播放音频的item临时索引

    private ArrayList<Uri> uris; // 存储当前聊天列表所有图片

    ChatRecycleViewAdapter(Context context, List<ChatItemBean> chatItemBeans) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.chatItemBeans = chatItemBeans;
    }

    @Override
    public RecycleChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecycleChatViewHolder viewHolder = null;
        switch (viewType) {
            case ChatItemBean.LEFT_SERVER_SESSION:
                viewHolder = new RecycleChatViewHolder(mInflater.inflate(R.layout.item_left_server_chat, parent, false));
                break;
            case ChatItemBean.RIGHT_CLIENT_SESSION:
                viewHolder = new RecycleChatViewHolder(mInflater.inflate(R.layout.item_right_client_chat, parent, false));
                break;
        }
        return viewHolder;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(final RecycleChatViewHolder holder, final int position) {
        final ChatItemBean chatItemBean = chatItemBeans.get(position);
        if (chatItemBean == null) {
            return;
        }
        if (chatItemBean.type == ChatItemBean.LEFT_SERVER_SESSION) {
            holder.tv_content.setText(chatItemBean.msg);
        } else if (chatItemBean.type == ChatItemBean.RIGHT_CLIENT_SESSION) {
            if (!TextUtils.isEmpty(chatItemBean.voiceFilePath)) {
                // 音频文件
                holder.tv_voice_length.setVisibility(View.VISIBLE);
                holder.tv_content.setVisibility(View.VISIBLE);
                holder.iv_user_chat_pic.setVisibility(View.GONE);
                holder.tv_voice_length.setText(String.format("%d''", chatItemBean.voiceLength));
                holder.tv_content.setSingleLine();
                holder.tv_content.setText(formatVoiceLength(chatItemBean.voiceLength));
                holder.tv_content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playVoice(chatItemBean.voiceFilePath, position);
                    }
                });
            } else if (chatItemBean.imageSize != null) {
                // 图片
                holder.tv_voice_length.setVisibility(View.GONE);
                holder.tv_content.setVisibility(View.GONE);
                holder.iv_user_chat_pic.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(chatItemBean.imageSize.uri)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(CommonUtil.dp2px(context, 5)))
                                        .placeholder(R.drawable.img_loading)
                                        .error(R.drawable.img_load_failed)
                                        .override(chatItemBean.imageSize.width, chatItemBean.imageSize.height)
//                                .transform(new MyTransformtion()) // 高斯模糊
                        )
                        .into(holder.iv_user_chat_pic);
                holder.iv_user_chat_pic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 点击进入浏览大图
                        Intent browseIntent = new Intent(context, LargerImageActivity.class);
                        browseIntent.putExtra("position", position);
                        browseIntent.putExtra("imageuri", chatItemBean.imageSize.uri);
                        context.startActivity(browseIntent);
                    }
                });
            } else if (!TextUtils.isEmpty(chatItemBean.msg)) {
                // 文本
                holder.tv_voice_length.setVisibility(View.GONE);
                holder.iv_user_chat_pic.setVisibility(View.GONE);
                holder.tv_content.setVisibility(View.VISIBLE);
                holder.tv_content.setText(chatItemBean.msg);
            }
        }
    }

    class MyTransformtion extends BitmapTransformation {

        @Override
        protected Bitmap transform(BitmapPool bitmapPool, Bitmap bitmap, int i, int i1) {
            return FastBlur.doBlur(bitmap, 10, true);
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

        }
    }

    private void playVoice(String voiceFilePath, int position) {
        if (isVoicePlaying) {
            // 点击的是当前正在播放的Item
            if (playVoicePosition == position) {
                MediaManager.release();
                playVoicePosition = -1;
                isVoicePlaying = false;
            } else {
                // 点击的是其他item 需要关闭之前的再播放点击的
                MediaManager.release();
                //开始实质播放
                MediaManager.playSound(voiceFilePath,
                        new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
//                                            animationDrawable.selectDrawable(0);//显示动画第一帧
//                                            animationDrawable.stop();

                                //播放完毕，当前播放索引置为-1。
                                playVoicePosition = -1;
                                isVoicePlaying = false;
                            }
                        });
                isVoicePlaying = true;
                playVoicePosition = position;
            }
        } else {
            //播放前重置。
            MediaManager.release();
            //开始实质播放
            MediaManager.playSound(voiceFilePath,
                    new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            LogUtil.i(TAG, "播放完毕");
//                                            animationDrawable.selectDrawable(0);//显示动画第一帧
//                                            animationDrawable.stop();

                            //播放完毕，当前播放索引置为-1。
                            playVoicePosition = -1;
                            isVoicePlaying = false;
                        }
                    });
            playVoicePosition = position;
            isVoicePlaying = true;
        }
    }

    private String formatVoiceLength(int spaceCount) {
        if (spaceCount > 0 && spaceCount <= 60) {
            StringBuilder spaceStrBuild = new StringBuilder();
            for (int i = 0; i < spaceCount; i++) {
                spaceStrBuild.append("  ");
            }
            return spaceStrBuild.toString();
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return chatItemBeans == null ? 0 : chatItemBeans.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatItemBean chatItemBean = chatItemBeans.get(position);
        return chatItemBean.type;
    }

    public void insertLastItemData(ChatItemBean newBean) {
        chatItemBeans.add(newBean);
        notifyDataSetChanged();
    }

    static class RecycleChatViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_current_time)
        TextView tv_current_time;

        @BindView(R.id.tv_join_chat)
        TextView tv_join_chat;

        @BindView(R.id.tv_content)
        TextView tv_content;

        @BindView(R.id.tv_voice_length)
        TextView tv_voice_length;

        @BindView(R.id.iv_user_icon)
        ImageView iv_user_icon;

        @BindView(R.id.iv_user_chat_pic)
        ImageView iv_user_chat_pic;

        @BindView(R.id.chat_area)
        LinearLayout chat_area;

        RecycleChatViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
