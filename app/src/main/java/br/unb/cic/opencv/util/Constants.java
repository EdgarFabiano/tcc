package br.unb.cic.opencv.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class Constants {

    public static final int GALLERY_PICK_REQUEST_CODE = 0x0064;
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private Constants() {
        throw new UnsupportedOperationException("No " + Constants.class.getSimpleName() + " instances for you!");
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean isWriteExternalStoragePermissionGranted(Context context) {
        return isPermissionGranted(context, WRITE_EXTERNAL_STORAGE);
    }



}
