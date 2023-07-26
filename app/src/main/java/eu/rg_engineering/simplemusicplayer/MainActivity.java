package eu.rg_engineering.simplemusicplayer;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
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

import eu.rg_engineering.simplemusicplayer.ui.home.HomeFragment;




public class MainActivity extends AppCompatActivity
        implements
            MusicItemsAdapter.MusicItemsAdapterListener,
        HomeFragment.HomeFragmentListener {

    private AppBarConfiguration mAppBarConfiguration;

    private String TAG = "Main";
    ExoPlayer exoPlayer;
    /* mediaplayer
    MediaPlayer music;

     */
    Timer progressTimer;

    @Override
    public void messageFromMusicItemsAdapter(String msg, String params) {

        Log.d(TAG, "got message from MusicItemsAdapter " + msg + " " + params);

        switch (msg) {

            case "PlayMusic":
                musicplay(params);
                break;

        }
    }

    @Override
    public void messageFromHomeFragment(String msg, String params) {

        Log.d(TAG, "got message from HomeFragment " + msg + " " + params);

        switch (msg) {

            case "PlayMusic":
                musicplay();
                break;
            case "PauseMusic":
                musicpause();
                break;
            case "StopMusic":
                musicstop();
                break;

        }
    }


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

                R.id.nav_home, R.id.nav_settings)
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

    private HomeFragment FindHomeFragment() {

        HomeFragment homeFragment = null;
        FragmentManager manager = getSupportFragmentManager();
        if (manager != null) {
            Fragment mainFragment = (Fragment) manager.findFragmentById(R.id.nav_host_fragment);
            if (mainFragment != null) {
                FragmentManager subManager = mainFragment.getChildFragmentManager();
                if (subManager != null) {

                    List<Fragment> allFrag = subManager.getFragments();
                    homeFragment = (HomeFragment) allFrag.get(0);
                    //homeFragment = (HomeFragment) subManager.findFragmentById(R.id.nav_home);
                }
            }
        }
        return homeFragment;
    }

    // Playing the music
    private void musicplay(String filename) {

        if (exoPlayer != null) {
            exoPlayer.stop();
        }

        exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();

        Uri uri = Uri.parse(filename);
        // Build the media item.
        MediaItem mediaItem = MediaItem.fromUri(uri);
        // Set the media item to be played.
        exoPlayer.setMediaItem(mediaItem);
        // Prepare the player.
        exoPlayer.prepare();

        CreateMusic();

        // Start the playback.
        exoPlayer.play();



        /* mediaplayer
        if (music != null) {
            music.stop();
        }
        Uri uri = Uri.parse(filename);
        music = MediaPlayer.create(this, uri);
        CreateMusic();
        music.start();

         */
    }

    private void musicplay() {

        if (exoPlayer != null) {
            exoPlayer.play();
        }

        /* mediaplayer
        if (music != null) {
            music.start();
        }

         */
    }

    // Pausing the music
    private void musicpause() {

        if (exoPlayer != null) {
            exoPlayer.pause();
        }

        /* mediaplayer
        if (music != null) {
            music.pause();
        }

         */
    }

    // Stopping the music
    private void musicstop() {

        if (exoPlayer != null) {
            exoPlayer.release();
        }

        /* mediaplayer
        if (music != null) {
            music.stop();
        }
        //music = MediaPlayer.create(this, R.raw.antihero);
        */

        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer.purge();
        }



    }

    private void CreateMusic() {

        /* mediaplayer
        music.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "Song Complete");

                HomeFragment homefragment = FindHomeFragment();
                if (homefragment != null) {
                    homefragment.GetNextSong();
                } else {
                    Log.e(TAG, "homefragement not found");
                }

            }
        });
         */
        //todo listener to add

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
                                Log.d(TAG, "Song Complete");

                                HomeFragment homefragment = FindHomeFragment();
                                if (homefragment != null) {
                                    homefragment.GetNextSong();
                                } else {
                                    Log.e(TAG, "homefragement not found");
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


    class UpdateProgressTask extends TimerTask {
        public void run() {
            runOnUiThread(UpdateProgress);
        }
    }

    private Runnable UpdateProgress = new Runnable() {
        public void run() {

            HomeFragment homefragment = FindHomeFragment();
            if (homefragment != null) {
                /* mediaplayer
                int position = music.getCurrentPosition();

                 */

                long position = exoPlayer.getCurrentPosition();
                Log.d(TAG, "current position " + position);
                homefragment.SetCurrentplaytime(position);
            }
        }
    };

}
