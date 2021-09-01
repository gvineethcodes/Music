package com.example.m7;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements InterfaceControl, ServiceConnection {
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    MediaPlayer mediaPlayer = null;
    MediaSessionCompat mediaSessionCompat;
    MediaService mediaService;
    Spinner spinner,spinner3;
    String subject,topic,textNotification="";
    Uri downloadUri;
    ImageButton imageButton,imageButton2,imageButton3;
    TextView textView, textView2;
    NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = findViewById(R.id.spinner);
        spinner3 = findViewById(R.id.spinner3);
        imageButton = findViewById(R.id.imageButton);
        imageButton2 = findViewById(R.id.imageButton2);
        imageButton3 = findViewById(R.id.imageButton3);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);

        mediaSessionCompat = new MediaSessionCompat(this,"mytag");

        sharedpreferences = getSharedPreferences("MyPREFERECES_M7", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        imageButton2.setEnabled(false);

        textView2.setText("disabled-start");

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        ArrayList<String> list = new ArrayList<String>();
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, list);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(arrayAdapter);

                        for (StorageReference prefix : listResult.getPrefixes()) {
                            Log.d("my", prefix.getName());
                            arrayAdapter.add(prefix.getName());
                        }
                        spinner.setSelection(sharedpreferences.getInt("SubjectPosition", 0));

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                    }
                });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                mStorageRef.child(spinner.getSelectedItem().toString()).listAll()
                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                ArrayList<String> list = new ArrayList<String>();
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, list);
                                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner3.setAdapter(arrayAdapter);

                                for (StorageReference item : listResult.getItems()) {
                                    arrayAdapter.add(item.getName());
                                    Log.d("my", item.getName());

                                }
                                spinner3.setSelection(sharedpreferences.getInt("TopicPosition", 0));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Uh-oh, an error occurred!
                            }
                        });
                keepInSharedPreferences("SubjectPosition",spinner.getSelectedItemPosition());
                subject = spinner.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                topic = spinner3.getSelectedItem().toString();
                textNotification="disabled-wait";
                textView2.setText(textNotification);
                showNotification(sharedpreferences.getInt("playPauseImage",R.drawable.ic_baseline_play_arrow_24));

                mStorageRef.child(subject).child(topic).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Toast.makeText(getApplicationContext(), "R", Toast.LENGTH_SHORT).show();
                        downloadUri=uri;
                        imageButton2.setEnabled(true);
                        textNotification="enabled-"+topic;
                        showNotification(sharedpreferences.getInt("playPauseImage",R.drawable.ic_baseline_play_arrow_24));
                        textView2.setText(textNotification);
                        if(sharedpreferences.getInt("prev",0)==1){
                            if (mediaPlayer != null ){
                                //if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                                mediaPlayer.reset();
                            }
                            play();
                            keepInSharedPreferences("prev",0);
                        }else if(sharedpreferences.getInt("next",0)==1){
                            if (mediaPlayer != null ){
                                //if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                                mediaPlayer.reset();
                            }
                            play();
                            keepInSharedPreferences("next",0);
                        }


                        Log.d("my", "s3d");
                    }
                });
                mStorageRef.child(subject).child(topic).getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        textView2.setText("error\n"+e.getMessage());
                    }
                });
                Log.d("my", "s3");
                keepInSharedPreferences("TopicPosition",spinner3.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               playInterface();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevInterface();
            }
        });

        imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextInterface();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this,MediaService.class),this,BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);

    }


    private void play(){
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayer.setDataSource(getApplicationContext(),downloadUri);
            mediaPlayer.prepareAsync();
            imageButton2.setEnabled(false);
            textNotification="disabled-preparing-"+topic;
            showNotification(sharedpreferences.getInt("playPauseImage",R.drawable.ic_baseline_play_arrow_24));
            textView2.setText(textNotification);

            Log.d("my", "p");

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    imageButton2.setImageResource(R.drawable.ic_baseline_pause_24);
                    keepInSharedPreferences("playPauseImage",R.drawable.ic_baseline_pause_24);
                    imageButton2.setEnabled(true);
                    textView2.setText("enabled-playing");
                    textNotification=topic;
                    showNotification(sharedpreferences.getInt("playPauseImage",R.drawable.ic_baseline_play_arrow_24));
                    textView.setText("\n\nsubject :\n"+spinner.getSelectedItem().toString()+"\n\ntopic :\n"+spinner3.getSelectedItem().toString());
                    Log.d("my", "pp");

                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    //imageButton2.setEnabled(false);
                    textView2.setText("disabled-MediaError");
                    mediaPlayer.reset();
                    play();
                    return false;
                }
            });
            keepStringSharedPreferences("uri",downloadUri.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        keepInSharedPreferences("playPauseImage",R.drawable.ic_baseline_play_arrow_24);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
        notificationManager.cancelAll();
    }

    private void keepInSharedPreferences(String keyStr, int valueInt) {
        editor.putInt(keyStr, valueInt);
        editor.apply();
    }
    private void keepStringSharedPreferences(String keyStr1, String valueStr1) {
        editor.putString(keyStr1, valueStr1);
        editor.apply();
    }

    @Override
    public void playInterface() {
        if (mediaPlayer != null ){
            if (sharedpreferences.getString("uri","").equals(downloadUri.toString())){
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    imageButton2.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    keepInSharedPreferences("playPauseImage",R.drawable.ic_baseline_play_arrow_24);
                }
                else {
                    mediaPlayer.start();
                    imageButton2.setImageResource(R.drawable.ic_baseline_pause_24);
                    keepInSharedPreferences("playPauseImage",R.drawable.ic_baseline_pause_24);

                }
                showNotification(sharedpreferences.getInt("playPauseImage",R.drawable.ic_baseline_play_arrow_24));

            }else {
                mediaPlayer.stop();
                mediaPlayer.reset();
                play();
            }
        }else play();
        //showNotification("play",R.drawable.playimg_foreground);
    }

    @Override
    public void prevInterface() {
        if (spinner3.getSelectedItemPosition()-1 > -1) {
            keepInSharedPreferences("prev",1);
            spinner3.setSelection(spinner3.getSelectedItemPosition() - 1);
            //showNotification("prev",R.drawable.ic_baseline_skip_previous_24);
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            imageButton2.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            keepInSharedPreferences("playPauseImage",R.drawable.ic_baseline_play_arrow_24);
            showNotification(sharedpreferences.getInt("playPauseImage",R.drawable.ic_baseline_play_arrow_24));

        }

    }

    @Override
    public void nextInterface() {
        if (spinner3.getSelectedItemPosition()+1 < spinner3.getAdapter().getCount()) {
            keepInSharedPreferences("next",1);
            spinner3.setSelection(spinner3.getSelectedItemPosition() + 1);
            //showNotification("next",R.drawable.ic_baseline_skip_next_24);
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            imageButton2.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            keepInSharedPreferences("playPauseImage",R.drawable.ic_baseline_play_arrow_24);
            showNotification(sharedpreferences.getInt("playPauseImage",R.drawable.ic_baseline_play_arrow_24));
        }
    }

    public void showNotification(int playPauseImage){
        Intent playI = new Intent(this, NotificationReceiver.class).setAction("play");
        PendingIntent playPI = PendingIntent.getBroadcast(this,90,playI,PendingIntent.FLAG_UPDATE_CURRENT);
        Intent prevI = new Intent(this, NotificationReceiver.class).setAction("prev");
        PendingIntent prevPI = PendingIntent.getBroadcast(this,9,prevI,PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextI = new Intent(this, NotificationReceiver.class).setAction("next");
        PendingIntent nextPI = PendingIntent.getBroadcast(this,80,nextI,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this,"92")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(subject)
                .setContentText(textNotification)
                .addAction(R.drawable.ic_baseline_skip_previous_24,"prev",prevPI)
                .addAction(playPauseImage,"play",playPI)
                .addAction(R.drawable.ic_baseline_skip_next_24,"next",nextPI)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .build();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MediaService.MyBinder myBinder = (MediaService.MyBinder) iBinder;
        mediaService = myBinder.getService();
        mediaService.setCallBack(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}