package com.maxsavteam.newmcalc.utils;

import java.math.BigDecimal;

public class Fraction{
	private BigDecimal numerator;
	private BigDecimal denominator;

	public BigDecimal getNumerator() {
		return numerator;
	}

	public BigDecimal getDenominator() {
		return denominator;
	}

	public Fraction(String s){
		s = Utils.deleteZeros(s);
		if(s.contains(".")){
			int pos = s.indexOf(".");
			int n = s.length() - pos - 1;
			this.denominator = BigDecimal.valueOf(10).pow(n);
			StringBuilder sb = new StringBuilder(s);
			sb.deleteCharAt(pos);
			this.numerator = new BigDecimal(sb.toString());
		}else{
			this.numerator = new BigDecimal(s);
			this.denominator = BigDecimal.ONE;
		}
	}
}
