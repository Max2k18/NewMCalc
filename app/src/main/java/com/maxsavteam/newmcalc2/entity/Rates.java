package com.maxsavteam.newmcalc2.entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class Rates {

	private final Map<String, Map<String, Double>> map;

	public Rates(Map<String, Map<String, Double>> map) {
		this.map = map;
	}

	public double getExchangeRate(String source, String target){
		var sourceMap = map.get(source);
		if(sourceMap == null)
			throw new IllegalArgumentException("No rates found for source: " + source);
		if(!sourceMap.containsKey(target))
			throw new IllegalArgumentException("No rates found for target: " + target);
		//noinspection ConstantConditions
		return sourceMap.get( target );
	}

	public String toJsonString() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		for (Map.Entry<String, Map<String, Double>> entry : map.entrySet()) {
			JSONObject jsonObject1 = new JSONObject();
			for (Map.Entry<String, Double> entry1 : entry.getValue().entrySet()) {
				jsonObject1.put( entry1.getKey(), entry1.getValue() );
			}
			jsonObject.put( entry.getKey(), jsonObject1 );
		}
		return jsonObject.toString();
	}

}
