package eu.rg_engineering.simplemusicplayer.MusicData;

import android.content.Intent;
import android.os.IBinder;
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

//todo implement MediaSession.Callback.onPlaybackResumption() https://developer.android.com/media/media3/session/background-playback
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
        trackSelectionParameters = new TrackSelectionParameters.Builder(this).build();
        clearStartPosition();
        initializePlayer();
    }


    @Nullable
    @Override
    public IBinder onBind(@Nullable Intent intent) {
        Log.d(TAG, "onBind");
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);

    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
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
            Log.i(TAG, "stop self");
        }
    }

    protected boolean initializePlayer() {
        if (exoPlayer == null) {

            mediaItems = createMediaItems();

            lastSeenTracks = Tracks.EMPTY;
            ExoPlayer.Builder playerBuilder = new ExoPlayer.Builder(this);
            //.setMediaSourceFactory(createMediaSourceFactory());
            //setRenderersFactory( playerBuilder, intent.getBooleanExtra(IntentUtil.PREFER_EXTENSION_DECODERS_EXTRA, false));
            exoPlayer = playerBuilder.build();
            exoPlayer.setTrackSelectionParameters(trackSelectionParameters);
            exoPlayer.addListener(new PlayerEventListener());
            exoPlayer.addAnalyticsListener(new EventLogger());
            exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true);
            exoPlayer.setPlayWhenReady(startAutoPlay);

            mediaSession = new MediaSession.Builder(this, exoPlayer).build();

            configurePlayerWithServerSideAdsLoader();
        }
        boolean haveStartPosition = startItemIndex != C.INDEX_UNSET;
        if (haveStartPosition) {
            exoPlayer.seekTo(startItemIndex, startPosition);
        }
        exoPlayer.setMediaItems(mediaItems, !haveStartPosition);
        exoPlayer.prepare();
        updateButtonVisibility();

        exoPlayer.addListener(
                new Player.Listener() {
                    @OptIn(markerClass = UnstableApi.class)
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
                        }
                    }
                });

        return true;
    }

    protected void releasePlayer() {

        Log.d(TAG, "release player");

        mediaSession.getPlayer().release();
        mediaSession.release();
        mediaSession = null;

        if (exoPlayer != null) {
            updateTrackSelectorParameters();
            updateStartPosition();
            releaseServerSideAdsLoader();

            exoPlayer.release();
            exoPlayer = null;

            mediaItems = Collections.emptyList();
        }
        if (clientSideAdsLoader != null) {
            clientSideAdsLoader.setPlayer(null);
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
        }
    }

    private List<MediaItem> createMediaItems() {

        ArrayList<MediaItem> mediaItems = new ArrayList<>();

        return mediaItems;
    }

    @Override
    public void onUpdateNotification(MediaSession session, boolean startInForegroundRequired) {
        super.onUpdateNotification(session, startInForegroundRequired);

        Log.d(TAG, "onUpdateNotification");
    }
}
