package com.maxsavteam.newmcalc.utils;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.maxsavteam.newmcalc.R;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

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

	public static BigDecimal fact(BigDecimal x){
		return BigDecimalMath.factorial(x, new MathContext(10));
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

	public static void saveVariables(ArrayList<MyTuple<Integer, String, String>> a, Context context) {
		String to_save = "";
		for(int i = 0; i < a.size(); i++){
			to_save = String.format("%s%s,%s,%s;", to_save, a.get(i).second, a.get(i).third, a.get(i).first.toString());
		}
		if (a.size() != 0) {
			PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit().putString("variables", to_save).apply();
		} else {
			PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit().remove("variables").apply();
		}
	}

	public static ArrayList<MyTuple<Integer, String, String>> readVariables(Context context) {
		ArrayList<MyTuple<Integer, String, String>> a = new ArrayList<>();
		String var_arr = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getString("variables", null);
		int i = 0;
		if (var_arr == null) {
			return null;
		}

		while (i < var_arr.length()) {
			String name = "";
			while (i < var_arr.length() && var_arr.charAt(i) != ',') {
				name = String.format("%s%s", name, var_arr.charAt(i));
				i++;
			}
			i++;

			String value = "";
			while (i < var_arr.length() && var_arr.charAt(i) != ',') {
				value = String.format("%s%s", value, var_arr.charAt(i));
				i++;
			}
			i++;
			String tag = "";
			while(i < var_arr.length() && var_arr.charAt(i) != ';'){
				tag = String.format("%s%s", tag, var_arr.charAt(i));
				i++;
			}
			//a.add(new Pair<>(Integer.valueOf(tag), new Pair<>(name, value)));
			a.add(MyTuple.create(Integer.parseInt(tag), name, value));
			i++;
		}
		return a;
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
