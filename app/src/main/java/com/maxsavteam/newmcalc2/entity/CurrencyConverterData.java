package com.maxsavteam.newmcalc2.entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class CurrencyConverterData {

	private final long timestamp;
	private final Currencies currencies;
	private final Rates rates;

	public CurrencyConverterData(long timestamp, Currencies currencies, Rates rates) {
		this.timestamp = timestamp;
		this.currencies = currencies;
		this.rates = rates;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Currencies getCurrencies() {
		return currencies;
	}

	public Rates getRates() {
		return rates;
	}

	public static class Currencies {
		private final String langCode;
		private final Map<String, String> currencies;

		public Currencies(String langCode, Map<String, String> currencies) {
			this.langCode = langCode;
			this.currencies = currencies;
		}

		public String getLangCode() {
			return langCode;
		}

		public Map<String, String> getCurrencies() {
			return currencies;
		}

		public JSONObject toJson() throws JSONException {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put( "lang", langCode );
			JSONObject jsonCurrencies = new JSONObject();
			for(Map.Entry<String, String> entry : currencies.entrySet()){
				jsonCurrencies.put( entry.getKey(), entry.getValue() );
			}
			jsonObject.put( "currencies", jsonCurrencies );
			return jsonObject;
		}

	}

}
