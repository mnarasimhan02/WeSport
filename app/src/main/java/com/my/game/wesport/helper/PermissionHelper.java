package com.my.game.wesport.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sabeeh on 01-Apr-17.
 */

public class PermissionHelper {
    public static boolean isGranted(Activity activity, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static boolean check(Activity activity, String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (!isGranted(activity, permission)) {
                    permissionsToRequest.add(permission);
                }
            }
            if (permissionsToRequest.size() > 0) {
                String[] perm = new String[permissionsToRequest.size()];
                for (int i = 0; i < permissionsToRequest.size(); i++) {
                    perm[i] = permissionsToRequest.get(i);
                }
                activity.requestPermissions(perm, requestCode);
                return false;
            }
        }
        return true;
    }
}
