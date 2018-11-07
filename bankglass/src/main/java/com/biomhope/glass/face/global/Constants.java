package com.biomhope.glass.face.global;

import android.os.Build;
import android.os.Environment;

import com.biomhope.glass.face.BuildConfig;

import java.io.File;

/**
 * author:BH
 * create at:2018/9/10
 * description:
 */
public class Constants {

    private static boolean isDebugMode = false;

    // android/data/data 随应用删除而删除
    public static final String EXTERNAL_CACHE_DIR = GlassApplication.getInstance().getExternalCacheDir().getPath();

    // /0/.. 存储在手机上
    public static final String EXTERNAL_STORAGE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final String REQUEST_HOST_IP_RELEASE = "120.24.222.237";
    private static final String REQUEST_HOST_IP_DEBUG = "172.16.8.24";

    private static final String REQUEST_HOST_PORT_DEBUG = "8080";
    private static final String REQUEST_HOST_PORT_RELEASE = "8680";

    public static final String CHANNEL = "123";

    // 自动生成客户标识号前缀
    public static final String _id = "GL_";

    public static final String REQUEST_HOST_MAIN_URL = "http://" + (isDebugMode ? REQUEST_HOST_IP_DEBUG : REQUEST_HOST_IP_RELEASE) + ":" +
            (isDebugMode ? REQUEST_HOST_PORT_DEBUG : REQUEST_HOST_PORT_RELEASE)
//            + File.separator + "bioauth-face-ws"
//            + File.separator + "face"
            + File.separator + "vipreco"
            + File.separator + "appService"
            + File.separator;

    public static final String REQUEST_HOST_MAIN_URL_FACE = "http://172.16.1.227:7001"
            + File.separator + "bioauth-face-ws"
            + File.separator + "face"
            + File.separator;

    // 原图片链接
    public static final String REQUEST_IMAGE_URL = "http://172.16.1.227:7001"
            + File.separator + "bioauth-face-ws"
            + File.separator + "file"
            + File.separator + "image"
            + File.separator + "face"
            + File.separator;

    public static final String FORM_JSON_PR = "params";

    // 2.1新增人脸接口
    public static final String REQUEST_DO_ADDFACE = "addFaces";

    // 2.2人脸验证
    public static final String REQUEST_DO_CHECKPERSON = "checkPerson";

    // 2.3人脸对比
    public static final String REQUEST_DO_COMPAREFACES = "compareFaces";

    // 2.6查询某个客户是否存在人脸模板
    public static final String REQUEST_DO_EXIST = "exist";

    // 2.8人脸搜索（1vN）
    public static final String REQUEST_DO_SEARCHPERSONS = "searchPersons";

    public static final String REQUEST_DO_REGISTER = "register";
    public static final String REQUEST_DO_LOGIN = "login";
    public static final String REQUEST_DO_RESETPASSWORD = "resetPassword";
    public static final String REQUEST_DO_CUSTOMERREGISTER = "customerRegister";
    public static final String REQUEST_DO_FACERECOGNIZESDETAIL = "faceRecognizesDetail";
    public static final String REQUEST_DO_FACERECOGNIZES = "faceRecognizes";
    public static final String REQUEST_DO_GETMYCREATELIB = "getMyCreateLib";
    public static final String REQUEST_DO_GETSERVERPUSHLIB = "getServerPushLib";
    public static final String REQUEST_DO_LISTRECOGNITIONHISTORY = "listRecognitionHistory";
    public static final String REQUEST_DO_CUSTOMERUPDATE = "customerUpdate";
    public static final String REQUEST_DO_CUSTOMERDELETE = "customerDelete";

    // 请求成功
    public static final int REQUEST_STATUS_SUCCESSFUL = 200;

    // 响应成功且有数据
    public static final String RESPONSE_STATUS_SUCCESSFUL = "0";

    // 存储眼镜检测到的人脸原图文件夹
    public static final String FACE_DECECTION_PIC_SOURCE_FILE = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + "glassSource";

    // 存储眼镜检测到的人脸区域剪裁图文件夹
    public static final String FACE_DECECTION_PIC_CROP_FILE = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + "glassCrop";

    public final static String FILE_PROVIDER_AUTHORITY = "com.biomhope.glass.face.fileprovider";

}
