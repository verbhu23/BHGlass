package com.biomhope.glass.face.utils;

import com.biomhope.glass.face.bean.Item;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.llvision.face.dtr.model.IdentificationLoader;
import com.llvision.glass3.sdk.util.LogUtil;

import java.util.List;

/**
 * Created by liuhui on 2018/3/30.
 */

public class LocalFaceUtils {
    private volatile static LocalFaceUtils shareUtils;

    private LocalFaceUtils() {
    }

    public static LocalFaceUtils getInstance() {
        if (shareUtils == null) {
            synchronized (LocalFaceUtils.class) {
                if (shareUtils == null) {
                    shareUtils = new LocalFaceUtils();
                }
            }
        }
        return shareUtils;
    }

    public synchronized void syncFeatures() {
        List<Item> list = DatabaseManager.getInstance().getLiteOrm().query(new QueryBuilder<Item>(Item.class).columns(new String[]{"feature"}));
        String sql1 = "SELECT id,\n" +
                "       md5,\n" +
                "       feature,\n" +
                "       name,\n" +
                "       IDcard,\n" +
                "       mobile,\n" +
                "       img_blob,\n" +
                "       img_dir\n" +
                "  FROM gen_feature_iddatabase ";
//        ArrayList<Item> list = (ArrayList<Item>) DatabaseManager.getInstance().query(sql1);
        LogUtil.w("syncFeatures size = " + list.size());
        byte[] features = new byte[1048576];
        int size = Math.min(list.size(), 1024);

        for (int i = 0; i < size; ++i) {
            System.arraycopy((list.get(i)).feature, 0, features, 1024 * i, 1024);
        }
        IdentificationLoader.getInstance().loadFeatures(features, size);
    }

}
