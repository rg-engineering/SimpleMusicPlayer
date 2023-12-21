package eu.rg_engineering.simplemusicplayer.PlexServer;


public class Plex_Lib {

    public final String allowSync;
    public final String art;
    public final String composite;
    public final String filters;
    public final String refreshing;
    public final String thumb;
    public final String key;
    public final String type;
    public final String title;
    public final String agent;
    public final String scanner;
    public final String language;
    public final String uuid;
    public final String updatedAt;
    public final String createdAt;
    public final String scannedAt;
    public final String content;
    public final String directory;
    public final String contentChangedAt;
    public final String hidden;
    public final String location;

    private static final String ns = null;

    public Plex_Lib(
            String allowSync,
            String art,
            String composite,
            String filters,
            String refreshing,
            String thumb,
            String key,
            String type,
            String title,
            String agent,
            String scanner,
            String language,
            String uuid,
            String updatedAt,
            String createdAt,
            String scannedAt,
            String content,
            String directory,
            String contentChangedAt,
            String hidden,
            String location
    ) {
        this.allowSync = allowSync;
        this.art = art;
        this.composite = composite;
        this.filters = filters;
        this.refreshing = refreshing;
        this.thumb = thumb;
        this.key = key;
        this.type = type;
        this.title = title;
        this.agent = agent;
        this.scanner = scanner;
        this.language = language;
        this.uuid = uuid;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.scannedAt = scannedAt;
        this.content = content;
        this.directory = directory;
        this.contentChangedAt = contentChangedAt;
        this.hidden = hidden;
        this.location = location;
    }

}




