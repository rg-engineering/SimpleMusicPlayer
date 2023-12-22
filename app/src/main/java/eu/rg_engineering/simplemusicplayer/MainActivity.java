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
import androidx.fragment.app.FragmentTransaction;
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

import java.util.Timer;
import java.util.TimerTask;

import eu.rg_engineering.simplemusicplayer.MusicData.MusicData;
import eu.rg_engineering.simplemusicplayer.PlexServer.Plex_FindData;
import eu.rg_engineering.simplemusicplayer.ui.home.AlbumsFragment;
import eu.rg_engineering.simplemusicplayer.ui.home.ArtistsFragment;
import eu.rg_engineering.simplemusicplayer.ui.home.HomeFragment;
import eu.rg_engineering.simplemusicplayer.ui.home.TracksFragment;


//todo: car intergration https://github.com/google/ExoPlayer/issues/8561

public class MainActivity extends AppCompatActivity
        implements
            MusicItemsAdapter.MusicItemsAdapterListener,
            ArtistItemsAdapter.ArtistItemsAdapterListener,
            AlbumItemsAdapter.AlbumItemsAdapterListener,
            TrackItemsAdapter.TrackItemsAdapterListener,
            HomeFragment.HomeFragmentListener,
            TracksFragment.TracksFragmentListener,
            AlbumsFragment.AlbumsFragmentListener,
            Plex_FindData.PlexFindArtistListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private AppBarConfiguration mAppBarConfiguration;

    private String TAG = "Main";
    private ExoPlayer exoPlayer;
    private Timer progressTimer;
    //private discoverServer  discover;
    //private ScanNASFolder scanNASFolder;
    public MusicData mMusicData;

    private boolean OnlyOneSong = true;
    private boolean SongNotFinished = true;

    private ArtistsFragment mArtistsFragment;
    private AlbumsFragment mAlbumsFragment;
    private TracksFragment mTracksFragment;

    private String nextSongFrom = "";

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
            default:
                Log.e(TAG, "unknown message " + msg);
                break;
        }
    }

    @Override
    public void messageFromHomeFragment(String msg, String params) {

        Log.d(TAG, "got message from HomeFragment " + msg + " " + params);

        switch (msg) {

            case "PlayMusic":
                GetNextSong();
                OnlyOneSong = false;
                break;
            case "PauseMusic":
                musicpause();
                break;
            case "StopMusic":
                musicstop();
                OnlyOneSong = true;
                SongNotFinished = false;
                break;

            default:
                Log.e(TAG, "unknown message " + msg);
                break;
        }
    }

    @Override
    public void messageFromTracksFragment(String msg, String params) {

        Log.d(TAG, "got message from TracksFragment " + msg + " " + params);

        switch (msg) {

            case "btnBack":
                replaceFragment(mAlbumsFragment);
                break;

            default:
                Log.e(TAG, "unknown message " + msg);
                break;
        }
    }

    @Override
    public void messageFromAlbumsFragment(String msg, String params) {

        Log.d(TAG, "got message from AlbumsFragment " + msg + " " + params);

        switch (msg) {

            case "btnBack":
                replaceFragment(mArtistsFragment);
                break;

            default:
                Log.e(TAG, "unknown message " + msg);
                break;
        }
    }

    @Override
    public void messageFromArtistItemsAdapter(String msg, String params, String artistName) {
        Log.d(TAG, "got message from ArtistFragment " + msg + " " + params + " " + artistName);

        switch (msg) {
            case "ShowInfo":
                Toast.makeText(this, params, Toast.LENGTH_LONG).show();
                break;
            case "ArtistSelected":
                mMusicData.SetArtist4Album(params, artistName);

                if (mAlbumsFragment == null) {
                    mAlbumsFragment = new AlbumsFragment();
                }
                //open fragment "Album"
                replaceFragment(mAlbumsFragment);
                break;
            default:
                Log.e(TAG, "unknown message " + msg);
                break;
        }
    }

    @Override
    public void messageFromAlbumItemsAdapter(String msg, String params, String albumName) {
        Log.d(TAG, "got message from AlbumFragment " + msg + " " + params + " " + albumName);

        switch (msg) {
            case "ShowInfo":
                Toast.makeText(this, params, Toast.LENGTH_LONG).show();
                break;
            case "AlbumSelected":
                mMusicData.SetAlbum4Track(params, albumName);

                if (mTracksFragment == null) {
                    mTracksFragment = new TracksFragment();
                }
                //open fragment "Track"
                replaceFragment(mTracksFragment);
                break;
            default:
                Log.e(TAG, "unknown message " + msg);
                break;
        }
    }

    @Override
    public void messageFromTrackItemsAdapter(String msg, String params) {
        Log.d(TAG, "got message from TrackFragment " + msg + " " + params);

        switch (msg) {

            case "PlayMusic":
                musicplay(params);
                nextSongFrom = "TrackItemsAdapter";
                break;
            default:
                Log.e(TAG, "unknown message " + msg);
                break;
        }

    }

    @Override
    public void messageFromPlexFindArtist(String msg) {
        Log.d(TAG, "got message from PlexFindArtist " + msg);

        switch (msg) {
            case "got all artists ": //Leerzeichen ist hier wichtig...
                //plex founds all artists, now update View
                if (mArtistsFragment != null) {
                    mArtistsFragment.ReadPlexArtistData();
                } else {
                    Log.e(TAG, "artistfragment not found");
                }
                break;
            case "got all albums ": //Leerzeichen ist hier wichtig...
                //plex founds all artists, now update View
                if (mAlbumsFragment != null) {
                    mAlbumsFragment.ReadPlexAlbumData();
                } else {
                    Log.e(TAG, "albumfragment not found");
                }
                break;

            case "got all tracks ": //Leerzeichen ist hier wichtig...
                //plex founds all artists, now update View
                if (mTracksFragment != null) {
                    mTracksFragment.ReadPlexTrackData();
                } else {
                    Log.e(TAG, "trackfragment not found");
                }
                break;

            default:
                Log.e(TAG, "unknown message " + msg);
                break;
        }
    }


    public void replaceFragment(Fragment fragmentName) {


        if (fragmentName != null) {
            Log.d(TAG, "replace fragment " + fragmentName);
            // create a FragmentManager
            FragmentManager fm = getSupportFragmentManager();
            // create a FragmentTransaction to begin the transaction and replace the Fragment
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            // replace the FrameLayout with new Fragment
            fragmentTransaction.replace(R.id.mainframeLayout, fragmentName);
            fragmentTransaction.commit(); // save the changes

        } else {
            Log.e(TAG, "fragment not found, null");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mMusicData = new MusicData(this);

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


        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "need more permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "permission set correctly");
        }
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "need more permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_MEDIA_AUDIO},
                    PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "permission set correctly");
        }

        mArtistsFragment = new ArtistsFragment();
        replaceFragment(mArtistsFragment);

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

    // Playing the music
    private void musicplay(String filename) {

        Log.d(TAG, "musicplay " + filename);

        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
            Log.d(TAG, "builder called ");
            CreateMusic();
        }

        //original:
        Uri uri = Uri.parse(filename);

        // Build the media item.
        MediaItem mediaItem = MediaItem.fromUri(uri);

        // Set the media item to be played.
        exoPlayer.setMediaItem(mediaItem);
        Log.d(TAG, "media item set ");
        // Prepare the player.
        exoPlayer.prepare();
        Log.d(TAG, "player prepared ");

        try {
            // Start the playback.
            exoPlayer.play();
        } catch (Exception e) {
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
            exoPlayer = null;
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
                            SongNotFinished = true;
                        } else {
                            // Not playing because playback is paused, ended, suppressed, or the player
                            // is buffering, stopped or failed. Check player.getPlayWhenReady,
                            // player.getPlaybackState, player.getPlaybackSuppressionReason and
                            // player.getPlaybackError for details.
                            Log.i(TAG, "is not playing " + exoPlayer.getPlaybackState());

                            if (exoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                                Log.d(TAG, "Song Complete " + OnlyOneSong);
                                SongNotFinished = false;
                                if (!OnlyOneSong) {
                                    GetNextSong();
                                } else {
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


    private void GetNextSong() {
        Log.d(TAG, "get next song from " + nextSongFrom);

        switch (nextSongFrom) {
            case "TrackItemsAdapter":
                if (mTracksFragment != null) {
                    //wenn noch nicht fertig, dann aktuellen Song holen
                    if (SongNotFinished) {
                        mTracksFragment.GetCurrentSong();
                    } else {
                        mTracksFragment.GetNextSong();
                    }
                }
                break;

            default:
                Log.e(TAG, "no source of tracks");

                Toast.makeText(this, "no track selected.", Toast.LENGTH_LONG).show();
                break;
        }

    }


    class UpdateProgressTask extends TimerTask {
        public void run() {
            runOnUiThread(UpdateProgress);
        }
    }

    private Runnable UpdateProgress = new Runnable() {
        public void run() {
            long position = exoPlayer.getCurrentPosition();
            if (mTracksFragment != null) {
                mTracksFragment.SetCurrentplaytime(position);
            }
        }
    };

}
