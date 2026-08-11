package com.gsxgamer.c2004.library;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.gsxgamer.c2004.model.Track;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MediaStoreScanner {
    private final Context context;
    public MediaStoreScanner(Context context) { this.context=context.getApplicationContext(); }

    public List<Track> scan() {
        ArrayList<Track> result=new ArrayList<Track>();
        ContentResolver cr=context.getContentResolver();
        String[] projection={MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.DURATION};
        Cursor c=null;
        try {
            c=cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,MediaStore.Audio.Media.IS_MUSIC+"=1",null,MediaStore.Audio.Media.TITLE+" COLLATE NOCASE ASC");
            if(c!=null) while(c.moveToNext()) {
                long id=c.getLong(0);
                result.add(new Track(id,safe(c.getString(1)),safe(c.getString(2)),safe(c.getString(3)),Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,String.valueOf(id)).toString(),c.getLong(4),false));
            }
        } finally { if(c!=null)c.close(); }
        Collections.sort(result,new Comparator<Track>(){ public int compare(Track a,Track b){return a.title.compareToIgnoreCase(b.title);} });
        return result;
    }
    private String safe(String s){return s==null||s.length()==0?"Unknown":s;}
}
