package com.maxsavteam.newmcalc2.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.entity.CurrencyConverterData;
import com.maxsavteam.newmcalc2.entity.Rates;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CurrencyConverterActivity extends BaseConverterActivity {

	private static final String CURRENCY_CONVERTER_URL = "https://mcalc.maxsavteam.com/api/currency";
	private static final String TAG = "CurrencyConverterActivity";

	private final OkHttpClient client = new OkHttpClient.Builder()
			.connectTimeout( 17, TimeUnit.SECONDS )
			.readTimeout( 17, TimeUnit.SECONDS )
			.build();

	private CurrencyConverterData data;
	private CurrencyConverterData.Currencies currencies;
	private int dataLoadingFailureCount = 0;

	private final DecimalFormat decimalFormat = new DecimalFormat("0.##");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );

		setTitle( R.string.currency_converter );

		decimalFormat.setParseBigDecimal( true );
		decimalFormat.setDecimalFormatSymbols( new DecimalFormatSymbols( getResources().getConfiguration().getLocales().get( 0 ) ) );

		setNumpadViewDecimalSeparatorEnabled( true );
	}

	@Override
	protected String convert(String sourceItemId, String targetItemId, String amountString){
		BigDecimal amount;
		try {
			amount = (BigDecimal) decimalFormat.parse( amountString );
			if(amount == null)
				return "";
		} catch (ParseException e) {
			e.printStackTrace();
			Toast.makeText( this, "" + e, Toast.LENGTH_SHORT ).show();
			return "";
		}

		double exchangeRate = data.getRates().getExchangeRate( sourceItemId, targetItemId );
		BigDecimal result = amount.multiply( new BigDecimal( exchangeRate ) );

		return decimalFormat.format( result );
	}

	@Override
	protected void startDataLoading(boolean isUserInitiated){
		super.startDataLoading( isUserInitiated );

		setStatusText( "" );

		new Thread(()->loadData( isUserInitiated )).start();
	}

	private void displayData(){
		List<String> displayItems = new ArrayList<>();
		for(var entry : currencies.getCurrencies().entrySet()){
			displayItems.add( entry.getKey().toUpperCase() + " " + entry.getValue() );
		}
		displayItems.sort( String::compareTo );

		List<String> ids = new ArrayList<>();
		for(String s : displayItems) {
			ids.add( s.substring( 0, s.indexOf( " " ) ).toLowerCase() );
		}

		displayData(displayItems, ids);

		Locale locale = getResources().getConfiguration().getLocales().get( 0 );
		String formattedDate = new SimpleDateFormat( "dd MMMM yyyy, HH:mm", locale ).format( new Date( data.getTimestamp() ) );
		setStatusText( getString( R.string.currency_converter_status_text, formattedDate ) );
	}

	@Override
	protected String getDefaultItemId(int index) {
		if(index == 0){
			return "usd";
		}
		if(index == 1) {
			Locale locale = getResources().getConfiguration().getLocales().get( 0 );
			try {
				Currency currency = Currency.getInstance( locale );
				return currency.getCurrencyCode().toLowerCase();
			}catch (IllegalArgumentException e){ // unsupported locale
				return "eur";
			}
		}
		throw new IllegalArgumentException( "index must be 0 or 1" );
	}

	@Override
	protected String getSharedPrefsName() {
		return "currency_converter";
	}

	@Override
	protected int getFieldsCount() {
		return 2;
	}

	@Override
	protected String getLastAmountDefaultValue() {
		return "1";
	}

	private void loadData(boolean forceReload){
		long timestamp = sharedPreferences.getLong( "local_timestamp", 0 );
		try {
			if ( forceReload || timestamp == 0 || System.currentTimeMillis() - timestamp > TimeUnit.DAYS.toMillis( 1 ) ) {
				data = loadNewData();
				saveData( data );
			} else {
				data = loadDataFromCache();
				if(data == null) {
					sharedPreferences.edit().remove( "local_timestamp" ).apply();
					loadData( false );
					return;
				}
			}
			dataLoadingFailureCount = 0;
			endDataLoading();
		} catch (IOException | JSONException e){
			e.printStackTrace();
			setStatusText( e.toString() + ". " + getString( R.string.please_contact_us ) );
			onDataLoadingFailure( forceReload );
		}
	}

	private void onDataLoadingFailure(boolean forceReload){
		if ( dataLoadingFailureCount < 5 ) {
			dataLoadingFailureCount++;
			loadData( forceReload );
			return;
		}
		Supplier<Toast> toastSupplier = () -> Toast.makeText( this, "Data loading failed 5 times\nPlease, contact us and try again later", Toast.LENGTH_SHORT );
		if(forceReload) {
			runOnUiThread( ()->{
				toastSupplier.get().show();
				// if we are forcing reload, then it is user initiated,
				// that's why we already have loaded data, so we can show it again
				displayData();
			} );
		} else {
			try {
				data = loadDataFromCache();
				if(data != null)
					endDataLoading();
				else
					runOnUiThread( ()->{
						toastSupplier.get().show();
					} );
			} catch (JSONException e) {
				e.printStackTrace();
				runOnUiThread( ()->{
					toastSupplier.get().show();
				} );
			}
		}
	}

	private void endDataLoading(){
		currencies = data.getCurrencies( getString( R.string.lang_code ) );
		runOnUiThread( this::displayData );
	}

	private CurrencyConverterData loadDataFromCache() throws JSONException {
		long timestamp = sharedPreferences.getLong( "timestamp", 0 );
		if(timestamp == 0)
			return null;

		SharedPreferences sharedPreferencesCurrencies = getSharedPreferences( "currency_converter_currencies", MODE_PRIVATE );
		JSONArray currenciesArray = new JSONArray( sharedPreferencesCurrencies.getString( "currencies", "[]" ) );
		if(currenciesArray.length() == 0)
			return null;
		List<CurrencyConverterData.Currencies> currencies = getCurrenciesListFromJson( currenciesArray );

		SharedPreferences sharedPreferencesRates = getSharedPreferences( "currency_converter_rates", MODE_PRIVATE );
		String ratesString = sharedPreferencesRates.getString( "rates", "{}" );
		if(ratesString == null || ratesString.equals( "{}" ))
			return null;
		JSONObject ratesJson = new JSONObject( ratesString );
		Rates rates = getRatesFromJson( ratesJson );

		return new CurrencyConverterData( timestamp, currencies, rates );
	}

	private CurrencyConverterData loadNewData() throws JSONException, IOException {
		long timestamp = loadTimestamp();
		List<CurrencyConverterData.Currencies> currencies = loadCurrencies();
		Rates rates = loadRates();

		return new CurrencyConverterData( timestamp, currencies, rates );
	}

	private void saveData(CurrencyConverterData converterData) throws JSONException {
		sharedPreferences.edit().putLong( "timestamp", converterData.getTimestamp() )
				.putLong( "local_timestamp", System.currentTimeMillis() )
				.apply();

		SharedPreferences sharedPreferencesRates = getSharedPreferences( "currency_converter_rates", MODE_PRIVATE );
		sharedPreferencesRates.edit()
				.putString( "rates", converterData.getRates().toJsonString() )
				.apply();

		SharedPreferences sharedPreferencesCurrencies = getSharedPreferences( "currency_converter_currencies", MODE_PRIVATE );
		sharedPreferencesCurrencies.edit()
				.putString( "currencies", currenciesListToJsonString( converterData.getCurrenciesList() ) )
				.apply();
	}

	private String currenciesListToJsonString(List<CurrencyConverterData.Currencies> list) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for(var currencies : list){
			jsonArray.put( currencies.toJson() );
		}
		return jsonArray.toString();
	}

	private Rates loadRates() throws IOException, JSONException {
		Response response = client
				.newCall(
						new Request.Builder()
								.url( CURRENCY_CONVERTER_URL + "/rates" )
								.build()
				)
				.execute();
		if(response.code() != 200)
			throw new IOException( "Error loading rates: " + response.code() );
		String json = response.body().string();
		JSONObject jsonObject = new JSONObject( json );
		JSONObject ratesJson = jsonObject.getJSONObject( "rates" );
		return getRatesFromJson( ratesJson );
	}

	private Rates getRatesFromJson(JSONObject ratesJson) throws JSONException {
		Map<String, Map<String, Double>> ratesMap = new HashMap<>();
		for (Iterator<String> it = ratesJson.keys(); it.hasNext(); ) {
			String currency = it.next();
			JSONObject currencyRates = ratesJson.getJSONObject( currency );
			Map<String, Double> currencyRatesMap = new HashMap<>();
			for (Iterator<String> it2 = currencyRates.keys(); it2.hasNext(); ) {
				String currency2 = it2.next();
				currencyRatesMap.put( currency2, currencyRates.getDouble( currency2 ) );
			}
			ratesMap.put( currency, currencyRatesMap );
		}

		return new Rates( ratesMap );
	}

	private long loadTimestamp() throws IOException {
		Response response = client
				.newCall(
						new Request.Builder()
							.url( CURRENCY_CONVERTER_URL + "/timestamp" )
							.build()
				)
				.execute();
		if(response.code() != 200)
			throw new IOException( "Error loading timestamp: " + response.code() );
		return Long.parseLong( response.body().string() );
	}

	private List<CurrencyConverterData.Currencies> loadCurrencies() throws IOException, JSONException {
		Request request = new Request.Builder()
				.url( CURRENCY_CONVERTER_URL + "/currencies?lang=en,ru" )
				.build();

		Response response = client.newCall( request ).execute();
		if(response.code() != 200){
			throw new IOException("Error loading currencies: " + response.code());
		}
		String json = response.body().string();
		JSONObject jsonObject = new JSONObject(json);
		JSONArray result = jsonObject.getJSONArray( "result" );

		return getCurrenciesListFromJson( result );
	}

	private List<CurrencyConverterData.Currencies> getCurrenciesListFromJson(JSONArray jsonArray) throws JSONException {
		List<CurrencyConverterData.Currencies> list = new ArrayList<>();

		for(int i = 0; i < jsonArray.length(); i++){
			JSONObject item = jsonArray.getJSONObject( i );
			String langCode = item.getString( "lang" );
			JSONObject currencies = item.getJSONObject( "currencies" );
			Map<String, String> map = new HashMap<>();
			for (Iterator<String> it = currencies.keys(); it.hasNext(); ) {
				String currency = it.next();
				map.put( currency, currencies.getString( currency ) );
			}
			list.add( new CurrencyConverterData.Currencies( langCode, map ) );
		}

		return list;
	}

}