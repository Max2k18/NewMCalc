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
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.maxsavteam.newmcalc.adapters.window_recall_adapter;
import com.maxsavteam.newmcalc.exceptions.MyEmptyStackException;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import static com.maxsavteam.newmcalc.R.drawable.settings_dark;



public class MainActivity extends AppCompatActivity implements window_recall_adapter.inter {

    public static Stack<String> s0 = new Stack<>();
    public static Stack<BigDecimal> s1 = new Stack<>();
    public static Boolean was_error = false;

    public boolean isOtherActivityOpened = false;
    public SharedPreferences sp;
    public Button btn;
    public TextView t;
    public char last;
    public int brackets = 0;
    public String original = "";
    public String newVer = "";
    public AlertDialog about_app, al;
    public String newDevVer = "";
    public boolean add_menu_opened = false;
    public AlertDialog not_btn_pr = null;
    public String uptype = "simple";
    public int newCodeDev = 0, newVerCode = 0;
    public FirebaseAnalytics fr;
    public String app_type;
    public BigDecimal[] barr;
    public BigDecimal result_calc_for_mem;
    public TextView text_example;
    String FI, PI, E;
    final int UPDCHECKER_PERIOD = 1, UPDDELAY = 10000;
    final int READ_STORAGE_RESUEST_CODE = 10, WRITE_STORAGE_REQUEST_CODE = 12;

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
        if(ups.isup()){
            AlertDialog.Builder dialogal = new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.confirm_exit_on_up)
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            if(ups.isup()){
                                ups.kill();
                            }
                            finishAndRemoveTask();
                            overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha_hide);
                        }
                    });

            AlertDialog dal = dialogal.create();
            if(DarkMode)
                dal.getWindow().setBackgroundDrawableResource(R.drawable.grey);
            dal.show();
        }else{
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
        }
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

    /*protected void sh_dl_update(String flag, int versionNew, String newver, String text, boolean compileWithView){
        View mView = getLayoutInflater().inflate(R.layout.alert_checkbox, null);
        CheckBox mcheck = mView.findViewById(R.id.checkBoxAlert);
        if(DarkMode)
            mcheck.setTextColor(getResources().getColor(R.color.white));
        sp.edit().putBoolean("notification_showed", true).apply();
        //mcheck.setOnClickListener(forcheck);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.updateavail)
                .setMessage(text
                        + "\n\n" + getResources().getString(R.string.version) + " " + newver + "\n"
                        + getResources().getString(R.string.build) + " " + versionNew)
                .setCancelable(false)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sp.edit().remove("notification_showed").apply();
                        dialog.cancel();
                        if(mcheck.isChecked()){
                            sp.edit().putInt("notremindfor", versionNew).apply();
                        }
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sp.edit().remove("notification_showed").apply();
                        dialog.cancel();
                        if(uptype.equals("dev")){
                            ups.run("/forTesters/NewMCalc.apk", newversiondev, bytescountupdate);
                        }
                        else{
                            ups.run("/NewMCalc.apk", newversionsimple, bytescountupdate);
                        }
                    }
                });
        if(compileWithView)
            builder.setView(mView);
        al = builder.create();
        if(DarkMode)
            al.getWindow().setBackgroundDrawableResource(R.drawable.grey);
        if(flag.equals("fromUPDChecker")){
            al.show();
        }
    }*/

    public void onClickAdd(View v){
        if(v.getId() == R.id.imgBtnSettings){
            goto_add_scr("settings");
        }else if(v.getId() == R.id.btnImgNumGen){
            goto_add_scr("numgen");
        }else if(v.getId() == R.id.btnImgHistory){
            goto_add_scr("history");
        }else if(v.getId() == R.id.btnImgPassgen){
            goto_add_scr("pass");
        }
    }

    protected void goto_add_scr(String where){
        Intent resultIntent;
        switch (where) {
            case "settings":
                resultIntent = new Intent(getApplicationContext(), Updater.class);
                resultIntent.putExtra("action", "simple");
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
        }
    }



    public void show_hide(View v){
        LinearLayout ll = findViewById(R.id.additional_tools);
        int loc = sp.getInt("btn_add_align", 0);
        Button b = findViewById(v.getId());
        ConstraintLayout cl = findViewById(R.id.constraight);
        findViewById(R.id.imgBtnSettings).setBackground(cl.getBackground());
        findViewById(R.id.btnImgNumGen).setBackground(cl.getBackground());
        findViewById(R.id.btnImgHistory).setBackground(cl.getBackground());
        findViewById(R.id.btnImgPassgen).setBackground(cl.getBackground());
        int time = 100;
        RelativeLayout.LayoutParams par = new RelativeLayout.LayoutParams(35, 85);
        Button sh = findViewById(R.id.btnShAdd);
        Button sh2 = findViewById(R.id.btnShAdd2);
        if((b.getText().toString().equals(">") && loc == 0) || (b.getText().toString().equals("<") && loc == 1)){
            par = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35 + 80 * 4, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()));
            //b.setText(onhide);
            if(loc == 0){
                b.setText("<");
            }else {
                sh2.setVisibility(View.VISIBLE);
                sh.setVisibility(View.GONE);
                sh2.setText(">");
            }
            add_menu_opened = true;
        }else{
            add_menu_opened = false;
            par = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()));
            //b.setText(onshow);
            if(loc == 0){
                sh.setVisibility(View.VISIBLE);
                sh2.setVisibility(View.GONE);
                sh.setText(">");
            }else if(loc == 1){
                sh.setVisibility(View.VISIBLE);
                sh2.setVisibility(View.GONE);
                sh.setText("<");
            }
        }
        ll.setLayoutParams(par);
    }

    /*protected void btn_change(){
        int loc = sp.getInt("btn_add_align", 0);
        RelativeLayout rl = findViewById(R.id.relativelayout);
        ConstraintLayout.LayoutParams par =(ConstraintLayout.LayoutParams) rl.getLayoutParams();
        if(loc == 0){
            onshow = ">";
            onhide = "<";
        }else if(loc == 1){
            onshow = "<";
            onhide = ">";
        }
        Button sh2 = findViewById(R.id.btnShAdd);
        if(loc == 0){
            sh2 = findViewById(R.id.btnShAdd);
            //sh2.setVisibility(View.VISIBLE);
            //sh2 = findViewById(R.id.btnShAdd2);
            //sh2.setVisibility(View.GONE);
            par.horizontalBias = Float.valueOf(Integer.toString(loc) + ".0f");
            rl.setLayoutParams(par);
            rl.setGravity(Gravity.LEFT);
            rl.setTranslationX(22);
            sh2.setText(">");
        }else if(loc == 1){
            sh2 = findViewById(R.id.btnShAdd);
            //sh2.setVisibility(View.VISIBLE);
            //sh2 = findViewById(R.id.btnShAdd);
            //sh2.setVisibility(View.GONE);
            par.horizontalBias = Float.valueOf(Integer.toString(loc) + ".0f");
            rl.setLayoutParams(par);
            rl.setGravity(Gravity.RIGHT);
            rl.setTranslationX(-20);
            sh2.setText("<");
        }
    }*/

    boolean DarkMode = false;
    String ver_desc = "";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        DarkMode = sp.getBoolean("dark_mode", false);
        sp.edit().remove("simple_upd_exist").apply();
        sp.edit().remove("dev_upd_exist").apply();
        sp.edit().remove("notification_showed").apply();
        if(DarkMode){
            setTheme(android.R.style.Theme_Material_NoActionBar);
        }else{
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apply_theme();
        fr = FirebaseAnalytics.getInstance(getApplicationContext());
        Trace myTrace = FirebasePerformance.getInstance().newTrace("AppStart");
        myTrace.start();
        btn = findViewById(R.id.btnDelete);
        btn.setOnLongClickListener(fordel);
        //TextView ver = findViewById(R.id.lblVer);
        ver_desc = getResources().getString(R.string.version) + " " + BuildConfig.VERSION_NAME + "\n" + getResources().getString(R.string.build) + " " + BuildConfig.VERSION_CODE;
        //ver.setText(ver_desc);
        //ver.setText(getResources().getString(R.string.version) + " " + BuildConfig.VERSION_NAME + "\n" + getResources().getString(R.string.build) + " " + BuildConfig.VERSION_CODE);// + "\n" + "CompileName: " + BuildConfig.COMPILENAME);
        Button btn1 = findViewById(R.id.btnCalc);
        btn1.setOnLongClickListener(returnback);
        text_example = findViewById(R.id.textStr);
        if(DarkMode)
            btn1.setTextColor(getResources().getColor(R.color.white));
        else
            btn1.setTextColor(getResources().getColor(R.color.black));

        PI = getResources().getString(R.string.pi);
        FI = getResources().getString(R.string.fi);
        E = "e";
        try{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
        Intent start_in = getIntent();
        /*RelativeLayout.LayoutParams par;
        add_menu_opened = false;
        par = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()));
        LinearLayout ll = findViewById(R.id.additional_tools);
        ll.setLayoutParams(par);*/
        if(start_in.getBooleanExtra("shortcut_action", false)){
            goto_add_scr(start_in.getStringExtra("to_"));
            /*if(sp.getInt("btn_add_align", 0) == 0){
                show_hide(findViewById(R.id.btnShAdd));
            }else{
                show_hide(findViewById(R.id.btnShAdd2));
            }*/
            //show_hide(findViewById(R.id.btnShAdd));
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

            shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut3, shortcut2, shortcut1));
        }
        /*BroadcastReceiver on_lang = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                set_lang();
            }
        };
        registerReceiver(on_lang, new IntentFilter(BuildConfig.APPLICATION_ID + ".LANG_CH"));*/
       /* btn1.setWidth(btn2.getWidth());
        btn1.setHeight(btn2.getHeight());
        btn.setHeight(btn2.getHeight());
        btn.setWidth(btn2.getWidth());
        btn1 = findViewById(R.id.btnDelAll);
        btn1.setHeight(btn2.getHeight());
        btn1.setWidth(btn2.getWidth());*/

        // btn_change();

        myTrace.stop();
        fr.logEvent("OnCreate", Bundle.EMPTY);
    }

    protected void set_enabled_additional_buttons(boolean b){
        int[] ids = new int[]{R.id.imgBtnSettings, R.id.btnImgHistory, R.id.btnImgNumGen, R.id.btnImgPassgen};
        for(int i : ids){
            findViewById(i).setEnabled(b);
        }
    }

    public void click_addtional_tools(View v){
        ImageButton btn = (ImageButton) v;
        LinearLayout lay = findViewById(R.id.additional_tools);
        if(btn.getTag().toString().equals("up")){
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_scale_show);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    btn.setImageDrawable(getDrawable(R.drawable.arrow_down));
                    btn.setTag("down");
                    //btn.setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //btn.setEnabled(true);
                    set_enabled_additional_buttons(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            lay.clearAnimation();
            lay.setAnimation(anim);
            lay.setVisibility(View.VISIBLE);
            lay.animate();
            anim.start();
        }else if(btn.getTag().toString().equals("down")){
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_scale_hide);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    btn.setImageDrawable(getDrawable(R.drawable.arrow_up));
                    btn.setTag("up");
                    //btn.setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    lay.setVisibility(View.GONE);
                    //btn.setEnabled(true);
                    set_enabled_additional_buttons(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            lay.clearAnimation();
            lay.setAnimation(anim);
            lay.animate();
            anim.start();
        }
    }

    public void apply_theme(){
        if(DarkMode){
            ImageButton img;
            int[] imgbtnids = new int[]{
                    R.id.imgBtnSettings,
                    R.id.btnImgHistory,
                    R.id.btnImgNumGen,
                    R.id.btnImgPassgen
            };
            Drawable[] drawables;
            drawables = new Drawable[]{
                    getResources().getDrawable(settings_dark),
                    getResources().getDrawable(R.drawable.history_dark),
                    getResources().getDrawable(R.drawable.dice_dark),
                    getResources().getDrawable(R.drawable.passgen_dark)
            };
            for(int i = 0; i < imgbtnids.length; i++){
                img = findViewById(imgbtnids[i]);
                img.setImageDrawable(drawables[i]);
            }
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
            Button b = findViewById(R.id.btnDelAll);
            //b.setBackgroundColor(getResources().getColor(R.color.white));
            b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            b.setTextColor(getResources().getColor(R.color.black));
            b = findViewById(R.id.btnCalc);
            b.setTextColor(getResources().getColor(R.color.white));
            b = findViewById(R.id.btnShAdd);
            b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            b = findViewById(R.id.btnShAdd2);
            b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            int[] colorAccentIds = new int[]{
                    R.id.btnMemMinus,
                    R.id.btnMemPlus,
                    R.id.btnMR,
                    R.id.btnMS
            };
            for(int id : colorAccentIds){
                b = findViewById(id);
                b.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        }else{
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
            /*Button b = findViewById(R.id.btnDelAll);
            b.setBackgroundColor(getResources().getColor(R.color.black));
            b.setTextColor(getResources().getColor(R.color.white));*/
            Button b = findViewById(R.id.btnDelAll);
            //b.setBackgroundColor(getResources().getColor(R.color.white));
            b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            b.setTextColor(getResources().getColor(R.color.white));
            b = findViewById(R.id.btnCalc);
            b.setTextColor(getResources().getColor(R.color.black));
        }
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

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //new newver_check_service(this).create(sp.getBoolean("isdev", false));
        app_type = BuildConfig.APPTYPE;
        TableLayout tbl = findViewById(R.id.tbl);
        /*try {
            Display d = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            d.getSize(size);
            int height = size.y;
            int width = size.x;
            tbl.setLayoutParams(new TableLayout.LayoutParams(width - 20, height / 3));
        }catch (Throwable throwable){
            Toast.makeText(this, throwable.toString(), Toast.LENGTH_LONG).show();
        }*/
        sp.edit().remove("count_catchservice").apply();
        if(!sp.getBoolean("never_request_permissions", false) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)){
                /*ActivityCompat
                        .requestPermissions(this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                10);*/
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
        read_memory();
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
        boolean receive = sp.getBoolean("stop_receive_all", false);
        if(app_type.equals("tester") && !receive){
            sp.edit().putBoolean("isdev", true).apply();
        }
        /*if(app_type.equals("developer")){
            Crashlytics.setUserIdentifier("developer");
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if(!tm.getDeviceId().equals("869480036532475")) {
                finishAndRemoveTask();
                overridePendingTransition(R.anim.abc_popup_enter, R.anim.alpha_hide);
            }
        }*/

        apply_supertext_btns();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.about_layout, null);
        String message = getResources().getString(R.string.about_text)
                + "\n\n" + getResources().getString(R.string.version) + " " + BuildConfig.VERSION_NAME
                + (app_type.equals("stable") ? " stable" : " not stable")
                + "\n" + getResources().getString(R.string.build) + " " + BuildConfig.VERSION_CODE
                + "\nCompile date: " + BuildConfig.COMPILE_DATE;// + "\n\n" + "©" + "MaxSav Team, 2018-2019";
        if(!app_type.equals("stable")){
            message += "\n\nCV: " + BuildConfig.CoreVersion
                    + "\nUDV: " + BuildConfig.UpdateDowVersion
                    + "\nUCV: " + BuildConfig.UpdateCheckerVersion;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (app_type.equals("developer")) {
                message += "\n\n" + Html.fromHtml(getResources().getString(R.string.build_developer), Html.FROM_HTML_MODE_LEGACY);
            } else if (app_type.equals("tester")) {
                message += "\n\n" + Html.fromHtml(getResources().getString(R.string.build_tester), Html.FROM_HTML_MODE_LEGACY);
            }
        }else{
            if (app_type.equals("developer")) {
                message += "\n\n" + Html.fromHtml(getResources().getString(R.string.build_developer));
            } else if (app_type.equals("tester")) {
                message += "\n\n" + Html.fromHtml(getResources().getString(R.string.build_tester));
            }
        }
        message += "\n\n" + "©" + "MaxSav Team, 2018-2019";
        builder.setCancelable(false)
                .setTitle(R.string.about)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setView(view);
        about_app = builder.create();
        if(DarkMode)
            about_app.getWindow().setBackgroundDrawableResource(R.drawable.grey);

        Button b = findViewById(R.id.btnMR);
        b.setOnLongClickListener(memory_actions);
        b = findViewById(R.id.btnMS);
        b.setOnLongClickListener(memory_actions);
        b = findViewById(R.id.btnMemPlus);
        b.setOnClickListener(memory_plus);
        b = findViewById(R.id.btnMemMinus);
        b.setOnClickListener(memory_minus);
        trace.stop();
        //должно быть всегда in the end
        fr.logEvent("OnPostCreate", Bundle.EMPTY);
    }

    private void apply_supertext_btns(){
        String[] arr = getResources().getStringArray(R.array.additional_chars);
        int[] btnIds = {
                R.id.btn7,
                R.id.btn8,
                R.id.btn4,
                R.id.btn5,
                R.id.btn1,
                R.id.btn2,

                R.id.btn9,
                R.id.btnMult,
                R.id.btn6,
                R.id.btnDiv,
                R.id.btn3,
                R.id.btnPlus,

                R.id.btnZero,
                R.id.btnDot
        };
        for(int ii = 0; ii < btnIds.length; ii++){
            Button btn = findViewById(btnIds[ii]);
            btn.setTransformationMethod(null);
            btn.setOnLongClickListener(additional_longclick);
            String num = btn.getText().toString().substring(0, 1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                btn.setText(Html.fromHtml(num + "<sup><small><small><small>" + arr[ii] + "</small></small></small></sup>", Html.FROM_HTML_MODE_COMPACT));
            }else{
                btn.setText(Html.fromHtml(num + "<sup><small><small><small>" + arr[ii] + "</small></small></small></sup>"));
            }
        }
    }

    View.OnClickListener memory_plus = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(text_example.getText().toString().equals(""))
                return;

            BigDecimal temp;
            try{
                temp = new BigDecimal(text_example.getText().toString());
            }catch(NumberFormatException e){
                return;
            }
            temp = temp.add(barr[0]);
            barr[0] = temp;
            save_memory();
        }
    };
    View.OnClickListener memory_minus = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(text_example.getText().toString().equals(""))
                return;
            BigDecimal temp;
            try{
                temp = new BigDecimal(text_example.getText().toString());
            }catch(NumberFormatException e){
                return;
            }
            temp = temp.subtract(barr[0]);
            barr[0] = temp;
            save_memory();
        }
    };

    void read_memory(){
        String mem_loc = sp.getString("memory", "0$0$0$0$0$0$0$0$0$0$");
        barr = new BigDecimal[10];
        for(int i = 0; i < 10; i++){
            barr[i] = BigDecimal.valueOf(0);
        }
        int i = 0, cell = 0;
        while(i < mem_loc.length()){
            String num = "";
            while(mem_loc.charAt(i) != '$'){
                num += mem_loc.charAt(i);
                i++;
            }
            barr[cell] = new BigDecimal(num);
            cell++;
            i++;
        }
    }

    public void save_memory(){
        String mem = "";
        for(int i = 0; i < 10; i++){
            mem += barr[i].toString() + "$";
        }
        sp.edit().putString("memory", mem).apply();
        //Toast.makeText(getApplicationContext(), sp.getString("memory", "none"), Toast.LENGTH_SHORT).show();
    }

    public boolean memory_calculated = false, store_custom = false;
    public int store_params = 0;

    public void memory_ms(View view){
        TextView t = findViewById(R.id.textStr);
        if(t.getText().toString().equals(""))
            return;
        BigDecimal temp;
        try{
            temp = new BigDecimal(t.getText().toString());
        }catch(NumberFormatException e){
            return;
        }
        barr[0] = temp;
        save_memory();
    }

    public void memory_mr(View view){
        TextView t = findViewById(R.id.textStr);
        t.setText(barr[0].toString());
        show_str();
        t = findViewById(R.id.textAns2);
        hide_ans();
        t.setText("");
    }

    String recall_type = "";
    AlertDialog showMemAl;

    @Override
    public void onRecallClick(View view, int position) {
        if(recall_type.equals("rc")){
            TextView t = findViewById(R.id.textStr);
            t.setText(barr[position].toString());
            t = findViewById(R.id.textAns2);
            hide_ans();
            t.setText("");
        }else if(recall_type.equals("st")){
            store_custom = true;
            store_params = position;
            memory_ms(findViewById(R.id.btnMS));
        }
        showMemAl.cancel();
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

    void show_str(){
        TextView t = findViewById(R.id.textStr);
        //t.setTextIsSelectable(false);
        t.setVisibility(View.VISIBLE);
    }
    void hide_str(){
        TextView t = findViewById(R.id.textStr);
        t.setVisibility(View.INVISIBLE);
    }

    void show_ans(){
        TextView t = findViewById(R.id.textAns2);
        t.setVisibility(View.VISIBLE);
    }
    void hide_ans(){
        TextView t = findViewById(R.id.textAns2);
        t.setVisibility(View.INVISIBLE);
    }

    protected void register_broadcasters(){
        /*BroadcastReceiver on_version_checked = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type = intent.getStringExtra("type");
                String version = intent.getStringExtra("version");
                int code = intent.getIntExtra("code", 0);
                uptype = (type.equals("simple") ? type : "dev");
                if(!ups.isup()) {
                    if (type.equals("simple")) {
                        sp.edit().putBoolean("simple_upd_exist", true).putString("version", newVer).apply();
                        newversionsimple = version;
                        bytescountupdate = intent.getIntExtra("bytes", 0);
                        if (sp.getInt("notremindfor", 0) != code) {
                            sh_dl_update("fromUPDChecker", code, version, getResources().getString(R.string.updateavailable), true);
                        }
                    } else if (type.equals("tc")) {
                        newversiondev = version;
                        bytescountupdate = intent.getIntExtra("bytes", 0);
                        sp.edit().putBoolean("dev_upd_exist", true).putString("version", newVer).apply();
                        sh_dl_update("fromUPDChecker", code, version, getResources().getString(R.string.dev_update), false);
                    }
                }
            }
        };
        registerReceiver(on_version_checked, new IntentFilter(BuildConfig.APPLICATION_ID + ".VERSIONS_CHECKED"));*/


        BroadcastReceiver on_memory_edited = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                read_memory();
            }
        };
        registerReceiver(on_memory_edited, new IntentFilter(BuildConfig.APPLICATION_ID + ".MEMORY_EDITED"));
        BroadcastReceiver on_recall_mem = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TextView t = findViewById(R.id.textStr);
                t.setText(intent.getStringExtra("value"));
                show_str();
                hide_ans();
            }
        };
        registerReceiver(on_recall_mem, new IntentFilter(BuildConfig.APPLICATION_ID + ".RECALL_MEM"));

        Map<Integer, Boolean> m = new HashMap<>();

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
                apply_supertext_btns();
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
                        if(islet(txt.charAt(txt.length() - 1)))
                            return;
                        boolean was_action = false;
                        for(int i = txt.length() - 1; i >= 0; i--){
                            if(isaction(txt.charAt(i)) || islet(txt.charAt(i))
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
                    if(!isdigit(last)){
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
        for(; i >= 0 && (isdigit(txt.charAt(i)) || txt.charAt(i) == ' ' || txt.charAt(i) == '.'); i--){
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

    protected boolean isdigit(char c){
        return c >= '0' && c <= '9';
    }

    protected boolean isdigit(String x){
        return x.compareTo("0") >= 0 && x.compareTo("9") <= 0;
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

    protected void mult(String x) throws MyEmptyStackException {
        if(was_error)
            return;
        try {
            if (x.length() == 3 || x.equals("ln") || x.equals("R")) {
                double d = s1.peek().doubleValue(), ans = 1;
                if (x.equals("log") && d <= 0) {
                    //Toast.makeText(getApplicationContext(), "You cannot find the logarithm of a zero or a negative number.", Toast.LENGTH_SHORT).show();
                    return;
                }
                s1.pop();
                switch (x) {
                    case "cos": {
                        ans = Math.cos(d);
                        break;
                    }
                    case "sin": {
                        ans = Math.sin(d);
                        break;
                    }
                    case "tan": {
                        ans = Math.tan(d);
                        break;
                    }
                    case "log": {
                        ans = Math.log10(d);
                        break;
                    }
                    case "ln": {
                        ans = Math.log(d);
                        break;
                    }
                    case "R":
                        ans = Math.sqrt(d);
                        break;
                }

            String answer = Double.toString(ans);
            int len = answer.length();
            if(answer.charAt(len-1) == '0'){
                while (len > 0 && answer.charAt(len - 1) == '0') {
                    len--;
                    answer = answer.substring(0, len);
                }
                if(answer.charAt(len-1) == '.'){
                    answer = answer.substring(0, len-1);
                }
                ans = Double.valueOf(answer);
                s1.push(new BigDecimal(answer));
            }else{
                BigDecimal ansb = BigDecimal.valueOf(ans);

                    ansb = ansb.divide(BigDecimal.valueOf(1.0), 9, RoundingMode.HALF_EVEN);
                    s1.push(ansb);
                }
                return;
            }
        }catch (EmptyStackException e){
            was_error = true;
            throw new MyEmptyStackException();
        }
        BigDecimal b = s1.peek();
        s1.pop();
        BigDecimal a = s1.peek();
        BigDecimal ans = s1.peek();
        s1.pop();
        double a1, b1, ansd = 0.0;
        a1 = a.doubleValue();
        b1 = b.doubleValue();
        try{
            switch (x) {
                case "+":
                    ansd = a1 + b1;
                    break;
                case "-":
                    ansd = a1 - b1;
                    break;
                case "*":
                    ansd = a1 * b1;
                    break;
                case "/":
                    if(b1 == 0){
                        was_error = true;
                        return;
                    }
                    ansd = a1 / b1;
                    break;
                case "^":
                    ansd = Math.pow(a1, b1);
                    break;
            }
            ans = new BigDecimal(BigDecimal.valueOf(ansd).toPlainString());
            if(!ans.equals(BigDecimal.valueOf(0.0)) && !(ans.toString().contains("E")))
                ans = ans.divide(BigDecimal.valueOf(1.0), 9, RoundingMode.HALF_EVEN);

            String answer = ans.toPlainString();
            int len = answer.length();
            if(answer.charAt(len-1) == '0'){
                while (len > 0 && answer.charAt(len - 1) == '0') {
                    len--;
                    answer = answer.substring(0, len);
                }
                if(answer.charAt(len-1) == '.'){
                    answer = answer.substring(0, len-1);
                }
                ans = new BigDecimal(answer);
            }
            s1.push(ans);
        }catch (ArithmeticException e){
            //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            String str = e.toString();
            if(str.contains("Non-terminating decimal expansion; no exact representable decimal result")){
                ans = a.divide(b, 3, RoundingMode.HALF_EVEN);
                s1.push(ans);
            }else{
                was_error = true;
            }
        }catch(Exception e){
            was_error = true;
            hide_ans();
        }

    }

    protected boolean islet(char c){
        return c >= 'a' && c <= 'z';
    }

    protected void in_s0(char x){
        Map<String, Integer> priority = new HashMap<>();
        priority.put("(", 0);
        priority.put("-", 1);
        priority.put("+", 1);
        priority.put("/", 2);
        priority.put("*", 2);
        priority.put("^", 3);
        priority.put("R", 3);
        //Toast.makeText(getApplicationContext(), priority.get("(") + " " + priority.get("-"), Toast.LENGTH_SHORT).show();
        if(x == '('){
            s0.push(Character.toString(x));
            return;
        }
        if(s0.empty()){
            s0.push(Character.toString(x));
            return;
        }

        if(!s0.empty() && s0.peek().equals("(")){
            s0.push(Character.toString(x));
            return;
        }
        try {
            if (!s0.empty() && (priority.get(Character.toString(x)) < priority.get(s0.peek()) || priority.get(Character.toString(x)).equals(priority.get(s0.peek())))) {
                mult(s0.peek());
                s0.pop();
                in_s0(x);
                return;
            }
        }catch (MyEmptyStackException e){
            e.printStackTrace();
            was_error = true;
            return;
        }
        if(!s0.empty() && priority.get(Character.toString(x)) > priority.get(s0.peek()))
            s0.push(Character.toString(x));
    }

    protected BigDecimal fact(BigDecimal x){
        /*if(x.compareTo(new BigDecimal(1)) == 0 || x.signum() == 0){
            return new BigDecimal(1);
        }
        return x.multiply(fact(x.subtract(BigDecimal.valueOf(1))));*/
        BigDecimal ans = BigDecimal.valueOf(1);
        for(BigDecimal i = BigDecimal.valueOf(1); i.compareTo(x) <= 0;){
            ans = ans.multiply(i);
            i = i.add(new BigDecimal(1));
        }
        return ans;
    }

    @SuppressLint("SetTextI18n")
    public void calc(String stri, String type, int digits, int actions){
        s0.clear();
        s1.clear();
        if(type.equals("all"))
            brackets = 0;
        if(stri.equals("P") || stri.equals("F") || stri.equals("e")){
            if(type.equals("all")){
                TextView tans = findViewById(R.id.textStr);
                if(stri.equals("P")){
                    tans.setText(Double.toString(3.14159265));
                    original = getResources().getString(R.string.pi);
                }else if(stri.equals("F")) {
                    tans.setText(Double.toString(1.618));
                    original = getResources().getString(R.string.fi);
                }
                else {
                    tans.setText(Double.toString(Math.E));
                    original = stri;
                }

                tans = findViewById(R.id.textAns2);
                show_ans();
                tans.setText(original);
                HorizontalScrollView scrollview = findViewById(R.id.scrollview);

                HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
                scrollviewans.setVisibility(HorizontalScrollView.VISIBLE);
                scroll_to_end();
                return;
            }else if(type.equals("not")){
                TextView preans = findViewById(R.id.textAns2);
                show_ans();
                if(stri.equals("P")){
                    preans.setText(Double.toString(3.14159265));
                }else if(stri.equals("F"))
                    preans.setText(Double.toString(1.618));
                else preans.setText(Double.toString(Math.E));
                //preans.setText(s1.peek().toString());
                HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
                scrollviewans.setVisibility(HorizontalScrollView.VISIBLE);
                scroll_to_end();
                return;
            }
        }
        if(!stri.contains("R") && !stri.contains("P") && !stri.contains("F") && !stri.contains("e")) {
            if (actions == 0) {
                return;
            } else {
                if(digits == 0 && !stri.contains("cos") && !stri.contains("sin") && !stri.contains("log") && !stri.contains("ln") && !stri.contains("tan"))
                    return;
                else if (!stri.contains("%") && actions == 1 && (digits == 1 || digits == 0) && !stri.contains("R") && !stri.contains("!")) {
                    return;
                }
            }
        }
        char[] str = new char[stri.length()];
        stri.getChars(0, stri.length(), str, 0);
        String x;
        String s;
        int len = stri.length();
        if(digits == 0)
            return;

        for(int i = 0; i < len; i++){
            s = Character.toString(str[i]);
            if(s.equals("s") || s.equals("t") || s.equals("l") || s.equals("c")){
                if(i != 0){
                    if(stri.charAt(i-1) == ')'){
                        s0.push("*");
                    }
                }
                if(i + 4 <= stri.length()){
                    String let = "";
                    while(i < stri.length() && islet(stri.charAt(i))){
                        let += Character.toString(stri.charAt(i));
                        i++;
                    }
                    if(i != stri.length() && stri.charAt(i) != '('){
                        was_error = true;
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalidstateforsin) + getResources().getString(R.string.invalidfor) + let, Toast.LENGTH_LONG).show();
                        break;
                    }else{
                        if(i == stri.length()){
                            was_error = true;
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalidstateforsin) + getResources().getString(R.string.invalidfor) + let, Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                    switch (let) {
                        case "sin":
                            s0.push("sin");
                            s0.push("(");
                            break;
                        case "cos":
                            s0.push("cos");
                            s0.push("(");
                            break;
                        case "tan":
                            s0.push("tag");
                            s0.push("(");
                            break;
                        case "log":
                            s0.push("log");
                            s0.push("(");
                            break;
                        case "ln":
                            s0.push("ln");
                            s0.push("(");
                            break;
                    }
                    continue;
                }else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalidstateforsin), Toast.LENGTH_LONG).show();
                    was_error = true;
                    break;
                    /*if(i != 0 && !isdigit(stri.charAt(i-1))){
                        if(stri.charAt(i-1) == '(' || stri.charAt(i-1) == '.'){
                            break;
                        }else{
                            s0.pop();
                        }
                    }*/
                }
            }
            if(s.equals("P")){
                BigDecimal f = new BigDecimal(3.14159265);
                s1.push(f);
                if(i != 0 && isdigit(stri.charAt(i-1))){
                    in_s0('*');
                }
                char next = '\0';
                if(i != stri.length() - 1)
                    next = stri.charAt(i+1);
                if(i != stri.length()-1 && (isdigit(stri.charAt(i+1))  || next == 'F' || next == 'P' || next == 'e')){
                    in_s0('*');
                }
                //s1.push(f);
                continue;
            }else if(s.equals("F")){
                BigDecimal f = new BigDecimal(1.618);
                s1.push(f);
                if(i != 0 && isdigit(stri.charAt(i-1))){
                    in_s0('*');
                }
                char next = '\0';
                if(i != stri.length() - 1)
                    next = stri.charAt(i+1);
                if(i != stri.length()-1 && (isdigit(stri.charAt(i+1))  || next == 'F' || next == 'P' || next == 'e')){
                    in_s0('*');
                }
                continue;
            }else if(s.equals("!")){
                if(i != len - 1 && stri.charAt(i + 1) == '!'){
                    BigDecimal y = s1.peek(), ans = BigDecimal.ONE;
                    if (y.signum() < 0 || y.compareTo(BigDecimal.valueOf(500)) > 0){
                        was_error = true;
                        break;
                    }
                    for(; y.compareTo(BigDecimal.valueOf(0)) > 0; y = y.subtract( BigDecimal.valueOf(2) ) ){
                        ans = ans.multiply(y);
                    }
                    i++;
                    s1.pop();
                    s1.push(ans);
                    continue;
                }else {
                    BigDecimal y = s1.peek();
                    if (y.signum() == -1) {
                        was_error = true;
                        Toast.makeText(getApplicationContext(), "Error: Unable to find negative factorial.", Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        if (y.compareTo(new BigDecimal(1000)) > 0) {
                            was_error = true;
                            Toast.makeText(getApplicationContext(), "For some reason, we cannot calculate the factorial of this number " +
                                    "(because it is too large and may not have enough device resources when executed)", Toast.LENGTH_LONG).show();
                            break;
                        } else {
                            s1.pop();
                            String fa = y.toString();
                            if (fa.contains(".")) {
                                int index = fa.lastIndexOf(".");
                                fa = fa.substring(0, index);
                                y = new BigDecimal(fa);
                            }
                            s1.push(fact(y));
                        }
                    }
                    continue;
                }
            }else if(s.equals("%")){
                BigDecimal y = s1.peek();
                s1.pop();
                s1.push(y.divide(new BigDecimal(100)));
                continue;
            }else if(s.equals("e")){
                BigDecimal f = new BigDecimal(Math.E);
                s1.push(f);
                if(i != 0 && isdigit(stri.charAt(i-1))){
                    in_s0('*');
                }
                if(i != stri.length()-1 && isdigit(stri.charAt(i+1))){
                    in_s0('*');
                }
                continue;
            }else if(s.equals("R")){
                if(i == len-1){
                    was_error = true;
                    break;
                }else{
                    if(stri.charAt(i + 1) == '('){
                        in_s0('R');
                        continue;
                    }else{
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.invalidstateforsin) + " " + getResources().getString(R.string.invalidfor)
                                        + " √", Toast.LENGTH_LONG).show();
                        was_error = true;
                        break;
                    }
                }
            }
            if(isdigit(str[i])){
                x = "";
                while((i < stri.length()) && ((stri.charAt(i) == '.') || isdigit(str[i]) || (stri.charAt(i) == '-' && stri.charAt(i-1) == 'E'))){
                    s = Character.toString(str[i]);
                    x += s;
                    i++;
                }
                /*if(x.contains("E"))
                    x = calc_e(x);*/
                s1.push(new BigDecimal(x));
                if(i < stri.length() && str[i] == 'E'){
                    in_s0('^');
                    i++;
                    BigDecimal t = BigDecimal.ONE;
                    if(str[i] == '+'){
                        t = BigDecimal.ONE;
                    }else if(str[i] == '-'){
                        t = BigDecimal.valueOf(-1.0);
                    }
                    x = "";
                    while(i < stri.length() && (stri.charAt(i) == '.' || isdigit(stri.charAt(i)))){
                        x += Character.toString(stri.charAt(i));
                        i++;
                    }
                    s1.push(new BigDecimal(x).multiply(t));
                }
                i--;
            }else{
                if(str[i] != ')'){
                    if(str[i] == '^'){
                        if(i != stri.length()-1 && str[i + 1] == '('){
                            i += 2;
                            in_s0('^');
                            s0.push("(");
                            x = "";
                            double cf = 1.0;
                            while(i < stri.length() && str[i] != ')'){
                                x = "";
                                if(str[i] == '('){
                                    i++;
                                    continue;
                                }else if(str[i] == '^'){
                                    i += 2;
                                }

                                if(str[i] == '-' && str[i-1] == '('){
                                    cf = -1.0;
                                    i++;
                                    //continue;
                                }else{
                                    cf = 1.0;
                                    //i++;
                                }

                                while((i < stri.length()) && ((stri.charAt(i) == '.') || isdigit(str[i]) || stri.charAt(i) == 'E' || (stri.charAt(i) == '-' && stri.charAt(i-1) == 'E'))){
                                    s = Character.toString(str[i]);
                                    x += s;
                                    i++;
                                }
                                s1.push(new BigDecimal(x).multiply(BigDecimal.valueOf(cf)));
                                /*if(!isdigit(str[i]) && str[i] != ')')
                                    in_s0(str[i]);*/
                                break;
                            }
                            i--;
                            continue;
                        }else if(i != stri.length()-1 && str[i + 1] != '('){
                            //i++;
                            in_s0('^');
                            x = "";
                            continue;
                        }
                    }
                    if((i == 0 && str[i] == '-') || (str[i] == '-' && stri.charAt(i-1) == '(')){
                        x = "";
                        i++;
                        while((i < stri.length()) && ((stri.charAt(i) == '.') || isdigit(str[i]) || stri.charAt(i) == 'E' || (stri.charAt(i) == '-' && stri.charAt(i-1) == 'E'))){
                            s = Character.toString(str[i]);
                            x += s;
                            i++;
                        }
                        i--;
                        s1.push(new BigDecimal(x).multiply(BigDecimal.valueOf(-1)));
                        continue;
                    }
                    if(i != 0 && str[i] == '(' && (isdigit(str[i-1]) || str[i-1] == ')')){
                        in_s0('*');
                    }

                    in_s0(str[i]);
                }else{
                    try {
                        while (!s0.empty() && !s0.peek().equals("(")) {
                            mult(s0.peek());
                            s0.pop();
                        }
                        if (!s0.empty() && s0.peek().equals("(")) {
                            s0.pop();
                        }
                        if (i != stri.length() - 1) {
                            if (isdigit(stri.charAt(i + 1))) {
                                in_s0('*');
                            }
                        }
                    }catch (MyEmptyStackException e){
                        was_error = true;
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }
        try {
            while (!s0.isEmpty() && s1.size() >= 2) {
                mult(s0.peek());
                s0.pop();
            }
            if (!s0.isEmpty() && s1.size() == 1) {
                if (s0.peek().equals("R")) {
                    mult(s0.peek());
                    s0.pop();
                }
                if (!s0.isEmpty() && (s0.peek().equals("cos") || s0.peek().equals("sin") || s0.peek().equals("log") || s0.peek().equals("ln") || s0.peek().equals("tan"))) {
                    mult(s0.peek());
                    s0.pop();
                }
            }
        }catch (MyEmptyStackException e){
            e.printStackTrace();
            was_error = true;
        }
        if(!was_error){
            switch (type) {
                case "all":
                    TextView tans = findViewById(R.id.textStr);
                    tans.setText(s1.peek().toPlainString());
                    format(R.id.textStr);
                    tans = findViewById(R.id.textAns2);
                    tans.setText(original);
                    show_ans();
                    scroll_to_end();

                    String his = sp.getString("history", "");
                    StringBuilder sb = new StringBuilder(original);
                    for (int i = 0; i < sb.length(); i++) {
                        if (sb.charAt(i) == ' ') {
                            sb.deleteCharAt(i);
                        }
                    }
                    String for_his = sb.toString();
                    if (his.indexOf(for_his + "," + s1.peek().toPlainString() + ";") != 0) {
                        his = original + "," + s1.peek().toString() + ";" + his;
                        sp.edit().putString("history", his).apply();
                    }
                    if (sp.getBoolean("saveResult", false))
                        sp.edit().putString("saveResultText", original + ";" + s1.peek().toPlainString()).apply();
                    break;
                case "not":
                    TextView preans = findViewById(R.id.textAns2);
                    preans.setText(s1.peek().toPlainString());
                    format(R.id.textAns2);
                    show_ans();
                    set_text_toDef();
                    resize_text();
                    scroll_to_end();
                    break;
                case "for_memory":
                    result_calc_for_mem = s1.peek();
                    memory_calculated = true;
                    break;
            }
        }else{
            hide_ans();
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
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return;*/
        TextView txt = findViewById(R.id.textStr);
        TextView t = findViewById(R.id.textAns2);

        String stri = txt.getText().toString(), strians = t.getText().toString();
        Rect bounds = new Rect();
        Rect boundsans = new Rect();
        Paint textPaint = txt.getPaint();
        Paint PaintAns = t.getPaint();
        textPaint.getTextBounds(stri, 0, stri.length(), bounds);
        PaintAns.getTextBounds(strians, 0, strians.length(), boundsans);
        int height = bounds.height();
        int twidth = bounds.width();
        int widthAns = bounds.width();
        Display dis = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        dis.getSize(size);
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 46, getResources().getDisplayMetrics());
        int width = size.x - px - 45, answidth = size.x - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 54, getResources().getDisplayMetrics());
        while(twidth >= width && txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity > 32){
            txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity - 1);
            if(boundsans.width() >= answidth)
                t.setTextSize(TypedValue.COMPLEX_UNIT_SP, t.getTextSize() / getResources().getDisplayMetrics().scaledDensity - 1);
            //twidth = txt.getWidth();
            textPaint.getTextBounds(stri, 0, stri.length(), bounds);
            PaintAns.getTextBounds(strians, 0, strians.length(), boundsans);
            twidth = bounds.width();
            //txt.setWidth(twidth + 10);
            //sz = txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
        }
        while(bounds.width() >= answidth && t.getTextSize() / getResources().getDisplayMetrics().scaledDensity > 29){
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, t.getTextSize() / getResources().getDisplayMetrics().scaledDensity - 1);
            PaintAns.getTextBounds(strians, 0, strians.length(), boundsans);
        }
        if (twidth < width && txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity < 46){
            txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity + 1);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, t.getTextSize() / getResources().getDisplayMetrics().scaledDensity + 1);
            //twidth = txt.getWidth();
            textPaint.getTextBounds(stri, 0, stri.length(), bounds);
            twidth = bounds.width();
        }
        //LinearLayout ll = findViewById(R.id.textAns2);
    }

    public void set_text_toDef(){
        TextView txt = findViewById(R.id.textStr);
        TextView t = findViewById(R.id.textAns2);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 46);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
    }

    public void equallu(String type){
        TextView txt = findViewById(R.id.textStr);
        String stri = txt.getText().toString();
        format(R.id.textStr);
        int len = stri.length();
        if(len != 0) {
            show_str();
            scroll_to_end();
        }
        if(type.equals("for_memory") && len == 0)
            return;
        if(len != 0 && stri.charAt(len-1) == '^' && type.equals("all")){
            stri += "(";
            brackets++;
            txt.setText(stri);
            return;
        }
        //int twidth = txt.getWidth();
        float sz = txt.getTextSize();
        if(len != 0 && !isdigit(stri.charAt(len-1)) && stri.charAt(len-1) != ')' && stri.charAt(len-1) != '!' && stri.charAt(len-1) != '%')
            if(!stri.contains(getResources().getString(R.string.pi)) && !stri.contains(getResources().getString(R.string.fi)) && !stri.contains("e") && !stri.contains(getResources().getString(R.string.sqrt)))
                return;
        //txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        resize_text();
        //Toast.makeText(getApplicationContext(), Float.toString(txt.getTextSize()), Toast.LENGTH_LONG).show();
        brackets = 0;
        for(int i = 0; i < stri.length(); i++){
            if(stri.charAt(i) == '(')
                brackets++;
            else if(stri.charAt(i) == ')')
                brackets--;
        }
        if(brackets < 0)
            return;

        //if(type.equals("all")){
        if(!stri.equals("")){
            was_error = false;
            if(stri.charAt(stri.length() - 1) != '('){
                if(brackets > 0){
                    for(int i = 0; i < brackets; i++){
                        stri += ")";
                    }
                    brackets = 0;
                }
            }
            original = stri;
            int digits = 0, actions = 0;
            for(int i = 0; i < stri.length(); i++){
                if(isdigit(stri.charAt(i))
                        || Character.toString(stri.charAt(i)).equals(getResources().getString(R.string.fi))
                        || Character.toString(stri.charAt(i)).equals(getResources().getString(R.string.pi))
                        || Character.toString(stri.charAt(i)).equals("e")){
                    digits++;
                }
                if(isaction(stri.charAt(i)))
                    actions++;

                if(stri.charAt(i) == ' '){
                    StringBuilder stringBuilder = new StringBuilder(stri);
                    stringBuilder.deleteCharAt(i);
                    stri = new String(stringBuilder);
                }
            }
            if(stri.contains(getResources().getString(R.string.multiply)) || stri.contains(getResources().getString(R.string.div))
                    || stri.contains(getResources().getString(R.string.pi)) || stri.contains(getResources().getString(R.string.fi))
                    || stri.contains(getResources().getString(R.string.sqrt))){
                char[] mas = stri.toCharArray();
                String p = "";

                for(int i = 0; i < stri.length(); i++){
                    p = Character.toString(mas[i]);
                    if(p.equals(getResources().getString(R.string.div))){
                        mas[i] = '/';
                    }else if(p.equals(getResources().getString(R.string.multiply))){
                        mas[i] = '*';
                    }else if(p.equals(getResources().getString(R.string.pi))){
                        mas[i] = 'P';
                    }else if(p.equals(getResources().getString(R.string.fi))){
                        mas[i] = 'F';
                    }else if(p.equals(getResources().getString(R.string.sqrt))){
                        mas[i] = 'R';
                    }
                }
                stri = new String(mas);
            }
            if(actions == 0 && type.equals("for_memory")){
                result_calc_for_mem = new BigDecimal(stri);
            }
            calc(stri, type, digits, actions);
        }
    }

    protected void check_dot(){
        TextView t = findViewById(R.id.textStr);
        String txt = t.getText().toString();
        int i = txt.length()-1;
        if(!isdigit(txt.charAt(i))){
            return;
        }
        boolean dot = false;
        while(i >= 0 && (isdigit(txt.charAt(i)) || txt.charAt(i) == '.')){
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
        if(btntxt.equals("φ") || btntxt.equals("π") || btntxt.equals("e")){
            if(len == 0){
                t.setText(btntxt);
                equallu("not");
                return;
            }else{
                if(!isdigit(txt.charAt(len-1))){
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

            if(isdigit(last) || Character.toString(last).equals(FI) || Character.toString(last).equals(PI) || last == 'e'){
                t.setText(txt + btntxt + "(");
                equallu("not");
                return;
            }
            if(!isdigit(last)){
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
                    if(!isdigit(txt.charAt(i)) && txt.charAt(i) != '.'){
                        break;
                    }else{
                        if(isdigit(txt.charAt(i)) || txt.charAt(i) != '.')
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
                        if(!isdigit(last)){
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
                    if(!isdigit(last) && last != '!' && !Character.toString(last).equals(FI) && !Character.toString(last).equals(PI) && last != 'e'){
                        t.setText(txt + btntxt + "(");
                        equallu("not");
                    }else {
                        if (last != '(' && last != '^') {
                            t.setText(txt + getResources().getString(R.string.multiply) + btntxt + "(");
                            equallu("not");
                        }
                    }
                    /*if(!isdigit(last)){

                        t.setText(txt + btntxt + "(");
                        brackets++;
                        //Toast.makeText(getApplicationContext(), Integer.toString(brackets), Toast.LENGTH_SHORT).show();
                        equallu("not");
                    }else{
                        t.setText(txt + getResources().getString(R.string.multiply) + btntxt + "(");
                        equallu("not");
                    }*/
                }
            }
            return;
        }

        if(len != 0 && txt.charAt(len-1) == '(' && btntxt.equals("-")){
            t.setText(txt + btntxt);
            equallu("not");
            return;
        }

        if(btntxt.equals(")")){
            //Toast.makeText(getApplicationContext(), Integer.toString(brackets), Toast.LENGTH_SHORT).show();
            if(brackets > 0){
                if(txt.charAt(len-1) == ')'){
                    t.setText(txt + btntxt);
                    equallu("not");
                    return;
                }
                if(txt.charAt(len-1) == '(')
                    return;
                if(!isdigit(last) && !Character.toString(last).equals(getResources().getString(R.string.pi))
                        && !Character.toString(last).equals(getResources().getString(R.string.fi)) && last != 'e' && last != '!'){
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

        if(!isdigit(btntxt) && !btntxt.equals("(") && !btntxt.equals(")") && !islet(btntxt.charAt(0))){
            if(len != 0){
                if(txt.charAt(len-1) == 'π' || txt.charAt(len-1) == 'φ' || txt.charAt(len-1) == 'e'){
                    t.setText(txt + btntxt);
                    equallu("not");
                    return;
                }
            }
        }

        if(last == '!' && !btntxt.equals(".") && !btntxt.equals("!")){
            t.setText(txt + btntxt);
            equallu("not");
            return;
        }

        if(btntxt.equals(".")){
            if(txt.equals(""))
                t.setText("0.");
            else
            if(!isdigit(txt.charAt(len-1)) && txt.charAt(len-1) != '.' && last != '!')
                t.setText(txt + "0.");
            else
                check_dot();
        }else{
            if(btntxt.equals("!") || btntxt.equals("%")){
                if(!txt.equals("")){
                    if( last != ')' && !isdigit(last) && (len > 1 && last != '(' && islet(txt.charAt(len - 2))
                            && !Character.toString(txt.charAt(len - 2)).equals(PI) && !Character.toString(txt.charAt(len-2)).equals(FI) && txt.charAt(len - 2) != 'e') ){
                        if(btntxt.equals("!")){
                            if(last == '!'){
                                if(txt.charAt(len - 2) != '!'){
                                    t.setText(txt + btntxt);
                                    equallu("not");
                                    return;
                                }
                            }
                        }
                        txt = txt.substring(0, len-1);
                        t.setText(txt + btntxt);
                        equallu("not");
                    }else{
                        if(isdigit(last) || last == ')'){
                            t.setText(txt + btntxt);
                            equallu("not");
                        }
                    }
                }
                return;
            }
            if(len > 1 && (btntxt.equals("(") || (txt.charAt(len-1) == ')' && !btntxt.equals(".") && !isdigit(btntxt.toCharArray()[0])))) {
                if(btntxt.equals("("))
                    brackets++;
                t.setText(txt + btntxt);
                equallu("not");
            }else if(len == 0 && btntxt.equals("(")){
                brackets++;
                t.setText(btntxt);
                equallu("not");
            }else{
                if(!txt.equals("")){
                    if(!isdigit(btntxt.charAt(0)) && !btntxt.equals(".")){

                        if(!isdigit(txt.charAt(len-1))){
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
                        if(isdigit(btntxt.charAt(0))){
                            t.setText(txt + btntxt);
                            equallu("not");
                        }
                    }
                }else{
                    if(isdigit(btntxt.charAt(0)) || (btntxt.equals("-"))){
                        t.setText(btntxt);
                        equallu("not");
                    }
                }
            }
        }

    }

    public void onClick(@NonNull View v){
        btn = findViewById(v.getId());
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
            if(last == '(' && len > 1){
                if(text.charAt(text.length() - 2) == '√' || text.charAt(len - 2) == '^')
                    a = 2;
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