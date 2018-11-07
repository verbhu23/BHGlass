package com.biomhope.glass.face.home.ocr;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseFragment;
import com.biomhope.glass.face.bean.CompareFaceBean;
import com.biomhope.glass.face.bean.CompareFaceResult;
import com.biomhope.glass.face.bean.eventvo.GlassMessage;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.utils.Base64;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.google.gson.Gson;
import com.kernal.passport.sdk.utils.Devcode;
import com.kernal.passport.sdk.utils.SharedPreferencesHelper;
import com.kernal.passportreader.sdk.CameraActivity;
import com.kernal.passportreader.sdk.RecogResultBean;

import org.greenrobot.eventbus.Subscribe;

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
 */
public class OcrFunctionFragment extends BaseFragment {

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.tv_action_end)
    TextView tv_action_end;

    @BindView(R.id.iv_show_ocr_result)
    ImageView iv_show_ocr_result;

    @BindView(R.id.iv_show_glass_result)
    ImageView iv_show_glass_result;

    @BindView(R.id.tv_is_self)
    TextView tv_is_self;

    @BindView(R.id.tv_approve_sim_pct)
    TextView tv_approve_sim_pct;

    @BindView(R.id.tv_ocr_recog_name)
    TextView tv_ocr_recog_name;

    @BindView(R.id.tv_ocr_recog_sex)
    TextView tv_ocr_recog_sex;

    @BindView(R.id.tv_ocr_recog_birth_date)
    TextView tv_ocr_recog_birth_date;

    @BindView(R.id.tv_ocr_recog_family_address)
    TextView tv_ocr_recog_family_address;

    @BindView(R.id.tv_ocr_recog_idcard)
    TextView tv_ocr_recog_idcard;

    @BindView(R.id.tv_ocr_idcard_take_photo)
    TextView tv_ocr_idcard_take_photo;

    @BindView(R.id.tv_ocr_glass_take_photo)
    TextView tv_ocr_glass_take_photo;

    @OnClick({R.id.iv_show_ocr_result, R.id.tv_action_end, R.id.tv_ocr_idcard_take_photo, R.id.tv_ocr_glass_take_photo})
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.tv_ocr_idcard_take_photo:
                requestOcrCamera();
                break;
            case R.id.tv_ocr_glass_take_photo:
                if (!isHasOcrResult) {
                    showToast("请先进行证件识别!");
                    return;
                }
                openLLCamera();
                break;
            case R.id.iv_show_ocr_result:
                break;
            case R.id.tv_action_end:
                // 假装保存 清空一切数据
                if (isCloseMatch) {
                    showLoadingDialog();
                    mHandler.sendEmptyMessageDelayed(msg_cancel_dialog, 1500);
                    tv_is_self.setText("");
                    tv_approve_sim_pct.setText("");
                    // ocr
                    tv_ocr_recog_name.setText("");
                    tv_ocr_recog_sex.setText("");
                    tv_ocr_recog_birth_date.setText("");
                    tv_ocr_recog_family_address.setText("");
                    tv_ocr_recog_idcard.setText("");
                    // image head
                    iv_show_ocr_result.setImageBitmap(null);
                    iv_show_glass_result.setImageBitmap(null);
                    isCloseMatch = false;
                } else {
                    showToast("请先完成比对!");
                }
                break;
        }
    }

    private final static int msg_get_ocr_reog_result = 1;
    private final static int msg_get_glass_reog_result = 2;

    private final static int msg_cancel_dialog = 3;
    private final static int msg_show_result_dialog = 4;
    private Dialog submitResultDialog;

    private TextView dialogTipsView;
    private String headJpgPath;
    private boolean isHasOcrResult = false; // OCR成功返回结果才能开启眼镜
    private boolean isCloseMatch = false;// 人证比对完成标识

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == msg_get_ocr_reog_result) {
                RecogResultBean resultBean = (RecogResultBean) msg.obj;
                if (resultBean != null) {
                    String recogResult = resultBean.recogResultString;
                    String exception = resultBean.exception;
                    headJpgPath = resultBean.HeadJpgPath;
                    LogUtil.i(TAG, "handleMessage: recogResult " + recogResult);
                    if (exception != null && !exception.equals("")) {
                        // 错误异常
                        LogUtil.i(TAG, "handleMessage: ocr reognize exception " + exception);
                    } else {
                        StringBuilder result = new StringBuilder();
                        String[] splite_Result = recogResult.split(",");
                        // 切割顺序 姓名 性别 民族 出生 住址 公民身份号码
                        for (String aSplite_Result : splite_Result) {
                            if (result.toString().equals("")) {
                                result = new StringBuilder(aSplite_Result + "\n");
                            } else {
                                result.append(aSplite_Result).append("\n");
                            }
                        }
                        LogUtil.i(TAG, "OCR result : " + result.toString());
                        for (int i = 0; i < splite_Result.length; i++) {
                            String aSplite_Result = splite_Result[i].substring(splite_Result[i].indexOf(":") + 1);
                            if (i == 0) {
                                tv_ocr_recog_name.setText(aSplite_Result);
                            } else if (i == 1) {
                                tv_ocr_recog_sex.setText(aSplite_Result);
                            } else if (i == 3) {
                                tv_ocr_recog_birth_date.setText(aSplite_Result);
                            } else if (i == 4) {
                                tv_ocr_recog_family_address.setText(aSplite_Result);
                            } else if (i == 5) {
                                tv_ocr_recog_idcard.setText(aSplite_Result);
                            }
                        }
                        isHasOcrResult = true;
                    }
                    if (!isHasOcrResult) return;
                    CommonUtil.loadLocalPic(context, headJpgPath, iv_show_ocr_result);
                    // OCR成功才开启眼镜
                    showToast("等待眼镜识别..");
                } else {
                    isHasOcrResult = false;
                    showToast("请重新进行证件识别!");
                }
            } else if (msg.what == msg_get_glass_reog_result) {
                String imgCropPath = (String) msg.obj;
                CommonUtil.loadLocalPic(context, imgCropPath, iv_show_glass_result);
                // 停止检测
                closeLLCamera();
                showLoadingDialog();
                compareFace(headJpgPath, imgCropPath);
            } else if (msg.what == msg_cancel_dialog) {
                cancelDialog();
            } else if (msg.what == msg_show_result_dialog) {
                showSubmitResultDialog((String) msg.obj);
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_show_ocr_result;
    }

    @Override
    protected void initialize() {
        tv_center_title.setText(context.getResources().getString(R.string.tab_ocr_function));
        tv_action_end.setText("清空");
        tv_action_end.setVisibility(View.VISIBLE);
    }

    private void showSubmitResultDialog(String result) {
        if (submitResultDialog == null) {
            submitResultDialog = new Dialog(context);
            View view = View.inflate(context, R.layout.dialog_register_result, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.width = CommonUtil.getScreenWidth(context) * 3 / 4;
            submitResultDialog.setContentView(view, layoutParams);
            submitResultDialog.setCanceledOnTouchOutside(true);
            view.findViewById(R.id.setup_btn_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitResultDialog.dismiss();
                }
            });
            dialogTipsView = view.findViewById(R.id.tv_show_tips);
        }
        dialogTipsView.setText(result);
        submitResultDialog.show();
    }

    private void compareFace(final String headJpgPath, final String keepCropImgPath) {
        LogUtil.i(TAG, "start compare idcard vs glasses.");
        // 进行比对请求
        CompareFaceBean compareFaceBean = new CompareFaceBean();
        compareFaceBean.img1 = Base64.encodeToString(CommonUtil.readLocalFile(headJpgPath), Base64.NO_WRAP);
        compareFaceBean.img2 = Base64.encodeToString(CommonUtil.readLocalFile(keepCropImgPath), Base64.NO_WRAP);
        OkHttpUtil.getInstance().doAsyncOkHttpPost(
                Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_COMPAREFACES,
                new Gson().toJson(compareFaceBean), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        mHandler.sendEmptyMessage(msg_cancel_dialog);
                        LogUtil.e(TAG, "onFailure: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showSubmitResultDialog(getResources().getString(R.string.net_status_error));
                            }
                        }, 0);
                    }

                    @Override
                    public void onResponse(final Call call, final Response response) throws IOException {
                        mHandler.sendEmptyMessage(msg_cancel_dialog);
                        String json = response.body().string();
                        LogUtil.i(TAG, "compareFaces response json: " + json);
                        Message msg = Message.obtain();
                        String diglogResult = ""; //Dialog显示比对结果
                        msg.what = msg_show_result_dialog;
                        String sim = "0";
                        String comparaResult = "否";
                        if (response.code() == Constants.REQUEST_STATUS_SUCCESSFUL) {
                            // {"exCode":"0","exMsg":"操作成功","data":{"sim":"39.35868453979492","defaultSim":"87.0"}}
                            CompareFaceResult compareFaceResult = new Gson().fromJson(json, CompareFaceResult.class);
                            if (Constants.RESPONSE_STATUS_SUCCESSFUL.equals(compareFaceResult.exCode)) {
                                CompareFaceResult.Compare data = compareFaceResult.data;
                                if (!TextUtils.isEmpty(data.defaultSim) && !TextUtils.isEmpty(data.sim)) {
                                    sim = Math.round(Float.valueOf(data.sim)) + "%";
                                    if (Float.valueOf(data.sim) > Float.valueOf(data.defaultSim)) {
                                        diglogResult = "通过 相似度" + Math.round(Float.valueOf(data.sim)) + "%";
                                        comparaResult = "是";
                                    } else {
                                        diglogResult = "不通过！非本人";
                                    }
                                }
                            } else {
                                // exCode != 0
                                // dialog显示错误结果提示
                                diglogResult = compareFaceResult.exMsg;
                            }
                        } else {
                            // code != 200
                            diglogResult = getResources().getString(R.string.net_status_error);
                        }
                        msg.obj = diglogResult;
                        mHandler.sendMessage(msg);
                        final String finalComparaResult = comparaResult;
                        final String finalSim = sim;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setCompareTextResult(finalComparaResult, finalSim);
                            }
                        }, 0);
                        isCloseMatch = true;
                    }
                });
    }

    private void setCompareTextResult(String comparaResult, String sim) {
        tv_is_self.setText(comparaResult);
        tv_approve_sim_pct.setText(sim);
    }

    @Override
    public void onPause() {
        super.onPause();
        hiddenForCloseCamera();
    }

    private void hiddenForCloseCamera() {
        try {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
            if (getCurrentPagePosition(context) == 1) {
                closeLLCamera();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "hiddenForCloseCamera: failed " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    // 开启本地相机识别身份证
    private void requestOcrCamera() {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
                context.getApplicationContext(), "nMainId", 2004));
        intent.putExtra("devcode", Devcode.devcode);
        intent.putExtra("flag", 0);
        startActivity(intent);
    }

    // 获取到OCR反馈的信息
    @Subscribe
    public void getRecogResult(RecogResultBean resultBean) {
        Message msg = Message.obtain();
        msg.what = msg_get_ocr_reog_result;
        msg.obj = resultBean;
        mHandler.sendMessage(msg);
    }

    // 获取到眼镜的信息
    @Subscribe
    public void getGlassCropImg(GlassMessage message) {
        LogUtil.i(TAG, "ocr receive msg = " + message.toString());
        if (message != null && message.position == 1) {
            Message msg = Message.obtain();
            msg.what = msg_get_glass_reog_result;
            msg.obj = message.imgCropPath;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    protected boolean isEventSubscribe() {
        return true;
    }
}
