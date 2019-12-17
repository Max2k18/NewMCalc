package com.maxsavteam.newmcalc.utils;

import java.util.Locale;

public class CurrentAppLocale {
	private static Locale sLocale = null;

	public static Locale getLocale() {
		if (sLocale == null) {
			return Locale.getDefault();
		}
		return sLocale;
	}

	public static void setLocale(Locale locale) {
		sLocale = locale;
	}
}
