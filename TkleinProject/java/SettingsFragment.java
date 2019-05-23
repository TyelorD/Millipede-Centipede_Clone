package com.klein.tyelor.tkleinmillipede;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int TIMER_UPDATE = 33;


    public SettingsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();

        if(preferences != null) {
            onSharedPreferenceChanged(preferences, getString(R.string.prefs_SnakeSegs));
            onSharedPreferenceChanged(preferences, getString(R.string.prefs_NumRocks));
            onSharedPreferenceChanged(preferences, getString(R.string.prefs_NumLives));
            onSharedPreferenceChanged(preferences, getString(R.string.prefs_SnakeSpeed));
            onSharedPreferenceChanged(preferences, getString(R.string.prefs_PlaySounds));
            onSharedPreferenceChanged(preferences, getString(R.string.prefs_SoundVolume));
            onSharedPreferenceChanged(preferences, getString(R.string.prefs_SnakeNum));
            onSharedPreferenceChanged(preferences, getString(R.string.prefs_PowerUpsOn));
            onSharedPreferenceChanged(preferences, getString(R.string.prefs_PowerUpsSpeed));
            onSharedPreferenceChanged(preferences, getString(R.string.prefs_ResetOnDeath));
            onSharedPreferenceChanged(preferences, getString(R.string.prefs_PlayerSpeed));
        }
    }

    @Override
    public void onResume() {
        super.onResume() ;
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
        Preference preference = findPreference(key);

        if(key.equals(getString(R.string.prefs_SnakeSegs))) {
            preference.setSummary(sharedPrefs.getInt(getString(R.string.prefs_SnakeSegs), 10) + "");

        } else if(key.equals(getString(R.string.prefs_NumRocks))) {
            preference.setSummary(sharedPrefs.getInt(getString(R.string.prefs_NumRocks), 15) + "");

        } else if(key.equals(getString(R.string.prefs_NumLives))) {
            preference.setSummary(sharedPrefs.getInt(getString(R.string.prefs_NumLives), 3) + "");

        } else if(key.equals(getString(R.string.prefs_PlaySounds))) {
            boolean soundsOn = sharedPrefs.getBoolean(getString(R.string.prefs_PlaySounds), true);

            Preference volumePref = findPreference(getString(R.string.prefs_SoundVolume));
            volumePref.setEnabled(soundsOn);

            if(soundsOn)
                preference.setSummary("Sounds are on");
            else
                preference.setSummary("Sounds are off");

        } else if(key.equals(getString(R.string.prefs_SoundVolume))) {
            preference.setSummary(sharedPrefs.getInt(getString(R.string.prefs_SoundVolume), 50) + "%");

        } else if(key.equals(getString(R.string.prefs_SnakeSpeed))) {
            int temp = sharedPrefs.getInt(getString(R.string.prefs_SnakeSpeed), 5);
            preference.setSummary(temp + " ~" + (temp * TIMER_UPDATE) + "ms");

        } else if(key.equals(getString(R.string.prefs_SnakeNum))) {
            if(sharedPrefs.getBoolean(getString(R.string.prefs_SnakeNum), false))
                preference.setSummary("Is this even beatable?!");
            else
                preference.setSummary("No fun for you...");

        } else if(key.equals(getString(R.string.prefs_PowerUpsOn))) {
            boolean powerUpsOn = sharedPrefs.getBoolean(getString(R.string.prefs_PowerUpsOn), true);

            Preference speedPref = findPreference(getString(R.string.prefs_PowerUpsSpeed));
            speedPref.setEnabled(powerUpsOn);

            if(powerUpsOn)
                preference.setSummary("Lives will fall from the heavens!");
            else
                preference.setSummary("No 1-ups for you...");

        } else if(key.equals(getString(R.string.prefs_PowerUpsSpeed))) {
            int temp = sharedPrefs.getInt(getString(R.string.prefs_PowerUpsSpeed), 5);
            preference.setSummary(temp + " ~" + (temp * TIMER_UPDATE) + "ms");

        } else if(key.equals(getString(R.string.prefs_ResetOnDeath))) {
            if(sharedPrefs.getBoolean(getString(R.string.prefs_ResetOnDeath), true))
                preference.setSummary("Yes pause and reset when I die");
            else
                preference.setSummary("No play a little more like the real one");

        } else if(key.equals(getString(R.string.prefs_PlayerSpeed))) {
            preference.setSummary("~" + sharedPrefs.getInt(getString(R.string.prefs_PlayerSpeed), 85) + "ms");

        } else {
            preference.setSummary("Preference not found");
        }
    }
}
