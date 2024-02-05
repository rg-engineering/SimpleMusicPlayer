package eu.rg_engineering.simplemusicplayer.utils;

public interface OnPlaylistitemListener {
    void ItemDeleted(int position);
    void ItemMoved(int oldPos, int newPos);
    void PlayList2Save();
}
