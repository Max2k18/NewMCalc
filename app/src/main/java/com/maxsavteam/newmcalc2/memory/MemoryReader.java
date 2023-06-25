package com.maxsavteam.newmcalc2.memory;

import static com.maxsavteam.newmcalc2.utils.Utils.MEMORY_ENTRIES_COUNT;

import android.content.SharedPreferences;

import com.maxsavteam.calculator.results.NumberList;
import com.maxsavteam.newmcalc2.core.CalculatorWrapper;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MemoryReader {

    public static List<NumberList> read(SharedPreferences sharedPreferences) {
        String memory = sharedPreferences.getString("memory", null);
        if (memory == null) {
            ArrayList<NumberList> results = new ArrayList<>(MEMORY_ENTRIES_COUNT);
            for (int i = 0; i < MEMORY_ENTRIES_COUNT; i++)
                results.add(NumberList.of(BigDecimal.ZERO));
            return results;
        }

        try {
            return parse(memory);
        } catch (JSONException e) {
            e.printStackTrace();

            return parseOldFormat(memory);
        }
    }

    private static List<NumberList> parseOldFormat(String memory) {
        String[] parts = memory.split("\\$");
        List<NumberList> results = new ArrayList<>(parts.length);
        for (String part : parts) {
            results.add(NumberList.of(new BigDecimal(part)));
        }
        for (int i = results.size(); i < MEMORY_ENTRIES_COUNT; i++) {
            results.add(NumberList.of(BigDecimal.ZERO));
        }
        return results;
    }

    private static List<NumberList> parse(String memory) throws JSONException {
        JSONArray jsonArray = new JSONArray(memory);

        ArrayList<NumberList> results = new ArrayList<>();
        for (int i = 0; i < Math.min(MEMORY_ENTRIES_COUNT, jsonArray.length()); i++) {
            results.add(
                    CalculatorWrapper.getInstance().calculate(jsonArray.getString(i) // easiest way to parse list
                    ));
        }
        for (int i = results.size(); i < MEMORY_ENTRIES_COUNT; i++) {
            results.add(NumberList.of(BigDecimal.ZERO));
        }

        return results;
    }

}
