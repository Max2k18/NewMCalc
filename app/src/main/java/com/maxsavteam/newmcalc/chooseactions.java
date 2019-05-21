package com.maxsavteam.newmcalc;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class chooseactions extends AppCompatActivity {

    SharedPreferences sp;
    int width;
    int height;

    public void backPressed() {
        finish();
        overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
    }


    @Override
    public void onBackPressed(){
        backPressed();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
        if (id == android.R.id.home) {
            backPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String language_code = sp.getString("lang", "def");
        if(!language_code.equals("def")){
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.setLocale(new Locale(language_code.toLowerCase())); // API 17+ only.
            res.updateConfiguration(conf, dm);
        }
        setContentView(R.layout.activity_chooseactions);

        height = sp.getInt("btnHeight", 100);
        width = sp.getInt("btnWidth", 100);
        //setButtons();
        try{
            getSupportActionBar().setTitle(getResources().getString(R.string.chooseaction));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            finish();
        }
        update_service ups = new update_service(this);
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AlertDialog.Builder b = new AlertDialog.Builder(chooseactions.this);
                b.setCancelable(false)
                        .setTitle(R.string.installation)
                        .setMessage(R.string.update_avail_to_install)
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ups.install();
                    }
                });
                AlertDialog inst = b.create();
                inst.show();
            }
        };
        BroadcastReceiver brfail = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AlertDialog.Builder b = new AlertDialog.Builder(chooseactions.this);
                b.setTitle(R.string.installation).setMessage(R.string.cannot_update).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            }
        };
        registerReceiver(br, new IntentFilter("android.intent.action.NEWMCALC_UPDATE_SUC"));
        registerReceiver(brfail, new IntentFilter("android.intent.action.NEWMCALC_UPDATE_FAIL"));
    }


    public void onClick(View v) {
        Button btn = findViewById(v.getId());
        sp.edit().putString("chooseValue", btn.getText().toString()).apply();
        finish();
        overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
    }

    public void setButtons() {
        ArrayList<Button> ar = new ArrayList<>();
        ar.add((Button) findViewById(R.id.btnPer));
        ar.add((Button) findViewById(R.id.btnN));
        ar.add((Button) findViewById(R.id.btnF));
        ar.add((Button) findViewById(R.id.btnBr1));
        ar.add((Button) findViewById(R.id.btnBr2));
        ar.add((Button) findViewById(R.id.btnLog));
        ar.add((Button) findViewById(R.id.btnCos));
        ar.add((Button) findViewById(R.id.btnSin));
        ar.add((Button) findViewById(R.id.btnTan));
        ar.add((Button) findViewById(R.id.btnLn));
        ar.add((Button) findViewById(R.id.btnLn));
        ar.add((Button) findViewById(R.id.btnP));
        ar.add((Button) findViewById(R.id.btnE));

        for (int i = 0; i < ar.size(); i++) {
            ar.get(i).setWidth(width);
            ar.get(i).setHeight(height);
        }
    }
}
