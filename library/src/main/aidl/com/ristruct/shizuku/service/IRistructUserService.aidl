package com.ristruct.shizuku.service;

import android.os.Bundle;

interface IRistructUserService {
    Bundle execute(String command, long timeoutMs);
}
