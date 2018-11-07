package com.biomhope.glass.face.home.session.audio;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.utils.CommonUtil;
import com.bumptech.glide.Glide;

public class DialogManager {

    private Dialog mDialog;

    private ImageView dm_iv_bg;
    private TextView tipsTxt;
    private Context mContext;

    DialogManager(Context context) {
        mContext = context;
    }

    public void showRecordingDialog() {
        if (mDialog == null) {
            mDialog = new Dialog(mContext, R.style.Theme_audioDialog);
            View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_manager, null);
            mDialog.setContentView(view);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(true);
            dm_iv_bg = view.findViewById(R.id.dm_iv_bg);
            tipsTxt = view.findViewById(R.id.dm_tv_txt);

            Window dialogWindow = mDialog.getWindow();
//            WindowManager.LayoutParams lp =  new WindowManager.LayoutParams(
//                    WindowManager.LayoutParams.WRAP_CONTENT| WindowManager.LayoutParams.TYPE_SYSTEM_ERROR|
//                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| PixelFormat.TRANSPARENT);
//            lp.type = WindowManager.LayoutParams.TYPE_TOAST;
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            // 处理Dialog抢夺焦点无法触发长按bug
            // WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE是关键！！！！！
            lp.flags =  WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ;
            lp.height = lp.width = CommonUtil.dp2px(mContext, 180);
            dialogWindow.setAttributes(lp);
        }
//        Glide.with(mContext).load(R.drawable.yuyin_voice_1).into(dm_iv_bg);
        mDialog.show();
    }

    /**
     * 设置正在录音时的dialog界面
     */
    public void recording() {
        if (mDialog != null && mDialog.isShowing()) {
            Glide.with(mContext).load(R.drawable.yuyin_voice_1).into(dm_iv_bg);
            tipsTxt.setText(R.string.up_for_cancel);
            tipsTxt.setBackgroundResource(0);
        }
    }

    /**
     * 取消界面
     */
    public void wantToCancel() {
        if (mDialog != null && mDialog.isShowing()) {
            Glide.with(mContext).load(R.drawable.yuyin_cancel).into(dm_iv_bg);
            tipsTxt.setText(R.string.want_to_cancle);
            tipsTxt.setBackgroundColor(mContext.getResources().getColor(R.color.cancel_send_voice));
        }
    }

    // 时间过短
    public void tooShort() {
        if (mDialog != null && mDialog.isShowing()) {
            Glide.with(mContext).load(R.drawable.yuyin_gantanhao).into(dm_iv_bg);
            tipsTxt.setText(R.string.time_too_short);
            tipsTxt.setBackgroundResource(0);
        }

    }

    // 隐藏dialog
    public void dimissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
//            mDialog = null;
        }
    }

    // 更新声音 但是在想取消的时候忽略掉
    public void updateVoiceLevel(int level) {
        if (level > 0 && level < 6) {
            // 1 2 3 4 5
        } else {
            level = 5;
        }
        if (mDialog != null && mDialog.isShowing()) {
            // 通过level来找到图片的id，也可以用switch来寻址，但是代码可能会比较长
            int resId = mContext.getResources().getIdentifier("yuyin_voice_" + level,
                    "drawable", mContext.getPackageName());
            Glide.with(mContext).load(resId).into(dm_iv_bg);
        }

    }

    public TextView getTipsTxt() {
        return tipsTxt;
    }

    public void setTipsTxt(TextView tipsTxt) {
        this.tipsTxt = tipsTxt;
    }
}
