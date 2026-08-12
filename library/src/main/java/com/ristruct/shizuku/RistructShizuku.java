package com.ristruct.shizuku;

import android.content.Context;
import android.content.pm.PackageManager;

import com.ristruct.shizuku.command.RistructCommand;
import com.ristruct.shizuku.command.RistructCommandCallback;
import com.ristruct.shizuku.internal.ServiceController;

import rikka.shizuku.Shizuku;

public final class RistructShizuku {
    public static final int LIBRARY_VERSION_CODE = 1;
    public static final String LIBRARY_VERSION_NAME = "1.0.0";
    public static final int PERMISSION_REQUEST_CODE = 19051;

    private static volatile RistructListener listener;
    private static volatile boolean initialized;
    private static ServiceController serviceController;

    private static final Shizuku.OnRequestPermissionResultListener PERMISSION_LISTENER =
            new Shizuku.OnRequestPermissionResultListener() {
                @Override
                public void onRequestPermissionResult(int requestCode, int grantResult) {
                    if (requestCode != PERMISSION_REQUEST_CODE) return;
                    RistructListener l = listener;
                    if (l != null) l.onPermissionResult(grantResult == PackageManager.PERMISSION_GRANTED);
                }
            };

    private static final Shizuku.OnBinderReceivedListener BINDER_RECEIVED_LISTENER =
            new Shizuku.OnBinderReceivedListener() {
                @Override
                public void onBinderReceived() {
                    RistructListener l = listener;
                    if (l != null) l.onBinderReceived();
                }
            };

    private static final Shizuku.OnBinderDeadListener BINDER_DEAD_LISTENER =
            new Shizuku.OnBinderDeadListener() {
                @Override
                public void onBinderDead() {
                    RistructListener l = listener;
                    if (l != null) l.onBinderDead();
                }
            };

    private RistructShizuku() {}

    public static synchronized void initialize(Context context) {
        initialize(context, null);
    }

    public static synchronized void initialize(Context context, RistructListener callback) {
        if (context == null) throw new IllegalArgumentException("context == null");
        listener = callback;
        if (initialized) return;
        initialized = true;
        serviceController = new ServiceController(context.getApplicationContext());
        Shizuku.addBinderReceivedListener(BINDER_RECEIVED_LISTENER);
        Shizuku.addBinderDeadListener(BINDER_DEAD_LISTENER);
        Shizuku.addRequestPermissionResultListener(PERMISSION_LISTENER);
        if (!isAvailable()) {
            RistructListener l = listener;
            if (l != null) l.onUnavailable();
        }
    }

    public static void setListener(RistructListener callback) {
        listener = callback;
    }

    public static boolean isAvailable() {
        try {
            return Shizuku.pingBinder();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isBinderAlive() { return isAvailable(); }

    public static boolean hasPermission() {
        if (!isAvailable()) return false;
        try {
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean shouldShowPermissionRationale() {
        if (!isAvailable()) return false;
        try { return Shizuku.shouldShowRequestPermissionRationale(); }
        catch (Throwable ignored) { return false; }
    }

    public static void requestPermission() {
        if (!isAvailable()) {
            RistructListener l = listener;
            if (l != null) l.onUnavailable();
            return;
        }
        if (hasPermission()) {
            RistructListener l = listener;
            if (l != null) l.onPermissionResult(true);
            return;
        }
        try {
            Shizuku.requestPermission(PERMISSION_REQUEST_CODE);
        } catch (Throwable t) {
            RistructListener l = listener;
            if (l != null) l.onPermissionResult(false);
        }
    }

    public static int getUid() {
        if (!isAvailable()) return -1;
        try { return Shizuku.getUid(); }
        catch (Throwable ignored) { return -1; }
    }

    public static RistructBackend getBackend() {
        int uid = getUid();
        if (uid < 0) return RistructBackend.UNAVAILABLE;
        if (uid == 0) return RistructBackend.ROOT;
        if (uid == 2000) return RistructBackend.ADB_SHELL;
        return RistructBackend.UNKNOWN;
    }

    public static RistructState getState() {
        if (android.os.Build.VERSION.SDK_INT < 24) return RistructState.UNSUPPORTED;
        if (!isAvailable()) return RistructState.UNAVAILABLE;
        if (!hasPermission()) return RistructState.CONNECTED_NO_PERMISSION;
        return RistructState.READY;
    }

    public static boolean isReady() { return getState() == RistructState.READY; }
    public static boolean isRoot() { return getBackend() == RistructBackend.ROOT; }
    public static boolean isShell() { return getBackend() == RistructBackend.ADB_SHELL; }

    public static RistructDiagnostic getDiagnostic() {
        boolean available = isAvailable();
        boolean permission = hasPermission();
        int uid = getUid();
        return new RistructDiagnostic(available, available, permission, uid, getBackend());
    }

    public static synchronized RistructResult<Void> bindUserService(com.ristruct.shizuku.service.RistructServiceCallback callback) {
        if (serviceController == null) {
            return RistructResult.failure(RistructError.INTERNAL_ERROR, "RistructShizuku.initialize(context) has not been called");
        }
        return serviceController.bind(callback);
    }

    public static boolean isUserServiceConnected() {
        return serviceController != null && serviceController.isConnected();
    }

    public static void execute(RistructCommand command, RistructCommandCallback callback) {
        if (serviceController == null) {
            if (callback != null) {
                android.os.Bundle b = new android.os.Bundle();
                b.putBoolean("success", false); b.putInt("exitCode", -1); b.putString("stdout", ""); b.putString("stderr", "");
                b.putString("error", "RistructShizuku is not initialized"); b.putBoolean("timedOut", false);
                callback.onComplete(com.ristruct.shizuku.command.RistructCommandResult.fromBundle(b));
            }
            return;
        }
        serviceController.execute(command, callback);
    }

    public static synchronized void unbindUserService() {
        if (serviceController != null) serviceController.unbind();
    }

    public static synchronized void destroy() {
        try { Shizuku.removeBinderReceivedListener(BINDER_RECEIVED_LISTENER); } catch (Throwable ignored) {}
        try { Shizuku.removeBinderDeadListener(BINDER_DEAD_LISTENER); } catch (Throwable ignored) {}
        try { Shizuku.removeRequestPermissionResultListener(PERMISSION_LISTENER); } catch (Throwable ignored) {}
        if (serviceController != null) serviceController.unbind();
        serviceController = null;
        listener = null;
        initialized = false;
    }
}
