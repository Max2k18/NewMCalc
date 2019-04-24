package com.maxsavteam.newmcalc;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.lang.Boolean;

public class Updater extends AppCompatActivity {

    public StorageReference mStorageRef;
    Boolean downloading = false;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference("versionCode");
    DatabaseReference refCount = db.getReference("version");
    File localFile;
    StorageReference riversRef;
    String file_url_path = "http://maxsavteam.tk/apk/NewMCalc.apk";

    SharedPreferences sp;

    String newversion;
    File outputFile = null;


    protected void backPressed(){
        if(downloading){
            Toast.makeText(getApplicationContext(), "Wait for the download to finish...", Toast.LENGTH_LONG).show();
        }else{
            finish();
            overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
        }
    }


    @Override
    public void onBackPressed(){
        backPressed();
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

    String vk = "maksin.colf", insta = "maksin.colf/", facebook = "profile.php?id=100022307565005", tw = "maks_savitsky";

    ValueEventListener list = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            vk = dataSnapshot.getValue(String.class);
            Toast.makeText(getApplicationContext(), vk, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Toast.makeText(getApplicationContext(), "cancelled", Toast.LENGTH_SHORT).show();
        }
    };

    public void social(View v){
        Intent in = new Intent(Intent.ACTION_VIEW);
        if(v.getId() == R.id.imgBtnVk){
            in.setData(Uri.parse("https://vk.com/" + vk));
        }else if(v.getId() == R.id.imgBtnInsta){
            in.setData(Uri.parse("https://instagram.com/" + insta));
        }else if(v.getId() == R.id.imgBtnTw){
            in.setData(Uri.parse("https://twitter.com/" + tw));
        }
        startActivity(in);
    }

    public void clear_history(View v){
        sp.edit().putString("history", "").apply();
        findViewById(R.id.btnClsHistory).setVisibility(View.GONE);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.updater_main);
        getSupportActionBar().setTitle(getResources().getString(R.string.settings));
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
       // StrictMode.enableDefaults();
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

        }
        refCount.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newversion = (String) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                newversion = "";
            }
        });

        if(!sp.getString("history", "").equals("")){
            findViewById(R.id.btnClsHistory).setVisibility(View.VISIBLE);
        }
        Intent action = getIntent();
        if(action.getStringExtra("action").equals("update")){
            setContentView(R.layout.layout_updater);
            downloading = true;
            try{
                Thread.sleep(250);
            }catch(Exception e){
                e.printStackTrace();
            }
            new DownloadingTask().execute();
            return;
        }

        String vercode = Integer.toString(BuildConfig.VERSION_CODE);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                Integer versionMy = Integer.valueOf(vercode);
                Integer versionNew = Integer.valueOf(value);
                if(versionNew > versionMy){
                    LinearLayout l = findViewById(R.id.layoutUpdate);
                    l.setVisibility(LinearLayout.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseDB", "Cancelled: " + databaseError.toString());
            }
        });
    }

    public void installupdate(String path){
        Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse("file://" + path),
                        "application/vnd.android.package-archive");
        startActivity(promptInstall);
    }

    public void update(View v){
        setContentView(R.layout.layout_updater);
        downloading = true;
        try{
            Thread.sleep(250);
        }catch(Exception e){
            e.printStackTrace();
        }
        new DownloadingTask().execute();
    }

    private class DownloadingTask extends AsyncTask<Void, Void, Void> {

        File apkStorage = null;


        @Override
        protected void onPostExecute(Void result) {

            try {
                if (outputFile != null) {
                    //Susses Download
                    setContentView(R.layout.downloaded);
                    Thread.sleep(1000);
                    downloading = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(Updater.this);
                    builder.setTitle(R.string.succesful)
                            .setMessage(getResources().getString(R.string.savedto) + " " + outputFile.getPath())
                            .setCancelable(false)
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog al = builder.create();
                    al.show();
                    installupdate(outputFile.getPath());
                } else {
                    //Failed Download
                    downloading = false;
                    setContentView(R.layout.downloaded);
                    TextView t = findViewById(R.id.textViewDownloaded);
                    t.setText(getResources().getString(R.string.downloaderr));
                    ImageView img = (ImageView)findViewById(R.id.imageView);
                    img.setImageResource(R.drawable.error);
                    t = findViewById(R.id.textView3);
                    t.setVisibility(View.INVISIBLE);
                }
            } catch (Exception e) {
                Toast.makeText(Updater.this, "Download Failed", Toast.LENGTH_SHORT).show();
            }

            super.onPostExecute(result);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            /*SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            sp.edit().putInt("notremindfor", 0).apply();*/

            try {
                int permissionStatus = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if(permissionStatus == PackageManager.PERMISSION_GRANTED){
                    boolean success = false;
                    //That is url file you want to download
                    URL url = new URL("https://maxsavteam.tk/apk/NewMCalc.apk");
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("GET");
                    c.connect();
                    //Toast.makeText(getApplicationContext(), Environment.getExternalStorageDirectory().toString(), Toast.LENGTH_LONG).show();
                    //Creating Path
                    apkStorage = new File(
                            Environment.getExternalStorageDirectory().getPath() + "/"
                                    + "MST files");

                    if (!apkStorage.exists()) {
                        //Create Folder From Path
                        success = apkStorage.mkdir();
                    }else{
                        success = true;
                    }
                    if(success){
                        outputFile = new File(apkStorage, "/NewMCalc " + newversion + ".apk");

                        if (!outputFile.exists()) {
                            success = outputFile.createNewFile();
                            Log.e("clipcodes", "File Created");
                        }
                        if(success){
                            FileOutputStream fos = new FileOutputStream(outputFile);

                            InputStream is = c.getInputStream();


                            byte[] buffer = new byte[1024];
                            int len1 = 0;
                            while ((len1 = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len1);
                            }

                            fos.close();
                            is.close();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Make dir error", Toast.LENGTH_SHORT).show();
                    }
                }

                //Path And Filename.type
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }

            return null;
        }
    }
}
