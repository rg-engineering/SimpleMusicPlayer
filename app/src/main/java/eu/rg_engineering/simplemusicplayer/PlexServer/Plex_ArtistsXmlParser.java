package eu.rg_engineering.simplemusicplayer.PlexServer;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class Plex_ArtistsXmlParser {
    private String TAG = "Plex_ArtistsXmlParser";
    private static final String ns = null;

   /*
    <Directory
        ratingKey="46763"
        key="/library/metadata/46763/children"
        guid="plex://artist/5d07bcec403c64029056e9ec"
        type="artist"
        title="Duffy"
        summary="Emerging during the same boom of retro U.K.-based singers that launched Amy Winehouse, Duffy distinguished herself with a melodic, vintage voice that brought to mind such &#39;60s artists as Dusty Springfield and Petula Clark. Born in the small coastal town of Nefyn on the Llyn Peninsula in Gwynedd, Wales, Amy Ann Duffy began singing at an early age. With a record collection limited to her parents&#39; classic LPs, though, she had few influences to draw from. Her father&#39;s VHS tapes of the 1960s television rock show Ready Steady Go! thus became one of her biggest sources of inspiration, and Duffy started performing with various bands throughout her teenage years, sporting a charmingly retro sound that caught the ear of Rough Trade Records owner Jeannette Lee in 2004. Lee helped nurture the singer&#39;s talent by pairing her up with former Suede guitarist Bernard Butler. The duo co-wrote several songs, including the single &#34;Rockferry,&#34; which revealed Duffy&#39;s knack for soulful music inspired by Stax and Motown.&#xD;&#xA;&#34;Rockferry&#34; became the title song of Duffy&#39;s debut album, which was released on Polydor in 2008. However, it took a different song -- &#34;Mercy&#34; -- to make her an international star. &#34;Mercy&#34; topped the charts in the U.K., the first song by a Welsh female in a quarter-century to hit number one, and the follow-up single, &#34;Warwick Avenue,&#34; peaked at number three. Overseas, &#34;Mercy&#34; also scored platinum status in America. Rockferry became one of the year&#39;s most popular records, selling over two million copies in the U.K. alone, winning a Grammy for Best Pop Vocal Album, and going multi-platinum in multiple other countries. Duffy began working on her second album in 2009, taking some time off to make her acting debut in the 2010 drama Patagonia. Meanwhile, recording sessions for the album took place in England and New York, with Albert Hammond pulling a double shift as producer and co-writer. The result, Endlessly, was released in November 2010. ~ Matt Collar &amp; Andrew Leahey"
        index="1"
        thumb="/library/metadata/46763/thumb/1701376687"
        art="/library/metadata/46763/art/1701376687"
        addedAt="1261261722"
        updatedAt="1701376687">
        <Genre tag="Pop/Rock" />
        <Country tag="United Kingdom" />
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
    private Plex_Artists readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
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

        return new Plex_Artists(ratingKey,key,guid,type,title,summary,index,thumb,art,addedAt,updatedAt,Genre, Country);
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
