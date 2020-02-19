package com.maxsavteam.newmcalc2.utils;

import android.util.Log;

import java.math.BigDecimal;
import java.math.MathContext;

import ch.obermuhlner.math.big.BigDecimalMath;

public class Math {
	private static String TAG = "Math";

	public static final BigDecimal E = new BigDecimal("2.7182818284590452354");
	public static final BigDecimal PI = new BigDecimal( "3.14159265358979323846" );
	public static final BigDecimal FI = new BigDecimal( "1.618" );

	private static int mRoundScale = 8;

	public static void setRoundScale(int mRoundScale) {
		Math.mRoundScale = mRoundScale;
	}

	public static BigDecimal exp(BigDecimal x){
		Log.v(TAG, "exp called with x=" + x.toPlainString());
		return BigDecimalMath.exp( x, new MathContext( mRoundScale ) );
	}

	public static BigDecimal pow(BigDecimal a, BigDecimal n){
		BigDecimal ln = ln( a );
		BigDecimal multiplying = n.multiply(ln);
		return exp( multiplying );
	}

	public static BigDecimal ln(BigDecimal x){
		return BigDecimalMath.log( x, new MathContext( mRoundScale + 2 ) );
	}

	public static BigDecimal log(BigDecimal x){
		return BigDecimalMath.log10( x, new MathContext( mRoundScale ) );
	}

	public static BigDecimal abs(BigDecimal x){
		if(x.signum() < 0){
			return x.multiply( new BigDecimal( "-1" ) );
		}else{
			return x;
		}
	}

	public static BigDecimal tan(BigDecimal x){
		return BigDecimalMath.tan(Utils.toRadians(x), new MathContext(6));
	}

	public static BigDecimal sin(BigDecimal x){
		return BigDecimalMath.sin(Utils.toRadians(x), new MathContext(6));
	}

	public static BigDecimal cos(BigDecimal x){
		return BigDecimalMath.cos(Utils.toRadians(x), new MathContext(6));
	}

	public static BigDecimal fact(BigDecimal y){
		return BigDecimalMath.factorial(y, new MathContext(mRoundScale));
	}
}
