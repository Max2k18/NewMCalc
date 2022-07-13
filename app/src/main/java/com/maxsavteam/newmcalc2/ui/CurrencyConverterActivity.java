package com.maxsavteam.newmcalc2.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.entity.CurrencyConverterData;
import com.maxsavteam.newmcalc2.entity.Rates;
import com.maxsavteam.newmcalc2.ui.base.ThemeActivity;
import com.maxsavteam.newmcalc2.widget.ButtonWithDropdown;
import com.maxsavteam.newmcalc2.widget.FullNumpadView;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CurrencyConverterActivity extends ThemeActivity {

	private static final String CURRENCY_CONVERTER_URL = "https://mcalc.maxsavteam.com/api/currency";
	private static final String TAG = "CurrencyConverterActivity";

	private final OkHttpClient client = new OkHttpClient();

	private CurrencyConverterData data;
	private CurrencyConverterData.Currencies currencies;
	private int dataLoadingFailureCount = 0;

	private List<String> sortedCurrenciesList;

	private final DecimalFormat decimalFormat = new DecimalFormat("0.##");

	private EditText editText1;
	private EditText editText2;

	private SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_currency_converter );

		setActionBar( R.id.toolbar );
		displayHomeAsUp();

		sharedPreferences = getSharedPreferences( "currency_converter", MODE_PRIVATE );

		decimalFormat.setParseBigDecimal( true );
		decimalFormat.setDecimalFormatSymbols( new DecimalFormatSymbols( getResources().getConfiguration().getLocales().get( 0 ) ) );

		editText1 = findViewById( R.id.currency_converter_editText1 );
		editText2 = findViewById( R.id.currency_converter_editText2 );

		SwipeRefreshLayout refreshLayout = findViewById( R.id.currency_converter_refreshLayout );
		refreshLayout.setOnRefreshListener( ()->startDataLoading(true) );

		FullNumpadView numpadView = findViewById( R.id.currency_converter_numpad_view );
		numpadView.linkWith( editText1 );
		numpadView.linkWith( editText2 );

		startDataLoading(false);

		setupButtons();
		setupEditTexts();

		editText1.requestFocus();
	}

	private void setupButtons(){
		ButtonWithDropdown button1 = findViewById( R.id.currency_converter_button1 );
		ButtonWithDropdown button2 = findViewById( R.id.currency_converter_button2 );
		button1.setOnItemSelectedListener( index -> {
			sharedPreferences.edit().putString( "lastSelectedCurrency1", sortedCurrenciesList.get( index ) ).apply();
			if(editText1.hasFocus())
				convert( 0 );
			else
				convert( 1 );
		} );
		button2.setOnItemSelectedListener( index -> {
			sharedPreferences.edit().putString( "lastSelectedCurrency2", sortedCurrenciesList.get( index ) ).apply();
			if(editText1.hasFocus())
				convert( 0 );
			else
				convert( 1 );
		} );
	}

	private void setupEditTexts(){
		editText1.addTextChangedListener( new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(editText1.hasFocus()) convert( 0 );
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		} );
		editText2.addTextChangedListener( new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(editText2.hasFocus()) convert( 1 );
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		} );
	}

	private void convert(int sourceIndex){
		ButtonWithDropdown sourceButton;
		ButtonWithDropdown targetButton;
		EditText sourceEditText;
		EditText targetEditText;
		if(sourceIndex == 0){
			sourceButton = findViewById( R.id.currency_converter_button1 );
			targetButton = findViewById( R.id.currency_converter_button2 );
			sourceEditText = editText1;
			targetEditText = editText2;
		}else{
			sourceButton = findViewById( R.id.currency_converter_button2 );
			targetButton = findViewById( R.id.currency_converter_button1 );
			sourceEditText = editText2;
			targetEditText = editText1;
		}
		if(sourceEditText.getText().toString().isEmpty()) {
			targetEditText.getText().clear();
			return;
		}
		String sourceCurrency = sortedCurrenciesList.get( sourceButton.getSelectedItem() );
		String targetCurrency = sortedCurrenciesList.get( targetButton.getSelectedItem() );
		BigDecimal amount;
		try {
			amount = (BigDecimal) decimalFormat.parse( sourceEditText.getText().toString() );
			if(amount == null)
				return;
		} catch (ParseException e) {
			e.printStackTrace();
			Toast.makeText( this, "" + e, Toast.LENGTH_SHORT ).show();
			return;
		}

		double exchangeRate = data.getRates().getExchangeRate( sourceCurrency, targetCurrency );
		BigDecimal result = amount.multiply( new BigDecimal( exchangeRate ) );

		targetEditText.clearFocus();
		targetEditText.setText( decimalFormat.format( result ) );
	}

	private void setStatus(String status){
		TextView textView = findViewById( R.id.currency_converter_status );
		textView.setText( status );
	}

	private void startDataLoading(boolean forceReload){
		(( SwipeRefreshLayout ) findViewById( R.id.currency_converter_refreshLayout )).setRefreshing( true );

		findViewById( R.id.flexboxLayout ).setVisibility( View.INVISIBLE );

		setStatus( "" );

		new Thread(()->loadData( forceReload )).start();
	}

	private void displayData(){
		List<String> buttonsElements = new ArrayList<>();
		for(var entry : currencies.getCurrencies().entrySet()){
			buttonsElements.add( entry.getKey().toUpperCase() + " " + entry.getValue() );
		}
		buttonsElements.sort( String::compareTo );

		sortedCurrenciesList = new ArrayList<>();
		for(String s : buttonsElements) {
			sortedCurrenciesList.add( s.substring( 0, s.indexOf( " " ) ).toLowerCase() );
		}

		Locale locale = getResources().getConfiguration().getLocales().get(0);
		Currency currency = Currency.getInstance( locale );
		String localeCurrency = currency.getCurrencyCode().toUpperCase();

		String lastSelectedCurrency1 = sharedPreferences
				.getString( "lastSelectedCurrency1", "USD" )
				.toUpperCase();
		String lastSelectedCurrency2 = sharedPreferences
				.getString( "lastSelectedCurrency2", localeCurrency )
				.toUpperCase();

		int i1 = 0;
		for(int i = 0; i < buttonsElements.size(); i++){
			String element = buttonsElements.get( i );
			if(element.startsWith( lastSelectedCurrency1 + " " )){
				i1 = i;
				break;
			}
		}

		ButtonWithDropdown button1 = findViewById( R.id.currency_converter_button1 );
		button1.setElements( buttonsElements.toArray() );
		button1.setSelection( i1 );

		int i2 = 0;
		for(int i = 0; i < buttonsElements.size(); i++){
			String element = buttonsElements.get( i );
			if(element.startsWith( lastSelectedCurrency2 + " " )){
				i2 = i;
				break;
			}
		}

		ButtonWithDropdown button2 = findViewById( R.id.currency_converter_button2);
		button2.setElements( buttonsElements.toArray() );
		button2.setSelection( i2 );

		String formattedDate = new SimpleDateFormat( "dd MMMM yyyy, HH:mm", locale ).format( new Date( data.getTimestamp() ) );
		setStatus( getString( R.string.currency_converter_status_text, formattedDate ) );

		(( SwipeRefreshLayout ) findViewById( R.id.currency_converter_refreshLayout )).setRefreshing( false );
		findViewById( R.id.flexboxLayout ).setVisibility( View.VISIBLE );
	}

	private void loadData(boolean forceReload){
		long timestamp = sharedPreferences.getLong( "local_timestamp", 0 );
		try {
			if ( forceReload || timestamp == 0 || System.currentTimeMillis() - timestamp > TimeUnit.DAYS.toMillis( 1 ) ) {
				data = loadNewData();
				saveData( data );
			} else {
				data = loadDataFromCache();
			}
			dataLoadingFailureCount = 0;
			currencies = data.getCurrencies( getString( R.string.lang_code ) );
			runOnUiThread( this::displayData );
		} catch (IOException | JSONException e){
			e.printStackTrace();
			setStatus( e.getMessage() );
			if ( dataLoadingFailureCount < 5 ) {
				dataLoadingFailureCount++;
				loadData(forceReload);
			}else {
				runOnUiThread( ()->Toast.makeText( this, "Data loading failed 5 times\nPlease, try again later", Toast.LENGTH_SHORT ).show());
			}
		}
	}

	private CurrencyConverterData loadDataFromCache() throws JSONException {
		long timestamp = sharedPreferences.getLong( "timestamp", 0 );

		SharedPreferences sharedPreferencesCurrencies = getSharedPreferences( "currency_converter_currencies", MODE_PRIVATE );
		JSONArray currenciesArray = new JSONArray( sharedPreferencesCurrencies.getString( "currencies", "[]" ) );
		List<CurrencyConverterData.Currencies> currencies = getCurrenciesListFromJson( currenciesArray );

		SharedPreferences sharedPreferencesRates = getSharedPreferences( "currency_converter_rates", MODE_PRIVATE );
		//noinspection ConstantConditions
		JSONObject ratesJson = new JSONObject( sharedPreferencesRates.getString( "rates", "{}" ) );
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