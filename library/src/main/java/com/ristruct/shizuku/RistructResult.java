package com.ristruct.shizuku;

public final class RistructResult<T> {
    private final boolean success;
    private final T value;
    private final RistructError error;
    private final String message;
    private final Throwable cause;

    private RistructResult(boolean success, T value, RistructError error, String message, Throwable cause) {
        this.success = success;
        this.value = value;
        this.error = error == null ? RistructError.INTERNAL_ERROR : error;
        this.message = message == null ? "" : message;
        this.cause = cause;
    }

    public static <T> RistructResult<T> success(T value) {
        return new RistructResult<>(true, value, RistructError.NONE, "", null);
    }

    public static <T> RistructResult<T> failure(RistructError error, String message) {
        return new RistructResult<>(false, null, error, message, null);
    }

    public static <T> RistructResult<T> failure(RistructError error, String message, Throwable cause) {
        return new RistructResult<>(false, null, error, message, cause);
    }

    public boolean isSuccess() { return success; }
    public T getValue() { return value; }
    public RistructError getError() { return error; }
    public String getMessage() { return message; }
    public Throwable getCause() { return cause; }
}
