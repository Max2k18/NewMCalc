package com.maxsavteam.newmcalc2.memory;

import android.content.SharedPreferences;

import com.maxsavteam.calculator.results.NumberList;

import org.json.JSONArray;

import java.util.List;

public class MemorySaver {

    public static void save(SharedPreferences sharedPreferences, List<NumberList> results) {
        if (results == null) {
            sharedPreferences.edit().remove("memory").apply();
            return;
        }
        JSONArray jsonArray = new JSONArray();
        for (NumberList r : results) {
            jsonArray.put(r.format());
        }
        sharedPreferences.edit().putString("memory", jsonArray.toString()).apply();
    }

}
