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
        Log.d(TAG, "Try to register BroadcastReceiver...");
        Toast.makeText(context,
                context.getResources().getString(R.string.app_name) + " started by boot completed intent",
                Toast.LENGTH_LONG).show();
    }
}
