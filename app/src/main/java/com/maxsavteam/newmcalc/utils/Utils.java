package com.maxsavteam.newmcalc.utils;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.maxsavteam.newmcalc.R;

import java.math.BigDecimal;
import java.util.ArrayList;

public final class Utils {
	public static boolean isDigit(char c){
		return c >= '0' && c <= '9';
	}

	public static boolean isDigit(String x){
		char c = x.toCharArray()[0];
		return c >= '0' && c <= '9';
	}

	public static boolean islet(char c){
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
		if(x.toPlainString().contains(".")){
			String numberString = x.toPlainString();
			int positionOfDot = numberString.indexOf(".");
			numberString = numberString.substring(0, positionOfDot);
			x = new BigDecimal(numberString);
		}

		BigDecimal ans = BigDecimal.valueOf(1);
		for(BigDecimal i = BigDecimal.valueOf(1); i.compareTo(x) <= 0;){
			ans = ans.multiply(i);
			i = i.add(new BigDecimal(1));
		}
		return ans;
	}
	/**
	 * @param source String in which need to delete back zeros after dot
	 * @return String without back zeros
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
		return source;
	}

	public static void recolorAlertDialogButtons(AlertDialog alertDialog, Context context){
		Window window = alertDialog.getWindow();
		if (window != null) {
			window.requestFeature(Window.FEATURE_SWIPE_TO_DISMISS);
		}
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

	public static void saveVariables(ArrayList<Pair<Integer, Pair<String, String>>> a, Context context){
		String to_save = "";
		for(int i = 0; i < a.size(); i++){
			//to_save = to_save + (a.get(i).second.first + "," + a.get(i).second.second + "," + a.get(i).first.toString() + ";");
			to_save = String.format("%s%s,%s,%s;", to_save, a.get(i).second.first, a.get(i).second.second, a.get(i).first.toString());
		}
		PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit().putString("variables", to_save).apply();
	}

	public static ArrayList<Pair<Integer, Pair<String, String>>> readVariables(Context context){
		ArrayList<Pair<Integer, Pair<String, String>>> a = new ArrayList<>();
		String var_arr = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getString("variables", null);
		int i = 0;
		if (var_arr == null) {
			return null;
		}

		while (i < var_arr.length()) {
			String name = "";
			while (i < var_arr.length() && var_arr.charAt(i) != ',') {
				name += var_arr.charAt(i);
				i++;
			}
			i++;

			String value = "";
			while (i < var_arr.length() && var_arr.charAt(i) != ',') {
				value += var_arr.charAt(i);
				i++;
			}
			i++;
			String tag = "";
			while(i < var_arr.length() && var_arr.charAt(i) != ';'){
				tag += var_arr.charAt(i);
				i++;
			}
			a.add(new Pair<>(Integer.valueOf(tag), new Pair<>(name, value)));
			i++;
		}
		return a;
	}

	/**
	 * Returns true if string is number (spaces and dot includes)
	 */
	public static boolean isNumber(String s){
		if(s.contains(" ")){
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < s.length(); i++){
				if(s.charAt(i) != ' '){
					sb.append(s.charAt(i));
				}
			}
			s = sb.toString();
		}
		try{
			BigDecimal b = null;
			b = new BigDecimal(s);
			return true;
		}catch (NumberFormatException e){
			return false;
		}
	}
}
