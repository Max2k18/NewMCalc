package com.maxsavteam.newmcalc2.types;

import com.maxsavteam.newmcalc2.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Fraction {
	private BigDecimal numerator; // числитель
	private BigDecimal denominator; // знаменатель

	public BigDecimal getNumerator() {
		return numerator;
	}

	public BigDecimal getDenominator() {
		return denominator;
	}

	private BigDecimal gcd(BigDecimal f, BigDecimal s){
		BigDecimal a = new BigDecimal( f.toPlainString() );
		BigDecimal b = new BigDecimal( s.toPlainString() );
		while(!a.equals( BigDecimal.ZERO ) && !b.equals( BigDecimal.ZERO )){
			if(a.compareTo(b) > 0){
				a = a.remainder( b );
			}else{
				b = b.remainder( a );
			}
		}

		return a.add(b);
	}

	public Fraction(String fraction){
		String s = Utils.deleteZeros(fraction);
		if(s.contains(".")){
			int pos = s.indexOf(".");
			int n = s.length() - pos - 1;
			this.denominator = BigDecimal.valueOf(10).pow(n);
			StringBuilder sb = new StringBuilder(s);
			sb.deleteCharAt(pos);
			this.numerator = new BigDecimal(sb.toString());

			BigDecimal g = gcd( denominator, numerator );
			while(!g.equals( BigDecimal.ONE )){
				denominator = denominator.divide(g, RoundingMode.HALF_EVEN);
				numerator = numerator.divide(g, RoundingMode.HALF_EVEN);

				g = gcd( denominator, numerator );
			}
		}else{
			this.numerator = new BigDecimal(s);
			this.denominator = BigDecimal.ONE;
		}
	}

	public static boolean isFraction(BigDecimal a){
		return a.toPlainString().contains( "." );
	}
}