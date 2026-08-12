package com.ristruct.shizuku.command;

import android.os.Bundle;

public final class RistructCommandResult {
    private final boolean success;
    private final int exitCode;
    private final String stdout;
    private final String stderr;
    private final String error;
    private final boolean timedOut;

    private RistructCommandResult(boolean success, int exitCode, String stdout, String stderr, String error, boolean timedOut) {
        this.success = success;
        this.exitCode = exitCode;
        this.stdout = stdout == null ? "" : stdout;
        this.stderr = stderr == null ? "" : stderr;
        this.error = error == null ? "" : error;
        this.timedOut = timedOut;
    }

    public static RistructCommandResult fromBundle(Bundle b) {
        if (b == null) {
            return new RistructCommandResult(false, -1, "", "", "null result", false);
        }
        return new RistructCommandResult(
                b.getBoolean("success", false),
                b.getInt("exitCode", -1),
                b.getString("stdout", ""),
                b.getString("stderr", ""),
                b.getString("error", ""),
                b.getBoolean("timedOut", false)
        );
    }

    public boolean isSuccess() { return success; }
    public int getExitCode() { return exitCode; }
    public String getStdout() { return stdout; }
    public String getStderr() { return stderr; }
    public String getError() { return error; }
    public boolean isTimedOut() { return timedOut; }
}
