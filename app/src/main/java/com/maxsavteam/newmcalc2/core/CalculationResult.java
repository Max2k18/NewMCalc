package com.maxsavteam.newmcalc2.core;

import com.maxsavteam.calculator.results.ListResult;

import java.math.BigDecimal;

public final class CalculationResult {
	private String mType = null;
	private ListResult mResult = null;

	public final String getType() {
		return mType;
	}

	public final CalculationResult setType(String type) {
		mType = type;
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
