package com.swat_cat.com.testmediaplayer.Controllers;

import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * Created by Dell on 03.07.2015.
 */
public class FileBrowserActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new FileBrowser();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED,null);
    }
}
