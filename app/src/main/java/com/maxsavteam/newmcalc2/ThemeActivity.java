package com.maxsavteam.newmcalc2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class ThemeActivity extends AppCompatActivity {

	protected int textColor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(sp.getBoolean( "dark_mode", false )){
			setTheme( R.style.AppTheme_Dark );
			textColor = Color.WHITE;
		}else{
			setTheme( R.style.AppTheme );
			textColor = Color.BLACK;
		}
	}
}