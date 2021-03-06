/*
 * Copyright (C) 2012 CyanogenMod
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cnd;

import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;
import android.view.VolumePanel;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class SoundSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "SoundSettings";

    private static final String KEY_VOLUME_OVERLAY = "volume_overlay";
    private static final String KEY_SAFE_HEADSET_RESTORE = "safe_headset_restore";
    private static final String KEY_VOLBTN_MUSIC_CTRL = "volbtn_music_controls";

    private String[] volumeSubNames;

    private final Configuration mCurConfig = new Configuration();

    private ListPreference mVolumeOverlay;
    private CheckBoxPreference mSafeHeadsetRestore;
    private CheckBoxPreference mVolBtnMusicCtrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();

        addPreferencesFromResource(R.xml.sound_settings_rom);

        mVolumeOverlay = (ListPreference) findPreference(KEY_VOLUME_OVERLAY);
        mVolumeOverlay.setOnPreferenceChangeListener(this);

        mSafeHeadsetRestore = (CheckBoxPreference) findPreference(KEY_SAFE_HEADSET_RESTORE);
        mSafeHeadsetRestore.setPersistent(false);
        mSafeHeadsetRestore.setChecked(Settings.System.getInt(resolver,
                Settings.System.SAFE_HEADSET_VOLUME_RESTORE, 1) != 0);

        mVolBtnMusicCtrl = (CheckBoxPreference) findPreference(KEY_VOLBTN_MUSIC_CTRL);
        mVolBtnMusicCtrl.setChecked(Settings.System.getInt(resolver,
                Settings.System.VOLBTN_MUSIC_CONTROLS, 1) != 0);

        volumeSubNames = getResources().getStringArray(R.array.volume_overlay_entries);

    }
    
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setVolumeOverlaySettingValue(String value) {
        // Persist
        int toPersist = -1;
        if (value != null) {
            if (value.equals("single")) {
                toPersist = Settings.System.VOLUME_OVERLAY_SINGLE;
            } else if (value.equals("expandable")) {
                toPersist = Settings.System.VOLUME_OVERLAY_EXPANDABLE;
            } else if (value.equals("expanded")) {
                toPersist = Settings.System.VOLUME_OVERLAY_EXPANDED;
            } else if (value.equals("none")) {
                toPersist = Settings.System.VOLUME_OVERLAY_NONE;
            }
        }
        if (toPersist != -1) {
            Settings.System.putInt(getContentResolver(), Settings.System.MODE_VOLUME_OVERLAY, toPersist);
            if (toPersist < volumeSubNames.length && volumeSubNames[toPersist] != null) {
                mVolumeOverlay.setSummary(volumeSubNames[toPersist]);
            }
            // Fire Intent so that the panel can update
            Intent i = new Intent();
            i.setAction(VolumePanel.ACTION_VOLUME_OVERLAY_CHANGED);
            i.putExtra("state", toPersist);
            ActivityManagerNative.broadcastStickyIntent(i, null);
        }
    }
            
    private String getVolumeOverlaySettingValue() {
        // Load from Settings
        int settingAsInt = Settings.System.getInt(getContentResolver(),Settings.System.MODE_VOLUME_OVERLAY, Settings.System.VOLUME_OVERLAY_SINGLE);
        if (settingAsInt != -1 && settingAsInt < volumeSubNames.length && volumeSubNames[settingAsInt] != null) {
            mVolumeOverlay.setSummary(volumeSubNames[settingAsInt]);
        }
                
        switch (settingAsInt) {
            case Settings.System.VOLUME_OVERLAY_SINGLE :
                return "single";
            case Settings.System.VOLUME_OVERLAY_EXPANDABLE :
                return "expandable";
            case Settings.System.VOLUME_OVERLAY_EXPANDED :
                return "expanded";
            case Settings.System.VOLUME_OVERLAY_NONE :
                return "none";
        }
        if (! getActivity().getResources().getBoolean(com.android.internal.R.bool.config_voice_capable)) {
            mVolumeOverlay.setSummary(volumeSubNames[Settings.System.VOLUME_OVERLAY_EXPANDABLE]);
            return "expandable";
        }
        mVolumeOverlay.setSummary(volumeSubNames[Settings.System.VOLUME_OVERLAY_SINGLE]);
        return "single";
    }

    // updateState in fact updates the UI to reflect the system state
    private void updateState(boolean force) {
        if (getActivity() == null) return;
        ContentResolver resolver = getContentResolver();
                
        mVolumeOverlay.setValue(getVolumeOverlaySettingValue());
        mVolumeOverlay.setSummary(mVolumeOverlay.getEntry());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mSafeHeadsetRestore) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SAFE_HEADSET_VOLUME_RESTORE,
                    mSafeHeadsetRestore.isChecked() ? 1 : 0);

		} else if (preference == mVolBtnMusicCtrl) {
            Settings.System.putInt(getContentResolver(), Settings.System.VOLBTN_MUSIC_CONTROLS,
                    mVolBtnMusicCtrl.isChecked() ? 1 : 0);

        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        
        if (preference == mVolumeOverlay) {
            setVolumeOverlaySettingValue(objValue.toString());
        }
        
        return true;
    }
}
