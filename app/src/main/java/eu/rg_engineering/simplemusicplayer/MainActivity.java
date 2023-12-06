package eu.rg_engineering.simplemusicplayer;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import eu.rg_engineering.simplemusicplayer.PlexServer.Plex_FindArtists;
import eu.rg_engineering.simplemusicplayer.ui.home.HomeFragment_old;


//todo: car intergration https://github.com/google/ExoPlayer/issues/8561
//todo: spielt nur 2 oder drei songs und stoppt dann
public class MainActivity extends AppCompatActivity
        implements
            MusicItemsAdapter.MusicItemsAdapterListener,
        ArtistItemsAdapter.ArtistItemsAdapterListener,
        HomeFragment_old.HomeFragmentListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private AppBarConfiguration mAppBarConfiguration;

    private String TAG = "Main";
    private ExoPlayer exoPlayer;
    /* mediaplayer
    private MediaPlayer music;

     */
    private Timer progressTimer;
    //private discoverServer  discover;
    //private ScanNASFolder scanNASFolder;

    private boolean OnlyOneSong=true;

    @Override
    public void messageFromMusicItemsAdapter(String msg, String params) {

        Log.d(TAG, "got message from MusicItemsAdapter " + msg + " " + params);

        switch (msg) {

            case "PlayMusic":
                musicplay(params);
                break;
            case "NoSongs":
                Toast.makeText(this, "This is my Toast message!",
                        Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void messageFromHomeFragment(String msg, String params) {

        Log.d(TAG, "got message from HomeFragment " + msg + " " + params);

        switch (msg) {

            case "PlayMusic":
                GetNextSong();
                OnlyOneSong=false;
                break;
            case "PauseMusic":
                musicpause();
                break;
            case "StopMusic":
                musicstop();
                OnlyOneSong=true;
                break;
            case "ReplaceFragment":
                //replaceFragment("SearchArtist");
                break;
        }
    }

    @Override
    public void messageFromArtistItemsAdapter(String msg, String params) {
        Log.d(TAG, "got message from ArtistFragment " + msg + " " + params);
    }

/*
    public void replaceFragment(String FragmentName) {

        Fragment fragment = null;
        if (FragmentName=="SearchArtist"){

        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();


    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        //fragments wieder rein: activity_main_drawer.xml und mobile_navigation.xml
        mAppBarConfiguration = new AppBarConfiguration.Builder(

                R.id.nav_home,R.id.nav_artists, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //show version info
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "exception in onCreate " + e.getMessage());
        }
        String Version = packageinfo.versionName.toString();

        View header = navigationView.getHeaderView(0);
        TextView text = (TextView) header.findViewById(R.id.VersionView);
        text.setText(Version);

        //discover = new discoverServer(this);
        //discover.start();

        //scanNASFolder = new ScanNASFolder();
        //scanNASFolder.execute();


        Plex_FindArtists plex_FindArtists = new Plex_FindArtists();
        plex_FindArtists.start();


        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "need more permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
        else {
            Log.d(TAG, "permission set correctly");
        }
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "need more permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_MEDIA_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
        else {
            Log.d(TAG, "permission set correctly");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //SaveData();
    }

    private HomeFragment_old FindHomeFragment() {

        HomeFragment_old homeFragment = null;
        FragmentManager manager = getSupportFragmentManager();
        if (manager != null) {
            Fragment mainFragment = (Fragment) manager.findFragmentById(R.id.nav_host_fragment);
            if (mainFragment != null) {
                FragmentManager subManager = mainFragment.getChildFragmentManager();
                if (subManager != null) {

                    List<Fragment> allFrag = subManager.getFragments();
                    homeFragment = (HomeFragment_old) allFrag.get(0);
                    //homeFragment = (HomeFragment) subManager.findFragmentById(R.id.nav_home);
                }
            }
        }
        return homeFragment;
    }

    // Playing the music
    private void musicplay(String filename) {

        Log.d(TAG, "musicplay " + filename);
       /*
        if (exoPlayer != null) {
            exoPlayer.stop();
            Log.d(TAG, "player stopped ");
        }
*/
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
            Log.d(TAG, "builder called ");
            CreateMusic();
        }

        //original:
        //Uri uri = Uri.parse(filename);

        //funktioniert:
        Uri uri = Uri.parse("http://192.168.3.21:32400/library/parts/48571/1261258691/file.mp3?X-Plex-Token=LAtVbxshNWzuGUwtm8bJ");

        //funktioniert:
        //Uri uri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3");

        // Build the media item.
        MediaItem mediaItem = MediaItem.fromUri(uri);

        // Set the media item to be played.
        exoPlayer.setMediaItem(mediaItem);
        Log.d(TAG, "media item set " );
        // Prepare the player.
        exoPlayer.prepare();
        Log.d(TAG, "player prepared " );

        try {
            // Start the playback.
            exoPlayer.play();
        }
        catch (Exception e){
            Log.e(TAG, "exception in musicplay " + e.getMessage());
        }



    }

    private void musicplay() {

        if (exoPlayer != null) {

            //first stop
            exoPlayer.stop();

            //then start
            exoPlayer.play();
        }


    }

    // Pausing the music
    private void musicpause() {

        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    // Stopping the music
    private void musicstop() {

        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer.purge();
        }
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer=null;
        }
    }

    private void CreateMusic() {

        exoPlayer.addListener(
                new Player.Listener() {
                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        if (isPlaying) {
                            // Active playback.
                            Log.i(TAG, "is playing");
                        } else {
                            // Not playing because playback is paused, ended, suppressed, or the player
                            // is buffering, stopped or failed. Check player.getPlayWhenReady,
                            // player.getPlaybackState, player.getPlaybackSuppressionReason and
                            // player.getPlaybackError for details.
                            Log.i(TAG, "is not playing " + exoPlayer.getPlaybackState());

                            if (exoPlayer.getPlaybackState()==Player.STATE_ENDED){
                                Log.d(TAG, "Song Complete " + OnlyOneSong);
                                if (!OnlyOneSong) {
                                    GetNextSong();
                                }
                                else {
                                    musicstop();
                                }
                            }
                        }
                    }
                });

        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer.purge();
        }

        progressTimer = new Timer();
        TimerTask updateProgress = new UpdateProgressTask();
        progressTimer.scheduleAtFixedRate(updateProgress, 0, 1000);
    }




    private void GetNextSong(){
        Log.d(TAG, "get next song");
        HomeFragment_old homefragment = FindHomeFragment();
        if (homefragment != null) {
            homefragment.GetNextSong();
        } else {
            Log.e(TAG, "homefragement not found");
        }
    }



    class UpdateProgressTask extends TimerTask {
        public void run() {
            runOnUiThread(UpdateProgress);
        }
    }

    private Runnable UpdateProgress = new Runnable() {
        public void run() {

            HomeFragment_old homefragment = FindHomeFragment();
            if (homefragment != null) {
                /* mediaplayer
                int position = music.getCurrentPosition();

                 */

                if (exoPlayer != null) {
                    long position = exoPlayer.getCurrentPosition();
                    //Log.d(TAG, "current position " + position);
                    homefragment.SetCurrentplaytime(position);
                }
            }
        }
    };

}
