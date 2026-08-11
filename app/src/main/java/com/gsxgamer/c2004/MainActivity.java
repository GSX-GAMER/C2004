package com.gsxgamer.c2004;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.gsxgamer.c2004.library.MediaStoreScanner;
import com.gsxgamer.c2004.model.Track;
import com.gsxgamer.c2004.playback.PlaybackService;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final List<Track> tracks=new ArrayList<Track>(); private TrackAdapter adapter; private TextView status; private EditText search;
    @Override protected void onCreate(Bundle state){super.onCreate(state);setContentView(R.layout.activity_main);status=(TextView)findViewById(R.id.status_text);search=(EditText)findViewById(R.id.search_box);RecyclerView list=(RecyclerView)findViewById(R.id.track_list);list.setLayoutManager(new LinearLayoutManager(this));adapter=new TrackAdapter();list.setAdapter(adapter);findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener(){public void onClick(View v){scan();}});findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener(){public void onClick(View v){filter(search.getText().toString());}});findViewById(R.id.remote_button).setOnClickListener(new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(MainActivity.this,RemoteActivity.class));}});scan();}
    private void scan(){status.setText("Scanning music…");new Thread(new Runnable(){public void run(){final List<Track> found=new MediaStoreScanner(MainActivity.this).scan();runOnUiThread(new Runnable(){public void run(){tracks.clear();tracks.addAll(found);adapter.notifyDataSetChanged();status.setText(found.size()+" local tracks");}});}}).start();}
    private void filter(String q){String query=q==null?"":q.trim().toLowerCase();List<Track> found=new MediaStoreScanner(this).scan();tracks.clear();for(Track t:found)if(query.length()==0||t.title.toLowerCase().contains(query)||t.artist.toLowerCase().contains(query)||t.album.toLowerCase().contains(query))tracks.add(t);adapter.notifyDataSetChanged();status.setText(tracks.size()+" matches");}
    private void play(Track t){Intent i=new Intent(this,PlaybackService.class);i.setAction(PlaybackService.ACTION_PLAY);i.setData(Uri.parse(t.uri));startService(i);Toast.makeText(this,"Playing: "+t.title,Toast.LENGTH_SHORT).show();}
    private class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.Holder>{public Holder onCreateViewHolder(ViewGroup p,int type){return new Holder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_track,p,false));}public void onBindViewHolder(Holder h,int pos){final Track t=tracks.get(pos);h.title.setText(t.title);h.artist.setText(t.artist+" • "+t.album);h.itemView.setOnClickListener(new View.OnClickListener(){public void onClick(View v){play(t);}});}public int getItemCount(){return tracks.size();}class Holder extends RecyclerView.ViewHolder{final TextView title,artist;Holder(View v){super(v);title=(TextView)v.findViewById(R.id.track_title);artist=(TextView)v.findViewById(R.id.track_artist);}}}
}
