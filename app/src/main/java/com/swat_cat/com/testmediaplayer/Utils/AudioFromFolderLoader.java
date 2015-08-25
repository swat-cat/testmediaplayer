package com.swat_cat.com.testmediaplayer.Utils;

import android.content.Context;

import com.swat_cat.com.testmediaplayer.Model.Melody;

import java.util.ArrayList;

/**
 * Created by Dell on 13.07.2015.
 */
public class AudioFromFolderLoader extends DataLoader<ArrayList<Melody>> {
    private MediaFilesUtil util;
    private String path;
    public AudioFromFolderLoader(Context context,MediaFilesUtil util, String dirpath) {
        super(context);
        this.util= new MediaFilesUtil(context);
        path = dirpath;
    }

    @Override
    public ArrayList<Melody> loadInBackground() {
        return util.getAudioFromFolder(path);
    }
}
