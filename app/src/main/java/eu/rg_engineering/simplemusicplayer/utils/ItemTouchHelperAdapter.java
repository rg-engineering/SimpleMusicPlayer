package eu.rg_engineering.simplemusicplayer.utils;


public interface ItemTouchHelperAdapter {

    void onItemMoved(int fromPosition, int toPosition);

    void onItemSwiped(int position);

}