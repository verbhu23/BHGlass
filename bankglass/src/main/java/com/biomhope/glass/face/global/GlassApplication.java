package com.biomhope.glass.face.global;

import android.app.Activity;

import com.biomhope.glass.face.utils.CrashHandler;
import com.llvision.glass3.appstore.BaseApplication;
import com.llvision.glass3.sdk.util.LogUtil;

import java.util.HashSet;

/**
 * author:BH
 * create at:2018/9/6
 * description:
 */
public class GlassApplication extends BaseApplication {

    private static GlassApplication instance;

    public static GlassApplication getInstance() {
        return instance;
    }

    private HashSet<Activity> mActivities = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        LogUtil.setDebug(true);
        CrashHandler.getInstance().init(this);
//        BlockCanary.install(this, new AppContext()).start();
    }

    public void addActivity(Activity activity) {
        if (mActivities != null && !mActivities.contains(activity)) {
            mActivities.add(activity);
        }
    }

    public void removeActivity(Activity activity) {
        if (mActivities != null && mActivities.contains(activity)) {
            mActivities.remove(activity);
            activity.finish();
        }
    }

    public void exit() {
        if (mActivities != null) {

        }
    }

//    class AppContext extends BlockCanaryContext {
//
//        @Override
//        public String provideQualifier() {
//            String qualifier = "";
//            try {
//                PackageInfo info = getApplicationContext().getPackageManager()
//                        .getPackageInfo(getApplicationContext().getPackageName(), 0);
//                qualifier += info.versionCode + "_" + info.versionName + "_YYB";
//            } catch (PackageManager.NameNotFoundException e) {
//                Log.e(TAG, "provideQualifier exception", e);
//            }
//            return qualifier;
//
//        }
//
//        @Override
//        public int provideBlockThreshold() {
//            return 500;
//        }
//
//        @Override
//        public boolean displayNotification() {
//            return BuildConfig.DEBUG;
//        }
//
//        @Override
//        public boolean stopWhenDebugging() {
//            return false;
//        }
//    }

}
