package com.swat_cat.com.testmediaplayer.Utils;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Created by Dell on 13.07.2015.
 */
public abstract class DataLoader<D> extends android.support.v4.content.AsyncTaskLoader<D> {

    private D data;

    public DataLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        } else {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(D data) {
        this.data = data;
        if (isStarted())
            super.deliverResult(data);
    }
}
