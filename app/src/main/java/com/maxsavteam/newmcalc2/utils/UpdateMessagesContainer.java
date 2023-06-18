package com.maxsavteam.newmcalc2.utils;

import static java.util.Map.entry;

import android.content.Context;
import android.content.SharedPreferences;

import com.maxsavteam.newmcalc2.R;

import java.util.Map;

public class UpdateMessagesContainer {

	private static final Map<String, Integer> releaseMessages = Map.ofEntries(

		entry( "3.5", R.string.release_note_3_5 ),

		entry( "3.2", R.string.release_note_3_2 ),

		entry( "3.0", R.string.release_note_3_0 ),

		entry( "2.8.0", R.string.release_note_2_8 ),
		entry( "2.7.0", R.string.release_note_2_7 ),
		entry( "2.6.0", R.string.release_note_2_6 ),
		entry( "2.4.0", R.string.release_note_2_4 ),
		entry( "2.3.1", R.string.release_note_2_3 ),
		entry( "2.3.0", R.string.release_note_2_3 ),
		entry( "2.1.0", R.string.release_note_2_1 ),
		entry( "2.0.0", R.string.release_note_2_0 )

	);

	private static String getMajorVersion(String version){
		String[] split = version.split( "\\." );
		return split[0] + "." + split[1];
	}

	public static boolean isReleaseNoteExists(String version) {
		return releaseMessages.containsKey( getMajorVersion( version ) );
	}

	public static boolean isReleaseNoteShown(String version) {
		SharedPreferences sp = Utils.getContext().getSharedPreferences( "shown_release_notes", Context.MODE_PRIVATE );
		return sp.getBoolean( getMajorVersion( version ), false );
	}

	public static void markAsShown(String version){
		SharedPreferences sp = Utils.getContext().getSharedPreferences( "shown_release_notes", Context.MODE_PRIVATE );
		sp.edit().putBoolean(getMajorVersion(version), true).apply();
	}

	public static int getStringIdForNote(String version) {
		Integer res = releaseMessages.get( getMajorVersion( version ) );
		if ( res == null ) {
			return R.string.something_went_wrong_when_getting_note;
		}
		return res;
	}

}
