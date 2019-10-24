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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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

import com.google.android.material.button.MaterialButton;
import com.maxsavteam.newmcalc.adapters.MyRecyclerViewAdapter;
import com.maxsavteam.newmcalc.adapters.MyRecyclerViewAdapter.ViewHolder;
import com.maxsavteam.newmcalc.swipes.SwipeController;
import com.maxsavteam.newmcalc.swipes.SwipeControllerActions;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class history extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{

    MyRecyclerViewAdapter adapter;

    SharedPreferences sp;
    Intent history_action;
    String his_des;
    ArrayList< ArrayList<String> > str = new ArrayList<>();
    private RecyclerView rv;

    protected void backPressed(){
        sendBroadcast(history_action);
        finish();
        overridePendingTransition(R.anim.activity_in1, R.anim.activity_out1);
    }

    private void restartActivity(){
        Intent in = new Intent(this, history.class);
        this.startActivity(in);
        this.finish();
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

    @Override
    public void onItemClick(View view, int position) {
        if(position == POSITION_TO_DEL){
            cancelTimer();
            animate_hide();
        }
        history_action = new Intent(BuildConfig.APPLICATION_ID + ".HISTORY_ACTION");
        String ex = adapter.getItem(position).get(0);
        if(ex.contains("~")) {
            int j;
	        j = 0;
	        while (j < ex.length() && ex.charAt(j) != '~') {
		        j++;
	        }
	        ex = ex.substring(0, j);
        }
        history_action.putExtra("example", ex).putExtra("result", adapter.getItem(position).get(1));
        backPressed();
        //Toast.makeText(this, "You clicked " + adapter.getItem(position).get(0) + " " + adapter.getItem(position).get(0) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    int POSITION_TO_DEL = -1;
    View VIEW_ON_DELETE;

    @Override
    public void onDelete(int position, ViewHolder v) {
        try {
            if (POSITION_TO_DEL == -1 && IN_ORDER) {
                Toast.makeText(this, "Something went wrong. Please, restart activity", Toast.LENGTH_SHORT).show();
                return;
            }

            if (IN_ORDER) {
                return;
            }
            View view = v.itemView;
            view.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            TextView t;
            for (int id : new int[]{R.id.tvAns, R.id.tvAns2}) {
                t = view.findViewById(id);
                t.setTextColor(getResources().getColor(R.color.white));
            }
            //view.setEnabled(false);
            VIEW_ON_DELETE = view;
            POSITION_TO_DEL = position;
            showDeleteCountdownAndRun();
        }catch (Exception e){
            Toast.makeText(this, e.toString() + "\n\n" + position + "\n\n" + rv.getLayoutManager().getChildCount(), Toast.LENGTH_LONG).show();
            cancelTimer();
            animate_hide();
        }
    }
    final int SECONDS_BEFORE_DELETE = 5;

    @SuppressLint("DefaultLocale")
    private void setupTimer(){
        if (mTimer != null) {
            mTimer.cancel();
        }
        countdown_del = SECONDS_BEFORE_DELETE;
        mTimer = new Timer();
        cancel = findViewById(R.id.cancel_delete);
        tCount = cancel.findViewById(R.id.txtCountDown);
        tCount.setText(String.format("%d", countdown_del));
        MyTimer myTimer = new MyTimer();
        mTimer.schedule(myTimer, 600, 1000);
    }

    View.OnLongClickListener forceDelete = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            delete();
            animate_hide();
            cancelTimer();
            setupRecyclerView();
            return true;
        }
    };

    private void cancelTimer(){
        IN_ORDER = false;
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void showDeleteCountdownAndRun(){
        LinearLayout lay = findViewById(R.id.cancel_delete);
        lay.setVisibility(View.VISIBLE);
        ((TextView)lay.findViewById(R.id.txtCountDown)).setText(String.format("%d", SECONDS_BEFORE_DELETE));
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scale_show);
        //animation.setDuration(500);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onAnimationEnd(Animation animation) {
                //setupTimer();
                if(!sp.getBoolean("force_delete_guide_was_showed", false)) {
                    new MaterialTapTargetPrompt.Builder(history.this)
                            .setTarget(R.id.btnCancel)
                            .setPrimaryText(getResources().getString(R.string.force_delete))
                            .setSecondaryText(getResources().getString(R.string.force_delete_guide_text))
                            .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                                @Override
                                public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                                    if(state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                                        sp.edit().putBoolean("force_delete_guide_was_showed", true).apply();
                                        setupTimer();
                                    }
                                }
                            }).show();
                }else{
                    setupTimer();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        lay.setAnimation(animation);
        lay.animate();
        animation.start();
    }

	@Override
	public void ShowInfoButtonPressed(ViewHolder viewHolder, int position) {
        //view = (View) view.getParent().getParent();
        View view = viewHolder.itemView;
        TextView t = view.findViewById(R.id.tvWithDesc);
        boolean with = Boolean.parseBoolean(t.getText().toString());
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
			                    adapter.clearViews();adapter.notifyDataSetChanged();
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
                .setPositiveButton("OK", (dialogInterface, i) -> {
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
                        adapter.clearViews();adapter.notifyDataSetChanged();
                    }else if(mode.equals("add")){
                        if(new_desc.equals(""))
                            return;
                        String s = str.get(position).get(0);
                        new_desc = new_desc.replaceAll(",", "&,");
                        s += "~" + new_desc;
                        str.get(position).set(0, s);
                        new_desc = new_desc.replaceAll(Character.toString('&'), "");
                        view.findViewById(R.id.without_desc).setVisibility(View.GONE);
                        ((TextView) view.findViewById(R.id.txtDesc2)).setText(new_desc);
                        view.findViewById(R.id.with_desc).setVisibility(View.GONE);
                        save_history();
                    }
                    adapter.clearViews();adapter.notifyDataSetChanged();
                    dialogInterface.cancel();
                })
                .setNegativeButton(R.string.cancel, (dialog, i) -> dialog.cancel())
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
        cancelTimer();
        str.remove(POSITION_TO_DEL);
        //VIEW_ON_DELETE.setEnabled(true);
        if(DarkMode)
            VIEW_ON_DELETE.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        else
            VIEW_ON_DELETE.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        //VIEW_ON_DELETE.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.transparent)));
        if(!DarkMode){
            TextView t;
            for(int id : new int[]{R.id.tvAns, R.id.tvAns2}){
                t = VIEW_ON_DELETE.findViewById(id);
                t.setTextColor(getResources().getColor(R.color.black));
            }
        }
        save_history();
        setupRecyclerView();
    }

    private void setupRecyclerView(){
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
            t.setText(String.format("%s\n%s\n%s", strings[0], strings[1], strings[2]));
        }else {
            try {
                LinearLayoutManager lay = new LinearLayoutManager(this);
                lay.setOrientation(RecyclerView.VERTICAL);
                adapter = new MyRecyclerViewAdapter(getApplicationContext(), str);
                adapter.setClickListener(this);
                rv.setAdapter(adapter);
                rv.setLayoutManager(lay);
                SwipeController sc = new SwipeController(new SwipeControllerActions() {
                    @Override
                    public void onRightClicked(int position) {
                        ArrayList<MyRecyclerViewAdapter.ViewHolder> ar = adapter.getViews();
                        /*
                         * Crutch, because recyclerview returns child count one value, but adapter returns right value.
                         * I tried to google, but so far I haven't found anything.
                         */
                        onDelete(position, ar.get(position));
                        //Toast.makeText(history.this, Integer.toString(adapter.getViews().size()), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLeftClicked(int position) {
                        ShowInfoButtonPressed(adapter.getViews().get(position), position);
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
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                Log.e("Layout", e.toString());
            }
        }
    }

    public void cancel(View v){
        cancelTimer();
        IN_ORDER = false;
        POSITION_TO_DEL = -1;
        VIEW_ON_DELETE.setEnabled(true);
        if (DarkMode) {
            VIEW_ON_DELETE.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        } else {
            TextView t;
            for(int id : new int[]{R.id.tvAns, R.id.tvAns2}){
                t = VIEW_ON_DELETE.findViewById(id);
                t.setTextColor(getResources().getColor(R.color.black));
            }
            VIEW_ON_DELETE.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        }
        animate_hide();
    }

    Timer mTimer;
    int countdown_del = 6;
    TextView tCount;
    LinearLayout cancel;
    public boolean IN_ORDER = false;

    class MyTimer extends TimerTask{
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            if (countdown_del != 0) {
                countdown_del--;
                IN_ORDER = true;
                runOnUiThread(() -> tCount.setText(Integer.toString(countdown_del)));

            } else {
                cancelTimer();
                IN_ORDER = false;
                runOnUiThread(history.this::delete);
                animate_hide();
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
    private void applyTheme(){
        ActionBar appActionBar = getSupportActionBar();
        try{
            appActionBar.setDisplayHomeAsUpEnabled(true);
            appActionBar.setTitle(R.string.hitory);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        if(DarkMode){
            getWindow().setBackgroundDrawableResource(R.drawable.black);
            appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_32dp);
            appActionBar.setBackgroundDrawable(getDrawable(R.drawable.black));
            getWindow().setNavigationBarColor(Color.BLACK);
        }else{
            getWindow().setNavigationBarColor(Color.WHITE);
            getWindow().setBackgroundDrawable(getDrawable(R.drawable.white));
            appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_32dp);
            appActionBar.setBackgroundDrawable(getDrawable(R.drawable.white));
        }
        appActionBar.setElevation(0);
        Button btn = findViewById(R.id.btnCancel);
        btn.setTextColor(getResources().getColor(R.color.white));
        btn.setOnLongClickListener(forceDelete);
    }

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
        applyTheme();

        rv = findViewById(R.id.rv_view);

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
            setupRecyclerView();
        }
    }
}
