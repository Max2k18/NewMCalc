package com.maxsavteam.newmcalc.core;

import java.math.BigDecimal;

import javax.annotation.Nullable;

public class CalculationResult {
	private String mType = null;
	private BigDecimal mResult = null;

	@Nullable
	public String getType() {
		return mType;
	}

	CalculationResult setType(String type) {
		mType = type;
		return this;
	}

	@Nullable
	public BigDecimal getResult() {
		return mResult;
	}

	CalculationResult setResult(BigDecimal result) {
		mResult = result;
		return this;
	}
}
