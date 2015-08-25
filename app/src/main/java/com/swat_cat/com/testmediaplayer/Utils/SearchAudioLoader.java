package com.swat_cat.com.testmediaplayer.Utils;

import android.content.Context;

import com.swat_cat.com.testmediaplayer.Model.Melody;

import java.util.ArrayList;

/**
 * Created by Dell on 13.07.2015.
 */
public class SearchAudioLoader extends DataLoader<ArrayList<Melody>> {

    private MediaFilesUtil util;
    private String query;
    private MediaFilesUtil.SEARCH_FILTER search_filter;

    public SearchAudioLoader(Context context, MediaFilesUtil util, String query, MediaFilesUtil.SEARCH_FILTER search_filter) {
        super(context);
        this.util = util;
        this.query = query;
        this.search_filter = search_filter;
    }

    @Override
    public ArrayList<Melody> loadInBackground() {
        return util.performSearch(query,search_filter);
    }
}
