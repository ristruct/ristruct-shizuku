package com.ristruct.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ristruct.shizuku.RistructBackend;
import com.ristruct.shizuku.RistructListener;
import com.ristruct.shizuku.RistructShizuku;
import com.ristruct.shizuku.command.RistructCommand;
import com.ristruct.shizuku.command.RistructCommandResult;
import com.ristruct.shizuku.service.RistructServiceCallback;

public class MainActivity extends Activity {
    private TextView status;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(com.ristruct.sample.R.layout.activity_main);
        status = findViewById(com.ristruct.sample.R.id.status);

        RistructShizuku.initialize(this, new RistructListener() {
            @Override public void onBinderReceived() { refresh(); }
            @Override public void onBinderDead() { refresh(); }
            @Override public void onUnavailable() { refresh(); }
            @Override public void onPermissionResult(boolean granted) { refresh(); }
        });

        ((Button) findViewById(com.ristruct.sample.R.id.request)).setOnClickListener(v -> RistructShizuku.requestPermission());
        ((Button) findViewById(com.ristruct.sample.R.id.bind)).setOnClickListener(v -> {
            com.ristruct.shizuku.RistructResult<Void> result = RistructShizuku.bindUserService(new RistructServiceCallback() {
                @Override public void onConnected() { refresh(); }
                @Override public void onDisconnected() { refresh(); }
                @Override public void onError(Throwable error) { toast(error.toString()); }
            });
            if (!result.isSuccess()) toast(result.getMessage());
        });
        ((Button) findViewById(com.ristruct.sample.R.id.run)).setOnClickListener(v -> {
            RistructShizuku.execute(new RistructCommand("id", 5000L), new com.ristruct.shizuku.command.RistructCommandCallback() {
                @Override public void onComplete(RistructCommandResult result) {
                    toast(result.getStdout().isEmpty() ? result.getError() : result.getStdout());
                }
            });
        });
        ((Button) findViewById(com.ristruct.sample.R.id.unbind)).setOnClickListener(v -> { RistructShizuku.unbindUserService(); refresh(); });
        refresh();
    }

    private void refresh() {
        RistructBackend backend = RistructShizuku.getBackend();
        status.setText("available=" + RistructShizuku.isAvailable() +
                "\npermission=" + RistructShizuku.hasPermission() +
                "\nuid=" + RistructShizuku.getUid() +
                "\nbackend=" + backend +
                "\nservice=" + RistructShizuku.isUserServiceConnected());
    }

    private void toast(String text) { Toast.makeText(this, text == null ? "" : text, Toast.LENGTH_LONG).show(); }

    @Override protected void onDestroy() { RistructShizuku.destroy(); super.onDestroy(); }
}
