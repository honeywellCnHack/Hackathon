package com.test.poweroptimizer.Tools;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import com.test.poweroptimizer.R;

import java.util.List;

public class BacklightProfilePreferenceFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    EditTextPreference mEditTextTimeout;
    EditTextPreference mEditTextName;
    EditTextPreference mEditTextLevel;
    ListPreference mTargetPackage;

    private PackageManager mPm;
    String KEY_NAME = "profile_name";
    String KEY_PACKAGE = "profile_package";
    String KEY_LEVEL = "profile_level";
    String KEY_TIMEOUT = "profile_timeout";

    BacklightProfile profile = new BacklightProfile();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(BacklightConst.LOG_TAG, " onCreate ");
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.profile_preferences);

        mEditTextName = ((EditTextPreference) findPreference(KEY_NAME));
        mEditTextName.setOnPreferenceChangeListener(this);

        mEditTextLevel = ((EditTextPreference) findPreference(KEY_LEVEL));
        mEditTextLevel.getEditText().setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        mEditTextLevel.setOnPreferenceChangeListener(this);

        mEditTextTimeout = ((EditTextPreference) findPreference(KEY_TIMEOUT));
        mEditTextTimeout.getEditText().setFilters(new InputFilter[]{new InputFilterMinMax(0, Integer.MAX_VALUE)});
        mEditTextTimeout.setOnPreferenceChangeListener(this);

        mTargetPackage = (ListPreference) findPreference(KEY_PACKAGE);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        mPm = this.getContext().getPackageManager();
        List<PackageInfo> packageInfoList = mPm.getInstalledPackages(PackageManager.GET_ACTIVITIES);

        int packageSum = packageInfoList.size();
        CharSequence[] packageNameList = new CharSequence[packageSum];
        CharSequence[] displayNameList = new CharSequence[packageSum];

        for(int i = 0; i<packageSum; i++) {
            displayNameList[i] = packageInfoList.get(i).packageName;
            packageNameList[i] = packageInfoList.get(i).applicationInfo.processName;
        }

        mTargetPackage.setEntries(displayNameList);
        mTargetPackage.setEntryValues(packageNameList);

        mEditTextName.setOnPreferenceChangeListener(this);
        mEditTextLevel.setOnPreferenceChangeListener(this);
        mEditTextTimeout.setOnPreferenceChangeListener(this);
        mTargetPackage.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        Log.i(BacklightConst.LOG_TAG, " onResume ");
        super.onResume();
    }

    // wifi should use wifimanager to control its property
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String key = preference.getKey();
        if (key == null) return true;

        if (KEY_NAME.equals(key)) {
            profile.profileName = (String)newValue;
            preference.setSummary((String)newValue);
        }
        else if (KEY_PACKAGE.equals(key)) {
            profile.packageName = (String)newValue;
            preference.setSummary((String)newValue);
        }
        else if (KEY_LEVEL.equals(key)) {
            profile.level = Integer.parseInt((String) newValue);
            preference.setSummary((String)newValue+"%");
        }
        else if (KEY_TIMEOUT.equals(key)) {
            profile.timeout = Integer.parseInt((String) newValue);
            preference.setSummary((String)newValue+"s");
        }

        Log.i(BacklightConst.LOG_TAG, "onPreferenceChange " + key + " " + findPreference(key).getTitle().toString() + " " + newValue);

        return true;
    }

    public boolean SaveAndClose() {
        if (profile.profileName == null || profile.profileName.length() == 0)
            return false;

        if (profile.packageName == null || profile.packageName.length() == 0)
            return false;

        ContentValues cv = new ContentValues();
        // replace package with classname
        cv.put(BacklightConst.STRING_PROFILE_NAME, profile.profileName);
        cv.put(BacklightConst.STRING_PN, profile.packageName);
        cv.put(BacklightConst.STRING_BACKLIGHT_LEVEL, profile.level);
        cv.put(BacklightConst.STRING_STATUS, profile.packageName);
        cv.put(BacklightConst.STRING_BACKLIGHT_TIME, profile.timeout);

        this.getContext().getContentResolver().insert(BacklightConst.CONTENT_URI, cv);

        return true;
    }

    public class InputFilterMinMax implements InputFilter {

        private final int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
            Log.i(BacklightConst.LOG_TAG, " min " + Integer.toString(min) + " max " + Integer.toString(max));
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            try {

                Log.i(BacklightConst.LOG_TAG, " dest " + dest.toString() + " source " + source.toString());
                int input = Integer.parseInt(dest.toString()
                        + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {

            Log.i(BacklightConst.LOG_TAG,
                    " min " + Integer.toString(a) + " max " + Integer.toString(b) + " cur " + Integer.toString(c));
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }
}
