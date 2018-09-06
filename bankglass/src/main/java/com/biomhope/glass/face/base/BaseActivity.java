package com.biomhope.glass.face.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
/**
 * author:BH
 * create at:2018/9/6
 * description:
 *
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected final String TAG = getClass().getSimpleName();
    protected Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(savedInstanceState);
        ButterKnife.bind(this);
        if (isEventSubscribe())
            EventBus.getDefault().register(this);
        initialize();
    }

    protected abstract void setContentView(Bundle savedInstanceState);

    protected abstract void initialize();

    protected abstract boolean isEventSubscribe();

    protected void showToast(String content) {
        showToast(Toast.LENGTH_SHORT, content);
    }

    @SuppressLint("ShowToast")
    protected void showToast(int time, String content) {
        if (mToast == null) {
            mToast = Toast.makeText(this, content, time);
        } else {
            mToast.setText(content);
        }
        mToast.show();
    }

    @Override
    protected void onDestroy() {

//		ButterKnife.unbind(this);
        if (isEventSubscribe())
            EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
