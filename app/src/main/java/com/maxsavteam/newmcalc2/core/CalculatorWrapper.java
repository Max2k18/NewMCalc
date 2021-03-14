package com.maxsavteam.newmcalc2.core;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.maxsavteam.calculator.Calculator;
import com.maxsavteam.calculator.exceptions.CalculatingException;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Max Savitsky
 */
public final class CalculatorWrapper {
	private final Resources mResources;

	private final Calculator mCalculator;

	public interface CoreInterface {
		void onSuccess(CalculationResult calculationResult);

		void onError(CalculationError calculationError);
	}

	public CalculatorWrapper() {
		this.mResources = Utils.getContext().getResources();
		int roundScale = Utils.getDefaultSP().getInt( "rounding_scale", 8 );

		mCalculator = new Calculator();
		Map<String, String> replacementMap = new HashMap<>() {{
			put( "*", mResources.getString( R.string.multiply ) );
			put( "/", mResources.getString( R.string.div ) );
			put( "R", mResources.getString( R.string.sqrt ) );
		}};
		replacementMap.putAll( Calculator.defaultReplacementMap );
		mCalculator.setAliases( replacementMap );
	}

	public void prepareAndRun(@NotNull final String example, @Nullable String type, @NonNull CoreInterface coreInterface) {
		try {
			BigDecimal result = mCalculator.calculate( example );
			coreInterface.onSuccess( new CalculationResult().setResult( result ).setType( type ) );
		} catch (CalculatingException e) {
			int errorCode = e.getErrorCode();
			int res = getStringResForErrorCode( errorCode );
			if(res != -1){
				coreInterface.onError( new CalculationError().setShortError( mResources.getString( res ) ) );
			}
		}
	}

	public BigDecimal calculate(String example){
		return mCalculator.calculate( example );
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

