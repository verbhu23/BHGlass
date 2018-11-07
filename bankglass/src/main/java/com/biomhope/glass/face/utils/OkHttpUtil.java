package com.biomhope.glass.face.utils;

import com.biomhope.glass.face.global.Constants;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 没有特别说明的都将是使用HTTP POST
 */
public class OkHttpUtil {

    private static final String TAG = OkHttpUtil.class.getSimpleName();
    private static final byte[] LOCKER = new byte[0];
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private OkHttpClient mOkHttpClient;
    private static OkHttpUtil mOkHttpUtils;
    private static final long time_out = 10;

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

    public interface CallBack {
        void onFailure(); // 请求失败

        void onResponseSuccess(String json); // 响应成功且excode = 0

        void onResponseFailed(); // 响应成功 exCode不为0
    }

    public void doAsyncOkHttpPost(final String requestUrl, String json, final CallBack callBack) {

        // MediaType  设置Content-Type 标头中包含的媒体类型值
        String formJson = Constants.FORM_JSON_PR + "=" + json;
//        RequestBody requestBody = FormBody.create(FORM, formJson);
        FormBody formBody = new FormBody.Builder()
                .add(Constants.FORM_JSON_PR, json)
                .build();

        LogUtil.d(TAG, String.format("Url[%s]请求json = [%s]", requestUrl, formJson));
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(formBody)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e(TAG, "onFailure: " + e.getMessage());
                if (callBack != null) {
                    // 超时等情况
                    callBack.onFailure();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.body() != null) {
                    // 只能处理一次
                    String json = response.body().string();
                    LogUtil.i(TAG, String.format("Url[%s]\n response json = [%s]", requestUrl, json));
                    if (Constants.REQUEST_STATUS_SUCCESSFUL == response.code()) {
                        // response code 200
                        if (callBack != null) {
                            callBack.onResponseSuccess(json);
                        }
                    } else {
                        if (callBack != null) {
                            callBack.onResponseFailed();
                        }
                    }
                } else {
                    if (callBack != null) {
                        callBack.onResponseFailed();
                    }
                }
            }
        });
    }

    public void doAsyncOkHttpPost(String requestUrl, String json, Callback callback) {

        // MediaType  设置Content-Type 标头中包含的媒体类型值
        String formJson = Constants.FORM_JSON_PR + "=" + json;
//        RequestBody requestBody = FormBody.create(FORM, formJson);
        FormBody formBody = new FormBody.Builder()
                .add(Constants.FORM_JSON_PR, json)
                .build();

//        LogUtil.d(TAG, "doOkHttpPost: request json is " + " \n " + formJson);
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
