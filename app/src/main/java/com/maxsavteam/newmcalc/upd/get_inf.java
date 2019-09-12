package com.maxsavteam.newmcalc.upd;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.view.View;
import android.widget.Toast;

import com.maxsavteam.newmcalc.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class get_inf extends View {
	Context con;
	UPDChecker updChecker = null;

	public get_inf(Context context) {
		super(context);
		con = context;
		if(updChecker == null)
			updChecker = new UPDChecker(context);

	}
	String p, type, output;
	boolean dev;
	download dow = new download();
	public void run(String path,String output_file, String typ, boolean isdev){
		p = path;
		type = typ;
		output = output_file;
		dev = isdev;
		try{
			dow.execute();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/*protected void downloader() throws IOException {
		File f = new File(Environment.getExternalStorageDirectory() + "/" + output);
		if(type.equals("tc")){
			type = "tc";
		}
		if(!f.exists()){
			f.createNewFile();
		}
		URL url = new URL("https://max2k18.github.io/maxsavteam.github.io/apk/" + p);
		HttpURLConnection c = (HttpURLConnection) url.openConnection();
		c.setRequestMethod("GET");
		c.connect();
		FileOutputStream fos = new FileOutputStream(f);
		InputStream is = c.getInputStream();
		byte[] buffer = new byte[1024];
		int len1 = 0;
		while(len1 != -1){
			fos.write(buffer, 0, len1);
			len1 = is.read(buffer);
		}
		c.disconnect();
		fos.close();
		is.close();
		postDownload();

	}
	protected void postDownload(){
		Intent in = new Intent(BuildConfig.APPLICATION_ID + ".GOTTEN");
		if(type.equals("tc")){
			in.putExtra("qwerty", 0);
		}
		in.putExtra("type", type);
		in.putExtra("output", output);
		updChecker.check_versions(type, output);
		con.sendBroadcast(in);
	}*/

	private class download extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			Intent in = new Intent(BuildConfig.APPLICATION_ID + ".GOTTEN");
			in.putExtra("type", type);
			in.putExtra("output", output);

			updChecker.check_versions(type, output);
			dow.cancel(true);
			con.sendBroadcast(in);
		}

		@Override
		protected Void doInBackground(Void... voids) {
			try{
				File f = new File(Environment.getExternalStorageDirectory() + "/" + output);
				if(!f.exists()){
					f.createNewFile();
				}
				URL url = new URL("https://max2k18.github.io/maxsavteam.github.io/apk/" + p);
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				c.setRequestMethod("GET");
				c.connect();
				FileOutputStream fos = new FileOutputStream(f);
				InputStream is = c.getInputStream();
				byte[] buffer = new byte[1024];
				int len1 = 0;
				while(len1 != -1){
					fos.write(buffer, 0, len1);
					len1 = is.read(buffer);
				}
				c.disconnect();
				fos.close();
				is.close();
			}catch (Exception e){
				e.printStackTrace();
			}


			return null;
		}
	}
}
