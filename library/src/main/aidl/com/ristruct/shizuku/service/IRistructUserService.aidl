package com.ristruct.shizuku.service;

import android.os.Bundle;

interface IRistructUserService {
    /**
     * Reserved by Shizuku for its UserService destroy transaction.
     */
    void destroy() = 16777114;

    Bundle execute(String command, long timeoutMs) = 1;
}
