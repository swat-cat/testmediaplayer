package com.swat_cat.com.testmediaplayer.Utils;

import android.content.Context;

import com.swat_cat.com.testmediaplayer.Model.Melody;

import java.util.ArrayList;

/**
 * Created by Dell on 13.07.2015.
 */
public class AllAudioLoader extends DataLoader<ArrayList<Melody>> {
    private MediaFilesUtil util;
    public AllAudioLoader(Context context, MediaFilesUtil util) {
        super(context);
        this.util=util;
    }

    @Override
    public ArrayList<Melody> loadInBackground() {
        return util.getAllAudioFiles();
    }
}
