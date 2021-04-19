package com.maxsavteam.newmcalc2;

import android.app.Application;
import android.os.Build;

import com.maxsavitsky.exceptionhandler.ExceptionHandler;

import java.util.Locale;

public class MCalcApplication extends Application {

	private static MCalcApplication instance;
	private Locale appLocale;

	public Locale getAppLocale() {
		return appLocale;
	}

	public static MCalcApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
			appLocale = getApplicationContext().getResources().getConfiguration().getLocales().get( 0 );
		}else{
			appLocale = getApplicationContext().getResources().getConfiguration().locale;
		}

		ExceptionHandler exceptionHandler = new ExceptionHandler( getApplicationContext(), null, true );
		Thread.setDefaultUncaughtExceptionHandler( exceptionHandler );
	}
}
