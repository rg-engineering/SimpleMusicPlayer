package eu.rg_engineering.simplemusicplayer.MusicData;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.media3.common.MediaItem;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.rg_engineering.simplemusicplayer.AlbumItem;
import eu.rg_engineering.simplemusicplayer.ArtistItem;
import eu.rg_engineering.simplemusicplayer.PlexServer.Plex_Album;
import eu.rg_engineering.simplemusicplayer.PlexServer.Plex_Artist;
import eu.rg_engineering.simplemusicplayer.PlexServer.Plex_FindData;
import eu.rg_engineering.simplemusicplayer.PlexServer.Plex_Track;
import eu.rg_engineering.simplemusicplayer.TrackItem;
import io.sentry.Sentry;

public class MusicData  {

    private final String TAG = "MusicData";
    private final Activity mActivity;
    private Plex_FindData mPlex_FindData=null;
    private String mArtist4Album="";
    private String mArtistPath2Image="";
    private String mArtistName4Album="";
    private String mAlbumName4Track="";
    private String mAlbumPath2Image="";
    private Boolean mSearchAlbumOnServer = false;
    private String mAlbum4Track="";
    private Boolean mSearchTrackOnServer = false;
    private String IP = "";
    private String Port = "";
    private String Token = "";
    ArrayList<ArtistItem> mLocalArtists=null;
    ArrayList<AlbumItem> mLocalAlbums=null;
    ArrayList<AlbumItem> mLocalAlbumsAll=null;
    ArrayList<TrackItem> mLocalTracks=null;
    ArrayList<TrackItem> mLocalTracksAll=null;

    public MusicData(Activity activity) {

        mActivity = activity;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        IP = sharedPreferences.getString("plex_server_ip", "");
        Port = sharedPreferences.getString("plex_server_port", "");
        Token = sharedPreferences.getString("plex_server_token", "");
    }

    public List<MediaItem> createMediaItems(){

        ArrayList<MediaItem> items = new ArrayList<>();

        /*
        for (int i = 1; i <= 10; i++) {

            //see https://developer.android.com/media/media3/exoplayer/media-items
            MediaItem item = MediaItem.fromUri("http://192.168.3.21:32400/library/parts/48571/1261258691/file.mp3?X-Plex-Token=LAtVbxshNWzuGUwtm8bJ");
            items.add(item);
        }
        */

        return items;
    }



    public ArrayList<ArtistItem> getArtistData() {

        //prepare all lists
        if (mLocalArtists==null) {
            mLocalArtists = getDummyArtistData(0);
        }
        else {
            mLocalArtists.clear();
        }
        if (mLocalAlbums==null) {
            mLocalAlbums = getDummyAlbumData(0);
        }
        else {
            mLocalAlbums.clear();
        }
        if (mLocalAlbumsAll==null) {
            mLocalAlbumsAll = getDummyAlbumData(0);
        }
        else {
            mLocalAlbumsAll.clear();
        }
        if (mLocalTracks==null) {
            mLocalTracks = getDummyTrackData(0);
        }
        else {
            mLocalTracks.clear();
        }
        if (mLocalTracksAll==null) {
            mLocalTracksAll = getDummyTrackData(0);
        }
        else {
            mLocalTracksAll.clear();
        }

        //now get artists
        getServerArtistData();
        if (mLocalArtists.size() == 0) {
            mLocalArtists = getLocalMusicData();
        }
        //if (artists.size() == 0) {
        //    artists = getDummyArtistData(7);
        //}
        return mLocalArtists;
    }

    private ArrayList<ArtistItem> getDummyArtistData(int count) {
        ArrayList<ArtistItem> artists = ArtistItem.createItemsList(count);

        return artists;
    }

    private void getServerArtistData() {
        if (mPlex_FindData != null && mPlex_FindData.isAlive()) {
            mPlex_FindData.interrupt();
        }

        mPlex_FindData = new Plex_FindData(mActivity);
        mPlex_FindData.setMode("Artists");
        mPlex_FindData.start();
    }

    public void ReadPlexArtistData() {
        Log.d(TAG, "reading artist data from plex ");

        if (mPlex_FindData != null && mPlex_FindData.mArtists.size() > 0) {

            for (Plex_Artist item : mPlex_FindData.mArtists) {

                String name = item.title;
                String genre = item.Genre;
                String country = item.Country;
                String path2image = (item.thumb != null && item.thumb.length() > 0) ? "http://" + IP + ":" + Port + item.thumb + "?X-Plex-Token=" + Token : "";

                String summery = item.summary;
                int ratingKey = -1;
                try {
                    ratingKey = Integer.parseInt(item.ratingKey);
                } catch (NumberFormatException ex) {
                    Log.e(TAG, "ratingkey is not a number");
                    Sentry.captureException(ex);
                }
                ArtistItem artist = new ArtistItem(name, genre, country, path2image, summery, ratingKey);
                mLocalArtists.add(artist);
            }
        }
        mPlex_FindData.interrupt();
    }

    public void ReadPlexAlbumData() {
        Log.d(TAG, "reading album data from plex ");

        if (mPlex_FindData !=null && mPlex_FindData.mAlbums.size()>0) {

            if (mLocalAlbums.size() > 0) {
                Log.d(TAG, "clear album list");
                mLocalAlbums.clear();
            }

            for (Plex_Album item : mPlex_FindData.mAlbums) {

                String name = item.title;
                String artist = item.parentTitle;
                int year = -1;
                try {
                    year = Integer.parseInt(item.year);
                } catch (NumberFormatException ex) {
                    Log.e(TAG, "year is not a number");
                    Sentry.captureException(ex);
                }
                String path2image = (item.thumb != null && item.thumb.length() > 0) ? "http://" + IP + ":" + Port + item.thumb + "?X-Plex-Token=" + Token : "";

                String summery = item.summary;
                int ratingKey = -1;
                try {
                    ratingKey = Integer.parseInt(item.ratingKey);
                } catch (NumberFormatException ex) {
                    Log.e(TAG, "ratingkey is not a number");
                    Sentry.captureException(ex);
                }
                AlbumItem album = new AlbumItem(name, artist, year, path2image, summery, ratingKey);

                mLocalAlbums.add(album);
            }
        }
        mPlex_FindData.interrupt();
    }

    public void ReadPlexTrackData() {
        Log.d(TAG, "reading track data from plex");

        if (mPlex_FindData != null && mPlex_FindData.mTracks.size() > 0) {

            if (mLocalTracks.size() > 0) {
                Log.d(TAG, "clear track list");
                mLocalTracks.clear();
            }


            for (Plex_Track item : mPlex_FindData.mTracks) {

                String name = item.title;
                String album = item.parentTitle;
                String artist = item.grandparentTitle;

                String path2AlbumImage = (item.parentThumb != null && item.parentThumb.length() > 0) ? "http://" + IP + ":" + Port + item.parentThumb + "?X-Plex-Token=" + Token : "";
                String path2ArtistImage = (item.grandparentThumb != null && item.grandparentThumb.length() > 0) ? "http://" + IP + ":" + Port + item.grandparentThumb + "?X-Plex-Token=" + Token : "";

                /*
                http://192.168.3.21:32400/library/parts/48571/1261258691/file.mp3?X-Plex-Token=LAtVbxshNWzuGUwtm8bJ"
                */
                //todo: erst mal nur ersten Teil des tracks, später alle Teile, wenn es mehrere gibt
                String filename = "http://" + IP + ":" + Port + item.parts.get(0).key + "?X-Plex-Token=" + Token;
                int duration = 0;
                try {
                    duration = Integer.parseInt(item.duration);
                } catch (NumberFormatException ex) {
                    Log.e(TAG, "duration is not a number");
                    Sentry.captureException(ex);
                }

                int albumIndex=-1;
                try {
                    albumIndex = Integer.parseInt(item.index);
                } catch (NumberFormatException ex) {
                    Log.e(TAG, "album index is not a number");
                    Sentry.captureException(ex);
                }

                TrackItem track = new TrackItem(name, album, artist, filename, duration, path2AlbumImage, path2ArtistImage, albumIndex);
                mLocalTracks.add(track);
            }
        }
        mPlex_FindData.interrupt();
    }

    private void getServerAlbumData() {

        if (mPlex_FindData != null && mPlex_FindData.isAlive()) {
            mPlex_FindData.interrupt();
        }

        mPlex_FindData = new Plex_FindData(mActivity);
        mPlex_FindData.setMode("Albums");
        mPlex_FindData.setArtist4AlbumFilter(mArtist4Album);

        mPlex_FindData.start();
    }

    private void getServerTrackData() {

        if (mPlex_FindData != null && mPlex_FindData.isAlive()) {
            mPlex_FindData.interrupt();
        }

        mPlex_FindData = new Plex_FindData(mActivity);
        mPlex_FindData.setMode("Tracks");
        mPlex_FindData.setAlbum4TrackFilter(mAlbum4Track);

        mPlex_FindData.start();
    }

    //======================================================================================================================

    public void SetArtist4Album( ArrayList<String> data){

        //if RatingKey==-1, then local
        if (data.get(2).contains("-1")){
            mArtist4Album = data.get(0);
            mArtistName4Album = data.get(0); //clear text name
            mArtistPath2Image=data.get(1);
            mSearchAlbumOnServer=false;
        }
        else {
            //if on Plex then we need RatingKey for next search
            mArtist4Album = data.get(2);
            mArtistName4Album = data.get(0); //clear text name
            mArtistPath2Image=data.get(1);
            mSearchAlbumOnServer=true;
        }
    }

    public String GetArtist4Album(){
        return mArtistName4Album;
    }
    public String GetPath2Image4Album(){
        return mArtistPath2Image;
    }

    public void SetAlbum4Track(String album, ArrayList<String> data) {
        if (data.get(2).contains("-1")){
            mAlbum4Track = data.get(0);
            mAlbumName4Track = data.get(0); //clear text name
            mAlbumPath2Image=data.get(1);
            mSearchTrackOnServer=false;
        }
        else {
            mAlbum4Track = data.get(2);
            mAlbumName4Track = data.get(0); //clear text name
            mAlbumPath2Image=data.get(1);
            mSearchTrackOnServer=true;
        }
    }
    public String GetAlbum4Track(){
        return mAlbumName4Track;
    }

    public String GetPath2Image4Track(){
        return mAlbumPath2Image;
    }
    public ArrayList<AlbumItem> getAlbumData() {

        if (mArtist4Album.length()>0 && mSearchAlbumOnServer) {
            getServerAlbumData();
        }
        if (mArtist4Album.length()>0 && !mSearchAlbumOnServer) {
            mLocalAlbums = getLocalAlbumData();
        }
        //if (albums.size() == 0) {
        //    albums = getDummyAlbumData(0);
        //}

        return mLocalAlbums;
    }

    private ArrayList<AlbumItem> getDummyAlbumData(int count) {
        ArrayList<AlbumItem> albums = AlbumItem.createItemsList(count);

        return albums;
    }

    //======================================================================================================================

    public ArrayList<TrackItem> getTrackData() {

        if (mAlbum4Track.length() > 0 && mSearchTrackOnServer) {
            getServerTrackData();
        }
        if (mAlbum4Track.length() > 0 && !mSearchTrackOnServer) {
            mLocalTracks = getLocalTrackData();
        }
        //if (tracks.size() == 0) {
        //    tracks = getDummyTrackData(7);
        //}

        return mLocalTracks;
    }

    private ArrayList<TrackItem> getDummyTrackData(int count) {
        ArrayList<TrackItem> tracks = TrackItem.createItemsList(count);

        return tracks;
    }

    //=======================================================================================================================
    private ArrayList<ArtistItem> getLocalMusicData() {

        ContentResolver cr = mActivity.getContentResolver();
        getAllLocalMusic(cr);

        return mLocalArtists;
    }

    private ArrayList<AlbumItem> getLocalAlbumData() {
        if (mArtist4Album.length()>0){
            mLocalAlbums.clear();
            //only albums of selected artist
            for (AlbumItem item : mLocalAlbumsAll) {
                if (item.containArtist(mArtist4Album)){
                    mLocalAlbums.add(item);
                }
            }
        }
        return mLocalAlbums;
    }

    private ArrayList<TrackItem> getLocalTrackData() {
        if (mAlbum4Track.length()>0){
            mLocalTracks.clear();
            //only tracks of selected album
            for (TrackItem item : mLocalTracksAll){

                if (item.getAlbum().contains(mAlbum4Track)){
                    mLocalTracks.add(item);
                }
            }
        }
        return mLocalTracks;
    }

    public final Uri AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;



    //todo scan optimieren, dauert sehr lange..

    public void getAllLocalMusic(ContentResolver cr) {
        Log.d(TAG, "check for audio files ");
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.TRACK

                //MediaStore.Audio.Media.DATA
        };
        String sortOrder = MediaStore.Audio.Media._ID + " desc";
        Cursor cur = cr.query(AUDIO_URI, projection, null, null, sortOrder);
        List<String> pathNameList = new ArrayList<>();
        if (cur != null) {

            while (cur.moveToNext()) {
                String Album = cur.getString(0);
                String AlbumId = cur.getString(5);
                String Artist = cur.getString(1);
                String ArtistId = cur.getString(6);
                String Title = cur.getString(2);
                String fileName = cur.getString(3);
                int nDuration=0;
                try {
                    nDuration = Integer.parseInt(cur.getString(4));
                }
                catch (NumberFormatException ex) {
                    Log.e(TAG, "duration is not a number");
                    Sentry.captureException(ex);
                }
                int nAlbumIndex=-1;
                try {
                    nAlbumIndex = Integer.parseInt(cur.getString(7));
                }
                catch (NumberFormatException ex) {
                    Log.e(TAG, "album index is not a number");
                    Sentry.captureException(ex);
                }



                String sDuration = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(nDuration),
                        TimeUnit.MILLISECONDS.toMinutes(nDuration) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(nDuration)),
                        TimeUnit.MILLISECONDS.toSeconds(nDuration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(nDuration)));


                String Line = Artist + " - " + Title + " " + sDuration;

                Log.d(TAG, "Audio " + Line);

                //https://stackoverflow.com/questions/23043123/retrive-the-track-image-from-android-mediastore-audio-media-content-provider
                String[] projection2 = {
                        MediaStore.Audio.AlbumColumns.ALBUM_ART
                };
                String selection = MediaStore.Audio.Media._ID + " =?";
                String[] selectionArgs = {
                        String.valueOf(AlbumId)
                };
                Cursor artCursor = cr.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        projection2,
                        selection,
                        selectionArgs,
                        null);
                String albumArt;
                if (artCursor.moveToNext()) {
                    albumArt = "file://" + artCursor.getString(0);
                } else {
                    albumArt = null;
                }
                Log.d(TAG, "Image " + albumArt);
                artCursor.close();

                TrackItem track = new TrackItem(Title,Album,Artist,fileName,nDuration,"","", nAlbumIndex);
                mLocalTracksAll.add(track);

                if (!ArtistExists(Artist)) {
                    ArtistItem artist = new ArtistItem(Artist, "", "", "", "",-1);
                    mLocalArtists.add(artist);
                }

                if (!AlbumExists(Album)) {
                    AlbumItem album = new AlbumItem(Album,Artist,0,"","", -1);
                    mLocalAlbumsAll.add(album);
                }
                else {
                    //wenn schon da, dann evtl. artist hinzufügen siehe Bravo Hits
                    int idx = AlbumGetIndex(Album);
                    if (idx >= 0) {
                        mLocalAlbumsAll.get(idx).AddArtist(Artist);
                    }
                }


            }
            cur.close();
        }
    }
    private boolean ArtistExists(String ArtistName) {

        //boolean ret = mLocalArtists.contains(ArtistName);

        boolean ret = false;

        for (ArtistItem item : mLocalArtists) {

            String name= item.getName();
            if (name.equals(ArtistName)) {
                ret = true;
                break;
            }
        }


        return ret;
    }

    private boolean AlbumExists(String AlbumName) {

        //boolean ret = mLocalAlbumsAll.contains(AlbumName);

        boolean ret = false;

        for (AlbumItem item: mLocalAlbumsAll){

            String name= item.getName();
            if (name.equals(AlbumName)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private int AlbumGetIndex(String AlbumName){
        int ret = -1;

        for (int index = 0; index < mLocalAlbumsAll.size(); index++) {
            if (mLocalAlbumsAll.get(index).getName().contains(AlbumName)) {
                return index;
            }
        }

        return ret;
    }

}
