package com.maxsavteam.newmcalc;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class numgen extends AppCompatActivity {

    int len = 0;

    ArrayList<String> s = new ArrayList<>();

    Drawable dr;

    View.OnLongClickListener longclick = new View.OnLongClickListener(){
        @Override
        public boolean onLongClick(View v){
            EditText min = findViewById(R.id.edTextMin);
            EditText max = findViewById(R.id.edTextMax);
            min.setText("");
            max.setText("");
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numgen);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
            getSupportActionBar().setTitle(R.string.randomgen);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            finish();
        }
        Intent intent = getIntent();
        if(intent.getStringExtra("type").equals("pass")){
            setContentView(R.layout.numgen_passgen);
            try{
                getSupportActionBar().setTitle(R.string.passgen);
            }catch(Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                finish();
            }
            for(int i = 97; i <= 122; i++){
                s.add(Character.toString((char) i));
                s.add(Character.toString((char) i).toLowerCase());
            }
            for (int i = 0; i < 10; i++){
                s.add(Integer.toString(i));
            }

            setbackground();
            Button b = findViewById(R.id.btnPass6);
            dr = b.getBackground();

            TextView t = findViewById(R.id.add_chars);
            t.setText("<>_.&%-?$^!=[]{}()");
            /*Button b = findViewById(R.id.btnGen);
            b.setOnLongClickListener(longclick);*/
        }

        //b.setOnLongClickListener(longclick);*/
    }

    protected void setbackground(){
        ArrayList<Button> b = new ArrayList<>();
        b.add(findViewById(R.id.btnPass6));
        b.add(findViewById(R.id.btnPass8));
        b.add(findViewById(R.id.btnPass12));
        b.add(findViewById(R.id.btnPass16));
        for(int i = 0; i < b.size(); i++){
            b.get(i).setBackground(dr);
            b.get(i).setTextColor(getResources().getColor(R.color.black));
        }
    }

    public void checkbox(View v){
        CheckBox ch = findViewById(R.id.checkBox);
        TextView tx = findViewById(R.id.add_chars);
        String x = "<>_.&%-?$^!=[]{}()";
        if(ch.isChecked()){
            tx.setVisibility(View.VISIBLE);
            for(int i = 0; i < x.length(); i++){
                s.add(Character.toString(x.charAt(i)));
            }
        }
        else{
            tx.setVisibility(View.INVISIBLE);
            int sz = s.size();
            for(int i = 0; i < x.length(); i++){
                s.remove(s.size() - 1);
            }
        }
    }

    public void btnonclick(View v){
        Button btn = findViewById(v.getId());
        setbackground();
        btn.setTextColor(getResources().getColor(R.color.white));
        btn.setBackgroundColor(getResources().getColor(R.color.black));
        len = Integer.valueOf(btn.getText().toString());
    }

    public void genpass(View v){
        if(len != 0){
            String pass = "";
            for(int i = 0, a; i < len; i++){
                a = ThreadLocalRandom.current().nextInt(0, s.size());
                pass += s.get(a);
            }
            findViewById(R.id.btnCopyPass).setVisibility(View.VISIBLE);
            TextView t = findViewById(R.id.txtGenedPass);
            t.setText(pass);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
        if(id == android.R.id.home){
            finish();
            overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
        }
        return super.onOptionsItemSelected(item);
    }

    public void generate(View v){

        EditText minInput = findViewById(R.id.edTextMin);
        EditText maxInput = findViewById(R.id.edTextMax);
        String maxs = maxInput.getText().toString();
        String mins = minInput.getText().toString();
        if(!minInput.getText().toString().equals("") && !maxInput.getText().toString().equals("")){
            /*if(Integer.valueOf(minInput.getText().toString()) < Integer.MIN_VALUE){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.minintexception), Toast.LENGTH_LONG).show();
                return;
            }
            if(maxInput.getText().toString().compareTo(Integer.toString(Integer.MAX_VALUE)) > 0){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.maxintexception), Toast.LENGTH_LONG).show();
                return;
            }*/
            //Toast.makeText(getApplicationContext(), maxInput.getText().toString() + " " + minInput.getText().toString()
                    //+ " " + Integer.toString(Integer.MIN_VALUE) + " " + Integer.toString(Integer.MAX_VALUE), Toast.LENGTH_SHORT).show();
            /*if(maxInput.getText().toString().compareTo(Integer.toString(Integer.MAX_VALUE)) > 0
                    || maxInput.getText().toString().compareTo(Integer.toString(Integer.MIN_VALUE)) < 0
                    || minInput.getText().toString().compareTo(Integer.toString(Integer.MAX_VALUE)) > 0
                    || minInput.getText().toString().compareTo(Integer.toString(Integer.MIN_VALUE)) < 0){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.maxintexception), Toast.LENGTH_LONG).show();
                return;
            }*/

            BigInteger b1 = new BigInteger(mins);
            BigInteger b2 = new BigInteger(maxs), lmax = BigInteger.valueOf(Long.MAX_VALUE), lmin = BigInteger.valueOf(Long.MIN_VALUE);

            if(b1.compareTo(lmin) < 0 || b1.compareTo(lmax) > 0 || b2.compareTo(lmin) < 0 || b2.compareTo(lmax) > 0){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.maxintexception), Toast.LENGTH_LONG).show();
                return;
            }

            Long minNum = Long.valueOf(minInput.getText().toString());
            Long maxNum = Long.valueOf(maxInput.getText().toString());

            /*Random rand = new Random();
            int result = maxNum - minNum;
            if(result < 0){
                result *= -1;
            }
            int randNum = rand.nextInt(result) + minNum;*/
            long randomNum = ThreadLocalRandom.current().nextLong(minNum, maxNum + 1);
            TextView t = findViewById(R.id.txtAnswer);
            t.setText(Long.toString(randomNum));
            findViewById(R.id.layoutLinear).setVisibility(LinearLayout.VISIBLE);

        }
    }

    public void copy(View v){
        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        TextView t;
        if(v.getId() == R.id.btnCopyPass){
            t = findViewById(R.id.txtGenedPass);
        }else
            t = findViewById(R.id.txtAnswer);
        clipboard.setText(t.getText().toString());
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.copied), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.abc_popup_enter, R.anim.alpha);
    }
}
