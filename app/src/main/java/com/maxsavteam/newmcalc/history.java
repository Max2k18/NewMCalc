package com.maxsavteam.newmcalc;

import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class history extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    MyRecyclerViewAdapter adapter;

    SharedPreferences sp;

    protected void backPressed(){
        finish();
        overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
    }


    @Override
    public void onBackPressed(){
        sp.edit().remove("action").apply();
        sp.edit().remove("history_action").apply();
        backPressed();
        super.onBackPressed();
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
        sp.edit().putString("history_action", adapter.getItem(position).get(0)).apply();
        backPressed();
        //Toast.makeText(this, "You clicked " + adapter.getItem(position).get(0) + " " + adapter.getItem(position).get(0) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_checking);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        RecyclerView rv;
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
            getSupportActionBar().setTitle(R.string.hitory);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        ArrayList< ArrayList<String> > str = new ArrayList<>();
        ArrayList<String> str2 = new ArrayList<>();
        String his = sp.getString("history", "not");
        if(Objects.equals(his, "not")){
            setContentView(R.layout.history_notfound);
        }else{
            int i = 0;
            String ex, ans;
            while(i < his.length() && his.charAt(i) != ';'){
                boolean was_dot = false;
                ex = ans = "";
                while(i < his.length()){
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
                setContentView(R.layout.activity_history);
                LinearLayoutManager lay = new LinearLayoutManager(this);
                lay.setOrientation(LinearLayoutManager.VERTICAL);
                rv = (RecyclerView) findViewById(R.id.rv_view);
                rv.setLayoutManager(lay);
                adapter = new MyRecyclerViewAdapter(getApplicationContext(), str);
                adapter.setClickListener(this);
                rv.setAdapter(adapter);
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                Log.e("Layout", e.toString());
            }

        }
    }
}
