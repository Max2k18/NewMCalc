package com.maxsavteam.newmcalc2.utils;

import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.maxsavteam.newmcalc2.R;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import ch.obermuhlner.math.big.BigDecimalMath;

public final class Utils {
	public static boolean isDigit(char c){
		return c >= '0' && c <= '9';
	}

	public static boolean isDigit(String x){
		char c = x.toCharArray()[0];
		return c >= '0' && c <= '9';
	}

	public static boolean isLetter(char c){
		return c >= 'a' && c <= 'z';
	}

	public static boolean isBasicAction(String action){
		return action.equals("+") || action.equals("-") || action.equals("*") || action.equals("/");
	}

	public static boolean isConstNum(char c, Context context){
		String PI = context.getResources().getString(R.string.pi);
		String FI = context.getResources().getString(R.string.fi);
		return Character.toString(c).equals(PI) || Character.toString(c).equals(FI) || c == 'e';
	}

	public static BigDecimal fact(BigDecimal y){
		return BigDecimalMath.factorial(y, new MathContext(10));
	}

	public static BigDecimal toRadians(BigDecimal decimal){
		return decimal.multiply(BigDecimal.valueOf(Math.PI)).divide(BigDecimal.valueOf(180), 8, RoundingMode.HALF_EVEN);
	}

	public static BigDecimal getRemainder(BigDecimal a, BigDecimal b){
		return a.remainder( b );
	}


	/**
	 * @param source String in which need to delete back zeros after dot and front zeros
	 * @return String without back and front zeros
	 */
	public static String deleteZeros(String source){
		int len = source.length();
		if(source.contains(".")) {
			if (source.charAt(len - 1) == '0') {
				while (source.charAt(len - 1) == '0') {
					len--;
					source = source.substring(0, len);
				}
				if(source.charAt(len - 1) == '.')
					source = source.substring(0, len - 1);
			}
		}
		while(source.charAt(0) == '0' && source.length() > 1 && source.charAt(1) != '.'){
			source = source.substring(1);
		}
		return source;
	}

	private static void addSwipeFeature(AlertDialog alertDialog){
		Window window = alertDialog.getWindow();
		if(window != null){
			window.requestFeature(Window.FEATURE_SWIPE_TO_DISMISS);
		}
	}

	private static void recolorButtons(AlertDialog alertDialog, Context context) {
		alertDialog.setOnShowListener(dialog -> {
			Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
			Button negative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
			Button neutral = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
			if(negative != null) {
				negative.setTextColor(context.getResources().getColor(R.color.colorAccent));
			}
			if(positive != null) {
				positive.setTextColor(context.getResources().getColor(R.color.colorAccent));
			}
			if(neutral != null){
				neutral.setTextColor(context.getResources().getColor(R.color.colorAccent));
			}
		});
	}

	public static void recolorAlertDialogButtons(AlertDialog alertDialog, Context context){
		addSwipeFeature(alertDialog);
		recolorButtons(alertDialog, context);
	}

	/** Returns string without any spaces
	 *
	 * @param source String in which need to delete spaces
	 * @return String without spaces
	 */
	public static String deleteSpaces(String source){
		if(source.contains(" ")){
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < source.length(); i++) {
				if(source.charAt(i) != ' ') {
					sb.append(source.charAt(i));
				}
			}
			source = sb.toString();
		}
		return source;
	}

	public static String trim(String s){
		if(s == null)
			throw new NullPointerException("Utils.trim: String is null");
		String res = s;
		while(res.length() > 0 && (res.charAt(0) == ' ' || res.charAt(0) == '\n')){
			res = res.substring(1);
		}
		while(res.length() > 0 && (res.charAt(res.length() - 1) == ' ' || res.charAt(res.length() - 1) == '\n')){
			res = res.substring(0, res.length() - 1);
		}
		return res;
	}

	/**
	 * Returns true if string is number (spaces and dot includes)
	 */
	public static boolean isNumber(String s){
		s = deleteSpaces(s);
		try{
			BigDecimal b = null;
			b = new BigDecimal(s);
			return true;
		}catch (NumberFormatException e){
			return false;
		}
	}
}
