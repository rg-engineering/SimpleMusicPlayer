package eu.rg_engineering.simplemusicplayer.MusicData;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ads.AdsLoader;
import androidx.media3.exoplayer.util.EventLogger;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import eu.rg_engineering.simplemusicplayer.MainActivity;

public class MusicService extends MediaSessionService {

    private final String TAG = "MusicService";
    private ExoPlayer exoPlayer;
    private MediaSession mediaSession = null;

    private List<MediaItem> mediaItems;

    private Tracks lastSeenTracks;
    private TrackSelectionParameters trackSelectionParameters;
    private boolean startAutoPlay;
    private int startItemIndex;
    private long startPosition;
    @Nullable
    private AdsLoader clientSideAdsLoader;

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //todo saved instance
        //if (savedInstanceState != null) {
        //    trackSelectionParameters =
        //            TrackSelectionParameters.fromBundle(
        //                    savedInstanceState.getBundle(KEY_TRACK_SELECTION_PARAMETERS));
        //    startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
        //    startItemIndex = savedInstanceState.getInt(KEY_ITEM_INDEX);
        //    startPosition = savedInstanceState.getLong(KEY_POSITION);
        //    restoreServerSideAdsLoaderState(savedInstanceState);
        //} else {
        trackSelectionParameters = new TrackSelectionParameters.Builder( this).build();
        clearStartPosition();
        //}


        initializePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Player player = mediaSession.getPlayer();
        if (!player.getPlayWhenReady() || player.getMediaItemCount() == 0) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf();
        }
    }




    protected boolean initializePlayer() {
        if (exoPlayer == null) {

            mediaItems = createMediaItems();
            /*
            if (mediaItems==null) {
                mediaItems = createMediaItems();
            }
            if (mediaItems.isEmpty()) {
                Log.e(TAG, "no media items");
                return false;
            }
             */
            lastSeenTracks = Tracks.EMPTY;
            ExoPlayer.Builder playerBuilder = new ExoPlayer.Builder( this);
            //.setMediaSourceFactory(createMediaSourceFactory());
            //setRenderersFactory( playerBuilder, intent.getBooleanExtra(IntentUtil.PREFER_EXTENSION_DECODERS_EXTRA, false));
            exoPlayer = playerBuilder.build();
            exoPlayer.setTrackSelectionParameters(trackSelectionParameters);
            exoPlayer.addListener(new PlayerEventListener());
            exoPlayer.addAnalyticsListener(new EventLogger());
            exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT,  true);
            exoPlayer.setPlayWhenReady(startAutoPlay);

            mediaSession = new MediaSession.Builder(this, exoPlayer).build();



            //playerView.setPlayer(exoPlayer);
            configurePlayerWithServerSideAdsLoader();
            //debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            //debugViewHelper.start();
        }
        boolean haveStartPosition = startItemIndex != C.INDEX_UNSET;
        if (haveStartPosition) {
            exoPlayer.seekTo(startItemIndex, startPosition);
        }
        exoPlayer.setMediaItems(mediaItems,  !haveStartPosition);
        exoPlayer.prepare();
        updateButtonVisibility();


        exoPlayer.addListener(
                new Player.Listener() {
                    @OptIn(markerClass = UnstableApi.class) @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        if (isPlaying) {
                            // Active playback.
                            Log.i(TAG, "is playing");
                            //playerView.showController();
                            //playerView.setVisibility(View.VISIBLE);

                        } else {
                            // Not playing because playback is paused, ended, suppressed, or the player
                            // is buffering, stopped or failed. Check player.getPlayWhenReady,
                            // player.getPlaybackState, player.getPlaybackSuppressionReason and
                            // player.getPlaybackError for details.
                            Log.i(TAG, "is not playing " + exoPlayer.getPlaybackState());
                        }
                    }
                });


        /*
        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer.purge();
        }

        progressTimer = new Timer();
        TimerTask updateProgress = new MainActivity.UpdateProgressTask();
        progressTimer.scheduleAtFixedRate(updateProgress, 0, 1000);
*/


        return true;
    }

    protected void releasePlayer() {

        mediaSession.getPlayer().release();
        mediaSession.release();
        mediaSession = null;


        if (exoPlayer != null) {
            updateTrackSelectorParameters();
            updateStartPosition();
            releaseServerSideAdsLoader();
            //debugViewHelper.stop();
            //debugViewHelper = null;
            exoPlayer.release();
            exoPlayer = null;
            //playerView.setPlayer(/* player= */ null);
            mediaItems = Collections.emptyList();
        }
        if (clientSideAdsLoader != null) {
            clientSideAdsLoader.setPlayer(null);
        } else {
            //playerView.getAdViewGroup().removeAllViews();
        }
    }

    private class PlayerEventListener implements Player.Listener {

        @Override
        public void onPlaybackStateChanged(@Player.State int playbackState) {
            if (playbackState == Player.STATE_ENDED) {
                showControls();
            }
            updateButtonVisibility();
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                exoPlayer.seekToDefaultPosition();
                exoPlayer.prepare();
            } else {
                updateButtonVisibility();
                showControls();
            }
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(Tracks tracks) {
            updateButtonVisibility();
            if (tracks == lastSeenTracks) {
                return;
            }
            if (tracks.containsType(C.TRACK_TYPE_VIDEO)
                    && !tracks.isTypeSupported(C.TRACK_TYPE_VIDEO, /* allowExceedsCapabilities= */ true)) {

                Toast.makeText(getApplicationContext(), "error_unsupported_video", Toast.LENGTH_LONG).show();
            }
            if (tracks.containsType(C.TRACK_TYPE_AUDIO)
                    && !tracks.isTypeSupported(C.TRACK_TYPE_AUDIO, /* allowExceedsCapabilities= */ true)) {
                Toast.makeText(getApplicationContext(), "error_unsupported_audio", Toast.LENGTH_LONG).show();
            }
            lastSeenTracks = tracks;
        }
    }

    private void configurePlayerWithServerSideAdsLoader() {
        //serverSideAdsLoader.setPlayer(player);
    }

    private void updateTrackSelectorParameters() {
        if (exoPlayer != null) {
            trackSelectionParameters = exoPlayer.getTrackSelectionParameters();
        }
    }

    private void updateStartPosition() {
        if (exoPlayer != null) {
            startAutoPlay = exoPlayer.getPlayWhenReady();
            startItemIndex = exoPlayer.getCurrentMediaItemIndex();
            startPosition = Math.max(0, exoPlayer.getContentPosition());
        }
    }

    protected void clearStartPosition() {
        startAutoPlay = true;
        startItemIndex = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }

    private void updateButtonVisibility() {
        //selectTracksButton.setEnabled(exoPlayer != null && TrackSelectionDialog.willHaveContent(exoPlayer));
    }

    private void showControls() {
        //debugRootView.setVisibility(View.VISIBLE);
    }

    private void releaseServerSideAdsLoader() {
        //serverSideAdsLoaderState = serverSideAdsLoader.release();
        //serverSideAdsLoader = null;
    }

    private void releaseClientSideAdsLoader() {
        if (clientSideAdsLoader != null) {
            clientSideAdsLoader.release();
            clientSideAdsLoader = null;
            //playerView.getAdViewGroup().removeAllViews();
        }
    }

    private List<MediaItem> createMediaItems() {

        //todo
        //mediaItems = mMusicData.createMediaItems();



        ArrayList<MediaItem> mediaItems = new ArrayList<>();

        /* do not create items here
        for (int i = 1; i <= 10; i++) {

            //see https://developer.android.com/media/media3/exoplayer/media-items
            MediaItem item = MediaItem.fromUri("http://192.168.3.21:32400/library/parts/48571/1261258691/file.mp3?X-Plex-Token=LAtVbxshNWzuGUwtm8bJ");
            mediaItems.add(item);
        }
        */

        return mediaItems;
    }

}
