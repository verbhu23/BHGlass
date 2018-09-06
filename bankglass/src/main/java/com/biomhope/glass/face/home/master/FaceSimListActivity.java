package com.biomhope.glass.face.home.master;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseActivity;
import com.biomhope.glass.face.bean.SearchPersonsBean;
import com.biomhope.glass.face.bean.SearchPersonsResultTemp;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.home.adapter.SimFaceAdapter;
import com.biomhope.glass.face.utils.Base64;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
/**
 * author:BH
 * create at:2018/9/5
 * description:
 *
 */
public class FaceSimListActivity extends BaseActivity {

    private final static int MSG_UPDATE_SIM_LIST_FLAG = 1;
    private final static int MSG_HIDE_PROGRESS_FLAG = 2;

    private Dialog dialog;
    private String imgCropPath;
    private List<SearchPersonsResultTemp.UserInfo> simUsers = new ArrayList<>();
    private SimFaceAdapter simFaceAdapter;

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

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_SIM_LIST_FLAG:
                    List<SearchPersonsResultTemp.UserInfo> userinfos = (List<SearchPersonsResultTemp.UserInfo>) msg.obj;
                    pb_sim_list_loading.setVisibility(View.GONE);
                    if (userinfos != null && userinfos.size() > 0) {
                        simFaceAdapter.notifyData(userinfos);
                    }
                    int itemCount = simFaceAdapter.getItemCount();
                    Log.i(TAG, "handleMessage: itemCount = " + itemCount);
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
            String sourceImgPath = CommonUtil.getSourceFaceImgPath(imgCropPath);
            Log.i(TAG, "showSourceImgDialog: " + sourceImgPath);
            Glide.with(this)
                    .load("file://" + Constants.FACE_DECECTION_PIC_SOURCE_FILE + File.separator + sourceImgPath)
                    .transition(new DrawableTransitionOptions().crossFade(250))
                    .apply(new RequestOptions().error(R.drawable.pictures_no))
                    .into(iv_face_source_img);
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
        imgCropPath = getIntent().getStringExtra("imgCropPath");
        String name = getIntent().getStringExtra("name");
        String userType = getIntent().getStringExtra("userType");

        if ("vip".equals(userType)) {
            iv_is_vip.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(name)) {
            name = getResources().getString(R.string.user_detail_error);
        }
        tv_user_name.setText(name);
        Glide.with(this)
                .load("file://" + imgCropPath)
                .transition(new DrawableTransitionOptions().crossFade(250))
                .apply(new RequestOptions().bitmapTransform(new CircleCrop()).error(R.drawable.remote_refresh))
                .into(iv_img_crop);

        recyclerview_sim.setLayoutManager(new LinearLayoutManager(this));
        recyclerview_sim.setItemAnimator(new DefaultItemAnimator());
        simFaceAdapter = new SimFaceAdapter(simUsers, this, new SimFaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String imgUrl) {
                Intent simDetailIntent = new Intent(FaceSimListActivity.this, FaceSimDetailActivity.class);
                simDetailIntent.putExtra("imgUrl", imgUrl);
                simDetailIntent.putExtra("imgUrlType", "net");
                startActivity(simDetailIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        recyclerview_sim.setAdapter(simFaceAdapter);
        requestSearchPerson(CommonUtil.readLocalFile(imgCropPath));

    }

    private void requestSearchPerson(byte[] base64Img1) {
        String requestUrl = Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_SEARCHPERSONS;
        Log.i(TAG, "requestSearchPerson: requestUrl = " + requestUrl);

        SearchPersonsBean searchPersonsBean = new SearchPersonsBean(Base64.encodeToString(base64Img1, Base64.NO_WRAP));

        OkHttpUtil.getInstance().doAsyncOkHttpPost(requestUrl, new Gson().toJson(searchPersonsBean), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                Log.i(TAG, "onResponse: code = " + code);
                if (code == Constants.REQUEST_STATUS_SUCCESSFUL) {
                    String string = null;
                    if (response.body() != null) {
                        string = response.body().string();
                        SearchPersonsResultTemp resultTemp = new Gson().fromJson(string, SearchPersonsResultTemp.class);
                        List<SearchPersonsResultTemp.UserInfo> userInfos = null;
                        if (resultTemp.result == 1) {
                            userInfos = resultTemp.userInfos;
                        } else {
                            Log.e(TAG, "onResponse: error exCode = " + resultTemp.exCode + " exMsg is " + resultTemp.exMsg);
                        }
                        Message msg = Message.obtain();
                        msg.what = MSG_UPDATE_SIM_LIST_FLAG;
                        msg.obj = userInfos;
                        mHandler.sendMessage(msg);
                    }
                    Log.i(TAG, "onResponse: content = " + string);
                }
                mHandler.sendEmptyMessage(MSG_HIDE_PROGRESS_FLAG);
            }
        });
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
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}