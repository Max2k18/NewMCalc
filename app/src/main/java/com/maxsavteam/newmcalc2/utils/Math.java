package com.maxsavteam.newmcalc2.utils;

import android.util.Log;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import ch.obermuhlner.math.big.BigDecimalMath;

public class Math {
	private static String TAG = "Math";

	public static final BigDecimal E = new BigDecimal( "2.7182818284590452354" );
	public static final BigDecimal PI = new BigDecimal( "3.14159265358979323846" );
	public static final BigDecimal FI = new BigDecimal( "1.618" );

	private static int mRoundScale = 8;

	public static void setRoundScale(int mRoundScale) {
		Math.mRoundScale = mRoundScale;
	}

	public static BigDecimal exp(BigDecimal x) {
		Log.v( TAG, "exp called with x=" + x.toPlainString() );
		return BigDecimalMath.exp( x, new MathContext( mRoundScale ) );
	}

	public static BigDecimal pow(BigDecimal a, BigDecimal n) {
		BigDecimal ln = ln( a );
		BigDecimal multiplying = n.multiply( ln );
		return exp( multiplying );
	}

	public static BigDecimal ln(BigDecimal x) {
		return BigDecimalMath.log( x, new MathContext( mRoundScale + 2 ) );
	}

	public static BigDecimal log(BigDecimal x) {
		return BigDecimalMath.log10( x, new MathContext( mRoundScale ) );
	}

	public static BigDecimal logWithBase(BigDecimal x, BigDecimal base){
		BigDecimal logX = BigDecimalMath.log10( x, new MathContext( mRoundScale ) ),
				logB = BigDecimalMath.log10( base, new MathContext( mRoundScale ) );
		return logX.divide( logB, mRoundScale, RoundingMode.HALF_EVEN );
	}

	public static BigDecimal abs(BigDecimal x) {
		if ( x.signum() < 0 ) {
			return x.multiply( new BigDecimal( "-1" ) );
		} else {
			return x;
		}
	}

	public static BigDecimal tan(BigDecimal x) {
		return BigDecimalMath.tan( Utils.toRadians( x ), new MathContext( 6 ) );
	}

	public static BigDecimal sin(BigDecimal x) {
		return BigDecimalMath.sin( Utils.toRadians( x ), new MathContext( 6 ) );
	}

	public static BigDecimal cos(BigDecimal x) {
		return BigDecimalMath.cos( Utils.toRadians( x ), new MathContext( 6 ) );
	}

	public static BigDecimal fact(BigDecimal x, int step) {
		if(x.signum() == 0)
			return BigDecimal.ONE;
		BigDecimal ans = BigDecimal.ONE;
		BigDecimal bStep = BigDecimal.valueOf( step );
		for(; 0 <= x.compareTo( BigDecimal.ONE ); x = x.subtract( bStep )){
			ans = ans.multiply( x );
		}
		return ans;
	}

	public static BigDecimal floor(BigDecimal x){
		String s = x.toPlainString();
		int pos = s.indexOf( "." );
		if(pos == -1 || pos == s.length() - 1) {
			return x;
		}else{
			return new BigDecimal( s.substring( 0, pos ) );
		}
	}

	public static BigDecimal ceil(BigDecimal x){
		String s = x.toPlainString();
		int pos = s.indexOf( "." );
		if(pos == -1 || pos == s.length() - 1) {
			return x;
		}else {
			String afterDot = s.substring( pos + 1 );
			if(Utils.deleteZeros( afterDot ).equals( "0" ))
				return new BigDecimal( s.substring( 0, pos ) ); // delete part after dot

			BigDecimal b = abs( new BigDecimal( s.substring( 0, pos ) ) );
			b = b.add( BigDecimal.ONE );
			if(s.charAt( 0 ) == '-')
				b = b.multiply( BigDecimal.valueOf( -1 ) );
			return b;
		}
	}

	public static BigDecimal round(BigDecimal x) {
		String s = x.toPlainString();
		int pos = s.indexOf( "." );
		if ( pos == -1 || pos == s.length() - 1 ) {
			return x;
		} else {
			String newString = s.substring( 0, pos );
			char next = s.charAt( pos + 1 );
			if(next >= '0' && next < '5'){
				return new BigDecimal( newString );
			}else {
				BigDecimal b = abs( new BigDecimal( newString ) );
				b = b.add( BigDecimal.ONE );
				if(newString.charAt( 0 ) == '-')
					b = b.multiply( BigDecimal.valueOf( -1 ) );
				return b;
			}
		}
	}
}
