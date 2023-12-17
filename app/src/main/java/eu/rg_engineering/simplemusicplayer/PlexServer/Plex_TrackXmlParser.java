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

    /*
    <Track
        ratingKey="49913"
        key="/library/metadata/49913"
        parentRatingKey="49906"
        grandparentRatingKey="47871"
        guid="plex://track/5d07cdb1403c640290f4978d"
        parentGuid="plex://album/5d07c194403c640290857f1a"
        grandparentGuid="plex://artist/5d07bbfc403c6402904a5ef4"
        parentStudio="Epic"
        type="track"
        title="Hard as a Rock"
        grandparentKey="/library/metadata/47871"
        parentKey="/library/metadata/49906"
        grandparentTitle="AC/DC"
        parentTitle="Ballbreaker"
        summary=""
        index="1"
        parentIndex="1"
        ratingCount="228483"
        parentYear="1995"
        thumb="/library/metadata/49906/thumb/1702006391"
        art="/library/metadata/47871/art/1702432917"
        parentThumb="/library/metadata/49906/thumb/1702006391"
        grandparentThumb="/library/metadata/47871/thumb/1702432917"
        grandparentArt="/library/metadata/47871/art/1702432917"
        duration="271220"
        addedAt="1261258694"
        updatedAt="1702006385">
        <Media
            id="48573"
            duration="271220"
            bitrate="192"
            audioChannels="2"
            audioCodec="mp3"
            container="mp3">
            <Part
                id="48573"
                key="/library/parts/48573/1261258694/file.mp3"
                duration="271220"
                file="/shares/familie/Musik/AC-DC/Ballbreaker/AC-DC-Hard as a Rock.mp3"
                size="6513628"
                container="mp3" />
        </Media>
    </Track>
     */
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


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Media")) {
                String Media = readMedia(parser);
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
                null);
    }

    // Processes title tags in the feed.
    private String readMedia(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Media");
        String id = parser.getAttributeValue(null, "id");
        String duration = parser.getAttributeValue(null, "duration");
        String bitrate = parser.getAttributeValue(null, "bitrate");
        String audioChannels = parser.getAttributeValue(null, "audioChannels");
        String audioCodec = parser.getAttributeValue(null, "audioCodec");
        String container = parser.getAttributeValue(null, "container");

        String part;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Part")) {
                part = readMediaPart(parser);
            } else {
                skip(parser);
            }
        }
        return "to do";
    }

    private String readMediaPart(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Part");
        String id = parser.getAttributeValue(null, "id");
        String key = parser.getAttributeValue(null, "key");
        String duration = parser.getAttributeValue(null, "duration");
        String file = parser.getAttributeValue(null, "file");
        String size = parser.getAttributeValue(null, "size");
        String container = parser.getAttributeValue(null, "container");

        while (parser.next() != XmlPullParser.END_TAG) {

        }
        return "todo";
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
