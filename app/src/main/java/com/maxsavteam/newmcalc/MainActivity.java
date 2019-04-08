package com.maxsavteam.newmcalc;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import com.maxsavteam.newmcalc.BuildConfig;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    public static Stack<String> s0 = new Stack<>();
    public static Stack<BigDecimal> s1 = new Stack<>();
    public static Boolean was_error = false;
    View.OnLongClickListener fordel = v -> {
        TextView t = findViewById(R.id.textStr);
        t.setText("");
        TextView ans = findViewById(R.id.txtAns);
        ans.setText("");
        return true;
    };

    public boolean isOtherActivityOpened = false;
    public SharedPreferences sp;
    public Button btn;
    TextView t;

    @Override
    protected void onPause(){
        super.onPause();
        isOtherActivityOpened = true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(sp.getBoolean("toChoose", false)){
            sp.edit().remove("toChoose").apply();
            if(!sp.getString("chooseValue", "").equals(""))
                add_text(sp.getString("chooseValue", ""));
            sp.edit().remove("chooseValue").apply();
        }
        isOtherActivityOpened = false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_settings: {
                Intent resultIntent = new Intent(getApplicationContext(), Updater.class);
                startActivity(resultIntent);
                return  true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btnDelete);
        btn.setOnLongClickListener(fordel);
        TextView ver = findViewById(R.id.lblVer);
        ver.setText(getResources().getString(R.string.version) + " " + BuildConfig.VERSION_NAME);
        Button btn1 = findViewById(R.id.btnCalc);
        Button btn2 = findViewById(R.id.btnZero);
        //FirebaseApp.initializeApp(this);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        btn1.setWidth(btn2.getWidth());
        btn1.setHeight(btn2.getHeight());
        btn.setHeight(btn2.getHeight());
        btn.setWidth(btn2.getWidth());
        btn1 = findViewById(R.id.btnDelAll);
        btn1.setHeight(btn2.getHeight());
        btn1.setWidth(btn2.getWidth());
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("versionCode");
        String vercode = Integer.toString(BuildConfig.VERSION_CODE);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                String value = dataSnapshot.getValue().toString();
                if(value.compareTo(vercode) > 0){
                    if(!isOtherActivityOpened)
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.pleaseupdate),
                                Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseDB", "Cancelled: " + databaseError.toString());
            }
        });
    }

    protected boolean isdigit(char c){
        return c >= '0' && c <= '9';
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
        BigDecimal b = s1.peek();
        s1.pop();
        BigDecimal a = s1.peek();
        BigDecimal ans = s1.peek();
        s1.pop();
        try{
            if(x.equals("+")){
                ans = a.add(b);
            }else if(x.equals("-")){
                ans = a.subtract(b);
            }else if(x.equals("*")){
                ans = a.multiply(b);
            }else if(x.equals("/")) {
                ans = a.divide(b, 10, RoundingMode.HALF_EVEN);
                String answer = ans.toString();
                int len = answer.length();
                while (len > 0 && answer.charAt(len - 1) == '0') {
                    len--;
                    answer = answer.substring(0, len);
                }
                ans = new BigDecimal(answer);
            }
            s1.push(ans);
        }catch (ArithmeticException e){
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

    protected void in_s0(char x){
        Map<String, Integer> priority = new HashMap<>();
        priority.put("(", 0);
        priority.put("-", 1);
        priority.put("+", 1);
        priority.put("/", 2);
        priority.put("*", 2);
        //Toast.makeText(getApplicationContext(), priority.get("(") + " " + priority.get("-"), Toast.LENGTH_SHORT).show();
        if(x == '('){
            s0.push(Character.toString(x));
            return;
        }
        if(s0.empty() && x != '('){
            s0.push(Character.toString(x));
            return;
        }

        if(s0.peek() == "("){
            s0.push(Character.toString(x));
            return;
        }
        if(priority.get(Character.toString(x)) < priority.get(s0.peek()) || priority.get(Character.toString(x)) == priority.get(s0.peek())){
            mult(s0.peek());
            s0.pop();
            in_s0(x);
            return;
        }
        if(priority.get(Character.toString(x)) > priority.get(s0.peek())){
            s0.push(Character.toString(x));
            return;
        }
    }

    protected BigDecimal fact(BigDecimal x){
        if(x.compareTo(new BigDecimal(1)) == 0 || x.signum() == 0){
            return new BigDecimal(1);
        }
        return x.multiply(fact(x.subtract(BigDecimal.valueOf(1))));
    }

    @SuppressLint("SetTextI18n")
    public void calc(String stri){
        s0.clear();
        s1.clear();
        char[] str = new char[stri.length()];
        stri.getChars(0, stri.length(), str, 0);
        String x;
        String s;
        for(int i = 0; i < stri.length(); i++){
            s = Character.toString(str[i]);
            if(s.equals(Character.toString('P'))){
                BigDecimal f = new BigDecimal(3.14159265);
                if(i != 0 && isdigit(stri.charAt(i-1))){
                    in_s0('*');
                }
                s1.push(f.divide(new BigDecimal(1), 8, RoundingMode.HALF_EVEN));
                continue;
            }else if(s.equals(Character.toString('F'))){
                BigDecimal f = new BigDecimal(1.618);
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
                    if(i != 0 && str[i] == '(' && isdigit(str[i-1])){
                        in_s0('*');
                    }

                    in_s0(str[i]);
                }else{
                    while(!s0.empty() && s0.peek() != "("){
                        mult(s0.peek());
                        s0.pop();
                    }
                    if(s0.peek() == "("){
                        s0.pop();
                    }
                }
            }
        }
        while(!s0.isEmpty() && s1.size() >= 2){
            mult(s0.peek());
            s0.pop();
        }
        if(!was_error){
            TextView tans = findViewById(R.id.textStr);
            tans.setText(s1.peek().toString());
        }

    }

    public void equallu(View view){
        TextView txt = findViewById(R.id.txtAns);
        txt.setText("");
        txt.setContentDescription("");
        txt = findViewById(R.id.textStr);
        String stri = txt.getText().toString();
        if(!stri.equals("")){
            was_error = false;
            if(stri.contains(getResources().getString(R.string.multiply)) || stri.contains(getResources().getString(R.string.div))
                    || stri.contains(getResources().getString(R.string.pi)) || stri.contains(getResources().getString(R.string.fi))){
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
                    }
                }
                stri = new String(mas);
            }
            calc(stri);
        }

    }

    protected void check_dot(){
        TextView t = findViewById(R.id.textStr);
        String txt = t.getText().toString();
        int i = txt.length()-1;
        if(!isdigit(txt.charAt(i))){
            return;
        }
        Boolean dot = false;
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

    @SuppressLint("SetTextI18n")
    public void add_text(String btntxt){
        t = findViewById(R.id.textStr);
        String txt = t.getText().toString();
        int len = txt.length();
        if(btntxt.equals("φ") || btntxt.equals("π")){
            if(len == 0){
                t.setText(btntxt);
                return;
            }else{
                if(!isdigit(txt.charAt(len-1))){
                    if(txt.charAt(len-1) != '.') {
                        t.setText(txt + btntxt);
                        return;
                    }
                }else{
                    t.setText(txt + btntxt);
                }
            }
            return;
        }

        if(btntxt.equals(".")){
            if(txt.equals(""))
                t.setText("0.");
            else
            if(!isdigit(txt.charAt(len-1)) && txt.charAt(len-1) != '.')
                t.setText(txt + "0.");
            else
                check_dot();
        }else{
            if(btntxt.equals("!") || btntxt.equals("%")){
                if(!txt.equals("")){
                    if(txt.charAt(len-1) != ')' && !isdigit(txt.charAt(len-1))){
                        txt = txt.substring(0, len-1);
                        t.setText(txt + btntxt);
                    }else{
                        if(isdigit(txt.charAt(len-1)) || txt.charAt(len-1) == ')'){
                            t.setText(txt + btntxt);
                        }
                    }
                }
            }
            if(len > 1 && (btntxt.equals("(") || (txt.charAt(len-1) == ')' && !btntxt.equals(".") && !isdigit(btntxt.toCharArray()[0])))){
                t.setText(txt + btntxt);
            }else{
                if(!txt.equals("")){
                    if(!isdigit(btntxt.charAt(0)) && btntxt != "."){

                        if(!isdigit(txt.charAt(len-1))){
                            txt = txt.substring(0, len-1);
                            t.setText(txt + btntxt);
                        }else{
                            t.setText(txt + btntxt);
                        }
                    }else{
                        if(isdigit(btntxt.charAt(0))){
                            t.setText(txt + btntxt);
                        }
                    }
                }else{
                    if(isdigit(btntxt.charAt(0)) || (btntxt.equals("-"))){
                        t.setText(btntxt);
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
        Intent resultIntent = new Intent(getApplicationContext(), chooseactions.class);
        startActivity(resultIntent);
    }

    public void onClick(View v){
        btn = findViewById(v.getId());
        t = findViewById(R.id.textStr);
        String txt = t.getText().toString();
        String btntxt = btn.getText().toString();
        add_text(btntxt);
    }

    public void delall(View v){
        TextView t = findViewById(R.id.textStr);
        t.setText("");
        t = findViewById(R.id.txtAns);
        t.setText("");
    }

    public void delSymbol(View v){
        TextView txt = findViewById(R.id.textStr);
        String text = txt.getText().toString();
        if(text.length() != 0){
            text = text.substring(0, text.length()-1);
            txt.setText(text);
        }
        if(text == ""){
            TextView ans = findViewById(R.id.txtAns);
            ans.setText("");
        }
    }
}