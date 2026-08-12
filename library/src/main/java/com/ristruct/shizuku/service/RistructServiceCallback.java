package com.ristruct.shizuku.service;

public interface RistructServiceCallback {
    void onConnected();
    void onDisconnected();
    void onError(Throwable error);
}
