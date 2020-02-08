package com.maxsavteam.newmcalc2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.maxsavteam.newmcalc2.utils.FTPInfo;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
	private Context mContext;
	Thread.UncaughtExceptionHandler previousHandler;
	final String TAG = "UncaughtEHandler";

	UncaughtExceptionHandler(Context context) {
		mContext = context;
		previousHandler = Thread.getDefaultUncaughtExceptionHandler();
	}

	@Override
	public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
		handle( t, e );
	}

	private File getExternalStoragePath(){
		return mContext.getExternalFilesDir( null );
	}

	private void handle(@NonNull Thread t, @NonNull final Throwable e) {
		e.printStackTrace();
		PackageInfo mPackageInfo = null;
		try {
			mPackageInfo = mContext.getPackageManager().getPackageInfo( mContext.getPackageName(), 0 );
		} catch (PackageManager.NameNotFoundException ex) {
			ex.printStackTrace();
		}

		File file = new File( getExternalStoragePath().getPath() + "/stacktraces/" );
		if(!file.exists())
			file.mkdir();
		Date date = new Date( System.currentTimeMillis() );
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss", Locale.ROOT );
		SimpleDateFormat fileFormatter = new SimpleDateFormat( "dd-MM-yyyy_HH:mm:ss", Locale.ROOT );
		String formattedDate = fileFormatter.format( date );
		file = new File( file.getPath() + "/stacktrace-" + formattedDate + ".trace" );

		try {
			file.createNewFile();
		}catch (IOException ignored){
		}
		StringBuilder report = new StringBuilder(  );
		report.append( "Time: " ).append( simpleDateFormat.format( date ) ).append( "\n" )
				.append( "Thread name: " ).append( t.getName() ).append( "\n" )
				.append( "Thread id: " ).append( t.getId() ).append( "\n" )
				.append( "Thread state: " ).append( t.getState() ).append( "\n" );
		if(mPackageInfo != null) {
			report.append( "Version name: " ).append( mPackageInfo.versionName ).append( "\n" )
					.append( "Version code: " ).append( mPackageInfo.versionCode ).append( "\n" );
		}else{
			report.append( "Package info N/A\n" );
		}
		printStackTrace( e, report );
		report.append( "Caused by:\n" );
		if(e.getCause() != null) {
			for (StackTraceElement element : e.getCause().getStackTrace()) {
				report.append( "\tat " ).append( element.toString() ).append( "\n" );
			}
		}else{
			report.append( "getCause() returned null\n" );
		}
		try {
			FileWriter fr = new FileWriter( file, false );
			fr.write( report.toString() );
			fr.flush();
			fr.close();
		}catch (Exception ignored){
		}
		String id = generateUniqueId();
		String url = "/NewMCalcReports/" + id + "_" + file.getName() + ".txt";
		final boolean[] resultReturned = { false };
		BroadcastReceiver br = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getBooleanExtra( "result", false )){
					Crashlytics.setString( "report_url", "http://maxsavteamreports.000webhostapp.com" + url );
					Log.e( TAG, "upload successful" );
				}else{
					Crashlytics.setString( "report_url", "failed1" );
					Log.e( TAG, "upload failed" );
				}
				resultReturned[ 0 ] = true;
				previousHandler.uncaughtException( t, e );
			}
		};
		mContext.registerReceiver( br, new IntentFilter( BuildConfig.APPLICATION_ID + ".RESULT_" ) );
		uploadStackTrace( file, url );
		/*AlarmManager alarmManager = (AlarmManager) mContext.getSystemService( Context.ALARM_SERVICE );
		Intent intent = new Intent( mContext, ErrorHandlerActivity.class )
				.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK )
				.putExtra( "path", file.getAbsolutePath() );
		PendingIntent pendingIntent = PendingIntent.getActivity( mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT );
		alarmManager.set( AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent );*/
		while( true ){
			if( isResultReturned[0]){
				break;
			}
		}
		if(mResult[0]){
			Crashlytics.setString( "report_url", "http://maxsavteamreports.000webhostapp.com" + url );
			Log.e( TAG, "upload successful" );
		}else{
			Crashlytics.setString( "report_url", "failed1" );
			Log.e( TAG, "upload failed" );
		}
		previousHandler.uncaughtException( t, e );

	}

	boolean[] isResultReturned = new boolean[]{false};
	boolean[] mResult = new boolean[]{false};

	private String generateUniqueId(){
		String id = "";
		ArrayList<Character> symbols = new ArrayList<>();
		for(int i = 'a'; i <= 'z'; i++){
			symbols.add((char) i);
		}
		for(int i = 'A'; i <= 'Z'; i++){
			symbols.add((char) i);
		}
		for(int i = '0'; i <= '9'; i++){
			symbols.add((char) i);
		}
		Collections.shuffle(symbols);

		for(int i = 0; i < 20; i++){
			int pos = ThreadLocalRandom.current().nextInt(0, symbols.size());
			id = String.format("%s%c", id, symbols.get(pos));
		}

		return id;
	}

	private void uploadStackTrace(File file, String url){
		final boolean[] result = new boolean[]{false};
		new Thread( new Runnable() {
			@Override
			public void run() {
				Log.e( TAG, "thread started" );
				try {
					final FTPClient ftpClient = new FTPClient();
					ftpClient.connect( FTPInfo.HOST_NAME, 21 );
					if(ftpClient.login( FTPInfo.USR_NAME, FTPInfo.PASSWORD )){
						ftpClient.enterLocalPassiveMode();
						FileInputStream in = new FileInputStream( file );
						ftpClient.setFileType( FTP.ASCII_FILE_TYPE );
						result[ 0 ] = ftpClient.storeFile( url, in );

						Log.e(TAG, "result = " + result[0]);
						in.close();
						ftpClient.logout();
						ftpClient.disconnect();
					}else{
						Log.e( TAG, "login was not successful" );
					}
				}catch (Exception e){
					Crashlytics.setString( "upload failed reason in subthread", e.toString() );
					Log.e( TAG, "reason in subthread; " + e.toString() );
				}
				/*Intent i = new Intent( BuildConfig.APPLICATION_ID + ".RESULT_" );
				i.putExtra( "result", result[0] );
				mContext.sendBroadcast( i );*/
				mResult[0] = result[0];
				isResultReturned[0] = true;
			}
		} ).start();
		//return result[0];
	}

	private void printStackTrace(Throwable t, StringBuilder builder){
		if(t == null)
			return;
		StackTraceElement[] stackTraceElements = t.getStackTrace();
		builder
				.append( "Exception: " ).append( t.getClass().getName() ).append( "\n" )
				.append( "Message: " ).append( t.getMessage() ).append( "\n" )
				.append( "Stacktrace:\n" );
		for(StackTraceElement stackTraceElement : stackTraceElements){
			builder.append( "\t" ).append( stackTraceElement.toString() ).append( "\n" );
		}
	}
}
