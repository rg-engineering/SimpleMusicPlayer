package eu.rg_engineering.simplemusicplayer;



import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.ErrorMessageProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import androidx.media3.ui.PlayerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import eu.rg_engineering.simplemusicplayer.MusicData.MusicData;
import eu.rg_engineering.simplemusicplayer.MusicData.MusicService;
import eu.rg_engineering.simplemusicplayer.PlexServer.Plex_FindData;
import eu.rg_engineering.simplemusicplayer.ui.home.AlbumsFragment;
import eu.rg_engineering.simplemusicplayer.ui.home.ArtistsFragment;
import eu.rg_engineering.simplemusicplayer.ui.home.HomeFragment;
import eu.rg_engineering.simplemusicplayer.ui.home.PlaylistFragment;
import eu.rg_engineering.simplemusicplayer.ui.home.TracksFragment;
import io.sentry.Sentry;



//todo Rückkehr von "Einstellungen" oder "About" muss Bibliothek oder playlist öffnen
//todo in Einstellungen link hinzufügen, wie man den Token ermittelt
//todo Auswahl der plex-Bibliothek (derzeit automatisch bzw. in Einstellungen)
//todo überall Sentry catches einbauen
//todo link zu Sentry-Informtionen
//todo sentry abschaltbar machen
//todo Albums mit various interpretes werden nicht gefunden (lokale Bibliothek) z.Bsp. Bravo Hits
//todo info text album und artist) muss in echte Textbox


public class MainActivity extends AppCompatActivity
        implements
            ArtistItemsAdapter.ArtistItemsAdapterListener,
            AlbumItemsAdapter.AlbumItemsAdapterListener,
            TrackItemsAdapter.TrackItemsAdapterListener,
            PlaylistItemsAdapter.PlaylistItemsAdapterListener,
            HomeFragment.HomeFragmentListener,
            TracksFragment.TracksFragmentListener,
            AlbumsFragment.AlbumsFragmentListener,
            PlaylistFragment.PlaylistFragmentListener,
            Plex_FindData.PlexFindArtistListener ,
            PlayerView.ControllerVisibilityListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private AppBarConfiguration mAppBarConfiguration;
    private final String TAG = "Main";
    private Timer progressTimer;
    public MusicData mMusicData;
    private ArtistsFragment mArtistsFragment;
    private AlbumsFragment mAlbumsFragment;
    private TracksFragment mTracksFragment;
    private PlaylistFragment mPlaylistFragment;
    //private String nextSongFrom = "";
    protected PlayerView playerView;
    private List<MediaItem> mediaItems;
    ListenableFuture<MediaController> controllerFuture;
    private String mPlayStartedFrom;

    @Override
    public void messageFromHomeFragment(String msg, String params) {

        Log.d(TAG, "got message from HomeFragment " + msg + " " + params);

        switch (msg) {
            case "created":
                replaceFragment(mArtistsFragment);
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
                stopMusic();
                setPlayControllerHeight(10);
                break;
            case "PlexDataRead":
                break;
            default:
                Log.e(TAG, "unknown message " + msg);
                break;
        }
    }

    @Override
    public void messageFromAlbumsFragment(String msg, String params) {

        Log.d(TAG, "got message from AlbumsFragment " + msg + " " + params);

        if (msg.equals("btnBack")) {
            replaceFragment(mArtistsFragment);
        } else {
            Log.e(TAG, "unknown message " + msg);
        }
    }

    @Override
    public void messageFromArtistItemsAdapter(String msg, String params, ArrayList<String> data) {
        Log.d(TAG, "got message from ArtistFragment " + msg + " " + params + " " + data.get(0));

        switch (msg) {
            case "ShowInfo":
                Toast.makeText(this, params, Toast.LENGTH_LONG).show();
                break;
            case "ArtistSelected":
                mMusicData.SetArtist4Album( data);

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
    public void messageFromTrackItemsAdapter(String msg, ArrayList<String> params, ArrayList<TrackData> tracks) {
        Log.d(TAG, "got message from TrackItemsAdapter " + msg + " " + params);

        switch (msg) {
            case "IsReady":
                GetSongs("TrackItemsAdapter");
                break;
            case "PlayFromCurrentPos":
                startFromPos( Integer.parseInt(params.get(0))  );
                break;
            case "UpdatePlayList":
                UpdatePlayList(tracks);
                startMusic();
                mPlayStartedFrom="TrackItemsAdapter";
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

    @Override
    public void messageFromPlaylistFragment(String msg, String params) {
        Log.d(TAG, "got message from PlaylistFragment " + msg);

        switch (msg) {

            default:
                Log.e(TAG, "unknown message " + msg);
                break;
        }
    }

    @Override
    public void messageFromPlaylistItemsAdapter(String msg, ArrayList<String> params, ArrayList<TrackData> tracks) {
        Log.d(TAG, "got message from PlaylistItemsAdapter " + msg);

        switch (msg) {
            case "IsReady":
                GetSongs("PlaylistItemsAdapter");
                break;
            case "PlayFromCurrentPos":
                startFromPos( Integer.parseInt(params.get(0))  );
                break;
            case "UpdatePlayList":
                UpdatePlayList(tracks);
                startMusic();
                mPlayStartedFrom="PlaylistItemsAdapter";
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
            fragmentTransaction.addToBackStack(null);
            //}
            fragmentTransaction.commit(); // save the changes

        } else {
            Log.e(TAG, "fragment not found, null");
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMusicData = new MusicData(this);

        playerView = findViewById(R.id.player_view);
        playerView.setControllerVisibilityListener(this);
        playerView.setErrorMessageProvider(new PlayerErrorMessageProvider());

        playerView.setControllerHideOnTouch(false);
        playerView.setControllerShowTimeoutMs(0);

        playerView.setControllerAutoShow(true);
        playerView.setShowRewindButton(true);
        playerView.setShowFastForwardButton(true);
        playerView.setShowNextButton(true);
        playerView.setShowPreviousButton(true);

        setPlayControllerHeight(10);

        playerView.requestFocus();

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
            Sentry.captureException(e);
        }
        String Version = packageinfo.versionName;

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
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_AUDIO},
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            Log.d(TAG, "permission set correctly");
        }

        CreateMediaController();

        RadioButton rbShowLib = (RadioButton) findViewById(R.id.showLibrary);
        RadioButton rbShowPlaylist = (RadioButton) findViewById(R.id.showPlaylist);
        rbShowLib.setChecked(true);

        mArtistsFragment = new ArtistsFragment();
        mPlaylistFragment = new PlaylistFragment();

        rbShowLib.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d(TAG, "show lib clicked");
                        stopMusic();
                        replaceFragment(mArtistsFragment);
                    }
                });
        rbShowPlaylist.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d(TAG, "show playlist clicked");
                        stopMusic();
                        replaceFragment(mPlaylistFragment);
                    }
                });

        //todo wenn Playlist empty, dann auf Bibliothek, andernfalls auf Playlist schalten
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

    private void GetSongs(String target) {
        Log.d(TAG, "get songs from " + target);

        if (target.equals("PlaylistItemsAdapter")) {
            if (mPlaylistFragment != null) {
                mPlaylistFragment.GetSongs();
            }
        } else if (target.equals("TrackItemsAdapter")) {
            if (mTracksFragment != null) {
                mTracksFragment.GetSongs();
            }
        }
    }

    private void UpdatePlayList(ArrayList<TrackData> tracks) {

        Log.d(TAG, "update UpdatePlayList" + tracks.size());
        try {
            mediaItems.clear();

            for (TrackData track : tracks) {

                MediaItem item =
                        new MediaItem.Builder()
                                .setMediaId("media-1")
                                .setUri(track.Url)
                                .setMediaMetadata(
                                        new MediaMetadata.Builder()
                                                .setArtist(track.Artist)
                                                .setTitle(track.TrackName)
                                                //.setArtworkUri("")
                                                .build())
                                .build();

                mediaItems.add(item);
            }

            controllerFuture.get().addMediaItems(mediaItems);
        } catch (Exception ex) {
            Log.e(TAG, "exception in  UpdatePlayList" + ex);
        }
    }

    @Override
    public void onVisibilityChanged(int visibility) {
        Log.w(TAG, "onVisibilityChanged called " + visibility);
    }




    class UpdateProgressTask extends TimerTask {
        public void run() {
            runOnUiThread(UpdateProgress);
        }
    }

    private final Runnable UpdateProgress = new Runnable() {
        public void run() {
            try {
                long position = controllerFuture.get().getCurrentPosition();
                int index = controllerFuture.get().getCurrentMediaItemIndex();


                if (mPlayStartedFrom.equals("PlaylistItemsAdapter")) {
                    if (mPlaylistFragment != null) {
                        mPlaylistFragment.SetCurrentplaytime(index, position);
                    }
                }
                if (mPlayStartedFrom.equals("TrackItemsAdapter")) {
                    if (mTracksFragment != null) {
                        mTracksFragment.SetCurrentplaytime(index, position);
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, "exception in UpdateProgress  " + ex);
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        if (Build.VERSION.SDK_INT > 23) {
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        ReconnectMediaController();

        if (Build.VERSION.SDK_INT <= 23) {
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (Build.VERSION.SDK_INT <= 23) {
            if (playerView != null) {
                playerView.onPause();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (Build.VERSION.SDK_INT > 23) {
            if (playerView != null) {
                playerView.onPause();
            }
        }
        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer.purge();
        }
        //MediaController.releaseFuture(controllerFuture);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer.purge();
        }
        MediaController.releaseFuture(controllerFuture);
    }

    private void createMediaItems() {
        mediaItems = mMusicData.createMediaItems();
    }

    private class PlayerErrorMessageProvider implements ErrorMessageProvider<PlaybackException> {

        @Override
        public Pair<Integer, String> getErrorMessage(PlaybackException e) {

            String errorString = "playback error, todo";
            /*
            String errorString = getString(R.string.error_generic);

            Throwable cause = e.getCause();
            if (cause instanceof DecoderInitializationException) {
                // Special case for decoder initialization failures.
                DecoderInitializationException decoderInitializationException =
                        (DecoderInitializationException) cause;
                if (decoderInitializationException.codecInfo == null) {
                    if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString =
                                getString(
                                        R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                    } else {
                        errorString =
                                getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                    }
                } else {
                    errorString =
                            getString(
                                    R.string.error_instantiating_decoder,
                                    decoderInitializationException.codecInfo.name);
                }
            }

             */
            return Pair.create(0, errorString);
        }
    }

    private void CreateMediaController() {
        SessionToken sessionToken = new SessionToken(this, new ComponentName(this, MusicService.class));

        controllerFuture = new MediaController.Builder(this, sessionToken).buildAsync();
        controllerFuture.addListener(() -> {
            // MediaController is available here with controllerFuture.get()
            Log.w(TAG, "MediaController connected ");
            createMediaItems();
            try {
                controllerFuture.get().addMediaItems(mediaItems);

                playerView.setPlayer(controllerFuture.get());

                controllerFuture.get().addListener(
                        new Player.Listener() {
                            @OptIn(markerClass = UnstableApi.class)
                            @Override
                            public void onIsPlayingChanged(boolean isPlaying) {
                                if (isPlaying) {

                                    setPlayControllerHeight(200);

                                    // Active playback.
                                    Log.i(TAG, "is playing");
                                    if (progressTimer != null) {
                                        progressTimer.cancel();
                                        progressTimer.purge();
                                    }

                                    progressTimer = new Timer();
                                    TimerTask updateProgress = new UpdateProgressTask();
                                    progressTimer.scheduleAtFixedRate(updateProgress, 0, 1000);
                                } else {
                                    // Not playing because playback is paused, ended, suppressed, or the player
                                    // is buffering, stopped or failed. Check player.getPlayWhenReady,
                                    // player.getPlaybackState, player.getPlaybackSuppressionReason and
                                    // player.getPlaybackError for details.
                                    try {
                                        Log.i(TAG, "is not playing " + controllerFuture.get().getPlaybackState());

                                        if (controllerFuture.get().getPlaybackState() == Player.STATE_ENDED) {
                                            Log.d(TAG, "Song Complete ");
                                            if (progressTimer != null) {
                                                progressTimer.cancel();
                                                progressTimer.purge();
                                            }

                                        }
                                    } catch (Exception ex) {
                                        Log.e(TAG, "exception in onIsPlayingChanged " + ex);
                                    }
                                }
                            }
                        });
            } catch (Exception ex) {
                Log.e(TAG, "exception in CreateMediaController " + ex);
            }

        }, ContextCompat.getMainExecutor(this));
    }

    private void ReconnectMediaController(){
        Log.d(TAG, "reconnect Mediacontroller");

    }


    private void stopMusic() {
        Log.d(TAG, "stop music ");

        try {
            controllerFuture.get().stop();
            controllerFuture.get().clearMediaItems();

            if (progressTimer != null) {
                progressTimer.cancel();
                progressTimer.purge();
            }

            setPlayControllerHeight(10);

        } catch (Exception ex) {
            Log.e(TAG, "exception in stopMusic " + ex);
        }

    }

    private void startMusic() {
        Log.d(TAG, "start music ");

        try {
            controllerFuture.get().play();

        } catch (Exception ex) {
            Log.e(TAG, "exception in stopMusic " + ex);
        }

    }

    private void startFromPos(int pos){
        Log.d(TAG, "start from pos " + pos);

        try {
            controllerFuture.get().seekTo(pos,0);

        } catch (Exception ex) {
            Log.e(TAG, "exception in stopMusic " + ex);
        }
    }


    @OptIn(markerClass = UnstableApi.class) private void setPlayControllerHeight(int height) {
        if (playerView!=null) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) playerView.getLayoutParams();
            params.width = 0;

            float scale = getResources().getDisplayMetrics().density;

            params.height =  (int) (height * scale + 0.5f);
            playerView.setLayoutParams(params);

            if (height>100){
                playerView.showController();
                playerView.setVisibility(View.VISIBLE);
            }
            else {
                playerView.hideController();
                playerView.setVisibility(View.INVISIBLE);
            }
        }
    }


}
