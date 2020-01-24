package com.maxsavteam.newmcalc.types;

public class Tuple<F, S, T> {
	public F first;
	public S second;
	public T third;

	public Tuple(F a, S b, T c) {
		this.first = a;
		this.second = b;
		this.third = c;
	}

	public static <F, S, T> Tuple<F, S, T> create(F f, S s, T t) {
		return new Tuple<>( f, s, t );
	}
}
