package com.gsxgamer.c2004.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import com.gsxgamer.c2004.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadEngine {
    private final Context context; private final OkHttpClient client;
    public DownloadEngine(Context c,OkHttpClient client){context=c.getApplicationContext();this.client=client;}
    public void download(final String url,final String name,final boolean wifiOnly){
        new Thread(new Runnable(){public void run(){
            if(wifiOnly){android.net.ConnectivityManager cm=(android.net.ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); android.net.NetworkInfo ni=cm.getActiveNetworkInfo(); if(ni==null||ni.getType()!=android.net.ConnectivityManager.TYPE_WIFI)return;}
            File dir=context.getExternalFilesDir(Environment.DIRECTORY_MUSIC); if(dir==null)dir=context.getFilesDir(); if(!dir.exists())dir.mkdirs();
            File file=new File(dir,name); long existing=file.exists()?file.length():0;
            Request.Builder rb=new Request.Builder().url(url); if(existing>0)rb.header("Range","bytes="+existing+"-");
            try{Response r=client.newCall(rb.build()).execute(); if(!r.isSuccessful())return; boolean append=existing>0&&r.code()==206; InputStream in=r.body().byteStream(); FileOutputStream out=new FileOutputStream(file,append); byte[] buf=new byte[8192]; int n; while((n=in.read(buf))!=-1)out.write(buf,0,n); out.close();in.close();notifyDone(name);}catch(Exception ignored){}}
        }).start();
    }
    private void notifyDone(String name){Notification n=new NotificationCompat.Builder(context,"downloads").setSmallIcon(R.drawable.ic_launcher).setContentTitle("Download complete").setContentText(name).setAutoCancel(true).build();((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(name.hashCode(),n);}
}
