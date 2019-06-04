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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class history extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    MyRecyclerViewAdapter adapter;

    SharedPreferences sp;

    protected void backPressed(){
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_checking);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
        BroadcastReceiver br = new BroadcastReceiver() {
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
        registerReceiver(brfail, new IntentFilter("android.intent.action.NEWMCALC_UPDATE_FAIL"));
        start_type = getIntent().getStringExtra("start_type");
        ArrayList< ArrayList<String> > str = new ArrayList<>();
        ArrayList<String> str2 = new ArrayList<>();
        String his = sp.getString("history", "not");
        if(Objects.equals(his, "not") || Objects.requireNonNull(his).equals("")){
            setContentView(R.layout.history_notfound);
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
                setContentView(R.layout.activity_history);
                LinearLayoutManager lay = new LinearLayoutManager(this);
                lay.setOrientation(LinearLayoutManager.VERTICAL);
                rv = (RecyclerView) findViewById(R.id.rv_view);
                rv.setLayoutManager(lay);
                adapter = new MyRecyclerViewAdapter(getApplicationContext(), str);
                adapter.setClickListener(this);
                rv.setAdapter(adapter);
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                Log.e("Layout", e.toString());
            }

        }
    }
}
