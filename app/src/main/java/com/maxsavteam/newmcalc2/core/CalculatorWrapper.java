package com.maxsavteam.newmcalc2.core;

import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.maxsavteam.calculator.Calculator;
import com.maxsavteam.calculator.exceptions.CalculatingException;
import com.maxsavteam.calculator.results.List;
import com.maxsavteam.calculator.results.List;
import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Max Savitsky
 */
public final class CalculatorWrapper {
	private static final String TAG = Main2Activity.TAG + " CalculatorWrapper";
	private final Resources mResources;

	private final Calculator mCalculator;

	private static CalculatorWrapper instance;

	private final Map<String, String> mReplacementMap;

	public Map<String, String> getReplacementMap() {
		return mReplacementMap;
	}

	public static CalculatorWrapper getInstance() {
		if ( instance == null ) {
			instance = new CalculatorWrapper();
		}
		return instance;
	}

	private CalculatorWrapper() {
		this.mResources = Utils.getContext().getResources();
		int roundScale = Utils.getDefaultSP().getInt( "rounding_scale", 8 );

		mCalculator = new Calculator();
		mReplacementMap = new HashMap<>() {{
			put( mResources.getString( R.string.multiply ), "*" );
			put( mResources.getString( R.string.div ), "/" );
			put( mResources.getString( R.string.sqrt ), "R" );
			put( "НОД", "gcd" );
			put( "НОК", "lcm" );
		}};
		mReplacementMap.putAll( Calculator.defaultReplacementMap );
		mCalculator.setAliases( mReplacementMap );

		Locale locale;
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
			locale = mResources.getConfiguration().getLocales().get( 0 );
		}else{
			locale = mResources.getConfiguration().locale;
		}
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
		mCalculator.setDecimalSeparator( decimalFormatSymbols.getDecimalSeparator() );
		mCalculator.setGroupingSeparator( decimalFormatSymbols.getGroupingSeparator() );
	}

	public List calculate(String example) {
		return mCalculator.calculate( example );
	}

	@StringRes
	public static int getStringResForErrorCode(int errorCode) {
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

