package com.maxsavteam.newmcalc2.entity;

public class Tuple<F, S, T> {
	public final F first;
	public final S second;
	public final T third;

	public Tuple(F a, S b, T c) {
		this.first = a;
		this.second = b;
		this.third = c;
	}

	public static <F, S, T> Tuple<F, S, T> create(F f, S s, T t) {
		return new Tuple<>( f, s, t );
	}
}
