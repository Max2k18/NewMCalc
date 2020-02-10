package com.maxsavteam.newmcalc2.utils;

import androidx.annotation.NonNull;

public class CoreInterruptedError extends java.lang.Error {
	public CoreInterruptedError() {
		super();
	}

	@NonNull
	@Override
	public String toString() {
		return "CoreInterruptedError";
	}
}
