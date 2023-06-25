package com.maxsavteam.newmcalc2.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.maxsavteam.newmcalc2.App;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.utils.Utils;

public class SettingsPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState, @Nullable @org.jetbrains.annotations.Nullable String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, null);
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (preference instanceof EditTextPreference) {
            DialogFragment f = EditTextPreferenceDialogFragment.newInstance(preference.getKey());
            f.setTargetFragment(this, 0);
            f.show(getParentFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG"); // copied from PreferenceFragmentCompat#DIALOG_FRAGMENT_TAG
            return;
        }
        super.onDisplayPreferenceDialog(preference);
    }

    public void registerOnPreferenceChangedListener(Preference.OnPreferenceChangeListener onPreferenceChangedListener) {
        int preferenceCount = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = getPreferenceScreen().getPreference(i);
            preference.setOnPreferenceChangeListener(onPreferenceChangedListener);
        }
    }

    public static class EditTextPreferenceDialogFragment extends EditTextPreferenceDialogFragmentCompat {

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            if (dialog instanceof AlertDialog) {
                Utils.recolorButtons((AlertDialog) dialog, App.getInstance());
            }
            return dialog;
        }

        @NonNull
        public static EditTextPreferenceDialogFragment newInstance(String key) {
            final EditTextPreferenceDialogFragment
                    fragment = new EditTextPreferenceDialogFragment();
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            return fragment;
        }

    }

}
