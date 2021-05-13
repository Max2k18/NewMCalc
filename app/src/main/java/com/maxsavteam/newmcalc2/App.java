package com.maxsavteam.newmcalc2;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.maxsavitsky.exceptionhandler.ExceptionHandler;

import java.util.Locale;

public class App extends Application {

	private static App instance;
	private Locale appLocale;

	public Locale getAppLocale() {
		return appLocale;
	}

	public static App getInstance() {
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

		ExceptionHandler exceptionHandler = new ExceptionHandler( getApplicationContext(), AfterCrashActivity.class, false );
		Thread.setDefaultUncaughtExceptionHandler( (t, e)->{
			FirebaseCrashlytics.getInstance().recordException( e );
			FirebaseCrashlytics.getInstance().sendUnsentReports();
			exceptionHandler.uncaughtException( t, e );
		} );
	}
}
