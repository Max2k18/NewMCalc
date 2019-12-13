package com.maxsavteam.newmcalc.core;

import java.math.BigDecimal;

import javax.annotation.Nullable;

public final class CalculationResult {
	private String mType = null;
	private BigDecimal mResult = null;

	@Nullable
	public final String getType() {
		return mType;
	}

	public final CalculationResult setType(String type) {
		mType = type;
		return this;
	}

	@Nullable
	public final BigDecimal getResult() {
		return mResult;
	}

	public final CalculationResult setResult(BigDecimal result) {
		mResult = result;
		return this;
	}
}
