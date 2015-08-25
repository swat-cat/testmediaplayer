package com.swat_cat.com.testmediaplayer.Controllers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.swat_cat.com.testmediaplayer.Model.Item;
import com.swat_cat.com.testmediaplayer.R;
import com.swat_cat.com.testmediaplayer.Utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Dell on 03.07.2015.
 */
public class FileBrowser extends ListFragment {

    private static final String TAG = FileBrowser.class.getName();
    protected static final String PATH = TAG + "dirr path";
    private File currentDir;
    private ArrayList<String> exceptedExstentions;
    private ListView listView;
    ImageButton upButton;
    private TextView currentLocation;
    private FileArrayAdpter adpter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        currentDir = Environment.getExternalStorageDirectory();
        exceptedExstentions = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.audio_extensions)));
        fillFileList(currentDir);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_browser_fragment,container,false);
        listView = (ListView)view.findViewById(android.R.id.list);
        upButton = (ImageButton)view.findViewById(R.id.button_up);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File parentDir = currentDir.getParentFile();
                if (!currentDir.getName().equals("0")) {
                    fillFileList(parentDir);
                    currentDir=parentDir;
                    updatePathView();
                }
            }
        });
        currentLocation = (TextView)view.findViewById(R.id.current_dir_text);
        currentLocation.setText(currentDir.getAbsolutePath());
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.browser_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.choose_folder:
                returnResult();break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Item item = adpter.getItem(position);
        if(item.isDirectory()){
            currentDir = new File(item.getPath());
            fillFileList(currentDir);
            updatePathView();
        }
        else playFile(item.getPath());
    }

    public void playFile(String path){
        //TODO
    }

    public void returnResult(){
        Intent intent = new Intent(getActivity(),MelodyListFragment.class);
        intent.putExtra(PATH,currentDir.getAbsolutePath());
        getActivity().setResult(Activity.RESULT_OK,intent);
        getActivity().finish();
    }

    private void fillFileList(File directory){
        File[]files = directory.listFiles();
        ArrayList<Item> dirs = new ArrayList<>();
        ArrayList<Item> fls = new ArrayList<>();
        ContentResolver contentResolver = getActivity().getContentResolver();
        for(File file:files){
            if(!file.isDirectory()){
                String ext = Utils.getFileExt(file.getName());
                if (exceptedExstentions.contains(ext)) {
                    fls.add(new Item(file.getName(),file.getAbsolutePath(),false));
                }
            }else dirs.add(new Item(file.getName(),file.getAbsolutePath(),true));
        }
        Collections.sort(dirs);
        Collections.sort(fls);
        dirs.addAll(fls);
        adpter = new FileArrayAdpter(dirs);
        setListAdapter(adpter);
    }



    private class FileArrayAdpter extends ArrayAdapter<Item>{
        private ArrayList<Item> items;
        public FileArrayAdpter(ArrayList<Item> items){
            super(getActivity(),0,items);
            this.items=items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.file_browser_item,parent,false);
            }
            Item item = getItem(position);
            ImageView itemImage = (ImageView)convertView.findViewById(R.id.browser_item_image);
            if(item.isDirectory())
                itemImage.setImageResource(R.mipmap.folder);
            else itemImage.setImageResource(R.mipmap.ic_action);
            TextView itemText = (TextView)convertView.findViewById(R.id.browser_item_text);
            itemText.setText(item.getName());
            return convertView;
        }

        @Override
        public int getCount() {
            return items.size();
        }
    }

    public void updatePathView(){
        currentLocation.setText(currentDir.getAbsolutePath());
    }
}
