package com.biomhope.glass.face.home.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseFragment;
import com.biomhope.glass.face.bean.FaceAddBean;
import com.biomhope.glass.face.bean.FaceAddResult;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.home.adapter.HorizontalFaceGalleryAdapter;
import com.biomhope.glass.face.home.adapter.SpaceItemDecoration;
import com.biomhope.glass.face.utils.Base64;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.DatabaseManager;
import com.biomhope.glass.face.utils.LLCameraUtil;
import com.biomhope.glass.face.utils.LocalFaceUtils;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.google.gson.Gson;
import com.llvision.face.dtr.glxss.FaceDtrClient;
import com.llvision.face.dtr.glxss.IFaceDtrClient;
import com.llvision.face.dtr.glxss.security.listener.IOnFaceDetectListener;
import com.llvision.face.dtr.model.IndicatorColor;
import com.llvision.face.dtr.model.RecognizeResult;
import com.llvision.glass3.framework.LLVisionGlass3SDK;
import com.llvision.glass3.framework.camera.CameraStatusListener;
import com.llvision.glass3.framework.camera.ICameraClient;
import com.llvision.glass3.framework.lcd.ILCDClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FaceRegisterFragment extends BaseFragment {

    private final String TAG_CAMERA = "tag_camera";
    private static final int MSG_DELAY_OPEN_LCD = 1;
    private static final int MSG_DELAY_START_DETEC = 2;
    private static final int MSG_INSERT_FACE_IMG = 3;
    private final static int MSG_CANCEL_DIALOG = 6;
    private final static int MSG_SHOW_RESULT_TOAST = 7;

    private Dialog dialog;
    private View mGlxssView;
    int mIdentifyCount = 0;
    private IFaceDtrClient mFaceDtrClient;
    private ILCDClient mLCDClient;
    private ICameraClient mCameraClient;
    private HorizontalFaceGalleryAdapter faceGalleryAdapter;
    private String sex, vipLevel, registerImgPath;

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.iv_user_header)
    ImageView iv_user_header;

    @BindView(R.id.edt_register_name)
    EditText edt_register_name;

    @BindView(R.id.edt_register_idcard)
    EditText edt_register_idcard;

    @BindView(R.id.spinner_sex)
    Spinner spinner_sex;

    @BindView(R.id.spinner_vip_level)
    Spinner spinner_vip_level;

    @BindView(R.id.face_gallery)
    RecyclerView face_gallery;

    @BindView(R.id.checked_img_area)
    RelativeLayout checked_img_area;

    @BindView(R.id.iv_selected_header)
    ImageView iv_selected_header;

    @OnClick(R.id.tv_face_register_save)
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        if (v.getId() == R.id.tv_face_register_save) {
            submit();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_DELAY_OPEN_LCD:
                    openLCD();
                    Log.i(TAG_CAMERA, "handleMessage: 已经开启LCD 0.5s后开启人脸检测..");
                    mHandler.sendEmptyMessageDelayed(MSG_DELAY_START_DETEC, 500);
                    break;
                case MSG_DELAY_START_DETEC:
                    startDetectFace();
                    break;
                case MSG_INSERT_FACE_IMG:
                    String img = (String) msg.obj;
                    faceGalleryAdapter.insertFaceData(img);
                    break;
                case MSG_CANCEL_DIALOG:
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    break;
                case MSG_SHOW_RESULT_TOAST:
                    int result = msg.arg1;
                    if (result == 1) {
                        showToast(getResources().getString(R.string.face_register_success));
                    } else if (result == 0) {
                        // 具体错误代码
                        showToast((String) msg.obj);
                    } else {
                        showToast((String) msg.obj);
                    }
                    break;
            }
        }
    };

    private CameraStatusListener mCameraStatusListener = new CameraStatusListener() {
        @Override
        public void onCameraConnected() {
            Log.i(TAG_CAMERA, "onCameraConnected: Camera连接上 1.5s后开启LCD...");
            if (mHandler != null)
                mHandler.sendEmptyMessageDelayed(MSG_DELAY_OPEN_LCD, 1500);
        }

        @Override
        public void onCameraDisconnected() {
            Log.i(TAG_CAMERA, "onCameraDisconnected: Camera断开连接..");
        }

        @Override
        public void onCameraServiceIsOccupied() {
            showToast("Camera被占用了");
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_face_register;
    }

    @Override
    protected void initialize() {
        setTitleAndSpinners();
        initGlassConfiguration();
    }

    private void setTitleAndSpinners() {
        tv_center_title.setText(getResources().getString(R.string.tab_face_register));

        final String[] register_sex = getResources().getStringArray(R.array.register_sex);
        final String[] register_vip_level = getResources().getStringArray(R.array.register_vip_level);
        spinner_sex.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, register_sex));
        spinner_sex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner_sex.setSelection(position);
                sex = register_sex[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner_vip_level.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, register_vip_level));
        spinner_vip_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner_vip_level.setSelection(position);
                vipLevel = register_vip_level[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        File file = new File(Constants.FACE_DECECTION_PIC_CROP_FILE);
        List<String> imgs = null;
        if (file.exists()) {
            imgs = new ArrayList<>();
            for (String path : file.list()) {
                imgs.add(Constants.FACE_DECECTION_PIC_CROP_FILE + File.separator + path);
            }
        }

        faceGalleryAdapter = new HorizontalFaceGalleryAdapter(context, new HorizontalFaceGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String path) {
                registerImgPath = path;
                CommonUtil.loadBitmap(context, path, true, R.drawable.userimg, iv_user_header);
                CommonUtil.loadBitmap(context, path, true, R.drawable.recordedfiles_thumbnail_mask, iv_selected_header);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        face_gallery.setLayoutManager(linearLayoutManager);
        face_gallery.setItemAnimator(new DefaultItemAnimator());
        face_gallery.addItemDecoration(new SpaceItemDecoration(5));
        face_gallery.setAdapter(faceGalleryAdapter);

        if (imgs != null && imgs.size() > 0) {
            faceGalleryAdapter.update(imgs);
        }
    }

    @SuppressLint("InflateParams")
    private void initGlassConfiguration() { // 初始化眼镜界面
        mGlxssView = LayoutInflater.from(context).inflate(R.layout.layout_lcd_screen, null);

        new Thread() {
            @Override
            public void run() {
                //1、如果需要在本地识别，那么需要将人脸的feature加载到数据库
                //做一次就行了，不需要多次同步人脸feature
                //2、如果只做DT 或者D（多脸检测）那么不需要同步feature
                DatabaseManager.init(context, R.raw.db_sqlite3);
                LocalFaceUtils.getInstance().syncFeatures();
            }
        }.start();
    }

    private void submit() {

        if (TextUtils.isEmpty(registerImgPath)) {
            showToast("请设置头像!");
            return;
        }

        String name = edt_register_name.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            showToast("请输入姓名!");
            return;
        }

        if (TextUtils.isEmpty(sex) || TextUtils.isEmpty(vipLevel)) {
            showToast("请设置性别和VIP等级!");
            return;
        }

        String idCard = edt_register_idcard.getText().toString().trim();
        if (TextUtils.isEmpty(idCard)) {
            showToast("请输入身份证号!");
            return;
        }

        if (dialog == null) {
            dialog = new Dialog(context, R.style.LoadDialogStyle);
            dialog.setContentView(R.layout.dialog_common_loading);
            dialog.setCancelable(false);
        }
        dialog.show();
        final String addFaceUrl = Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_ADDFACE;
        Log.i(TAG, "submit: addFaceUrl = " + addFaceUrl);
        try {
            final FaceAddBean bean = new FaceAddBean();
            bean.id = Constants._id + CommonUtil.formatMills("yyyyMMddHHmmss", System.currentTimeMillis());
            bean.baseFlag = "0";
            bean.img1 = Base64.encodeToString(CommonUtil.readLocalFile(registerImgPath), Base64.NO_WRAP);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpUtil.getInstance().doAsyncOkHttpPost(addFaceUrl, new Gson().toJson(bean), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                            Log.e(TAG, "onFailure: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                            Log.i(TAG, "onResponse: code = " + response.code());
                            String result = getResources().getString(R.string.net_status_error);
                            Message msg = Message.obtain();
                            msg.what = MSG_SHOW_RESULT_TOAST;
                            if (response.code() == Constants.REQUEST_STATUS_SUCCESSFUL) {
                                ResponseBody responseBody = response.body();
                                if (responseBody != null) {
                                    String content = responseBody.string();
                                    Log.i(TAG, "onResponse: content = " + content);
                                    FaceAddResult addResult = new Gson().fromJson(content, FaceAddResult.class);
                                    msg.arg1 = addResult.result;
                                    if (addResult.result == 0) {
                                        result = addResult.exMsg;
                                    }
                                }
                            }
                            msg.obj = result;
                            mHandler.sendMessage(msg);
                        }
                    });
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCurrentPagePosition(context) == 0)
            openCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        hiddenForCloseCamera();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            hiddenForCloseCamera();
        } else {
            openCamera();
        }
    }

    private void hiddenForCloseCamera() {
        try {
            if (mHandler != null)
                mHandler.removeCallbacksAndMessages(null);
            if (mFaceDtrClient != null) {
                mFaceDtrClient.stopDetect();
            }
            if (mCameraClient != null) {
                mCameraClient.disconnect();
            }
            if (mLCDClient != null) {
                mLCDClient.stopCaptureScreen();
            }
        } catch (Exception e) {
            Log.e(TAG, "hiddenForCloseCamera: failed " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        release();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }

    private void openCamera() {
        Log.i(TAG, "openCamera: open camera");
        try {
            if (!LLVisionGlass3SDK.getInstance().isDeviceConnected()) {
                Log.i(TAG, "openCamera: open camera failed ,glass not connected!!");
                return;
            }
            mCameraClient = LLVisionGlass3SDK.getInstance().getCameraClient();
            if (mCameraClient == null) {
                return;
            }
            mCameraClient.open(mCameraStatusListener);
            mCameraClient.setPreviewSize(1280, 720);
            mCameraClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void release() {
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        // 需要按照顺序关闭
        // 1.关闭人脸
        // 2.关闭Camera和LCD
        if (mFaceDtrClient != null) {
            mFaceDtrClient.stopDetect();
//            mFaceDtrClient.destroyFaceDtr();
        }
        try {
            if (mCameraClient != null) {
                mCameraClient.disconnect();
            }
            if (mLCDClient != null) {
                mLCDClient.stopCaptureScreen();
            }
        } catch (Exception e) {
            Log.e(TAG, "release: failed " + e.getMessage());
        }
    }

    private void openLCD() {
        if (!LLVisionGlass3SDK.getInstance().isConnected()) {
            showToast("服务未绑定，请重启应用同时重新插拔USB");
            return;
        }
        try {
            mLCDClient = LLVisionGlass3SDK.getInstance().getLcdClient();
            if (mLCDClient == null) {
                return;
            }
            if (mGlxssView.getParent() != null) {
                ((ViewGroup) mGlxssView.getParent()).removeView(mGlxssView);
            }
            mLCDClient.createCaptureScreen(context, mGlxssView);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private IOnFaceDetectListener mIOnFaceDetectListener = new IOnFaceDetectListener() {

        @Override
        public void onRecognize(RecognizeResult recognizeResult) {
            mIdentifyCount++;
            mFaceDtrClient.setTrackPhase(mIdentifyCount % 3 == 0 ? IndicatorColor.PHASE_TRACK_IDENTIFIED : IndicatorColor.PHASE_TRACK_UNIDENTIFIED);

            String currentTimeMillis = CommonUtil.formatMills(System.currentTimeMillis());
            String cropImgPath = LLCameraUtil.getCropImgPath(currentTimeMillis);
            boolean imwrite = LLCameraUtil.handleRecognizeResult(TAG, LLCameraUtil.getSourceImgPath(currentTimeMillis), cropImgPath, recognizeResult);
            if (imwrite) {
                Message msg = Message.obtain();
                msg.what = MSG_INSERT_FACE_IMG;
                msg.obj = cropImgPath;
                mHandler.sendMessage(msg);
            }
        }

        // 返回比较相似的前几条数据
        @Override
        public int getTopKFaceNum() {
            // 识别后，比对时返回的人脸数目
            return 1;
        }
    };

    private void startDetectFace() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!LLVisionGlass3SDK.getInstance().isConnected()) {
                        return;
                    }
                    mFaceDtrClient = FaceDtrClient.getInstance(LLVisionGlass3SDK.getInstance().getHostAIDL());
                    mFaceDtrClient.loadFaceDetectionModel(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mFaceDtrClient.setOnFaceDetectListener(mIOnFaceDetectListener);

                if (mFaceDtrClient != null) {
                    mFaceDtrClient.startDetect(context, LLCameraUtil.buildFaceParamter());
                }
            }
        }).start();
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }
}
