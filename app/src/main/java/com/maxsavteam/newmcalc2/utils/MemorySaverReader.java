package com.maxsavteam.newmcalc2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.math.BigDecimal;

public class MemorySaverReader {
	private final SharedPreferences sp;

	public MemorySaverReader(Context c){
		sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
	}

	public void save(BigDecimal[] barr){
		String mem = "";
		for(int i = 0; i < 10; i++){
			mem = String.format("%s%s%c", mem, barr[i].toString(), '$');
		}
		sp.edit().putString("memory", mem).apply();
	}

	public BigDecimal[] read(){
		final String def = "0$0$0$0$0$0$0$0$0$0$";
		String memory = sp.getString("memory", def );
		if(memory == null)
			memory = def;

		BigDecimal[] barr = new BigDecimal[10];
		for(int i = 0; i < 10; i++){
			barr[i] = BigDecimal.ZERO;
		}

		String[] strings = memory.split( "\\$" );
		for(int i = 0; i < strings.length; i++){
			barr[i] = new BigDecimal( strings[i] );
		}
		return barr;
	}
}
