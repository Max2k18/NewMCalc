package com.maxsavteam.newmcalc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.maxsavteam.newmcalc.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class update_service extends View {
	private Context mcon;


	update_service(Context con) {
		super(con);
		mcon = con;
	}

	public static class gl {
		static boolean tostop = false;
		static boolean sh_alert = true;
		static int all = 0, cf = 0;
		static int bytes = 0;
		static boolean isupdating = false;
		static File outputFile = null;
		static boolean pause = false;
		static boolean need_sh_progress = false;
		static String task = "update";
		static boolean view_was_created = false;
		static AlertDialog on_mobile;
		static DownloadingTask downloadingTask;
		static NotificationManager notman;
	}

	private String up_ver = "";
	private String up_path = "";
	private Intent res = new Intent();
	//private NotificationManager gl.notman;
	private PendingIntent pinte;
	//public boolean tostop = false;
	private SharedPreferences sp;
	int count_of_not = 0;

	public void kill() {
		gl.outputFile.delete();
		gl.downloadingTask.cancel(true);
	}

	public void set_to_default(){
		//gl.isupdating = false;
		gl.cf = 0;
		gl.bytes = 0;
		gl.all = 0;
		set_send_progress(false);
		set_pause(false);
		set_sh_alert(true);
	}

	public void set_send_progress(boolean b){
		gl.need_sh_progress = b;
	}

	public void set_sh_alert(boolean b) {
		count_of_not = 0;
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
		return res;
	}

	public void set_pause(boolean b){
		//gl.pause = b;
	}

	DownloadingTask downloadingTask;

	public boolean isup() {
		return gl.isupdating;
	}

	public void run(String up_path0, String up_version, int bytes) {
		gl.notman = (NotificationManager) mcon.getSystemService(Context.NOTIFICATION_SERVICE);
		BroadcastReceiver del_not = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				gl.notman.cancelAll();
				kill();
			}
		};
		mcon.registerReceiver(del_not, new IntentFilter(BuildConfig.APPLICATION_ID + ".DELETE_NOT"));
		up_ver = up_version;
		up_path = up_path0;
		gl.bytes = bytes;
		Intent inte = new Intent(mcon, catch_service.class);
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
		boolean darkMode = sp.getBoolean("dark_mode", false);
		/*FirebaseDatabase db = FirebaseDatabase.getInstance();
		DatabaseReference dbm = db.getReference("bytesCount");
		if (up_path.contains("forTesters")) {
			dbm = db.getReference("dev/bytesCount");
		}
		dbm.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				gl.bytes = dataSnapshot.getValue(Integer.TYPE);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
		try {
			Thread.sleep(250);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		boolean sh_window = sp.getBoolean("sh_up_window", true);
		sp.edit().putInt("notremindfor", 0).apply();
		if (sh_window) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mcon);
			builder.setTitle(R.string.confirm).setMessage(R.string.update_in_bg).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					sp.edit().putBoolean("sh_up_window", false).apply();
					run(up_path0, up_version, gl.bytes);
				}
			});
			AlertDialog dl = builder.create();
			if(darkMode)
				dl.getWindow().setBackgroundDrawableResource(R.drawable.grey);
			dl.show();
		} else {
			ConnectivityManager conMan = (ConnectivityManager) mcon.getSystemService(Context.CONNECTIVITY_SERVICE);
			//State wifi = conMan.getNetworkInfo(1).getState();
			State mobile = conMan.getNetworkInfo(0).getState();
			if((mobile == State.CONNECTED || mobile == State.CONNECTING) && sp.getBoolean("show_on_mobile_update", true)) {
				if (!gl.view_was_created) {
					AlertDialog.Builder noticeBuilder = new AlertDialog.Builder(mcon).setMessage(R.string.on_mobil_ntwork_update).setTitle(R.string.confirm);
					LayoutInflater inflater = (LayoutInflater) mcon.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View mv = inflater.inflate(R.layout.on_mobile_update, null);
					CheckBox mcheck = mv.findViewById(R.id.donotremind);
					noticeBuilder.setView(mv).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							if (mcheck.isChecked()) {
								sp.edit().putBoolean("show_on_mobile_update", false).apply();
							}
							run_update();
						}
					}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					gl.on_mobile = noticeBuilder.create();
					gl.view_was_created = true;
					if(darkMode)
						gl.on_mobile.getWindow().setBackgroundDrawableResource(R.drawable.grey);
					gl.on_mobile.show();
				}
			}else{
				run_update();
			}
		}

	}

	protected void run_update(){
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
		gl.notman.notify(1, not);
		//cs.save_in_sp(true);
		gl.isupdating = true;
		gl.downloadingTask = new DownloadingTask();
		gl.downloadingTask.execute();
	}

	public void install() {
		try{
			Intent promptInstall = new Intent(Intent.ACTION_VIEW)
					.setDataAndType(Uri.parse("file://" + gl.outputFile.getPath()),
							"application/vnd.android.package-archive");
			mcon.startActivity(promptInstall);
		}catch (Exception e){
			Toast.makeText(mcon, e.toString(), Toast.LENGTH_LONG).show();
		}

	}

	private void onfail() {
		gl.notman.cancel(1);
		//cs.save_in_sp(false);
		gl.isupdating = false;
		kill();
		set_to_default();
		res.setAction(BuildConfig.APPLICATION_ID + ".NEWMCALC_UPDATE_FAIL");
		mcon.sendBroadcast(res);
	}



	protected class DownloadingTask extends AsyncTask<Void, Void, Void> {
		String pr = "";

		@Override
		protected void onCancelled() {
			set_to_default();
			gl.notman.cancel(1);
			super.onCancelled();
		}

		private void sh(int buffer) {
			gl.all += buffer;
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mcon);
			gl.cf = (gl.all * 100) / gl.bytes;
			//gl.cf = gl.cf / gl.bytes;
			if(!gl.pause) {
				builder.setProgress(100, gl.cf, false).setContentText(gl.cf + "%").setOngoing(true).setSmallIcon(R.drawable.update).setContentTitle("New MCalc is updating...")
						.setContentIntent(pinte);
			}else{
				builder.setProgress(100, 0, true).setContentText("Pause...").setOngoing(true).setSmallIcon(R.drawable.update).setContentTitle("New MCalc is updating...");
			}
			//gl.notman.cancelAll();
			gl.notman.notify(1, builder.build());
			/*if (gl.sh_alert) {
				gl.notman.notify(1, builder.build());
			}else {
				if(count_of_not == 0)
					gl.notman.cancel(1);
				count_of_not++;
			}*/
			mcon.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID + ".PROGRESS_CF").putExtra("cf", 0));
		}

		@Override
		protected void onPostExecute(Void result) {

			try {
				if (gl.outputFile != null) {
					//Toast.makeText(mcon.getApplicationContext(), pr, Toast.LENGTH_LONG).show();
					if (!gl.tostop) {
						gl.notman.cancel(1);
						//cs.save_in_sp(false);
						gl.isupdating = false;
						res.setAction(BuildConfig.APPLICATION_ID + ".NEWMCALC_UPDATE_SUC");
						//AlertDialog al = new AlertDialog.Builder(mcon).setMessage(all).setCancelable(true).create();
						//al.show();
						gl.need_sh_progress = false;
						gl.tostop = false;
						mcon.sendBroadcast(res);
					} else {
						gl.notman.cancelAll();
						if (gl.outputFile.exists()) {
							gl.outputFile.delete();
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

		@SuppressLint("WrongThread")
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
			            File apkStorage = new File(
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
					            	if(isCancelled()){
					            		break;
						            }
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
					            }
					            c.disconnect();
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