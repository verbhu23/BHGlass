package com.biomhope.glass.face.home.session;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.widget.PageIndicatorsView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

import butterknife.BindView;
import uk.co.senab.photoview.PhotoView;

/**
 * @author $USER_NAME
 * create at : 2018-11-05
 * description : 点击图片进入大图浏览
 */
public class LargerImageActivity extends BaseActivity {

    @BindView(R.id.view_pager)
    ViewPager view_pager;

    @BindView(R.id.page_indicator_view)
    PageIndicatorsView page_indicator_view;

    private ArrayList<Uri> urlList;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_larger_image_browse);
    }

    @Override
    protected void initialize() {

        view_pager.setAdapter(new SamplePagerAdapter(this));
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

    private class SamplePagerAdapter extends PagerAdapter {

        private Context context;

        SamplePagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return urlList == null ? 0 : urlList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            final PhotoView photoView = new PhotoView(container.getContext());
//            photoView.setImageResource(sDrawables[position]);
            Glide.with(context).load(urlList.get(position)).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    photoView.setImageDrawable(resource);
                }
            });
            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

}
