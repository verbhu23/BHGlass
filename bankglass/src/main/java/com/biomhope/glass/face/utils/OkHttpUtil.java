package com.biomhope.glass.face.utils;

import android.util.Log;

import com.biomhope.glass.face.global.Constants;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 没有特别说明的都将是使用HTTP POST
 */
public class OkHttpUtil {

    private final String TAG = this.getClass().getSimpleName();
    private static final byte[] LOCKER = new byte[0];
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private OkHttpClient mOkHttpClient;
    private static OkHttpUtil mOkHttpUtils;
    private static final long time_out = 30;

    private OkHttpUtil() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(time_out, TimeUnit.SECONDS)
                    .writeTimeout(time_out, TimeUnit.SECONDS)
                    .readTimeout(time_out, TimeUnit.SECONDS)
                    .build();
        }
    }

    public static OkHttpUtil getInstance() {
        if (mOkHttpUtils == null) {
            synchronized (LOCKER) {
                if (mOkHttpUtils == null) {
                    mOkHttpUtils = new OkHttpUtil();
                }
            }
        }
        return mOkHttpUtils;
    }

    public void doAsyncOkHttpPost(String requestUrl, String json, Callback callback) {

        // MediaType  设置Content-Type 标头中包含的媒体类型值
        String formJson = Constants.FORM_JSON_PR + "=" + json;
//        RequestBody requestBody = FormBody.create(FORM, formJson);
        FormBody formBody = new FormBody.Builder()
                .add(Constants.FORM_JSON_PR, json)
                .build();

        Log.i(TAG, "doOkHttpPost: request json is " + " \n " + formJson);
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(formBody)
                .build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    public void doAsyncOkHttpGet(String requestUrl, Callback callback) {
        Request request = new Request.Builder()
                .url(requestUrl)
                .get()
                .build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    public SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}
