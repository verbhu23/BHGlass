package com.biomhope.glass.face.global;

import android.os.Environment;

import java.io.File;

public class Constants {

    // android/data/data 随应用删除而删除
    public static final String EXTERNAL_CACHE_DIR = GlassApplication.getInstance().getExternalCacheDir().getPath();

    // /0/.. 存储在手机上
    public static final String EXTERNAL_STORAGE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath();

//    public static final String REQUEST_HOST_IP = "120.24.222.237";
    public static final String REQUEST_HOST_IP = "172.16.8.100";

//    public static final String REQUEST_HOST_PORT = "8080";
    public static final String REQUEST_HOST_PORT = "7001";

    public static final String CHANNEL = "123";

    // 自动生成客户标识号前缀
    public static final String _id = "GL_";

    public static final String REQUEST_HOST_MAIN_URL = "http://" + REQUEST_HOST_IP + ":" + REQUEST_HOST_PORT
            + File.separator + "bioauth-face-ws"
            + File.separator + "face"
            + File.separator;

    public static final String REQUEST_IMAGE_URL = "http://" + REQUEST_HOST_IP + ":" + REQUEST_HOST_PORT
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

    // http request successful code
    public static final int REQUEST_STATUS_SUCCESSFUL = 200;

    // http response successful code
    public static final String RESPONSE_STATUS_SUCCESSFUL = "0";

    // 存储眼镜检测到的人脸原图文件夹
    public static final String FACE_DECECTION_PIC_SOURCE_FILE = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + "zkbhGlassSource";

    // 存储眼镜检测到的人脸区域剪裁图文件夹
    public static final String FACE_DECECTION_PIC_CROP_FILE = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + "zkbhGlassCrop";

    public final static String FILE_PROVIDER_AUTHORITY = "com.biomhope.glass.face.fileprovider";

}
