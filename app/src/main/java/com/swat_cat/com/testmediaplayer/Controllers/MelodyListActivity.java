package com.swat_cat.com.testmediaplayer.Controllers;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.swat_cat.com.testmediaplayer.R;

/**
 * Created by Dell on 02.07.2015.
 */
public class MelodyListActivity extends SingleFragmentActivity{


    @Override
    protected Fragment createFragment() {
        return new MelodyListFragment();
    }
}
