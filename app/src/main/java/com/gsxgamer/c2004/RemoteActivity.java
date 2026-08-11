package com.gsxgamer.c2004;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import com.gsxgamer.c2004.model.Track;
import com.gsxgamer.c2004.network.NetworkStack;
import com.gsxgamer.c2004.network.SubsonicClient;
import com.gsxgamer.c2004.playback.PlaybackService;
import java.util.List;

public class RemoteActivity extends AppCompatActivity {
    private EditText url,user,password,query; private TextView status;
    @Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_remote);url=(EditText)findViewById(R.id.server_url);user=(EditText)findViewById(R.id.server_user);password=(EditText)findViewById(R.id.server_password);query=(EditText)findViewById(R.id.remote_query);status=(TextView)findViewById(R.id.remote_status);findViewById(R.id.remote_search).setOnClickListener(new View.OnClickListener(){public void onClick(View v){search();}});}
    private void search(){final String u=url.getText().toString(),n=user.getText().toString(),p=password.getText().toString(),q=query.getText().toString();status.setText("Searching…");new Thread(new Runnable(){public void run(){try{final List<Track> r=new SubsonicClient(NetworkStack.client()).search(u,n,p,q);runOnUiThread(new Runnable(){public void run(){show(r);}});}catch(final Exception e){runOnUiThread(new Runnable(){public void run(){status.setText("Error: "+e.getMessage());}});}}}).start();}
    private void show(final List<Track> r){status.setText(r.size()+" remote tracks");android.widget.LinearLayout box=(android.widget.LinearLayout)findViewById(R.id.remote_results);box.removeAllViews();for(final Track t:r){TextView v=new TextView(this);v.setText(t.title+"\n"+t.artist+" • "+t.album);v.setTextColor(0xffffffff);v.setTextSize(15);v.setPadding(12,10,8,10);v.setOnClickListener(new View.OnClickListener(){public void onClick(View x){Intent i=new Intent(RemoteActivity.this,PlaybackService.class);i.setAction(PlaybackService.ACTION_PLAY);i.setData(Uri.parse(t.uri));startService(i);Toast.makeText(RemoteActivity.this,"Playing: "+t.title,Toast.LENGTH_SHORT).show();}});box.addView(v);}}
}
