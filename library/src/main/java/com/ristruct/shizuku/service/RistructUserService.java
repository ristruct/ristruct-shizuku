package com.ristruct.shizuku.service;

import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RistructUserService extends IRistructUserService.Stub {

    private static final int DESTROY_TRANSACTION = 16777115;

    public RistructUserService() {
    }

    @Override
    public Bundle execute(String command, long timeoutMs) throws RemoteException {
        Bundle out = new Bundle();
        if (command == null || command.trim().isEmpty()) {
            out.putBoolean("success", false);
            out.putInt("exitCode", -1);
            out.putString("stdout", "");
            out.putString("stderr", "");
            out.putString("error", "command must not be empty");
            out.putBoolean("timedOut", false);
            return out;
        }
        if (timeoutMs <= 0L) {
            timeoutMs = 30000L;
        }

        Process process = null;
        StreamCollector stdout = null;
        StreamCollector stderr = null;
        try {
            process = new ProcessBuilder("sh", "-c", command)
                    .redirectErrorStream(false)
                    .start();

            stdout = new StreamCollector(process.getInputStream());
            stderr = new StreamCollector(process.getErrorStream());
            Thread stdoutThread = new Thread(stdout, "ristruct-stdout");
            Thread stderrThread = new Thread(stderr, "ristruct-stderr");
            stdoutThread.start();
            stderrThread.start();

            long deadline = System.nanoTime() + timeoutMs * 1000000L;
            int exitCode = Integer.MIN_VALUE;
            boolean timedOut = false;

            while (System.nanoTime() < deadline) {
                try {
                    exitCode = process.exitValue();
                    break;
                } catch (IllegalThreadStateException ignored) {
                    try {
                        Thread.sleep(25L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        if (process != null) {
                            process.destroy();
                        }
                        out.putBoolean("success", false);
                        out.putInt("exitCode", -1);
                        out.putString("stdout", stdout.getText());
                        out.putString("stderr", stderr.getText());
                        out.putString("error", "cancelled");
                        out.putBoolean("timedOut", false);
                        return out;
                    }
                }
            }

            if (exitCode == Integer.MIN_VALUE) {
                timedOut = true;
                process.destroy();
                long killWait = System.nanoTime() + 1000000000L;
                while (System.nanoTime() < killWait) {
                    try {
                        exitCode = process.exitValue();
                        break;
                    } catch (IllegalThreadStateException ignored) {
                        try {
                            Thread.sleep(10L);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                if (exitCode == Integer.MIN_VALUE) {
                    exitCode = -1;
                }
            }

            stdoutThread.join(1500L);
            stderrThread.join(1500L);

            out.putBoolean("success", !timedOut && exitCode == 0);
            out.putInt("exitCode", exitCode);
            out.putString("stdout", stdout.getText());
            out.putString("stderr", stderr.getText());
            out.putString("error", timedOut ? "timeout" : "");
            out.putBoolean("timedOut", timedOut);
            return out;
        } catch (IOException e) {
            out.putBoolean("success", false);
            out.putInt("exitCode", -1);
            out.putString("stdout", stdout == null ? "" : stdout.getText());
            out.putString("stderr", stderr == null ? "" : stderr.getText());
            out.putString("error", e.toString());
            out.putBoolean("timedOut", false);
            return out;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    @Override
    public void destroy() throws RemoteException {
        shutdown();
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == DESTROY_TRANSACTION) {
            shutdown();
            return true;
        }
        return super.onTransact(code, data, reply, flags);
    }

    private void shutdown() {
        System.exit(0);
    }

    private static final class StreamCollector implements Runnable {
        private final InputStream input;
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private final AtomicBoolean closed = new AtomicBoolean(false);

        StreamCollector(InputStream input) {
            this.input = input;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[8192];
            try {
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            } catch (IOException ignored) {
            } finally {
                closed.set(true);
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
        }

        String getText() {
            synchronized (output) {
                return new String(output.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }
    }
}
