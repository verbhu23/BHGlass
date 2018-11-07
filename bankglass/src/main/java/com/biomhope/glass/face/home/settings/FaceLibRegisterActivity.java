package com.biomhope.glass.face.home.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatRatingBar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.bean.FaceLibRegisterResult;
import com.biomhope.glass.face.bean.GroupUserInfo;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.utils.Base64;
import com.biomhope.glass.face.utils.CheckPermissionUtil;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.biomhope.glass.face.utils.SharedPreferencesUtils;
import com.bumptech.glide.Glide;
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
 * description: 10/12修改手动注册界面为人脸库界面
 */
public class FaceLibRegisterActivity extends BaseActivity {

    @BindView(R.id.ib_back)
    RelativeLayout ib_back;

    @BindView(R.id.main_layout)
    RelativeLayout main_layout;

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

    @BindView(R.id.edt_set_register_phone_number)
    EditText edt_set_register_phone_number;

    @BindView(R.id.edt_set_register_family_address)
    EditText edt_set_register_family_address;

    @BindView(R.id.user_set_vip_level)
    AppCompatRatingBar user_set_vip_level;

    @BindView(R.id.show_vip_aera)
    RelativeLayout show_vip_aera;

    @BindView(R.id.show_black_people_aera)
    RelativeLayout show_black_people_aera;

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
    private final static int MSG_WHAT_REGISTER_RESULT = 7;

    private boolean isAlreadySubmit = false;// 防止重复提交
    private PopupWindow popupWindow;
    private Uri takePicTargetUri;// 相机输出结果
    private Uri cropTargetUri;
    private File keepCropFile = null;// 用来存放crop后的文件 防止onActivityResult中Intent拿到的缩略图过小
    private float mRating = -1;
    private String family;
    private GroupUserInfo registerBean; //全局

    private static final String[] CAMERA_NEED_PERMISSION = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//  Write access
            Manifest.permission.CAMERA,
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_CANCEL_DIALOG) {
                cancelDialog();
                isAlreadySubmit = true;
            } else if (msg.what == MSG_WHAT_REGISTER_RESULT) {
                int result = msg.arg1;
                showToast((String) msg.obj);
                if (result == 200) {
                    // 清空设置
//                    resetRegisterInfo();
                    Intent intent = new Intent(FaceLibRegisterActivity.this, FaceLibraryGroupActivity.class);
                    intent.putExtra("registerBean", registerBean);
                    setResult(555, intent);
                    finish();
                }
            }
        }
    };

    private void submit() {
        if (cropTargetUri == null) {
            showToast("请重新选择照片");
            return;
        }
//        if (isAlreadySubmit) {
//            showToast("请勿重复提交!");
//            return;
//        }
        final String name = edt_register_name.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            showToast("请输入姓名");
            return;
        }
        final String idCard = edt_set_register_idcard.getText().toString().trim();
        if (TextUtils.isEmpty(idCard)) {
            showToast("请输入身份证号");
            return;
        }
        final String phontNumber = edt_set_register_phone_number.getText().toString().trim();
        if (TextUtils.isEmpty(phontNumber)) {
            showToast("请输入有效电话号码");
            return;
        }
        final String familyAddress = edt_set_register_family_address.getText().toString().trim();
        if (TextUtils.isEmpty(familyAddress)) {
            showToast("请输入家庭住址");
            return;
        }

        if (GroupUserInfo.USER_TYPE_VIP.equals(family) && mRating == -1) {
            showToast("请输入VIP等级");
            return;
        }

        // 隐藏软键盘
        CommonUtil.hideKeyboard(this);

        showLoadingDialog();

        final String addFaceUrl = Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_CUSTOMERREGISTER;
        LogUtil.i(TAG, "customerRegister request url: " + addFaceUrl);
        registerBean = new GroupUserInfo();
        registerBean.img1 = Base64.encodeToString(CommonUtil.readLocalFile(keepCropFile.getAbsolutePath()), Base64.NO_WRAP);
        registerBean.name = name;
        registerBean.gender = "0";
        registerBean.idCard = idCard;
        registerBean.address = familyAddress;
        registerBean.family = family;
        registerBean.phoneNumber = phontNumber;
        registerBean.userId = (String) SharedPreferencesUtils.get(getApplicationContext(), "userId", "");
        if (GroupUserInfo.USER_TYPE_VIP.equals(family)) {
            registerBean.viplevel = String.valueOf(Math.round(mRating));
        }

        OkHttpUtil.getInstance().doAsyncOkHttpPost(addFaceUrl, new Gson().toJson(registerBean), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                LogUtil.e(TAG, "onFailure: " + e.getMessage());
                showConnectHttpError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                String json = response.body().string();
                LogUtil.i(TAG, "customerRegister response json: " + json);
                if (response.code() == Constants.REQUEST_STATUS_SUCCESSFUL) {
                    Message msg = Message.obtain();
                    msg.what = MSG_WHAT_REGISTER_RESULT;
                    FaceLibRegisterResult faceLibRegisterResult = new Gson().fromJson(json, FaceLibRegisterResult.class);
                    if (Constants.RESPONSE_STATUS_SUCCESSFUL.equals(faceLibRegisterResult.exCode)) {
                        // 仅仅用于标识注册成功
                        msg.arg1 = 200;
                        // 本地图片路径
                        registerBean.img2 = keepCropFile.getAbsolutePath();
                        // 用于操作删除编辑操作
                        registerBean.clientId = faceLibRegisterResult.clientId;
                    } else {
                        // 注册失败
                    }
                    msg.obj = faceLibRegisterResult.exMsg;
                    mHandler.sendMessage(msg);
                }
            }
        });

    }

    private void resetRegisterInfo() {
        iv_user_header.setImageBitmap(null);
        edt_register_name.getText().clear();
        edt_set_register_idcard.getText().clear();
        if ("2".equals(family)) {
            user_set_vip_level.setRating(0);
        }
    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_face_lib_register);
    }

    @Override
    protected void initialize() {
        ib_back.setVisibility(View.VISIBLE);

        tv_center_title.setText(getResources().getString(R.string.set_recoil_register));
        tv_action_end.setText(getResources().getString(R.string.set_register_finish));
        tv_action_end.setVisibility(View.VISIBLE);
        Glide.with(this).load(R.drawable.take_photo).into(iv_user_header);

        String group_name = getIntent().getExtras().getString("group_name", "");
        if (getResources().getString(R.string.user_type_vip).equals(group_name)) {
            show_vip_aera.setVisibility(View.VISIBLE);
            user_set_vip_level.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    mRating = rating;
                }
            });
            family = GroupUserInfo.USER_TYPE_VIP;
        } else if (getResources().getString(R.string.user_type_black).equals(group_name)) {
            show_black_people_aera.setVisibility(View.VISIBLE);
            family = GroupUserInfo.USER_TYPE_BLACK;
        } else if (getResources().getString(R.string.user_type_com).equals(group_name)) {
            family = GroupUserInfo.USER_TYPE_COMMON;
        }

        // 设置edt hint字体大小
        String nameHint = getResources().getString(R.string.register_name_hint);
        SpannableString nameSb = new SpannableString(nameHint);
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(12, true);
        nameSb.setSpan(sizeSpan, 0, nameSb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        edt_register_name.setHint(nameSb);

        String idCardHint = getResources().getString(R.string.register_idcard_hint);
        SpannableString idCardSb = new SpannableString(idCardHint);
        idCardSb.setSpan(sizeSpan, 0, idCardSb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        edt_set_register_idcard.setHint(idCardSb);

        String numberHint = getResources().getString(R.string.register_phone_number_hint);
        SpannableString numberSb = new SpannableString(numberHint);
        numberSb.setSpan(sizeSpan, 0, numberSb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        edt_set_register_phone_number.setHint(numberSb);

        String addressHint = getResources().getString(R.string.register_family_address_hint);
        SpannableString addressSb = new SpannableString(addressHint);
        addressSb.setSpan(sizeSpan, 0, addressSb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        edt_set_register_family_address.setHint(addressSb);

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
                LogUtil.e(TAG, "onRequestPermissionsResult: GALLERY ACCESS DENY finish !");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setMessage("该功能需要赋予存储权限,不开启将无法正常工作.")
                            .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    // 引导用户至设置页手动授权
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .create().show();
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
                LogUtil.e(TAG, "onRequestPermissionsResult: CAMERA ACCESS DENY !");
                // shouldShowRequestPermissionRationale点击不再提醒之后有效
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

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
                        apply(new RequestOptions().placeholder(R.drawable.img_loading).error(R.drawable.img_load_failed))
                        .into(iv_user_header);
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

    @Override
    protected void onDestroy() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
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
