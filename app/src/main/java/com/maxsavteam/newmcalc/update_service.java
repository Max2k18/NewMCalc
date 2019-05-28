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
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
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
import java.util.ArrayList;

class update_service extends View {
	private Context mcon;
	private File apkStorage = null;


	update_service(Context con) {
		super(con);
		mcon = con;
	}

	public static class gl {
		public static boolean tostop = false;
		public static boolean sh_alert = true;
		public static int all = 0, cf = 0;
		public static int bytes = 0;
		public static boolean isupdating = false;
		public static File outputFile = null;
		public static boolean pause = false;
		public static boolean need_sh_progress = false;
		public static String task = "update";
	}

	private String up_ver = "";
	private String up_path = "";
	private Intent res = new Intent();
	private NotificationManager motman;
	private Intent inte;
	private PendingIntent pinte;
	//public boolean tostop = false;
	private SharedPreferences sp;

	public void kill() {
		gl.tostop = true;
		gl.isupdating = false;
		uslogger("updating kill");
		set_to_default();
	}

	public void set_to_default(){
		//gl.isupdating = false;
		gl.cf = 0;
		gl.bytes = 0;
		gl.all = 0;
		set_send_progress(false);
		set_pause(false);
		set_sh_alert(true);
		uslogger("set_to_default. update_service");
	}

	public void set_send_progress(boolean b){
		uslogger("gl.need_sh_progress set to " + b);
		gl.need_sh_progress = b;
	}

	public void set_sh_alert(boolean b) {
		uslogger("gl.sh_alert set to " + b);
		gl.sh_alert = b;
	}

	public int[] get_ints(){
		int[] res = new int[4];
		res[0] = gl.cf;
		res[1] = gl.all;
		res[2] = gl.bytes;
		if(gl.pause){
			res[3] = 1;
		}else
			res[3] = 0;
		uslogger("get_ints requested: cf - " + gl.cf + " all - " + gl.all + " bytes - " + gl.bytes);
		return res;
	}

	public NotificationManager getMotman() {
		return motman;
	}

	public void set_pause(boolean b){
		uslogger("gl.pause set to " + Boolean.toString(b));
		gl.pause = b;
	}

	public boolean isup() {
		uslogger("requested isup() status");
		return gl.isupdating;
	}

	Updater up;

	public void run(String up_path0, String up_version) {
		motman = (NotificationManager) mcon.getSystemService(Context.NOTIFICATION_SERVICE);
		BroadcastReceiver del_not = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				motman.cancelAll();
				kill();
			}
		};
		up = new Updater();
		uslogger("update_service init");
		mcon.registerReceiver(del_not, new IntentFilter(BuildConfig.APPLICATION_ID + ".DELETE_NOT"));
		up_ver = up_version;
		up_path = up_path0;
		inte = new Intent(mcon, catch_service.class);
		//inte.setData(Uri.parse(BuildConfig.APPLICATION_ID + ".NOT_BTN_PRESSED"));
		inte.putExtra("action", "sh_progress");
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
		if (up_path.contains("forTesters")) {
			dbm = db.getReference("dev/bytesCount");
		}
		dbm.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				gl.bytes = dataSnapshot.getValue(Integer.TYPE);
				uslogger("gl.bytes got: " + Integer.toString(gl.bytes));
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
		try {
			Thread.sleep(250);
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean sh_window = sp.getBoolean("sh_up_window", true);
		sp.edit().putInt("notremindfor", 0).apply();
		if (sh_window) {
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
			uslogger("window showed in update_service");
		} else {
			NotificationCompat.Builder build = new NotificationCompat.Builder(mcon);
			Intent in_not = new Intent(mcon, catch_service.class);
			in_not.putExtra("action", "on_prepare_pressed");
			PendingIntent pendingIntent = PendingIntent.getService(mcon, 0, in_not, 0);
			build.setContentTitle("New MCalc is updating...")
					.setSmallIcon(R.drawable.update)
					.setOngoing(true)
					.setProgress(100, 0, true).addAction(R.drawable.stop, getResources().getString(R.string.stop), pendingIntent)
					.setContentText("Preparing...");
			Notification not = build.build();
			motman.notify(1, not);
			uslogger("motman notified");
			//cs.save_in_sp(true);
			gl.isupdating = true;
			new DownloadingTask().execute();
			uslogger("DownloadingTask executed");
		}

	}

	public void install() {
		try{
			Intent promptInstall = new Intent(Intent.ACTION_VIEW)
					.setDataAndType(Uri.parse("file://" + gl.outputFile.getPath()),
							"application/vnd.android.package-archive");
			mcon.startActivity(promptInstall);
		}catch (Exception e){
			Toast.makeText(mcon, e.toString(), Toast.LENGTH_LONG).show();
			uslogger("install fail. " + e.toString());
		}

	}

	private void onfail() {
		motman.cancel(1);
		//cs.save_in_sp(false);
		gl.isupdating = false;
		kill();
		set_to_default();
		res.setAction(BuildConfig.APPLICATION_ID + ".NEWMCALC_UPDATE_FAIL");
		mcon.sendBroadcast(res);
		uslogger("update failed. Broadcast sent");
	}
	
	protected void uslogger(String txt){
		Updater up = new Updater();
		up.logger("update_service\n" + txt);
	}

	private class DownloadingTask extends AsyncTask<Void, Void, Void> {
		String pr = "";


		private void sh(int buffer) {
			gl.all += buffer;
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mcon);
			gl.cf = (gl.all * 100) / gl.bytes;
			//gl.cf = gl.cf / gl.bytes;
			if(!gl.pause)
				builder.setProgress(100, gl.cf, false).setContentText(gl.cf + "%").setOngoing(true).setSmallIcon(R.drawable.update).setContentTitle("New MCalc is updating...")
						.setContentIntent(pinte);
			else{
				builder.setProgress(100, 0, true).setContentText("Pause...").setOngoing(true).setSmallIcon(R.drawable.update).setContentTitle("New MCalc is updating...");
			}
			//motman.cancelAll();
			if (gl.sh_alert)
				motman.notify(1, builder.build());
			else
				motman.cancel(1);
			mcon.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID + ".PROGRESS_CF").putExtra("cf", 0));
		}

		@Override
		protected void onPostExecute(Void result) {

			try {
				if (gl.outputFile != null) {
					//Toast.makeText(mcon.getApplicationContext(), pr, Toast.LENGTH_LONG).show();
					if (!gl.tostop) {
						motman.cancel(1);
						//cs.save_in_sp(false);
						gl.isupdating = false;
						uslogger("updated successful");
						res.setAction(BuildConfig.APPLICATION_ID + ".NEWMCALC_UPDATE_SUC");
						//AlertDialog al = new AlertDialog.Builder(mcon).setMessage(all).setCancelable(true).create();
						//al.show();
						gl.need_sh_progress = false;
						gl.tostop = false;
						mcon.sendBroadcast(res);
					} else {
						motman.cancelAll();
						if (gl.outputFile.exists()) {
							gl.outputFile.delete();
							uslogger("file was deleted");
						}
						gl.tostop = false;
					}
					//kill();
					set_to_default();
					//Toast.makeText(mcon, Boolean.toString(gl.tostop), Toast.LENGTH_SHORT).show();
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

            if(gl.task.equals("update")){
	            try {
		            int permissionStatus = ContextCompat.checkSelfPermission(mcon, Manifest.permission.WRITE_EXTERNAL_STORAGE);

		            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
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
			            } else {
				            success = true;
			            }
			            if (success) {
				            gl.outputFile = new File(apkStorage, "/NewMCalc " + up_ver + ".apk");


				            if (!gl.outputFile.exists()) {
					            success = gl.outputFile.createNewFile();
					            Log.e("clipcodes", "File Created");
				            }
				            if (success) {
					            FileOutputStream fos = new FileOutputStream(gl.outputFile);

					            InputStream is = c.getInputStream();


					            byte[] buffer = new byte[1024];
					            int len1 = 0;
					            while (len1 != -1 && !gl.tostop) {
						            if(gl.need_sh_progress && !gl.pause){
							            int[] array = get_ints();
							            Intent broad = new Intent(BuildConfig.APPLICATION_ID + ".ON_UPDATE"), src = new Intent();
							            broad.putExtra("values", array);
							            mcon.sendBroadcast(broad);
						            }
						            if(!gl.pause){
							            fos.write(buffer, 0, len1);
							            len1 = is.read(buffer);
						            }
						            sh(len1);
                                /*all += is.read(buffer);
                                int cf = (int) all / bytes * 100;
                                builder.setProgress(100, cf, false).setContentText(cf + " of " + 100).setOngoing(true);
                                motman.notify(1, builder.build());*/
					            }
					            //Toast.makeText(mcon.getApplicationContext(), Integer.toString(len1), Toast.LENGTH_LONG).show();

					            fos.close();
					            is.close();

				            }
			            } else {
				            onfail();
			            }
		            }

		            //Path And Filename.type
	            } catch (Exception e) {
		            e.printStackTrace();
		            onfail();
	            }
            }

			return null;
		}
	}
}
