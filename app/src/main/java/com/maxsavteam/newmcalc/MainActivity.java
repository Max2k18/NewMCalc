package com.maxsavteam.newmcalc;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import com.maxsavteam.newmcalc.adapters.MyFragmentPagerAdapter;
import com.maxsavteam.newmcalc.core.CoreMain;
import com.maxsavteam.newmcalc.memory.MemorySaverReader;
import com.maxsavteam.newmcalc.utils.Utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;


public class MainActivity extends AppCompatActivity implements CoreMain.CoreLinkBridge{
    public static Boolean was_error = false;

    private boolean isOtherActivityOpened = false;
    private SharedPreferences sp;
    private TextView t;
    private char last;
    private int brackets = 0;
    private String original = "";
    private AlertDialog about_app, al;
    private String uptype = "simple";
    private FirebaseAnalytics fr;
    private BigDecimal[] barr;
    private BigDecimal result_calc_for_mem;
    private MyFragmentPagerAdapter myFragmentPagerAdapter;
    public TextView text_example;
    String FI, PI, E;
    private MemorySaverReader memorySaverReader;
    private String MULTIPLY_SIGN;
    private String DIVIDE_SIGN;
    private Utils utils;
    private CoreMain coreMain;

    View.OnLongClickListener fordel = (View v) -> {
        TextView t = findViewById(R.id.textStr);
        t.setText("");
        TextView ans = findViewById(R.id.textAns2);
        hide_ans();
        ans.setText("");

        sp.edit().remove("saveResultText").apply();
        set_text_toDef();
        return true;
    };

    @Override
    protected void onPause(){
        super.onPause();
        isOtherActivityOpened = true;
    }

    View.OnLongClickListener returnback = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            TextView back = findViewById(R.id.textAns2);
            if(back.getVisibility() == View.INVISIBLE || back.getVisibility() == View.GONE){
                return false;
            }
            String txt = back.getText().toString();
            hide_ans();
            back.setText("");
            back = findViewById(R.id.textStr);
            back.setText(txt);
            scroll_to_end();
            equallu("not");
            return true;
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        isOtherActivityOpened = false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.exit)
                .setMessage(R.string.areyousureexit)
                .setCancelable(false)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finishAndRemoveTask();
                        overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha_hide);
                    }
                });
        AlertDialog al_1 = builder.create();
        if(DarkMode)
            al_1.getWindow().setBackgroundDrawableResource(R.drawable.grey);
        al_1.show();
        //super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.about) {
            /*if(DarkMode)
                dl.getWindow().setBackgroundDrawableResource(R.drawable.grey);*/
            //about_app.show();
            Intent in = new Intent(this, catch_service.class);
            in.putExtra("action", "about_app");
            startActivity(in);
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onClickAdd(View v){
        if(v.getId() == R.id.imgBtnSettings){
            goto_add_scr("settings");
        }else if(v.getId() == R.id.btnImgNumGen){
            goto_add_scr("numgen");
        }else if(v.getId() == R.id.btnImgHistory){
            goto_add_scr("history");
        }else if(v.getId() == R.id.btnImgPassgen){
            goto_add_scr("pass");
        }else if(v.getId() == R.id.imgBtnBin){
            goto_add_scr("bin");
        }
    }

    protected void goto_add_scr(String where){
        Intent resultIntent;
        switch (where) {
            case "settings":
                resultIntent = new Intent(getApplicationContext(), Updater.class);
                resultIntent.putExtra("action", "simple");
                resultIntent.putExtra("start_type", "app");
                if (uptype.equals("simple"))
                    resultIntent.putExtra("update_path", "/NewMCalc.apk");
                else
                    resultIntent.putExtra("update_path", "/forTesters/NewMCalc.apk");
                startActivity(resultIntent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                break;
            case "numgen":
                resultIntent = new Intent(getApplicationContext(), numgen.class);
                resultIntent.putExtra("type", "number");
                resultIntent.putExtra("start_type", "app");
                startActivity(resultIntent);
	            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                break;
            case "history":
                sp.edit().putString("action", "history").apply();
                resultIntent = new Intent(getApplicationContext(), history.class);
                resultIntent.putExtra("start_type", "app");
                startActivity(resultIntent);
	            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                break;
            case "pass":
                resultIntent = new Intent(getApplicationContext(), numgen.class);
                resultIntent.putExtra("type", "pass");
                resultIntent.putExtra("start_type", "app");
                startActivity(resultIntent);
	            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                break;
            case "bin":
                resultIntent = new Intent(this, number_system.class);
                resultIntent.putExtra("start_type", "app");
                startActivity(resultIntent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                break;
        }
    }


    boolean DarkMode = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    DarkMode = sp.getBoolean("dark_mode", false);
	    sp.edit().remove("notification_showed").apply();
	    utils = new Utils();
	    coreMain = new CoreMain(this);
	    coreMain.setInterface(this);
	    if(DarkMode){
	    	setTheme(android.R.style.Theme_Material_NoActionBar);
            //setTheme(R.style.AppTheme);
	    }else{
	        setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apply_theme();
        fr = FirebaseAnalytics.getInstance(getApplicationContext());
        Trace myTrace = FirebasePerformance.getInstance().newTrace("AppStart");
        myTrace.start();

        PI = getResources().getString(R.string.pi);
        FI = getResources().getString(R.string.fi);
        E = "e";
        try{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
		Intent start_in = getIntent();
		if(start_in.getBooleanExtra("shortcut_action", false)){
            goto_add_scr(start_in.getStringExtra("to_"));
		}
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            Intent t = new Intent(Intent.ACTION_VIEW, null, this, MainActivity.class);
            t.putExtra("shortcut_action", true);
            t.putExtra("to_", "numgen");
            ShortcutInfo shortcut1 = new ShortcutInfo.Builder(getApplicationContext(), "id1")
                    .setLongLabel(getResources().getString(R.string.randomgen))
                    .setShortLabel(getResources().getString(R.string.randomgen))
                    .setIcon(Icon.createWithResource(this, R.drawable.dice))
                    .setIntent(t)
                    .build();
            t.putExtra("to_", "pass");
            ShortcutInfo shortcut2 = new ShortcutInfo.Builder(getApplicationContext(), "id2")
                    .setLongLabel(getResources().getString(R.string.passgen))
                    .setShortLabel(getResources().getString(R.string.passgen))
                    .setIcon(Icon.createWithResource(this, R.drawable.passgen))
                    .setIntent(t)
                    .build();

            t.putExtra("to_", "history");
            ShortcutInfo shortcut3 = new ShortcutInfo.Builder(getApplicationContext(), "id3")
                    .setLongLabel(getResources().getString(R.string.hitory))
                    .setShortLabel(getResources().getString(R.string.hitory))
                    .setIcon(Icon.createWithResource(this, R.drawable.history))
                    .setIntent(t)
                    .build();
	        t.putExtra("to_", "bin");
            ShortcutInfo shortCutNumSys = new ShortcutInfo.Builder(getApplicationContext(), "idNumSys")
		            .setLongLabel(getResources().getString(R.string.number_system_convrter))
		            .setShortLabel(getResources().getString(R.string.number_system_convrter))
		            .setIcon(Icon.createWithResource(this, R.drawable.binary))
		            .setIntent(t)
		            .build();


            shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut3, shortCutNumSys, shortcut2, shortcut1));
        }
        set_viewpager();

        myTrace.stop();
        fr.logEvent("OnCreate", Bundle.EMPTY);
    }

    public void apply_theme(){
        ActionBar bar = null;
        bar = getSupportActionBar();
        try {
            if (DarkMode) {
                getWindow().setBackgroundDrawableResource(R.drawable.black);
                TextView t;
                int[] ids = new int[]{
                        R.id.textAns2,
                        R.id.textStr
                };
                for (int id : ids) {
                    t = findViewById(id);
                    t.setTextColor(getResources().getColor(R.color.white));
                }
                bar.setBackgroundDrawable(getDrawable(R.drawable.black));
            } else {
                TextView t;
                getWindow().setBackgroundDrawableResource(R.drawable.white);
                int[] ids = new int[]{
                        R.id.textAns2,
                        R.id.textStr
                };
                for (int id : ids) {
                    t = findViewById(id);
                    t.setTextColor(getResources().getColor(R.color.black));
                }
                bar.setBackgroundDrawable(getDrawable(R.drawable.white));
                //barTitle.setTextColor(Color.BLACK);
            }
            bar.setElevation(0);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void aboutAG(View v){
        Intent in = new Intent(this, catch_service.class);
        in.putExtra("action", "aboutAG");
        startActivity(in);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    View.OnLongClickListener additional_longclick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            t = findViewById(R.id.textStr);
            String txt = t.getText().toString();
                //Toast.makeText(MainActivity.this, ((Button) view).getText().toString(), Toast.LENGTH_SHORT).show();
            add_text(((Button) view).getText().toString().substring(1));
            return true;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 10 && grantResults.length == 2){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED){
                sp.edit().putBoolean("storage_denied", true).apply();
            }
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                sp.edit().remove("storage_denied").remove("never_request_permissions").apply();
        }
    }
    String app_type = BuildConfig.APPTYPE;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        memorySaverReader = new MemorySaverReader(this);
	    boolean read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
	    boolean write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if(!sp.getBoolean("never_request_permissions", false)
		        && (!read || !write)){
                View v = getLayoutInflater().inflate(R.layout.never_show_again, null);
                AlertDialog request = new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm)
                        .setView(v)
                        .setMessage(R.string.activity_requet_permissions)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(((CheckBox) v.findViewById(R.id.never_show_again)).isChecked()) {
                                    sp.edit().putBoolean("never_request_permissions", true).apply();
                                }
                                ActivityCompat
                                        .requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                10);
                            }
                        })
                        .create();
                request.show();
        }
        if(read && write){
            sp.edit().remove("storage_denied").remove("never_request_permissions").apply();
        }
        barr = memorySaverReader.read();
        MULTIPLY_SIGN = getResources().getString(R.string.multiply);
        DIVIDE_SIGN = getResources().getString(R.string.div);
        //Toast.makeText(this, String.format("%d %d", height, findViewById(R.id.imageButton2).getHeight()), Toast.LENGTH_SHORT).show();
        Trace trace = FirebasePerformance.getInstance().newTrace("PostCreate");
        trace.start();

        register_broadcasters();

        if(sp.getBoolean("saveResult", false)){
            if(!sp.getString("saveResultText", "none").equals("none")){
                String text = sp.getString("saveResultText", "none");
                int i = 0;
                StringBuilder ex = new StringBuilder();
	            StringBuilder ans = new StringBuilder();
	            while(i < Objects.requireNonNull(text).length() && text.charAt(i) != ';'){
                    ex.append(text.charAt(i));
                    i++;
                }
                TextView ver = findViewById(R.id.textAns2);
	            if(!DarkMode)
	                ver.setTextColor(getResources().getColor(R.color.black));
                ver.setText(ex.toString());
                show_ans();
                i++;
                while(i < text.length() && text.charAt(i) != ';'){
                    ans.append(text.charAt(i));
                    i++;
                }
                ver = findViewById(R.id.textStr);
                if(!DarkMode)
                    ver.setTextColor(getResources().getColor(R.color.black));
                ver.setSelectAllOnFocus(false);
                ver.setText(ans.toString());
                ver.setSelected(false);
                //equallu("not");
                format(R.id.textStr);
                ver = null;
            }
        }
        boolean offer_to_rate = sp.getBoolean("offer_was_showed", false);
        int offers_count = sp.getInt("offers_count", 0);
        if(!offer_to_rate){
            if(offers_count == 10){
                offers_count = 0;
                AlertDialog rate;
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(R.string.please_rate_out_app)
                        .setMessage(R.string.rate_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.rate, (dialog, i) -> {
                            Toast.makeText(this, getResources().getString(R.string.thank_you), Toast.LENGTH_SHORT).show();
                            sp.edit().putBoolean("offer_was_showed", true).apply();
                            Intent go_to = new Intent(Intent.ACTION_VIEW);
                            go_to.setData(Uri.parse(getResources().getString(R.string.link_app_in_google_play)));
                            startActivity(go_to);
                            dialog.cancel();
                        })
                        .setNegativeButton(R.string.no_thanks, ((dialog, which) -> {
                            Toast.makeText(this, ":(", Toast.LENGTH_SHORT).show();
                            sp.edit().putBoolean("offer_was_showed", true).apply();
                            dialog.cancel();
                        }))
                        .setNeutralButton(R.string.later, ((dialog, which) -> {
                            Toast.makeText(this, R.string.we_will_wait, Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }));

                rate = builder.create();
                if(DarkMode)
                    Objects.requireNonNull(rate.getWindow()).setBackgroundDrawableResource(R.drawable.grey);
                rate.show();
            }else{
                offers_count++;
            }
            sp.edit().putInt("offers_count", offers_count).apply();
        }

        trace.stop();
        //should be always in the end
        fr.logEvent("OnPostCreate", Bundle.EMPTY);
    }
    int count_of_memory_plu_min_calls = 0;
    public void memory_plus_min(View v){
	    text_example = findViewById(R.id.textStr);
	    String text = text_example.getText().toString();
	    if(text.equals(""))
		    return;

	    BigDecimal temp = null;
	    try {
            temp = new BigDecimal(text_example.getText().toString());
        }catch(NumberFormatException e){
	        equallu("all");
	        if(count_of_memory_plu_min_calls < 1){
	            count_of_memory_plu_min_calls++;
	            memory_plus_min(v);
            }
	        return;
        }
	    if(v.getId() == R.id.btnMemPlus) {
		    temp = temp.add(barr[0]);
	    }else if(v.getId() == R.id.btnMemMinus){
		    temp = barr[0].subtract(temp);
	    }
        if(count_of_memory_plu_min_calls > 0)
            returnback.onLongClick(findViewById(R.id.btnCalc));
	    barr[0] = temp;
	    memorySaverReader.save(barr);
    }


    public boolean memory_calculated = false;

    public void memory_ms(View view){
        TextView t = findViewById(R.id.textStr);
        String txt = t.getText().toString();
        if(txt.equals(""))
            return;
        BigDecimal temp;
        try{
            temp = new BigDecimal(txt);
        }catch(NumberFormatException e){
            equallu("all");
            if (count_of_ms_recalls < 1) {
                count_of_ms_recalls++;
                memory_ms(view);
            }
            return;
        }
        if(count_of_ms_recalls > 0)
            returnback.onLongClick(findViewById(R.id.btnCalc));
        count_of_ms_recalls = 0;
        barr[0] = temp;
    	memorySaverReader.save(barr);
    	
    }
    int count_of_ms_recalls = 0;

    public void memory_mr(View view){
    	String value = barr[0].toString();
    	add_value_from_mem(value);
    }

    private void showMemAlert(final String type){
        Intent in = new Intent(this, memory_actions_activity.class);
        in.putExtra("type", type);
        if(type.equals("st")){
            TextView t = findViewById(R.id.textStr);
            if(t.getText().toString().equals(""))
                return;
            BigDecimal temp;
            try{
                temp = new BigDecimal(t.getText().toString());
            }catch(NumberFormatException e){
                return;
            }
            in.putExtra("value", temp.toString());
        }
        startActivity(in);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    View.OnLongClickListener memory_actions = new View.OnLongClickListener() {
	    @Override
	    public boolean onLongClick(View v) {
	        try {
                if(v.getId() == R.id.btnMR)
                    showMemAlert("rc");
                else
                    showMemAlert("st");
            }catch (Exception e){
	            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }

		    return true;
	    }
    };

    private void show_str(){
        TextView t = findViewById(R.id.textStr);
        //t.setTextIsSelectable(false);
        t.setVisibility(View.VISIBLE);
    }
    private void hide_str(){
        TextView t = findViewById(R.id.textStr);
        t.setVisibility(View.INVISIBLE);
    }

    private void show_ans(){
        TextView t = findViewById(R.id.textAns2);
        t.setVisibility(View.VISIBLE);
    }
    private void hide_ans(){
        TextView t = findViewById(R.id.textAns2);
        t.setVisibility(View.INVISIBLE);
    }

    private void set_viewpager(){
        myFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), this);
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(myFragmentPagerAdapter);
        myFragmentPagerAdapter.prepare_to_initialize(
        		new View.OnLongClickListener[]{ //Fragment1
                        additional_longclick,
                        returnback,
                        fordel
                }, new View.OnLongClickListener[]{ //Fragment2
                        memory_actions
                }
        );
        viewPager.setCurrentItem(0);
    }

    private void add_value_from_mem(String value){
        TextView t = findViewById(R.id.textStr);
        String txt = t.getText().toString();
        if(txt.equals("")) {
            t.setText(value);
            show_str();
            hide_ans();
        }else{
            char last = txt.charAt(txt.length() - 1);
            if(new BigDecimal(value).signum() < 0)
                value = "(" + value + ")";
            if(Utils.isDigit(last) || last == '%' || last == '!'
                    || Character.toString(last).equals(FI)
                    || Character.toString(last).equals(PI) || last == 'e' || last == ')'){
                t.setText(String.format("%s%s%s", txt, MULTIPLY_SIGN, value));
                equallu("not");
            }else{
                t.setText(String.format("%s%s", txt, value));
                equallu("not");
            }
        }
    }

    private void register_broadcasters(){
        BroadcastReceiver on_memory_edited = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                barr = memorySaverReader.read();
            }
        };
        registerReceiver(on_memory_edited, new IntentFilter(BuildConfig.APPLICATION_ID + ".MEMORY_EDITED"));
        BroadcastReceiver on_recall_mem = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String value = intent.getStringExtra("value");
                add_value_from_mem(value);
            }
        };
        registerReceiver(on_recall_mem, new IntentFilter(BuildConfig.APPLICATION_ID + ".RECALL_MEM"));

        BroadcastReceiver on_theme_changed = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
		        DarkMode = sp.getBoolean("dark_mode", false);
		        TextView t = findViewById(R.id.textStr);
		        String str = t.getText().toString(), ans;
		        t = findViewById(R.id.textAns2);
		        ans = t.getText().toString();
		        if(DarkMode){
			        setTheme(android.R.style.Theme_Material_NoActionBar);
		        }else{
			        setTheme(R.style.AppTheme);
		        }
		        setContentView(R.layout.activity_main);
		        t.setText(ans);
		        t = findViewById(R.id.textStr);
		        t.setText(str);
		        apply_theme();
		        set_viewpager();
	        }
        };
        registerReceiver(on_theme_changed, new IntentFilter(BuildConfig.APPLICATION_ID + ".THEME_CHANGED"));

        BroadcastReceiver on_his_action = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                t = findViewById(R.id.textStr);
                if(!intent.getStringExtra("example").equals("")){
                    String txt = t.getText().toString();
                    if(!txt.equals("") && txt.contains(".")){
                        if(Utils.islet(txt.charAt(txt.length() - 1)))
                            return;
                        boolean was_action = false;
                        for(int i = txt.length() - 1; i >= 0; i--){
                            if(isaction(txt.charAt(i)) || Utils.islet(txt.charAt(i))
                                    || Character.toString(txt.charAt(i)).equals(getResources().getString(R.string.fi))
                                    || Character.toString(txt.charAt(i)).equals(getResources().getString(R.string.pi)) || txt.charAt(i) == 'e'){
                                was_action = true;
                            }
                            if(txt.charAt(i) == '.'){
                                if(!was_action){
                                    return;
                                }
                            }
                        }
                    }
                    int len = txt.length();
                    char last = '\0';
                    String example = intent.getStringExtra("example"), result = intent.getStringExtra("result");
                    if(len != 0){
                        last = txt.charAt(len-1);
                    }else{
                        txt = example;
                        t.setText(txt);
                        equallu("not");
                        return;
                    }
                    if(!Utils.isDigit(last)){
                        if(last != '!' && last != '%'){
                            txt = txt  + result;
                            t.setText(txt);
                        }
                    }else{
                        txt = txt  + result;
                        t.setText(txt);
                    }
                    sp.edit().remove("action").apply();
                    sp.edit().remove("history_action").apply();
                    equallu("not");
                }
                sp.edit().remove("action").apply();
                sp.edit().remove("history_action").apply();
            }
        };
        registerReceiver(on_his_action, new IntentFilter(BuildConfig.APPLICATION_ID + ".HISTORY_ACTION"));
        BroadcastReceiver on_sp_edited = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent resultIntent = new Intent(getApplicationContext(), Updater.class);
                resultIntent.putExtra("action", "simple");
                if(uptype.equals("simple"))
                    resultIntent.putExtra("update_path", "/NewMCalc.apk");
                else
                    resultIntent.putExtra("update_path", "/forTesters/NewMCalc.apk");
                startActivity(resultIntent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        };
        registerReceiver(on_sp_edited, new IntentFilter(BuildConfig.APPLICATION_ID + ".SP_EDITED"));
    }

    void format(int id){
        TextView t = findViewById(id);
        String txt = t.getText().toString();
        if(txt.equals("") || txt.length() < 4)
            return;
        t.setText(format_core2(txt));
    }

    /**
     MaxSav Team Technologies
     */
    private String format_core2(String txt){
        String number = "";
        int spaces = 0, dot_pos = -1, len = txt.length(), i = len - 1, nums = 0;
        for(; i >= 0 && (Utils.isDigit(txt.charAt(i)) || txt.charAt(i) == ' ' || txt.charAt(i) == '.'); i--){
            if(txt.charAt(i) != ' '){
                number = txt.charAt(i) + number;
            }else
                spaces++;
        }
        txt = txt.substring(0, len - number.length() - spaces);
        len = number.length();
        if(len < 4)
            return txt + number;
        if(number.contains(".")){
            dot_pos = number.indexOf(".");
        }
        String number_on_ret;
        if(dot_pos == -1)
            number_on_ret = "";
        else
            number_on_ret = number.substring(dot_pos);
        for(i = (dot_pos == -1 ? len - 1 : number.indexOf(".") - 1); i >= 0; i--, nums++){
            /*number_on_ret += number.charAt(i);
            if(i != 0 && i != len - 1){
                if(i % 3 == 0 && (dot_pos == -1 || i < dot_pos)){
                    number_on_ret += " ";
                }
            }*/
            if(nums != 0 && nums % 3 == 0){// && (dot_pos == -1 || i < dot_pos)){
                number_on_ret = " " + number_on_ret;
            }
            number_on_ret = number.charAt(i) + number_on_ret;
        }
        return txt + number_on_ret;
    }

    protected String calc_e(String s){
        String result = "";
        String before_e = "";
        int i = 0;
        for(; i < s.length(); i++){
            if(s.charAt(i) == 'E'){
                break;
            }else{
                before_e += Character.toString(s.charAt(i));
            }
        }
        /*if(before_e.contains(".")){
            before_e.replaceAll(".", "");
        }*/
        String after_e = "";
        i++;
        int a = 1;
        for(; i < s.length(); i++){
            if(s.charAt(i) == '-'){
                a = -1;
                continue;
            }
            after_e += Character.toString(s.charAt(i));
        }
        for(int j = 0; j < new Integer(after_e)-1; j++){
            result += "0";
        }
        String news = "";
        for(int j = 0; j < before_e.length(); j++){
            if(before_e.charAt(j) != '.'){
                news += Character.toString(before_e.charAt(j));
            }
        }
        before_e = news;
        news = null;
        System.gc();
        if (a == 1){
            result = before_e + result;
        }else if(a == -1){
            result = "0." + result;
            result += before_e;
        }
        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        return result;
    }
    
    private void write_result_of_calculation(String type, BigDecimal result){
        switch (type) {
            case "all":
                TextView tans = findViewById(R.id.textStr);
                String ans = result.toPlainString();
                if(ans.contains(".")) {
                    if (ans.length() - ans.indexOf(".") > 9){
                        BigDecimal d = result;
                        d = d.divide(BigDecimal.ONE, 8, RoundingMode.HALF_EVEN);
                        ans = d.toPlainString();
                    }
                }
                tans.setText(ans);
                format(R.id.textStr);
                tans = findViewById(R.id.textAns2);
                tans.setText(original);
                show_ans();
                scroll_to_end();
                resize_text();

                String his = sp.getString("history", "");
                StringBuilder sb = new StringBuilder(original);
                for (int i = 0; i < sb.length(); i++) {
                    if (sb.charAt(i) == ' ') {
                        sb.deleteCharAt(i);
                    }
                }
                String for_his = sb.toString();
                if (his.indexOf(for_his + "," + result.toPlainString() + ";") != 0) {
                    his = original + "," + result.toString() + ";" + his;
                    sp.edit().putString("history", his).apply();
                }
                if (sp.getBoolean("saveResult", false))
                    sp.edit().putString("saveResultText", original + ";" + result.toPlainString()).apply();
                break;
            case "not":
                TextView preans = findViewById(R.id.textAns2);
                String ans1 = result.toPlainString();
                if(ans1.contains(".")) {
                    if (ans1.length() - ans1.indexOf(".") > 9){
                        BigDecimal d = result;
                        d = d.divide(BigDecimal.ONE, 8, RoundingMode.HALF_EVEN);
                        ans1 = d.toPlainString();
                    }
                }
                preans.setText(ans1);
                format(R.id.textAns2);
                show_ans();
                set_text_toDef();
                resize_text();
                scroll_to_end();
                break;
            default:
                throw new IllegalArgumentException("Arguments should be of two types: all, not");
        }
    }

    public boolean isaction(char c){
        return c == '+'
                || c == '-'
                || c == '/'
                || c == '*'
                || Character.toString(c).equals(getResources().getString(R.string.multiply))
                || Character.toString(c).equals(getResources().getString(R.string.div))
                || Character.toString(c).equals(getResources().getString(R.string.sqrt))
                || c == 's'
                || c == 'c'
                || c == 'l'
                || c == 't'
                || c == '!'
                || c == '%'
                || c == '^';

    }

    public void resize_text(){
        TextView txt = findViewById(R.id.textStr);
        TextView t = findViewById(R.id.textAns2);

        String stri = txt.getText().toString(), strians = t.getText().toString();
        Rect bounds = new Rect();
        Rect boundsans = new Rect();
        Paint textPaint = txt.getPaint();
        Paint PaintAns = t.getPaint();
        textPaint.getTextBounds(stri, 0, stri.length(), bounds);
        PaintAns.getTextBounds(strians, 0, strians.length(), boundsans);
        int twidth = bounds.width();
        Display dis = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        dis.getSize(size);
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 46, getResources().getDisplayMetrics());
        int width = size.x - px - 45, answidth = size.x - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 54, getResources().getDisplayMetrics());
        while(twidth >= width && txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity > 32){
            txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity - 1);
            if(boundsans.width() >= answidth)
                t.setTextSize(TypedValue.COMPLEX_UNIT_SP, t.getTextSize() / getResources().getDisplayMetrics().scaledDensity - 1);
            textPaint.getTextBounds(stri, 0, stri.length(), bounds);
            PaintAns.getTextBounds(strians, 0, strians.length(), boundsans);
            twidth = bounds.width();
        }
        while(bounds.width() >= answidth && t.getTextSize() / getResources().getDisplayMetrics().scaledDensity > 29){
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, t.getTextSize() / getResources().getDisplayMetrics().scaledDensity - 1);
            PaintAns.getTextBounds(strians, 0, strians.length(), boundsans);
        }
        if (twidth < width && txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity < 46){
            txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity + 1);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, t.getTextSize() / getResources().getDisplayMetrics().scaledDensity + 1);
            textPaint.getTextBounds(stri, 0, stri.length(), bounds);
        }
    }

    public void set_text_toDef(){
        TextView txt = findViewById(R.id.textStr);
        TextView t = findViewById(R.id.textAns2);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 46);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
    }

    @Override
    public void onSuccess(BigDecimal result, String type) {
        write_result_of_calculation(type, result);
    }

    @Override
    public void onError(String error) {
        //if(error.startsWith("/Core/"))
        boolean showErr = sp.getBoolean("show_calc_errors", true);
        if(showErr && !error.startsWith("/Core/")){
            Toast toast = Toast.makeText(this, error, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
        hide_ans();
    }

    private void equallu(String type){
        TextView txt = findViewById(R.id.textStr);
        String example = txt.getText().toString();
        format(R.id.textStr);
        int len = example.length();
        if(len != 0) {
            show_str();
            scroll_to_end();
        }else
        	return;
        char last = example.charAt(len - 1);
        if(!Utils.isDigit(last) && last != ')' && last != '!' && last != '%'
                && !Character.toString(last).equals(FI) && !Character.toString(last).equals(PI) && last != 'e') {
            hide_ans();
            return;
        }
        if(last == '(')
            return;
        resize_text();
        brackets = 0;
        for(int i = 0; i < example.length(); i++){
            if(example.charAt(i) == '(')
                brackets++;
            else if(example.charAt(i) == ')')
                brackets--;
        }
        if(brackets > 0){
            StringBuilder exampleBuilder = new StringBuilder(example);
            for(int i = 0; i < brackets; i++){
                exampleBuilder.append(")");
            }
            example = exampleBuilder.toString();
            brackets = 0;
        }
        was_error = false;
        original = example;
        coreMain.prepare(example, type);
    }

    protected void check_dot(){
        TextView t = findViewById(R.id.textStr);
        String txt = t.getText().toString();
        int i = txt.length()-1;
        if(!Utils.isDigit(txt.charAt(i))){
            return;
        }
        boolean dot = false;
        while(i >= 0 && (Utils.isDigit(txt.charAt(i)) || txt.charAt(i) == '.')){
            if(txt.charAt(i) == '.'){
                dot = true;
                break;
            }else{
                i--;
            }
        }
        if(!dot){
            t.setText(txt + ".");
        }
    }

    public void call_equally(View v){
        equallu("all");
    }

    enum EnterModes{
        SIMPLE,
        AVERAGE,
        GEOMETRIC
    }

    EnterModes add_text_mode = EnterModes.SIMPLE;

    boolean isSpecific(char last){
        return last == ')' || last == '!' || last == '%' || Character.toString(last).equals(PI) || Character.toString(last).equals(FI);
    }

    @SuppressLint("SetTextI18n")
    public void add_text(String btntxt){
        t = findViewById(R.id.textStr);
        String txt = t.getText().toString();
        int len = txt.length();
        if(len >= 300000)
            return;
        //log("add text. len - " + len + ", btntxt - " + btntxt);
        if(len != 0)
            last = txt.charAt(len-1);

        if(btntxt.equals("A")){
            if(len == 0){
                t.setText(btntxt + "(");
                equallu("not");
                add_text_mode = EnterModes.AVERAGE;
                return;
            }
            if(add_text_mode != EnterModes.SIMPLE)
                return;

            if(Utils.isDigit(last) || isSpecific(last)){
                t.setText(txt + MULTIPLY_SIGN + btntxt + "(");
                equallu("not");
                add_text_mode = EnterModes.AVERAGE;
            }else if(!Utils.isDigit(last) && !isSpecific(last)){
                t.setText(txt + btntxt + "(");
                equallu("not");
                add_text_mode = EnterModes.AVERAGE;
            }
        }else if(btntxt.equals("G")){
            if(len == 0){
                t.setText(btntxt + "(");
                equallu("not");
                add_text_mode = EnterModes.GEOMETRIC;
                return;
            }
            if(add_text_mode != EnterModes.SIMPLE)
                return;

            if(Utils.isDigit(last) || isSpecific(last)){
                t.setText(txt + MULTIPLY_SIGN + btntxt + "(");
                equallu("not");
                add_text_mode = EnterModes.GEOMETRIC;
            }else if(!Utils.isDigit(last) && !isSpecific(last)){
                t.setText(txt + btntxt + "(");
                equallu("not");
                add_text_mode = EnterModes.GEOMETRIC;
            }
        }
        if(add_text_mode != EnterModes.SIMPLE){
            if(btntxt.equals(")")){
                if(Utils.isDigit(last)){
                    t.setText(txt + btntxt);
                    equallu("not");
                }else{
                    txt = txt.substring(0, txt.length() - 1);
                    t.setText(txt + btntxt);
                    equallu("not");
                }
                add_text_mode = EnterModes.SIMPLE;
                return;
            }
            if (add_text_mode == EnterModes.AVERAGE && (btntxt.length() > 1 || (!btntxt.equals("+") && !btntxt.equals("."))) && !Utils.isDigit(btntxt.charAt(0))) {
                return;
            }
            if (add_text_mode == EnterModes.GEOMETRIC && (btntxt.length() > 1 || (!btntxt.equals(MULTIPLY_SIGN) && !btntxt.equals("."))) && !Utils.isDigit(btntxt.charAt(0))) {
                return;
            }
        }

        if(Utils.isDigit(btntxt)){
            if(txt.equals("0")) {
                t.setText(btntxt);
                return;
            }
            if(len > 1){
                if(last == '0' && !Utils.isDigit(txt.charAt(len - 2))){
                    txt = txt.substring(0, len - 1) + btntxt;
                    t.setText(txt);
                    equallu("not");
                    return;
                }
            }
        }
        if(btntxt.equals("φ") || btntxt.equals("π") || btntxt.equals("e")){
            if(len == 0){
                t.setText(btntxt);
                equallu("not");
                return;
            }else{
                if(!Utils.isDigit(txt.charAt(len-1))){
                    if(txt.charAt(len-1) != '.') {
                        t.setText(txt + btntxt);
                        equallu("not");
                        return;
                    }
                }else{
                    t.setText(txt + btntxt);
                    scroll_to_end();
                }
            }
            return;
        }

        brackets = 0;
        for(int i = 0; i < len; i++){
            if(txt.charAt(i) == '(')
                brackets++;
            else if(txt.charAt(i) == ')')
                brackets--;
        }

        if(btntxt.equals("^")){
            if(len == 0 || last == '(')
                return;

            if(Utils.isDigit(last) || Character.toString(last).equals(FI) || Character.toString(last).equals(PI) || last == 'e'){
                t.setText(txt + btntxt + "(");
                equallu("not");
                return;
            }
            if(!Utils.isDigit(last)){
                if(last == '!' || last == '%'){
                    t.setText(txt + btntxt + "(");
                    equallu("not");
                }
            }
            return;
        }

        if(btntxt.equals("√")){
            if(len == 0){
                t.setText(btntxt + "(");
                brackets++;
                show_str();
                scroll_to_end();
                return;
            }else{
                String x = "";
                for(int i = 0; i < len; i++){
                    if(!Utils.isDigit(txt.charAt(i)) && txt.charAt(i) != '.'){
                        break;
                    }else{
                        if(Utils.isDigit(txt.charAt(i)) || txt.charAt(i) != '.')
                            x += Character.toString(txt.charAt(i));
                    }
                }
                if(x.length() == len){
                    double sq = Double.valueOf(x);
                    sq = Math.sqrt(sq);
                    String answer = Double.toString(sq);
                    int lend = answer.length();
                    if(answer.charAt(len-1) == '0'){
                        while (lend > 0 && answer.charAt(lend - 1) == '0') {
                            lend--;
                            answer = answer.substring(0, lend);
                        }
                        if(answer.charAt(lend-1) == '.'){
                            answer = answer.substring(0, lend-1);
                        }
                        sq = Double.valueOf(answer);
                    }
                    t.setText(Double.toString(sq));
                    scroll_to_end();
                    return;
                }else{
                    if(last == '.'){
                        return;
                    }else{
                        if(!Utils.isDigit(last)){
                            t.setText(txt + btntxt + "(");
                            brackets++;
                            //Toast.makeText(getApplicationContext(), Integer.toString(brackets), Toast.LENGTH_SHORT).show();
                            equallu("not");
                            show_str();
                            scroll_to_end();
                        }
                    }
                }
            }
            return;
        }

        if(btntxt.equals(")") && len == 0){
            return;
        }
        if(btntxt.equals("(")){
            if(last == '.')
                return;
            if((Utils.isDigit(last) || last == '!' || last == '%' || Utils.isConstNum(last, this)) && !txt.equals("")){
                t.setText(txt + MULTIPLY_SIGN + btntxt);
                equallu("not");
            }else{
                t.setText(txt + btntxt);
                equallu("not");
            }
            return;
        }
        if(btntxt.equals("sin") || btntxt.equals("log") || btntxt.equals("tan") || btntxt.equals("cos") || btntxt.equals("ln")){
            if(len == 0){
                t = findViewById(R.id.textStr);
                t.setText(btntxt + "(");
                brackets++;
                //Toast.makeText(getApplicationContext(), Integer.toString(brackets), Toast.LENGTH_SHORT).show();
                equallu("not");
            }else{
                if(last == '.'){
                    return;
                }else{
                	if(!Utils.isDigit(last) && last != '!' && !Character.toString(last).equals(FI) && !Character.toString(last).equals(PI) && last != 'e'){
						t.setText(txt + btntxt + "(");
						equallu("not");
	                }else {
		                if (last != '(' && last != '^') {
			                t.setText(txt + getResources().getString(R.string.multiply) + btntxt + "(");
			                equallu("not");
		                }
	                }
                }
            }
            return;
        }

        if(len != 0 && last == '(' && btntxt.equals("-")){
            t.setText(txt + btntxt);
            equallu("not");
            return;
        }

        if(btntxt.equals(")")){
            //Toast.makeText(getApplicationContext(), Integer.toString(brackets), Toast.LENGTH_SHORT).show();
            if(brackets > 0){
                if(last == ')'){
                    t.setText(txt + btntxt);
                    equallu("not");
                    return;
                }
                if(last == '(')
                    return;
                if(!Utils.isDigit(last) && !Character.toString(last).equals(getResources().getString(R.string.pi))
                        && !Character.toString(last).equals(getResources().getString(R.string.fi)) && last != 'e' && last != '!' && last != '%'){
                    if(len != 1){
                        txt = txt.substring(0, len-1);
                        t.setText(txt + btntxt);
                        equallu("not");
                    }
                }else{
                    t.setText(txt + btntxt);
                    equallu("not");
                }
                brackets--;
            }
            return;
        }

        if(!Utils.isDigit(btntxt) && !btntxt.equals("(") && !Utils.islet(btntxt.charAt(0))){
            if(len != 0){
                if(txt.charAt(len-1) == 'π' || txt.charAt(len-1) == 'φ' || txt.charAt(len-1) == 'e'){
                    t.setText(txt + btntxt);
                    equallu("not");
                    return;
                }
            }
        }

        if((last == '!' || last == '%') && !btntxt.equals(".") && !btntxt.equals("!") && !btntxt.equals("%")){
            t.setText(txt + btntxt);
            equallu("not");
            return;
        }
        if(btntxt.equals("+") || btntxt.equals("-")
                || btntxt.equals(getResources().getString(R.string.multiply))
                || btntxt.equals(getResources().getString(R.string.div))){
            if(last == '(' && !btntxt.equals("-")) {
                return;
            }else if(last == '('){
                t.setText(txt + btntxt);
                equallu("not");
                return;
            }
        }

        if(btntxt.equals(".")){
            if(txt.equals(""))
                t.setText("0.");
            else
            if(!Utils.isDigit(txt.charAt(len-1)) && txt.charAt(len-1) != '.' && last != '!')
                t.setText(txt + "0.");
            else
                check_dot();
        }else{
            if(btntxt.equals("!") || btntxt.equals("%")){
                if(!txt.equals("")){
                    if(btntxt.equals("!")){
                        if(last == '!'){
                            if(txt.charAt(len - 2) != '!'){
                                t.setText(txt + btntxt);
                                equallu("not");
                                return;
                            }
                        }
                    }else{
                        if(last != '%'){
                            t.setText(txt + btntxt);
                            equallu("not");
                            return;
                        }
                    }
                    if( last != ')' && !Utils.isDigit(last) && (len > 1 && last != '(' && Utils.islet(txt.charAt(len - 2))
                            && !Character.toString(txt.charAt(len - 2)).equals(PI) && !Character.toString(txt.charAt(len-2)).equals(FI) && txt.charAt(len - 2) != 'e') ){
                        txt = txt.substring(0, len-1);
                        t.setText(txt + btntxt);
                        equallu("not");
                    }else{
                        if(Utils.isDigit(last) || last == ')'){
                            t.setText(txt + btntxt);
                            equallu("not");
                        }
                    }
                }
                return;
            }
            if(len >= 1 && (last == ')' && !Utils.isDigit(btntxt.charAt(0)))) {
                t.setText(txt + btntxt);
                equallu("not");
            }else{
                if(!txt.equals("")){
                    if(!Utils.isDigit(btntxt.charAt(0))){

                        if(!Utils.isDigit(txt.charAt(len-1))){
                            if(len != 1){
                                txt = txt.substring(0, len-1);
                                t.setText(txt + btntxt);
                                equallu("not");
                            }
                        }else{
                            t.setText(txt + btntxt);
                            equallu("not");
                        }
                    }else{
                        if(Utils.isDigit(btntxt.charAt(0))){
                            t.setText(txt + btntxt);
                            equallu("not");
                        }
                    }
                }else{
                    if(Utils.isDigit(btntxt.charAt(0)) || btntxt.equals("-")){
                        t.setText(btntxt);
                        equallu("not");
                    }
                }
            }
        }

    }

    public void onClick(@NonNull View v){
        Button btn = findViewById(v.getId());
        t = findViewById(R.id.textStr);
        String btntxt = btn.getText().toString().substring(0, 1);
        add_text(btntxt);
       // log("onClick action;");
    }

    public void delall(View v){
        TextView t = findViewById(R.id.textStr);
        hide_str();
        t.setText("");
        t = findViewById(R.id.textAns2);
        hide_ans();
        t.setText("");
        brackets = 0;
        was_error = false;
        sp.edit().remove("saveResultText").apply();
        t = findViewById(R.id.textStr);
        set_text_toDef();
        //log("all text del");
    }

    /*public boolean iscpecial(char c){
        return c == '!' || c == '%' || Character.toString(c).equals(FI) || Character.toString(c).equals(PI) || c == 'e';
    }*/

    public void delSymbol(View v){
        TextView txt = findViewById(R.id.textStr);
        String text = txt.getText().toString();
        int len = text.length();
        //ans.setVisibility(View.INVISIBLE);
        /**/
        if(len != 0){
            char last = text.charAt(len - 1);
            int a = 1;
            if(last == ')' && (text.contains("A") || text.contains("G"))){
                int i = len-1;
                while(i >= 1 && text.charAt(i) != '(')
                    i--;
                i--;
                if(text.charAt(i) == 'A'){
                    add_text_mode = EnterModes.AVERAGE;
                }else if(text.charAt(i) == 'G'){
                    add_text_mode = EnterModes.GEOMETRIC;
                }
            }
            if(last == '(' && len > 1){
                if(text.charAt(text.length() - 2) == '√'
                        || text.charAt(len - 2) == '^') {
                    a = 2;
                }
                if( text.charAt(len - 2) == 'A' || text.charAt(len-2) == 'G'){
                    a = 2;
                    add_text_mode = EnterModes.SIMPLE;
                }
                if(text.charAt(len - 2) == 's' || text.charAt(len - 2) == 'g')
                	a = 4;
                if(text.charAt(len - 2) == 'n'){
                	if(text.charAt(len - 3) == 'l')
                		a = 3;
                	else if(text.charAt(len - 3) == 'i' || text.charAt(len - 3) == 'a')
                		a = 4;
                }
            }
            was_error = false;
            if(text.length() - a == 0)
                hide_str();
            text = text.substring(0, text.length() - a);
            txt.setText(text);
            equallu("not");
        }
        if(txt.getText().toString().equals("")){
            t = findViewById(R.id.textAns2);
            /*HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
            scrollviewans.setVisibility(HorizontalScrollView.INVISIBLE);*/
            hide_ans();
            //t.setText("");
            sp.edit().remove("saveResultText").apply();
            set_text_toDef();
        }
        scroll_to_end();
    }

    protected void scroll_to_end(){
        if(findViewById(R.id.textStr).getVisibility() == View.INVISIBLE)
            return;
        HorizontalScrollView scrollview = findViewById(R.id.scrollview);
        scrollview.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 50L);
        HorizontalScrollView scrollview1 = findViewById(R.id.scrollViewAns);
        scrollview.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollview1.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 50L);
    }
}