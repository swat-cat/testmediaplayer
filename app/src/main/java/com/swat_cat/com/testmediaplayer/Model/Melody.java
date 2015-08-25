package com.swat_cat.com.testmediaplayer.Model;

/**
 * Created by Dell on 01.07.2015.
 */
public class Melody {
    private long id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    boolean isPlaying;

    public Melody(long id, String title, String artist, String album,long duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        isPlaying = false;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }
}
