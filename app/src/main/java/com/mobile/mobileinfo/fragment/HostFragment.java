package com.mobile.mobileinfo.fragment;


import android.os.Bundle;

import com.mobile.mobileinfo.fragment.base.BaseNetFragment;

import fairy.easy.httpmodel.resource.HttpType;

/**
 * HOST信息
 */
public class HostFragment extends BaseNetFragment {

    public static HostFragment newInstance() {
        Bundle args = new Bundle();
        HostFragment fragment = new HostFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public HttpType getType() {
        return HttpType.HOST;
    }
}
