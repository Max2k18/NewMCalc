package com.maxsavteam.newmcalc2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Pair;

import java.util.ArrayList;

public class HistoryStorageProtocolsFormatter {
	private final SharedPreferences sp;

	public HistoryStorageProtocolsFormatter(Context context){
		sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}

	private ArrayList<Pair<Pair<String, String>, String>> readStorageWithFirstProtocolVersion(String history) throws StringIndexOutOfBoundsException{
		ArrayList<Pair<Pair<String, String>, String>> formatted = new ArrayList<>();
		try {
			if (history != null) {
				int i;
				while (true) {
					i = 0;
					while (i < history.length()) {
						if (history.charAt(i) == '&')
							i++;
						else {
							if (history.charAt(i) == ';')
								break;
						}
						i++;
					}
					String expr = history.substring(0, i);
					if (i != history.length() - 1)
						history = history.substring(i + 1);
					else
						break;

					int j = expr.length() - 1;
					String example = "", result = "";
					while (j >= 0 && expr.charAt(j) != ',') {
						result = String.format("%c%s", expr.charAt(j), result);
						j--;
					}
					j--;
					while (j >= 0) {
						example = String.format("%c%s", expr.charAt(j), example);
						j--;
					}
					String description = null;
					if (example.contains("~")) {
						int pos = example.indexOf("~");
						description = example.substring(pos + 1);
						example = example.substring(0, pos);
					}
					formatted.add(new Pair<>(new Pair<>(example, description), result));
				}

			}
		}catch (StringIndexOutOfBoundsException e){
			throw new StringIndexOutOfBoundsException(e.getMessage());
		}
		return formatted;
	}

	private String handleDataForSecondProtocolVersion(ArrayList<Pair<Pair<String, String>, String>> data){
		String newHistory = "";
		for(int i = 0; i < data.size(); i++){
			newHistory = String.format("%s%s", newHistory, data.get(i).first.first);
			if(data.get(i).first.second != null){
				newHistory = String.format("%s%c%s", newHistory,  ( (char) 31), data.get(i).first.second);
			}
			newHistory = String.format("%s%c%s%c", newHistory, (char) 30, data.get(i).second, (char) 29);
		}
		return newHistory;
	}

	public void reformatHistory (int from, int to) throws StringIndexOutOfBoundsException{
		String history = sp.getString("history", null);
		ArrayList<Pair<Pair<String, String>, String>> formatted = new ArrayList<>();
		try {
			if (from == 1)
				formatted = readStorageWithFirstProtocolVersion(history);

			String newHistory = "";
			if (to == 2)
				newHistory = handleDataForSecondProtocolVersion(formatted);

			sp.edit().putString("history", newHistory).putInt("local_history_storage_protocol_version", to).apply();
		}catch (StringIndexOutOfBoundsException e){
			throw new StringIndexOutOfBoundsException(e.getMessage());
		}
	}
}
