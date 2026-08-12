package com.ristruct.shizuku;

public interface RistructListener {
    void onBinderReceived();
    void onBinderDead();
    void onPermissionResult(boolean granted);
    void onUnavailable();
}
