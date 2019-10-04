package com.maxsavteam.newmcalc.utils;

import java.math.BigDecimal;

public final class Utils {
	public static boolean isdigit(char c){
		return c >= '0' && c <= '9';
	}

	public static boolean isdigit(String x){
		return x.compareTo("0") >= 0 && x.compareTo("9") <= 0;
	}

	public static boolean islet(char c){
		return c >= 'a' && c <= 'z';
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
		if(source.charAt(len - 1) == '0'){
			while(source.charAt(len - 1) == '0' || source.charAt(len - 1) == '.'){
				len--;
				source = source.substring(0, len);
			}
		}
		return source;
	}
}
