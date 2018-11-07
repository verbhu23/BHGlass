package com.biomhope.glass.face.home.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.bean.LoginBean;
import com.biomhope.glass.face.bean.RegisterBean;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.home.MainActivity;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.biomhope.glass.face.utils.SharedPreferencesUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * author:BH
 * create at:2018/9/6
 * description:
 */
public class LoginActivity extends BaseActivity {

    @BindView(R.id.et_username)
    EditText et_username;

    @BindView(R.id.et_password)
    EditText et_password;

    @BindView(R.id.login_iv_showpassword)
    ImageView login_iv_showpassword;

    @BindView(R.id.btn_login)
    Button btn_login;

    @BindView(R.id.login_cb_auto_login)
    CheckBox login_cb_auto_login;

    private static final String LOGIN_USERNAME_KEY = "login_username";
    private String userName, password;
    private boolean isPasswordGone = true;
    private boolean isAutoLogin;

    private static final int msg_what_login_success_flag = 101;
    private static final int msg_what_cancel_dialog = 102;
    private static final int msg_what_result_fail = 103;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == msg_what_login_success_flag) {
                // 隐藏软键盘
                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                mainIntent.putExtra("userId", (String) msg.obj);
                // 更新本地userId
                SharedPreferencesUtils.put(getApplicationContext(), "userId", (String) msg.obj);
                startActivity(mainIntent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if (msg.what == msg_what_cancel_dialog) {
                cancelDialog();
            } else if (msg.what == msg_what_result_fail) {
                showToast((String) msg.obj);
            }

        }
    };

    private TextWatcher accountTW = new TextWatcher() {
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
    };

    private TextWatcher passwordTW = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            password = s.toString().trim();
        }
    };

    @OnClick({R.id.btn_login, R.id.login_iv_showpassword, R.id.login_cb_auto_login, R.id.signup_view, R.id.resetpw_view})
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.login_iv_showpassword:
                showPassword();
                break;
            case R.id.login_cb_auto_login:
                isAutoLogin = !isAutoLogin;
                login_cb_auto_login.setChecked(isAutoLogin);
                break;
            case R.id.signup_view:
                Intent registerIntent = new Intent(this, RegisterActivity.class);
                registerIntent.putExtra("type", 1);
                startActivity(registerIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.resetpw_view:
                Intent resetIntent = new Intent(this, RegisterActivity.class);
                resetIntent.putExtra("type", 2);
                startActivity(resetIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
        }
    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void initialize() {

        et_username.addTextChangedListener(accountTW);
        et_password.addTextChangedListener(passwordTW);

        userName = (String) SharedPreferencesUtils.get(this, LOGIN_USERNAME_KEY, "");
        if (!TextUtils.isEmpty(userName)) {
            et_username.setText(userName);
            et_password.requestFocus();
        }

    }

    @Override
    protected boolean translucentStatusBar() {
        return false;
    }

    private void login() {
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
            showToast("请输入账号或密码");
            return;
        }
        // 隐藏软键盘
        CommonUtil.hideKeyboard(this);

        showLoadingDialog();
        // 记住当前登录用户名
        SharedPreferencesUtils.put(this, LOGIN_USERNAME_KEY, userName);

        String requestUrl = Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_LOGIN;
        LogUtil.i(TAG, "login request Url: " + requestUrl);
        LogUtil.i(TAG, "账户 = " + userName + " 密码 = " + password);
        OkHttpUtil.getInstance().doAsyncOkHttpPost(requestUrl, new Gson().toJson(new LoginBean(userName, password)), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e(TAG, "onFailure: " + e.getMessage());
                handler.sendEmptyMessage(msg_what_cancel_dialog);
                showConnectHttpError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handler.sendEmptyMessage(msg_what_cancel_dialog);
                String json = response.body().string();
                LogUtil.i(TAG, "login response json: " + json);
                if (response.code() == Constants.REQUEST_STATUS_SUCCESSFUL) {
                    Message msg = Message.obtain();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String exCode = jsonObject.getString("exCode");
                        if (Constants.RESPONSE_STATUS_SUCCESSFUL.equals(exCode)) {
                            msg.what = msg_what_login_success_flag;
                            JSONObject data = jsonObject.getJSONObject("data");
                            msg.obj = data.getString("userId");
                        } else {
                            msg.what = msg_what_result_fail;
                            msg.obj = jsonObject.getString("exMsg");
                        }
                        handler.sendMessage(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    LogUtil.i(TAG, "登录请求失败");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Subscribe
    public void doLoginFromRegister(final RegisterBean registerBean) {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                userName = registerBean.userName;
                password = registerBean.password;
                et_username.setText(userName);
                et_password.setText(password);
                et_password.setSelection(password.length());
                btn_login.performClick();
            }
        }, 500);
    }

    private void showPassword() {

        if (TextUtils.isEmpty(password)) return;

        if (isPasswordGone) {
            // 切换成显示密码
            login_iv_showpassword.setImageResource(R.drawable.btn_password_visible);
            et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            // 切换成隐藏密码
            login_iv_showpassword.setImageResource(R.drawable.btn_password_gone);
            et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        et_password.setSelection(password.length());
        isPasswordGone = !isPasswordGone;

    }

    @Override
    protected boolean isEventSubscribe() {
        return true;
    }

}
