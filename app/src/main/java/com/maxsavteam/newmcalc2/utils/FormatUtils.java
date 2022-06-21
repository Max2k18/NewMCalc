package com.maxsavteam.newmcalc2.utils;

import com.maxsavteam.newmcalc2.App;
import com.maxsavteam.newmcalc2.Main2Activity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormatUtils {

	private static final String TAG = Main2Activity.TAG + " FormatUtils";

	public static DecimalFormat getDecimalFormat() {
		DecimalFormat decimalFormat = new DecimalFormat( "#,##0.###", new DecimalFormatSymbols( App.getInstance().getAppLocale() ) );
		decimalFormat.setParseBigDecimal( true );
		decimalFormat.setMaximumFractionDigits( Integer.MAX_VALUE );
		return decimalFormat;
	}

	public static DecimalFormatSymbols getRootLocaleFormatSymbols() {
		return new DecimalFormatSymbols( Locale.ROOT );
	}

	public static String normalizeNumbersInExample(String ex, DecimalFormat decimalFormat) {
		DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
		return formatExpression( ex, number->number /* return as it is */, symbols );
	}

	public interface Formatter {
		String format(String number);
	}

	public static String formatExpression(String ex, Formatter formatter, DecimalFormatSymbols sourceSymbols) {
		StringBuilder formatted = new StringBuilder();
		StringBuilder number = new StringBuilder();
		for (int i = 0; i < ex.length(); i++) {
			// we should use substrings because of UTF-16 symbols (such as degree sign)
			// UTF-16 characters are split into two separate characters
			String character = ex.substring( i, i + 1 );
			if ( isDigit( character ) ) {
				number.append( character );
			} else if ( character.equals( String.valueOf( sourceSymbols.getDecimalSeparator() ) ) ) {
				number.append( "." );
			} else if ( !character.equals( String.valueOf( sourceSymbols.getGroupingSeparator() ) ) ) {
				if ( number.length() > 0 ) {
					formatted.append( formatter.format( number.toString() ) );
					number.setLength( 0 );
				}
				formatted
						.append( character );
			}
		}
		if ( number.length() > 0 ) {
			formatted.append( formatter.format( number.toString() ) );
		}
		return formatted.toString();
	}

	public static String formatNumbersInExpression(String ex, DecimalFormat decimalFormat, DecimalFormatSymbols sourceSymbols){
		return formatExpression( ex, number -> decimalFormat.format( new BigDecimal( number ) ), sourceSymbols );
	}

	public static String formatNumbersInExpression(String ex, DecimalFormat decimalFormat) {
		return formatNumbersInExpression( ex, decimalFormat, getRootLocaleFormatSymbols() );
	}

	private static boolean isDigit(String character) {
		if ( character.length() > 1 ) {
			return false;
		}
		return Character.isDigit( character.codePointAt( 0 ) );
	}

}
