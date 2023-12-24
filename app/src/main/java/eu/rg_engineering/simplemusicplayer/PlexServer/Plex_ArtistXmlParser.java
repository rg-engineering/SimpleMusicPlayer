package eu.rg_engineering.simplemusicplayer.PlexServer;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class Plex_ArtistXmlParser {
    private final String TAG = "Plex_ArtistsXmlParser";
    private static final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }
    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List artists = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "MediaContainer");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag.
            if (name.equals("Directory")) {
                artists.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return artists;
    }
    private Plex_Artist readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Directory");
        String ratingKey="";
        String key="";
        String guid="";
        String type="";
        String title="";
        String summary="";
        String index="";
        String thumb="";
        String art="";
        String addedAt="";
        String updatedAt="";
        String  Genre="";
        String Country="";

        ratingKey= parser.getAttributeValue(null, "ratingKey");
        key= parser.getAttributeValue(null, "key");
        guid= parser.getAttributeValue(null, "guid");
        type= parser.getAttributeValue(null, "type");
        title= parser.getAttributeValue(null, "title");
        summary= parser.getAttributeValue(null, "summary");
        index= parser.getAttributeValue(null, "index");
        thumb= parser.getAttributeValue(null, "thumb");
        art= parser.getAttributeValue(null, "art");
        addedAt= parser.getAttributeValue(null, "addedAt");
        updatedAt= parser.getAttributeValue(null, "updatedAt");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Genre")) {
                Genre= readGenre(parser);
            }
            else if (name.equals("Country")) {
                Country= readCountry(parser);
            }

            else {
                skip(parser);
            }
        }

        Log.d(TAG, "new artist from plex: " + title);

        return new Plex_Artist(ratingKey,key,guid,type,title,summary,index,thumb,art,addedAt,updatedAt,Genre, Country);
    }

    // Processes title tags in the feed.
    private String readGenre(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Genre");
        String genre= parser.getAttributeValue(null, "tag");

        while (parser.next() != XmlPullParser.END_TAG) {

        }
        return genre;
    }

    private String readCountry(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Country");
        String country= parser.getAttributeValue(null, "tag");
        while (parser.next() != XmlPullParser.END_TAG) {

        }
        return country;
    }


    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
