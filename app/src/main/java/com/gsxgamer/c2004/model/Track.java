package com.gsxgamer.c2004.model;

public class Track {
    public long id;
    public String title;
    public String artist;
    public String album;
    public String uri;
    public long duration;
    public boolean remote;

    public Track(long id, String title, String artist, String album, String uri, long duration, boolean remote) {
        this.id=id; this.title=title; this.artist=artist; this.album=album; this.uri=uri; this.duration=duration; this.remote=remote;
    }
}
