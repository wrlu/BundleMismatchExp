package com.wrlus.htest.launcher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.wrlus.htest.R;
import com.wrlus.htest.common.UriFileOperator;

import java.util.List;

public class LauncherActivity extends AppCompatActivity {
    private static final String TAG = "HTest.LA";

    private List<Uri> mUriList;
    private LauncherDbManager launcherDbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        mUriList = UriFileOperator.resolveIntentUris(getIntent(), UriFileOperator.RESOLVE_DATA_ONLY);

        if (mUriList != null) {
            for (Uri uri : mUriList) {
                if (uri.toString().endsWith("launcher.db")) {
                    launcherDbManager = new LauncherDbManager(new UriFileOperator(uri));
                    break;
                }
            }
        }

        if (launcherDbManager != null) {
            launcherDbManager.openDatabase(this);
            showAddAndHide();
        }
    }

    private void showAddAndHide() {
        LauncherDbManager.IconLocation selfLocation =
                launcherDbManager.getSelfIconLocation(this);
        LauncherDbManager.IconLocation appLocation =
                launcherDbManager.getIconLocation("哔哩哔哩");
        LauncherDbManager.IconLocation shortcutLocation =
                launcherDbManager.getIconLocation("离线视频");

        Intent shortcutIntent = getShortcutIntent();
        shortcutLocation.screen = 0;

        Handler handler = new Handler();

        handler.postDelayed(() -> {
//            selfLocation.container = LauncherDbManager.IconLocation.CONTAINER_BOTTOM_DOCK;
//            selfLocation.screen = 3;
//            selfLocation.cellX = 3;
//            selfLocation.cellY = 0;
//            launcherDbManager.moveSelfIcon(this, selfLocation);

            appLocation.cellX += 4;
            launcherDbManager.moveSelfIcon(this, appLocation);
            launcherDbManager.commitDatabase(this);
            restartLauncher();
        }, 1000L);

        handler.postDelayed(() -> {
            shortcutLocation.cellY = 5;
            for (shortcutLocation.cellX = 1; shortcutLocation.cellX <= 3; ++shortcutLocation.cellX) {
                launcherDbManager.addIcon("离线视频" +
                        shortcutLocation.cellY + shortcutLocation.cellX, shortcutIntent, shortcutLocation);
            }
            shortcutLocation.cellY = 4;
            for (shortcutLocation.cellX = 1; shortcutLocation.cellX <= 3; ++shortcutLocation.cellX) {
                launcherDbManager.addIcon("离线视频" +
                        shortcutLocation.cellY + shortcutLocation.cellX, shortcutIntent, shortcutLocation);
            }
            launcherDbManager.commitDatabase(this);
            restartLauncher();
        }, 3000L);

        handler.postDelayed(() -> {
            appLocation.cellX -= 4;
            launcherDbManager.moveSelfIcon(this, appLocation);
            launcherDbManager.commitDatabase(this);
            restartLauncher();
        }, 7000L);

        handler.postDelayed(this::cleanUp, 8000L);
    }


    private void showMovingIcon() {
        LauncherDbManager.IconLocation iconLocation =
                launcherDbManager.getSelfIconLocation(this);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            iconLocation.cellX += 1;
            launcherDbManager.moveSelfIcon(this, iconLocation);
            launcherDbManager.commitDatabase(this);
            restartLauncher();
        }, 1000L);

        handler.postDelayed(() -> {
            iconLocation.cellX += 1;
            launcherDbManager.moveSelfIcon(this, iconLocation);
            launcherDbManager.commitDatabase(this);
            restartLauncher();
        }, 3000L);

        handler.postDelayed(() -> {
            iconLocation.cellX -= 1;
            launcherDbManager.moveSelfIcon(this, iconLocation);
            launcherDbManager.commitDatabase(this);
            restartLauncher();
        }, 5000L);

        handler.postDelayed(() -> {
            iconLocation.cellX -= 1;
            launcherDbManager.moveSelfIcon(this, iconLocation);
            launcherDbManager.commitDatabase(this);
            restartLauncher();
        }, 7000L);

        handler.postDelayed(this::cleanUp, 8000L);
    }

    private Intent getShortcutIntent() {
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setPackage("tv.danmaku.bili");
        shortcutIntent.setClassName("tv.danmaku.bili",
                ".MainActivityV2");
        shortcutIntent.addCategory("com.android.launcher3.DEEP_SHORTCUT");
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.putExtra("shortcut_id", "download-list");
        return shortcutIntent;
    }

    private void restartLauncher() {
        startActivity(LauncherDbManager.getRestartLauncherIntent());
    }

    private void cleanUp() {
        launcherDbManager.closeDatabase(this);
        finish();
    }
}