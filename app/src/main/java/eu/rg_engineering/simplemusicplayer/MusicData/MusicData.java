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

import eu.rg_engineering.simplemusicplayer.ArtistItem;
import eu.rg_engineering.simplemusicplayer.MusicItem;
import eu.rg_engineering.simplemusicplayer.PlexServer.Plex_FindArtists;

public class MusicData  {

    private String TAG = "MusicData";

    private Activity mActivity;

    private Plex_FindArtists mPlex_FindArtists;

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

        mPlex_FindArtists = new Plex_FindArtists(mActivity);
        mPlex_FindArtists.start();

        return artists;
    }

    public void ReadPlexArtistData() {
        Log.d(TAG, "reading artist data from plex ");

        if (mPlex_FindArtists!=null && mPlex_FindArtists.mArtists.size()>0){

            for (int i=0; i<mPlex_FindArtists.mArtists.size();i++) {

                String name = mPlex_FindArtists.mArtists.get(i).title;
                String genre = mPlex_FindArtists.mArtists.get(i).Genre;
                String country = mPlex_FindArtists.mArtists.get(i).Country;
                String path2image = mPlex_FindArtists.mArtists.get(i).thumb;
                String summery = mPlex_FindArtists.mArtists.get(i).summary;

                ArtistItem artist = new ArtistItem(name, genre, country, path2image, summery);
                mLocalArtists.add(artist);
            }
        }

    }

    private ArrayList<ArtistItem> getLocalMusicData() {


        ContentResolver cr = mActivity.getContentResolver();
        getAllLocalMusic(cr);

        return mLocalArtists;
    }

    public final Uri AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    ArrayList<ArtistItem> mLocalArtists;

    public void getAllLocalMusic(ContentResolver cr) {

        mLocalArtists = getDummyArtistData(0);

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

                MusicItem song = new MusicItem(Title, Album, Artist, fileName, nDuration);

                if (!ArtistExists(Artist)) {
                    ArtistItem artist = new ArtistItem(Artist, "", "", "", "");
                    mLocalArtists.add(artist);
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


}
