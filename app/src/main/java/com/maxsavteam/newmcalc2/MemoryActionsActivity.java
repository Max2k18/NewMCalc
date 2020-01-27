package com.maxsavteam.newmcalc2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.newmcalc2.adapters.WindowRecallAdapter;
import com.maxsavteam.newmcalc2.utils.MemorySaverReader;
import com.maxsavteam.newmcalc2.utils.Utils;

import java.math.BigDecimal;

public class MemoryActionsActivity extends AppCompatActivity implements WindowRecallAdapter.inter {
	private BigDecimal[] barr;
	private WindowRecallAdapter adapter;
	private boolean DarkMode;
	private String type;
	private Intent activity_intent;
	private MemorySaverReader memorySaverReader;

	private void backPressed(){
		finish();
		overridePendingTransition(R.anim.activity_in1, R.anim.activity_out1);
	}

	@Override
	public void onBackPressed() {
		backPressed();
	}

	private void applyActionBarProps(){
		ActionBar appActionBar = getSupportActionBar();
		if(appActionBar != null) {
			if (DarkMode) {
				appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_32dp);
				appActionBar.setBackgroundDrawable(getDrawable(R.drawable.black));
				getWindow().setNavigationBarColor(Color.BLACK);
			} else {
				getWindow().setNavigationBarColor(Color.WHITE);
				appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_32dp);
				appActionBar.setBackgroundDrawable(getDrawable(R.drawable.white));
			}
			appActionBar.setElevation(0);
		}
	}

	public void cancel_all(View v){
		backPressed();
	}

	public void clear_all(View v){
		AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setMessage(R.string.delete_all_memories_quest)
				.setPositiveButton(R.string.yes, (dialogInterface1, i) -> {
					barr = new BigDecimal[10];
					for(int ii = 0; ii < 10; ii++){
						barr[ii] = BigDecimal.valueOf(0);
					}
					memorySaverReader.save(barr);
					adapter.notifyDataSetChanged();
					dialogInterface1.cancel();
					Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".MEMORY_EDITED");
					sendBroadcast(intent);
					backPressed();
				})
				.setNegativeButton(R.string.no, (dialogInterface1, i) -> dialogInterface1.cancel()).create();
		Window window = alertDialog.getWindow();
		if(window != null){
			if(DarkMode)
				window.setBackgroundDrawable(getDrawable(R.drawable.grey));
			
			window.requestFeature(Window.FEATURE_NO_TITLE);
		}
		Utils.recolorAlertDialogButtons(alertDialog, this);
		alertDialog.show();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home){
			backPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("SourceLockedOrientationActivity")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		DarkMode = sp.getBoolean("dark_mode", false);
		if(DarkMode){
			setTheme(android.R.style.Theme_Material_NoActionBar);
		}else{
			setTheme(R.style.AppTheme);
		}
		setContentView(R.layout.activity_memory_actions);
		memorySaverReader = new MemorySaverReader(this);

		if(DarkMode)
			getWindow().setBackgroundDrawableResource(R.drawable.black);
		else
			getWindow().setBackgroundDrawableResource(R.drawable.white);

		activity_intent = getIntent();
		type = activity_intent.getStringExtra("type");
		try{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle( ( type.equals("rc") ? "Recall Memory" : "Store Memory" ) );
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}catch(NullPointerException e){
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		applyActionBarProps();

		barr = memorySaverReader.read();
		RecyclerView rv = findViewById(R.id.memory_actions_rv);
		adapter = new WindowRecallAdapter(this, barr);
		adapter.setInterface(this);
		LinearLayoutManager manager = new LinearLayoutManager(this);
		manager.setOrientation(RecyclerView.VERTICAL);
		rv.setLayoutManager(manager);
		rv.setAdapter(adapter);
	}

	@Override
	public void onRecallClick(View view, int position) {
		if(type.equals("st")){
			String value = activity_intent.getStringExtra("value");
			barr[position] = new BigDecimal(value);
			memorySaverReader.save(barr);
			Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".MEMORY_EDITED");
			sendBroadcast(intent);
			backPressed();
		}else if(type.equals("rc")){
			Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".RECALL_MEM");
			intent.putExtra("value", barr[position].toString());
			sendBroadcast(intent);
			backPressed();
		}
	}
}
