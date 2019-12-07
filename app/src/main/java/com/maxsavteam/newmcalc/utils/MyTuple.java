package com.maxsavteam.newmcalc.utils;

public class MyTuple<F, S, T> {
	public F first;
	public S second;
	public T third;

	/**
	 * Instead constructor use create()
	 * */
	private MyTuple(F a, S b, T c) {
		this.first = a;
		this.second = b;
		this.third = c;
	}

	public static <F, S, T> MyTuple<F, S, T> create(F f, S s, T t) {
		return new MyTuple<>(f, s, t);
	}
}
