package com.maxsavteam.newmcalc2.utils;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.TextView;

import com.maxsavteam.newmcalc2.Main2Activity;

import java.text.DecimalFormatSymbols;

public class FormatUtil {
	public final static String TAG = Main2Activity.TAG + " Format";

	/**
	 * Returns string with formatted last number (if last symbol is digit)
	 */
	public static String format(String text) {
		if ( text.equals( "" ) || text.length() < 4 ) {
			return text;
		}

		return core( text );
	}

	private static String core(String source) {
		String txt = source;
		String number = "";
		int spaces = 0;
		int dotPos = -1;
		int len = txt.length();
		int i = len - 1;
		int nums = 0;
		for (; i >= 0 && ( Utils.isDigit( txt.charAt( i ) ) || txt.charAt( i ) == ' ' || txt.charAt( i ) == '.' ); i--) {
			if ( txt.charAt( i ) != ' ' ) {
				number = String.format( "%c%s", txt.charAt( i ), number );
			} else {
				spaces++;
			}
		}
		txt = txt.substring( 0, len - number.length() - spaces );
		len = number.length();
		if ( len < 4 ) {
			return txt + number;
		}
		if ( number.contains( "." ) ) {
			dotPos = number.indexOf( "." );
		}
		String numberOnRet;
		if ( dotPos == -1 ) {
			numberOnRet = "";
		} else {
			numberOnRet = number.substring( dotPos );
		}
		for (i = ( dotPos == -1 ? len - 1 : number.indexOf( "." ) - 1 ); i >= 0; i--, nums++) {
			if ( nums != 0 && nums % 3 == 0 ) {// && (dot_pos == -1 || i < dot_pos)){
				numberOnRet = String.format( " %s", numberOnRet );
			}
			numberOnRet = number.charAt( i ) + numberOnRet;
		}
		return txt + numberOnRet;
	}

	public static void scaleText(Activity activity, TextView textView, int maxWidth, int minScale, int maxScale) {
		String text = textView.getText().toString();

		Paint paint = new Paint();
		paint.setTypeface( textView.getTypeface() );
		paint.setTextSize( textView.getTextSize() );
		Rect bounds = new Rect();

		paint.getTextBounds( text, 0, text.length(), bounds );
		float scaledDensity = activity.getResources().getDisplayMetrics().scaledDensity;
		int currentTextSize = (int) ( textView.getTextSize() / scaledDensity );

		while ( bounds.width() > maxWidth && currentTextSize > minScale ) {
			paint.setTextSize( --currentTextSize * scaledDensity );
			paint.getTextBounds( text, 0, text.length(), bounds );
		}

		while ( bounds.width() < maxWidth && currentTextSize < maxScale ) {
			paint.setTextSize( ++currentTextSize * scaledDensity );
			paint.getTextBounds( text, 0, text.length(), bounds );
		}
		Log.i( TAG, "scaleText: new text size=" + currentTextSize );
		textView.setTextSize( currentTextSize );
	}

	public static void scaleTextN(TextView textView, int maxWidth) {
		Paint paint = new Paint();
		paint.setTypeface( textView.getTypeface() );
		paint.setTextSize( textView.getTextSize() );

		Rect bounds = new Rect();

		String text = textView.getText().toString();
		float textSize = textView.getTextSize();
		paint.getTextBounds( text, 0, text.length(), bounds );

		while ( bounds.width() > maxWidth ) {
			textSize--;
			paint.setTextSize( textSize );
			paint.getTextBounds( text, 0, text.length(), bounds );
		}

		textView.setTextSize( textSize );
	}

	public static String formatText(String text, DecimalFormatSymbols formatSymbols){
		StringBuilder formatted = new StringBuilder();
		StringBuilder number = new StringBuilder();
		for(char c : text.toCharArray()){
			if(c >= '0' && c <= '9' || c == formatSymbols.getDecimalSeparator())
				number.append( c );
			else if(c != formatSymbols.getGroupingSeparator()){
				formatted
						//.append( mDecimalFormat.format( mDecimalFormat.parse( number.toString(), new ParsePosition( 0 ) ) ) )
						.append( formatNumber( number.toString(), formatSymbols ) )
						.append( c );
				number.setLength( 0 );
			}
		}
		if(number.length() > 0)
			formatted
					.append( formatNumber( number.toString(), formatSymbols ) );
		//.append( mDecimalFormat.format( mDecimalFormat.parse( number.toString(), new ParsePosition( 0 ) ) ) );
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
		if(sb.charAt( sb.length() - 1 ) == formatSymbols.getGroupingSeparator())
			sb.deleteCharAt( sb.length() - 1 );
		return sb
				.reverse()
				.toString();
	}

}
