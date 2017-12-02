package com.ing.software.ocrtestapp;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

/**
 * @author M
 * Inspired by https://github.com/Fotoapparat/Fotoapparat/blob/master/sample/src/main/java/io/fotoapparat/sample/PermissionsDelegate.java
 */
public class PermissionsHandler {

    public static final int REQUEST_STORAGE_CODE = 16;
    private final Activity activity;

    public PermissionsHandler(Activity activity) {
        this.activity = activity;
    }

    public boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT < 23)
            return true;
        int permissionCheckResult = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
            );
        return permissionCheckResult == PackageManager.PERMISSION_GRANTED;
    }

    public void requestStoragePermission() {
        ActivityCompat.requestPermissions(
            activity,
            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
            REQUEST_STORAGE_CODE
            );
    }

    public boolean resultGranted(int requestCode,
                          String[] permissions,
                          int[] grantResults) {
        if (requestCode != REQUEST_STORAGE_CODE)
        {
            return false;
        }
        if (grantResults.length < 1)
        {
            return false;
        }
        if (requestCode == REQUEST_STORAGE_CODE && !( permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) ))
        {
            return false;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        if (requestCode == REQUEST_STORAGE_CODE)
            requestStoragePermission();
        return false;
    }
}
