package com.ristruct.shizuku.command;

public final class RistructCommand {
    private final String command;
    private final long timeoutMs;

    public RistructCommand(String command) {
        this(command, 30000L);
    }

    public RistructCommand(String command, long timeoutMs) {
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("command must not be empty");
        }
        if (timeoutMs <= 0L) {
            throw new IllegalArgumentException("timeoutMs must be > 0");
        }
        this.command = command;
        this.timeoutMs = timeoutMs;
    }

    public String getCommand() { return command; }
    public long getTimeoutMs() { return timeoutMs; }
}
