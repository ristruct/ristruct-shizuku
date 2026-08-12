package com.SzkRistruct;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import rikka.shizuku.Shizuku;

/**
 * Sketchware Pro component. Method names below match the block definitions
 * in shizuku_13_6_2.json exactly - do not rename them without updating the
 * blocks file too, since blocks call these methods by name.
 */
public class ShizukuRistruct {

    private static final int REQUEST_CODE = 20260812;

    private Context context;
    private Activity activity;

    public ShizukuRistruct(Activity activity) {
        this.activity = activity;
        this.context = activity;
        ShizukuPermissionHelper.ensureRegistered();
    }

    public ShizukuRistruct(Context context) {
        this.context = context;
        if (context instanceof Activity) {
            this.activity = (Activity) context;
        }
        ShizukuPermissionHelper.ensureRegistered();
    }

    // ---------------- Shizuku permission & status ----------------

    public boolean isShizukuInstalled() {
        try {
            context.getPackageManager().getPackageInfo("moe.shizuku.privileged.api", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public boolean isShizukuAvailable() {
        try {
            return Shizuku.pingBinder();
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean hasPermission() {
        try {
            if (!isShizukuAvailable()) return false;
            if (Shizuku.isPreV11()) return false;
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            return false;
        }
    }

    public void requestPermission() {
        try {
            if (!isShizukuAvailable()) {
                openShizukuApp();
                return;
            }
            if (Shizuku.isPreV11() || hasPermission()) return;
            Shizuku.requestPermission(REQUEST_CODE);
        } catch (Throwable t) {
            if (activity != null) {
                Toast.makeText(activity, "Shizuku permission request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** All-in-one readiness check: app installed, service running, permission granted. */
    public boolean ensureShizukuReady() {
        return isShizukuInstalled() && isShizukuAvailable() && hasPermission();
    }

    /** True once the user has answered the Shizuku permission dialog at least once this run. */
    public boolean hasPermissionResult() {
        return ShizukuPermissionHelper.hasResult();
    }

    /** Result of the most recent permission prompt. Use after requestPermission() + the user returning to your app. */
    public boolean wasPermissionGranted() {
        return ShizukuPermissionHelper.wasLastResultGranted();
    }

    public void destroy() {
        // Placeholder for releasing any listeners the block layer may have registered.
    }

    public String getShizukuMode() {
        try {
            if (!isShizukuAvailable()) return "Unknown";
            return Shizuku.getUid() == 0 ? "Root" : "ADB";
        } catch (Throwable t) {
            return "Unknown";
        }
    }

    public boolean isDeveloperModeEnabled() {
        try {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAdbEnabled() {
        try {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.ADB_ENABLED, 0) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean needsStoragePermission() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q;
    }

    public void openShizukuApp() {
        if (activity == null) return;
        Intent launch = context.getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api");
        if (launch != null) {
            activity.startActivity(launch);
            return;
        }
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=moe.shizuku.privileged.api")));
        } catch (Exception e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app")));
        }
    }

    public void openShizukuChooser() {
        if (activity == null) return;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app"));
        activity.startActivity(Intent.createChooser(intent, "Open Shizuku"));
    }

    // ---------------- Shell-backed command execution ----------------

    public boolean execCommandBool(String command) {
        return ShizukuShell.execBoolean(command);
    }

    public String execCommand(String command) {
        return ShizukuShell.execSync(command);
    }

    // ---------------- File & directory operations ----------------

    public boolean delete(String path) {
        return ShizukuShell.execBoolean("rm -rf '" + path + "'");
    }

    public boolean move(String from, String to) {
        return ShizukuShell.execBoolean("mv '" + from + "' '" + to + "'");
    }

    public boolean copy(String from, String to) {
        return ShizukuShell.execBoolean("cp -rf '" + from + "' '" + to + "'");
    }

    public boolean mkdir(String path) {
        return ShizukuShell.execBoolean("mkdir -p '" + path + "'");
    }

    public boolean touch(String path) {
        return ShizukuShell.execBoolean("touch '" + path + "'");
    }

    public boolean chmod(String mode, String path) {
        return ShizukuShell.execBoolean("chmod " + mode + " '" + path + "'");
    }

    public boolean chown(String owner, String path) {
        return ShizukuShell.execBoolean("chown " + owner + " '" + path + "'");
    }

    public boolean rename(String from, String to) {
        return move(from, to);
    }

    public boolean fileExists(String path) {
        return ShizukuShell.execSync("[ -e '" + path + "' ] && echo yes || echo no").trim().startsWith("yes");
    }

    public boolean isDirectory(String path) {
        return ShizukuShell.execSync("[ -d '" + path + "' ] && echo yes || echo no").trim().startsWith("yes");
    }

    public String readFile(String path) {
        return ShizukuShell.execSync("cat '" + path + "'");
    }

    public boolean writeFile(String content, String path) {
        String escaped = content.replace("'", "'\\''");
        return ShizukuShell.execBoolean("printf '%s' '" + escaped + "' > '" + path + "'");
    }

    public String getFilePermissions(String path) {
        return ShizukuShell.execSync("stat -c '%a' '" + path + "'").trim();
    }

    public String getFileOwner(String path) {
        return ShizukuShell.execSync("stat -c '%U:%G' '" + path + "'").trim();
    }

    public String getFileSize(String path) {
        return ShizukuShell.execSync("stat -c '%s' '" + path + "'").trim();
    }

    public boolean zip(String source, String zipFile) {
        return ShizukuShell.execBoolean(
                "cd \"$(dirname '" + source + "')\" && zip -r '" + zipFile + "' \"$(basename '" + source + "')\"");
    }

    public boolean unzip(String zipFile, String targetDir) {
        return ShizukuShell.execBoolean("unzip -o '" + zipFile + "' -d '" + targetDir + "'");
    }

    public boolean zipWithPassword(String source, String zipFile, String password) {
        return ShizukuShell.execBoolean(
                "cd \"$(dirname '" + source + "')\" && zip -r -P '" + password + "' '" + zipFile + "' \"$(basename '" + source + "')\"");
    }

    public boolean unzipWithPassword(String zipFile, String targetDir, String password) {
        return ShizukuShell.execBoolean("unzip -o -P '" + password + "' '" + zipFile + "' -d '" + targetDir + "'");
    }

    // ---------------- Asset operations ----------------

    public boolean copyAssetSmart(String assetName, String targetPath) {
        // Try a plain app-process copy first (works for app-writable paths, no Shizuku needed).
        try {
            InputStream in = context.getAssets().open(assetName);
            File outFile = new File(targetPath);
            File parent = outFile.getParentFile();
            if (parent != null) parent.mkdirs();
            FileOutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
            in.close();
            out.close();
            return true;
        } catch (Exception directCopyFailed) {
            // Fall back to a Shizuku-privileged copy for protected paths.
            try {
                File staging = new File(context.getCacheDir(), new File(targetPath).getName());
                InputStream in = context.getAssets().open(assetName);
                FileOutputStream out = new FileOutputStream(staging);
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
                in.close();
                out.close();
                return copy(staging.getAbsolutePath(), targetPath);
            } catch (Exception e) {
                return false;
            }
        }
    }

    public boolean copyAndUnzipAssetSmart(String assetName, String targetDir) {
        try {
            File staging = new File(context.getCacheDir(), new File(assetName).getName());
            if (!copyAssetSmart(assetName, staging.getAbsolutePath())) return false;
            return unzip(staging.getAbsolutePath(), targetDir);
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------- Package management (new - extends the original 43 blocks) ----------------

    public boolean installApk(String apkPath) {
        return ShizukuShell.execBoolean("pm install -r '" + apkPath + "'");
    }

    public boolean uninstallPackage(String packageName) {
        return ShizukuShell.execBoolean("pm uninstall '" + packageName + "'");
    }
}
