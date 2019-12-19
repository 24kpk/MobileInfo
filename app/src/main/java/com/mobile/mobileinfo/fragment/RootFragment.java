package com.mobile.mobileinfo.fragment;



import android.os.Bundle;

import com.mobile.mobilehardware.root.RootHelper;
import com.mobile.mobileinfo.data.Param;
import com.mobile.mobileinfo.fragment.base.BaseFragment;

import java.util.List;

/**
 * ROOT 信息 (是否ROOT)
 */
public class RootFragment extends BaseFragment {

    public static RootFragment newInstance() {
        Bundle args = new Bundle();
        RootFragment fragment = new RootFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public List<Param> addListView() {
        return getListParam("isRoot",RootHelper.mobileRoot());
    }


}
