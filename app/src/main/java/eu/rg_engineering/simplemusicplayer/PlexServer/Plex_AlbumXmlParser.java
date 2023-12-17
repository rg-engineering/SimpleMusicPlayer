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

    /*
    <Directory
        ratingKey="47872"
        key="/library/metadata/47872/children"
        parentRatingKey="47871"
        guid="plex://album/5d07c194403c640290857f1a"
        parentGuid="plex://artist/5d07bbfc403c6402904a5ef4"
        studio="Epic"
        type="album"
        title="Ballbreaker"
        parentKey="/library/metadata/47871"
        parentTitle="AC/DC"
        summary="In over 20 years, AC/DC never changed their minimalist, bone-crunching hard rock. During their first ten years, that wasn&#39;t a problem, since they were still finding ways to expand and subvert the pattern, but ever since For Those About to Rock, they had trouble coming up with consistent material. Consequently, their performances tended to be a little lazy and their records didn&#39;t deliver a reliable knockout punch. Released in 1990, The Razor&#39;s Edge showed some signs of life, and their comeback culminated in the Rick Rubin-produced Ballbreaker. What makes Ballbreaker different than the albums AC/DC churned out during the &#39;80s is simple -- it&#39;s a matter of focus. Although &#34;Hard as a Rock&#34; comes close, there aren&#39;t any songs as immediately memorable as any of their &#39;70s classics, or even &#34;Moneytalks.&#34; However, unlike any record since Back in Black, there are no bad songs on the album. Surprisingly, Rubin&#39;s production is a bit too dry, lacking the muscle needed to make the riffs sound truly earthshaking. Nevertheless, Angus Young&#39;s riffs are powerful and catchy, showcasing every element that makes him one of hard rock and heavy metal&#39;s greatest guitarists. Throughout the album, the band sounds committed and professional, making Ballbreaker the best late-period AC/DC album to date. ~ Stephen Thomas Erlewine"
        index="1"
        rating="6.0"
        year="1995"
        thumb="/library/metadata/47872/thumb/1702006377"
        art="/library/metadata/47871/art/1702432917"
        parentThumb="/library/metadata/47871/thumb/1702432917"
        originallyAvailableAt="1995-09-22"
        addedAt="1413048370"
        updatedAt="1702006377"
        loudnessAnalysisVersion="2">
        <Genre tag="Pop/Rock" />
        <Director tag="EastWest Records America" />
    </Directory>
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
