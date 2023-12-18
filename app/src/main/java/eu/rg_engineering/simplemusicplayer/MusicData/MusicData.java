package eu.rg_engineering.simplemusicplayer.MusicData;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.rg_engineering.simplemusicplayer.AlbumItem;
import eu.rg_engineering.simplemusicplayer.ArtistItem;
import eu.rg_engineering.simplemusicplayer.PlexServer.Plex_FindData;
import eu.rg_engineering.simplemusicplayer.TrackItem;

public class MusicData  {

    private String TAG = "MusicData";

    private Activity mActivity;

    private Plex_FindData mPlex_FindData=null;

    private String mArtist4Album="";
    private String mAlbum4Track="";

    public MusicData(Activity activity) {
        mActivity = activity;
    }

    public ArrayList<ArtistItem> getArtistData() {

        ArrayList<ArtistItem> artists;

        artists = getServerArtistData();
        if (artists.size() == 0) {
            artists = getLocalMusicData();
        }
        if (artists.size() == 0) {
            artists = getDummyArtistData(7);
        }
        return artists;
    }

    private ArrayList<ArtistItem> getDummyArtistData(int count) {
        ArrayList<ArtistItem> artists = ArtistItem.createItemsList(count);

        return artists;
    }

    private ArrayList<ArtistItem> getServerArtistData() {

        ArrayList<ArtistItem> artists = getDummyArtistData(0);

        if (mPlex_FindData != null && mPlex_FindData.isAlive()) {
            mPlex_FindData.interrupt();
        }

        //if (mPlex_FindData==null) {
        mPlex_FindData = new Plex_FindData(mActivity);
        //}
        mPlex_FindData.setMode("Artists");

        mPlex_FindData.start();

        return artists;
    }

    public void ReadPlexArtistData() {
        Log.d(TAG, "reading artist data from plex ");

        if (mPlex_FindData !=null && mPlex_FindData.mArtists.size()>0){

            for (int i = 0; i< mPlex_FindData.mArtists.size(); i++) {

                String name = mPlex_FindData.mArtists.get(i).title;
                String genre = mPlex_FindData.mArtists.get(i).Genre;
                String country = mPlex_FindData.mArtists.get(i).Country;
                String path2image = mPlex_FindData.mArtists.get(i).thumb;
                String summery = mPlex_FindData.mArtists.get(i).summary;
                int ratingKey = Integer.parseInt(mPlex_FindData.mArtists.get(i).ratingKey);

                ArtistItem artist = new ArtistItem(name, genre, country, path2image, summery,ratingKey);
                mLocalArtists.add(artist);
            }
        }
        mPlex_FindData.interrupt();
    }

    public void ReadPlexAlbumData() {
        Log.d(TAG, "reading album data from plex ");

        if (mPlex_FindData !=null && mPlex_FindData.mAlbums.size()>0) {

            for (int i = 0; i < mPlex_FindData.mAlbums.size(); i++) {

                String name = mPlex_FindData.mAlbums.get(i).title;
                String artist = mPlex_FindData.mAlbums.get(i).parentTitle;
                int year = Integer.parseInt(mPlex_FindData.mAlbums.get(i).year);
                String path2image = mPlex_FindData.mAlbums.get(i).thumb;
                String summery = mPlex_FindData.mAlbums.get(i).summary;
                int ratingKey = Integer.parseInt(mPlex_FindData.mAlbums.get(i).ratingKey);

                AlbumItem album = new AlbumItem(name, artist, year, path2image, summery, ratingKey);
                mLocalAlbums.add(album);
            }
        }
        mPlex_FindData.interrupt();
    }

    public void ReadPlexTrackData() {
        Log.d(TAG, "reading track data from plex ");

        if (mPlex_FindData !=null && mPlex_FindData.mTracks.size()>0){

            for (int i = 0; i< mPlex_FindData.mTracks.size(); i++) {

                String name = mPlex_FindData.mTracks.get(i).title;
                String album = mPlex_FindData.mTracks.get(i).parentTitle;
                String artist = mPlex_FindData.mTracks.get(i).grandparentTitle;

                /*
                http://192.168.3.21:32400/library/parts/48571/1261258691/file.mp3?X-Plex-Token=LAtVbxshNWzuGUwtm8bJ"
                */
                //todo: erst mal nur ersten Teil, später alle Teile
                String filename= "http://192.168.3.21:32400"+mPlex_FindData.mTracks.get(i).parts.get(0).key + "?X-Plex-Token=LAtVbxshNWzuGUwtm8bJ";

                int duration = Integer.parseInt(mPlex_FindData.mTracks.get(i).duration);

                TrackItem track = new TrackItem(name,album,artist,filename,duration);
                mLocalTracks.add(track);
            }
        }
        mPlex_FindData.interrupt();
    }

    private ArrayList<AlbumItem> getServerAlbumData() {

        ArrayList<AlbumItem> albums = getDummyAlbumData(0);

        if (mPlex_FindData != null && mPlex_FindData.isAlive()) {
            mPlex_FindData.interrupt();
        }

        //if (mPlex_FindData ==null) {
        mPlex_FindData = new Plex_FindData(mActivity);
        //}
        mPlex_FindData.setMode("Albums");
        mPlex_FindData.setArtist4AlbumFilter(mArtist4Album);


        mPlex_FindData.start();

        return albums;
    }

    private ArrayList<TrackItem> getServerTrackData() {

        ArrayList<TrackItem> artists = getDummyTrackData(0);

        if (mPlex_FindData != null && mPlex_FindData.isAlive()) {
            mPlex_FindData.interrupt();
        }

        //if (mPlex_FindData==null) {
        mPlex_FindData = new Plex_FindData(mActivity);
        //}
        mPlex_FindData.setMode("Tracks");
        mPlex_FindData.setAlbum4TrackFilter(mAlbum4Track);

        mPlex_FindData.start();

        return artists;
    }

    //======================================================================================================================

    public void SetArtist4Album(String artist){
        mArtist4Album= artist;
    }

    public void SetAlbum4Track(String album){
        mAlbum4Track= album;
    }


    public ArrayList<AlbumItem> getAlbumData() {

        ArrayList<AlbumItem> albums = null;
        if (mArtist4Album.length()>0) {
            albums = getServerAlbumData();
        }
        if (albums == null || albums.size() == 0) {
            albums = getLocalAlbumData();
        }
        if (albums.size() == 0) {
            albums = getDummyAlbumData(7);
        }

        return albums;
    }

    private ArrayList<AlbumItem> getDummyAlbumData(int count) {
        ArrayList<AlbumItem> albums = AlbumItem.createItemsList(count);

        return albums;
    }

    //======================================================================================================================

    public ArrayList<TrackItem> getTrackData() {

        ArrayList<TrackItem> tracks=null;
        if (mAlbum4Track.length()>0) {
            tracks = getServerTrackData();
        }
        if (tracks==null || tracks.size() == 0) {
            tracks = getLocalTrackData();
        }
        if (tracks.size() == 0) {
            tracks = getDummyTrackData(7);
        }

        return tracks;
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

        return mLocalAlbums;
    }

    private ArrayList<TrackItem> getLocalTrackData() {

        return mLocalTracks;
    }

    public final Uri AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    ArrayList<ArtistItem> mLocalArtists;
    ArrayList<AlbumItem> mLocalAlbums;
    ArrayList<TrackItem> mLocalTracks;

    public void getAllLocalMusic(ContentResolver cr) {

        mLocalArtists = getDummyArtistData(0);
        mLocalAlbums = getDummyAlbumData(0);
        mLocalTracks = getDummyTrackData(0);

        Log.d(TAG, "check for audio files ");
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID,

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
                int nDuration = Integer.parseInt(cur.getString(4));

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

                //MusicItem song = new MusicItem(Title, Album, Artist, fileName, nDuration);


                TrackItem track = new TrackItem(Title,Album,Artist,fileName,nDuration);
                mLocalTracks.add(track);


                if (!ArtistExists(Artist)) {
                    ArtistItem artist = new ArtistItem(Artist, "", "", "", "",-1);
                    mLocalArtists.add(artist);
                }

                if (!AlbumExists(Album)) {
                    AlbumItem album = new AlbumItem(Album,Artist,0,"","", -1);
                    mLocalAlbums.add(album);
                }

            }
            cur.close();
        }
    }
    private boolean ArtistExists(String ArtistName) {

        boolean ret = false;
        for (int i = 0; i < mLocalArtists.size(); i++) {
            String name= mLocalArtists.get(i).getName();
            if ( name.equals(ArtistName)) {
                ret = true;
            }
        }
        return ret;
    }

    private boolean AlbumExists(String AlbumName) {

        boolean ret = false;
        for (int i = 0; i < mLocalAlbums.size(); i++) {
            String name= mLocalAlbums.get(i).getName();
            if ( name.equals(AlbumName)) {
                ret = true;
            }
        }
        return ret;
    }


}
