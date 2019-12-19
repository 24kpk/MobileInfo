package com.mobile.mobileinfo.fragment;



import android.os.Bundle;

import com.mobile.mobilehardware.setting.SettingsHelper;
import com.mobile.mobileinfo.data.Param;
import com.mobile.mobileinfo.fragment.base.BaseFragment;

import java.util.List;

/**
 * Setting信息
 */
public class SettingFragment extends BaseFragment {

    public static SettingFragment newInstance() {
        Bundle args = new Bundle();
        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public List<Param> addListView() {
        return getListParam(SettingsHelper.mobGetMobSettings());
    }


}
