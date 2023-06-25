package com.maxsavteam.newmcalc2.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.maxsavteam.currencyconverterservice.protobuf.CurrenciesDataOuterClass;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.entity.CurrencyConverterData;
import com.maxsavteam.newmcalc2.entity.Rates;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
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
            .connectTimeout(17, TimeUnit.SECONDS)
            .readTimeout(17, TimeUnit.SECONDS)
            .build();

    private CurrencyConverterData data;
    private CurrencyConverterData.Currencies currencies;
    private int dataLoadingFailureCount = 0;

    private final DecimalFormat decimalFormat = new DecimalFormat("0.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.currency_converter);

        decimalFormat.setParseBigDecimal(true);
        decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(getResources().getConfiguration().getLocales().get(0)));

        setNumpadViewDecimalSeparatorEnabled(true);
        setNumpadViewArrowButtonsEnabled(true);
    }

    @Override
    protected String convert(String sourceItemId, String targetItemId, String amountString) {
        BigDecimal amount;
        try {
            amount = (BigDecimal) decimalFormat.parse(amountString);
            if (amount == null)
                return "";
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
            return "";
        }

        double exchangeRate = data.getRates().getExchangeRate(sourceItemId, targetItemId);
        BigDecimal result = amount.multiply(new BigDecimal(exchangeRate));

        return decimalFormat.format(result);
    }

    @Override
    protected void startDataLoading(boolean isUserInitiated) {
        super.startDataLoading(isUserInitiated);

        setStatusText("");

        new Thread(() -> loadData(isUserInitiated)).start();
    }

    private void displayData() {
        List<String> displayItems = new ArrayList<>();
        for (var entry : currencies.getCurrencies().entrySet()) {
            displayItems.add(entry.getKey().toUpperCase() + " " + entry.getValue());
        }
        displayItems.sort(String::compareTo);

        List<String> ids = new ArrayList<>();
        for (String s : displayItems) {
            ids.add(s.substring(0, s.indexOf(" ")).toLowerCase());
        }

        displayData(displayItems, ids);

        Locale locale = getResources().getConfiguration().getLocales().get(0);
        String formattedDate = new SimpleDateFormat("dd MMMM yyyy, HH:mm", locale).format(new Date(data.getTimestamp()));
        setStatusText(getString(R.string.currency_converter_status_text, formattedDate));
    }

    @Override
    protected String getDefaultItemId(int index) {
        if (index == 0) {
            return "usd";
        }
        if (index == 1) {
            Locale locale = getResources().getConfiguration().getLocales().get(0);
            try {
                Currency currency = Currency.getInstance(locale);
                return currency.getCurrencyCode().toLowerCase();
            } catch (IllegalArgumentException e) { // unsupported locale
                return "eur";
            }
        }
        throw new IllegalArgumentException("index must be 0 or 1");
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

    private void loadData(boolean forceReload) {
        long timestamp = sharedPreferences.getLong("local_timestamp", 0);
        String savedLanguage = sharedPreferences.getString("lang", null);
        String currentLanguage = getString(R.string.lang_code);
        try {
            if (forceReload || timestamp == 0 || System.currentTimeMillis() - timestamp > TimeUnit.DAYS.toMillis(1) || !currentLanguage.equals(savedLanguage)) {
                long startTime = System.currentTimeMillis();
                data = loadNewData();
                Log.d(TAG, "loadData: loaded in " + (System.currentTimeMillis() - startTime) + "ms");
                saveData(data);
            } else {
                data = loadDataFromCache();
                if (data == null) {
                    sharedPreferences.edit().remove("local_timestamp").apply();
                    loadData(false);
                    return;
                }
            }
            dataLoadingFailureCount = 0;
            endDataLoading();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            setStatusText(e.toString() + ". " + getString(R.string.please_contact_us));
            onDataLoadingFailure(forceReload);
        }
    }

    private void onDataLoadingFailure(boolean forceReload) {
        if (dataLoadingFailureCount < 5) {
            dataLoadingFailureCount++;
            loadData(forceReload);
            return;
        }
        Supplier<Toast> toastSupplier = () -> Toast.makeText(this, "Data loading failed 5 times\nPlease, contact us and try again later", Toast.LENGTH_SHORT);
        if (forceReload) {
            runOnUiThread(() -> {
                toastSupplier.get().show();
                // if we are forcing reload, then it is user initiated,
                // that's why we already have loaded data, so we can show it again
                displayData();
            });
        } else {
            try {
                data = loadDataFromCache();
                if (data != null)
                    endDataLoading();
                else
                    runOnUiThread(() -> {
                        toastSupplier.get().show();
                    });
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    toastSupplier.get().show();
                });
            }
        }
    }

    private void endDataLoading() {
        currencies = data.getCurrencies();
        runOnUiThread(this::displayData);
    }

    private CurrencyConverterData loadDataFromCache() throws JSONException {
        long timestamp = sharedPreferences.getLong("timestamp", 0);
        if (timestamp == 0)
            return null;

        SharedPreferences sharedPreferencesCurrencies = getSharedPreferences("currency_converter_currencies", MODE_PRIVATE);
        String currenciesJSONString = sharedPreferencesCurrencies.getString("currencies", null);
        if (currenciesJSONString == null)
            return null;
        CurrencyConverterData.Currencies currencies = getCurrenciesFromJSON(new JSONObject(currenciesJSONString));

        SharedPreferences sharedPreferencesRates = getSharedPreferences("currency_converter_rates", MODE_PRIVATE);
        String ratesString = sharedPreferencesRates.getString("rates", "{}");
        if (ratesString.equals("{}"))
            return null;
        JSONObject ratesJson = new JSONObject(ratesString);
        Rates rates = getRatesFromJson(ratesJson);

        return new CurrencyConverterData(timestamp, currencies, rates);
    }

    private CurrencyConverterData loadNewData() throws JSONException, IOException {
        CurrenciesDataOuterClass.CurrenciesData currenciesData = loadCurrenciesData();
        return parseCurrenciesData(currenciesData);
    }

    private CurrencyConverterData parseCurrenciesData(CurrenciesDataOuterClass.CurrenciesData currenciesData) {
        CurrencyConverterData.Currencies converterCurrencies = new CurrencyConverterData.Currencies(currenciesData.getLanguage(), currenciesData.getCurrenciesMapMap());
        Map<String, Map<String, Double>> ratesMap = new HashMap<>();
        List<CurrenciesDataOuterClass.CurrenciesData.Rate> ratesList = currenciesData.getRatesList();
        for (CurrenciesDataOuterClass.CurrenciesData.Rate rate : ratesList) {
            ratesMap.put(rate.getCurrencyName(), rate.getRatesMapMap());
        }
        Rates rates = new Rates(ratesMap);

        return new CurrencyConverterData(currenciesData.getTimestamp(), converterCurrencies, rates);
    }

    private CurrenciesDataOuterClass.CurrenciesData loadCurrenciesData() throws IOException {
        try (Response response = client
                .newCall(
                        new Request.Builder()
                                .url(CURRENCY_CONVERTER_URL + "/data?lang=" + getString(R.string.lang_code))
                                .build()
                )
                .execute()) {
            if (response.code() != 200)
                throw new IOException("Error loading data: " + response.code());
            if (response.body() == null)
                throw new IOException("Response body is null");
            InputStream is = response.body().byteStream();
            return CurrenciesDataOuterClass.CurrenciesData.parseFrom(is);
        }
    }

    private void saveData(CurrencyConverterData converterData) throws JSONException {
        sharedPreferences.edit().putLong("timestamp", converterData.getTimestamp())
                .putLong("local_timestamp", System.currentTimeMillis())
                .putString("lang", getString(R.string.lang_code))
                .apply();

        SharedPreferences sharedPreferencesRates = getSharedPreferences("currency_converter_rates", MODE_PRIVATE);
        sharedPreferencesRates.edit()
                .putString("rates", converterData.getRates().toJsonString())
                .apply();

        SharedPreferences sharedPreferencesCurrencies = getSharedPreferences("currency_converter_currencies", MODE_PRIVATE);
        sharedPreferencesCurrencies.edit()
                .putString("currencies", converterData.getCurrencies().toJson().toString())
                .apply();
    }

    private Rates getRatesFromJson(JSONObject ratesJson) throws JSONException {
        Map<String, Map<String, Double>> ratesMap = new HashMap<>();
        for (Iterator<String> it = ratesJson.keys(); it.hasNext(); ) {
            String currency = it.next();
            JSONObject currencyRates = ratesJson.getJSONObject(currency);
            Map<String, Double> currencyRatesMap = new HashMap<>();
            for (Iterator<String> it2 = currencyRates.keys(); it2.hasNext(); ) {
                String currency2 = it2.next();
                currencyRatesMap.put(currency2, currencyRates.getDouble(currency2));
            }
            ratesMap.put(currency, currencyRatesMap);
        }

        return new Rates(ratesMap);
    }

    private CurrencyConverterData.Currencies getCurrenciesFromJSON(JSONObject item) throws JSONException {
        String langCode = item.getString("lang");
        JSONObject currencies = item.getJSONObject("currencies");
        Map<String, String> map = new HashMap<>();
        for (Iterator<String> it = currencies.keys(); it.hasNext(); ) {
            String currency = it.next();
            map.put(currency, currencies.getString(currency));
        }
        return new CurrencyConverterData.Currencies(langCode, map);
    }

}