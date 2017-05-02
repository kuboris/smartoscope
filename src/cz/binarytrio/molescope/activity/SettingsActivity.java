package cz.binarytrio.molescope.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import cz.binarytrio.molescope.R;

/**
 * Created by nicko on 4/29/17
 */

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
