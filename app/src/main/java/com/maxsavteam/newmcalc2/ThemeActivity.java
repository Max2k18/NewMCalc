package com.maxsavteam.newmcalc2;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class ThemeActivity extends AppCompatActivity {

	@ColorInt
	protected int textColor;
	protected boolean isDarkMode;

	private void applyLightTheme(){
		isDarkMode = false;
		setTheme( R.style.AppTheme );
		textColor = Color.BLACK;
	}

	private void applyDarkTheme(){
		isDarkMode = true;
		setTheme( R.style.AppTheme_Dark );
		textColor = Color.WHITE;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		int state = sp.getInt( "theme_state", 2 );
		if(state == 0)
			applyLightTheme();
		else if(state == 1)
			applyDarkTheme();
		else {
			switch ( getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK ) {
				case Configuration.UI_MODE_NIGHT_YES:
					applyDarkTheme();
					break;
				case Configuration.UI_MODE_NIGHT_NO:
					applyLightTheme();
					break;
			}
		}
	}
}