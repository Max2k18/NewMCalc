package com.maxsavteam.newmcalc2.utils;

import android.content.SharedPreferences;

import com.maxsavteam.calculator.results.NumberList;
import com.maxsavteam.newmcalc2.core.CalculatorWrapper;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MemorySaverReader {
	private final SharedPreferences sp;

	public static final int MEMORY_ENTRIES_COUNT = 10;

	public MemorySaverReader() {
		sp = Utils.getDefaultSP();
	}

	public void save(ArrayList<NumberList> results) {
		if(results == null){
			sp.edit().remove( "memory" ).apply();
			return;
		}
		JSONArray jsonArray = new JSONArray();
		for(NumberList r : results){
			jsonArray.put( r.format() );
		}
		sp.edit().putString( "memory", jsonArray.toString() ).apply();
	}

	public BigDecimal[] readOld() {
		final String def = "0$0$0$0$0$0$0$0$0$0$";
		String memory = sp.getString( "memory", def );
		if ( memory == null ) {
			memory = def;
		}

		BigDecimal[] barr = new BigDecimal[ 10 ];
		for (int i = 0; i < 10; i++) {
			barr[ i ] = BigDecimal.ZERO;
		}

		String[] strings = memory.split( "\\$" );
		for (int i = 0; i < strings.length; i++) {
			barr[ i ] = new BigDecimal( strings[ i ] );
		}
		return barr;
	}

	public ArrayList<NumberList> read() {
		String memory = sp.getString( "memory", null );
		if ( memory == null ) {
			ArrayList<NumberList> results = new ArrayList<>( MEMORY_ENTRIES_COUNT );
			for (int i = 0; i < MEMORY_ENTRIES_COUNT; i++)
				results.add( NumberList.of( BigDecimal.ZERO ) );
			return results;
		}

		try {
			JSONArray jsonArray = new JSONArray( memory );

			ArrayList<NumberList> results = new ArrayList<>();
			for(int i = 0; i < Math.min(MEMORY_ENTRIES_COUNT, jsonArray.length()); i++){
				results.add(
						CalculatorWrapper.getInstance().calculate( jsonArray.getString( i ) // easiest way to parse list
				) );
			}
			// expand, because in future number of entries can be increased
			while(results.size() < MEMORY_ENTRIES_COUNT){
				results.add( NumberList.of( BigDecimal.ZERO ) );
			}

			return results;
		} catch (JSONException e) {
			e.printStackTrace();

			ArrayList<NumberList> results = new ArrayList<>();
			BigDecimal[] b = readOld();
			for(int i = 0; i < Math.min( 10, MEMORY_ENTRIES_COUNT ); i++){
				results.add( NumberList.of( b[i] ) );
			}

			// expand, because in future number of entries can be increased, but old method returns only 10 entries
			while(results.size() < MEMORY_ENTRIES_COUNT){
				results.add( NumberList.of( BigDecimal.ZERO ) );
			}

			return results;
		}
	}

}
