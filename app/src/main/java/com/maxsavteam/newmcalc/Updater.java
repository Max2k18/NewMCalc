package com.maxsavteam.newmcalc;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
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
import java.util.concurrent.TimeUnit;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    String newversion;


    protected void backPressed(){
        if(downloading){
            Toast.makeText(getApplicationContext(), "Wait for the download to finish...", Toast.LENGTH_LONG).show();
        }else{
            finish();
        }
    }


    @Override
    public void onBackPressed(){
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.updater_main);
        getSupportActionBar().setTitle(getResources().getString(R.string.settings));
        StrictMode.enableDefaults();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        String vercode = Integer.toString(BuildConfig.VERSION_CODE);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                if(value.compareTo(vercode) > 0){
                    TextView t = findViewById(R.id.txtNothingToShow);
                    t.setVisibility(View.INVISIBLE);
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
        File outputFile = null;

        @Override
        protected void onPostExecute(Void result) {

            try {
                if (outputFile != null) {
                    //Susses Download
                    setContentView(R.layout.downloaded);
                    Thread.sleep(1000);
                    downloading = false;
                    installupdate(outputFile.getPath());
                } else {
                    //Failed Download
                    downloading = false;
                    setContentView(R.layout.downloaded);
                    TextView t = findViewById(R.id.textViewDownloaded);
                    t.setText("Download error!");
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
            try {
                //That is url file you want to download
                URL url = new URL("https://maxsavteam.tk/apk/NewMCalc.apk");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.connect();

                //Creating Path
                apkStorage = new File(
                        Environment.getExternalStorageDirectory() + "/"
                                + "MST files");

                if (!apkStorage.exists()) {
                    //Create Folder From Path
                    apkStorage.mkdir();
                }

                //Path And Filename.type
                outputFile = new File(apkStorage, "NewMCalc " + newversion + ".apk");

                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                    Log.e("clipcodes", "File Created");
                }

                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }

                fos.close();
                is.close();

            } catch (Exception e) {
            }

            return null;
        }
    }
}
