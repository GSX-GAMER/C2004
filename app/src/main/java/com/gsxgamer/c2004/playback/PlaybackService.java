package com.gsxgamer.c2004.playback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import com.gsxgamer.c2004.MainActivity;
import com.gsxgamer.c2004.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.media.audiofx.Equalizer;

public class PlaybackService extends Service implements AudioManager.OnAudioFocusChangeListener {
    public static final String ACTION_PLAY="com.gsxgamer.c2004.action.PLAY";
    public static final String ACTION_NEXT="com.gsxgamer.c2004.action.NEXT";
    public static final String ACTION_PREVIOUS="com.gsxgamer.c2004.action.PREVIOUS";
    public static final String ACTION_TOGGLE="com.gsxgamer.c2004.action.TOGGLE";
    public static final String ACTION_SHUFFLE="com.gsxgamer.c2004.action.SHUFFLE";
    public static final String ACTION_REPEAT="com.gsxgamer.c2004.action.REPEAT";
    public static final String EXTRA_QUEUE="queue";
    public static final String EXTRA_INDEX="index";
    private static final String CHANNEL_ID="playback"; private static final int NOTIFICATION_ID=2004;
    private MediaPlayer player; private Equalizer equalizer; private MediaSessionCompat session; private AudioManager audio;
    private final ArrayList<String> queue=new ArrayList<String>(); private int index=0; private boolean shuffle=false; private int repeat=0;

    @Override public void onCreate(){super.onCreate();audio=(AudioManager)getSystemService(AUDIO_SERVICE);session=new MediaSessionCompat(this,"C2004Playback");session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS|MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);session.setCallback(new MediaSessionCompat.Callback(){
        @Override public void onPlay(){resume();} @Override public void onPause(){pause();} @Override public void onStop(){stopPlayback();stopSelf();} @Override public void onSkipToNext(){next();} @Override public void onSkipToPrevious(){previous();}
    });session.setActive(true);createNotificationChannel();}
    @Override public int onStartCommand(Intent intent,int flags,int startId){if(intent==null)return START_STICKY;String a=intent.getAction();if(ACTION_PLAY.equals(a)){if(intent.hasExtra(EXTRA_QUEUE)){ArrayList<String> q=intent.getStringArrayListExtra(EXTRA_QUEUE);if(q!=null){queue.clear();queue.addAll(q);index=intent.getIntExtra(EXTRA_INDEX,0);playCurrent();}}else if(intent.getData()!=null){queue.clear();queue.add(intent.getData().toString());index=0;playCurrent();}}else if(ACTION_TOGGLE.equals(a)){if(player!=null&&player.isPlaying())pause();else resume();}else if(ACTION_NEXT.equals(a))next();else if(ACTION_PREVIOUS.equals(a))previous();else if(ACTION_SHUFFLE.equals(a))shuffle=!shuffle;else if(ACTION_REPEAT.equals(a))repeat=(repeat+1)%3;return START_STICKY;}
    private void playCurrent(){if(queue.size()==0)return;if(index<0)index=queue.size()-1;if(index>=queue.size())index=0;if(!requestAudioFocus())return;stopPlayerOnly();player=new MediaPlayer();player.setAudioStreamType(AudioManager.STREAM_MUSIC);try{player.setDataSource(this,Uri.parse(queue.get(index)));player.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){public void onPrepared(MediaPlayer mp){try{equalizer=new Equalizer(0,mp.getAudioSessionId());equalizer.setEnabled(false);}catch(Throwable ignored){}mp.start();updateState();startForeground(NOTIFICATION_ID,buildNotification());}});player.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){public void onCompletion(MediaPlayer mp){advanceAfterCompletion();}});player.setOnErrorListener(new MediaPlayer.OnErrorListener(){public boolean onError(MediaPlayer mp,int w,int e){stopPlayerOnly();updateState();return true;}});player.prepareAsync();}catch(IOException e){stopPlayerOnly();advanceAfterCompletion();}}
    private void advanceAfterCompletion(){if(repeat==1){playCurrent();return;}if(shuffle&&queue.size()>1){int old=index;while(index==old)index=new Random().nextInt(queue.size());playCurrent();return;}if(index+1<queue.size()){index++;playCurrent();}else if(repeat==2){index=0;playCurrent();}else{updateState();}}
    private void next(){if(queue.size()==0)return;if(shuffle&&queue.size()>1){int old=index;while(index==old)index=new Random().nextInt(queue.size());}else if(index+1<queue.size())index++;else if(repeat==2)index=0;else return;playCurrent();}
    private void previous(){if(queue.size()==0)return;if(player!=null&&player.getCurrentPosition()>3000){player.seekTo(0);return;}if(index>0)index--;else if(repeat==2)index=queue.size()-1;playCurrent();}
    private void resume(){if(player!=null){player.start();updateState();}else playCurrent();}
    private void pause(){if(player!=null&&player.isPlaying())player.pause();updateState();}
    private boolean requestAudioFocus(){return audio.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;}
    @Override public void onAudioFocusChange(int c){if(player==null)return;if(c==AudioManager.AUDIOFOCUS_LOSS||c==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){if(player.isPlaying())player.pause();}else if(c==AudioManager.AUDIOFOCUS_GAIN){player.setVolume(1f,1f);}else if(c==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){player.setVolume(.25f,.25f);}updateState();}
    private Notification buildNotification(){Intent launch=new Intent(this,MainActivity.class);PendingIntent content=PendingIntent.getActivity(this,0,launch,PendingIntent.FLAG_UPDATE_CURRENT);PendingIntent toggle=serviceIntent(ACTION_TOGGLE,1);PendingIntent prev=serviceIntent(ACTION_PREVIOUS,2);PendingIntent next=serviceIntent(ACTION_NEXT,3);NotificationCompat.Builder b=new NotificationCompat.Builder(this,CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher).setContentTitle("C2004").setContentText(queue.size()>0?Uri.parse(queue.get(index)).getLastPathSegment():"Music").setContentIntent(content).setOngoing(player!=null&&player.isPlaying()).setOnlyAlertOnce(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous,"Previous",prev)).addAction(new NotificationCompat.Action(player!=null&&player.isPlaying()?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play,"Play/Pause",toggle)).addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next,"Next",next));b.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setMediaSession(session.getSessionToken()).setShowActionsInCompactView(0,1,2));return b.build();}
    private PendingIntent serviceIntent(String action,int request){return PendingIntent.getService(this,request,new Intent(this,PlaybackService.class).setAction(action),PendingIntent.FLAG_UPDATE_CURRENT);}
    private void updateState(){long p=player==null?0:player.getCurrentPosition();long d=player==null?0:player.getDuration();int state=player!=null&&player.isPlaying()?PlaybackStateCompat.STATE_PLAYING:PlaybackStateCompat.STATE_PAUSED;long actions=PlaybackStateCompat.ACTION_PLAY|PlaybackStateCompat.ACTION_PAUSE|PlaybackStateCompat.ACTION_SKIP_TO_NEXT|PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS|PlaybackStateCompat.ACTION_STOP;session.setPlaybackState(new PlaybackStateCompat.Builder().setActions(actions).setState(state,p,1f).setBufferedPosition(d).build());if(player!=null)startForeground(NOTIFICATION_ID,buildNotification());}
    private void createNotificationChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL_ID,"Playback",NotificationManager.IMPORTANCE_LOW);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}}
    private void stopPlayerOnly(){if(equalizer!=null){try{equalizer.release();}catch(Throwable ignored){}equalizer=null;}if(player!=null){try{player.stop();}catch(IllegalStateException ignored){}player.release();player=null;}}
    private void stopPlayback(){stopPlayerOnly();audio.abandonAudioFocus(this);session.setActive(false);stopForeground(true);}
    @Override public void onDestroy(){stopPlayback();session.release();super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
