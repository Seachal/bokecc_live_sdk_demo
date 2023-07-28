package com.bokecc.livemodule.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bokecc.sdk.mobile.live.DWLive;

/**
 * 系统电话状态监听
 */
public class AppPhoneStateListener extends BroadcastReceiver {

    private final static String TAG = "AppPhoneStateListener";
    public static final String ACTION_PAUSE = "phone_action_pause";
    public static final String ACTION_RESUME = "phone_action_resume";

    // 是否在系统通话中
    public static boolean isPhoneInCall = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        /* Incoming call */
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            Log.i(TAG, "current state is EXTRA_STATE_RINGING.");
            isPhoneInCall = true;
        }


        /* Outgoing call */
        else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            Log.i(TAG, "current state is OFFHOOK.");
            isPhoneInCall = true;
            send(context,ACTION_PAUSE);

        }

        /* Call ended */
        else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            Log.i(TAG, "current state is IDLE.");
            isPhoneInCall = false;
            send(context,ACTION_RESUME);
        }
    }

    private void send(Context context,String action){
        Intent intent=new Intent(action);
        context.sendBroadcast(intent);
    }


}
