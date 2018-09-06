package com.biomhope.glass.face.home.login;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import android.widget.EditText;
import android.widget.ImageView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseActivity;
import com.biomhope.glass.face.home.MainActivity;
import com.biomhope.glass.face.utils.CommonUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.et_username)
    EditText et_username;

    @BindView(R.id.et_password)
    EditText et_password;

    @BindView(R.id.login_iv_showpassword)
    ImageView login_iv_showpassword;

    @BindView(R.id.btn_login)
    Button btn_login;

    private String userName, password;
    private boolean isPasswordGone = true;
    private Dialog dialog;

    private static final int LOGIN_CODE_FLAG = 10020;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == LOGIN_CODE_FLAG) {
                if (dialog != null)
                    dialog.dismiss();
                dialog = null;

                // 隐藏软键盘
                CommonUtil.hideKeyboard(LoginActivity.this);
                CommonUtil.skipAnotherActivity(LoginActivity.this, MainActivity.class, true);
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
            btn_login.setEnabled(checkLoginBtnEnabled());
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
            btn_login.setEnabled(checkLoginBtnEnabled());
        }
    };

    @OnClick({R.id.btn_login, R.id.login_iv_showpassword})
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.login_iv_showpassword:
                showPassword();
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

        CommonUtil.setEditTextInhibitInputSpace(et_password);
        CommonUtil.setEditTextInhibitInputSpeChat(et_password);
        et_password.addTextChangedListener(passwordTW);

        String userName = "zkbh123";
        String passWord = "123456";

        if (TextUtils.isEmpty(userName)) {
            et_username.requestFocus();
            return;
        }
        et_username.setText(userName);

        if (TextUtils.isEmpty(passWord)) {
            et_password.requestFocus();
            return;
        }

        et_password.setText(passWord);
        et_password.setSelection(passWord.length());
        et_password.requestFocus();

        btn_login.performClick();
    }

    private void login() {

        if (dialog == null) {
            dialog = new Dialog(this, R.style.LoadDialogStyle);
            dialog.setContentView(R.layout.dialog_common_loading);
            dialog.setCancelable(false);
        }
        dialog.show();

        handler.sendEmptyMessageDelayed(LOGIN_CODE_FLAG, 1500);
    }

    @Override
    protected void onDestroy() {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void showPassword() {

        if (TextUtils.isEmpty(password)) return;

        if (isPasswordGone) {
            login_iv_showpassword.setImageResource(R.drawable.btn_password_visible);
        } else {
            login_iv_showpassword.setImageResource(R.drawable.btn_password_gone);
        }
        isPasswordGone = !isPasswordGone;

        if (isPasswordGone) {
            et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            et_password.setSelection(password.length());
            return;
        }
        et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        et_password.setSelection(password.length());
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

    private boolean checkLoginBtnEnabled() {
        return !TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password);
    }
}
