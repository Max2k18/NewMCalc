package com.maxsavteam.newmcalc2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.math.BigDecimal;

public class MemorySaverReader {
	private SharedPreferences sp;

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
		String mem_loc = sp.getString("memory", "0$0$0$0$0$0$0$0$0$0$");
		BigDecimal[] barr = new BigDecimal[10];
		for(int i = 0; i < 10; i++){
			barr[i] = BigDecimal.valueOf(0);
		}
		int i = 0, cell = 0;
		while(i < mem_loc.length()){
			String num = "";
			while(mem_loc.charAt(i) != '$'){
				num += mem_loc.charAt(i);
				i++;
			}
			barr[cell] = new BigDecimal(num);
			cell++;
			i++;
		}
		return barr;
	}
}
