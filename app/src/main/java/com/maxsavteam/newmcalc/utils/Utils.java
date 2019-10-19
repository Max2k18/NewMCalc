package com.maxsavteam.newmcalc.utils;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.widget.Button;

import com.maxsavteam.newmcalc.R;

import java.math.BigDecimal;
import java.util.ArrayList;

public final class Utils {
	public static boolean isDigit(char c){
		return c >= '0' && c <= '9';
	}

	public static boolean isDigit(String x){
		return x.compareTo("0") >= 0 && x.compareTo("9") <= 0;
	}

	public static boolean islet(char c){
		return c >= 'a' && c <= 'z';
	}

	public static boolean isConstNum(char c, Context context){
		String PI = context.getResources().getString(R.string.pi);
		String FI = context.getResources().getString(R.string.fi);
		return Character.toString(c).equals(PI) || Character.toString(c).equals(FI) || c == 'e';
	}

	public static BigDecimal fact(BigDecimal x){
		BigDecimal ans = BigDecimal.valueOf(1);
		for(BigDecimal i = BigDecimal.valueOf(1); i.compareTo(x) <= 0;){
			ans = ans.multiply(i);
			i = i.add(new BigDecimal(1));
		}
		return ans;
	}

	public static String delete_zeros(String source){
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

	public static void saveVariables(ArrayList<Pair<Integer, Pair<String, String>>> a, Context context){
		String to_save = "";
		for(int i = 0; i < a.size(); i++){
			to_save += a.get(i).second.first + "," + a.get(i).second.second + "," + a.get(i).first.toString() + ";";
		}
		PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit().putString("variables", to_save).apply();
	}

	public static ArrayList<Pair<Integer, Pair<String, String>>> readVariables(Context context){
		ArrayList<Pair<Integer, Pair<String, String>>> a = new ArrayList<>();
		String var_arr = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getString("variables", null);
		int i = 0;
		if(var_arr == null)
			return null;

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

	public static BigDecimal pow(BigDecimal b, int n){
		if(n == 0)
			return BigDecimal.ONE;

		if(n < 0 || n > 999999)
			throw new ArithmeticException("Very big value");

		for(int i = 2; i <= n; i++){
			b = b.multiply(b);
		}

		return b;
	}

	public static boolean isNumber(String s){
		try{
			BigDecimal b = null;
			b = new BigDecimal(s);
			return true;
		}catch (NumberFormatException e){
			return false;
		}
	}
}
