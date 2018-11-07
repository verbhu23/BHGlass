package com.biomhope.glass.face.home.session;

import android.net.Uri;
import android.os.Bundle;

import com.biomhope.glass.face.global.BaseFragment;

import java.io.Serializable;

/**
 * @author $USER_NAME
 * create at : 2018-11-05
 * description :
 */
public class PictureSlideFragment extends BaseFragment {

    private Uri imgaeUri;

    public static PictureSlideFragment newInstance(Uri uri) {
        PictureSlideFragment f = new PictureSlideFragment();

        Bundle args = new Bundle();
        args.putSerializable("uri", (Serializable) uri);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        imgaeUri = (Uri) getArguments().get("uri");
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }
}
