package eu.rg_engineering.simplemusicplayer.PlexServer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import io.sentry.Sentry;

//https://www.plexopedia.com/plex-media-server/api/
public class Plex_FindData extends Thread {

    private final String TAG = "plex_findArtists";
    private String IP = "";
    private String Port = "";
    private String Token = "";
    private String LibID = "";

    public List<Plex_Artist> mArtists = null;
    public List<Plex_Album> mAlbums = null;
    public List<Plex_Track> mTracks = null;
    public List<Plex_Lib> mLibs=null;

    private static final String ns = null;
    private String  mSearchMode="Artists";
    private String mArtist4Album="";
    private String mAlbum4Track="";


    PlexFindArtistListener mCommunication;
    Activity activity = null;

    //Interface for communication
    public interface PlexFindArtistListener {
        void messageFromPlexFindArtist(String msg);
    }
    public Plex_FindData(Activity activity) {

        mCommunication = (PlexFindArtistListener) activity;
        this.activity = activity;
    }

    public void setMode(String mode){
        mSearchMode = mode;
    }
    public void setArtist4AlbumFilter(String artist4album){
        mArtist4Album = artist4album;
    }
    public void setAlbum4TrackFilter(String album4track){
        mAlbum4Track = album4track;
    }

    public void run() {

        boolean ConfigOk = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        IP = sharedPreferences.getString("plex_server_ip", "");
        Port = sharedPreferences.getString("plex_server_port", "");
        Token = sharedPreferences.getString("plex_server_token", "");
        LibID = sharedPreferences.getString("plex_server_libid", "");

        if (IP.length()>0 && Port.length()>0 && Token.length()>0 && LibID.length()>0){
            ConfigOk=true;
        }
        else {
            Log.e(TAG, "bad config " + IP + " " + Port + " " + Token);
            mCommunication.messageFromPlexFindArtist("config not okay");
        }

        Boolean usePlexServer = sharedPreferences.getBoolean("usePlexServer", false);

        if (usePlexServer && ConfigOk) {
            //started in separate thread
            switch (mSearchMode) {
                case "Artists":
                    Log.d(TAG, "start find artists ");
                    startFindArtists();
                    break;
                case "Albums":
                    Log.d(TAG, "start find albums for " + mArtist4Album);
                    startFindAlbums();
                    break;
                case "Tracks":
                    Log.d(TAG, "start find tracks ");
                    startFindTracks();
                    break;
            }
        }
        else {
            Log.d(TAG, "plex server disabled ");
        }
    }

    public void startFindArtists() {
        try {
            boolean needLibId=false;
            int nLibId=-1;
            try {
                nLibId = Integer.parseInt(LibID);
            }
            catch (NumberFormatException ex) {
                //handle exception here
                needLibId=true;
            }
            if (needLibId || nLibId<0 ){
                Log.e(TAG, "need to parse lib id ");
                //get LibId first
                String oldSearchMode = mSearchMode;
                mSearchMode = "LibID";
                URL url = new URL("http://" + IP + ":" + Port + "/library/sections?X-Plex-Token=" + Token);
                downloadXml(url);
                mSearchMode=oldSearchMode;
            }

            URL url = new URL("http://" + IP + ":" + Port + "/library/sections/" + LibID + "/all?X-Plex-Token=" + Token);
            downloadXml(url);
        } catch (MalformedURLException ex) {
            Log.e(TAG, "wrong url: " + ex);
            Sentry.captureException(ex);
        }
    }

    public void startFindAlbums() {
        try {

            String artistID= mArtist4Album;

            URL url = new URL("http://" + IP + ":" + Port + "/library/sections/" + LibID + "/all?artist.id=" + artistID + "&type=9&X-Plex-Token=" + Token);
            downloadXml(url);
        } catch (MalformedURLException ex) {
            Log.e(TAG, "wrong url: " + ex);
            Sentry.captureException(ex);
        }
    }
    public void startFindTracks() {
        try {

            String albumID=mAlbum4Track;

            URL url = new URL("http://" + IP + ":" + Port + "/library/metadata/" + albumID + "/children?X-Plex-Token=" + Token);
            downloadXml(url);
        } catch (MalformedURLException ex) {
            Log.e(TAG, "wrong url: " + ex);
            Sentry.captureException(ex);
        }
    }

    //taken from
    //https://developer.android.com/develop/connectivity/network-ops/xml
    //https://support.plex.tv/articles/201638786-plex-media-server-url-commands/

    //how to get plex token
    //https://support.plex.tv/articles/204059436-finding-an-authentication-token-x-plex-token/
    private void downloadXml(URL url) {
        try {
            loadXmlFromNetwork(url);
        } catch (IOException e) {

        } catch (XmlPullParserException ex) {
            Log.e(TAG, "exception in downloadXml " + ex);
            Sentry.captureException(ex);
        }
    }

    private void loadXmlFromNetwork(URL url) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiates the parser.

        Plex_ArtistXmlParser artistsXmlParser=null;
        Plex_AlbumXmlParser albumsXmlParser=null;
        Plex_TrackXmlParser tracksXmlParser=null;
        Plex_LibXmlParser libXmlParser=null;

        switch (mSearchMode) {
            case "Artists":
                artistsXmlParser = new Plex_ArtistXmlParser();
                break;
            case "Albums":
                albumsXmlParser = new Plex_AlbumXmlParser();
                break;
            case "Tracks":
                tracksXmlParser = new Plex_TrackXmlParser();
                break;
            case "LibID":
                libXmlParser = new Plex_LibXmlParser();
                break;
            default:
                Log.e(TAG, "wrong Search Mode " + mSearchMode);
                break;
        }


        try {
            stream = downloadUrl(url);

            switch (mSearchMode) {
                case "Artists":
                    mArtists = artistsXmlParser.parse(stream);

                    Log.d(TAG, "parsed artists " + mArtists.size());

                    mCommunication.messageFromPlexFindArtist("got all artists ");
                    break;
                case "Albums":
                    mAlbums = albumsXmlParser.parse(stream);

                    Log.d(TAG, "parsed albums " + mAlbums.size());

                    mCommunication.messageFromPlexFindArtist("got all albums ");
                    break;
                case "Tracks":
                    mTracks = tracksXmlParser.parse(stream);

                    Log.d(TAG, "parsed tracks " + mTracks.size());

                    mCommunication.messageFromPlexFindArtist("got all tracks ");
                    break;
                case "LibID":
                    mLibs = libXmlParser.parse(stream);

                    for (int i = 0; i < mLibs.size(); i++) {

                        String scanner = mLibs.get(i).scanner;
                        if (scanner.contains("Plex Music")) {
                            LibID = mLibs.get(i).key;
                        }
                    }

                    Log.d(TAG, "parsed lib ID " + LibID);
                    break;
                default:
                    Log.e(TAG, "wrong Search Mode " + mSearchMode);
                    break;
            }

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private InputStream downloadUrl(URL url) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query.
        conn.connect();
        return conn.getInputStream();
    }
}



