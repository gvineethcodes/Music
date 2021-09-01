package com.example.m7;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MediaService extends Service {
    private IBinder mBinder = new MyBinder();
    InterfaceControl interfaceControl;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        MediaService getService(){
            return MediaService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra("AName");
        if(action != null){
            switch (action){
                case "play":
                    if (interfaceControl != null)
                        interfaceControl.playInterface();
                    break;
                case "prev":
                    if (interfaceControl != null)
                        interfaceControl.prevInterface();
                    break;
                case "next":
                    if (interfaceControl != null)
                        interfaceControl.nextInterface();
                    break;
            }
        }
        return START_STICKY;
    }
    public void setCallBack(InterfaceControl interfaceControl){
        this.interfaceControl = interfaceControl;
    }
}
