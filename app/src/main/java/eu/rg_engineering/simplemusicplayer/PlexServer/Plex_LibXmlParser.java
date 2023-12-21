package eu.rg_engineering.simplemusicplayer.PlexServer;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class Plex_LibXmlParser {
    private String TAG = "Plex_LibXmlParser";
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
        List libs = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "MediaContainer");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag.
            if (name.equals("Directory")) {
                libs.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return libs;
    }

    private Plex_Lib readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Directory");
        String allowSync;
        String art;
        String composite;
        String filters;
        String refreshing;
        String thumb;
        String key;
        String type;
        String title;
        String agent;
        String scanner;
        String language;
        String uuid;
        String updatedAt;
        String createdAt;
        String scannedAt;
        String content;
        String directory;
        String contentChangedAt;
        String hidden;
        String location="";

        allowSync = parser.getAttributeValue(null, "allowSync");
        art = parser.getAttributeValue(null, "art");
        composite = parser.getAttributeValue(null, "composite");
        filters = parser.getAttributeValue(null, "filters");
        refreshing = parser.getAttributeValue(null, "refreshing");
        thumb = parser.getAttributeValue(null, "thumb");
        key = parser.getAttributeValue(null, "key");
        type = parser.getAttributeValue(null, "type");
        title = parser.getAttributeValue(null, "title");
        agent = parser.getAttributeValue(null, "agent");
        scanner = parser.getAttributeValue(null, "scanner");
        language = parser.getAttributeValue(null, "language");
        uuid = parser.getAttributeValue(null, "uuid");
        updatedAt = parser.getAttributeValue(null, "updatedAt");
        createdAt = parser.getAttributeValue(null, "createdAt");
        scannedAt = parser.getAttributeValue(null, "scannedAt");
        content = parser.getAttributeValue(null, "content");
        directory = parser.getAttributeValue(null, "directory");
        contentChangedAt = parser.getAttributeValue(null, "contentChangedAt");
        hidden = parser.getAttributeValue(null, "hidden");


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Location")) {
                location = readLocation(parser);

            } else {
                skip(parser);
            }
        }

        Log.d(TAG, "new lib from plex: " + title + " " + key );

        return new Plex_Lib(allowSync,
                 art,
                 composite,
                 filters,
                 refreshing,
                 thumb,
                 key,
                 type,
                 title,
                 agent,
                 scanner,
                 language,
                 uuid,
                 updatedAt,
                 createdAt,
                 scannedAt,
                 content,
                 directory,
                 contentChangedAt,
                 hidden,
                 location);
    }

    // Processes title tags in the feed.
    private String readLocation(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Location");
        String id = parser.getAttributeValue(null, "id");
        String path = parser.getAttributeValue(null, "path");

        while (parser.next() != XmlPullParser.END_TAG) {

        }
        return path;
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
