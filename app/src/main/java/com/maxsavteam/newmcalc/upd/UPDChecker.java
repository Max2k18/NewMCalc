package com.maxsavteam.newmcalc.upd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;

import com.maxsavteam.newmcalc.BuildConfig;
import com.maxsavteam.newmcalc.consts.Names;
import com.maxsavteam.newmcalc.files.FileWR;

import java.io.File;
import java.net.Inet4Address;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UPDChecker extends View {
	Context context;
	static boolean working = false;
	SharedPreferences sp;
	Timer timer = null;
	FileWR wr = new FileWR();

	public UPDChecker(Context con){
		super(con);
		context = con;
		sp = PreferenceManager.getDefaultSharedPreferences(con.getApplicationContext());
		gl.stable = new VersionInfo();
		gl.tc = new VersionInfo();
	}

	public boolean is_working(){
		return working;
	}

	public void start(int period_in_minutes, int delay, SharedPreferences sharedPreferences){
		working = true;
		sp = sharedPreferences;
		timer = new Timer();
		timer.schedule(new checker(), delay, period_in_minutes * 60 * 1000);
	}

	static class VersionInfo{
		String version_name = "";
		int version_code = 0;
		int min_api = 0;
		int bytes = 0;
	}
	public static class gl{
		static VersionInfo stable;
		static VersionInfo tc;
	}

	protected void send_broadcast(String type, VersionInfo v){
		if(Build.VERSION.SDK_INT < v.min_api)
			return;
		Intent in = new Intent(BuildConfig.APPLICATION_ID + ".VERSIONS_CHECKED");
		in.putExtra("type", type)
				.putExtra("version", v.version_name)
				.putExtra("code", v.version_code)
				.putExtra("bytes", v.bytes);
		context.sendBroadcast(in);
	}
	protected VersionInfo readContent(){
		VersionInfo v = new VersionInfo();
		String content = sp.getString("stable_content", "");
		int i = 0;
		while (i < content.length() && content.charAt(i) != ';'){
			v.version_name += content.charAt(i);
			i++;
		}
		i++;
		while(i < content.length() && content.charAt(i) != ';'){
			v.version_code = v.version_code * 10 + Integer.valueOf(Character.toString(content.charAt(i)));
			i++;
		}
		i++;
		while(i < content.length() && content.charAt(i) != ';'){
			v.min_api = v.min_api * 10 + Integer.valueOf(Character.toString(content.charAt(i)));
			i++;
		}
		i++;
		while (i < content.length() && content.charAt(i) != ';'){
			v.bytes = v.bytes * 10 + Integer.valueOf(Character.toString(content.charAt(i)));
			i++;
		}
		return v;
	}

	protected void compare(){
		gl.stable = readContent();
		int local_app_ver = BuildConfig.VERSION_CODE;
		if(sp.getBoolean("isdev", false)){
			if(local_app_ver < gl.tc.version_code && gl.stable.version_code < gl.tc.version_code){
				send_broadcast("tc", gl.tc);
				return;
			}
		}
		if(local_app_ver < gl.stable.version_code){
			send_broadcast("simple", gl.stable);
		}
	}

	protected void check_versions(String type, String output_path){
		if(type.equals("simple")){
			try {
				String content = wr.readFrom(output_path);
				sp.edit().putString("stable_content", content).apply();
				if(!sp.getBoolean("isdev", false)){
					compare();
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}else if(type.equals("tc")){
			try {

				String content = wr.readFrom(output_path);
				int i = 0;
				while (i < content.length() && content.charAt(i) != ';'){
					gl.tc.version_name += content.charAt(i);
					i++;
				}
				i++;
				while(i < content.length() && content.charAt(i) != ';'){
					gl.tc.version_code = gl.tc.version_code * 10 + Integer.valueOf(Character.toString(content.charAt(i)));
					i++;
				}
				i++;
				while(i < content.length() && content.charAt(i) != ';'){
					gl.tc.min_api = gl.tc.min_api * 10 + Integer.valueOf(Character.toString(content.charAt(i)));
					i++;
				}
				i++;
				while (i < content.length() && content.charAt(i) != ';'){
					gl.tc.bytes = gl.tc.bytes * 10 + Integer.valueOf(Character.toString(content.charAt(i)));
					i++;
				}
				compare();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public void stop(){
		working = false;
		if(timer != null){
			timer.cancel();
			timer = null;
		}
	}

	Names names = new Names();

	class checker extends TimerTask{
		@Override
		public void run() {
			get_inf getInf = new get_inf(context);
			File f = new File(Environment.getExternalStorageDirectory() + "/MST Files");
			if(!f.isDirectory())
				f.mkdir();
			f = new File(Environment.getExternalStorageDirectory() + "/MST Files/New MCalc");
			if(!f.isDirectory())
				f.mkdir();
			new get_inf(context).run("newmcalc.infm", "MST Files/New MCalc/stable.infm", "simple");
			if(sp.getBoolean("isdev", false)){
				File f1 = new File(Environment.getExternalStorageDirectory() + "/MST Files");
				if(!f1.isDirectory())
					f1.mkdir();
				f1 = new File(f1.getPath() + "/New MCalc");
				if(!f1.isDirectory())
					f1.mkdir();
				BroadcastReceiver br = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						if(intent.getStringExtra("type").equals("simple"))
							new get_inf(context).run("forTesters/newmcalc.infm", "MST Files/New MCalc/tc.infm", "tc");
					}
				};
				context.registerReceiver(br, new IntentFilter(BuildConfig.APPLICATION_ID + ".GOTTEN"));
			}
		}
	}
}
