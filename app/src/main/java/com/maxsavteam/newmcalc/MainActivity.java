package com.maxsavteam.newmcalc;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

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
    public String newVer = "\b";
    public AlertDialog dl, al;
    public String newDevVer = "";
    public boolean add_menu_opened = false;
    public AlertDialog not_btn_pr = null;
    public String onshow = "", onhide = "";
    public update_service ups;
    public String uptype = "simple";
    public int newCodeDev = 0;
    public Integer versionSt = 0;

    View.OnLongClickListener fordel = (View v) -> {
        TextView t = findViewById(R.id.textStr);
        t.setText("");
        TextView ans = findViewById(R.id.textAns2);
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
            String txt = back.getText().toString();
            back.setText("");
            back = findViewById(R.id.textStr);
            back.setText(txt);
            HorizontalScrollView scrollview = findViewById(R.id.scrollview);
            scrollview.post(new Runnable() {
                @Override
                public void run() {
                    scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            });
            HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
            scrollviewans.post(new Runnable() {
                @Override
                public void run() {
                    scrollviewans.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            });
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
                            overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
					    }
				    });

    		AlertDialog dal = dialogal.create();
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
						    overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
					    }
				    });
		    AlertDialog al = builder.create();
		    al.show();
	    }
        //super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.about) {
            dl.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    protected void sh_dl_update(int versionMy, int versionNew, String newver, String text, boolean compileWithView){
        View mView = getLayoutInflater().inflate(R.layout.alert_checkbox, null);
        CheckBox mcheck = mView.findViewById(R.id.checkBoxAlert);
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
                        dialog.cancel();
                        if(mcheck.isChecked()){
                            sp.edit().putInt("notremindfor", versionNew).apply();
                        }
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        File file = new File(Environment.getExternalStorageDirectory() + "/" + "MST files/NewMCalc " + newver + ".apk");
                        /*Intent in = new Intent(MainActivity.this, Updater.class);
                        in.putExtra("action", "update");
                        in.putExtra("upVerName", newDevVer);*/
                        if(uptype.equals("dev")){
                            /*in.putExtra("upVerName", newDevVer);
                            in.putExtra("update_path", "/forTesters/NewMCalc.apk");*/
                            ups.run("/forTesters/NewMCalc.apk", newDevVer);
                        }
                        else{
                            /*in.putExtra("upVerName", newver);
                            in.putExtra("update_path", "/NewMCalc.apk");*/
                            ups.run("/NewMCalc.apk", newver);
                        }
                    }
                });
        if(compileWithView)
            builder.setView(mView);
        al = builder.create();
    }


    protected void check_up(Integer versionMy, Integer versionNew){
        if(!ups.isup()){
            if(versionNew < newCodeDev && versionMy < newCodeDev){
                sh_dl_update(versionMy, newCodeDev, newDevVer, getResources().getString(R.string.dev_update), false);
                uptype = "dev";
                al.show();
                dl.cancel();
            }else if(versionNew > versionMy && versionNew >= newCodeDev){
                if(sp.getInt("notremindfor", 0) != versionNew) {
                    uptype = "simple";
                    sh_dl_update(versionMy, versionNew, newVer, getResources().getString(R.string.updateavailable), true);
                    al.show();
                    dl.cancel();
                }
            }
        }
    }

    public void onClickAdd(View v){
        Intent resultIntent = new Intent();
        Button sh = findViewById(R.id.btnShAdd);
        ConstraintLayout cl = findViewById(R.id.constraight);

        findViewById(R.id.imgBtnSettings).setBackground(cl.getBackground());
        findViewById(R.id.btnImgNumGen).setBackground(cl.getBackground());
        if(v.getId() == R.id.imgBtnSettings){
            goto_add_scr("settings");
        }else if(v.getId() == R.id.btnImgNumGen){
            goto_add_scr("numgen");
        }else if(v.getId() == R.id.btnImgHistory){
            goto_add_scr("history");
        }else if(v.getId() == R.id.btnImgPassgen){
            goto_add_scr("pass");
        }
        if(sp.getInt("btn_add_align", 0) == 0){
            show_hide(findViewById(R.id.btnShAdd));
        }else if(sp.getInt("btn_add_align", 0) == 1){
            show_hide(findViewById(R.id.btnShAdd2));
        }
    }

    protected void goto_add_scr(String where){
        Intent resultIntent = new Intent();
        if(where.equals("settings")){
            resultIntent = new Intent(getApplicationContext(), Updater.class);
            resultIntent.putExtra("action", "simple");
            if(uptype.equals("simple"))
                resultIntent.putExtra("update_path", "/NewMCalc.apk");
            else
                resultIntent.putExtra("update_path", "/forTesters/NewMCalc.apk");
            startActivity(resultIntent);
            overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
        }else if(where.equals("numgen")){
            resultIntent = new Intent(getApplicationContext(), numgen.class);
            resultIntent.putExtra("type", "number");
            resultIntent.putExtra("start_type", "app");
            startActivity(resultIntent);
            overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
        }else if(where.equals("history")){
            sp.edit().putString("action", "history").apply();
            resultIntent = new Intent(getApplicationContext(), history.class);
            resultIntent.putExtra("start_type", "app");
            startActivity(resultIntent);
            overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
        }else if(where.equals("pass")){
            resultIntent = new Intent(getApplicationContext(), numgen.class);
            resultIntent.putExtra("type", "pass");
            resultIntent.putExtra("start_type", "app");
            startActivity(resultIntent);
            overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
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

    protected void btn_change(){
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
    }



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btnDelete);
        btn.setOnLongClickListener(fordel);
        TextView ver = findViewById(R.id.lblVer);
        ver.setText(getResources().getString(R.string.version) + " " + BuildConfig.VERSION_NAME + "\n" + getResources().getString(R.string.build) + " " + BuildConfig.VERSION_CODE);// + "\n" + "CompileName: " + BuildConfig.COMPILENAME);
        Button btn1 = findViewById(R.id.btnCalc);
        btn1.setOnLongClickListener(returnback);
        Button btn2 = findViewById(R.id.btnZero);
        //FirebaseApp.initializeApp(this);
        ups = new update_service(MainActivity.this);
	    sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try{
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
		Intent start_in = getIntent();
        RelativeLayout.LayoutParams par;
        add_menu_opened = false;
        par = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()));
        LinearLayout ll = findViewById(R.id.additional_tools);
        ll.setLayoutParams(par);
		if(start_in.getBooleanExtra("shortcut_action", false)){
            goto_add_scr(start_in.getStringExtra("to_"));
            /*if(sp.getInt("btn_add_align", 0) == 0){
                show_hide(findViewById(R.id.btnShAdd));
            }else{
                show_hide(findViewById(R.id.btnShAdd2));
            }*/
			//show_hide(findViewById(R.id.btnShAdd));
		}
        BroadcastReceiver on_btn_align_change = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                btn_change();
            }
        };
        registerReceiver(on_btn_align_change, new IntentFilter(BuildConfig.APPLICATION_ID + ".ON_BTN_ALIGN_CHANGE"));
        update_service ups = new update_service(this);
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(not_btn_pr != null)
                    not_btn_pr.cancel();
                if(!isOtherActivityOpened){
                    AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                    b.setCancelable(false)
                            .setTitle(R.string.installation)
                            .setMessage(R.string.update_avail_to_install)
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ups.install();
                        }
                    });
                    AlertDialog inst = b.create();
                    inst.show();
                }

            }
        };
        BroadcastReceiver on_his_action = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                t = findViewById(R.id.textStr);
                if(!sp.getString("history_action", "").equals("")){
                    String txt = t.getText().toString();
                    if(!txt.equals("") && txt.contains(".")){
                        if(islet(txt.charAt(txt.length() - 1)))
                            return;
                        boolean was_action = false;
                        for(int i = txt.length() - 1; i >= 0; i++){
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
                    txt = txt  + sp.getString("history_action", "");
                    t.setText(txt);
                    sp.edit().remove("action").apply();
                    sp.edit().remove("history_action").apply();
                    equallu("not");
                }
                sp.edit().remove("action").apply();
                sp.edit().remove("history_action").apply();
            }
        };
        registerReceiver(on_his_action, new IntentFilter(BuildConfig.APPLICATION_ID + ".HISTORY_ACTION"));
        BroadcastReceiver on_choose_action = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sp.edit().remove("toChoose").remove("btnHeight").remove("btnWidth").apply();
                if(!intent.getStringExtra("value").equals("")){
                    t = findViewById(R.id.textStr);
                    String txt = t.getText().toString();
                    if(intent.getStringExtra("value").equals("1/X")){
                        if(txt.equals(""))
                            return;
                        String newt = "";
                        int i = txt.length()-1;
                        while(i >= 0 && (isdigit(txt.charAt(i)) || txt.charAt(i) == '.')){
                            newt = txt.charAt(i) + newt;
                            i--;
                        }

                        s1.push(BigDecimal.valueOf(1));
                        s1.push(new BigDecimal(newt));
                        mult("/");
                        if(!was_error){
                            BigDecimal ans = s1.peek();
                            s1.pop();
                            if(txt.length() == newt.length()){
                                t.setText(ans.toString());
                            }else{
                                txt = txt.substring(0, txt.length() - newt.length());
                                txt += ans.toString();
                                t.setText(txt);
                            }
                            equallu("not");
                        }
                    }else
                        add_text(intent.getStringExtra("value"));
                }
            }
        };
        registerReceiver(on_choose_action, new IntentFilter(BuildConfig.APPLICATION_ID + ".ON_CHOOSE_ACTION"));
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
        BroadcastReceiver brfail = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(not_btn_pr != null)
                    not_btn_pr.cancel();
                if(!isOtherActivityOpened){
                    AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                    b.setTitle(":(").setMessage(R.string.cannot_update).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog inst = b.create();
                    inst.show();
                }
            }
        };
        BroadcastReceiver on_sp_edited = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
		        btn_change();
                Intent resultIntent = new Intent(getApplicationContext(), Updater.class);
                resultIntent.putExtra("action", "simple");
                if(uptype.equals("simple"))
                    resultIntent.putExtra("update_path", "/NewMCalc.apk");
                else
                    resultIntent.putExtra("update_path", "/forTesters/NewMCalc.apk");
                startActivity(resultIntent);
                overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
	        }
        };
        registerReceiver(br, new IntentFilter(BuildConfig.APPLICATION_ID + ".NEWMCALC_UPDATE_SUC"));
        registerReceiver(brfail, new IntentFilter(BuildConfig.APPLICATION_ID + ".NEWMCALC_UPDATE_FAIL"));
        registerReceiver(on_sp_edited, new IntentFilter(BuildConfig.APPLICATION_ID + ".SP_EDITED"));
        /*BroadcastReceiver on_lang = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                set_lang();
            }
        };
        registerReceiver(on_lang, new IntentFilter(BuildConfig.APPLICATION_ID + ".LANG_CH"));*/

        BroadcastReceiver btn_not = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                not_btn_pr = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_stop_update)
                        .setCancelable(false).setNegativeButton(R.string.stop, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                if(ups.isup())
                                    ups.kill();
                            }
                        }).setPositiveButton(R.string.hide, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create();
                not_btn_pr.show();
            }
        };
        registerReceiver(btn_not, new IntentFilter(BuildConfig.APPLICATION_ID + ".NOT_BTN_PRESSED"));
        btn1.setWidth(btn2.getWidth());
        btn1.setHeight(btn2.getHeight());
        btn.setHeight(btn2.getHeight());
        btn.setWidth(btn2.getWidth());
        btn1 = findViewById(R.id.btnDelAll);
        btn1.setHeight(btn2.getHeight());
        btn1.setWidth(btn2.getWidth());

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.about_layout, null);
        builder.setCancelable(false)
                .setTitle(R.string.about)
                .setMessage(getResources().getString(R.string.about_text)
                        + "\n\n" + getResources().getString(R.string.version) + BuildConfig.VERSION_NAME
                        + "\n" + getResources().getString(R.string.build) + BuildConfig.VERSION_CODE
                        + "\nCompile date: " + BuildConfig.COMPILE_DATE + "\n\n" + "©" + "MaxSav Team, 2018-2019")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setView(view);

        dl = builder.create();

        btn_change();

        BroadcastReceiver on_dev_up_avail = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
		        if(!isOtherActivityOpened){
		        	uptype = "dev";
		        	sh_dl_update(BuildConfig.VERSION_CODE, intent.getIntExtra("newCodeDev", 0), intent.getStringExtra("newVerDev"),  getResources().getString(R.string.dev_update), false);
		        }
	        }
        };
        registerReceiver(on_dev_up_avail, new IntentFilter(BuildConfig.APPLICATION_ID + ".UPDATE_AVIAL_DEV"));
        BroadcastReceiver on_simple_up = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
				if(!isOtherActivityOpened){
					uptype = "simple";
					sh_dl_update(BuildConfig.VERSION_CODE, intent.getIntExtra("newCode", 0), intent.getStringExtra("newVer"),  getResources().getString(R.string.updateavailable), true);
				}
	        }
        };
        registerReceiver(on_simple_up, new IntentFilter(BuildConfig.APPLICATION_ID + ".UPDATE_AVAIL"));

        FirebaseAnalytics fr = FirebaseAnalytics.getInstance(getApplicationContext());
        fr.logEvent("OnCreate", Bundle.EMPTY);
    }

    protected void set_lang(){
        String language_code = sp.getString("lang", "def");
        if(!language_code.equals("def")){
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.setLocale(new Locale(language_code.toLowerCase())); // API 17+ only.
            res.updateConfiguration(conf, dm);
            setContentView(R.layout.activity_main);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //new newver_check_service(this).create(sp.getBoolean("isdev", false));

        if(BuildConfig.APPTYPE.equals("tester")){
            sp.edit().putBoolean("isdev", true).apply();
        }

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
                ver.setText(ex.toString());
                i++;
                while(i < text.length() && text.charAt(i) != ';'){
                    ans.append(text.charAt(i));
                    i++;
                }
                ver = findViewById(R.id.textStr);
                ver.setText(ans.toString());
                //ver.setSelected(false);
                equallu("not");
                ver = null;
                System.gc();
            }
        }

        String vercode = Integer.toString(BuildConfig.VERSION_CODE);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference refVer = db.getReference("version");
        refVer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newVer = Objects.requireNonNull(dataSnapshot.getValue()).toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                newVer = "\b";
                Log.e("FirebaseDB", "Cancelled: " + databaseError.toString());
            }
        });

        DatabaseReference ref = db.getReference("versionCode");

        DatabaseReference refdev = db.getReference("dev/versionCodeDev");
        DatabaseReference refdevver = db.getReference("dev/versionDev");

        if(sp.getBoolean("isdev", false)){
            refdevver.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    newDevVer = dataSnapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        try {
            Thread.sleep(1350);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                Long value = dataSnapshot.getValue(Long.class);
                Integer versionMy = Integer.valueOf(vercode);
                Integer versionNew = Integer.valueOf(String.valueOf(value));
                versionSt = versionNew;
                if (!sp.getBoolean("isdev", false))
                    check_up(versionMy, versionNew);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseDB", "Cancelled: " + databaseError.toString());
                Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_LONG).show();
            }
        });

        if(sp.getBoolean("isdev", false)){
            refdev.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.getValue().toString();
                    //int versionMy = Integer.valueOf(vercode);
                    newCodeDev = Integer.valueOf(value);
                    Integer versionMy = Integer.valueOf(vercode);
                    Integer versionNew = versionSt;
                    check_up(versionMy, versionNew);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("firebase", "cancelled");
                }
            });
        }
    }

    /*protected void run_check(){
	    new newver_check_service(this).create(sp.getBoolean("isdev", false));
    }*/

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
            if(before_e.charAt(j) == '.'){
                continue;
            }else{
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

    protected void mult(String x){
        if(x.length() == 3 || x.equals("ln") || x.equals("R")){
            double d = s1.peek().doubleValue(), ans = 1;
            if(x.equals("log") && d <= 0){
                Toast.makeText(getApplicationContext(), "You cannot find the logarithm of a zero or a negative number.", Toast.LENGTH_SHORT).show();
                return;
            }
            s1.pop();
            switch(x){
                case "cos":{
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
                }case "ln":{
                    ans = Math.log(d);
                    break;
                }case "R":
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
                        Toast.makeText(getApplicationContext(), "Error: division by zero", Toast.LENGTH_LONG).show();
                        return;
                    }
                    ansd = a1 / b1;
                    //ans = a.divide(b, 10, RoundingMode.HALF_EVEN);
                    /*String answer = ans.toString();
                    int len = answer.length();
                    while (len > 0 && answer.charAt(len - 1) == '0') {
                        len--;
                        answer = answer.substring(0, len);
                    }
                    ans = new BigDecimal(answer);*/
                    break;
                case "^":
                    ansd = Math.pow(a1, b1);
                    break;
            }
            ans = BigDecimal.valueOf(ansd);
            if(!ans.equals(BigDecimal.valueOf(0.0)))
                ans = ans.divide(BigDecimal.valueOf(1.0), 9, RoundingMode.HALF_EVEN);

            String answer = ans.toString();
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
                ans = a.divide(b, 2, RoundingMode.HALF_EVEN);
                s1.push(ans);
            }else{
                was_error = true;
                str = str.replaceAll("java.lang.ArithmeticException: ", "");
                Toast.makeText(getApplicationContext(), "Error: " + str, Toast.LENGTH_SHORT).show();
                /*TextView t = findViewById(R.id.txtAns);
                t.setText("Error: " + str);
                t.setContentDescription(t.getText());
                t.setVisibility(View.VISIBLE);*/
            }
        }

    }

    protected boolean islet(String c){
        return c.compareTo("a") >= 0 && c.compareTo("z") <= 0;
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
        if(s0.empty() && x != '('){
            s0.push(Character.toString(x));
            return;
        }

        if(s0.peek().equals("(")){
            s0.push(Character.toString(x));
            return;
        }
        if(priority.get(Character.toString(x)) < priority.get(s0.peek()) || priority.get(Character.toString(x)) == priority.get(s0.peek())){
            mult(s0.peek());
            s0.pop();
            in_s0(x);
            return;
        }
        if(priority.get(Character.toString(x)) > priority.get(s0.peek()))
            s0.push(Character.toString(x));
    }

    protected BigDecimal fact(BigDecimal x){
        if(x.compareTo(new BigDecimal(1)) == 0 || x.signum() == 0){
            return new BigDecimal(1);
        }
        return x.multiply(fact(x.subtract(BigDecimal.valueOf(1))));
    }

    @SuppressLint("SetTextI18n")
    public void calc(String stri, String type, int digits, int actions){
        s0.clear();
        s1.clear();
        if(add_menu_opened){
            if(sp.getInt("btn_add_align", 0) == 0){
                show_hide(findViewById(R.id.btnShAdd));
            }else if(sp.getInt("btn_add_align", 0) == 1){
                show_hide(findViewById(R.id.btnShAdd2));
            }
        }
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

                tans.setText(original);
                HorizontalScrollView scrollview = findViewById(R.id.scrollview);

                scrollview.post(() -> {
                    scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                });
                HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
                scrollviewans.setVisibility(HorizontalScrollView.VISIBLE);
                scrollviewans.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollviewans.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                });
                return;
            }else if(type.equals("not")){
                TextView preans = findViewById(R.id.textAns2);
                if(stri.equals("P")){
                    preans.setText(Double.toString(3.14159265));
                }else if(stri.equals("F"))
                    preans.setText(Double.toString(1.618));
                else preans.setText(Double.toString(Math.E));
                //preans.setText(s1.peek().toString());
                HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
                scrollviewans.setVisibility(HorizontalScrollView.VISIBLE);
                scrollviewans.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollviewans.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                });
                return;
            }
        }
        if(actions == 0){
            return;
        }else{
            if(!stri.contains("%") && actions == 1 && (digits == 1 || digits == 0) && !stri.contains("R") && !stri.contains("!")){
                return;
            }
        }
        char[] str = new char[stri.length()];
        stri.getChars(0, stri.length(), str, 0);
        String x;
        String s;
        int len = stri.length();

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
            if(s.equals(Character.toString('P'))){
                BigDecimal f = new BigDecimal(3.14159265);
                if(i != 0 && isdigit(stri.charAt(i-1))){
                    in_s0('*');
                }
                if(i != stri.length()-1 && isdigit(stri.charAt(i+1))){
                    in_s0('*');
                }
                s1.push(f.divide(new BigDecimal(1), 8, RoundingMode.HALF_EVEN));
                continue;
            }else if(s.equals(Character.toString('F'))){
                BigDecimal f = new BigDecimal(1.618);
                if(i != 0 && isdigit(stri.charAt(i-1))){
                    in_s0('*');
                }
                if(i != stri.length()-1 && isdigit(stri.charAt(i+1))){
                    in_s0('*');
                }
                s1.push(f.divide(new BigDecimal(1), 3, RoundingMode.HALF_EVEN));
                continue;
            }else if(s.equals("!")){
                BigDecimal y = s1.peek();
                if(y.signum() == -1){
                    was_error = true;
                    Toast.makeText(getApplicationContext(), "Error: Unable to find negative factorial.", Toast.LENGTH_SHORT).show();
                    break;
                }else{
                    if(y.compareTo(new BigDecimal(500)) == 1){
                        was_error = true;
                        Toast.makeText(getApplicationContext(), "For some reason, we cannot calculate the factorial of this number " +
                                "(because it is too large and may not have enough device resources when executed)", Toast.LENGTH_LONG).show();
                        break;
                    }else{
                        s1.pop();
                        String fa = y.toString();
                        if(fa.contains(".")){
                            int index = fa.lastIndexOf(".");
                            fa = fa.substring(0, index);
                            y = new BigDecimal(fa);
                        }
                        s1.push(fact(y));
                    }
                }
                continue;
            }else if(s.equals("%")){
                BigDecimal y = s1.peek();
                s1.pop();
                s1.push(y.divide(new BigDecimal(100)));
                continue;
            }else if(s.equals("e")){
                BigDecimal f = new BigDecimal(Math.E);
                if(i != 0 && isdigit(stri.charAt(i-1))){
                    in_s0('*');
                }
                if(i != stri.length()-1 && isdigit(stri.charAt(i+1))){
                    in_s0('*');
                }
                s1.push(f);
                continue;
            }else if(s.equals("R")){
                if(i == len-1){
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.invalidstateforsin) + " " + getResources().getString(R.string.invalidfor)
                                    + " √", Toast.LENGTH_LONG).show();
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
                while((i < stri.length()) && ((stri.charAt(i) == '.') || isdigit(str[i]) || stri.charAt(i) == 'E' || (stri.charAt(i) == '-' && stri.charAt(i-1) == 'E'))){
                    s = Character.toString(str[i]);
                    x += s;
                    i++;
                }
                i--;
                if(x.contains("E")){
                    x = calc_e(x);
                    //Toast.makeText(getApplicationContext(), x, Toast.LENGTH_LONG).show();
                }
                s1.push(new BigDecimal(x));
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
                                if(!isdigit(str[i]) && str[i] != ')')
                                    in_s0(str[i]);
                                i++;
                            }
                            i-= 2;
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
                    if(i != 0 && str[i] == '(' && isdigit(str[i-1])){
                        in_s0('*');
                    }

                    in_s0(str[i]);
                }else{
                    while(!s0.empty() && !s0.peek().equals("(")){
                        mult(s0.peek());
                        s0.pop();
                    }
                    if(!s0.empty() && s0.peek().equals("(")){
                        s0.pop();
                    }
                }
            }
        }
        while(!s0.isEmpty() && s1.size() >= 2){
            mult(s0.peek());
            s0.pop();
        }
        if(!s0.isEmpty() && s1.size() == 1){
            if(s0.peek().equals("R")){
                mult(s0.peek());
                s0.pop();
            }
            if(!s0.isEmpty() && (s0.peek().equals("cos") || s0.peek().equals("sin") || s0.peek().equals("log") || s0.peek().equals("ln") || s0.peek().equals("tan"))){
                mult(s0.peek());
                s0.pop();
            }
        }
        if(!was_error){
            if(type.equals("all")){
                TextView tans = findViewById(R.id.textStr);
                tans.setText(s1.peek().toString());
                tans = findViewById(R.id.textAns2);
                tans.setText(original);
                HorizontalScrollView scrollview = findViewById(R.id.scrollview);

                scrollview.post(() -> {
                    scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                });
                HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
                scrollviewans.setVisibility(HorizontalScrollView.VISIBLE);
                scrollviewans.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollviewans.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                });

                String his = sp.getString("history", "");
                if(his.indexOf(original + "," + s1.peek().toString() + ";") != 0)
                    his = original + "," + s1.peek().toString() + ";" + his;
                sp.edit().putString("history", his).apply();
                if(sp.getBoolean("saveResult", false))
                    sp.edit().putString("saveResultText", original + ";" + s1.peek().toString()).apply();
            }else if(type.equals("not")){
                TextView preans = findViewById(R.id.textAns2);
                preans.setText(s1.peek().toString());
                HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
                scrollviewans.setVisibility(HorizontalScrollView.VISIBLE);
                scrollviewans.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollviewans.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                });
            }
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
        int len = stri.length();
        if(len != 0 && stri.charAt(len-1) == '^' && type.equals("all")){
            stri += "(";
            brackets++;
            txt.setText(stri);
            return;
        }
        //int twidth = txt.getWidth();
        float sz = txt.getTextSize();
        if(len != 0 && !isdigit(stri.charAt(len-1)) && stri.charAt(len-1) != '!' && stri.charAt(len-1) != '%')
            return;
        //txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        resize_text();
        //Toast.makeText(getApplicationContext(), Float.toString(txt.getTextSize()), Toast.LENGTH_LONG).show();
        HorizontalScrollView scrollview = findViewById(R.id.scrollview);

        scrollview.post(() -> {
            scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
        });
        if(type.equals("all")){
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
                calc(stri, type, digits, actions);
            }
        }else if(type.equals("not")){
            if(!stri.equals("")){
                if(stri.charAt(stri.length() - 1) != '('){
                    //Toast.makeText(getApplicationContext(), Integer.toString(brackets), Toast.LENGTH_SHORT).show();
                    if(brackets > 0){
                        for(int i = 0; i < brackets; i++){
                            stri += ")";
                        }
                    }
                }
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
                calc(stri, type, digits, actions);
                /*if(stri.charAt(len-1) == 'P' || stri.charAt(len-1) == 'F' || stri.charAt(len-1) == 'e' || (!islet(stri.charAt(len-1)) && (isdigit(stri.charAt(len-1)) || stri.charAt(len-1) == ')'))){

                }else{
                    HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
                    scrollviewans.setVisibility(HorizontalScrollView.INVISIBLE);
                }*/
            }else{
                HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
                scrollviewans.setVisibility(HorizontalScrollView.INVISIBLE);
            }
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
                }
            }
            return;
        }

        if(btntxt.equals("^") && last == '('){
            return;
        }

        if(btntxt.equals("√")){
            if(len == 0){
                t.setText(btntxt + "(");
                brackets++;
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
                    if(!isdigit(last)){
                        t.setText(txt + btntxt + "(");
                        brackets++;
                        //Toast.makeText(getApplicationContext(), Integer.toString(brackets), Toast.LENGTH_SHORT).show();
                        equallu("not");
                    }
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
                if(txt.charAt(len-1) == '(')
                    return;
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
                brackets--;
            }
            return;
        }

        if(!isdigit(btntxt) && !btntxt.equals("(") && !btntxt.equals(")") && !btntxt.equals("(") && !islet(btntxt.charAt(0))){
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
                    if(txt.charAt(len-1) != ')' && !isdigit(txt.charAt(len-1))){
                        if(len != 1){
                            txt = txt.substring(0, len-1);
                            t.setText(txt + btntxt);
                            equallu("not");
                        }
                    }else{
                        if(isdigit(txt.charAt(len-1)) || txt.charAt(len-1) == ')'){
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
        HorizontalScrollView scrollview = findViewById(R.id.scrollview);
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
    }

    public void gotoactions(View v){
        Button btn8 = findViewById(R.id.btn8);
        sp.edit().putInt("btnWeight", btn8.getHeight()).apply();
        sp.edit().putInt("btnWidth", btn8.getWidth()).apply();
        sp.edit().putBoolean("toChoose", true).apply();
        sp.edit().putString("action", "gotoactions").apply();
        Intent resultIntent = new Intent(getApplicationContext(), chooseactions.class);
        resultIntent.putExtra("btnHeight", btn8.getHeight());
        resultIntent.putExtra("btnWidth", btn8.getWidth());
        if(add_menu_opened){
            if(sp.getInt("btn_add_align", 0) == 0){
                show_hide(findViewById(R.id.btnShAdd));
            }else if(sp.getInt("btn_add_align", 0) == 1){
                show_hide(findViewById(R.id.btnShAdd2));
            }
        }
        startActivity(resultIntent);
        overridePendingTransition(R.anim.abc_popup_enter, R.anim.alpha);
    }

    public void onClick(View v){
        btn = findViewById(v.getId());
        t = findViewById(R.id.textStr);
        String txt = t.getText().toString();
        String btntxt = btn.getText().toString();
        add_text(btntxt);
       // log("onClick action;");
        if(add_menu_opened){
            if(sp.getInt("btn_add_align", 0) == 0){
                show_hide(findViewById(R.id.btnShAdd));
            }else if(sp.getInt("btn_add_align", 0) == 1){
                show_hide(findViewById(R.id.btnShAdd2));
            }
        }
    }

    public void delall(View v){
        TextView t = findViewById(R.id.textStr);
        t.setText("");
        t = findViewById(R.id.textAns2);
        HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
        scrollviewans.setVisibility(HorizontalScrollView.INVISIBLE);
        t.setText("");
        brackets = 0;
        was_error = false;
        sp.edit().remove("saveResultText").apply();
        t = findViewById(R.id.textStr);
        set_text_toDef();
	    HorizontalScrollView scrollview = findViewById(R.id.scrollview);

	    scrollview.post(() -> {
		    scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
	    });
        if(add_menu_opened){
            if(sp.getInt("btn_add_align", 0) == 0){
                show_hide(findViewById(R.id.btnShAdd));
            }else if(sp.getInt("btn_add_align", 0) == 1){
                show_hide(findViewById(R.id.btnShAdd2));
            }
        }
        //log("all text del");
    }

    public void delSymbol(View v){
        TextView txt = findViewById(R.id.textStr);
        String text = txt.getText().toString();
        HorizontalScrollView scrollviewans1 = findViewById(R.id.scrollViewAns);
        scrollviewans1.setVisibility(HorizontalScrollView.INVISIBLE);
        //ans.setVisibility(View.INVISIBLE);
        /**/
        if(text.length() != 0){
            if(text.charAt(text.length()-1) == ')'){
                brackets++;
            }
            if(text.charAt(text.length()-1) == '('){
                brackets--;
            }
            was_error = false;
            text = text.substring(0, text.length()-1);
            txt.setText(text);
            equallu("not");
        }
        if(txt.getText().toString().equals("")){
            t = findViewById(R.id.textAns2);
            /*HorizontalScrollView scrollviewans = findViewById(R.id.scrollViewAns);
            scrollviewans.setVisibility(HorizontalScrollView.INVISIBLE);*/
            t.setText("");
            sp.edit().remove("saveResultText").apply();
            set_text_toDef();
        }
        if(add_menu_opened){
            if(sp.getInt("btn_add_align", 0) == 0){
                show_hide(findViewById(R.id.btnShAdd));
            }else if(sp.getInt("btn_add_align", 0) == 1){
                show_hide(findViewById(R.id.btnShAdd2));
            }
        }
        HorizontalScrollView scrollview = findViewById(R.id.scrollview);

        scrollview.post(() -> {
            scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
        });
    }
}