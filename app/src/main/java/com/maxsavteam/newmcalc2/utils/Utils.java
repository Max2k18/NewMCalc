package com.maxsavteam.newmcalc2.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.types.Pair;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptFocal;

public final class Utils {
	@SuppressLint("StaticFieldLeak")
	private static Context sContext;

	public static final String MCALC_SITE = "https://mcalc.maxsavteam.com/";

	public static void setContext(Context context) {
		sContext = context;
	}

	public static SharedPreferences getDefaultSP() {
		return PreferenceManager.getDefaultSharedPreferences( sContext );
	}

	public static Context getContext() {
		return sContext;
	}

	public static File getExternalStoragePath() {
		return sContext.getExternalFilesDir( null );
	}

	public static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	public static boolean isDigit(String x) {
		char c = x.toCharArray()[ 0 ];
		return c >= '0' && c <= '9';
	}

	public static boolean isLetter(char c) {
		return c >= 'a' && c <= 'z';
	}

	public static boolean isBasicAction(String action) {
		return action.equals( "+" ) || action.equals( "-" ) || action.equals( "*" ) || action.equals( "/" );
	}

	public static boolean isConstNum(char c, Context context) {
		String PI = context.getResources().getString( R.string.pi );
		String FI = context.getResources().getString( R.string.fi );
		return Character.toString( c ).equals( PI ) || Character.toString( c ).equals( FI ) || c == 'e';
	}

	public static BigDecimal getRemainder(BigDecimal a, BigDecimal b) {
		return a.remainder( b );
	}


	/**
	 * @param source String in which need to delete back zeros after dot and front zeros
	 * @return String without back and front zeros
	 */
	public static String deleteZeros(String source) {
		String res = source;
		int len = res.length();
		if ( res.contains( "." ) && res.charAt( len - 1 ) == '0' ) {
			while ( res.charAt( len - 1 ) == '0' ) {
				len--;
				res = res.substring( 0, len );
			}
			if ( res.charAt( len - 1 ) == '.' ) {
				res = res.substring( 0, len - 1 );
			}
		}
		while ( res.charAt( 0 ) == '0' && res.length() > 1 && res.charAt( 1 ) != '.' ) {
			res = res.substring( 1 );
		}
		return res;
	}

	public static BigDecimal deleteZeros(BigDecimal x){
		return new BigDecimal( deleteZeros( x.toPlainString() ) );
	}

	private static void addSwipeFeature(AlertDialog alertDialog) {
		Window window = alertDialog.getWindow();
		if ( window != null ) {
			window.requestFeature( Window.FEATURE_SWIPE_TO_DISMISS );
		}
	}

	public static void recolorButtons(AlertDialog alertDialog, Context context) {
		alertDialog.setOnShowListener( dialog->{
			Button positive = ( (AlertDialog) dialog ).getButton( AlertDialog.BUTTON_POSITIVE );
			Button negative = ( (AlertDialog) dialog ).getButton( AlertDialog.BUTTON_NEGATIVE );
			Button neutral = ( (AlertDialog) dialog ).getButton( AlertDialog.BUTTON_NEUTRAL );
			if ( negative != null ) {
				negative.setTextColor( context.getResources().getColor( R.color.colorAccent ) );
			}
			if ( positive != null ) {
				positive.setTextColor( context.getResources().getColor( R.color.colorAccent ) );
			}
			if ( neutral != null ) {
				neutral.setTextColor( context.getResources().getColor( R.color.colorAccent ) );
			}
		} );
	}

	public static void defaultActivityAnim(Activity activity) {
		//activity.overridePendingTransition( R.anim.activity_in1, R.anim.activity_out1 );
	}

	public static void recolorAlertDialogButtons(AlertDialog alertDialog, Context context) {
		addSwipeFeature( alertDialog );
		recolorButtons( alertDialog, context );
	}

	/**
	 * Returns string without any spaces
	 *
	 * @param source String in which need to delete spaces
	 * @return String without spaces
	 */
	public static String deleteSpaces(String source) {
		String res = source;
		if ( res.contains( " " ) ) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < res.length(); i++) {
				if ( res.charAt( i ) != ' ' ) {
					sb.append( res.charAt( i ) );
				}
			}
			res = sb.toString();
		}
		return res;
	}

	public static String trim(String s) {
		if ( s == null ) {
			throw new IllegalArgumentException( "Utils.trim: String is null" );
		}
		String res = s;
		while ( res.length() > 0 && ( res.charAt( 0 ) == ' ' || res.charAt( 0 ) == '\n' ) ) {
			res = res.substring( 1 );
		}
		while ( res.length() > 0 && ( res.charAt( res.length() - 1 ) == ' ' || res.charAt( res.length() - 1 ) == '\n' ) ) {
			res = res.substring( 0, res.length() - 1 );
		}
		return res;
	}

	/**
	 * Returns true if string is number (spaces and dot includes)
	 */
	public static boolean isNumber(String source) {
		String s = deleteSpaces( source );
		try {
			new BigDecimal( s );
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Function returns string without ( at the start and ) at the end and number of deleted pairs of brackets
	 */
	public static Pair<String, Integer> trimBrackets(String source) {
		String s = source;
		Integer cnt = 0;
		while(s.startsWith( "(" ) && s.endsWith( ")" )){
			s = s.substring( 1, s.length() - 1 );
			cnt++;
		}
		return new Pair<>( s, cnt );
	}

	public static MaterialTapTargetPrompt getGuideTip(Activity activity, String primary, String secondary, @IdRes int id, PromptFocal promptFocal) {
		return new MaterialTapTargetPrompt.Builder( activity )
				.setPrimaryText( primary )
				.setSecondaryText( secondary )
				.setTarget( id )
				.setPromptFocal( promptFocal )
				.setFocalColour( Color.TRANSPARENT )
				.setBackgroundColour( activity.getColor( R.color.colorAccent ) )
				.create();
	}
}
