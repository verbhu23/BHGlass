package com.biomhope.glxssbhope.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    protected final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // no title
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(savedInstanceState);
        ButterKnife.bind(this);
        if (isEventSubscribe())
            EventBus.getDefault().register(this);
        initialize();
    }

    protected abstract void setContentView(Bundle savedInstanceState);

    protected abstract void initialize();

    protected abstract boolean isEventSubscribe();

    public void showToast(int time, String content) {
        Toast.makeText(this, content, time).show();
    }

    @Override
    protected void onDestroy() {

//		ButterKnife.unbind(this);
        if (isEventSubscribe())
            EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
