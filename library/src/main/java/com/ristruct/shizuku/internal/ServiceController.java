package com.ristruct.shizuku.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Handler;

import com.ristruct.shizuku.RistructError;
import com.ristruct.shizuku.RistructResult;
import com.ristruct.shizuku.command.RistructCommand;
import com.ristruct.shizuku.command.RistructCommandCallback;
import com.ristruct.shizuku.command.RistructCommandResult;
import com.ristruct.shizuku.service.IRistructUserService;
import com.ristruct.shizuku.service.RistructServiceCallback;
import com.ristruct.shizuku.service.RistructUserService;

import rikka.shizuku.Shizuku;

public final class ServiceController {
    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ServiceConnection connection;
    private volatile IRistructUserService service;
    private volatile boolean bound;
    private Shizuku.UserServiceArgs args;
    private RistructServiceCallback callback;

    public ServiceController(Context context) {
        this.context = context.getApplicationContext();
        this.connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                if (binder == null || !binder.pingBinder()) {
                    service = null;
                    notifyError(new IllegalStateException("UserService binder is invalid"));
                    return;
                }
                service = IRistructUserService.Stub.asInterface(binder);
                notifyConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                service = null;
                bound = false;
                notifyDisconnected();
            }
        };
    }

    public synchronized RistructResult<Void> bind(RistructServiceCallback cb) {
        this.callback = cb;
        if (!Shizuku.pingBinder()) {
            return RistructResult.failure(RistructError.SHIZUKU_UNAVAILABLE, "Shizuku binder unavailable");
        }
        if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return RistructResult.failure(RistructError.PERMISSION_REQUIRED, "Shizuku permission is required");
        }
        if (bound && service != null) {
            notifyConnected();
            return RistructResult.success(null);
        }
        args = new Shizuku.UserServiceArgs(new ComponentName(context, RistructUserService.class))
                .daemon(false)
                .processNameSuffix("ristruct")
                .debuggable(false)
                .version(1)
                .tag("com.ristruct.shizuku.user-service");
        try {
            Shizuku.bindUserService(args, connection);
            bound = true;
            return RistructResult.success(null);
        } catch (Throwable t) {
            bound = false;
            return RistructResult.failure(RistructError.SERVICE_BIND_FAILED, "Failed to bind UserService", t);
        }
    }

    public synchronized void unbind() {
        if (args == null) {
            return;
        }
        try {
            Shizuku.unbindUserService(args, connection, true);
        } catch (Throwable ignored) {
        } finally {
            service = null;
            bound = false;
        }
    }

    public boolean isConnected() {
        return service != null;
    }

    public void execute(RistructCommand command, RistructCommandCallback callback) {
        if (command == null) {
            if (callback != null) callback.onComplete(new RistructCommandResultProxy(false).toPublic(RistructError.INVALID_ARGUMENT, "command == null"));
            return;
        }
        final IRistructUserService remote = service;
        if (remote == null) {
            if (callback != null) {
                final RistructCommandResult result = new RistructCommandResultProxy(false).toPublic(RistructError.SERVICE_DISCONNECTED, "UserService is not connected");
                main.post(new Runnable() { @Override public void run() { callback.onComplete(result); } });
            }
            return;
        }
        Threading.execute(new Runnable() {
            @Override public void run() {
                try {
                    Bundle bundle = remote.execute(command.getCommand(), command.getTimeoutMs());
                    final RistructCommandResult result = RistructCommandResult.fromBundle(bundle);
                    main.post(new Runnable() { @Override public void run() { if (callback != null) callback.onComplete(result); } });
                } catch (final Throwable t) {
                    main.post(new Runnable() { @Override public void run() { if (callback != null) callback.onComplete(new RistructCommandResultProxy(false).toPublic(RistructError.COMMAND_FAILED, t.toString())); } });
                }
            }
        });
    }

    private void notifyConnected() {
        final RistructServiceCallback cb = callback;
        if (cb != null) main.post(new Runnable() { @Override public void run() { cb.onConnected(); } });
    }
    private void notifyDisconnected() {
        final RistructServiceCallback cb = callback;
        if (cb != null) main.post(new Runnable() { @Override public void run() { cb.onDisconnected(); } });
    }
    private void notifyError(final Throwable t) {
        final RistructServiceCallback cb = callback;
        if (cb != null) main.post(new Runnable() { @Override public void run() { cb.onError(t); } });
    }

    // Small adapter because RistructCommandResult deliberately exposes only public construction from Bundle.
    private static final class RistructCommandResultProxy {
        private final boolean success;
        RistructCommandResultProxy(boolean success) { this.success = success; }
        RistructCommandResult toPublic(RistructError e, String msg) {
            android.os.Bundle b = new android.os.Bundle();
            b.putBoolean("success", success);
            b.putInt("exitCode", -1);
            b.putString("stdout", "");
            b.putString("stderr", "");
            b.putString("error", msg);
            b.putBoolean("timedOut", false);
            return RistructCommandResult.fromBundle(b);
        }
    }
}
