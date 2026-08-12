package com.SzkRistruct;

import android.content.pm.PackageManager;

import rikka.shizuku.Shizuku;

/**
 * Wraps Shizuku's request-permission-result listener so it is registered
 * exactly once no matter how many ShizukuRistruct instances get created
 * (e.g. one per Activity). Without this, calling `new ShizukuRistruct(...)`
 * on every screen would each try to add its own listener, and Shizuku does
 * not de-duplicate those - they'd pile up and fire multiple times per grant.
 *
 * hasPermission() on ShizukuMaster/ShizukuRistruct itself already reflects
 * the live state correctly via Shizuku.checkSelfPermission(), so this
 * listener is not required for that call to work. What it adds is a single
 * source of truth for "was permission just granted or denied", which is
 * what you need if you want to react right after the user answers the
 * system dialog instead of polling hasPermission() again later.
 */
public final class ShizukuPermissionHelper {

    private static boolean listenerRegistered = false;
    private static volatile boolean lastResultGranted = false;
    private static volatile boolean hasResult = false;

    private static final Shizuku.OnRequestPermissionResultListener LISTENER =
            new Shizuku.OnRequestPermissionResultListener() {
                @Override
                public void onRequestPermissionResult(int requestCode, int grantResult) {
                    hasResult = true;
                    lastResultGranted = grantResult == PackageManager.PERMISSION_GRANTED;
                }
            };

    private ShizukuPermissionHelper() {}

    /** Safe to call as many times as you like - registers the underlying Shizuku listener only once. */
    public static synchronized void ensureRegistered() {
        if (listenerRegistered) return;
        try {
            Shizuku.addRequestPermissionResultListener(LISTENER);
            listenerRegistered = true;
        } catch (Throwable ignored) {
            // Shizuku not on the classpath at runtime / pre-API-11 build - safe to skip.
        }
    }

    /** True once at least one permission request has come back with a result. */
    public static boolean hasResult() {
        return hasResult;
    }

    /** Result of the most recent permission request. False if none has happened yet. */
    public static boolean wasLastResultGranted() {
        return lastResultGranted;
    }
}
