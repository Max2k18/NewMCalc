package com.maxsavteam.newmcalc2.core;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.types.Fraction;
import com.maxsavteam.newmcalc2.utils.CoreInterruptedError;
import com.maxsavteam.newmcalc2.utils.Utils;
import com.maxsavteam.newmcalc2.utils.Utils.Pair;
import com.maxsavteam.newmcalc2.utils.Math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Max Savitsky
 */
public final class CalculationCore {
	private final CoreInterface mCoreInterface;

	private final Resources mResources;
	private final String invalidArgument, valueIsTooBig, divisionByZero;
	private final Context mContext;
	private final String TAG = "Core";
	private final String bracketFloorOpen, bracketFloorClose,
			bracketCeilOpen, bracketCeilClose;
	private final int mRoundScale;

	private final Stack<String> s0 = new Stack<>(); // actions

	private final Stack<BigDecimal> s1 = new Stack<>(); // numbers

	private boolean mWasError = false;

	public CalculationCore(Context context, CoreInterface coreInterface) {
		this.mResources = context.getApplicationContext().getResources();
		this.mContext = context;
		mRoundScale = PreferenceManager.getDefaultSharedPreferences( context.getApplicationContext() ).getInt( "rounding_scale", 8 );
		invalidArgument = mResources.getString( R.string.invalid_argument );
		valueIsTooBig = mResources.getString( R.string.value_is_too_big );
		divisionByZero = mResources.getString( R.string.division_by_zero );

		bracketFloorOpen = mResources.getString( R.string.bracket_floor_open );
		bracketFloorClose = mResources.getString( R.string.bracket_floor_close );
		bracketCeilOpen = mResources.getString( R.string.bracket_ceil_open );
		bracketCeilClose = mResources.getString( R.string.bracket_ceil_close );

		Math.setRoundScale( mRoundScale );

		this.mCoreInterface = coreInterface;
	}

	private boolean isOpenBracket(String str) {
		return str.equals( "(" ) ||
				str.equals( bracketCeilOpen ) ||
				str.equals( bracketFloorOpen );
	}

	private boolean isOpenBracket(char c) {
		String str = Character.toString( c );
		return isOpenBracket( str );
	}

	private boolean isCloseBracket(String str) {
		return str.equals( ")" ) ||
				str.equals( bracketFloorClose ) ||
				str.equals( bracketCeilClose );
	}

	private boolean isCloseBracket(char c) {
		return isCloseBracket( String.valueOf( c ) );
	}

	private String getTypeOfBracket(String bracket) {
		if ( bracket.equals( "(" ) || bracket.equals( ")" ) ) {
			return "simple";
		}
		if ( bracket.equals( bracketFloorClose ) || bracket.equals( bracketFloorOpen ) ) {
			return "floor";
		}
		if ( bracket.equals( bracketCeilOpen ) || bracket.equals( bracketCeilClose ) ) {
			return "ceil";
		}
		return "undefined";
	}

	private String getTypeOfBracket(char c) {
		return getTypeOfBracket( Character.toString( c ) );
	}

	public interface CoreInterface {
		void onSuccess(CalculationResult calculationResult);

		void onError(CalculationError calculationError);
	}

	private void onSuccess(CalculationResult calculationResult) {
		mCoreInterface.onSuccess( calculationResult );
	}

	private void onError(CalculationError calculationError) {
		mCoreInterface.onError( calculationError );
		throw new CoreInterruptedError();
	}

	private final BigDecimal MAX_FACTORIAL_VALUE = new BigDecimal( "1000" );

	private final BigDecimal MAX_POW = new BigDecimal( "10000000000" );

	/**
	 * Performs all necessary checks and changes, and if everything is in order, starts the core (calculation)
	 *
	 * @param exampleArg expression to be calculated
	 * @param type       type of calculation. Can be null
	 * @throws NullPointerException throws, when interface hasn't been set
	 * @see CalculationResult
	 */
	public void prepareAndRun(@NotNull final String exampleArg, @Nullable String type) throws NullPointerException, CoreInterruptedError {
		String example = String.copyValueOf( exampleArg.toCharArray() );
		int len = example.length();
		char last;
		if ( len == 0 ) {
			return;
		} else {
			last = example.charAt( len - 1 );
		}

		if ( last == '(' ) {
			return;
		}

		if ( example.contains( bracketFloorOpen ) || example.contains( bracketCeilOpen ) || example.contains( "(" ) ) {
			Stack<Character> bracketsStack = new Stack<>();
			for (int i = 0; i < example.length(); i++) {
				char cur = example.charAt( i );
				if ( isOpenBracket( cur ) ) {
					bracketsStack.push( cur );
				} else if ( isCloseBracket( cur ) ) {
					try {
						bracketsStack.pop();
					} catch (EmptyStackException e) {
						e.printStackTrace();
						mWasError = true;
						onError( new CalculationError().setStatus( "Core" ) );
						return;
					}
				}
			}
			if ( !bracketsStack.isEmpty() ) {
				while ( !bracketsStack.isEmpty() ) {
					String br = "";
					String typeOf = getTypeOfBracket( bracketsStack.peek() );
					switch ( typeOf ) {
						case "simple":
							br = ")";
							break;
						case "floor":
							br = bracketFloorClose;
							break;
						case "ceil":
							br = bracketCeilClose;
							break;
					}
					Log.v( TAG, "appending brackets; bracketsStack size=" + bracketsStack.size() + "; bracket type=" + typeOf + " bracket=" + br );
					example = String.format( "%s%s", example, br );
					bracketsStack.pop();
				}
				Log.v( TAG, "example after appending ex=" + example );
			}
		}
		if ( example.contains( " " ) ) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < example.length(); i++) {
				if ( example.charAt( i ) != ' ' ) {
					sb.append( example.charAt( i ) );
				}
			}
			example = sb.toString();
		}
		if ( Utils.isNumber( example ) ) {
			onError( new CalculationError().setStatus( "Core" ).setErrorMessage( "String is number" ).setPossibleResult( new BigDecimal( example ) ) );
			return;
		}
		if ( example.contains( mResources.getString( R.string.multiply ) ) || example.contains( mResources.getString( R.string.div ) )
				|| example.contains( mResources.getString( R.string.pi ) ) || example.contains( mResources.getString( R.string.fi ) )
				|| example.contains( mResources.getString( R.string.sqrt ) ) ) {
			char[] mas = example.toCharArray();
			String p;

			for (int i = 0; i < example.length(); i++) {
				p = Character.toString( mas[ i ] );
				if ( p.equals( mResources.getString( R.string.div ) ) ) {
					mas[ i ] = '/';
				} else if ( p.equals( mResources.getString( R.string.multiply ) ) ) {
					mas[ i ] = '*';
				} else if ( p.equals( mResources.getString( R.string.pi ) ) ) {
					mas[ i ] = 'P';
				} else if ( p.equals( mResources.getString( R.string.fi ) ) ) {
					mas[ i ] = 'F';
				} else if ( p.equals( mResources.getString( R.string.sqrt ) ) ) {
					mas[ i ] = 'R';
				}
			}
			example = new String( mas );
		}

		calculate( example, type );
	}

	private static class MovedExample {

		String subExample;
		int newPos;

		MovedExample(String subExample, int newPos) {
			this.subExample = subExample;
			this.newPos = newPos;
		}

	}

	private BigDecimal rootWithBase(BigDecimal a, BigDecimal n) {
		Log.v( TAG, "rootBase called with a=" + a.toPlainString() + " n=" + n.toPlainString() );

		BigDecimal log = Math.ln( a );
		BigDecimal dLog = log.divide( n, 10, RoundingMode.HALF_EVEN );
		return Math.exp( dLog );
	}

	private BigDecimal pow(BigDecimal a, BigDecimal n) {
		Log.v( TAG, "pow called with a=" + a.toPlainString() + " n=" + n.toPlainString() );
		if ( Fraction.isFraction( n ) ) {
			Fraction fraction = new Fraction( n.toPlainString() );
			return rootWithBase( sysPow( a, fraction.getNumerator() ), fraction.getDenominator() );
		}
		if ( n.compareTo( BigDecimal.ZERO ) < 0 ) {
			BigDecimal result = sysPow( a, n.multiply( BigDecimal.valueOf( -1 ) ) );
			String strRes = BigDecimal.ONE.divide( result, 8, RoundingMode.HALF_EVEN ).toPlainString();
			return new BigDecimal( Utils.deleteZeros( strRes ) );
		} else {
			return sysPow( a, n );
		}
	}

	private BigDecimal sysPow(BigDecimal a, BigDecimal n) {
		if ( Thread.currentThread().isInterrupted() ) {
			Log.v( TAG, "sysPow stopped" );
			//throw new RuntimeException( "Stopped because thread waa interrupted" );
			throw new CoreInterruptedError();
		}
		Log.v( TAG, "sysPow called with a=" + a.toPlainString() + " and n=" + n.toPlainString() );
		if ( n.compareTo( BigDecimal.ZERO ) == 0 ) {
			return BigDecimal.ONE;
		}
		Log.v( TAG, "remainder=" + Utils.getRemainder( n, BigDecimal.valueOf( 2 ) ).toPlainString() );
		if ( Utils.getRemainder( n, BigDecimal.valueOf( 2 ) ).compareTo( BigDecimal.ONE ) == 0 ) {
			return sysPow( a, n.subtract( BigDecimal.ONE ) ).multiply( a );
		} else {
			BigDecimal b = sysPow( a, n.divide( BigDecimal.valueOf( 2 ), 0, RoundingMode.HALF_EVEN ) );
			return b.multiply( b );
		}
	}

	private MovedExample getSubExampleFromBrackets(String example, int pos) {
		int i = pos + 1;
		String subExample = "";
		int bracketsLvl = 0;
		while ( bracketsLvl != 0 || !isCloseBracket( example.charAt( i ) ) ) {
			char now = example.charAt( i );
			if ( isOpenBracket( now ) ) {
				bracketsLvl++;
			} else if ( isCloseBracket( now ) ) {
				bracketsLvl--;
			}

			subExample = String.format( "%s%c", subExample, now );
			i++;
		}
		return new MovedExample( subExample, i );
	}

	private Pair<BigDecimal, Integer> getResultFromBrackets(String example, int pos) {
		String bracket = String.valueOf( example.charAt( pos ) );
		MovedExample movedExample = getSubExampleFromBrackets( example, pos );
		String subExample = movedExample.subExample;
		pos = movedExample.newPos;
		CoreSubProcess coreSubProcess = new CoreSubProcess( mContext );
		BigDecimal result;
		if ( Utils.isNumber( subExample ) ) {
			result = new BigDecimal( subExample );
		} else {
			coreSubProcess.run( subExample );
			result = coreSubProcess.getResult();
			if ( result == null ) {
				mWasError = true;
				onError( coreSubProcess.getError() );
				return null;
			}
		}

		String bracketType = getTypeOfBracket( bracket );
		switch ( bracketType ) {
			case "simple":
				return new Pair<>( result, pos );
			case "ceil":
				return new Pair<>( Math.ceil( result ), pos );
			case "floor":
				return new Pair<>( Math.floor( result ), pos );
			default:
				mWasError = true;
				onError( new CalculationError().setStatus( "Core" ).setErrorMessage( "type of bracket is undefined" ) );
				return null;
		}
	}

	private void calculate(String example, String type) throws CoreInterruptedError {
		s0.clear();
		s1.clear();
		mWasError = false;
		String x;
		String s;
		int len = example.length();

		for (int i = 0; i < len; i++) {

			if ( mWasError || Thread.currentThread().isInterrupted() ) {
				Log.v( TAG, "Main loop destroyer was called; mWasError=" + mWasError + "; Thread.currentThread().isInterrupted()=" + Thread.currentThread().isInterrupted() );
				throw new CoreInterruptedError();
			}

			try {
				s = Character.toString( example.charAt( i ) );

				if ( isOpenBracket( s ) ) {
					Pair<BigDecimal, Integer> result = getResultFromBrackets( example, i );
					if ( result == null ) {
						return;
					}
					i = result.second;
					s1.push( result.first );

					continue;
				}

				switch ( s ) {
					case "s":
					case "t":
					case "l":
					case "c":
					case "a":
					case "r":
						if ( i != 0 ) {
							if ( isCloseBracket( example.charAt( i - 1 ) ) ) {
								s0.push( "*" );
							}
						}
						String let = "";
						while ( i < example.length() && example.charAt( i ) != 'e' && Utils.isLetter( example.charAt( i ) ) ) {
							let = String.format( "%s%c", let, example.charAt( i ) );
							i++;
						}
						i--;
						s0.push( let );
						continue;
					case "P":
					case "F": {
						BigDecimal f;
						if ( s.equals( "P" ) ) {
							f = Math.PI;
						} else {
							f = Math.FI;
						}
						s1.push( f );
						if ( i != 0 && Utils.isDigit( example.charAt( i - 1 ) ) ) {
							in_s0( '*' );
						}
						if ( i != example.length() - 1 ) {
							char next = example.charAt( i + 1 );
							if ( Utils.isDigit( example.charAt( i + 1 ) ) || next == 'F' || next == 'P' || next == 'e' ) {
								in_s0( '*' );
							}
						}
						//s1.push(f);
						continue;
					}
					case "!":
						try {
							if ( i != len - 1 && example.charAt( i + 1 ) == '!' ) {
								BigDecimal y = s1.peek(), ans = BigDecimal.ONE;
								boolean isNumberBigger = y.compareTo( MAX_FACTORIAL_VALUE ) > 0;
								if ( y.signum() < 0 || isNumberBigger ) {
									mWasError = true;
									if ( isNumberBigger ) {
										onError( new CalculationError().setErrorMessage( "Invalid argument: factorial value is too much" ).setShortError( valueIsTooBig ) ); // I do not know how to name this error
										return;
									}
									break;
								}
								for (; y.compareTo( BigDecimal.valueOf( 0 ) ) > 0; y = y.subtract( BigDecimal.valueOf( 2 ) )) {
									ans = ans.multiply( y );
								}
								i++;
								s1.pop();
								s1.push( ans );
								continue;
							} else {
								BigDecimal y = s1.peek();
								if ( y.signum() < 0 ) {
									mWasError = true;
									onError( new CalculationError().setErrorMessage( "Error: Unable to find negative factorial." ).setShortError( invalidArgument ) );
									return;
								} else {
									if ( y.compareTo( MAX_FACTORIAL_VALUE ) > 0 ) {
										mWasError = true;
										onError( new CalculationError().setErrorMessage( "For some reason, we cannot calculate the factorial of this number " +
												"(because it is too large and may not have enough device resources when executed)" ).setShortError( valueIsTooBig ) );
										return;
									} else {
										s1.pop();
										s1.push( Math.fact( y ) );
									}
								}
								if ( i != len - 1 ) {
									char next = example.charAt( i + 1 );
									if ( Utils.isDigit( next ) || next == 'P' || next == 'F' || next == 'e' ) {
										in_s0( '*' );
									}
								}
								continue;
							}
						} catch (Exception e) {
							e.printStackTrace();
							mWasError = true;
							onError( new CalculationError().setErrorMessage( e.toString() ).setMessage( e.getMessage() ).setShortError( valueIsTooBig ) );
							return;
						}
					case "%":
						if ( s0.empty() || ( !s0.empty() && !Utils.isBasicAction( s0.peek() ) ) ) {
							BigDecimal y = s1.peek();
							s1.pop();
							y = y.divide( BigDecimal.valueOf( 100 ), mRoundScale, RoundingMode.HALF_EVEN );
							y = new BigDecimal( Utils.deleteZeros( y.toPlainString() ) );
							s1.push( y );
							if ( i != len - 1 ) {
								char next = example.charAt( i + 1 );
								if ( Utils.isDigit( next ) || next == 'P' || next == 'F' || next == 'e' ) {
									in_s0( '*' );
								}
							}
							continue;
						} else if ( !s0.empty() && Utils.isBasicAction( s0.peek() ) ) {
							i++;
							String x1 = "";
							int brackets = 0;
							while ( i < example.length() ) {
								if ( brackets == 0 && ( example.charAt( i ) == '-' || example.charAt( i ) == '+' ) ) {
									break;
								}
								if ( brackets == 0 && isCloseBracket( example.charAt( i ) ) ) {
									break;
								}

								if ( isOpenBracket( example.charAt( i ) ) ) {
									brackets++;
								} else if ( isCloseBracket( example.charAt( i ) ) ) {
									brackets--;
								}

								x1 = String.format( "%s%c", x1, example.charAt( i ) );
								i++;
							}
							x1 = s1.peek().toPlainString() + x1;
							s1.pop();
							CoreSubProcess coreSubProcess = new CoreSubProcess( mContext );
							BigDecimal top;
							if ( Utils.isNumber( x1 ) ) {
								top = new BigDecimal( x1 );
							} else {
								coreSubProcess.run( x1 );
								top = coreSubProcess.getResult();
								if ( top == null ) {
									mWasError = true;
									onError( coreSubProcess.getError() );
									return;
								}
							}

							top = top.divide( BigDecimal.valueOf( 100 ), mRoundScale, RoundingMode.HALF_EVEN );
							top = new BigDecimal( Utils.deleteZeros( top.toPlainString() ) );
							//s1.push(top);
							BigDecimal finalResult = s1.peek(), percent;
							s1.pop();
							percent = finalResult.multiply( top );
							if ( !s0.empty() ) {
								String action = s0.peek();
								s0.pop();

								if ( Utils.isBasicAction( action ) ) {
									switch ( action ) {
										case "+":
											finalResult = finalResult.add( percent );
											break;
										case "-":
											finalResult = finalResult.subtract( percent );
											break;
										case "*":
											finalResult = finalResult.multiply( percent );
											break;
										case "/":
											finalResult = finalResult.divide( percent, mRoundScale, RoundingMode.HALF_EVEN );
											finalResult = new BigDecimal( Utils.deleteZeros( finalResult.toPlainString() ) );
											break;
									}
									finalResult = new BigDecimal( Utils.deleteZeros( finalResult.toPlainString() ) );
									s1.push( finalResult );
								}
							}
							i--;
							continue;
						}
						break;
					case "e": {
						BigDecimal f = Math.E;
						s1.push( f );
						if ( i != 0 && Utils.isDigit( example.charAt( i - 1 ) ) ) {
							in_s0( '*' );
						}
						if ( i != example.length() - 1 && Utils.isDigit( example.charAt( i + 1 ) ) ) {
							in_s0( '*' );
						}
						continue;
					}
					case "R":
						if ( i == len - 1 ) {
							mWasError = true;
							break;
						} else {
							in_s0( 'R' );
							continue;
						}
					case "A": {
						i += 2;
						String n = "";
						int actions = 0;
						while ( example.charAt( i ) != ')' ) {
							if ( example.charAt( i ) == '+' ) {
								actions++;
								s1.push( new BigDecimal( n ) );
								n = "";
							} else {
								n = String.format( "%s%c", n, example.charAt( i ) );
							}
							i++;
						}
						s1.push( new BigDecimal( n ) );
						BigDecimal sum = BigDecimal.ZERO;
						for (int j = 0; j <= actions; j++) {
							sum = sum.add( s1.peek() );
							s1.pop();
						}
						sum = sum.divide( BigDecimal.valueOf( actions + 1 ), mRoundScale, RoundingMode.HALF_EVEN );
						String answer = sum.toPlainString();
						s1.push( new BigDecimal( Utils.deleteZeros( answer ) ) );
						continue;
					}
					case "G": {
						i += 2;
						String n = "";
						int actions = 0;
						while ( example.charAt( i ) != ')' ) {
							if ( example.charAt( i ) == '*' ) {
								actions++;
								s1.push( new BigDecimal( n ) );
								n = "";
							} else {
								n = String.format( "%s%c", n, example.charAt( i ) );
							}
							i++;
						}
						s1.push( new BigDecimal( n ) );
						BigDecimal pr = BigDecimal.ONE;
						for (int j = 0; j <= actions; j++) {
							pr = pr.multiply( s1.peek() );
							s1.pop();
						}
						//sum = BigDecimal.valueOf(Math.sqrt(sum.doubleValue())); // BigDecimal has method BigDecimal.abs(), but it is available in Java 9 and high, Android uses Java 8
						//pr = BigDecimalMath.sqrt( pr, new MathContext( 10 ) );
						pr = rootWithBase( pr, BigDecimal.valueOf( actions + 1 ) );
						String answer = pr.toPlainString();
						s1.push( new BigDecimal( Utils.deleteZeros( answer ) ) );
						continue;
					}
					case "^":
						in_s0( '^' );
						continue;
				}
				if ( Utils.isDigit( s ) ) {
					x = "";
					while ( i < example.length() && ( example.charAt( i ) == '.' || Utils.isDigit( example.charAt( i ) ) ) ) {
						x = String.format( "%s%c", x, example.charAt( i ) );
						i++;
					}
					s1.push( new BigDecimal( x ) );
					i--;
				} else {
					if ( s.equals( "-" ) && ( i == 0 || isOpenBracket( example.charAt( i - 1 ) ) || Utils.isLetter( example.charAt( i - 1 ) ) ) ) {
						s0.push( "-*" );
					}else {
						in_s0( example.charAt( i ) );
					}
				}
			} catch (Exception e) {
				mWasError = true;
				break;
			}
		}
		try {
			while ( !mWasError && s0.size() > 0 ) {
				mult( s0.peek() );
				s0.pop();
			}
		} catch (EmptyStackException e) {
			mWasError = true;
		}
		if(mWasError){
			onError( new CalculationError().setStatus( "Core" ) );
		}
		if ( !mWasError && s0.empty() ) {
			onSuccess( new CalculationResult().setResult( s1.peek() ).setType( type ) );
		}
	}

	private void mult(String x) {
		if ( Thread.currentThread().isInterrupted() ) {
			throw new CoreInterruptedError();
		}
		try {
			if ( x.length() == 3 || x.equals( "ln" ) || x.equals( "R" ) || x.equals( "-*" ) ) {
				double d = s1.peek().doubleValue();
				BigDecimal operand = s1.peek();
				BigDecimal ans = BigDecimal.ONE;
				s1.pop();
				//d = Math.toRadians(d);
				int roundScale = mRoundScale;
				switch ( x ) {
					case "cos": {
						//ans = BigDecimal.valueOf(Math.cos(Math.toRadians(d)));
						ans = Math.cos( operand );
						roundScale = 7;
						Log.v( TAG, "cos of " + operand.toPlainString() + " = " + ans.toPlainString() );
						break;
					}
					case "sin": {
						ans = Math.sin( operand );
						roundScale = 7;
						break;
					}
					case "tan": {
						if ( operand.equals( BigDecimal.valueOf( 90 ) ) ) {
							mWasError = true;
							onError( new CalculationError().setShortError( "Impossible to find tan of 90" ) );
							return;
						}
						roundScale = 7;
						ans = Math.tan( operand );
						break;
					}
					case "log": {
						if ( operand.signum() <= 0 ) {
							mWasError = true;
							onError( new CalculationError().setErrorMessage( "Illegal argument: unable to find log of " + ( d == 0 ? "zero." : "negative number." ) ).setShortError( invalidArgument ) );
							return;
						}
						//ans = BigDecimal.valueOf(Math.log10(d));
						ans = Math.log( operand );
						break;
					}
					case "abs":
						ans = Math.abs( operand );
						break;
					case "rnd":
						ans = Math.round( operand );
						break;
					case "ln": {
						if ( operand.signum() <= 0 ) {
							mWasError = true;
							onError( new CalculationError().setErrorMessage( "Illegal argument: unable to find ln of " + ( d == 0 ? "zero." : "negative number." ) ).setShortError( invalidArgument ) );
							return;
						}
						//ans = BigDecimal.valueOf(Math.log(d));
						ans = Math.ln( operand );
						break;
					}
					case "R":
						if ( operand.signum() < 0 ) {
							mWasError = true;
							onError( new CalculationError().setErrorMessage( "Invalid argument: the root expression cannot be negative." ).setShortError( invalidArgument ) );
							return;
						}
						//ans = BigDecimalMath.sqrt(operand, new MathContext(9));
						ans = rootWithBase( operand, BigDecimal.valueOf( 2 ) );
						break;
					case "-*":
						ans = operand.multiply( BigDecimal.valueOf( -1 ) );
						break;
				}
				ans = ans.setScale( roundScale, RoundingMode.HALF_EVEN );
				String answer = ans.toPlainString();
				s1.push( new BigDecimal( Utils.deleteZeros( answer ) ) );
				return;
			}
			BigDecimal b = s1.peek();
			s1.pop();
			BigDecimal a = s1.peek();
			BigDecimal ans = s1.peek();
			s1.pop();
			try {
				switch ( x ) {
					case "+":
						ans = a.add( b );
						break;
					case "-":
						ans = a.subtract( b );
						break;
					case "*":
						ans = a.multiply( b );
						break;
					case "/":
						if ( b.signum() == 0 ) {
							mWasError = true;
							onError( new CalculationError().setErrorMessage( "Division by zero." ).setShortError( divisionByZero ) );
							return;
						}
						ans = a.divide( b, mRoundScale, RoundingMode.HALF_EVEN );
						break;

					case "^":
						if ( b.compareTo( MAX_POW ) > 0 ) {
							mWasError = true;
							onError( new CalculationError().setShortError( valueIsTooBig ) );
							return;
						} else if ( a.signum() == 0 ) {
							if ( b.signum() < 0 ) {
								mWasError = true;
								onError( new CalculationError().setShortError( "Not a number" ) );
							} else if ( b.signum() == 0 ) {
								mWasError = true;
								onError( new CalculationError().setShortError( "Undefined" ) );
							} else {
								ans = BigDecimal.ZERO;
							}
						} else {
							//ans = Math.pow( a, b );
							ans = pow( a, b );
							Log.v( TAG, "pow answer returned; ans=" + ans );
						}
						break;
				}
				if ( ans.scale() > 20 ) {
					ans = ans.setScale( 20, RoundingMode.HALF_EVEN );
				}
				String answer = ans.toPlainString();
				ans = new BigDecimal( Utils.deleteZeros( answer ) );
				s1.push( ans );
			} catch (ArithmeticException e) {
				String str = e.toString();
				if ( str.contains( "Infinity or Nan" ) ) {
					mWasError = true;
					onError( new CalculationError().setErrorMessage( e.toString() ).setShortError( valueIsTooBig ) );
				} else {
					mWasError = true;
					onError( new CalculationError().setErrorMessage( e.toString() ).setMessage( e.getMessage() ) );
				}
			}
		} catch (EmptyStackException e) {
			mWasError = true;
			onError( new CalculationError().setStatus( "Core" ).setErrorMessage( e.toString() ) );
		} catch (Exception e) {
			mWasError = true;
			onError( new CalculationError().setErrorMessage( e.toString() ).setMessage( e.getMessage() ) );
		}
	}

	private void in_s0(char x) {
		in_s0( Character.toString( x ) );
	}

	private void in_s0(String x) {
		if ( Thread.currentThread().isInterrupted() ) {
			throw new CoreInterruptedError();
		}

		Map<String, Integer> priority = new HashMap<>();
		priority.put( "-*", 0 );

		priority.put( "-", 1 );
		priority.put( "+", 1 );
		priority.put( "/", 2 );
		priority.put( "*", 2 );

		priority.put( "^", 3 );
		priority.put( "R", 3 );

		priority.put( "cos", 3 );
		priority.put( "tan", 3 );
		priority.put( "sin", 3 );
		priority.put( "ln", 3 );
		priority.put( "log", 3 );
		priority.put( "abs", 3 );
		priority.put( "rnd", 3 );

		Log.i( TAG, "in_s0 called with x=" + x + "; s0.size()=" + s0.size() );

		if ( s0.empty() ) {
			s0.push( x );
			return;
		}

		Integer priorityOfX = priority.get( x );
		Integer priorityOfTopAction = priority.get( s0.peek() );

		if ( priorityOfX == null || priorityOfTopAction == null ) {
			Log.v( TAG, "in_s0: priority.get returned null on " + s0.peek() );

			NullPointerException nullPointerException = new NullPointerException( "Method: in_s0; priorityOfX equals null or priorityOfTopAction equals null" );
			nullPointerException.printStackTrace();
			throw nullPointerException;
		}

		if ( priorityOfX < priorityOfTopAction || priorityOfX.equals( priorityOfTopAction ) ) {
			mult( s0.peek() );
			s0.pop();
			in_s0( x );
		}else {
			s0.push( x );
		}
	}

	public static final class CalculationResult {
		private String mType = null;
		private BigDecimal mResult = null;

		public final String getType() {
			return mType;
		}

		public final CalculationResult setType(String type) {
			mType = type;
			return this;
		}

		public final BigDecimal getResult() {
			return mResult;
		}

		public final CalculationResult setResult(BigDecimal result) {
			mResult = result;
			return this;
		}
	}
}

