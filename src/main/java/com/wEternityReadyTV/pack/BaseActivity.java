package com.wEternityReadyTV.pack;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.splashscreen.SplashScreen;
import androidx.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        //setTheme(R.style.AppTheme);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Integer colorPosition = preferences.getInt(ThemeModel.COLOR_KEY, 0);
        ThemeModel themeModel = ThemeModel.get(colorPosition);

        getWindow().setStatusBarColor(themeModel.STATUS_BAR_COLOR);

        listener = (sharedPreferences, key) -> {
            if (key.equals(ThemeModel.COLOR_KEY)){
                Integer position = sharedPreferences.getInt(ThemeModel.COLOR_KEY, 0);
                ThemeModel themeModel1 = ThemeModel.get(position);

                getWindow().setStatusBarColor(themeModel1.STATUS_BAR_COLOR);
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }
}
