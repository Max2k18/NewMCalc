package com.maxsavteam.newmcalc2.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.maxsavteam.newmcalc2.R;

import java.util.HashMap;
import java.util.Map;

public class UpdateMessagesContainer {

	private static final Map<String, Integer> releaseMessages = new HashMap<>() {{

		put( "2.1.0", R.string.release_note_2_1 );
		put( "2.0.0", R.string.release_note_2_0 );

	}};

	public static boolean isReleaseNoteExists(String version) {
		return releaseMessages.containsKey( version );
	}

	public static boolean isReleaseNoteShown(String version) {
		SharedPreferences sp = Utils.getContext().getSharedPreferences( "shown_release_notes", Context.MODE_PRIVATE );
		return sp.getBoolean( version, false );
	}

	public static int getStringIdForNote(String version) {
		Integer res = releaseMessages.get( version );
		if ( res == null ) {
			return R.string.something_went_wrong_when_getting_note;
		}
		return res;
	}

}
