package com.example.m7;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context,MediaService.class);
        if(intent.getAction() != null){
            switch (intent.getAction()){
                case "play":
                    intent1.putExtra("AName",intent.getAction());
                    context.startService(intent1);
                    break;
                case "prev":
                    intent1.putExtra("AName",intent.getAction());
                    context.startService(intent1);
                    break;
                case "next":
                    intent1.putExtra("AName",intent.getAction());
                    context.startService(intent1);
                    break;
            }
        }
    }
}
