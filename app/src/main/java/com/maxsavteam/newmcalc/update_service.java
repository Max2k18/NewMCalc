package com.maxsavteam.newmcalc;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class update_service extends View{
    private Context mcon;
    private File apkStorage = null;
    private File outputFile = null;
    public int bytes = 0;

    update_service(Context con){
        super(con);
        mcon = con;
        isupdating = false;
    }


    private String up_ver = "";
    private String up_path = "";
    private Intent res = new Intent();
    private NotificationManager motman;
    public boolean isupdating;
    private Intent inte;
    private PendingIntent pinte;
    private boolean tostop = false;
    public int all = 0, cf = 0;
    private SharedPreferences sp;
    catch_service cs;

    public void kill(){
        tostop = true;
    }

    public boolean isup(){
        return false;
    }

    public void create_sp(SharedPreferences s){
    	sp = s;
    }

    public void run(String up_path0, String up_version){
        up_ver = up_version;
        up_path = up_path0;
        inte = new Intent(mcon, catch_service.class);
        //inte.setData(Uri.parse(BuildConfig.APPLICATION_ID + ".NOT_BTN_PRESSED"));
        inte.putExtra("action", "NOT_BTN_PRESSED");
        pinte = PendingIntent.getActivity(mcon, 0, inte, PendingIntent.FLAG_UPDATE_CURRENT);
        /*BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                tostop = true;
            }
        };
        mcon.registerReceiver(br, new IntentFilter(BuildConfig.APPLICATION_ID + ".NOT_BTN_PRESSED"));*/
        sp = PreferenceManager.getDefaultSharedPreferences(mcon.getApplicationContext());
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbm = db.getReference("bytesCount");
        if(up_path.contains("forTesters")){
            dbm = db.getReference("dev/bytesCount");
        }
        dbm.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bytes = dataSnapshot.getValue(Integer.TYPE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        try{
            Thread.sleep(250);
        }catch(Exception e){
            e.printStackTrace();
        }
        boolean sh_window = sp.getBoolean("sh_up_window", true);
        sp.edit().putInt("notremindfor", 0).apply();
        if(sh_window){
            AlertDialog.Builder builder = new AlertDialog.Builder(mcon);
            builder.setTitle(R.string.confirm).setMessage(R.string.update_in_bg).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    sp.edit().putBoolean("sh_up_window", false).apply();
                    run(up_path0, up_version);
                }
            });
            AlertDialog dl = builder.create();
            dl.show();
        }else{
            NotificationCompat.Builder build = new NotificationCompat.Builder(mcon);
            build.setContentTitle("New MCalc is updating...").setSmallIcon(R.drawable.update).setOngoing(true).setProgress(100, 0, true).setContentText("Preparing...");
            Notification not = build.build();
            motman = (NotificationManager) mcon.getSystemService(Context.NOTIFICATION_SERVICE);
            motman.notify(1, not);
            //cs.save_in_sp(true);
            new DownloadingTask().execute();
        }

    }

    public String get_path(){
        return outputFile.getPath();
    }

    public void install(){
        Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse("file://" + outputFile.getPath()),
                        "application/vnd.android.package-archive");
        mcon.startActivity(promptInstall);
    }

    private void onfail(){
        motman.cancel(1);
        //cs.save_in_sp(false);
        res.setAction(BuildConfig.APPLICATION_ID + ".NEWMCALC_UPDATE_FAIL");
        mcon.sendBroadcast(res);
    }


    private class DownloadingTask extends AsyncTask<Void, Void, Void> {
        String pr = "";


        private void sh(int buffer){
            all += buffer;
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mcon);
            cf = (int) all * 100;
            cf = cf / bytes;
            builder.setProgress(100, cf, false).setContentText(cf + "% of " + 100 + "%").setOngoing(true).setSmallIcon(R.drawable.update).setContentTitle("New MCalc is updating...");
            //motman.cancelAll();
            motman.notify(1, builder.build());
        }

        @Override
        protected void onPostExecute(Void result) {

            try {
                if (outputFile != null) {
                    //Toast.makeText(mcon.getApplicationContext(), pr, Toast.LENGTH_LONG).show();
                    if(!tostop){
                        motman.cancel(1);
                        //cs.save_in_sp(false);
                        res.setAction(BuildConfig.APPLICATION_ID + ".NEWMCALC_UPDATE_SUC");
                        //AlertDialog al = new AlertDialog.Builder(mcon).setMessage(all).setCancelable(true).create();
                        //al.show();
                        mcon.sendBroadcast(res);
                    }
                } else {
                    //Failed Download
                    onfail();
                }
            } catch (Exception e) {
                onfail();
            }

            super.onPostExecute(result);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            /*SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            sp.edit().putInt("notremindfor", 0).apply();*/

            try {
                int permissionStatus = ContextCompat.checkSelfPermission(mcon, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if(permissionStatus == PackageManager.PERMISSION_GRANTED){
                    boolean success = false;
                    //That is url file you want to download
                    URL url = new URL("https://maxsavteam.tk/apk" + up_path);
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
                        outputFile = new File(apkStorage, "/NewMCalc " + up_ver + ".apk");



                        if (!outputFile.exists()) {
                            success = outputFile.createNewFile();
                            Log.e("clipcodes", "File Created");
                        }
                        if(success){
                            FileOutputStream fos = new FileOutputStream(outputFile);

                            InputStream is = c.getInputStream();


                            byte[] buffer = new byte[1024];
                            int len1 = 0;
                            while (len1 != -1 && !tostop) {
                                fos.write(buffer, 0, len1);
                                len1 = is.read(buffer);
                                sh(len1);
                                /*all += is.read(buffer);
                                int cf = (int) all / bytes * 100;
                                builder.setProgress(100, cf, false).setContentText(cf + " of " + 100).setOngoing(true);
                                motman.notify(1, builder.build());*/
                            }
                            //Toast.makeText(mcon.getApplicationContext(), Integer.toString(len1), Toast.LENGTH_LONG).show();

                            fos.close();
                            is.close();
                            if(tostop){
                                motman.cancelAll();
                                if(outputFile.exists()){
                                    outputFile.delete();
                                }
                            }
                        }
                    }else{
                        onfail();
                    }
                }

                //Path And Filename.type
            } catch (Exception e) {
                e.printStackTrace();
                onfail();
            }

            return null;
        }
    }
}
