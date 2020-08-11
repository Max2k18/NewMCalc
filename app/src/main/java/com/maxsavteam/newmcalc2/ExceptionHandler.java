package com.maxsavteam.newmcalc2;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.maxsavteam.newmcalc2.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

	@Override
	public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
		prepareStacktrace( thread, throwable, CallTags.CALLED_FROM_EXCEPTION_HANDLER );
		previous.uncaughtException( thread, throwable );
	}

	public enum CallTags {
		CALLED_MANUALLY,
		CALLED_FROM_ALERT_DIALOG_TO_SEND_LOG,
		CALLED_FROM_EXCEPTION_HANDLER
	}

	private Thread.UncaughtExceptionHandler previous;

	public ExceptionHandler(){
		previous = Thread.getDefaultUncaughtExceptionHandler();
	}

	private File prepareStacktrace(Thread t, Throwable e, CallTags type) {
		e.printStackTrace();
		PackageInfo mPackageInfo = null;
		try {
			mPackageInfo = MCalcApplication.getInstance().getBaseContext().getPackageManager().getPackageInfo( MCalcApplication.getInstance().getBaseContext().getPackageName(), 0 );
		} catch (PackageManager.NameNotFoundException ex) {
			ex.printStackTrace();
		}

		File file = new File( Utils.getExternalStoragePath().getPath() + "/stacktraces/" );
		if ( !file.exists() ) {
			file.mkdir();
		}
		Date date = new Date( System.currentTimeMillis() );
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss", Locale.ROOT );
		SimpleDateFormat fileFormatter = new SimpleDateFormat( "dd-MM-yyyy_HH:mm:ss", Locale.ROOT );
		String formattedDate = fileFormatter.format( date );
		String suffix = "";
		if(type == CallTags.CALLED_MANUALLY)
			suffix = "-m";
		else if(type == CallTags.CALLED_FROM_ALERT_DIALOG_TO_SEND_LOG)
			suffix = "-m-ad-sl";
		file = new File( file.getPath() + "/stacktrace-" + formattedDate + suffix + ".txt" );

		try {
			file.createNewFile();
		} catch (IOException ignored) {
		}
		StringBuilder report = new StringBuilder();
		report.append( type.toString() ).append( "\n" );
		report.append( "Time: " ).append( simpleDateFormat.format( date ) ).append( "\n" )
				.append( "Thread name: " ).append( t.getName() ).append( "\n" )
				.append( "Thread id: " ).append( t.getId() ).append( "\n" )
				.append( "Thread state: " ).append( t.getState() ).append( "\n" )
				.append( "Package: " ).append( BuildConfig.APPLICATION_ID ).append( "\n" )
				.append( "Manufacturer: " ).append( Build.MANUFACTURER ).append( "\n" )
				.append( "Model: " ).append( Build.MODEL ).append( "\n" )
				.append( "Brand: " ).append( Build.BRAND ).append( "\n" )
				.append( "Android Version: " ).append( Build.VERSION.RELEASE ).append( "\n" )
				.append( "Android SDK: " ).append( Build.VERSION.SDK_INT ).append( "\n" )
				.append( "Version name: " ).append( mPackageInfo.versionName ).append( "\n" )
				.append( "Version code: " ).append( mPackageInfo.versionCode ).append( "\n" );
		printStackTrace( e, report );
		report.append( "Caused by:\n" );
		Throwable cause = e.getCause();
		if ( cause != null ) {
			for (StackTraceElement element : cause.getStackTrace()) {
				report.append( "\tat " ).append( element.toString() ).append( "\n" );
			}
		} else {
			report.append( "\tN/A\n" );
		}
		try {
			FileWriter fr = new FileWriter( file, false );
			fr.write( report.toString() );
			fr.flush();
			fr.close();
		} catch (IOException ignored) {
		}
		return file;
	}

	private void printStackTrace(Throwable t, StringBuilder builder) {
		if ( t == null ) {
			return;
		}
		StackTraceElement[] stackTraceElements = t.getStackTrace();
		builder
				.append( "Exception: " ).append( t.getClass().getName() ).append( "\n" )
				.append( "Message: " ).append( t.getMessage() ).append( "\n" )
				.append( "Stacktrace:\n" );
		for (StackTraceElement stackTraceElement : stackTraceElements) {
			builder.append( "\t" ).append( stackTraceElement.toString() ).append( "\n" );
		}
	}

}
