package com.maxsavteam.newmcalc2.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;

public class FormatUtils {

	public static String formatText(String text, DecimalFormatSymbols formatSymbols){
		StringBuilder formatted = new StringBuilder();
		StringBuilder number = new StringBuilder();
		for(char c : text.toCharArray()){
			if(c >= '0' && c <= '9' || c == formatSymbols.getDecimalSeparator())
				number.append( c );
			else if(c != formatSymbols.getGroupingSeparator()){
				if(number.length() > 0) {
					formatted
							.append( formatNumber( number.toString(), formatSymbols ) );
					number.setLength( 0 );
				}
				formatted
						.append( c );
			}
		}
		if(number.length() > 0)
			formatted
					.append( formatNumber( number.toString(), formatSymbols ) );
		return formatted.toString();
	}

	public static String formatNumber(String number, DecimalFormatSymbols formatSymbols){
		int dotPos = number.indexOf( formatSymbols.getDecimalSeparator() );
		StringBuilder sb = new StringBuilder();
		if(dotPos != -1){
			for(int i = number.length() - 1; i > dotPos; i--){
				char c = number.charAt( i );
				if(c >= '0' && c <= '9')
					sb.append( c );
			}
			sb.append( formatSymbols.getDecimalSeparator() );
		}
		int insertedCount = 0;
		if(dotPos == -1)
			dotPos = number.length();
		for(int i = dotPos - 1; i >= 0; i--){
			char c = number.charAt( i );
			if(c >= '0' && c <= '9'){
				sb.append( c );
				insertedCount++;
				if(insertedCount % 3 == 0)
					sb.append( formatSymbols.getGroupingSeparator() );
			}
		}
		if(sb.length() > 0 && sb.charAt( sb.length() - 1 ) == formatSymbols.getGroupingSeparator())
			sb.deleteCharAt( sb.length() - 1 );
		return sb
				.reverse()
				.toString();
	}

	public static String normalizeNumbersInExample(String ex, DecimalFormat decimalFormat){
		decimalFormat.setParseBigDecimal( true );
		DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
		StringBuilder formatted = new StringBuilder();
		StringBuilder number = new StringBuilder();
		for(char c : ex.toCharArray()){
			if(c >= '0' && c <= '9' || c == symbols.getDecimalSeparator())
				number
						.append( c );
			else if(c != symbols.getGroupingSeparator()){
				if(number.length() > 0) {
					formatted
							.append( decimalFormat.parse( number.toString(), new ParsePosition( 0 ) ) );
					number.setLength( 0 );
				}
				formatted
						.append( c );
			}
		}
		if(number.length() > 0)
			formatted
					.append( decimalFormat.parse( number.toString(), new ParsePosition( 0 ) ) );
		return formatted.toString();
	}

	public static String formatNumbersInExpression(String ex, DecimalFormat decimalFormat){
		StringBuilder formatted = new StringBuilder();
		StringBuilder number = new StringBuilder();
		for(char c : ex.toCharArray()){
			if(c >= '0' && c <= '9' || c == '.'){
				number
						.append( c );
			}else{
				if(number.length() > 0) {
					formatted
							.append( decimalFormat.format( new BigDecimal( number.toString() ) ) );
					number.setLength( 0 );
				}
				formatted
						.append( c );
			}
		}
		if(number.length() > 0)
			formatted
					.append( decimalFormat.format( new BigDecimal( number.toString() ) ) );
		return formatted.toString();
	}

}
