package com.maxsavteam.newmcalc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.maxsavteam.newmcalc.adapters.window_recall_adapter;
import com.maxsavteam.newmcalc.memory.MemorySaverReader;

import java.math.BigDecimal;

public class memory_actions_activity extends AppCompatActivity implements window_recall_adapter.inter {
	SharedPreferences sp;
	BigDecimal[] barr;
	window_recall_adapter adapter;
	boolean DarkMode;
	String type;
	Intent activity_intent;
	private MemorySaverReader memorySaverReader;

	void backPressed(){
		finish();
		overridePendingTransition(R.anim.activity_in1, R.anim.activity_out1);
	}

	@Override
	public void onBackPressed() {
		backPressed();
	}

	private void apply_actionbar(){
		ActionBar appActionBar = getSupportActionBar();
		if(DarkMode){
			appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_32dp);
			appActionBar.setBackgroundDrawable(getDrawable(R.drawable.black));
			getWindow().setNavigationBarColor(Color.BLACK);
		}else{
			getWindow().setNavigationBarColor(Color.WHITE);
			appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_32dp);
			appActionBar.setBackgroundDrawable(getDrawable(R.drawable.white));
		}
		appActionBar.setElevation(0);
	}

	public void cancel_all(View v){
		backPressed();
	}

	public void clear_all(View v){
		AlertDialog alertDialog1 = new AlertDialog.Builder(this)
				.setTitle("New MCalc")
				.setMessage(R.string.delete_all_memories_quest)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface1, int i) {
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
					}
				})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface1, int i) {
						dialogInterface1.cancel();
					}
				}).create();
		if(DarkMode)
			alertDialog1.getWindow().setBackgroundDrawable(getDrawable(R.drawable.grey));
		alertDialog1.show();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home){
			backPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
			getSupportActionBar().setElevation(0);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}catch(Exception e){
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		apply_actionbar();

		barr = memorySaverReader.read();
		RecyclerView rv = findViewById(R.id.memory_actions_rv);
		adapter = new window_recall_adapter(this, barr);
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
