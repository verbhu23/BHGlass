package com.biomhope.glass.face.home.settings;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseActivity;
import com.biomhope.glass.face.bean.FaceAddBean;
import com.biomhope.glass.face.bean.FaceAddResult;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.utils.Base64;
import com.biomhope.glass.face.utils.CheckPermissionUtil;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

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
public class RecoilRegisterActivity extends BaseActivity {

    @BindView(R.id.ib_back)
    RelativeLayout ib_back;

    @BindView(R.id.main_layout)
    LinearLayout main_layout;

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.tv_action_end)
    TextView tv_action_end;

    @BindView(R.id.iv_user_header)
    ImageView iv_user_header;

    @BindView(R.id.edt_register_name)
    EditText edt_register_name;

    @BindView(R.id.edt_set_register_idcard)
    EditText edt_set_register_idcard;

    @BindView(R.id.edt_set_register_vip_level)
    EditText edt_set_register_vip_level;

    @OnClick({R.id.ib_back, R.id.tv_action_end, R.id.iv_user_header})
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.tv_action_end:
                submit();
                break;
            case R.id.iv_user_header:
                showSelectProfilePicPop();
                break;
        }
    }

    private final static int CAMERA_REQUEST_CODE = 1;
    private final static int GALLERY_REQUEST_CODE = 2;
    private static final int CROP_REQUEST_CODE = 3;

    private final static int FOR_WRITE_PERMISSION_CODE = 4;
    private final static int FOR_CAMERA_PERMISSION_CODE = 5;

    private final static int MSG_CANCEL_DIALOG = 6;
    private final static int MSG_SHOW_RESULT_TOAST = 7;

    private boolean isAlreadySubmit = false;// 防止重复提交
    private Dialog dialog;
    private PopupWindow popupWindow;
    private Uri takePicTargetUri;// 相机输出结果
    private Uri cropTargetUri;
    private File keepCropFile = null;// 用来存放crop后的文件 防止onActivityResult中Intent拿到的缩略图过小

    private static final String[] CAMERA_NEED_PERMISSION = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//  Write access
            Manifest.permission.CAMERA,
    };


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_CANCEL_DIALOG) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                isAlreadySubmit = true;
            } else if (msg.what == MSG_SHOW_RESULT_TOAST) {
                int result = msg.arg1;
                if (result == 1) {
                    showToast("上传成功!");
                } else if (result == 0) {
                    showToast((String) msg.obj);
                }
            }
        }
    };

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_recoil_register);
    }

    @Override
    protected void initialize() {
        ib_back.setVisibility(View.VISIBLE);
        tv_center_title.setText(getResources().getString(R.string.set_recoil_register));
        tv_action_end.setText(getResources().getString(R.string.set_register_finish));
        tv_action_end.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FOR_WRITE_PERMISSION_CODE) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openSystemGallery();
            } else {
                // ACCESS DENY!!
                Log.e(TAG, "onRequestPermissionsResult: GALLERY ACCESS DENY finish !");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    CommonUtil.showRequestPermissionDialog("该功能需要赋予存储权限，不开启将无法正常工作！", this);
                }
            }
        } else if (requestCode == FOR_CAMERA_PERMISSION_CODE) {
            boolean hasPermissionDismiss = false;
            for (int grantResult : grantResults) {
                if (grantResult == -1) {
                    hasPermissionDismiss = true;
                }
            }
            if (!hasPermissionDismiss) {
                openSystemCamera();
            } else {
                // ACCESS DENY!!
                Log.e(TAG, "onRequestPermissionsResult: CAMERA ACCESS DENY !");
                // shouldShowRequestPermissionRationale点击不再提醒之后有效
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    CommonUtil.showRequestPermissionDialog("该功能需要赋予存储及相机权限，不开启将无法正常工作！", this);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        try {
            if (requestCode == CAMERA_REQUEST_CODE) {
                if (takePicTargetUri != null) {
                    crop(takePicTargetUri);
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                if (data != null) {
                    crop(data.getData());
                }
            } else if (requestCode == CROP_REQUEST_CODE) {
                isAlreadySubmit = false;
                Glide.with(this).load(cropTargetUri).transition(new DrawableTransitionOptions().crossFade(250)).
                        apply(new RequestOptions().bitmapTransform(new CircleCrop()).placeholder(R.drawable.userimg).error(R.drawable.userimg)).into(iv_user_header);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSelectProfilePicPop() {
        if (popupWindow == null) {
            View popView = View.inflate(this, R.layout.pop_select_img_bottom, null);
            popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
            popupWindow.setFocusable(true);
            popupWindow.setAnimationStyle(R.style.PopupWindowBotom);
            popupWindow.setOutsideTouchable(true);

            popView.findViewById(R.id.tv_take_shoot).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                    openSystemCamera();
                }
            });
            popView.findViewById(R.id.tv_album).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                    openSystemGallery();
                }
            });
            popView.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                }
            });
        }
        if (!popupWindow.isShowing()) {
            popupWindow.showAtLocation(main_layout, Gravity.BOTTOM, 0, 0);
        } else {
            popupWindow.dismiss();
        }
    }

    // 打开相机拍摄
    private void openSystemCamera() {
        // 先检查权限
        if (!CheckPermissionUtil.permissionSet(this, CAMERA_NEED_PERMISSION)) {
            File targetFile = null;
            File storageDir;
            String timeStamp = "takeAt" + CommonUtil.formatMills("yyyy_MM_dd_HH_mm_ss", System.currentTimeMillis()) + ".png";
            try {
                // glassIcons 文件名必须和filepaths中一致
                String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "glassIcons";
                storageDir = new File(dirPath);
                // 不创建目录则保存失败
                if (!storageDir.exists())
                    storageDir.mkdirs();
                targetFile = new File(dirPath, timeStamp);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 7.0以后替换
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // authority对应manifest一致
                takePicTargetUri = FileProvider.getUriForFile(this, Constants.FILE_PROVIDER_AUTHORITY, targetFile);
            } else {
                takePicTargetUri = Uri.fromFile(targetFile);
            }
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // 输出路径
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, takePicTargetUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    FOR_CAMERA_PERMISSION_CODE);
        }
    }

    // 打开相册选图
    private void openSystemGallery() {
        if (!CheckPermissionUtil.permissionSet(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    FOR_WRITE_PERMISSION_CODE);
        }
    }

    // 手工裁剪
    private void crop(Uri uri) {
        String storagePath;
        File storageDir;
        try {
            storagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "glassIcons";
            storageDir = new File(storagePath);
            if (!storageDir.exists())
                storageDir.mkdirs();
            String cropPicName = "croppedAt" + CommonUtil.formatMills("yyyy_MM_dd_HH_mm_ss", System.currentTimeMillis()) + ".png";
            keepCropFile = new File(storagePath, cropPicName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cropTargetUri = Uri.fromFile(keepCropFile);

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        // 裁剪图片的宽高比例
        intent.putExtra("aspectX", 9998);
        intent.putExtra("aspectY", 9999);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 280);
        intent.putExtra("outputY", 280);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // 人脸识别
        intent.putExtra("noFaceDetection", false);
        //true - don't return uri |  false - return uri
        intent.putExtra("return-data", false);
        // 文件输出位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropTargetUri);
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }

    private void submit() {
        if (cropTargetUri == null) {
            showToast("请重新选择照片!");
            return;
        }
        if (isAlreadySubmit) {
            showToast("请勿重复提交!");
            return;
        }
        String name = edt_register_name.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            showToast("请输入姓名!");
            return;
        }
        String idCard = edt_set_register_idcard.getText().toString().trim();
        if (TextUtils.isEmpty(idCard)) {
            showToast("请输入身份证号!");
            return;
        }
        String vipLevel = edt_set_register_vip_level.getText().toString().trim();
        if (TextUtils.isEmpty(idCard)) {
            showToast("请输入VIP等级!");
            return;
        }

        if (dialog == null) {
            dialog = new Dialog(this, R.style.LoadDialogStyle);
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
            bean.img1 = Base64.encodeToString(CommonUtil.input2byte(getContentResolver().openInputStream(cropTargetUri)), Base64.NO_WRAP);
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
                            if (response.code() == Constants.REQUEST_STATUS_SUCCESSFUL) {
                                String string = response.body() != null ? response.body().string() : null;
                                Log.i(TAG, "onResponse: content = " + string);
                                Message msg = Message.obtain();
                                msg.what = MSG_SHOW_RESULT_TOAST;
                                FaceAddResult addResult = new Gson().fromJson(string, FaceAddResult.class);
                                msg.arg1 = addResult.result;
                                if (addResult.result == 0) {
                                    msg.obj = addResult.exMsg;
                                }
                                mHandler.sendMessage(msg);
                            }
                        }
                    });
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
        if (dialog != null) {
            dialog.cancel();
            dialog = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }
}
