package com.maxsavteam.newmcalc2.utils;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.TextView;

import com.maxsavteam.newmcalc2.Main2Activity;

public class Format {
	private final static String TAG = Main2Activity.TAG + " Format";

	/**
	 * Returns string with formatted last number (if last symbol is digit)
	 */
	public static String format(String text) {
		if ( text.equals( "" ) || text.length() < 4 ) {
			return text;
		}

		return core( text );
	}

	private static String core(String txt) {
		String number = "";
		int spaces = 0, dot_pos = -1, len = txt.length(), i = len - 1, nums = 0;
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
			dot_pos = number.indexOf( "." );
		}
		String number_on_ret;
		if ( dot_pos == -1 ) {
			number_on_ret = "";
		} else {
			number_on_ret = number.substring( dot_pos );
		}
		for (i = ( dot_pos == -1 ? len - 1 : number.indexOf( "." ) - 1 ); i >= 0; i--, nums++) {
			if ( nums != 0 && nums % 3 == 0 ) {// && (dot_pos == -1 || i < dot_pos)){
				number_on_ret = String.format( " %s", number_on_ret );
			}
			number_on_ret = number.charAt( i ) + number_on_ret;
		}
		return txt + number_on_ret;
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
}
