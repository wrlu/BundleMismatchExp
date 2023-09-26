package com.wrlus.htest.common;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

public class DexInjector {
    private static final String TAG = "HTest.Inject";

    private final UriFileOperator mUriFileOperator;

    public DexInjector(UriFileOperator uriFileOperator) {
        mUriFileOperator = uriFileOperator;
    }

    public void writeTargetFromAssets(Context context, String dexName) {
        try {
            mUriFileOperator.writeUriFile(context, context.getAssets().open(dexName));
            Log.d(TAG, "Inject dex from assets success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
