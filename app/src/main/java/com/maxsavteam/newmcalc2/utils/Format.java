package com.maxsavteam.newmcalc2.utils;

public class Format {
	/**
	 * Returns string with formatted last number (if last symbol is digit)
	 * */
	public static String format(String text){
		if(text.equals("") || text.length() < 4)
			return text;

		return core(text);
	}

	private static String core(String txt){
		String number = "";
		int spaces = 0, dot_pos = -1, len = txt.length(), i = len - 1, nums = 0;
		for(; i >= 0 && (Utils.isDigit(txt.charAt(i)) || txt.charAt(i) == ' ' || txt.charAt(i) == '.'); i--){
			if(txt.charAt(i) != ' '){
				number = String.format("%c%s", txt.charAt(i), number);
			}else
				spaces++;
		}
		txt = txt.substring(0, len - number.length() - spaces);
		len = number.length();
		if(len < 4)
			return txt + number;
		if(number.contains(".")){
			dot_pos = number.indexOf(".");
		}
		String number_on_ret;
		if(dot_pos == -1)
			number_on_ret = "";
		else
			number_on_ret = number.substring(dot_pos);
		for(i = (dot_pos == -1 ? len - 1 : number.indexOf(".") - 1); i >= 0; i--, nums++){
			if(nums != 0 && nums % 3 == 0){// && (dot_pos == -1 || i < dot_pos)){
				number_on_ret = String.format(" %s", number_on_ret);
			}
			number_on_ret = number.charAt(i) + number_on_ret;
		}
		return txt + number_on_ret;
	}
}
