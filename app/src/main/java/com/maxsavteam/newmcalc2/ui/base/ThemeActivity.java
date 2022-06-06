package com.maxsavteam.newmcalc2.ui.base;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import androidx.annotation.ColorInt;

import com.maxsavteam.newmcalc2.R;

public class ThemeActivity extends BaseActivity {

	@ColorInt
	protected int textColor;

	@ColorInt
	protected int windowBackgroundColor;

	protected boolean isDarkMode;

	private void applyLightTheme(){
		isDarkMode = false;
		setTheme( R.style.AppTheme );
	}

	private void applyDarkTheme(){
		isDarkMode = true;
		setTheme( R.style.AppTheme_Dark );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String state = sp.getString( getString( R.string.pref_theme ), "light" );
		if( "light".equals( state ))
			applyLightTheme();
		else if( "dark".equals( state ))
			applyDarkTheme();
		else {
			switch ( getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK ) {
				case Configuration.UI_MODE_NIGHT_YES:
					applyDarkTheme();
					break;
				case Configuration.UI_MODE_NIGHT_NO:
				default:
					applyLightTheme();
					break;
			}
		}

		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute( R.attr.textColor, typedValue, false );
		textColor = getColor( typedValue.data );

		getTheme().resolveAttribute( R.attr.windowBackgroundColor, typedValue, false );
		windowBackgroundColor = getColor( typedValue.data );
	}
}