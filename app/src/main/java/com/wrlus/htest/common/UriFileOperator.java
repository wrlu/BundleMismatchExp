package com.wrlus.htest.common;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class UriFileOperator {
    private static final String TAG = "HTest.Uri";

    private Uri uri;
    public static final int RESOLVE_ALL = 0;
    public static final int RESOLVE_DATA_ONLY = 1;
    public static final int RESOLVE_CLIP_DATA_ONLY = 2;

    public UriFileOperator(Uri uri) {
        this.uri = uri;
    }

    public static List<Uri> resolveIntentUris(Intent intent, int resolveFlags) {
        if (intent == null) {
            return null;
        }
        List<Uri> uriList = new ArrayList<>();
        if (resolveFlags == RESOLVE_ALL || resolveFlags == RESOLVE_DATA_ONLY) {
            Uri uri = intent.getData();
            if (uri != null) {
                uriList.add(uri);
                Log.d(TAG, "Detect URI from data: " + uri);
            }
        }
        if (resolveFlags == RESOLVE_ALL || resolveFlags == RESOLVE_CLIP_DATA_ONLY) {
            ClipData clipData = intent.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); ++i) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    if (uri != null) {
                        uriList.add(uri);
                        Log.d(TAG, "Detect URI from clip data: " + uri);
                    }
                }
            }
        }
        return uriList;
    }

    public void readUriFile(Context context, File destFile) {
        if (!destFile.canWrite()) {
            throw new SecurityException("Dest file is not writable");
        }
        enforceSelfReadUriPermission(context, "Read uri permission not granted");
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            FileOutputStream os = new FileOutputStream(destFile);
            int size;
            byte[] buffer = new byte[1024];
            while ((size = is.read(buffer)) != -1) {
                os.write(buffer, 0, size);
                os.flush();
            }
            os.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeUriFile(Context context, InputStream srcIn) {
        enforceSelfWriteUriPermission(context, "Write uri permission not granted");
        try {
            OutputStream os = context.getContentResolver().openOutputStream(uri);
            int size;
            byte[] buffer = new byte[1024];
            while ((size = srcIn.read(buffer)) != -1) {
                os.write(buffer, 0, size);
                os.flush();
            }
            srcIn.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeUriFile(Context context, File srcFile) {
        if (!srcFile.canRead()) {
            throw new SecurityException("Src file is not readable");
        }
        enforceSelfWriteUriPermission(context, "Write uri permission not granted");
        try {
            OutputStream os = context.getContentResolver().openOutputStream(uri);
            FileInputStream is = new FileInputStream(srcFile);
            int size;
            byte[] buffer = new byte[1024];
            while ((size = is.read(buffer)) != -1) {
                os.write(buffer, 0, size);
                os.flush();
            }
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enforceSelfReadUriPermission(Context context, String message) {
        context.enforceUriPermission(uri, Process.myPid(), Process.myUid(),
                Intent.FLAG_GRANT_READ_URI_PERMISSION, message);
    }

    public void enforceSelfWriteUriPermission(Context context, String message) {
        context.enforceUriPermission(uri, Process.myPid(), Process.myUid(),
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION, message);
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
