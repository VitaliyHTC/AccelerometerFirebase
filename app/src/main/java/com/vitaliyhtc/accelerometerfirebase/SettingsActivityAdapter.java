package com.vitaliyhtc.accelerometerfirebase;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsActivityAdapter extends PreferenceActivity {
    private AppCompatDelegate appCompatDelegate;

    private AppCompatDelegate getAppCompactDelegate() {
        if (this.appCompatDelegate == null) {
            this.appCompatDelegate = AppCompatDelegate.create(this, null);
        }
        return this.appCompatDelegate;
    }

    public ActionBar getSupportActionBar1() {
        return getAppCompactDelegate().getSupportActionBar();
    }

    @Override
    public void addContentView(View paramView, ViewGroup.LayoutParams paramLayoutParams) {
        getAppCompactDelegate().addContentView(paramView, paramLayoutParams);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return getAppCompactDelegate().getMenuInflater();
    }

    @Override
    public void invalidateOptionsMenu() {
        getAppCompactDelegate().invalidateOptionsMenu();
    }

    @Override
    public void onConfigurationChanged(Configuration paramConfiguration) {
        super.onConfigurationChanged(paramConfiguration);
        getAppCompactDelegate().onConfigurationChanged(paramConfiguration);
    }

    @Override
    protected void onCreate(Bundle paramBundle) {
        getAppCompactDelegate().installViewFactory();
        getAppCompactDelegate().onCreate(paramBundle);
        super.onCreate(paramBundle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getAppCompactDelegate().onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle paramBundle) {
        super.onPostCreate(paramBundle);
        getAppCompactDelegate().onPostCreate(paramBundle);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getAppCompactDelegate().onPostResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getAppCompactDelegate().onStop();
    }

    @Override
    protected void onTitleChanged(CharSequence paramCharSequence, int paramInt) {
        super.onTitleChanged(paramCharSequence, paramInt);
        getAppCompactDelegate().setTitle(paramCharSequence);
    }

    @Override
    public void setContentView(int paramInt) {
        getAppCompactDelegate().setContentView(paramInt);
    }

    @Override
    public void setContentView(View paramView) {
        getAppCompactDelegate().setContentView(paramView);
    }

    @Override
    public void setContentView(View paramView, ViewGroup.LayoutParams paramLayoutParams) {
        getAppCompactDelegate().setContentView(paramView, paramLayoutParams);
    }

}
