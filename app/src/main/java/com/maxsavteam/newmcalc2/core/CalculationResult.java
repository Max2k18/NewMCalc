package com.maxsavteam.newmcalc2.core;

import com.maxsavteam.calculator.results.ListResult;

public final class CalculationResult {
	private ListResult mResult = null;

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

	public ListResult getResult() {
		return mResult;
	}

	public CalculationResult setResult(ListResult result) {
		mResult = result;
		return this;
	}
}
