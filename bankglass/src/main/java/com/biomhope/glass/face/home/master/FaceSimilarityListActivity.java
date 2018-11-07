package com.biomhope.glass.face.home.master;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.bean.FaceRecogDetailRequestBean;
import com.biomhope.glass.face.bean.FaceRecogListVO;
import com.biomhope.glass.face.bean.FaceSimilarResult;
import com.biomhope.glass.face.bean.GroupUserInfo;
import com.biomhope.glass.face.bean.UserInfo;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * author:BH
 * create at:2018/9/5
 * description: 展示相似人脸列表
 */
public class FaceSimilarityListActivity extends BaseActivity {

    private final static int MSG_UPDATE_SIM_LIST_FLAG = 1;
    private final static int MSG_HIDE_PROGRESS_FLAG = 2;

    private Dialog dialog;
    private String headerIconUrl; // 展示原图路径
    private List<UserInfo> simUsers = new ArrayList<>();
    private FaceSimilarityAdapter faceSimilarityAdapter;
    private FaceRecogListVO userInfo;

    @BindView(R.id.ib_back)
    RelativeLayout ib_back;

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.iv_img_crop)
    ImageView iv_img_crop;

    @BindView(R.id.iv_is_vip)
    ImageView iv_is_vip;

    @BindView(R.id.tv_user_name)
    TextView tv_user_name;

    @BindView(R.id.tv_total_sim_result)
    TextView tv_total_sim_result;

    @BindView(R.id.recyclerview_sim)
    RecyclerView recyclerview_sim;

    @BindView(R.id.pb_sim_list_loading)
    ProgressBar pb_sim_list_loading;

    @OnClick({R.id.iv_img_crop, R.id.ib_back})
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.iv_img_crop:
                showSourceImgDialog();
                break;
            case R.id.ib_back:
                finish();
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_SIM_LIST_FLAG:
                    List<UserInfo> userinfos = (List<UserInfo>) msg.obj;
                    if (userinfos != null && !userinfos.isEmpty()) {
                        faceSimilarityAdapter.notifyData(userinfos);
                    }
                    int itemCount = faceSimilarityAdapter.getItemCount();
                    String source = itemCount + "个相似结果";
                    // 富文本拼接
                    SpannableString spannableString = new SpannableString(source);
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#F25353"));
                    int end = source.indexOf("个");
                    spannableString.setSpan(colorSpan, 0, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    tv_total_sim_result.setText(spannableString);
                    break;
                case MSG_HIDE_PROGRESS_FLAG:
                    pb_sim_list_loading.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private void showSourceImgDialog() {
        if (dialog == null) {
            dialog = new Dialog(this, R.style.Dialog_show_bigpic_style);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(CommonUtil.getScreenWidth(this), CommonUtil.getScreenHeight(this));
            View view = View.inflate(this, R.layout.dialog_show_face_source, null);
            dialog.setContentView(view, layoutParams);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            ImageView iv_face_source_img = dialog.findViewById(R.id.iv_face_source_img);

            if (userInfo != null) {
                if (userInfo.isServerPic) {
                    // 来自网络没有存原图 都是同一张图
                    CommonUtil.loadOnlinePic(this, userInfo.img1, iv_face_source_img);
                } else {
                    // 原图在本地
                    String sourceImgPath = CommonUtil.getSourceFaceImgPath(headerIconUrl);
                    CommonUtil.loadLocalPic(this, Constants.FACE_DECECTION_PIC_SOURCE_FILE + File.separator + sourceImgPath, iv_face_source_img);
                }
            }

            iv_face_source_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog != null)
                        dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_face_sim_list);
    }

    @Override
    protected void initialize() {
        ib_back.setVisibility(View.VISIBLE);
        tv_center_title.setText(getResources().getString(R.string.title_page_3));

        String userId = getIntent().getStringExtra("userId");
        userInfo = (FaceRecogListVO) getIntent().getExtras().get("userInfo");
        if (userInfo == null) {
            LogUtil.e(TAG, TAG + " getIntent() userInfo为Null");
            finish();
        }
        recyclerview_sim.setLayoutManager(new LinearLayoutManager(this));
        recyclerview_sim.setItemAnimator(new DefaultItemAnimator());
        faceSimilarityAdapter = new FaceSimilarityAdapter(simUsers, this, new FaceSimilarityAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserInfo userInfo) {
                Intent simDetailIntent = new Intent(FaceSimilarityListActivity.this, FaceSimilarityDetailActivity.class);
                simDetailIntent.putExtra("userInfo", userInfo);
                startActivity(simDetailIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        recyclerview_sim.setAdapter(faceSimilarityAdapter);
        if (GroupUserInfo.USER_TYPE_VIP.equals(userInfo.family)) {
            iv_is_vip.setVisibility(View.VISIBLE);
        }
        // 设置图片
        if (userInfo.isServerPic) {
            headerIconUrl = userInfo.img1;
            CommonUtil.loadOnlinePic(this, headerIconUrl, iv_img_crop);
        } else {
            // 本地图片
            headerIconUrl = userInfo.imgCropPath;
            CommonUtil.loadLocalPic(this, headerIconUrl, iv_img_crop);
        }
        requestFaceRecogDetail(userId, userInfo.id);

    }

    private void requestFaceRecogDetail(String userId, int id) {
        OkHttpUtil.getInstance().doAsyncOkHttpPost(
                Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_FACERECOGNIZESDETAIL,
                new Gson().toJson(new FaceRecogDetailRequestBean(userId, String.valueOf(id))),
                new OkHttpUtil.CallBack() {
                    @Override
                    public void onFailure() {
                        mHandler.sendEmptyMessage(MSG_HIDE_PROGRESS_FLAG);
                        showConnectHttpError();
                    }

                    @Override
                    public void onResponseSuccess(String json) {
                        mHandler.sendEmptyMessage(MSG_HIDE_PROGRESS_FLAG);
                        FaceSimilarResult faceSimilarResult = new Gson().fromJson(json, FaceSimilarResult.class);
                        if (Constants.RESPONSE_STATUS_SUCCESSFUL.equals(faceSimilarResult.exCode)) {
                            Message msg = Message.obtain();
                            msg.what = MSG_UPDATE_SIM_LIST_FLAG;
                            msg.obj = faceSimilarResult.clientList;
                            mHandler.sendMessage(msg);
                        }
                    }

                    @Override
                    public void onResponseFailed() {
                        mHandler.sendEmptyMessage(MSG_HIDE_PROGRESS_FLAG);
                    }
                });

    }

    private class GetImgCacheAsyncTask extends AsyncTask<String, Void, File> {

        private Context context;
        private ImageView imageView;
        private String imgUrl;

        public GetImgCacheAsyncTask(Context context, ImageView imageView) {
            this.context = context;
            this.imageView = imageView;
        }

        @Override
        protected File doInBackground(String... params) {
            imgUrl = params[0];
            try {
                return Glide.with(context)
                        .load(imgUrl)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(File result) {
            if (result == null) {
                return;
            }
            // 此path就是对应文件的缓存路径
            String path = result.getPath();
            LogUtil.e("Glide缓存path: ", path);
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.img_loading)
                    .error(R.drawable.img_load_failed)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
            Glide.with(context)
                    .load(imgUrl)
                    .transition(new DrawableTransitionOptions().crossFade(250))
                    .apply(requestOptions)
                    .into(imageView);

//            requestSearchPerson(CommonUtil.readLocalFile(path));
        }

    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

    @Override
    protected void onDestroy() {
        if (dialog != null) {
            dialog.cancel();
            dialog = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}