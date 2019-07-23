package com.maxsavteam.newmcalc;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class history extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    MyRecyclerViewAdapter adapter;

    SharedPreferences sp;

    protected void backPressed(){
        sendBroadcast(new Intent(BuildConfig.APPLICATION_ID + ".HISTORY_ACTION"));
        finish();
        overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
    }


    @Override
    public void onBackPressed(){
        sp.edit().remove("action").apply();
        sp.edit().remove("history_action").apply();
        if(start_type.equals("app")){
            backPressed();
        }else if(start_type.equals("shortcut")) {
        	startActivity(new Intent(this, MainActivity.class));
	        backPressed();
        }
        //super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
        if(id == android.R.id.home){
            backPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        sp.edit().putString("history_action", adapter.getItem(position).get(0)).apply();
        backPressed();
        //Toast.makeText(this, "You clicked " + adapter.getItem(position).get(0) + " " + adapter.getItem(position).get(0) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    String start_type;

    View.OnTouchListener touchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                try{
                    Thread.sleep(3000);
                }catch(Exception e){
                    e.printStackTrace();
                }
                return true;
            }else
                return false;
        }
    };

    boolean DarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        DarkMode = sp.getBoolean("dark_mode", false);
        if(DarkMode)
            setTheme(android.R.style.Theme_Material_NoActionBar);
        else
            setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        if(DarkMode)
        	getWindow().setBackgroundDrawableResource(R.drawable.black);
        RecyclerView rv;
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
            getSupportActionBar().setTitle(R.string.hitory);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

        update_service ups = new update_service(this);
        /*BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AlertDialog.Builder b = new AlertDialog.Builder(history.this);
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
                AlertDialog.Builder b = new AlertDialog.Builder(history.this);
                b.setTitle(R.string.installation).setMessage(R.string.cannot_update).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            }
        };
        registerReceiver(br, new IntentFilter("android.intent.action.NEWMCALC_UPDATE_SUC"));
        registerReceiver(brfail, new IntentFilter("android.intent.action.NEWMCALC_UPDATE_FAIL"));*/
        start_type = getIntent().getStringExtra("start_type");
        ArrayList< ArrayList<String> > str = new ArrayList<>();
        ArrayList<String> str2 = new ArrayList<>();
        String his = sp.getString("history", "not");
        if(Objects.equals(his, "not") || Objects.requireNonNull(his).equals("")){
            setContentView(R.layout.history_notfound);
            if(DarkMode){
                TextView t = findViewById(R.id.txtHistoryNotFound);
                t.setTextColor(getResources().getColor(R.color.white));
	            getWindow().setBackgroundDrawableResource(R.drawable.black);
            }
        }else{
            int i = 0;
            String ex, ans;
            while(i < his.length() && his.charAt(i) != ';'){
                boolean was_dot = false;
                ex = ans = "";
                while(i < his.length()){
                    if(his.charAt(i) == ';'){
                        i++;
                        break;
                    }
                    if(his.charAt(i) == ','){
                        i++;
                        was_dot = true;
                        continue;
                    }
                    if(!was_dot){
                        ex += Character.toString(his.charAt(i));
                    }else{
                        ans += Character.toString(his.charAt(i));
                    }
                    i++;
                }
                str2.add(ex);
                str2.add(ans);
                str.add((ArrayList<String>) str2.clone());
                str2.clear();
            }
            try {
                LinearLayoutManager lay = new LinearLayoutManager(this);
                lay.setOrientation(RecyclerView.VERTICAL);
                rv = (RecyclerView) findViewById(R.id.rv_view);
                rv.setLayoutManager(lay);
                adapter = new MyRecyclerViewAdapter(getApplicationContext(), str);
                adapter.setClickListener(this);
//                rv.setOnTouchListener();
                rv.setAdapter(adapter);
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                Log.e("Layout", e.toString());
            }

        }
    }
}
