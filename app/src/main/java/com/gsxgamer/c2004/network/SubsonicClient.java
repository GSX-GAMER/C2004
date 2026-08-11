package com.gsxgamer.c2004.network;

import com.gsxgamer.c2004.model.Track;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class SubsonicClient {
    private final OkHttpClient client;
    public SubsonicClient(OkHttpClient client){this.client=client;}
    public List<Track> search(String baseUrl,String user,String password,String query) throws Exception {
        String salt="c2004"+System.currentTimeMillis(); String token=md5(password+salt);
        String url=trim(baseUrl)+"/rest/search3.view?u="+enc(user)+"&s="+enc(salt)+"&t="+token+"&v=1.16.1&c=C2004&f=json&query="+enc(query);
        Response response=client.newCall(new Request.Builder().url(url).get().build()).execute();
        if(!response.isSuccessful())throw new IOException("HTTP "+response.code());
        JSONObject sr=new JSONObject(response.body().string()).optJSONObject("subsonic-response");
        if(sr==null||!"ok".equals(sr.optString("status")))throw new IOException("Subsonic authentication failed");
        JSONObject result=sr.optJSONObject("searchResult3"); JSONArray songs=result==null?null:result.optJSONArray("song");
        ArrayList<Track> out=new ArrayList<Track>(); if(songs==null)return out;
        for(int i=0;i<songs.length();i++){JSONObject s=songs.getJSONObject(i);long id=s.optLong("id",i);out.add(new Track(id,s.optString("title","Unknown"),s.optString("artist","Unknown"),s.optString("album","Unknown"),streamUrl(baseUrl,user,password,id,salt),s.optLong("duration",0)*1000L,true));}
        return out;
    }
    private String streamUrl(String base,String user,String password,long id,String salt)throws Exception{return trim(base)+"/rest/stream.view?u="+enc(user)+"&s="+enc(salt)+"&t="+md5(password+salt)+"&v=1.16.1&c=C2004&id="+id;}
    private String trim(String s){while(s.endsWith("/"))s=s.substring(0,s.length()-1);return s;}
    private String enc(String s)throws Exception{return URLEncoder.encode(s==null?"":s,"UTF-8");}
    private String md5(String s)throws Exception{byte[] b=MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8"));StringBuilder x=new StringBuilder();for(byte v:b)x.append(String.format("%02x",v&255));return x.toString();}
}
