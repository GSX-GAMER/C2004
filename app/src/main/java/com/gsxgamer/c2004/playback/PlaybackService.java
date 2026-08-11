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
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import java.io.IOException;

public class PlaybackService extends Service implements AudioManager.OnAudioFocusChangeListener {
    public static final String ACTION_PLAY = "com.gsxgamer.c2004.action.PLAY";
    private static final String CHANNEL_ID = "playback";
    private static final int NOTIFICATION_ID = 2004;

    private MediaPlayer player;
    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;
    private String currentPath;

    @Override public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mediaSession = new MediaSessionCompat(this, "C2004Playback");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override public void onPlay() { if (player != null) player.start(); updateState(); }
            @Override public void onPause() { if (player != null && player.isPlaying()) player.pause(); updateState(); }
            @Override public void onStop() { stopPlayback(); stopSelf(); }
        });
        mediaSession.setActive(true);
        createNotificationChannel();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_PLAY.equals(intent.getAction()) && intent.getData() != null) {
            play(intent.getData());
        }
        return START_STICKY;
    }

    private void play(Uri uri) {
        if (!requestAudioFocus()) return;
        stopPlayerOnly();
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            player.setDataSource(this, uri);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    updateState();
                    startForeground(NOTIFICATION_ID, buildNotification());
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override public void onCompletion(MediaPlayer mp) { updateState(); }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override public boolean onError(MediaPlayer mp, int what, int extra) {
                    stopPlayerOnly(); updateState(); return true;
                }
            });
            currentPath = uri.toString();
            player.prepareAsync();
        } catch (IOException e) {
            stopPlayerOnly();
            stopSelf();
        }
    }

    private boolean requestAudioFocus() {
        return audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @Override public void onAudioFocusChange(int focusChange) {
        if (player == null) return;
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            if (player.isPlaying()) player.pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            player.setVolume(1f, 1f);
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            player.setVolume(0.25f, 0.25f);
        }
        updateState();
    }

    private Notification buildNotification() {
        Intent launch = new Intent(this, com.gsxgamer.c2004.MainActivity.class);
        PendingIntent content = PendingIntent.getActivity(this, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent playPause = PendingIntent.getService(this, 1,
                new Intent(this, PlaybackService.class).setAction(ACTION_PLAY).setData(Uri.parse(currentPath)),
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(com.gsxgamer.c2004.R.drawable.ic_launcher)
                .setContentTitle("C2004")
                .setContentText("Playing local audio")
                .setContentIntent(content)
                .setOngoing(player != null && player.isPlaying())
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(new NotificationCompat.Action(
                        player != null && player.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        player != null && player.isPlaying() ? "Pause" : "Play", playPause));
        b.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken()).setShowActionsInCompactView(0));
        return b.build();
    }

    private void updateState() {
        long position = player == null ? 0 : player.getCurrentPosition();
        long duration = player == null ? 0 : player.getDuration();
        int state = player != null && player.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP)
                .setState(state, position, 1f).setBufferedPosition(duration).build());
        if (player != null) startForeground(NOTIFICATION_ID, buildNotification());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
    }

    private void stopPlayerOnly() {
        if (player != null) {
            try { player.stop(); } catch (IllegalStateException ignored) {}
            player.release();
            player = null;
        }
    }

    private void stopPlayback() {
        stopPlayerOnly();
        audioManager.abandonAudioFocus(this);
        mediaSession.setActive(false);
        stopForeground(true);
    }

    @Override public void onDestroy() {
        stopPlayback();
        mediaSession.release();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
