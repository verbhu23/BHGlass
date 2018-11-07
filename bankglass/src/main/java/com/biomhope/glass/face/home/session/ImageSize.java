package com.biomhope.glass.face.home.session;

import android.net.Uri;

/**
 * @author $USER_NAME
 * create at : 2018-11-02
 * description :
 */
public class ImageSize {

    public int width;
    public int height;
    public Uri uri; // 拍照或者相机选的图

    @Override
    public String toString() {
        return "ImageSize{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
