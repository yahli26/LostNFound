package com.gmail.yahlieyal.lostnfound;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;

public class BatteryReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); //represent the percent of battery
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1); // represent state of charger
            boolean isCharged = (plugged == BatteryManager.BATTERY_PLUGGED_AC) || (plugged == BatteryManager.BATTERY_PLUGGED_USB) || (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS);
            if (!isCharged && level <= 30 && level > 15)
                Toast.makeText(context, "Battery level " + level + "% is low. Please connect to a charger.", Toast.LENGTH_SHORT).show();
            else if (level <= 15) {
                if (isCharged)
                    Toast.makeText(context, "WARNING: Battery level " + level + "% is very low.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, "WARNING: Battery level " + level + "% is very low. Please connect to a charger.", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
