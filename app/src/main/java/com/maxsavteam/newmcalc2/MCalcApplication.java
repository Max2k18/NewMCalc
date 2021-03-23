package com.maxsavteam.newmcalc2;

import android.app.Application;

import com.maxsavitsky.exceptionhandler.ExceptionHandler;

public class MCalcApplication extends Application {

	private static MCalcApplication instance;

	public static MCalcApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		ExceptionHandler exceptionHandler = new ExceptionHandler( getApplicationContext(), null, true );
		Thread.setDefaultUncaughtExceptionHandler( exceptionHandler );
	}
}
