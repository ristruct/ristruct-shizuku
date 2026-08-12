package com.SzkRistruct;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import rikka.shizuku.Shizuku;

/**
 * Low level shell executor. Runs commands through Shizuku's privileged
 * process (root or adb, depending on how Shizuku was started) instead of
 * the app's own limited process.
 *
 * Shizuku's public API historically exposed Shizuku.newProcess(cmd, env, dir)
 * to spawn a process with the shell's privileges. It was later hidden from
 * the public surface but is still present in the binary, so it is called
 * here through reflection. This mirrors the approach used by most
 * Sketchware community Shizuku libraries, including the compiled
 * ShizukuShell class this project is based on.
 */
public class ShizukuShell {

    public interface OnProcessCompleteListener {
        void onComplete(boolean success, String output, int exitCode);
    }

    private static Method newProcessMethod;

    static {
        try {
            newProcessMethod = Shizuku.class.getDeclaredMethod(
                    "newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);
        } catch (Throwable t) {
            newProcessMethod = null;
        }
    }

    /**
     * Runs a command synchronously and returns combined stdout+stderr.
     * Blocks the calling thread - never call this on the UI thread.
     * Returns a string starting with "ERROR:" if the command could not run.
     */
    public static String execSync(String command) {
        StringBuilder output = new StringBuilder();
        try {
            if (newProcessMethod == null) {
                return "ERROR: Shizuku.newProcess is not available on this Shizuku version";
            }
            if (!Shizuku.pingBinder()) {
                return "ERROR: Shizuku service is not running";
            }

            String[] cmd = {"sh", "-c", command};
            Object process = newProcessMethod.invoke(null, (Object) cmd, null, null);

            InputStream in = (InputStream) process.getClass().getMethod("getInputStream").invoke(process);
            InputStream err = (InputStream) process.getClass().getMethod("getErrorStream").invoke(process);

            BufferedReader outReader = new BufferedReader(new InputStreamReader(in));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(err));

            String line;
            while ((line = outReader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = errReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = (int) process.getClass().getMethod("waitFor").invoke(process);
            if (exitCode != 0 && output.length() == 0) {
                output.append("ERROR: command exited with code ").append(exitCode);
            }
        } catch (Throwable t) {
            output.append("ERROR: ").append(t.getMessage());
        }
        return output.toString();
    }

    /** Runs a command on a background thread and delivers the result on the main thread. */
    public static void execAsync(final String command, final OnProcessCompleteListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String result = execSync(command);
                final boolean success = !result.startsWith("ERROR");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onComplete(success, result, success ? 0 : -1);
                        }
                    }
                });
            }
        }).start();
    }

    /** Convenience wrapper - true if the command ran without producing an ERROR result. */
    public static boolean execBoolean(String command) {
        return !execSync(command).startsWith("ERROR");
    }
}
