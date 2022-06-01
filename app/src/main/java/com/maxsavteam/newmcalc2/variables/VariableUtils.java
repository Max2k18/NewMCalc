package com.maxsavteam.newmcalc2.variables;

import com.maxsavteam.newmcalc2.entity.Tuple;
import com.maxsavteam.newmcalc2.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VariableUtils {

	public static ArrayList<Variable> readVariables(){
		String s = Utils.getDefaultSP().getString( "variables", "[]" );
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(s);
		} catch (JSONException e) {
			e.printStackTrace();
			ArrayList<Tuple<Integer, String, String>> variables = readVariablesLegacy();
			if(variables != null){
				ArrayList<Variable> newVariables = new ArrayList<>();
				for(Tuple<Integer, String, String > tuple : variables){
					newVariables.add( new Variable( tuple.second, tuple.third, tuple.first ) );
				}
				saveVariables( newVariables );
				return newVariables;
			}else{
				return new ArrayList<>();
			}
		}

		ArrayList<Variable> variables = new ArrayList<>();
		for(int i = 0; i < jsonArray.length(); i++){
			try {
				JSONObject jsonObject = jsonArray.getJSONObject( i );
				variables.add( new Variable( jsonObject.getString( "name" ), jsonObject.getString( "value" ), jsonObject.getInt( "tag" ) ) );
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return variables;
	}

	public static void saveVariables(ArrayList<Variable> variables){
		JSONArray jsonArray = new JSONArray();
		for(Variable variable : variables){
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put( "tag", variable.getTag() );
				jsonObject.put( "name", variable.getName() );
				jsonObject.put( "value", variable.getValue() );
				jsonArray.put( jsonObject );
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		String s = jsonArray.toString();
		Utils.getDefaultSP().edit().putString( "variables", s ).apply();
	}

	public static ArrayList<Tuple<Integer, String, String>> readVariablesLegacy() {
		ArrayList<Tuple<Integer, String, String>> a = new ArrayList<>();
		String var_arr = Utils.getDefaultSP().getString( "variables", null );
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
