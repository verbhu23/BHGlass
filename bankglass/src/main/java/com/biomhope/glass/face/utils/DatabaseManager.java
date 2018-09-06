package com.biomhope.glass.face.utils;


import android.content.Context;
import android.util.Log;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.assit.SQLStatement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static final String DB_NAME = "db.sqlite3"; //保存的数据库文件名
    //    private static final String PACKAGE_NAME = "com.llvision.face.llvisiondtdemo";
//    private static final String DB_PATH = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/" + PACKAGE_NAME;
    private final int BUFFER_SIZE = 1024;
    private LiteOrm liteOrm;
    private static volatile DatabaseManager instance;

    private DatabaseManager(Context mContext, int dbResId) {
        DataBaseConfig config = new DataBaseConfig(mContext);
//        config.dbName = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/"
//                + mContext.getPackageName() + File.separator + getDBName();
        config.dbName = mContext.getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + getDBName();
//        config.dbName = "/sdcard/llvision" + File.separator + getDBName();
        config.debugged = true; //是否打Log
        config.dbVersion = 3; // database Version
        config.onUpdateListener = null; //升级

        try {
            if (!(new File(config.dbName).exists())) {
                //判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
                InputStream is = mContext.getResources().openRawResource(
                        dbResId); //欲导入的数据库
                FileOutputStream fos = new FileOutputStream(config.dbName);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.flush();
                fos.close();
                is.close();
            }
            Log.w(TAG, "Open or create database");

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IO exception");
            e.printStackTrace();
        }
        this.liteOrm = LiteOrm.newSingleInstance(config);

    }

    protected String getDBName() {
        return DB_NAME;
    }

    public static void init(Context context, int dbResId) {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager(context, dbResId);
                }
            }
        }
    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    /**
     * 插入一条记录
     *
     * @param t
     */
    public <T> long save(T t) {
        return liteOrm.save(t);
    }

    public <T> long insert(T t) {
        return liteOrm.insert(t);
    }


    /**
     * 插入所有记录
     *
     * @param list
     */
    public <T> void insertAll(List<T> list) {
        liteOrm.save(list);
    }

    /**
     * @param t
     */
    public <T> void update(T t) {
        liteOrm.update(t);
    }

    /**
     * @param list
     */
    public <T> void updateAll(List<T> list) {
        liteOrm.update(list);
    }

    /**
     * 查询所有
     *
     * @param cla
     * @return
     */
    public <T> List<T> getQueryAll(Class<T> cla) {

        QueryBuilder<T> queryBuilder = new QueryBuilder<T>(cla);
        SQLStatement statement = queryBuilder.createStatement();
        return statement.query(liteOrm.getReadableDatabase(), cla);
//        return liteOrm.query(queryBuilder);
//        return liteOrm.query(cla);
    }


    public LiteOrm getLiteOrm() {
        return liteOrm;
    }


    public <T> List<T> getQuery(QueryBuilder<T> qb) {
        return liteOrm.query(qb);
    }

    /**
     * 查询  某字段 等于 Value的值
     *
     * @param cla
     * @param field
     * @param value
     * @return
     */
    public <T> List<T> getQueryByWhere(Class<T> cla, String field, String value) {
        return liteOrm.<T>query(new QueryBuilder(cla).where(field + "=?", value));
    }

    /**
     * 查询  某字段 等于 Value的值  可以指定从1-20，就是分页
     *
     * @param cla
     * @param field
     * @param value
     * @param start
     * @param length
     * @return
     */
    public <T> List<T> getQueryByWhereLength(Class<T> cla, String field, String value, int start, int length) {
        return liteOrm.<T>query(new QueryBuilder(cla).where(field + "=?", value).limit(start, length));
    }

    /**
     * 删除一个数据
     *
     * @param t
     * @param <T>
     */
    public <T> void delete(T t) {
        liteOrm.delete(t);
    }

    /**
     * 删除一个表
     *
     * @param cla
     * @param <T>
     */
    public <T> void delete(Class<T> cla) {
        liteOrm.delete(cla);
    }

    /**
     * 删除集合中的数据
     *
     * @param list
     * @param <T>
     */
    public <T> void deleteList(List<T> list) {
        liteOrm.delete(list);
    }

    /**
     * 删除数据库
     */
    public void deleteDatabase() {
        liteOrm.deleteDatabase();
    }


}
