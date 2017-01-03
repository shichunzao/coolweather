package app.coolweather.com.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import app.coolweather.com.service.AutoUpdateService;


/**
 * Created by scz on 2016/12/29.
 */

public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("AutoUpdateReceiver", "启动广播，打开后台服务");
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}
