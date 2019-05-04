package com.maxsavteam.newmcalc;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class update_service extends View{
    private Context mcon;
    private File apkStorage = null;
    private File outputFile = null;

    update_service(Context con){
        super(con);
        mcon = con;
    }


    private String up_ver = "";
    private String up_path = "";
    private Intent res = new Intent();
    private NotificationManager motman;
    public boolean isupdating = false;

    public void run(String up_path0, String up_version){
        up_ver = up_version;
        up_path = up_path0;
        NotificationCompat.Builder build = new NotificationCompat.Builder(mcon);
        build.setContentTitle("New MCalc is updating...").setSmallIcon(R.drawable.update).setOngoing(true).setProgress(1, 0, true);
        Notification not = build.build();
        motman = (NotificationManager) mcon.getSystemService(Context.NOTIFICATION_SERVICE);
        motman.notify(1, not);
        isupdating = true;
        new DownloadingTask().execute();
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
        isupdating = false;
        res.setAction(BuildConfig.APPLICATION_ID + ".NEWMCALC_UPDATE_FAIL");
        mcon.sendBroadcast(res);
    }

    private class DownloadingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void result) {

            try {
                if (outputFile != null) {
                    motman.cancel(1);
                    isupdating = false;
                    res.setAction(BuildConfig.APPLICATION_ID + ".NEWMCALC_UPDATE_SUC");
                    mcon.sendBroadcast(res);
                } else {
                    //Failed Download

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
                            while ((len1 = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len1);
                            }

                            fos.close();
                            is.close();
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
