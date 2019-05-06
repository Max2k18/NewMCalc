package com.maxsavteam.newmcalc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class catch_service extends AppCompatActivity {
	SharedPreferences sp;
	Context con;
	public void save_in_sp(boolean bool){
		sp.edit().putBoolean("isupdat", bool).apply();
	}

	public boolean isupdate(){
		return sp.getBoolean("isupdat", false);
	}

	public void create(){
		try{
			sp = PreferenceManager.getDefaultSharedPreferences(con);
		}catch (Exception e){
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	public void create_sp(SharedPreferences s){
		sp = s;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Intent in = getIntent();
		String aciton = in.getStringExtra("action");
		Intent ser;
		if(aciton.equals("NOT_BTN_PRESSED")){
			ser = new Intent(BuildConfig.APPLICATION_ID + ".NOT_BTN_PRESSED");
			sendBroadcast(ser);
			finish();
		}
	}
}
