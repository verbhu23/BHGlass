package com.biomhope.glass.face.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {

    private static final int MIN_CLICK_DELAY_TIME = 500;

    private static long lastClickTime = 0;

    private static final String mills2Sting = "yyyy-MM-dd HH:mm:ss";
    private static final int IO_BUFFER_SIZE = 1024; // 1kb

    /**
     * 判断是否为连续点击事件
     */
    public static boolean isFastDoubleClick() {

        long time = System.currentTimeMillis();
        long temp = time - lastClickTime;

        if (temp > 0 && temp < MIN_CLICK_DELAY_TIME) {
            return true;
        }

        lastClickTime = time;
        return false;
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatMills(long time) {
        Date date = new Date(time);
        return new SimpleDateFormat(mills2Sting).format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatMills(String format, long time) {
        Date date = new Date(time);
        return new SimpleDateFormat(format).format(date);
    }

    public static byte[] readLocalFile(String filePath) {
        BufferedInputStream bufferedInputStream;
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
            byteArrayOutputStream = new ByteArrayOutputStream(IO_BUFFER_SIZE);
            byte[] temp = new byte[IO_BUFFER_SIZE];
            int len;
            while ((len = bufferedInputStream.read(temp)) != -1) {
                byteArrayOutputStream.write(temp, 0, len);
            }
            bufferedInputStream.close();
//            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] input2byte(InputStream inStream) {
        BufferedInputStream bufferedInputStream;
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            bufferedInputStream = new BufferedInputStream(inStream);
            byteArrayOutputStream = new ByteArrayOutputStream(IO_BUFFER_SIZE);
            byte[] temp = new byte[IO_BUFFER_SIZE];
            int len;
            while ((len = bufferedInputStream.read(temp)) != -1) {
                byteArrayOutputStream.write(temp, 0, len);
            }
            bufferedInputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 自动生成服务端所需id
    @SuppressLint("SimpleDateFormat")
    public static String autoLongCpid() {
        return Constants._id + new SimpleDateFormat(mills2Sting).format(new Date(System.currentTimeMillis()));
    }

    // 截取图片中的时间段字符串
    // /storage/emulated/0/zkbhGlassCrop/2018-08-13 15:35:56_crop.png
    // /storage/emulated/0/zkbhGlassCrop/2018-08-13 15:35:56_source.png
    public static String getTimeStr(String imgPath) {
        String targetStr = "";
        if (TextUtils.isEmpty(imgPath))
            return targetStr;
        try {
            int underLineIndex = imgPath.indexOf("_");
//            int dotIndex = imgPath.indexOf(".");
            targetStr = imgPath.substring(0, underLineIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetStr;
    }

    public static String getSourceFaceImgPath(String cropImgPath) {
        if (TextUtils.isEmpty(cropImgPath)) return cropImgPath;
        int firstPIndex = cropImgPath.indexOf("p");
        int underLineIndex = cropImgPath.indexOf("_");
        String subTimeStr = cropImgPath.substring(firstPIndex + 2, underLineIndex);
        File file = new File(Constants.FACE_DECECTION_PIC_SOURCE_FILE);
        if (file.exists()) {
            String[] list = file.list();
            if (list != null && list.length > 0) {
                for (String aList : list) {
                    if (aList.contains(subTimeStr)) {
                        return aList;
                    }
                }
            }
        }
        return "";
    }

    /**
     * 获得屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(outMetrics);
        }
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(outMetrics);
        }
        return outMetrics.heightPixels;
    }

    /**
     * 禁止EditText输入空格
     */
    public static void setEditTextInhibitInputSpace(EditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.equals(" ")) return "";
                else return null;
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }

    /**
     * 禁止EditText输入特殊字符
     */
    public static void setEditTextInhibitInputSpeChat(EditText editText) {

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String speChat = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
                Pattern pattern = Pattern.compile(speChat);
                Matcher matcher = pattern.matcher(source.toString());
                if (matcher.find()) return "";
                else return null;
            }
        };
        editText.setFilters(new InputFilter[]{filter});

    }

    /**
     * 隐藏软键盘
     */
    public static void hideKeyboard(Activity context) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转activity
     */
    public static void skipAnotherActivity(Activity activity,
                                           Class<? extends Activity> cls) {
        skipAnotherActivity(activity, cls, false);
    }

    public static void skipAnotherActivity(Activity activity,
                                           Class<? extends Activity> cls, boolean shouFinish) {
        Intent intent = new Intent(activity, cls);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if (shouFinish)
            activity.finish();
    }

    public static void showRequestPermissionDialog(final String requestMsg, final Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(requestMsg)
                .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        // 引导用户至设置页手动授权
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activity.getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        activity.startActivity(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create().show();
    }

    public static void loadBitmap(Context context, String imgPath, boolean isFile, int placeResourceId, ImageView imageView) {
        if (isFile) {
            imgPath = "file://" + imgPath;
        }
        Glide.with(context)
                .load(imgPath)
                .transition(new DrawableTransitionOptions().crossFade(250))
                .apply(new RequestOptions().bitmapTransform(new CircleCrop()).placeholder(placeResourceId))
                .into(imageView);
    }

}