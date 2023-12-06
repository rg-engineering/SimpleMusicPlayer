package eu.rg_engineering.simplemusicplayer.PlexServer;

import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class Plex_FindArtists {

    private String TAG = "plex_findArtists";
    private String IP = "192.168.3.21";
    private String Port = "32400";
    private String Token = "LAtVbxshNWzuGUwtm8bJ";
    //private String Token = "eVp_3aBs33Jmdst9ifxmU";
    private String LibID = "2";

    private static final String ns = null;

    public void start() {
        Log.d(TAG, "start find artists ");

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // Your code goes here
                    startFindArtists();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    private void startFindArtists() {
        try {
            URL url = new URL("http://" + IP + ":" + Port + "/library/sections/" + LibID + "/all?X-Plex-Token=" + Token);
            downloadXml(url);
        } catch (MalformedURLException ex) {
            Log.e(TAG, "wrong url: " + ex.toString());
        }
    }

    //taken from
    //https://developer.android.com/develop/connectivity/network-ops/xml
    //https://support.plex.tv/articles/201638786-plex-media-server-url-commands/

    //how to get plex token
    //https://support.plex.tv/articles/204059436-finding-an-authentication-token-x-plex-token/
    private void downloadXml(URL url) {

        String result="";
        try {
            result = loadXmlFromNetwork(url);
        } catch (IOException e) {

        } catch (XmlPullParserException e) {

        }
        String finalResult = result;
    }

    private String loadXmlFromNetwork(URL url) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiates the parser.
        Plex_ArtistsXmlParser artistsXmlParser = new Plex_ArtistsXmlParser();
        List<Plex_Artists> artists = null;

        StringBuilder htmlString = new StringBuilder();
        try {
            stream = downloadUrl(url);
            artists = artistsXmlParser.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        //todo add arist data to string or json
        return htmlString.toString();
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




    /*

    private void startFindArtists() {
        HttpURLConnection urlConnection = null;
        InputStream data = null;

        try {
            URL url = new URL("http://" + IP + ":" + Port + "/library/sections/" + LibID + "/all?X-Plex-Token=" + Token);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            //urlConnection.setRequestProperty("User-Agent", USER_AGENT);

            data = new BufferedInputStream(urlConnection.getInputStream());
            readArtists(data);

        } catch (MalformedURLException ex) {
            Log.e(TAG, "wrong url: " + ex.toString());
        } catch (IOException ex) {
            Log.e(TAG, "io exception: " + ex.toString());
        } catch (Exception ex) {
            Log.e(TAG, " exception: " + ex.toString());
        } finally {
            urlConnection.disconnect();
        }
    }


    private void readArtists(InputStream data) {
        Log.d(TAG, "start reading artists ");
        Document dc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            dc = builder.parse(data);



            Log.d(TAG, "artists data parsed ");

        } catch (ParserConfigurationException ex) {
            Log.e(TAG, "ParserConfigurationException: " + ex.toString());
        } catch (IOException ex) {
            Log.e(TAG, "IOException: " + ex.toString());
        } catch (SAXException ex) {
            Log.e(TAG, "SAXException: " + ex.toString());
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }


    }

*/


