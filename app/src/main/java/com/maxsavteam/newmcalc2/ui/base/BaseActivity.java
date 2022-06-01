package com.maxsavteam.newmcalc2.ui.base;

import android.view.MenuItem;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BaseActivity extends AppCompatActivity {

	protected void displayHomeAsUp(){
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null)
			actionBar.setDisplayHomeAsUpEnabled( true );
	}

	protected void setActionBar(@IdRes int id){
		Toolbar toolbar = findViewById( id );
		setSupportActionBar( toolbar );
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if(item.getItemId() == android.R.id.home)
			onBackPressed();
		return super.onOptionsItemSelected( item );
	}
}
