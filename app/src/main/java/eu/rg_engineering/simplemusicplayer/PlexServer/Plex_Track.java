package eu.rg_engineering.simplemusicplayer.PlexServer;


import java.util.ArrayList;

public class Plex_Track {

    public final String ratingKey;
    public final String key;
    public final String parentRatingKey;
    public final String grandparentRatingKey;
    public final String guid;
    public final String parentGuid;
    public final String grandparentGuid;
    public final String parentStudio;
    public final String type;
    public final String title;
    public final String grandparentKey;
    public final String parentKey;
    public final String grandparentTitle;
    public final String parentTitle;
    public final String summary;
    public final String index;
    public final String parentIndex;
    public final String ratingCount;
    public final String parentYear;
    public final String thumb;
    public final String art;
    public final String parentThumb;
    public final String grandparentThumb;
    public final String grandparentArt;
    public final String duration;
    public final String addedAt;
    public final String updatedAt;
    public final ArrayList<Plex_TrackPart> parts;
    private static final String ns = null;

    public Plex_Track(
            String ratingKey,
            String key,
            String parentRatingKey,
            String grandparentRatingKey,
            String guid,
            String parentGuid,
            String grandparentGuid,
            String parentStudio,
            String type,
            String title,
            String grandparentKey,
            String parentKey,
            String grandparentTitle,
            String parentTitle,
            String summary,
            String index,
            String parentIndex,
            String ratingCount,
            String parentYear,
            String thumb,
            String art,
            String parentThumb,
            String grandparentThumb,
            String grandparentArt,
            String duration,
            String addedAt,
            String updatedAt,
            ArrayList<Plex_TrackPart> parts
    ) {
        this.ratingKey = ratingKey;
        this.key = key;
        this.parentRatingKey = parentRatingKey;
        this.grandparentRatingKey = grandparentRatingKey;
        this.guid = guid;
        this.parentGuid = parentGuid;
        this.grandparentGuid = grandparentGuid;
        this.parentStudio = parentStudio;
        this.type = type;
        this.title = title;
        this.grandparentKey = grandparentKey;
        this.parentKey = parentKey;
        this.grandparentTitle = grandparentTitle;
        this.parentTitle = parentTitle;
        this.summary = summary;
        this.index = index;
        this.parentIndex = parentIndex;
        this.ratingCount = ratingCount;
        this.parentYear = parentYear;
        this.thumb = thumb;
        this.art = art;
        this.parentThumb = parentThumb;
        this.grandparentThumb = grandparentThumb;
        this.grandparentArt = grandparentArt;
        this.duration = duration;
        this.addedAt = addedAt;
        this.updatedAt = updatedAt;
        this.parts = parts;
    }

}




