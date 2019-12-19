package com.mobile.mobileinfo.fragment;


import android.os.Bundle;

import com.mobile.mobilehardware.bluetooth.BluetoothHelper;
import com.mobile.mobileinfo.data.Param;
import com.mobile.mobileinfo.fragment.base.BaseFragment;

import java.util.List;

/**
 * 蓝牙信息
 */
public class BluetoothFragment extends BaseFragment {

    public static BluetoothFragment newInstance() {
        Bundle args = new Bundle();
        BluetoothFragment fragment = new BluetoothFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public List<Param> addListView() {
        return getListParam(BluetoothHelper.mobGetMobBluetooth());
    }


}
