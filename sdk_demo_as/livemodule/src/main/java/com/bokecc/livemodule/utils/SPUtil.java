package com.bokecc.livemodule.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.bokecc.sdk.mobile.live.logging.ELog;

/**
 * sp 工具类
 */
public class SPUtil {
    private static final String TAG = "SPUtil";
    private static final String SP_NAME = "com_bokecc_sp";
    private SharedPreferences mPreferences;
    private static SPUtil instance;

    public static SPUtil getInstance() {
        if (instance == null) {
            synchronized (SPUtil.class) {
                if (instance == null) {
                    instance = new SPUtil();
                }
            }
        }
        return instance;
    }

    private SPUtil() {
        Context context = DWLiveEngine.getInstance().getContext();
        if (context != null) {
            mPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
    }

    public void put(String key, int value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return;
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void put(String key, long value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return;
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void put(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return;
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void put(String key, boolean value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return;
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public int getInt(String key) {
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return 0;
        }
        return getInt(key, 0);
    }

    public int getInt(String key, int defValue) {
        if (TextUtils.isEmpty(key)) {
            throw new NullPointerException();
        }
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return 0;
        }
        return mPreferences.getInt(key, defValue);
    }

    public long getLong(String key) {
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return 0;
        }
        return getLong(key, 0L);
    }

    public long getLong(String key, long defValue) {
        if (TextUtils.isEmpty(key)) {
            throw new NullPointerException();
        }
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return 0;
        }
        return mPreferences.getLong(key, defValue);
    }

    public String getString(String key) {
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return "";
        }
        return getString(key, "");
    }

    public String getString(String key, String delValue) {
        if (TextUtils.isEmpty(key)) {
            throw new NullPointerException();
        }
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return "";
        }
        return mPreferences.getString(key, delValue);
    }

    public boolean getBoolean(String key) {
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return false;
        }
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean delValue) {
        if (TextUtils.isEmpty(key)) {
            throw new NullPointerException();
        }
        if (mPreferences == null) {
            ELog.e(TAG, "mPreferences == null ? true.");
            return false;
        }
        return mPreferences.getBoolean(key, delValue);
    }

}