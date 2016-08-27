package com.sr.pedatou.others;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by SR on 2016/8/17.
 */
public class MyLinearLayoutManager extends LinearLayoutManager {

    public MyLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }
}
