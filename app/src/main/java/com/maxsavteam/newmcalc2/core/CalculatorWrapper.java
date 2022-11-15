package com.maxsavteam.newmcalc2.core;

import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import com.maxsavteam.calculator.Calculator;
import com.maxsavteam.calculator.exceptions.CalculationException;
import com.maxsavteam.calculator.results.NumberList;
import com.maxsavteam.calculator.utils.MathUtils;
import com.maxsavteam.newmcalc2.App;
import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.utils.Utils;

import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Max Savitsky
 */
public final class CalculatorWrapper {
	private static final String TAG = Main2Activity.TAG + " CalculatorWrapper";

	public static final int MAX_ROUND_SCALE = 1000;

	private final Resources resources;

	private final Calculator calculator;

	private static CalculatorWrapper instance;

	private final Map<String, String> replacementMap;

	public Map<String, String> getReplacementMap() {
		return replacementMap;
	}

	public static CalculatorWrapper getInstance() {
		if ( instance == null ) {
			instance = new CalculatorWrapper();
		}
		return instance;
	}

	private CalculatorWrapper() {
		this.resources = Utils.getContext().getResources();

		calculator = new Calculator();
		replacementMap = new HashMap<>() {{
			put( resources.getString( R.string.multiply ), "*" );
			put( resources.getString( R.string.div ), "/" );
			put( resources.getString( R.string.sqrt ), "sqrt" );
			put( "НОД", "gcd" );
			put( "НОК", "lcm" );
		}};
		replacementMap.put( resources.getString( R.string.pi ), "(pi)" );
		replacementMap.put( resources.getString( R.string.fi ), "(fi)" );
		replacementMap.put( resources.getString( R.string.euler_constant ),
				"(" + MathUtils.E.toPlainString() + ")"
		);
		calculator.setAliases( replacementMap );

		updateLocale();
		loadRoundScale();
	}

	public void loadRoundScale(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( Utils.getContext() );
		String roundScaleString = sp.getString( resources.getString( R.string.pref_round_scale ), null );
		if(roundScaleString != null){
			Calculator.setRoundScale( Integer.parseInt( roundScaleString ) );
		}
	}

	public void setRoundScale(int roundScale){
		Calculator.setRoundScale( roundScale );
	}

	public void updateLocale(){
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols( App.getInstance().getAppLocale() );
		calculator.setDecimalSeparator( decimalFormatSymbols.getDecimalSeparator() );
		calculator.setGroupingSeparator( decimalFormatSymbols.getGroupingSeparator() );
	}

	public NumberList calculate(String example) {
		return calculator.calculate( example );
	}

	@StringRes
	public static int getStringResForErrorCode(int errorCode) {
		int result = -1;
		switch ( errorCode ) {
			case CalculationException.INVALID_VALUE_FOR_TANGENT:
				result = R.string.invalid_value_for_tangent;
				break;
			case CalculationException.INVALID_VALUE_FOR_COTANGENT:
				result = R.string.invalid_value_for_cotangent;
				break;
			case CalculationException.DIVISION_BY_ZERO:
				result = R.string.division_by_zero;
				break;
			case CalculationException.UNDEFINED:
				result = R.string.undefined;
				break;
			case CalculationException.INVALID_BRACKETS_SEQUENCE:
				result = R.string.invalid_brackets_sequence;
				break;
			case CalculationException.ROOT_OF_EVEN_DEGREE_OF_NEGATIVE_NUMBER:
				result = R.string.root_of_even_degree_of_even_number;
				break;
			case CalculationException.NAN:
				result = R.string.not_a_number;
				break;
			default:
				break;
		}
		return result;
	}

}

