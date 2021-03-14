package com.maxsavteam.newmcalc2.core;

import java.math.BigDecimal;

public final class CalculationResult {
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
