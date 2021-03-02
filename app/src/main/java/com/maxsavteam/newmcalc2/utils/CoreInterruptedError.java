package com.maxsavteam.newmcalc2.utils;

import androidx.annotation.NonNull;

public class CoreInterruptedError extends Error {
	@NonNull
	@Override
	public String toString() {
		return "CoreInterruptedError";
	}
}
