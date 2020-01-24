package com.maxsavteam.newmcalc.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.maxsavteam.newmcalc.types.Tuple;

import java.util.ArrayList;

public class VariableUtils {
	public static void saveVariables(ArrayList<Tuple<Integer, String, String>> a, Context context) {
		String to_save = "";
		for (int i = 0; i < a.size(); i++) {
			to_save = String.format( "%s%s,%s,%s;", to_save, a.get( i ).second, a.get( i ).third, a.get( i ).first.toString() );
		}
		if ( a.size() != 0 ) {
			PreferenceManager.getDefaultSharedPreferences( context.getApplicationContext() ).edit().putString( "variables", to_save ).apply();
		} else {
			PreferenceManager.getDefaultSharedPreferences( context.getApplicationContext() ).edit().remove( "variables" ).apply();
		}
	}

	public static ArrayList<Tuple<Integer, String, String>> readVariables(Context context) {
		ArrayList<Tuple<Integer, String, String>> a = new ArrayList<>();
		String var_arr = PreferenceManager.getDefaultSharedPreferences( context.getApplicationContext() ).getString( "variables", null );
		int i = 0;
		if ( var_arr == null ) {
			return null;
		}

		while ( i < var_arr.length() ) {
			String name = "";
			while ( i < var_arr.length() && var_arr.charAt( i ) != ',' ) {
				name = String.format( "%s%s", name, var_arr.charAt( i ) );
				i++;
			}
			i++;

			String value = "";
			while (i < var_arr.length() && var_arr.charAt(i) != ',') {
				value = String.format( "%s%s", value, var_arr.charAt( i ) );
				i++;
			}
			i++;
			String tag = "";
			while ( i < var_arr.length() && var_arr.charAt( i ) != ';' ) {
				tag = String.format( "%s%s", tag, var_arr.charAt( i ) );
				i++;
			}
			//a.add(new Pair<>(Integer.valueOf(tag), new Pair<>(name, value)));
			a.add( Tuple.create( Integer.parseInt( tag ), name, value ) );
			i++;
		}
		return a;
	}
}
