package com.ristruct.shizuku;

import android.os.Build;

public final class RistructDiagnostic {
    private final int androidApi;
    private final String manufacturer;
    private final String model;
    private final boolean available;
    private final boolean binderAlive;
    private final boolean permissionGranted;
    private final int uid;
    private final RistructBackend backend;

    public RistructDiagnostic(boolean available, boolean binderAlive, boolean permissionGranted, int uid, RistructBackend backend) {
        this.androidApi = Build.VERSION.SDK_INT;
        this.manufacturer = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER;
        this.model = Build.MODEL == null ? "" : Build.MODEL;
        this.available = available;
        this.binderAlive = binderAlive;
        this.permissionGranted = permissionGranted;
        this.uid = uid;
        this.backend = backend;
    }

    public int getAndroidApi() { return androidApi; }
    public String getManufacturer() { return manufacturer; }
    public String getModel() { return model; }
    public boolean isAvailable() { return available; }
    public boolean isBinderAlive() { return binderAlive; }
    public boolean isPermissionGranted() { return permissionGranted; }
    public int getUid() { return uid; }
    public RistructBackend getBackend() { return backend; }
}
