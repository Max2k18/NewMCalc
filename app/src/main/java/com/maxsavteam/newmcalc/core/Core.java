package com.maxsavteam.newmcalc.core;

import android.content.Context;
import android.content.res.Resources;

import com.maxsavteam.newmcalc.R;
import com.maxsavteam.newmcalc.error.Error;
import com.maxsavteam.newmcalc.utils.Fraction;
import com.maxsavteam.newmcalc.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ch.obermuhlner.math.big.BigDecimalMath;

/**
 * @author Maks
 * */
public final class Core {
	private CoreLinkBridge coreLinkBridge;
	private boolean was_error = false;
	private Resources res;
	private String invalidArgument, valueIsTooBig, divisionByZero;
	private Context c;
	private String mExample, mType;

	public Core(Context context) {
		this.res = context.getApplicationContext().getResources();
		this.c = context;
		invalidArgument = res.getString(R.string.invalid_argument);
		valueIsTooBig = res.getString(R.string.value_is_too_big);
		divisionByZero = res.getString(R.string.division_by_zero);
	}

	public final void setInterface(CoreLinkBridge clb){
		this.coreLinkBridge = clb;
	}
	
	public interface CoreLinkBridge{
		void onSuccess(CalculationResult calculationResult);
		void onError(Error error);
	}
	
	private void onSuccess(CalculationResult calculationResult){
		coreLinkBridge.onSuccess(calculationResult);
		//currentThread().interrupt();
	}

	private void onError(Error error){
		coreLinkBridge.onError(error);
		//currentThread().interrupt();
	}

	/**
	 * Is used for actions
	 * */
	private final Stack<String> s0 = new Stack<>();

	/**
	 * Is used for numbers
	 * */
	private final Stack<BigDecimal> s1 = new Stack<>();

	private final BigDecimal MAX_FACTORIAL_VALUE = new BigDecimal("1000");
	private final BigDecimal MAX_POW = new BigDecimal("1000");
	
	/**
	 * Performs all necessary checks and changes, and if everything is in order, starts the core (calculation)
	 *
	 * @param example expression to be calculated
	 * @param type type of calculation. Not necessary
	 * @throws NullPointerException throws, when interface hasn't been set
	 *
	 * @see CalculationResult
	 */
	public void prepareAndRun(@NotNull String example, String type) throws NullPointerException{
		if(coreLinkBridge == null)
			throw new NullPointerException("Calculation Core: Interface wasn't set");
		int len = example.length();
		char last;
		if(len == 0)
			return;
		else
			last = example.charAt(len - 1);

		if(last == '(')
			return;

		int brackets = 0;
		if(example.contains("(") || example.contains(")")) {
			for (int i = 0; i < example.length(); i++) {
				if (example.charAt(i) == '(')
					brackets++;
				else if (example.charAt(i) == ')')
					brackets--;
			}
			if (brackets > 0) {
				for (int i = 0; i < brackets; i++) {
					example = String.format("%s%s", example, ")");
				}
			} else if (brackets < 0) {
				was_error = true;
				onError(new Error().setStatus("Core"));
				return;
			}
		}
		if(example.contains(" ")){
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < example.length(); i++) {
				if (example.charAt(i) != ' ')
					sb.append(example.charAt(i));
			}
			example = sb.toString();
		}
		if(Utils.isNumber(example)){
			onError(new Error().setStatus("Core").setError("String is number").setPossibleResult(new BigDecimal(example)));
			return;
		}
		if(example.contains(res.getString(R.string.multiply)) || example.contains(res.getString(R.string.div))
				|| example.contains(res.getString(R.string.pi)) || example.contains(res.getString(R.string.fi))
				|| example.contains(res.getString(R.string.sqrt))){
			char[] mas = example.toCharArray();
			String p;

			for(int i = 0; i < example.length(); i++){
				p = Character.toString(mas[i]);
				if(p.equals(res.getString(R.string.div))){
					mas[i] = '/';
				}else if(p.equals(res.getString(R.string.multiply))){
					mas[i] = '*';
				}else if(p.equals(res.getString(R.string.pi))){
					mas[i] = 'P';
				}else if(p.equals(res.getString(R.string.fi))){
					mas[i] = 'F';
				}else if(p.equals(res.getString(R.string.sqrt))){
					mas[i] = 'R';
				}
			}
			example = new String(mas);
		}
		calculate(example, type);
	}

	private void calculate(String example, String type){
		s0.clear();
		s1.clear();
		was_error = false;
		String x;
		String s;
		int len = example.length();

		for(int i = 0; i < len; i++){
			try {
				s = Character.toString(example.charAt(i));
				if (s.equals("s") || s.equals("t") || s.equals("l") || s.equals("c")) {
					if (i != 0) {
						if (example.charAt(i - 1) == ')') {
							s0.push("*");
						}
					}
					//if(i + 4 <= example.length()){
					String let = "";
					while (i < example.length() && Utils.isLetter(example.charAt(i))) {
						let = String.format("%s%c", let, example.charAt(i));
						i++;
					}
					switch (let) {
						case "sin":
							s0.push("sin");
							s0.push("(");
							break;
						case "cos":
							s0.push("cos");
							s0.push("(");
							break;
						case "tan":
							s0.push("tan");
							s0.push("(");
							break;
						case "log":
							s0.push("log");
							s0.push("(");
							break;
						case "ln":
							s0.push("ln");
							s0.push("(");
							break;
					}
					continue;
				}
				if (s.equals("P")) {
					BigDecimal f = new BigDecimal(Math.PI);
					s1.push(f);
					if (i != 0 && Utils.isDigit(example.charAt(i - 1))) {
						in_s0('*');
					}
					char next = '\0';
					if (i != example.length() - 1)
						next = example.charAt(i + 1);
					if (i != example.length() - 1 && (Utils.isDigit(example.charAt(i + 1)) || next == 'F' || next == 'P' || next == 'e')) {
						in_s0('*');
					}
					//s1.push(f);
					continue;
				} else if (s.equals("F")) {
					BigDecimal f = new BigDecimal(1.618);
					s1.push(f);
					if (i != 0 && Utils.isDigit(example.charAt(i - 1))) {
						in_s0('*');
					}
					if (i != example.length() - 1) {
						char next = example.charAt(i + 1);
						if (Utils.isDigit(example.charAt(i + 1)) || next == 'F' || next == 'P' || next == 'e') {
							in_s0('*');
						}
					}
					continue;
				} else if (s.equals("!")) {
					try {
						if (i != len - 1 && example.charAt(i + 1) == '!') {
							BigDecimal y = s1.peek(), ans = BigDecimal.ONE;
							boolean isNumberBigger = y.compareTo(MAX_FACTORIAL_VALUE) > 0;
							if (y.signum() < 0 || isNumberBigger) {
								was_error = true;
								if (isNumberBigger)
									onError(new Error().setError("Invalid argument: factorial value is too much").setShortError(valueIsTooBig)); // I do not know how to name this error
								break;
							}
							for (; y.compareTo(BigDecimal.valueOf(0)) > 0; y = y.subtract(BigDecimal.valueOf(2))) {
								ans = ans.multiply(y);
							}
							i++;
							s1.pop();
							s1.push(ans);
							continue;
						} else {
							BigDecimal y = s1.peek();
							if (y.signum() < 0) {
								was_error = true;
								onError(new Error().setError("Error: Unable to find negative factorial.").setShortError(invalidArgument));
								break;
							} else {
								if (y.compareTo(MAX_FACTORIAL_VALUE) > 0) {
									was_error = true;
									onError(new Error().setError("For some reason, we cannot calculate the factorial of this number " +
											"(because it is too large and may not have enough device resources when executed)").setShortError(valueIsTooBig));
									break;
								} else {
									s1.pop();
									s1.push(Utils.fact(y));
								}
							}
							if (i != len - 1) {
								char next = example.charAt(i + 1);
								if (Utils.isDigit(next) || next == 'P' || next == 'F' || next == 'e')
									in_s0('*');
							}
							continue;
						}
					} catch (Exception e) {
						e.printStackTrace();
						was_error = true;
						onError(new Error().setError(e.toString()).setMessage(e.getMessage()).setShortError(valueIsTooBig));
						break;
					}
				} else if (s.equals("%")) {
					if (s0.empty() || (!s0.empty() && !Utils.isBasicAction(s0.peek()))) {
						BigDecimal y = s1.peek();
						s1.pop();
						y = y.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN);
						y = new BigDecimal(Utils.deleteZeros(y.toPlainString()));
						s1.push(y);
						if (i != len - 1) {
							char next = example.charAt(i + 1);
							if (Utils.isDigit(next) || next == 'P' || next == 'F' || next == 'e')
								in_s0('*');
						}
						continue;
					} else if (!s0.empty() && Utils.isBasicAction(s0.peek())) {
						try {

							class IsolatedCoreProcess implements CoreLinkBridge {
								private BigDecimal res;
								private Context mContext;

								private Error getError() {
									return error;
								}

								private boolean isWas_error() {
									return was_error;
								}

								private Error error;
								private boolean was_error = false;

								public BigDecimal getRes() {
									return res;
								}

								@Override
								public void onSuccess(CalculationResult calculationResult) {
									res = calculationResult.getResult();
								}

								@Override
								public void onError(Error error) {
									was_error = true;
									this.error = error;
									if(error.getStatus().equals("Core")) {
										if (error.getError().contains("String is number")) {
											res = error.getPossibleResult();
										}
									}
								}

								private void run(String ex) {
									Core core = new Core(mContext);
									core.setInterface(this);

									core.prepareAndRun(ex, "");
								}

								private IsolatedCoreProcess(Context context){
									this.mContext = context;
								}
							}
							i++;
							String x1 = "";
							int brackets = 0;
							while (i < example.length()) {
								if(brackets == 0 && (example.charAt(i) == '-' || example.charAt(i) == '+')){
									break;
								}
								if(brackets == 0 && example.charAt(i) == ')')
									break;

								if(example.charAt(i) == '(')
									brackets++;
								else if(example.charAt(i) == ')')
									brackets--;

								x1 = String.format("%s%c", x1, example.charAt(i));
								i++;
							}
							x1 = s1.peek().toPlainString() + x1;
							s1.pop();
							IsolatedCoreProcess isolatedCoreProcess = new IsolatedCoreProcess(c);
							isolatedCoreProcess.run(x1);
							if (isolatedCoreProcess.isWas_error() && !isolatedCoreProcess.getError().getError().contains("String is number")) {
								was_error = true;
								onError(isolatedCoreProcess.getError());
								return;
							} else {
								BigDecimal top;
								top = isolatedCoreProcess.getRes();
								top = top.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN);
								top = new BigDecimal(Utils.deleteZeros(top.toPlainString()));
								//s1.push(top);
								BigDecimal finalResult = s1.peek(), percent;
								s1.pop();
								percent = finalResult.multiply(top);
								if (!s0.empty()) {
									String action = s0.peek();
									s0.pop();

									if (Utils.isBasicAction(action)) {
										switch (action) {
											case "+":
												finalResult = finalResult.add(percent);
												break;
											case "-":
												finalResult = finalResult.subtract(percent);
												break;
											case "*":
												finalResult = finalResult.multiply(percent);
												break;
											case "/":
												finalResult = finalResult.divide(percent, 10, RoundingMode.HALF_EVEN);
												finalResult = new BigDecimal(Utils.deleteZeros(finalResult.toPlainString()));
												break;
										}
										finalResult = new BigDecimal(Utils.deleteZeros(finalResult.toPlainString()));
										s1.push(finalResult);
									}
								}

							}
							i--;
							continue;
						} catch (Exception e) {
							e.printStackTrace();
							was_error = true;
							onError(new Error().setError(e.toString()).setMessage(e.getMessage()).setShortError(valueIsTooBig));
							break;
						}
					}
				} else if (s.equals("e")) {
					BigDecimal f = new BigDecimal(Math.E);
					s1.push(f);
					if (i != 0 && Utils.isDigit(example.charAt(i - 1))) {
						in_s0('*');
					}
					if (i != example.length() - 1 && Utils.isDigit(example.charAt(i + 1))) {
						in_s0('*');
					}
					continue;
				} else if (s.equals("R")) {
					if (i == len - 1) {
						was_error = true;
						break;
					} else {
						if (example.charAt(i + 1) == '(') {
							in_s0('R');
							continue;
						} else {
							was_error = true;
							onError(new Error().setError("Invalid statement for square root").setShortError(invalidArgument));
							break;
						}
					}
				} else if (s.equals("A")) {
					i += 2;
					String n = "";
					int actions = 0;
					while (example.charAt(i) != ')') {
						if (example.charAt(i) == '+') {
							actions++;
							s1.push(new BigDecimal(n));
							n = "";
						} else {
							n = String.format("%s%c", n, example.charAt(i));
						}
						i++;
					}
					s1.push(new BigDecimal(n));
					BigDecimal sum = BigDecimal.ZERO;
					for (int j = 0; j <= actions; j++) {
						sum = sum.add(s1.peek());
						s1.pop();
					}
					sum = sum.divide(BigDecimal.valueOf(actions + 1), 2, RoundingMode.HALF_EVEN);
					String answer = sum.toPlainString();
					s1.push(new BigDecimal(Utils.deleteZeros(answer)));
					continue;
				} else if (s.equals("G")) {
					i += 2;
					String n = "";
					int actions = 0;
					while (example.charAt(i) != ')') {
						if (example.charAt(i) == '*') {
							actions++;
							s1.push(new BigDecimal(n));
							n = "";
						} else {
							n = String.format("%s%c", n, example.charAt(i));
						}
						i++;
					}
					s1.push(new BigDecimal(n));
					BigDecimal sum = BigDecimal.ONE;
					for (int j = 0; j <= actions; j++) {
						sum = sum.multiply(s1.peek());
						s1.pop();
					}
					sum = BigDecimal.valueOf(Math.sqrt(sum.doubleValue())); // BigDecimal has method BigDecimal.abs(), but it is available in Java 9 and high, Android uses Java 8
					String answer = sum.toPlainString();
					s1.push(new BigDecimal(Utils.deleteZeros(answer)));
					continue;
				}
				if (Utils.isDigit(example.charAt(i))) {
					x = "";
					while ((i < example.length()) && ((example.charAt(i) == '.') || Utils.isDigit(example.charAt(i)) || (example.charAt(i) == '-' && example.charAt(i - 1) == 'E'))) {
						x = String.format("%s%c", x, example.charAt(i));
						i++;
					}
					BigDecimal temp = new BigDecimal(x);
					if (temp.divide(BigDecimal.ONE, 2, RoundingMode.HALF_DOWN).equals(BigDecimal.valueOf(3.14))) {
						x = Double.toString(Math.PI);
					} else if (temp.divide(BigDecimal.ONE, 3, RoundingMode.FLOOR).equals(BigDecimal.valueOf(1.618))) {
						x = "1.618";
					}
					s1.push(new BigDecimal(x));
					i--;
				} else {
					if (example.charAt(i) != ')') {
						if (example.charAt(i) == '^') {
							if (i != example.length() - 1 && example.charAt(i + 1) == '(') {
								i++;
								in_s0('^');
								s0.push("(");
								continue;
							} else if (i != example.length() - 1 && example.charAt(i + 1) != '(') {
								//i++;
								in_s0('^');
								continue;
							}
						}
						if ((i == 0 && example.charAt(i) == '-') || (example.charAt(i) == '-' && example.charAt(i - 1) == '(')) {
							x = "";
							i++;
							while ((i < example.length()) && ((example.charAt(i) == '.') || Utils.isDigit(example.charAt(i)) || example.charAt(i) == 'E' || (example.charAt(i) == '-' && example.charAt(i - 1) == 'E'))) {
								x = String.format("%s%c", x, example.charAt(i));
								i++;
							}
							i--;
							s1.push(new BigDecimal(x).multiply(BigDecimal.valueOf(-1)));
							continue;
						}
						if (i != 0 && example.charAt(i) == '(' && (Utils.isDigit(example.charAt(i - 1)) || example.charAt(i - 1) == ')')) {
							in_s0('*');
						}

						in_s0(example.charAt(i));
					} else {
						while (!s0.empty() && !s0.peek().equals("(")) {
							mult(s0.peek());
							if (was_error)
								break;
							s0.pop();
						}
						if (!s0.empty() && s0.peek().equals("(")) {
							s0.pop();
						}
						if (i != example.length() - 1) {
							if (Utils.isDigit(example.charAt(i + 1))) {
								in_s0('*');
							}
						}
					}
				}
			}catch (Exception e){
				was_error = true;
				onError(new Error().setStatus("Core").setShortError("Smth went wrong"));
				break;
			}
		}
		try {
			while (!was_error && !s0.isEmpty() && s1.size() >= 2) {
				mult(s0.peek());
				if (was_error)
					break;
				s0.pop();
			}
			if (!was_error && !s0.isEmpty() && s1.size() == 1) {
				if (s0.peek().equals("R")) {
					mult(s0.peek());
					s0.pop();
				}
				if (!s0.isEmpty() && (s0.peek().equals("cos") || s0.peek().equals("sin") || s0.peek().equals("log") || s0.peek().equals("ln") || s0.peek().equals("tan"))) {
					mult(s0.peek());
					s0.pop();
				}
			}
		}catch (Exception e) {
			was_error = true;
			onError(new Error().setStatus("Core"));
		}
		if(!was_error){
			onSuccess(new CalculationResult().setResult(s1.peek()).setType(type));
		}
	}

	private void mult(String x) throws Exception {
		try {
			if (x.length() == 3 || x.equals("ln") || x.equals("R")) {
				double d = s1.peek().doubleValue();
				BigDecimal bigDecimal = s1.peek();
				BigDecimal ans = BigDecimal.ONE;
				if (x.equals("log") && d <= 0) {
					was_error = true;
					onError(new Error().setError("You cannot find the logarithm of a zero or a negative number.").setShortError(invalidArgument));
					return;
				}
				s1.pop();
				//d = Math.toRadians(d);
				switch (x) {
					case "cos": {
						//ans = BigDecimal.valueOf(Math.cos(Math.toRadians(d)));
						ans = BigDecimalMath.cos(Utils.toRadians(bigDecimal), new MathContext(9));
						break;
					}
					case "sin": {
						ans = BigDecimalMath.sin(Utils.toRadians(bigDecimal), new MathContext(9));
						break;
					}
					case "tan": {
						ans = BigDecimalMath.tan(Utils.toRadians(bigDecimal), new MathContext(9));
						break;
					}
					case "log": {
						if (bigDecimal.signum() <= 0) {
							was_error = true;
							onError(new Error().setError("Illegal argument: unable to find log of " + (d == 0 ? "zero." : "negative number.")).setShortError(invalidArgument));
							return;
						}
						//ans = BigDecimal.valueOf(Math.log10(d));
						ans = BigDecimalMath.log10(bigDecimal, new MathContext(9));
						break;
					}
					case "ln": {
						if (bigDecimal.signum() <= 0) {
							was_error = true;
							onError(new Error().setError("Illegal argument: unable to find ln of " + (d == 0 ? "zero." : "negative number.")).setShortError(invalidArgument));
							return;
						}
						//ans = BigDecimal.valueOf(Math.log(d));
						ans = BigDecimalMath.log(bigDecimal, new MathContext(9));
						break;
					}
					case "R":
						if (bigDecimal.signum() < 0) {
							was_error = true;
							onError(new Error().setError("Invalid argument: the root expression cannot be negative.").setShortError(invalidArgument));
							return;
						}
						ans = BigDecimalMath.sqrt(bigDecimal, new MathContext(9));
						break;
				}
				ans = ans.divide(BigDecimal.valueOf(1.0), 9, RoundingMode.HALF_EVEN);
				String answer = ans.toPlainString();
				s1.push(new BigDecimal(Utils.deleteZeros(answer)));
				return;
			}
			BigDecimal b = s1.peek();
			s1.pop();
			BigDecimal a = s1.peek();
			BigDecimal ans = s1.peek();
			s1.pop();
			double a1, b1;
			a1 = a.doubleValue();
			b1 = b.doubleValue();
			try {
				switch (x) {
					case "+":
						ans = a.add(b);
						break;
					case "-":
						ans = a.subtract(b);
						break;
					case "*":
						ans = a.multiply(b);
						break;
					case "/":
						if (b.signum() == 0) {
							was_error = true;
							onError(new Error().setError("Division by zero.").setShortError(divisionByZero));
							return;
						}
						ans = a.divide(b, 9, RoundingMode.HALF_EVEN);
						break;
					case "^":
						if(b.compareTo(MAX_POW) > 0){
							was_error = true;
							onError(new Error().setShortError(valueIsTooBig));
							return;
						}else if(b.signum() == 0 && a.signum() == 0){
							was_error = true;
							onError(new Error().setShortError("Raising zero to zero degree."));
							return;
						}
						String power = b.toPlainString();
						power = Utils.deleteZeros(power);
						if(power.contains(".")){
							Fraction fraction = new Fraction(power);
							a = BigDecimalMath.pow(a, fraction.getNumerator(), new MathContext(10));
							ans = BigDecimalMath.exp(
									BigDecimalMath.log(
											a,
											new MathContext(20)).divide(fraction.getDenominator(),
											20, RoundingMode.HALF_EVEN),
									new MathContext(8));
						}else{
							BigDecimal n = new BigDecimal(power);
							ans = BigDecimalMath.pow(a, n, new MathContext(8));
						}
						break;
				}
				String answer = ans.toPlainString();
				ans = new BigDecimal(Utils.deleteZeros(answer));
				s1.push(ans);
			} catch (ArithmeticException e) {
				String str = e.toString();
				if (str.contains("Non-terminating decimal expansion; no exact representable decimal result")) {
					ans = a.divide(b, 4, RoundingMode.HALF_EVEN);
					ans = new BigDecimal(Utils.deleteZeros(ans.toPlainString()));
					s1.push(ans);
				}else if(str.contains("Infinity or Nan")){
					was_error = true;
					onError(new Error().setError(e.toString()).setShortError(valueIsTooBig));
				} else {
					was_error = true;
					onError(new Error().setError(e.toString()).setMessage(e.getMessage()));
				}
			}
		}catch(EmptyStackException e){
			was_error = true;
			onError(new Error().setStatus("Core").setError(e.toString()));
		}catch (Exception e) {
			was_error = true;
			onError(new Error().setError(e.toString()).setMessage(e.getMessage()));
			throw new Exception(e.getMessage());
		}
	}

	private void in_s0(char x) throws Exception{
		Map<String, Integer> priority = new HashMap<>();
		priority.put("(", 0);
		priority.put("-", 1);
		priority.put("+", 1);
		priority.put("/", 2);
		priority.put("*", 2);
		priority.put("^", 3);
		priority.put("R", 3);

		if(s0.empty()) {
			s0.push(Character.toString(x));
			return;
		}

		Integer priorityOfX = priority.get(Character.toString(x));
		Integer priorityOfTopAction = priority.get(s0.peek());

		if(priorityOfX == null || priorityOfTopAction == null)
			throw new NullPointerException("Method: in_s0; priorityOfX equals null or priorityOfTopAction equals null");

		if(x == '(') {
			s0.push(Character.toString(x));
			return;
		}

		if(s0.peek().equals("(")){
			s0.push(Character.toString(x));
			return;
		}
		if (priorityOfX < priorityOfTopAction || priorityOfX.equals(priority.get(s0.peek()))) {
			mult(s0.peek());
			s0.pop();
			in_s0(x);
			return;
		}
		if(priorityOfX > priorityOfTopAction) {
			s0.push(Character.toString(x));
		}
	}
}
