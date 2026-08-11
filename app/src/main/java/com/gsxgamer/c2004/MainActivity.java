package com.gsxgamer.c2004;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private final List<Track> tracks = new ArrayList<Track>();
    private TrackAdapter adapter;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = (TextView) findViewById(R.id.status_text);
        RecyclerView list = (RecyclerView) findViewById(R.id.track_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrackAdapter();
        list.setAdapter(adapter);

        Button scan = (Button) findViewById(R.id.scan_button);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { scanLocalMusic(); }
        });
    }

    private void scanLocalMusic() {
        tracks.clear();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };
        Cursor cursor = getContentResolver().query(
                uri, projection, MediaStore.Audio.Media.IS_MUSIC + "=1", null,
                MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC");
        if (cursor != null) {
            try {
                int id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int data = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                while (cursor.moveToNext()) {
                    tracks.add(new Track(cursor.getLong(id), cursor.getString(title),
                            cursor.getString(artist), cursor.getString(data)));
                }
            } finally {
                cursor.close();
            }
        }
        adapter.notifyDataSetChanged();
        status.setText(tracks.size() + " local tracks found");
        Toast.makeText(this, "Scanned local music", Toast.LENGTH_SHORT).show();
    }

    private static class Track {
        final long id; final String title; final String artist; final String path;
        Track(long id, String title, String artist, String path) {
            this.id = id; this.title = title; this.artist = artist; this.path = path;
        }
    }

    private class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.Holder> {
        @Override public Holder onCreateViewHolder(ViewGroup parent, int type) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false));
        }
        @Override public void onBindViewHolder(Holder h, int position) {
            final Track track = tracks.get(position);
            h.title.setText(track.title == null ? "Unknown title" : track.title);
            h.artist.setText(track.artist == null ? "Unknown artist" : track.artist);
            h.itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, com.gsxgamer.c2004.playback.PlaybackService.class);
                    intent.setAction(com.gsxgamer.c2004.playback.PlaybackService.ACTION_PLAY);
                    intent.setData(Uri.parse(track.path));
                    startService(intent);
                }
            });
        }
        @Override public int getItemCount() { return tracks.size(); }
        class Holder extends RecyclerView.ViewHolder {
            final TextView title; final TextView artist;
            Holder(View item) {
                super(item); title = (TextView)item.findViewById(R.id.track_title);
                artist = (TextView)item.findViewById(R.id.track_artist);
            }
        }
    }
}
