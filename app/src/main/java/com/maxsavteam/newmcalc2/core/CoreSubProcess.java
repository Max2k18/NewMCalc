package com.maxsavteam.newmcalc2.core;

import android.content.Context;
import android.util.Log;

import java.math.BigDecimal;

public class CoreSubProcess {

	private BigDecimal mResult = null;
	private final Context mContext;
	public static final String TAG = "CoreSubProcess";
	private CalculationError error;

	public CalculationError getError() {
		return error;
	}

	public BigDecimal getResult() {
		return mResult;
	}

	public void run(String ex) {
		CalculationCore calculationCore = new CalculationCore( mContext, new CalculationCore.CoreInterface() {
			@Override
			public void onSuccess(CalculationResult calculationResult) {
				mResult = calculationResult.getResult();
			}

			@Override
			public void onError(CalculationError calculationError) {
				CoreSubProcess.this.error = calculationError;
				mResult = null;
			}
		} );

		Log.i( TAG, "run with ex=" + ex );
		calculationCore.prepareAndRun( ex, "isolated" );
	}

	CoreSubProcess(Context context) {
		this.mContext = context;
		Log.i( TAG, "constructor" );
	}

}
