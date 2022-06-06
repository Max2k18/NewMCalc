package com.maxsavteam.newmcalc2.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.maxsavteam.newmcalc2.R;

public class SettingsPreferencesFragment extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState, @Nullable @org.jetbrains.annotations.Nullable String rootKey) {
		setPreferencesFromResource( R.xml.settings_preferences, null );
	}

	public void registerOnPreferenceChangedListener(Preference.OnPreferenceChangeListener onPreferenceChangedListener){
		int preferenceCount = getPreferenceScreen().getPreferenceCount();
		for(int i = 0; i < preferenceCount; i++){
			Preference preference = getPreferenceScreen().getPreference( i );
			preference.setOnPreferenceChangeListener( onPreferenceChangedListener );
		}
	}

}
