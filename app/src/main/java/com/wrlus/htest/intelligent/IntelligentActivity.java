package com.wrlus.htest.intelligent;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.wrlus.htest.R;
import com.wrlus.htest.common.UriFileOperator;
import com.wrlus.htest.common.DexInjector;

import java.util.List;

public class IntelligentActivity extends AppCompatActivity {
    private static final String TAG = "HTest.IA";
    private List<Uri> mUriList;
    private DexInjector injector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intelligent);

        mUriList = UriFileOperator.resolveIntentUris(getIntent(), UriFileOperator.RESOLVE_ALL);

        if (mUriList != null) {
            for (Uri uri : mUriList) {
                if (uri.toString().contains("MainUtil")) {
                    injector = new DexInjector(new UriFileOperator(uri));
                    break;
                }
            }
        }

        if (injector != null) {
            injector.writeTargetFromAssets(getApplicationContext(), "MainUtil.jar");
        }
    }
}