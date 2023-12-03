package eu.rg_engineering.simplemusicplayer.PlexServer;



import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Plex_Artists {
    public final String ratingKey;
    public final String key;
    public final String guid;
    public final String type;
    public final String title;
    public final String summary;
    public final String index;
    public final String thumb;
    public final String art;
    public final String addedAt;
    public final String updatedAt;
    public final String Genre;
    public final String Country;
    private static final String ns = null;

    public Plex_Artists(
            String ratingKey,
            String key,
            String guid,
            String type,
            String title,
            String summary,
            String index,
            String thumb,
            String art,
            String addedAt,
            String updatedAt,
            String Genre,
            String Country
    ) {
        this.ratingKey = ratingKey;
        this.key = key;
        this.guid = guid;
        this.type = type;
        this.title = title;
        this.summary = summary;
        this.index = index;
        this.thumb = thumb;
        this.art = art;
        this.addedAt = addedAt;
        this.updatedAt = updatedAt;
        this.Genre = Genre;
        this.Country = Country;
    }

}




