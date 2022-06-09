package com.maxsavteam.newmcalc2.utils;

import com.maxsavteam.newmcalc2.Main2Activity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class FormatUtils {

	private static final String TAG = Main2Activity.TAG + " FormatUtils";

	public static String formatText(String text, DecimalFormatSymbols formatSymbols) {
		StringBuilder formatted = new StringBuilder();
		StringBuilder number = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			// we should use substrings because of UTF-16 symbols (such as degree sign)
			// UTF-16 characters are split into two separate characters
			String character = text.substring( i, i + 1 );
			if ( isDigit( character ) || character.equals( String.valueOf( formatSymbols.getDecimalSeparator() ) ) ) {
				number.append( character );
			} else if ( !character.equals( String.valueOf( formatSymbols.getGroupingSeparator() ) ) ) {
				if ( number.length() > 0 ) {
					formatted
							.append( formatNumber( number.toString(), formatSymbols ) );
					number.setLength( 0 );
				}
				formatted.append( character );
			}
		}
		if ( number.length() > 0 ) {
			formatted
					.append( formatNumber( number.toString(), formatSymbols ) );
		}
		return formatted.toString();
	}

	public static String formatNumber(String number, DecimalFormatSymbols formatSymbols, DecimalFormatSymbols sourceSymbols) {
		int dotPos = number.indexOf( sourceSymbols.getDecimalSeparator() );
		StringBuilder sb = new StringBuilder();
		if ( dotPos != -1 ) {
			for (int i = number.length() - 1; i > dotPos; i--) {
				char c = number.charAt( i );
				if ( c >= '0' && c <= '9' ) {
					sb.append( c );
				}
			}
			sb.append( formatSymbols.getDecimalSeparator() );
		}
		int insertedCount = 0;
		if ( dotPos == -1 ) {
			dotPos = number.length();
		}
		for (int i = dotPos - 1; i >= 0; i--) {
			char c = number.charAt( i );
			if ( c >= '0' && c <= '9' ) {
				sb.append( c );
				insertedCount++;
				if ( insertedCount % 3 == 0 ) {
					sb.append( formatSymbols.getGroupingSeparator() );
				}
			}
		}
		if ( sb.length() > 0 && sb.charAt( sb.length() - 1 ) == formatSymbols.getGroupingSeparator() ) {
			sb.deleteCharAt( sb.length() - 1 );
		}
		return sb
				.reverse()
				.toString();
	}

	public static String formatNumber(String number, DecimalFormatSymbols symbols) {
		return formatNumber( number, symbols, symbols );
	}

	public static String normalizeNumbersInExample(String ex, DecimalFormat decimalFormat) {
		decimalFormat.setParseBigDecimal( true );
		DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
		StringBuilder formatted = new StringBuilder();
		StringBuilder number = new StringBuilder();
		for (int i = 0; i < ex.length(); i++) {
			// we should use substrings because of UTF-16 symbols (such as degree sign)
			// UTF-16 characters are split into two separate characters
			String character = ex.substring( i, i + 1 );
			if ( isDigit( character ) ) {
				number.append( character );
			} else if ( character.equals( String.valueOf( symbols.getDecimalSeparator() ) ) ) {
				number.append( "." );
			} else if ( !character.equals( String.valueOf( symbols.getGroupingSeparator() ) ) ) {
				if ( number.length() > 0 ) {
					formatted
							.append( number );
					number.setLength( 0 );
				}
				formatted
						.append( character );
			}
		}
		if ( number.length() > 0 ) {
			formatted.append( number );
		}
		return formatted.toString();
	}

	public static String formatNumbersInExpression(String ex, DecimalFormat decimalFormat) {
		StringBuilder formatted = new StringBuilder();
		StringBuilder number = new StringBuilder();
		for (int i = 0; i < ex.length(); i++) {
			// we should use substrings because of UTF-16 symbols (such as degree sign)
			// UTF-16 characters are split into two separate characters
			String character = ex.substring( i, i + 1 );
			if ( isDigit( character ) || ".".equals( character ) ) {
				number.append( character );
			} else {
				if ( number.length() > 0 ) {
					formatted.append( decimalFormat.format( new BigDecimal( number.toString() ) ) );
					number.setLength( 0 );
				}
				formatted.append( character );
			}
		}
		if ( number.length() > 0 ) {
			formatted.append( decimalFormat.format( new BigDecimal( number.toString() ) ) );
		}
		return formatted.toString();
	}

	private static boolean isDigit(String character) {
		if ( character.length() > 1 ) {
			return false;
		}
		return Character.isDigit( character.codePointAt( 0 ) );
	}

}
