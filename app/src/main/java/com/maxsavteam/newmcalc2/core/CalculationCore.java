package com.maxsavteam.newmcalc2.core;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import androidx.annotation.StringRes;

import com.maxsavteam.calculator.Calculator;
import com.maxsavteam.calculator.exceptions.CalculatingException;
import com.maxsavteam.newmcalc2.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Max Savitsky
 */
public final class CalculationCore {
	private final CoreInterface mCoreInterface;

	public static final String TAG = "Core";
	private final Resources mResources;

	private final Calculator mCalculator;

	public CalculationCore(Context context, CoreInterface coreInterface) {
		this.mResources = context.getApplicationContext().getResources();
		int roundScale = PreferenceManager.getDefaultSharedPreferences( context.getApplicationContext() ).getInt( "rounding_scale", 8 );

		this.mCoreInterface = coreInterface;

		mCalculator = new Calculator();
		Map<String, String> replacementMap = new HashMap<>() {{
			put( "*", mResources.getString( R.string.multiply ) );
			put( "/", mResources.getString( R.string.div ) );
			put( "R", mResources.getString( R.string.sqrt ) );
		}};
		replacementMap.putAll( Calculator.defaultReplacementMap );
		mCalculator.setAliases( replacementMap );
	}

	public interface CoreInterface {
		void onSuccess(CalculationResult calculationResult);

		void onError(CalculationError calculationError);
	}

	private void onSuccess(CalculationResult calculationResult) {
		mCoreInterface.onSuccess( calculationResult );
	}

	private void onError(CalculationError calculationError) {
		mCoreInterface.onError( calculationError );
	}

	public void prepareAndRun(@NotNull final String example, @Nullable String type) {
		try {
			BigDecimal result = mCalculator.calculate( example );
			onSuccess( new CalculationResult().setResult( result ).setType( type ) );
		} catch (CalculatingException e) {
			int errorCode = e.getErrorCode();
			int res = getStringResForErrorCode( errorCode );
			if(res != -1){
				onError( new CalculationError().setShortError( mResources.getString( res ) ) );
			}
		}
	}

	@StringRes
	public static int getStringResForErrorCode(int errorCode){
		int result = -1;
		switch ( errorCode ) {
			case CalculatingException.TAN_OF_90:
				result = R.string.tan_of_90;
				break;
			case CalculatingException.DIVISION_BY_ZERO:
				result = R.string.division_by_zero;
				break;
			case CalculatingException.UNDEFINED:
				result = R.string.undefined;
				break;
			case CalculatingException.INVALID_BRACKETS_SEQUENCE:
				result = R.string.invalid_brackets_sequence;
				break;
			case CalculatingException.ROOT_OF_EVEN_DEGREE_OF_NEGATIVE_NUMBER:
				result = R.string.root_of_even_degree_of_even_number;
				break;
			default:
				break;
		}
		return result;
	}

}

