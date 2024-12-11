package com.envmonitor.app;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 3;
    private DeviceSelectionFragment deviceSelectionFragment;
    private GaugesFragment gaugesFragment;
    private SettingsFragment settingsFragment;

    public MainPagerAdapter(FragmentActivity activity) {
        super(activity);
        deviceSelectionFragment = new DeviceSelectionFragment();
        gaugesFragment = new GaugesFragment();
        settingsFragment = new SettingsFragment();
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return deviceSelectionFragment;
            case 1:
                return gaugesFragment;
            case 2:
                return settingsFragment;
            default:
                return deviceSelectionFragment;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public GaugesFragment getGaugesFragment() {
        return gaugesFragment;
    }
}
