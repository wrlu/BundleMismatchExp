package com.wrlus.htest.launcher;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.wrlus.htest.R;
import com.wrlus.htest.common.UriFileOperator;

import java.io.File;
import java.io.IOException;

public class LauncherDbManager {
    private static final String TAG = "HTest.LDB";

    private final UriFileOperator mUriFileOperator;
    private SQLiteDatabase launcherDb;

    private static final String TABLE_FAVORITES = "favorites4x6";
    private static final String COLUMN_ID = "_id" ;
    private static final String[] COLUMNS_FAVORITES = {
            "_id", "title", "intent", "container", "screen",
            "cellX", "cellY", "spanX", "spanY",
            "itemType", "appWidgetId" };
    private static final String SELECTION_TITLE = "title = ?";
    private static final String SELECTION_ID = "_id = ?";
    private static final String TMP_DB_FILENAME = "tmp_launcher.db";

    public static class IconLocation {
        public int container;
        public int screen;
        public int cellX;
        public int cellY;
        public int spanX;
        public int spanY;
        public int itemType;
        public int appWidgetId;

        public static final int CONTAINER_DESKTOP = -100;
        public static final int CONTAINER_BOTTOM_DOCK = -101;
        public static final int CONTAINER_WIDGET = -102;

        public static final int ITEM_TYPE_APP = 0;
        public static final int ITEM_TYPE_FOLDER = 2;
        public static final int ITEM_TYPE_WIDGET = 4;
        public static final int ITEM_TYPE_SHORTCUT = 7;
        public static final int ITEM_TYPE_SERVICE_CARD = 9;
        public static final int ITEM_TYPE_BIG_FOLDER = 10;

        public static String getContainerString(int container) {
            switch (container) {
                case CONTAINER_DESKTOP:
                    return "Desktop (-100)";
                case CONTAINER_BOTTOM_DOCK:
                    return "Bottom dock (-101)";
                case CONTAINER_WIDGET:
                    return "Widget (-102)";
                default:
                    return "Folder ("+container+")";
            }
        }

        public static String getItemTypeString(int itemType) {
            switch (itemType) {
                case ITEM_TYPE_APP:
                    return "App (0)";
                case ITEM_TYPE_FOLDER:
                    return "Folder (2)";
                case ITEM_TYPE_WIDGET:
                    return "Widget (4)";
                case ITEM_TYPE_SHORTCUT:
                    return "Shortcut (7)";
                case ITEM_TYPE_SERVICE_CARD:
                    return "Harmony OS service card (9)";
                case ITEM_TYPE_BIG_FOLDER:
                    return "Harmony OS big folder (10)";
                default:
                    return "Unknown type ("+itemType+")";
            }
        }

        @NonNull
        @Override
        public String toString() {
            return "IconLocation{" +
                    "container=" + getContainerString(container) +
                    ", screen=" + screen +
                    ", cellX=" + cellX +
                    ", cellY=" + cellY +
                    ", spanX=" + spanX +
                    ", spanY=" + spanY +
                    ", itemType=" + getItemTypeString(itemType) +
                    ", appWidgetId=" + appWidgetId +
                    '}';
        }
    }

    public LauncherDbManager(UriFileOperator uriFileOperator) {
        mUriFileOperator = uriFileOperator;
    }

    /**
     * Get a intent to restart huawei launcher.
     * Warning: This operation will let user confused, so please use it carefully.
     *
     * @return restart launcher intent.
     */
    public static Intent getRestartLauncherIntent() {
        Intent intent = new Intent();
        intent.setPackage("com.huawei.android.launcher");
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    public synchronized void openDatabase(Context context) {
        File tmpFile = new File(context.getCacheDir(), TMP_DB_FILENAME);
        try {
            boolean createNewFileStatus = tmpFile.createNewFile();
            Log.d(TAG, "Create tmp db file " + (createNewFileStatus ? "success" : "failed."));
        } catch (IOException e) {
            Log.e(TAG, "Open database temp file failed.");
            e.printStackTrace();
        }
        mUriFileOperator.readUriFile(context, tmpFile);

        launcherDb = SQLiteDatabase.openDatabase(tmpFile.getAbsolutePath(),
                null, SQLiteDatabase.OPEN_READWRITE);
    }

    public synchronized void commitDatabase(Context context) {
        File tmpFile = new File(context.getCacheDir(), TMP_DB_FILENAME);
        if (!tmpFile.exists() || launcherDb == null) {
            Log.e(TAG, "Database not exist.");
            return;
        }
        launcherDb.close();
        mUriFileOperator.writeUriFile(context, tmpFile);
        launcherDb = SQLiteDatabase.openDatabase(tmpFile.getAbsolutePath(),
                null, SQLiteDatabase.OPEN_READWRITE);
    }

    public synchronized void closeDatabase(Context context) {
        File tmpFile = new File(context.getCacheDir(), TMP_DB_FILENAME);
        if (!tmpFile.exists() || launcherDb == null) {
            Log.e(TAG, "Database not exist.");
            return;
        }
        launcherDb.close();
        boolean deleteStatus = tmpFile.delete();
        Log.d(TAG, "Clean tmp db file " + (deleteStatus ? "success" : "failed."));
    }

    public long getMaxId() {
        Cursor cursor = launcherDb.query(TABLE_FAVORITES, new String[] { COLUMN_ID }, null,
                null, null, null, COLUMN_ID + " DESC");
        cursor.moveToFirst();
        long maxId = Integer.parseInt(cursor.getString(
                cursor.getColumnIndexOrThrow("_id")));
        cursor.close();
        return maxId;
    }

    /**
     * Get icon location from launcher database.
     *
     * @param title the application icon title.
     * @return a IconLocation which contains the location on launcher.
     */
    public IconLocation getIconLocation(String title) {
        Cursor cursor = launcherDb.query(TABLE_FAVORITES, COLUMNS_FAVORITES, SELECTION_TITLE,
                new String[] { title }, null, null, null);
        cursor.moveToFirst();
        IconLocation iconLocation = new IconLocation();
        iconLocation.container = Integer.parseInt(cursor.getString(
                cursor.getColumnIndexOrThrow("container")));
        iconLocation.screen = Integer.parseInt(cursor.getString(
                cursor.getColumnIndexOrThrow("screen")));
        iconLocation.cellX = Integer.parseInt(cursor.getString(
                cursor.getColumnIndexOrThrow("cellX")));
        iconLocation.cellY = Integer.parseInt(cursor.getString(
                cursor.getColumnIndexOrThrow("cellY")));
        iconLocation.spanX = Integer.parseInt(cursor.getString(
                cursor.getColumnIndexOrThrow("spanX")));
        iconLocation.spanY = Integer.parseInt(cursor.getString(
                cursor.getColumnIndexOrThrow("spanY")));
        iconLocation.itemType = Integer.parseInt(cursor.getString(
                cursor.getColumnIndexOrThrow("itemType")));
        iconLocation.appWidgetId = Integer.parseInt(cursor.getString(
                cursor.getColumnIndexOrThrow("appWidgetId")));
        cursor.close();
        return iconLocation;
    }

    /**
     * Move any icon location on launcher.
     * Warning: This operation will let user confused, so please use it carefully.
     *
     * @param fromTitle title of the icon which you want to move.
     * @param newIconLocation new icon location on launcher, an invalid location
     *                        will let launcher crashed.
     */
    public void moveIcon(String fromTitle, IconLocation newIconLocation) {
        ContentValues updateValue = new ContentValues();
        updateValue.put("container", newIconLocation.container);
        updateValue.put("screen", newIconLocation.screen);

        updateValue.put("cellX", newIconLocation.cellX);
        updateValue.put("cellY", newIconLocation.cellY);
        updateValue.put("spanX", newIconLocation.spanX);
        updateValue.put("spanY", newIconLocation.spanY);
        updateValue.put("itemType", newIconLocation.itemType);
        updateValue.put("appWidgetId", newIconLocation.appWidgetId);
        int lines = launcherDb.update(TABLE_FAVORITES, updateValue, SELECTION_TITLE,
                new String[] { fromTitle });
        Log.d(TAG, "Update " + lines + " lines");
    }

    public void addIcon(String iconTitle, Intent launchIntent,
                        IconLocation iconLocation) {
        ContentValues insertValue = new ContentValues();
        long maxId = getMaxId();
        Log.d(TAG, "Max id = " + maxId);
        insertValue.put("_id", maxId + 1);
        insertValue.put("title", iconTitle);
        insertValue.put("intent", launchIntent.toUri(0));
        insertValue.put("container", iconLocation.container);
        insertValue.put("screen", iconLocation.screen);
        insertValue.put("cellX", iconLocation.cellX);
        insertValue.put("cellY", iconLocation.cellY);
        insertValue.put("spanX", iconLocation.spanX);
        insertValue.put("spanY", iconLocation.spanY);
        insertValue.put("itemType", iconLocation.itemType);
        insertValue.put("appWidgetId", iconLocation.appWidgetId);
        long rowId = launcherDb.insert(TABLE_FAVORITES, null, insertValue);
        Log.d(TAG, "Insert row id " + rowId);
    }

    public void addIcon(Context context, String iconTitle, String packageName,
                        IconLocation iconLocation) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        addIcon(iconTitle, launchIntent, iconLocation);
    }

    public void removeIcon(long id) {
        int lines = launcherDb.delete(TABLE_FAVORITES, SELECTION_ID,
                new String[] { Long.toString(id) });
        Log.d(TAG, "Delete " + lines + " lines");
    }

    public void removeIcon(String title) {
        int lines = launcherDb.delete(TABLE_FAVORITES, SELECTION_TITLE,
                new String[] { title });
        Log.d(TAG, "Delete " + lines + " lines");
    }

    /**
     * Get icon location of our application from launcher database.
     * @see #getIconLocation(String)
     *
     * @param context application context.
     * @return a IconLocation which contains the location on launcher.
     */
    public IconLocation getSelfIconLocation(Context context) {
        return getIconLocation(context.getString(R.string.app_name));
    }

    /**
     * Move our application icon location on launcher.
     * @see #moveIcon(String, IconLocation)
     *
     * @param context application context.
     * @param newIconLocation new icon location on launcher, an invalid location
     *                        will let launcher crashed.
     */
    public void moveSelfIcon(Context context, IconLocation newIconLocation) {
        moveIcon(context.getString(R.string.app_name), newIconLocation);
    }

    public void addSelfIcon(Context context, IconLocation iconLocation) {
        addIcon(context, context.getString(R.string.app_name),
                context.getPackageName(), iconLocation);
    }

}
