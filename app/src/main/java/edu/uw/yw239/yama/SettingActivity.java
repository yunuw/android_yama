package edu.uw.yw239.yama;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class SettingActivity extends AppCompatActivity {

    public final static String PARENT_ACTIVITY_KEY = "parent_activity_key";
    public final static String PREF_AUTO_REPLY = "pref_auto_reply";
    public final static String PREF_REPLY_CONTENT = "pref_reply_content";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //action bar "back"
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //the FM that moves fragments around
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preference);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

            if (prefs.getBoolean(PREF_AUTO_REPLY, false)) {
                AddEditTextPreference();
            }

            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        private void AddEditTextPreference() {
            EditTextPreference editPref = (EditTextPreference)findPreference(PREF_REPLY_CONTENT);
            if (editPref == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                String autoMessage = prefs.getString(PREF_REPLY_CONTENT, "Please enter the auto reply message");

                PreferenceScreen screen = this.getPreferenceScreen();

                editPref = new EditTextPreference(screen.getContext());
                editPref.setKey(PREF_REPLY_CONTENT);
                editPref.setTitle(R.string.summary_reply_text);
                editPref.setSummary(autoMessage);
                screen.addPreference(editPref);
            }
        }

        private void RemoveTextPreference() {
            EditTextPreference editPref = (EditTextPreference)findPreference(PREF_REPLY_CONTENT);
            if (editPref != null) {
                PreferenceScreen screen = this.getPreferenceScreen();
                screen.removePreference(editPref);
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(PREF_AUTO_REPLY))
            {
                if(sharedPreferences.getBoolean(key, false)) {
                    AddEditTextPreference();
                } else {
                    RemoveTextPreference();
                }
            }
            if (key.equals(PREF_REPLY_CONTENT)) {
                PreferenceScreen screen = this.getPreferenceScreen();
                EditTextPreference editPref = (EditTextPreference)findPreference(key);
                String autoMessage = sharedPreferences.getString(key, "Please enter the auto reply message");
                editPref.setSummary(autoMessage);
            }
        }
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    @Override
    public Intent getParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    private Intent getParentActivityIntentImpl() {
        String parent = getIntent().getExtras().getString(PARENT_ACTIVITY_KEY);

        Intent i = null;

        // Here you need to do some logic to determine from which Activity you came.
        // example: you could pass a variable through your Intent extras and check that.
        if (parent.equals(ReadMessages.ACTIVITY_NAME)) {
            i = new Intent(this, ReadMessages.class);
            // set any flags or extras that you need.
            // If you are reusing the previous Activity (i.e. bringing it to the top
            // without re-creating a new instance) set these flags:
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            i = new Intent(this, ComposeMessages.class);
            // same comments as above
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        return i;
    }
}
