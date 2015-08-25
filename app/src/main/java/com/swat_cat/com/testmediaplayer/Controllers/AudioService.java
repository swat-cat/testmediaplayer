package com.swat_cat.com.testmediaplayer.Controllers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.swat_cat.com.testmediaplayer.Model.Melody;
import com.swat_cat.com.testmediaplayer.R;
import com.swat_cat.com.testmediaplayer.Utils.MediaFilesUtil;

import java.util.ArrayList;

public class AudioService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    private static final String TAG = AudioService.class.getName();
    protected static final String UPDATE_UI = TAG+" upadte UI";
    private static final int NOTIFY_ID=1;

    private ArrayList<Melody> melodyList;
    private int currentMelodyPosition;
    private MediaFilesUtil util;
    private LocalBroadcastManager broadcaster;

    private MediaPlayer player;
    private final IBinder musicBind = new AudioBinder();
    private long songId=-1;
    private String songTitle= "";

    @Override
    public void onCreate() {
        super.onCreate();
        currentMelodyPosition = 0;
        melodyList = new ArrayList<>();
        util = new MediaFilesUtil(getApplicationContext());
        broadcaster = LocalBroadcastManager.getInstance(this);
        player = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public class AudioBinder extends Binder {
        AudioService getService() {
            return AudioService.this;
        }
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setMelody(int index){
        getCurrentMelody().setIsPlaying(false);
        currentMelodyPosition=index;
    }

    public void setFromOuterSpace(){
        currentMelodyPosition=0;
    }

    public void play(){
        songId =melodyList.get(currentMelodyPosition).getId();
        songTitle =melodyList.get(currentMelodyPosition).getTitle();
        player.reset();
        try{
            player.setDataSource(getApplicationContext(), util.currentSongUri(melodyList.get(currentMelodyPosition)));
        }
        catch(Exception e){
            Log.e(TAG, "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public void stop(){
        player.stop();
        //player.reset();
        getCurrentMelody().setIsPlaying(false);
        currentMelodyPosition=0;
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        player.reset();
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayer playback error");
        mp.reset();
        return false;
    }

    public void playPrev(){
        player.stop();
        player.reset();
        getCurrentMelody().setIsPlaying(false);
        currentMelodyPosition--;
        if(currentMelodyPosition<0) currentMelodyPosition=melodyList.size()-1;
        play();

    }

    //skip to next
    public void playNext(){
        player.stop();
        player.reset();
        getCurrentMelody().setIsPlaying(false);
        currentMelodyPosition++;
        if(currentMelodyPosition>=melodyList.size()) currentMelodyPosition=0;
        play();
    }

    public void updateUI(){
        Intent intent = new Intent(UPDATE_UI);
        broadcaster.sendBroadcast(intent);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        melodyList.get(currentMelodyPosition).setIsPlaying(true);
        updateUI();
        Intent notIntent = new Intent(this, MelodyListActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.mipmap.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setAutoCancel(true)
                .setContentText(songTitle);
        Notification not = builder.getNotification();
        startForeground(NOTIFY_ID, not);
    }

    //playback methods
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public long currentSongId(){
        return player.getAudioSessionId();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public long getSongId() {
        return songId;
    }

    public ArrayList<Melody> getMelodyList() {
        return melodyList;
    }

    public void setMelodyList(ArrayList<Melody> melodyList) {
        this.melodyList = melodyList;
    }

    public Melody getCurrentMelody(){
       return melodyList.get(currentMelodyPosition);
    }

    public int getCurrentMelodyPosition() {
        return currentMelodyPosition;
    }

    public void setCurrentMelodyPosition(int currentMelodyPosition) {
        this.currentMelodyPosition = currentMelodyPosition;
    }
}
