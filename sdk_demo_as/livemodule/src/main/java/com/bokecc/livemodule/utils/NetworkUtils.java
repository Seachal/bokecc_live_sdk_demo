package com.bokecc.livemodule.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.bokecc.sdk.mobile.live.DWLiveEngine;


/**
 * 网络工具类
 */
public class NetworkUtils {

    private NetworkUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }


    /**
     * Return whether network is connected.
     * <p>Must hold {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return {@code true}: connected<br>{@code false}: disconnected
     */
    public static boolean isConnected() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * 网络是否可用
     *
     * @param context context
     * @return true/false
     */
    public static boolean isNetworkAvailable(Context context) {
        return isNetworkAvailable(context, false);
    }

    /**
     * wifi是否可用
     *
     * @param context context
     * @return true/false
     */
    public static boolean isWifiNetworkAvailable(Context context) {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission")
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.isConnected();
    }

    /**
     * 4G是否可用
     *
     * @param context context
     * @return true/false
     */
    public static boolean isFastNetworkAvailable(Context context) {
        return isNetworkAvailable(context, true);
    }


    private static boolean isFastMobileNetwork(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true;
            default:
                return true;
        }
    }

    private static boolean isNetworkAvailable(Context context, boolean isFast) {

        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //  如果设备不支持此类型的网络链接会返回null
        @SuppressLint("MissingPermission")
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        @SuppressLint("MissingPermission")
        NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isFiltered = true;
        if (isFast && mobile != null) {
            int subType = mobile.getSubtype();
            isFiltered = isFastMobileNetwork(subType);
        }
        if (mobile == null && wifi == null) {
            return false;
        } else if (mobile != null && wifi == null) {
            return mobile.isConnected() && isFiltered;
        } else if (mobile == null) {
            return wifi.isConnected();
        } else {
            return wifi.isConnected() || (mobile.isConnected() && isFiltered);
        }
    }

    private static NetworkInfo getActiveNetworkInfo() {
        Context context = DWLiveEngine.getInstance().getContext();
        if (context == null) {
            return null;
        }
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return null;
        return cm.getActiveNetworkInfo();
    }


}