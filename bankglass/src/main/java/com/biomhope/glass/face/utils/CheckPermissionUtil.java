package com.biomhope.glass.face.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class CheckPermissionUtil {
    // 返回true说明有权限没通过
    // When inspecting permissions, system permission set can be judged.
    public static boolean permissionSet(Context context, String... permissions) {
        for (String permission : permissions) {
            if (isLackPermission(context, permission)) {//To see if all permission sets can be added.
                return true;
            }
        }
        return false;
    }

    // To insepct the system perssion is to judge if the current situation has enough permission( PERMISSION_DENIED:the permission is sufficient or not).
    private static boolean isLackPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context.getApplicationContext(), permission) == PackageManager.PERMISSION_DENIED;
    }
}
