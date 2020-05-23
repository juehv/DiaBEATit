package de.heoegbr.diabeatit.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import de.heoegbr.diabeatit.R;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BOOT_COMPLETE_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null
                && intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED"))
            Toast.makeText(context,
                    context.getResources().getString(R.string.app_name) + " started by boot completed intent",
                    Toast.LENGTH_LONG).show();
        Log.d(TAG, "App received boot completed intent");
    }
}
