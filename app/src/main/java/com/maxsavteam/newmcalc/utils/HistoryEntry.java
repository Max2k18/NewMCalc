package com.maxsavteam.newmcalc.utils;

public final class HistoryEntry {
	public final String example;
	public final String answer;

	public HistoryEntry(String example, String answer){
		this.example = example;
		this.answer = answer;
	}

	public static HistoryEntry create(String ex, String ans){
		return new HistoryEntry(ex, ans);
	}
}
