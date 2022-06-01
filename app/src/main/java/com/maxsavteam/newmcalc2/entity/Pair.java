package com.maxsavteam.newmcalc2.entity;

import androidx.annotation.NonNull;

/**
 * NonNull Pair
 */
public class Pair<F, T> {
	@NonNull
	public final F first;
	@NonNull
	public final T second;

	public Pair(@NonNull F first, @NonNull T second) {
		this.first = first;
		this.second = second;
	}
}
