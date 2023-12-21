package eu.rg_engineering.simplemusicplayer.PlexServer;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class Plex_AlbumXmlParser {
    private String TAG = "Plex_AlbumXmlParser";
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
        List albums = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "MediaContainer");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag.
            if (name.equals("Directory")) {
                albums.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return albums;
    }

    private Plex_Album readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Directory");
        String ratingKey;
        String key;
        String parentRatingKey;
        String guid;
        String parentGuid;
        String studio;
        String type;
        String title;
        String parentKey;
        String parentTitle;
        String summary;
        String year;
        String thumb;
        String art;
        String parentThumb;
        String originallyAvailableAt;
        String addedAt;
        String updatedAt;
        String loudnessAnalysisVersion;
        String Genre = "";
        String Director = "";

        ratingKey = parser.getAttributeValue(null, "ratingKey");
        key = parser.getAttributeValue(null, "key");
        parentRatingKey = parser.getAttributeValue(null, "parentRatingKey");
        guid = parser.getAttributeValue(null, "guid");
        parentGuid = parser.getAttributeValue(null, "parentGuid");
        studio = parser.getAttributeValue(null, "studio");
        type = parser.getAttributeValue(null, "type");
        title = parser.getAttributeValue(null, "title");
        parentKey = parser.getAttributeValue(null, "parentKey");
        parentTitle = parser.getAttributeValue(null, "parentTitle");
        summary = parser.getAttributeValue(null, "summary");
        year = parser.getAttributeValue(null, "year");
        thumb = parser.getAttributeValue(null, "thumb");
        art = parser.getAttributeValue(null, "art");
        parentThumb = parser.getAttributeValue(null, "parentThumb");
        originallyAvailableAt = parser.getAttributeValue(null, "originallyAvailableAt");
        addedAt = parser.getAttributeValue(null, "addedAt");
        updatedAt = parser.getAttributeValue(null, "updatedAt");
        loudnessAnalysisVersion = parser.getAttributeValue(null, "loudnessAnalysisVersion");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Genre")) {
                Genre = readGenre(parser);
            } else if (name.equals("Director")) {
                Director = readDirector(parser);
            } else {
                skip(parser);
            }
        }

        Log.d(TAG, "new album from plex: " + title);

        return new Plex_Album(ratingKey,
                key,
                parentRatingKey,
                guid,
                parentGuid,
                studio,
                type,
                title,
                parentKey,
                parentTitle,
                summary,
                year,
                thumb,
                art,
                parentThumb,
                originallyAvailableAt,
                addedAt,
                updatedAt,
                loudnessAnalysisVersion,
                Genre,
                Director);
    }

    // Processes title tags in the feed.
    private String readGenre(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Genre");
        String genre = parser.getAttributeValue(null, "tag");

        while (parser.next() != XmlPullParser.END_TAG) {

        }
        return genre;
    }

    private String readDirector(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Director");
        String director = parser.getAttributeValue(null, "tag");
        while (parser.next() != XmlPullParser.END_TAG) {

        }
        return director;
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
