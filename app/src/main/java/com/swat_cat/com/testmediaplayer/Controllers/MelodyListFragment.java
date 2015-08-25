package com.swat_cat.com.testmediaplayer.Controllers;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.swat_cat.com.testmediaplayer.Model.Melody;
import com.swat_cat.com.testmediaplayer.R;
import com.swat_cat.com.testmediaplayer.Utils.AllAudioLoader;
import com.swat_cat.com.testmediaplayer.Utils.AudioFromFolderLoader;
import com.swat_cat.com.testmediaplayer.Utils.MediaFilesUtil;
import com.swat_cat.com.testmediaplayer.Utils.SearchAudioLoader;
import com.swat_cat.com.testmediaplayer.Utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dell on 01.07.2015.
 */
public class MelodyListFragment extends ListFragment{
    private static final String TAG = MelodyListFragment.class.getName();
    private static final String ARG_DIR_PATH = TAG+" dir_path";
    private static final String ARG_QUERY = TAG+" query";
    protected static final int REQUEST_PATH = 1;
    private static  int ALL_AUDIO_LOADER_ID = 6;
    private static final int AUDIO_FROM_FOLDER_LOADER_ID = 7;
    private static final int SEARCH_AUDIO_LOADER_ID = 8;


    private ListView musicListView;
    private SlidingMenu menu;
    private ArrayList<Melody> melodyList =null;
    private View view;
    private MelodiesAdapter adapter;
    private MediaFilesUtil util;
    MediaFilesUtil.SEARCH_FILTER search_filter = MediaFilesUtil.SEARCH_FILTER.TITLE;
    MediaFilesUtil.SORT_FILTER sort_filter = MediaFilesUtil.SORT_FILTER.TITLE;

    private AudioService audioService;
    private boolean isBounded;
    private Intent playIntent;
    private boolean paused =false, initilised = false, stoped = true, fromOuterSpace=false;
    private TextView melodyInfo;
    private TextView albumInfo;
    private ImageButton prev;
    private ImageButton play_pause;
    private ImageButton stop;
    private ImageButton next;
    private TextView duration;
    private SeekBar audioSeekBar;
    private BroadcastReceiver receiver;

    private TextView lastDuration;
    private Handler durationHandler = new Handler();
    private double timeElapsed = 0, finalTime = 0;
    private WeakReference<View> prevItemView = null;
    private ServiceConnection musicConnection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        musicConnection = new ServiceConnection(){

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                AudioService.AudioBinder binder = (AudioService.AudioBinder)service;
                //get service
                audioService = binder.getService();
                //pass list
                isBounded = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBounded = false;
            }
        };

        if(playIntent==null){
            playIntent = new Intent(getActivity(), AudioService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            //getActivity().startService(playIntent);
        }
        util = new MediaFilesUtil(getActivity());
        Intent intent = getActivity().getIntent();
        if(Intent.ACTION_VIEW.equals(intent.getAction())){
            Uri uri = intent.getData();
            Log.d(TAG, "Path:" + uri.getPath());
            if(uri!=null){
                Bundle args = new Bundle();
                args.putString(ARG_DIR_PATH,uri.getPath());
                if(getLoaderManager().getLoader(AUDIO_FROM_FOLDER_LOADER_ID)!=null){
                    getLoaderManager().restartLoader(AUDIO_FROM_FOLDER_LOADER_ID, args, new AudioFromFolderLoaderCallbacks());
                } else getLoaderManager().initLoader(AUDIO_FROM_FOLDER_LOADER_ID, args, new AudioFromFolderLoaderCallbacks());
                fromOuterSpace=true;
            }
        }
        else {
           /* melodyList = util.getAllAudioFiles();
            util.sort(melodyList,sort_filter);*/
            getLoaderManager().initLoader(ALL_AUDIO_LOADER_ID, null, new AllAudioLoaderCallbacks());
        }
       /* for(Melody melody:melodyList){
            Log.d(TAG,melody.getTitle());
        }*/
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI();
            }
        };

        final ActionBar actionBar = ((MelodyListActivity)getActivity()).getSupportActionBar();
        setSlidingMenu();
        setSlidingMenuListeners();
        //actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.melody_list_fragment,container,false);
        musicListView = (ListView)view.findViewById(android.R.id.list);
        melodyInfo = (TextView)view.findViewById(R.id.melody_info);
        albumInfo = (TextView)view.findViewById(R.id.album_info);
        prev = (ImageButton)view.findViewById(R.id.prevTrack);
        prev.setBackgroundResource(R.drawable.prev_button_selector);
        play_pause = (ImageButton)view.findViewById(R.id.play_pause);
        play_pause.setBackgroundResource(R.mipmap.play);
        stop = (ImageButton)view.findViewById(R.id.stop);
        stop.setBackgroundResource(R.drawable.stop_button_selector);
        next = (ImageButton)view.findViewById(R.id.next);
        next.setBackgroundResource(R.drawable.next_button_selector);
        duration = (TextView)view.findViewById(R.id.full_duration);
        audioSeekBar = (SeekBar)view.findViewById(R.id.audioSeekBar);
        lastDuration = (TextView)view.findViewById(R.id.last_duration);
        /*adapter = new MelodiesAdapter(melodyList,getActivity());
        setListAdapter(adapter);*/
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_pause.setBackgroundResource(R.mipmap.pause);
                playPrev();
            }
        });
        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stoped){
                    play_pause.setBackgroundResource(R.mipmap.pause);
                    start();
                }else {
                    if (!paused) {
                        play_pause.setBackgroundResource(R.mipmap.play);
                        pause();
                    }
                    else {
                        play_pause.setBackgroundResource(R.mipmap.pause);
                        resume();
                    }
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_pause.setBackgroundResource(R.mipmap.play);
                stop();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_pause.setBackgroundResource(R.mipmap.pause);
                playNext();
            }
        });
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        initServiceList();
        //audioService.setMelodyList(melodyList);
        int index = Integer.parseInt(v.getTag().toString());
        if (!fromOuterSpace) {
            audioService.setMelody(index);
        }else {
            //audioService.setMelodyList(melodyList);
            audioService.setFromOuterSpace();
            fromOuterSpace=false;
        }
        start();
        play_pause.setBackgroundResource(R.mipmap.pause);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
        SearchManager sm = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchViewAction = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchViewAction.setSearchableInfo(sm.getSearchableInfo(getActivity().getComponentName()));
        searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /*melodyList = util.performSearch(query, search_filter);
                if (!melodyList.isEmpty()) {
                    audioService.setMelody(0);
                    audioService.setMelodyList(melodyList);
                    Log.d(TAG, "Search query:" + query + " Search result:" + melodyList.get(0).getTitle());
                    adapter = new MelodiesAdapter(melodyList,getActivity());
                    setListAdapter(adapter);
                    return true;
                }
                else {
                    Toast.makeText(getActivity(),"Nothing founded!",Toast.LENGTH_LONG).show();
                    melodyList = audioService.getMelodyList();
                    return false;
                }*/
                Bundle args= new Bundle();
                args.putString(ARG_QUERY, query);
                if(getLoaderManager().getLoader(SEARCH_AUDIO_LOADER_ID)!=null){
                    getLoaderManager().restartLoader(SEARCH_AUDIO_LOADER_ID, args, new SearchAudioLoaderCallbacks());
                }else getLoaderManager().initLoader(SEARCH_AUDIO_LOADER_ID, args, new SearchAudioLoaderCallbacks());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                menu.toggle(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MelodiesAdapter extends BaseAdapter{

        private ArrayList<Melody> melodies;
        private LayoutInflater inflater;

        public MelodiesAdapter(ArrayList<Melody> melodies, Context context) {
            this.melodies = melodies;
            inflater=LayoutInflater.from(context);
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Melody melody = melodies.get(position);
            LinearLayout melodyLayout = (LinearLayout)inflater.inflate(R.layout.melody_list_item, parent, false);
            ImageView listItemImage = (ImageView)melodyLayout.findViewById(R.id.item_image);
            if(melody.isPlaying()){
                listItemImage.setImageResource(R.mipmap.play);
            }
            TextView title_artist = (TextView)melodyLayout.findViewById(R.id.title_artist);
            title_artist.setText(melody.getTitle() + " : " +melody.getArtist());
            TextView albujm = (TextView)melodyLayout.findViewById(R.id.album);
            albujm.setText(melody.getAlbum());
            TextView duration = (TextView)melodyLayout.findViewById(R.id.melody_duration);
            duration.setText(Utils.durationFormater(melody.getDuration()));
            melodyLayout.setTag(position);
            return melodyLayout;
        }

        @Override
        public int getCount() {
           return melodyList.size();
        }
    }

    public void updateUI(){
        melodyList=audioService.getMelodyList();
        ((MelodiesAdapter)getListAdapter()).notifyDataSetChanged();
    }

    private void setSlidingMenu(){
        menu = new SlidingMenu(getActivity());
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowDrawable(R.drawable.slidingmenu_shadowgradient);
        menu.setShadowWidth(15);
        menu.setFadeDegree(0.0f);
        menu.attachToActivity(getActivity(), SlidingMenu.SLIDING_WINDOW);
        menu.setBehindWidth(getDisplayWidth() * 2);
        menu.setMenu(R.layout.menu_layout);
    }

    private void setSlidingMenuListeners(){
        menu.findViewById(R.id.all_songs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllSongs();
            }
        });
        menu.findViewById(R.id.from_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FileBrowserActivity.class);
                startActivityForResult(intent, REQUEST_PATH);
            }
        });
        RadioGroup search_filter_group = (RadioGroup)menu.findViewById(R.id.search_filter);
        search_filter_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton button;
                switch (checkedId) {
                    case R.id.title_search_filter:
                        button = (RadioButton) menu.findViewById(R.id.title_search_filter);
                        button.setChecked(true);
                        search_filter = MediaFilesUtil.SEARCH_FILTER.TITLE;
                        break;
                    case R.id.artist_search_filter:
                        button = (RadioButton) menu.findViewById(R.id.artist_search_filter);
                        button.setChecked(true);
                        search_filter = MediaFilesUtil.SEARCH_FILTER.ARTIST;
                        break;
                    case R.id.album_search_filter:
                        button = (RadioButton) menu.findViewById(R.id.album_search_filter);
                        button.setChecked(true);
                        search_filter = MediaFilesUtil.SEARCH_FILTER.ALBUM;
                        break;
                    default: search_filter = MediaFilesUtil.SEARCH_FILTER.TITLE;
                }
            }
        });
        final RadioGroup sort_filter_group = (RadioGroup)menu.findViewById(R.id.sort_filter);
        sort_filter_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton button;
                switch (checkedId) {
                    case R.id.title_sort_filter:
                        button = (RadioButton) menu.findViewById(R.id.title_sort_filter);
                        button.setChecked(true);
                        sort_filter = MediaFilesUtil.SORT_FILTER.TITLE;
                        util.sort(melodyList, sort_filter);
                        //TODO
                        audioService.setMelodyList(melodyList);
                        updateUI();
                        break;
                    case R.id.artist_sort_filter:
                        button = (RadioButton) menu.findViewById(R.id.artist_sort_filter);
                        button.setChecked(true);
                        sort_filter = MediaFilesUtil.SORT_FILTER.ARTIST;
                        util.sort(melodyList, sort_filter);
                        //TODO
                        audioService.setMelodyList(melodyList);
                        updateUI();
                        break;
                    case R.id.album_sort_filter:
                        button = (RadioButton) menu.findViewById(R.id.album_sort_filter);
                        button.setChecked(true);
                        sort_filter = MediaFilesUtil.SORT_FILTER.ALBUM;
                        util.sort(melodyList, sort_filter);
                        //TODO
                        audioService.setMelodyList(melodyList);
                        updateUI();
                        break;
                    default:
                        sort_filter = MediaFilesUtil.SORT_FILTER.ALBUM;
                        util.sort(melodyList, sort_filter);
                        //TODO
                        audioService.setMelodyList(melodyList);
                        updateUI();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode== FileBrowserActivity.RESULT_OK) {
            if (requestCode==REQUEST_PATH){
                String path = data.getStringExtra(FileBrowser.PATH);
                if(path!=null){
                    /*melodyList = util.getAudioFromFolder(path);
                    audioService.setMelodyList(melodyList);
                    adapter = new MelodiesAdapter(melodyList,getActivity());
                    setListAdapter(adapter);*/
                    Bundle args = new Bundle();
                    args.putString(ARG_DIR_PATH,path);
                    if(getLoaderManager().getLoader(AUDIO_FROM_FOLDER_LOADER_ID)!=null){
                        getLoaderManager().restartLoader(AUDIO_FROM_FOLDER_LOADER_ID, args, new AudioFromFolderLoaderCallbacks());
                    } else getLoaderManager().initLoader(AUDIO_FROM_FOLDER_LOADER_ID, args, new AudioFromFolderLoaderCallbacks());
                }
            }
        }
    }

    private int getDisplayWidth(){
        Configuration configuration = getActivity().getResources().getConfiguration();
        return configuration.screenWidthDp;
    }

    public void setAllSongs(){
        getLoaderManager().initLoader(ALL_AUDIO_LOADER_ID, null, new AllAudioLoaderCallbacks());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setController(){
        melodyInfo.setText(getActivity().getString(R.string.title)+":    "+audioService.getCurrentMelody().getTitle()+"     "
                +getActivity().getString(R.string.artist)+": "+audioService.getCurrentMelody().getArtist());
        albumInfo.setText(getActivity().getString(R.string.album)+": "+ audioService.getCurrentMelody().getAlbum());
        duration.setText(Utils.durationFormater(audioService.getCurrentMelody().getDuration()));
        lastDuration.setText(Utils.durationFormater(audioService.getCurrentMelody().getDuration()));
        finalTime = audioService.getCurrentMelody().getDuration();
        audioSeekBar.setMax((int) finalTime);
    }

    public int getDuration() {
        if(audioService!=null && isBounded && audioService.isPng())
            return audioService.getDur();
        else return 0;
    }

    public void play() {
        initServiceList();
        audioService.play();
        timeElapsed = getCurrentPosition();
        audioSeekBar.setProgress((int) timeElapsed);
        durationHandler.postDelayed(updateSeekBarTime, 1000);
    }

    public void stop(){
        initServiceList();
        stoped = true;
        audioService.stop();
    }

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            //get current position
            timeElapsed = getCurrentPosition();
            //set seekbar progress
            audioSeekBar.setProgress((int) timeElapsed);
            //set time remaing
            double timeRemaining = finalTime - timeElapsed;
            lastDuration.setText(Utils.durationFormater((long)timeRemaining));
            //repeat yourself that again in 100 miliseconds
            durationHandler.postDelayed(this, 1000);
        }
    };

    public void start(){
        stoped = false;
        paused=false;
        play();
        setController();
    }

    public void pause() {
        paused = true;
        audioService.pausePlayer();
    }

    public void resume(){
        paused=false;
        audioService.go();
    }

    public int getCurrentPosition() {
        return audioService.getPosn();
    }

    public void seekTo(int pos) {
        audioService.seek(pos);
    }

    public boolean isPlaying() {
        if(audioService!=null&&isBounded){
            audioService.isPng();
        }
        return false;
    }

    private void playNext(){
        initServiceList();
        setController();
        stoped = false;
        paused=false;
        audioService.playNext();
        //durationHandler.postDelayed(updateSeekBarTime, 1000);
    }

    private void playPrev(){
        initServiceList();
        setController();
        stoped = false;
        paused=false;
        audioService.playPrev();
        //durationHandler.postDelayed(updateSeekBarTime, 1000);
    }

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((receiver),
                new IntentFilter(AudioService.UPDATE_UI)
        );
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBounded) {
            getActivity().unbindService(musicConnection);
            isBounded = false;
        }
        durationHandler.removeCallbacks(updateSeekBarTime);
        durationHandler=null;
    }

    private void initServiceList(){
        if(!initilised){
            audioService.setMelodyList(melodyList);
            initilised=true;
        }
    }

    private class AllAudioLoaderCallbacks implements android.support.v4.app.LoaderManager.LoaderCallbacks<ArrayList<Melody>>{
        @Override
        public android.support.v4.content.Loader<ArrayList<Melody>> onCreateLoader(int id, Bundle args) {
            return new AllAudioLoader(getActivity(),util);
        }

        @Override
        public void onLoadFinished(android.support.v4.content.Loader<ArrayList<Melody>> loader, ArrayList<Melody> data) {
            melodyList=data;
            if(audioService!=null){
                audioService.setMelodyList(melodyList);
            }
            util.sort(data, sort_filter);
            adapter = new MelodiesAdapter(melodyList,getActivity());
            setListAdapter(adapter);
        }

        @Override
        public void onLoaderReset(android.support.v4.content.Loader<ArrayList<Melody>> loader) {

        }
    }

    private class AudioFromFolderLoaderCallbacks implements android.support.v4.app.LoaderManager.LoaderCallbacks<ArrayList<Melody>>{
        @Override
        public android.support.v4.content.Loader<ArrayList<Melody>> onCreateLoader(int id, Bundle args) {
            return new AudioFromFolderLoader(getActivity(),util, args.getString(ARG_DIR_PATH));
        }

        @Override
        public void onLoadFinished(android.support.v4.content.Loader<ArrayList<Melody>> loader, ArrayList<Melody> data) {
            melodyList = data;
            if(audioService!=null){
                audioService.setMelodyList(melodyList);
            }
            util.sort(melodyList, sort_filter);
            adapter = new MelodiesAdapter(melodyList,getActivity());
            setListAdapter(adapter);
        }

        @Override
        public void onLoaderReset(android.support.v4.content.Loader<ArrayList<Melody>> loader) {

        }
    }

    private class SearchAudioLoaderCallbacks implements android.support.v4.app.LoaderManager.LoaderCallbacks<ArrayList<Melody>>{
        @Override
        public android.support.v4.content.Loader<ArrayList<Melody>> onCreateLoader(int id, Bundle args) {
            return new SearchAudioLoader(getActivity(),util,args.getString(ARG_QUERY),search_filter);
        }

        @Override
        public void onLoadFinished(android.support.v4.content.Loader<ArrayList<Melody>> loader, ArrayList<Melody> data) {
            melodyList = data;
            if (!melodyList.isEmpty()) {
                audioService.setMelody(0);
                audioService.setMelodyList(melodyList);
                adapter = new MelodiesAdapter(melodyList,getActivity());
                setListAdapter(adapter);
            }
            else {
                Toast.makeText(getActivity(),"Nothing founded!",Toast.LENGTH_LONG).show();
                melodyList = audioService.getMelodyList();
                setListAdapter(adapter);
            }
        }

        @Override
        public void onLoaderReset(android.support.v4.content.Loader<ArrayList<Melody>> loader) {

        }
    }
}


