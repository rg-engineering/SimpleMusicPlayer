package eu.rg_engineering.simplemusicplayer.PlexServer;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class Plex_TrackXmlParser {
    private String TAG = "Plex_TrackXmlParser";
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
        List tracks = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "MediaContainer");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag.
            if (name.equals("Track")) {
                tracks.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return tracks;
    }

    private Plex_Track readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Track");
        String ratingKey;
        String key;
        String parentRatingKey;
        String grandparentRatingKey;
        String guid;
        String parentGuid;
        String grandparentGuid;
        String parentStudio;
        String type;
        String title;
        String grandparentKey;
        String parentKey;
        String grandparentTitle;
        String parentTitle;
        String summary;
        String index;
        String parentIndex;
        String ratingCount;
        String parentYear;
        String thumb;
        String art;
        String parentThumb;
        String grandparentThumb;
        String grandparentArt;
        String duration;
        String addedAt;
        String updatedAt;


        ratingKey = parser.getAttributeValue(null, "ratingKey");
        key = parser.getAttributeValue(null, "key");
        parentRatingKey = parser.getAttributeValue(null, "parentRatingKey");
        grandparentRatingKey = parser.getAttributeValue(null, "grandparentRatingKey");
        guid = parser.getAttributeValue(null, "guid");
        parentGuid = parser.getAttributeValue(null, "parentGuid");
        grandparentGuid = parser.getAttributeValue(null, "grandparentGuid");
        parentStudio = parser.getAttributeValue(null, "parentStudio");
        type = parser.getAttributeValue(null, "type");
        title = parser.getAttributeValue(null, "title");
        grandparentKey = parser.getAttributeValue(null, "grandparentKey");
        parentKey = parser.getAttributeValue(null, "parentKey");
        grandparentTitle = parser.getAttributeValue(null, "grandparentTitle");
        parentTitle = parser.getAttributeValue(null, "parentTitle");
        summary = parser.getAttributeValue(null, "summary");
        index = parser.getAttributeValue(null, "index");
        parentIndex = parser.getAttributeValue(null, "parentIndex");
        ratingCount = parser.getAttributeValue(null, "ratingCount");
        parentYear = parser.getAttributeValue(null, "parentYear");
        thumb = parser.getAttributeValue(null, "thumb");
        art = parser.getAttributeValue(null, "art");
        parentThumb = parser.getAttributeValue(null, "parentThumb");
        grandparentThumb = parser.getAttributeValue(null, "grandparentThumb");
        grandparentArt = parser.getAttributeValue(null, "grandparentArt");
        duration = parser.getAttributeValue(null, "duration");
        addedAt = parser.getAttributeValue(null, "addedAt");
        updatedAt = parser.getAttributeValue(null, "updatedAt");


        ArrayList<Plex_TrackPart> Media = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Media")) {
                Media = readMedia(parser);
            } else {
                skip(parser);
            }
        }

        Log.d(TAG, "new track from plex: " + title);

        return new Plex_Track(
                ratingKey,
                key,
                parentRatingKey,
                grandparentRatingKey,
                guid,
                parentGuid,
                grandparentGuid,
                parentStudio,
                type,
                title,
                grandparentKey,
                parentKey,
                grandparentTitle,
                parentTitle,
                summary,
                index,
                parentIndex,
                ratingCount,
                parentYear,
                thumb,
                art,
                parentThumb,
                grandparentThumb,
                grandparentArt,
                duration,
                addedAt,
                updatedAt,
                Media);
    }

    // Processes title tags in the feed.
    private ArrayList<Plex_TrackPart> readMedia(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Media");
        String id = parser.getAttributeValue(null, "id");
        String duration = parser.getAttributeValue(null, "duration");
        String bitrate = parser.getAttributeValue(null, "bitrate");
        String audioChannels = parser.getAttributeValue(null, "audioChannels");
        String audioCodec = parser.getAttributeValue(null, "audioCodec");
        String container = parser.getAttributeValue(null, "container");

        ArrayList<Plex_TrackPart> parts=new ArrayList<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Part")) {
                Plex_TrackPart part = readMediaPart(parser);

                part.bitrate=bitrate;
                part.audioChannels=audioChannels;
                part.audioCodec=audioCodec;

                parts.add(part);

            } else {
                skip(parser);
            }
        }
        return parts;
    }

    private Plex_TrackPart readMediaPart(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Part");
        String id = parser.getAttributeValue(null, "id");
        String key = parser.getAttributeValue(null, "key");
        String duration = parser.getAttributeValue(null, "duration");
        String file = parser.getAttributeValue(null, "file");
        String size = parser.getAttributeValue(null, "size");
        String container = parser.getAttributeValue(null, "container");

        while (parser.next() != XmlPullParser.END_TAG) {

        }
        return new Plex_TrackPart(id,key,duration,file,size,container,"","","");
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
