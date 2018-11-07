package com.biomhope.glass.face.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;

/**
 * Created by Administrator on 2016/9/20.
 */
public class PageIndicatorsView extends LinearLayout {

    private int remAll = 1;
    private int remCurrent = 0;
    private int textColor = Color.BLACK;

    public PageIndicatorsView(Context context) {
        super(context);
    }

    public PageIndicatorsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PageIndicatorsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int dip2px() {
        return (int) (2.0F * getContext().getResources().getDisplayMetrics().density + 0.5F);
    }

    public void setTextColor(int color) {
        this.textColor = color;
    }

    /**
     * 设置圆点
     *
     * @param currentPage 当前所在页
     * @param pageCount   总页数
     */
    public void setPageInfo(int currentPage, int pageCount) {
        if ((this.remCurrent == currentPage) && (this.remAll == pageCount)) {
            return;
        }
        this.remCurrent = currentPage;
        this.remAll = pageCount;
        removeAllViews();
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (pageCount < 10) {
            for (int i = 0; i < pageCount; i++) {
                ImageView imageView = new ImageView(getContext());
                imageView.setLayoutParams(layoutParams);
                imageView.setPadding(dip2px(), 0, dip2px(), 0);
                if (i != currentPage) {
                    imageView.setImageResource(R.drawable.liveview_skate_dark);
                } else {
                    imageView.setImageResource(R.drawable.liveview_skate_bright);
                }
                addView(imageView);
            }
        } else {
            TextView textView = new TextView(getContext());
            textView.setLayoutParams(layoutParams);
            if (this.textColor != Color.BLACK) {
                textView.setTextColor(textColor);
            } else {
                textView.setTextColor(Color.BLACK);
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0F);
            textView.setText(String.format("%d/%d", currentPage + 1, pageCount));
            addView(textView, 0);
        }
    }

}
