package com.iteye.weimingtom.jkanji;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.iteye.weimingtom.jkanji.R;

public class DictPreference extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.dict_preference);
    }
}
