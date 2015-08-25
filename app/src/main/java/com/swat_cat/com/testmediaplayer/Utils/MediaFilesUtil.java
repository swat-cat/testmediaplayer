package com.swat_cat.com.testmediaplayer.Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.swat_cat.com.testmediaplayer.Model.Melody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Dell on 01.07.2015.
 */
public class MediaFilesUtil {
    private static final String TAG = MediaFilesUtil.class.getName();
    private Context context;

    public MediaFilesUtil(Context context) {
        this.context = context;
    }

    public ArrayList<Melody> getAllAudioFiles(){
        ArrayList<Melody> melodies = new ArrayList<>();
        ContentResolver melodyResolver = context.getContentResolver();
        Uri allAudioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor melodyCursor = melodyResolver.query(allAudioUri,null,null,null,null);
        if(melodyCursor!=null && melodyCursor.moveToFirst()){
            //get columns
            int titleColumn = melodyCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = melodyCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = melodyCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = melodyCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM);
            int durationColumn = melodyCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            //add songs to list
            do {
                long id = melodyCursor.getLong(idColumn);
                String title = melodyCursor.getString(titleColumn);
                String artist = melodyCursor.getString(artistColumn);
                String album = melodyCursor.getString(albumColumn);
                long duration = melodyCursor.getLong(durationColumn);
                melodies.add(new Melody(id,title,artist,album,duration));
            }
            while (melodyCursor.moveToNext());
        }
        try {
            melodyCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Cursor closing error" );
        }
        melodyCursor=null;
        return melodies;
    }

    public ArrayList<Melody> getAudioFromFolder(String dirPath){
        String selection =MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DATA + " LIKE '"+dirPath+"%'";
        String[] projection = {MediaStore.Audio.Media.DATA};
        String[] selectionArgs={dirPath+"%"};
        ContentResolver melodyResolver = context.getContentResolver();
        Cursor melodyCursor = melodyResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
               /* projection*/null,
                selection,
                /*selectionArgs*/null,
                null);
        ArrayList<Melody> melodies = new ArrayList<>();
        if(melodyCursor!=null && melodyCursor.moveToFirst()&&!melodyCursor.isBeforeFirst()){
            //get columns
            int titleColumn = melodyCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = melodyCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = melodyCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = melodyCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int durationColumn = melodyCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            //add songs to list
            do {
                long id = melodyCursor.getLong(idColumn);
                String title = melodyCursor.getString(titleColumn);
                String artist = melodyCursor.getString(artistColumn);
                String album = melodyCursor.getString(albumColumn);
                long duration = melodyCursor.getLong(durationColumn);
                melodies.add(new Melody(id,title,artist,album,duration));
            }
            while (melodyCursor.moveToNext());
        }
        try {
            melodyCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Cursor closing error" );
        }
        melodyCursor=null;
        return melodies;
    }

    public ArrayList<Melody> search(String query,String selection){

        String[] projection = {MediaStore.Audio.Media.DATA};
        ContentResolver melodyResolver = context.getContentResolver();
        Uri allAudioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor melodyCursor = melodyResolver.query(allAudioUri, null, selection, null, null);
        ArrayList<Melody> melodies = new ArrayList<>();
        if(melodyCursor!=null && melodyCursor.moveToFirst()){
            //get columns
            int titleColumn = melodyCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = melodyCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = melodyCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = melodyCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int durationColumn = melodyCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            //add songs to list
            do {
                long id = melodyCursor.getLong(idColumn);
                String title = melodyCursor.getString(titleColumn);
                String artist = melodyCursor.getString(artistColumn);
                String album = melodyCursor.getString(albumColumn);
                long duration = melodyCursor.getLong(durationColumn);
                melodies.add(new Melody(id,title,artist,album,duration));
            }
            while (melodyCursor.moveToNext());
        }
        try {
            melodyCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Cursor closing error" );
        }
        melodyCursor=null;
        return melodies;
    }

    public ArrayList<Melody> searchByTitle(String query){
        String selection =MediaStore.Audio.Media.TITLE +" like "+"'%"+query+"%'";
        return search(query,selection);
    }

    public ArrayList<Melody> searchByArtist(String query){
        String selection =MediaStore.Audio.Media.ARTIST +" like "+"'%"+query+"%'";
        return search(query,selection);
    }

    public ArrayList<Melody> searchByAlbum(String query){
        String selection =MediaStore.Audio.Media.ALBUM +" like "+"'%"+query+"%'";
        return search(query, selection);
    }

    public ArrayList<Melody> performSearch(String query, SEARCH_FILTER filter){
        switch (filter){
            case TITLE:
                return searchByTitle(query);
            case ARTIST:
                return searchByArtist(query);
            case ALBUM:
                return searchByAlbum(query);
            default:return searchByTitle(query);
        }
    }


    public void sortByTitle(ArrayList<Melody> melodyList){
        Collections.sort(melodyList, new Comparator<Melody>() {
            @Override
            public int compare(Melody lhs, Melody rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });
    }

    public void sortByArtist(ArrayList<Melody> melodyList){
        Collections.sort(melodyList, new Comparator<Melody>() {
            @Override
            public int compare(Melody lhs, Melody rhs) {
                return lhs.getArtist().compareTo(rhs.getArtist());
            }
        });
    }

    public void sortByAlbum(ArrayList<Melody> melodyList){
        Collections.sort(melodyList, new Comparator<Melody>() {
            @Override
            public int compare(Melody lhs, Melody rhs) {
                return lhs.getAlbum().compareTo(rhs.getAlbum());
            }
        });
    }

    public void sort(ArrayList<Melody> melodyList, SORT_FILTER sort_filter){
        switch (sort_filter){
            case TITLE:
                sortByTitle(melodyList);
                break;
            case ARTIST:
                sortByArtist(melodyList);
                break;
            case ALBUM:
                sortByAlbum(melodyList);
                break;
            default:break;
        }
    }

    public Uri currentSongUri(Melody melody){
        long currSong =melody.getId();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        return trackUri;
    }


    public enum SEARCH_FILTER{
        TITLE,ARTIST,ALBUM
    }
    public enum SORT_FILTER{
        TITLE,ARTIST,ALBUM
    }
}
