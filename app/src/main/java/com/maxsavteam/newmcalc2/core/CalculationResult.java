package com.maxsavteam.newmcalc2.core;

import com.maxsavteam.calculator.results.List;

public final class CalculationResult {
	private List mResult = null;

	private String expression;

	private CalculationMode mode;

	public CalculationMode getMode() {
		return mode;
	}

	public CalculationResult setMode(CalculationMode mode) {
		this.mode = mode;
		return this;
	}

	public String getExpression() {
		return expression;
	}

	public CalculationResult setExpression(String expression) {
		this.expression = expression;
		return this;
	}

	public List getResult() {
		return mResult;
	}

	public CalculationResult setResult(List result) {
		mResult = result;
		return this;
	}
}
