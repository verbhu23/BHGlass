package com.biomhope.glass.face.home.login;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.bean.RegisterBean;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author $USER_NAME
 * create at : 2018-10-16
 * description :
 */
public class RegisterActivity extends BaseActivity {

    @BindView(R.id.ib_back)
    RelativeLayout ib_back;

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.et_username)
    EditText et_username;

    @BindView(R.id.et_password)
    EditText et_password;

    @BindView(R.id.register_iv_clear)
    ImageView register_iv_clear;

    @BindView(R.id.et_password_confirm)
    EditText et_password_confirm;

    @BindView(R.id.register_iv_clear_confirm)
    ImageView register_iv_clear_confirm;

    @BindView(R.id.et_password_find)
    EditText et_password_find;

    @BindView(R.id.register_iv_clear_find)
    ImageView register_iv_clear_find;

    @BindView(R.id.btn_register)
    Button btn_register;

    private String userName, passWord, confirmPassword, findPassword;
    private static final int msg_what_cancel_dialog = 1;
    private static final int msg_what_result_success = 2;
    private static final int msg_what_result_fail = 3;
    private int type;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msg_what_cancel_dialog:
                    cancelDialog();
                    break;
                case msg_what_result_fail:
                    String exMsg = (String) msg.obj;
                    showToast(exMsg);
                    break;
                case msg_what_result_success:
                    finish();
                    LogUtil.i(TAG, "注册成功");
                    RegisterBean registerBean = (RegisterBean) msg.obj;
                    EventBus.getDefault().post(registerBean);
                    break;
            }
        }
    };

    @OnClick({R.id.ib_back, R.id.btn_register, R.id.register_iv_clear, R.id.register_iv_clear_confirm, R.id.register_iv_clear_find})
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.register_iv_clear:
                et_password.getText().clear();
                break;
            case R.id.register_iv_clear_confirm:
                et_password_confirm.getText().clear();
                break;
            case R.id.register_iv_clear_find:
                et_password_find.getText().clear();
                break;
            case R.id.btn_register:
                doRegister();
                break;
        }
    }

    private void doRegister() {
        //隐藏软键盘
        CommonUtil.hideKeyboard(this);
        if (TextUtils.isEmpty(passWord) || TextUtils.isEmpty(findPassword) || TextUtils.isEmpty(confirmPassword)) {
            showToast("请输入完整信息");
            return;
        }
        if (!passWord.equals(confirmPassword)) {
            showToast("请确认密码一致");
            return;
        }

        showLoadingDialog();
        String requestUrl = Constants.REQUEST_HOST_MAIN_URL;
        if (type == 1) {//注册
            requestUrl += Constants.REQUEST_DO_REGISTER;
        } else if (type == 2) {//重置密码
            requestUrl += Constants.REQUEST_DO_RESETPASSWORD;
        }
        LogUtil.i(TAG, "requestUrl = " + requestUrl);
        final RegisterBean registerBean = new RegisterBean(userName, passWord, findPassword);
        LogUtil.i(TAG, "registerBean " + registerBean.toString());
        OkHttpUtil.getInstance().doAsyncOkHttpPost(requestUrl, new Gson().toJson(registerBean), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e(TAG, "onFailure: " + e.getMessage());
                mHandler.sendEmptyMessage(msg_what_cancel_dialog);
                showConnectHttpError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.sendEmptyMessage(msg_what_cancel_dialog);
                String json = response.body().string();
                LogUtil.i(TAG, "onResponse: json = " + json);
                if (response.code() == Constants.REQUEST_STATUS_SUCCESSFUL) {
                    Message msg = Message.obtain();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String exCode = jsonObject.getString("exCode");
                        if (Constants.RESPONSE_STATUS_SUCCESSFUL.equals(exCode)) {
                            msg.what = msg_what_result_success;
                            msg.obj = registerBean;
                        } else {
                            msg.what = msg_what_result_fail;
                            msg.obj = jsonObject.getString("exMsg");
                        }
                        mHandler.sendMessage(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    LogUtil.i(TAG, "注册请求失败");
                }
            }
        });
    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_user_register);
    }

    @Override
    protected void initialize() {

        ib_back.setVisibility(View.VISIBLE);
        type = getIntent().getIntExtra("type", 1);
        if (type == 1) {
            tv_center_title.setText(getResources().getString(R.string.btn_sign_up));
        } else if (type == 2) {
            tv_center_title.setText(getResources().getString(R.string.btn_reset_pw));
        }

        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                userName = s.toString().trim();
            }
        });
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                passWord = s.toString().trim();
                if (!TextUtils.isEmpty(passWord)) {
                    register_iv_clear.setVisibility(View.VISIBLE);
                } else {
                    register_iv_clear.setVisibility(View.GONE);
                }
            }
        });
        et_password_confirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                confirmPassword = s.toString().trim();
                if (!TextUtils.isEmpty(confirmPassword)) {
                    register_iv_clear_confirm.setVisibility(View.VISIBLE);
                } else {
                    register_iv_clear_confirm.setVisibility(View.GONE);
                }
            }
        });
        et_password_find.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                findPassword = s.toString().trim();
                if (!TextUtils.isEmpty(findPassword)) {
                    register_iv_clear_find.setVisibility(View.VISIBLE);
                } else {
                    register_iv_clear_find.setVisibility(View.GONE);
                }
            }
        });
    }

    private boolean checkBtnEnabled() {
        return !TextUtils.isEmpty(userName) && !TextUtils.isEmpty(passWord) && !TextUtils.isEmpty(confirmPassword) && !TextUtils.isEmpty(findPassword);
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }
}
