package com.maxsavteam.newmcalc;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maxsavteam.newmcalc.adapters.MyRecyclerViewAdapter;
import com.maxsavteam.newmcalc.swipes.SwipeController;
import com.maxsavteam.newmcalc.swipes.SwipeControllerActions;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class history extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{

    MyRecyclerViewAdapter adapter;

    SharedPreferences sp;
    Intent history_action;
    AlertDialog create_description;
    String his_des;
    ArrayList< ArrayList<String> > str = new ArrayList<>();
    ArrayList<String> desc = new ArrayList<>();

    protected void backPressed(){
        sendBroadcast(history_action);
        finish();
        overridePendingTransition(R.anim.activity_in1, R.anim.activity_out1);
    }

    @Override
    public void onBackPressed(){
        sp.edit().remove("action").apply();
        sp.edit().remove("history_action").apply();
        if (start_type.equals("app")) {
            backPressed();
        } else if (start_type.equals("shortcut")) {
            startActivity(new Intent(this, MainActivity.class));
            backPressed();
        }
        //super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
        if(id == android.R.id.home){
            backPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    void animation_hide_toolbar(){
        LinearLayout lay = findViewById(R.id.tools_onlongclick);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_scale_hide);
        anim.setDuration(500);
        anim.setInterpolator(this, android.R.anim.accelerate_interpolator);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lay.setVisibility(View.GONE);
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

    void animation_show_toolbar(){
        LinearLayout lay = findViewById(R.id.tools_onlongclick);
        lay.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_scale_show);
        anim.setInterpolator(this, android.R.anim.accelerate_interpolator);
        anim.setDuration(500);
        lay.clearAnimation();
        lay.setAnimation(anim);
        lay.animate();
        anim.start();
    }

    @Override
    public void onItemClick(View view, int position) {
        history_action = new Intent(BuildConfig.APPLICATION_ID + ".HISTORY_ACTION");
        String ex = adapter.getItem(position).get(0);
        if(ex.contains("~")) {
            int j;
            for (j = 0; j < ex.length() && ex.charAt(j) != '~'; j++);
            ex = ex.substring(0, j);
        }
        history_action.putExtra("example", ex).putExtra("result", adapter.getItem(position).get(1));
        backPressed();
        //Toast.makeText(this, "You clicked " + adapter.getItem(position).get(0) + " " + adapter.getItem(position).get(0) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    static int POSITION_to_del = -1;
    View view_ondelete;

    boolean delete_mode = false;

    @Override
    public void onDelete(int position, int adapter_position, View v) {
        //Toast.makeText(this, Boolean.toString(in_order), Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(), v.getResources().getResourceName(v.getId()), Toast.LENGTH_LONG).show();
        String ids = v.getResources().getResourceName(v.getId());
        if(POSITION_to_del == -1 && in_order){
            Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (in_order)
            return;
        v.findViewById(R.id.btnDelInRow).setVisibility(View.GONE);
        v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C80000")));
        TextView t;
        for(int id : new int[]{R.id.tvAns, R.id.tvAns2}){
            t = v.findViewById(id);
            t.setTextColor(getResources().getColor(R.color.white));
        }
        v.setEnabled(false);
        view_ondelete = v;
        POSITION_to_del = position;

        LinearLayout lay = findViewById(R.id.cancel_delete);
        lay.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scale_show);
        //animation.setDuration(500);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onAnimationEnd(Animation animation) {
                try {
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    countdown_del = 5;
                    mTimer = new Timer();
                    cancel = findViewById(R.id.cancel_delete);
                    tCount = cancel.findViewById(R.id.txtCountDown);
                    tCount.setText(Integer.toString(countdown_del));
                    MyTimer myTimer = new MyTimer();
                    mTimer.schedule(myTimer, 600, 1000);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "AnimationEnd\n" + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        lay.setAnimation(animation);
        lay.animate();
        animation.start();
        /*try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        //lay.postOnAnimation((Runnable) lay.animate());
    }

	@Override
	public void ShowInfoButtonPressed(View view, int position) {
        //view = (View) view.getParent().getParent();
        TextView t = view.findViewById(R.id.tvWithDesc);
        Boolean with = Boolean.parseBoolean(t.getText().toString());
        if(view.findViewById(R.id.with_desc).getVisibility() == View.VISIBLE
                || view.findViewById(R.id.without_desc).getVisibility() == View.VISIBLE){
            LinearLayout lay;
            if(with){
                lay = view.findViewById(R.id.with_desc);
            }else
                lay = view.findViewById(R.id.without_desc);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scalefordesc_hide);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    lay.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            lay.clearAnimation();
            lay.setAnimation(animation);
            lay.animate();
            animation.start();
        }else {
            LinearLayout lay;
            if (with) {
                lay = view.findViewById(R.id.with_desc);
            } else
                lay = view.findViewById(R.id.without_desc);
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scalefordesc_show);
            lay.clearAnimation();
            anim.setAnimationListener(new Animation.AnimationListener() {
	            @Override
	            public void onAnimationStart(Animation animation) {

	            }

	            @Override
	            public void onAnimationEnd(Animation animation) {
	            	//lay.setVisibility(View.VISIBLE);
					lay.clearAnimation();
	            }

	            @Override
	            public void onAnimationRepeat(Animation animation) {

	            }
            });
            lay.setAnimation(anim);
            lay.setVisibility(View.VISIBLE);
            lay.animate();
            anim.start();
        }
    }

    @Override
    public void onDescriptionDelete(View view, int position) {
        View par = view;
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String s = str.get(position).get(0);
                        int j;
                        for(j = 0; j < s.length() && s.charAt(j) != '~'; j++);
                        s = s.substring(0, j);
                        str.get(position).set(0, s);
                        //Toast.makeText(getApplicationContext(), par.getResources().getResourceName(par.getId()), Toast.LENGTH_LONG).show();
                        LinearLayout lay = par.findViewById(R.id.with_desc);
	                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scalefordesc_hide);
	                    animation.setAnimationListener(new Animation.AnimationListener() {
		                    @Override
		                    public void onAnimationStart(Animation animation) {

		                    }

		                    @Override
		                    public void onAnimationEnd(Animation animation) {
			                    lay.setVisibility(View.GONE);
			                    adapter.notifyDataSetChanged();
		                    }

		                    @Override
		                    public void onAnimationRepeat(Animation animation) {

		                    }
	                    });
	                    lay.clearAnimation();
	                    lay.setAnimation(animation);
	                    lay.animate();
	                    animation.start();
	                    //par.findViewById(R.id.with_desc).setVisibility(View.GONE);
                        //par.findViewById(R.id.without_desc).setVisibility(View.VISIBLE);
                        save_history();
                    }
                })
                .setTitle(R.string.confirm)
                .setMessage(R.string.confirm_del_desc)
                .create();
        alertDialog.show();
    }

    @Override
    public void onEdit_Add(View view, int position, String mode) {
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        AlertDialog al = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(input)
                .setMessage(R.string.enter_text)
                .setTitle(R.string.desc)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String new_desc = input.getText().toString();
                        if(mode.equals("edit")){
                            if(!new_desc.equals("")) {
                                String previous = str.get(position).get(0);
                                int j;
                                //noinspection StatementWithEmptyBody
                                for (j = 0; j < previous.length() && previous.charAt(j) != '~'; j++);
                                previous = previous.substring(0, j);
                                new_desc = new_desc.replaceAll(",", "&,");
                                previous += "~" + new_desc;
                                str.get(position).set(0, previous);
                                save_history();
                                ((TextView) view.findViewById(R.id.txtDesc2)).setText(new_desc);
                            }else{
                                String previous = str.get(position).get(0);
                                int j;
                                //noinspection StatementWithEmptyBody
                                for (j = 0; j < previous.length() && previous.charAt(j) != '~'; j++);
                                previous = previous.substring(0, j);
                                str.get(position).set(0, previous);
                                save_history();
                                view.findViewById(R.id.with_desc).setVisibility(View.GONE);
                                view.findViewById(R.id.without_desc).setVisibility(View.VISIBLE);
                            }
                            adapter.notifyDataSetChanged();
                        }else if(mode.equals("add")){
                            if(new_desc.equals(""))
                                return;
                            String s = str.get(position).get(0);
                            new_desc = new_desc.replaceAll(",", "&,");
                            s += "~" + new_desc;
                            str.get(position).set(0, s);
                            new_desc = new_desc.replaceAll(Character.toString('&'), "");
                            /*String temp = new_desc;
                            new_desc = "";
                            for(int j = 0; j < temp.length(); j++){
                            	if(temp.charAt(i) != '&'){
                            		new_desc += temp.charAt(j);
	                            }
                            }*/
                            view.findViewById(R.id.without_desc).setVisibility(View.GONE);
                            ((TextView) view.findViewById(R.id.txtDesc2)).setText(new_desc);
                            view.findViewById(R.id.with_desc).setVisibility(View.GONE);
                            save_history();
                        }
                        adapter.notifyDataSetChanged();
                        dialogInterface.cancel();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create();
        if(mode.equals("edit")){
            String desc = ((TextView) view.findViewById(R.id.txtDesc2)).getText().toString();
            input.append(desc);
        }
        al.show();
    }

    void save_history(){
        StringBuilder save = new StringBuilder();
        int len = str.size();
        for(int i = 0; i < len; i++){
            save.append(str.get(i).get(0)).append(",").append(str.get(i).get(1)).append(";");
        }
        sp.edit().putString("history", save.toString()).apply();

    }

    public void delete(){
        animate_hide();
        str.remove(POSITION_to_del);
        /*desc.remove(POSITION_to_del);
        save_history_description();*/
        view_ondelete.setEnabled(true);
        view_ondelete.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.transparent)));
        if(!DarkMode){
            TextView t;
            for(int id : new int[]{R.id.tvAns, R.id.tvAns2}){
                t = view_ondelete.findViewById(id);
                t.setTextColor(getResources().getColor(R.color.black));
            }
        }
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
        save_history();
        adapter.notifyItemRemoved(POSITION_to_del);
        adapter.notifyDataSetChanged();
        if(str.size() == 0){
            if(DarkMode){
                setTheme(android.R.style.Theme_Material_NoActionBar);
            }else{
                setTheme(R.style.AppTheme);
            }
            setContentView(R.layout.history_notfound);
            TextView t = findViewById(R.id.txtHistoryNotFound);
            if(DarkMode) {
                t.setTextColor(getResources().getColor(R.color.white));
            }
            String[] strings = getResources().getStringArray(R.array.history_not_found);
            t.setText(strings[0] + "\n" + strings[1] + "\n" + strings[2]);
        }
    }

    public void cancel(View v){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
        in_order = false;
        POSITION_to_del = -1;
        view_ondelete.setEnabled(true);
        view_ondelete.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.transparent)));
        if(!DarkMode){
            TextView t;
            for(int id : new int[]{R.id.tvAns, R.id.tvAns2}){
                t = view_ondelete.findViewById(id);
                t.setTextColor(getResources().getColor(R.color.black));
            }
        }
        animate_hide();
    }

    Timer mTimer;
    int countdown_del = 6;
    TextView tCount;
    LinearLayout cancel;
    public boolean in_order = false;

    class MyTimer extends TimerTask{
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            if (countdown_del != 0) {
                countdown_del--;
                in_order = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tCount.setText(Integer.toString(countdown_del));
                    }
                });

            } else {
                if(mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                in_order = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        delete();
                        animate_hide();
                    }
                });
                //animate_hide();
            }
        }
    }

    public void animate_hide(){
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scale_hide);
        //anim.setDuration(500);
        //cancel.findViewById(R.id.btnCancel).setEnabled(false);
        //LinearLayout lay = findViewById(R.id.cancel_delete);
        cancel.clearAnimation();
        cancel.setAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cancel.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        cancel.animate();
        anim.start();
    }

    String start_type;

    boolean DarkMode;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        DarkMode = sp.getBoolean("dark_mode", false);
        if(DarkMode)
            setTheme(android.R.style.Theme_Material_NoActionBar);
        else
            setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        history_action = new Intent(BuildConfig.APPLICATION_ID  + ".HISTORY_ACTION");
        history_action.putExtra("example", "").putExtra("result", "");

        if(DarkMode){
        	getWindow().setBackgroundDrawableResource(R.drawable.black);
        }else{
            getWindow().setBackgroundDrawable(getDrawable(R.drawable.white));
        }
        RecyclerView rv = findViewById(R.id.rv_view);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
            getSupportActionBar().setTitle(R.string.hitory);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        Button btn = findViewById(R.id.btnCancel);
        btn.setTextColor(getResources().getColor(R.color.white));
        update_service ups = new update_service(this);        /*BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AlertDialog.Builder b = new AlertDialog.Builder(history.this);
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
        };
        BroadcastReceiver brfail = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AlertDialog.Builder b = new AlertDialog.Builder(history.this);
                b.setTitle(R.string.installation).setMessage(R.string.cannot_update).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            }
        };
        registerReceiver(br, new IntentFilter("android.intent.action.NEWMCALC_UPDATE_SUC"));
        registerReceiver(brfail, new IntentFilter("android.intent.action.NEWMCALC_UPDATE_FAIL"));*/
        start_type = getIntent().getStringExtra("start_type");
        ArrayList<String> str2 = new ArrayList<>();
        String his = sp.getString("history", "not");
        his_des = sp.getString("history_description", "none");
        if(Objects.equals(his, "not") || Objects.requireNonNull(his).equals("")){
            setContentView(R.layout.history_notfound);
            TextView t = findViewById(R.id.txtHistoryNotFound);
            if(DarkMode){
                t.setTextColor(getResources().getColor(R.color.white));
	            getWindow().setBackgroundDrawableResource(R.drawable.black);
            }
            String[] strings = getResources().getStringArray(R.array.history_not_found);
            t.setText(strings[0] + "\n" + strings[1] + "\n" + strings[2]);
        }else{
            int i = 0;
            String ex, ans;
            while(i < his.length() && his.charAt(i) != ';'){
                boolean was_dot = false;
                ex = ans = "";
                while(i < his.length()){
                    if(his.charAt(i) == '&') {
                        i++;
                        if(!was_dot){
                            ex += Character.toString(his.charAt(i));
                        }else{
                            ans += Character.toString(his.charAt(i));
                        }
                        i++;
                        continue;
                    }
                    if(his.charAt(i) == ';'){
                        i++;
                        break;
                    }
                    if(his.charAt(i) == ','){
                        i++;
                        was_dot = true;
                        continue;
                    }
                    if(!was_dot){
                        ex += Character.toString(his.charAt(i));
                    }else{
                        ans += Character.toString(his.charAt(i));
                    }
                    i++;
                }
                str2.add(ex);
                str2.add(ans);
                str.add((ArrayList<String>) str2.clone());
                str2.clear();
            }
            try {
                LinearLayoutManager lay = new LinearLayoutManager(this);
                lay.setOrientation(RecyclerView.VERTICAL);

                rv.setLayoutManager(lay);
                adapter = new MyRecyclerViewAdapter(getApplicationContext(), str);
                adapter.setClickListener(this);
                rv.setAdapter(adapter);
                SwipeController sc = new SwipeController(new SwipeControllerActions() {
                    @Override
                    public void onRightClicked(int position) {
                        onDelete(position, position, rv.getChildAt(position));
                    }

                    @Override
                    public void onLeftClicked(int position) {
                        ShowInfoButtonPressed(rv.getChildAt(position), position);
                    }
                }, this);
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(sc);
                itemTouchHelper.attachToRecyclerView(rv);
                rv.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                        sc.onDraw(c);
                    }
                });
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                Log.e("Layout", e.toString());
            }

        }
    }
}
