package eu.rg_engineering.simplemusicplayer.PlexServer;

public class Plex_TrackPart {

    public final String id;
    public final String key;
    public final String duration;
    public final String file;
    public final String size;
    public final String container;

    public Plex_TrackPart(
            String id,
            String key,
            String duration,
            String file,
            String size,
            String container
    ) {
        this.id = id;
        this.key = key;
        this.duration = duration;
        this.file = file;
        this.size = size;
        this.container = container;
    }

}




