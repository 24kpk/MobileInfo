package com.mobile.mobileinfo.fragment;



import android.os.Bundle;

import com.mobile.mobilehardware.moreopen.MoreOpenHelper;
import com.mobile.mobileinfo.data.Param;
import com.mobile.mobileinfo.fragment.base.BaseFragment;

import java.util.List;

/**
 * 多开信息
 */
public class MoreOpenFragment extends BaseFragment {

    public static MoreOpenFragment newInstance() {
        Bundle args = new Bundle();
        MoreOpenFragment fragment = new MoreOpenFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public List<Param> addListView() {
        return getListParam(MoreOpenHelper.checkVirtual());
    }


}
