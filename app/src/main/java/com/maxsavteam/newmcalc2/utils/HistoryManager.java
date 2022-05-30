package com.maxsavteam.newmcalc2.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.maxsavteam.calculator.results.NumberList;
import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.core.CalculatorWrapper;
import com.maxsavteam.newmcalc2.types.HistoryEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HistoryManager {

	public final static char HISTORY_DESC_SEPARATOR = (char) 31;
	public final static char HISTORY_IN_ENTRY_SEPARATOR = (char) 30;
	public final static char HISTORY_ENTRIES_SEPARATOR = (char) 29;
	private static final String TAG = Main2Activity.TAG + " HistoryManager";

	private final SharedPreferences sharedPreferences;

	private final ArrayList<HistoryEntry> historyEntries = new ArrayList<>();

	private static HistoryManager instance;

	public static HistoryManager getInstance() {
		if ( instance == null ) {
			instance = new HistoryManager();
		}
		return instance;
	}

	private HistoryManager() {
		sharedPreferences = Utils.getDefaultSP();
		try {
			String historyString = sharedPreferences.getString( "history", null );
			JSONObject jsonObject;
			if ( historyString != null && !historyString.isEmpty() ) {
				if ( checkForCompatibility( historyString ) ) {
					jsonObject = new JSONObject( historyString );
				} else {
					jsonObject = new JSONObject( reformat( historyString ) );
					sharedPreferences.edit().putString( "history", jsonObject.toString() ).apply();
				}
			} else {
				jsonObject = new JSONObject()
						.put( "history", new JSONArray() );
				sharedPreferences.edit().putString( "history", jsonObject.toString() ).apply();
			}

			JSONArray historyArray = jsonObject.getJSONArray( "history" );
			for (int i = 0; i < historyArray.length(); i++) {
				JSONObject element = historyArray.getJSONObject( i );
				String example = element.getString( "example" );
				Log.i( TAG, example );
				try {
					NumberList result = CalculatorWrapper.getInstance().calculate( example );
					HistoryEntry entry = new HistoryEntry(
							element.getString( "example" ),
							result.format(),
							element.optString( "description", "" )
					);
					historyEntries.add( entry );
				}catch (Exception e){
					Log.i( TAG, "" + e );
				}

			}

		} catch (JSONException e) {
			e.printStackTrace();
			Log.i( TAG, "HistoryManager: " + e );
		}
	}

	public ArrayList<HistoryEntry> getHistory() {
		return new ArrayList<>( historyEntries );
	}

	public HistoryManager put(HistoryEntry entry) {
		historyEntries.add( 0, entry );
		return this;
	}

	public HistoryManager remove(int index) {
		historyEntries.remove( index );
		return this;
	}

	public HistoryManager change(int index, HistoryEntry entry) {
		historyEntries.set( index, entry );
		return this;
	}

	public HistoryManager clear() {
		historyEntries.clear();
		return this;
	}

	public void save() {
		try {
			JSONObject history = new JSONObject();
			JSONArray historyArray = new JSONArray();
			for (HistoryEntry entry : historyEntries) {
				historyArray.put( entry.getJSON() );
			}
			history.put( "history", historyArray );

			sharedPreferences.edit().putString( "history", history.toString() ).apply();
		} catch (JSONException e) {
			e.printStackTrace();
			Log.i( TAG, "save: " + e );
		}
	}

	private String reformat(String his) throws JSONException {
		JSONArray historyArray = new JSONArray();
		int i = 0;
		while ( i < his.length() ) {// && his.charAt(i) != Constants.HISTORY_ENTRIES_SEPARATOR){
			boolean was_dot = false;
			String ex = "";
			String ans = "";
			while ( i < his.length() ) {
				if ( his.charAt( i ) == HistoryManager.HISTORY_ENTRIES_SEPARATOR ) {
					i++;
					break;
				}
				if ( his.charAt( i ) == HistoryManager.HISTORY_IN_ENTRY_SEPARATOR ) {
					i++;
					was_dot = true;
					continue;
				}
				if ( !was_dot ) {
					ex = String.format( "%s%c", ex, his.charAt( i ) );
				} else {
					ans = String.format( "%s%c", ans, his.charAt( i ) );
				}
				i++;
			}
			String description = null;
			if ( ex.contains( Character.toString( HistoryManager.HISTORY_DESC_SEPARATOR ) ) ) {
				int j = 0;
				while ( j < ex.length() && ex.charAt( j ) != HistoryManager.HISTORY_DESC_SEPARATOR ) {
					j++;
				}
				description = ex.substring( j + 1 );
				ex = ex.substring( 0, j );
			}
			historyArray.put( new JSONObject()
					.put( "example", ex )
					.put( "answer", ans )
					.put( "description", description ) );
		}
		return new JSONObject()
				.put( "history", historyArray )
				.toString();
	}

	private boolean checkForCompatibility(String h) {
		try {
			JSONObject jsonObject = new JSONObject( h );
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}


}
