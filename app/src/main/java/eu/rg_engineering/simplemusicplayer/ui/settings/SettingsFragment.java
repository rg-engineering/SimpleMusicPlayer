package eu.rg_engineering.simplemusicplayer.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import eu.rg_engineering.simplemusicplayer.R;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int SELECT_IMAGE = 1;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        CheckBoxPreference usePlexServer = findPreference("usePlexServer");

        if(usePlexServer.isChecked()) {
            EditTextPreference pref_plexserverip = findPreference("plex_server_ip");
            if (pref_plexserverip != null) {
                pref_plexserverip.setVisible(true);
            }
            EditTextPreference pref_plexserverport = findPreference("plex_server_port");
            if (pref_plexserverport != null) {
                pref_plexserverport.setVisible(true);
            }
            EditTextPreference pref_plexservertoken = findPreference("plex_server_token");
            if (pref_plexservertoken != null) {
                pref_plexservertoken.setVisible(true);
            }
            Preference pref_plexservertokenhint = findPreference("GetHint");
            if (pref_plexservertokenhint != null) {
                pref_plexservertokenhint.setVisible(true);
            }
            EditTextPreference pref_plexserverlibid = findPreference("plex_server_libid");
            if (pref_plexserverlibid != null) {
                pref_plexserverlibid.setVisible(true);
            }
        }




        Preference backgroundImageFilePicker = (Preference) findPreference("BackgroundImage");
        backgroundImageFilePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);

                return true;
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {

                Uri selectedImageURI = data.getData();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("BackgroundImage", selectedImageURI.toString());
                editor.commit();
            }
        }
    }
}