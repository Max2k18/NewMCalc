package com.maxsavteam.newmcalc2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ui.base.ThemeActivity;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class AfterCrashActivity extends ThemeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_crash);

        Button btn = findViewById(R.id.btn_restart_app);

        btn.setOnClickListener(v -> restartApp());

        String path = getIntent().getStringExtra("path");

        String crashReport;
        try (FileInputStream fis = new FileInputStream(path)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            crashReport = bos.toString();
        } catch (IOException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().log("Failed to read crash report");
            FirebaseCrashlytics.getInstance().recordException(e);
            restartApp();
            return;
        }

        btn.setOnLongClickListener(v -> {
            if (!crashReport.isEmpty()) {
                CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this);
                builder
                        .setMessage(crashReport)
                        .setPositiveButton("OK", ((dialog, which) -> dialog.cancel()))
                        .show();
            }
            return true;
        });

    }

    private void restartApp() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

}