package com.maxsavteam.newmcalc2;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;

import com.maxsavitsky.exceptionhandler.ExceptionHandler;
import com.maxsavteam.newmcalc2.utils.Utils;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;

public class App extends Application {

	private static App instance;
	private Locale appLocale;
	private boolean isFirstStart;

	public Locale getAppLocale() {
		return appLocale;
	}

	public static App getInstance() {
		return instance;
	}

	public boolean isFirstStart() {
		return isFirstStart;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		Utils.setContext( getApplicationContext() );

		SharedPreferences sp = Utils.getDefaultSP();
		isFirstStart = sp.getBoolean( "isFirstStart", true );
		if(isFirstStart)
			sp.edit().putBoolean( "isFirstStart", false ).apply();

		updateAppLocale();

		ExceptionHandler exceptionHandler = new ExceptionHandler( getApplicationContext(), AfterCrashActivity.class, false );
		UncaughtExceptionHandler previousHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler( (t, e)->{
			exceptionHandler.uncaughtException( t, e );
			if ( previousHandler != null ) {
				previousHandler.uncaughtException( t, e );
			}
		} );
	}

	public void updateAppLocale(){
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
			appLocale = getApplicationContext().getResources().getConfiguration().getLocales().get( 0 );
		} else {
			appLocale = getApplicationContext().getResources().getConfiguration().locale;
		}
	}

}
