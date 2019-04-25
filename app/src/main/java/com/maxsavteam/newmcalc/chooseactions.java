package com.maxsavteam.newmcalc;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

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
        setContentView(R.layout.activity_chooseactions);

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        height = sp.getInt("btnHeight", 100);
        width = sp.getInt("btnWidth", 100);
        //setButtons();
        getSupportActionBar().setTitle(getResources().getString(R.string.chooseaction));
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
        }catch(Exception e){
            e.printStackTrace();
        }
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
