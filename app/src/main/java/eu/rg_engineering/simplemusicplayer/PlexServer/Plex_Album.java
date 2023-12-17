package eu.rg_engineering.simplemusicplayer.PlexServer;


public class Plex_Album {
    public final String ratingKey;
    public final String key;
    public final String parentRatingKey;
    public final String guid;
    public final String parentGuid;
    public final String studio;
    public final String type;
    public final String title;
    public final String parentKey;
    public final String parentTitle;
    public final String summary;
    public final String year;
    public final String thumb;
    public final String art;
    public final String parentThumb;
    public final String originallyAvailableAt;
    public final String addedAt;
    public final String updatedAt;
    public final String loudnessAnalysisVersion;
    public final String Genre;
    public final String Director;

    private static final String ns = null;

    public Plex_Album(
            String ratingKey,
            String key,
            String parentRatingKey,
            String guid,
            String parentGuid,
            String studio,
            String type,
            String title,
            String parentKey,
            String parentTitle,
            String summary,
            String year,
            String thumb,
            String art,
            String parentThumb,
            String originallyAvailableAt,
            String addedAt,
            String updatedAt,
            String loudnessAnalysisVersion,
            String Genre,
            String Director
    ) {
        this.ratingKey = ratingKey;
        this.key = key;
        this.parentRatingKey = parentRatingKey;
        this.guid = guid;
        this.parentGuid = parentGuid;
        this.studio = studio;
        this.type = type;
        this.title = title;
        this.parentKey = parentKey;
        this.parentTitle = parentTitle;
        this.summary = summary;
        this.year = year;
        this.thumb = thumb;
        this.art = art;
        this.parentThumb = parentThumb;
        this.originallyAvailableAt = originallyAvailableAt;
        this.addedAt = addedAt;
        this.updatedAt = updatedAt;
        this.loudnessAnalysisVersion = loudnessAnalysisVersion;
        this.Genre = Genre;
        this.Director = Director;
    }

}




