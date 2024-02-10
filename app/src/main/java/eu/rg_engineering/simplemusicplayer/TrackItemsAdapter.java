package eu.rg_engineering.simplemusicplayer;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.rg_engineering.simplemusicplayer.utils.ItemTouchHelperAdapter;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteTrackitemListener;
import io.sentry.Sentry;




public class TrackItemsAdapter extends
        RecyclerView.Adapter<TrackItemsAdapter.ViewHolder> implements
        Filterable
        //,ItemTouchHelperAdapter
        {

    private final String TAG = "TrackItemsAdapter";
    private final List<TrackItem> mItemsFiltered;
    private List<TrackItem> mItemsAll;
    private ItemTouchHelper mTouchHelper;
    private final OnDeleteTrackitemListener deleteListener;
    private int mFilterIdx = 0;
    private int currentPlayedMusicPosition = -1;
    Context mContext;
    TrackItemsAdapterListener mCommunication;
    private boolean mNeed2SendSongs=false;

    //todo playlist file einstellbar, oder mehrere supporten
    private String filename = "playlist.txt";
    private int mSortMode=-1;

    public void notifyDatasetChanged() {
        notifyDataSetChanged();
    }

    public void updateItems(ArrayList<TrackItem> mTracks) {
        mItemsAll = mTracks;
        UpdateData();
    }

    public interface TrackItemsAdapterListener {
        void messageFromTrackItemsAdapter(String msg, ArrayList<String> params, ArrayList<TrackData> tracks);
    }
    public TrackItemsAdapter(List<TrackItem> items, OnDeleteTrackitemListener deleteListener) {

        Log.d(TAG, "TrackItemsAdapter contructor ");

        this.deleteListener = deleteListener;
        mItemsFiltered = TrackItem.createItemsList(0);

        mItemsAll = items;
        UpdateData();

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(TAG, "onAttachedToRecyclerView");

        Context context = recyclerView.getContext();
        mCommunication = (TrackItemsAdapterListener) context;
        mContext = context;


        if (mCommunication != null) {
            mCommunication.messageFromTrackItemsAdapter("IsReady", null, null);
        }
        else {
            Log.w(TAG, "mCommunication == null ");
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Log.d(TAG, "onViewRecycled");
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.d(TAG, "onViewAttachedToWindow");
    }

    public void UpdateData() {

        Log.d(TAG, "UpdateData Tracks called ");
        mItemsFiltered.clear();
        for (TrackItem item : mItemsAll) {

            //todo filter beachten???
            mItemsFiltered.add(item);
        }

        //send songs here to main since plex needs longer then internal data
        if (mNeed2SendSongs) {
            GetSongs();
        }
    }
    @NonNull
    @Override
    public TrackItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View itemView = inflater.inflate(R.layout.track_item, parent, false);

        // Return a new holder instance
        TrackItemsAdapter.ViewHolder viewHolder = new TrackItemsAdapter.ViewHolder(itemView);



        Log.d(TAG, "onCreateViewHolder ");

        return viewHolder;


    }

    @Override
    public void onBindViewHolder(@NonNull TrackItemsAdapter.ViewHolder viewHolder, int position) {
// Get the data model based on position
        TrackItem item = mItemsFiltered.get(position);

        Log.d(TAG, "onBindViewHolder tracks called, position " + position);
        TextView nameTextView = viewHolder.nameTextView;

        TextView albumTextView = viewHolder.albumTextView;
        TextView artistTextView = viewHolder.artistTextView;
        TextView durationTextView = viewHolder.durationTextView;
        TextView currentPlaytimeTextView = viewHolder.currentPlaytimeTextView;

        if (nameTextView != null) {
            nameTextView.setText(item.getName());
        }
        if (albumTextView != null) {
            albumTextView.setText(item.getAlbum());
        }
        if (artistTextView != null) {
            artistTextView.setText(item.getArtist());
        }
        if (durationTextView != null) {
            durationTextView.setText(item.getDuration());
        }
        if (currentPlaytimeTextView != null) {
            currentPlaytimeTextView.setText(item.getCurrentPlaytime());
        }

        /*
        ProgressBar currentProgress = viewHolder.currentProgressbar;
        if (currentProgress != null) {
            currentProgress.setMin(0);
            currentProgress.setMax(100);
            currentProgress.setProgress(item.getProgress());
        }

         */
        SeekBar currentSeek = viewHolder.currentSeekbar;
        if (currentSeek != null) {
            currentSeek.setMin(0);
            currentSeek.setMax(100);
            currentSeek.setProgress(item.getProgress());
        }

        Button btnPlaySong = viewHolder.btnPlaySong;
        Button btnAdd2PlayList = viewHolder.btnAdd2PlayList;

        btnPlaySong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "PlayMusic pressed position " + position);
                PlayCurrentSong(position);
            }
        });

        btnAdd2PlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Add to playlist pressed position " + position);
                AddSong2Playlist(position);
            }
        });

        if (currentPlayedMusicPosition==position) {
            viewHolder.nameTextView.setTextColor(Color.RED);
        }
        else {
            viewHolder.nameTextView.setTextColor(Color.BLACK);
        }


    }
    public void SetCurrentPlaytime(int index, long playtime) {

        Log.d(TAG, "got current playtime " + index + " / " + playtime + " " + mItemsFiltered.size());

        if (index >= 0 && index < mItemsFiltered.size()) {
            mItemsFiltered.get(index).setCurrentPlaytime(playtime);

            currentPlayedMusicPosition = index;

            notifyItemChanged(index);

        } else {
            Log.e(TAG, "index out of range " + index);
        }
    }

    public void GetSongs() {
        Log.d(TAG, "GetSongs  " );

        if (mItemsFiltered.size()>0) {
            mNeed2SendSongs=false;
            ArrayList<TrackData> tracks = new ArrayList<>();

            for (TrackItem item : mItemsFiltered) {

                TrackData track = new TrackData();
                track.Artist = item.getArtist();
                track.TrackName = item.getName();
                track.Url = item.getFileName();
                //URL zum Bild
                track.Path2Image = item.getPath2ArtistImage();

                tracks.add(track);
            }

            mCommunication.messageFromTrackItemsAdapter("UpdatePlayList", null, tracks);
        }
        else {
            mNeed2SendSongs=true;
        }
    }

    private void PlayCurrentSong(int pos){

        Log.d(TAG, "PLayfromcurrent position  " + pos );

        ArrayList<String> items = new ArrayList<>();

        items.add(String.valueOf(pos));

        mCommunication.messageFromTrackItemsAdapter("PlayFromCurrentPos", items, null);
    }

    private void AddSong2Playlist(int pos){
        Log.d(TAG, "Add to playlist position  " + pos );

        TrackItem track = mItemsFiltered.get(pos);

        String data = track.Serialize(true);
        try {
            String Contents = data + "\n" ;

            Log.d(TAG, "save play list");

            writeToFile(Contents,filename);

            ArrayList<String> params = new ArrayList<>();
            params.add("to playlist added");

            mCommunication.messageFromTrackItemsAdapter("ShowInfo",params, null );

        } catch (Exception ex) {
            Log.e(TAG, "Exception in SavePlaylist " + ex);
            Sentry.captureException(ex);
        }
    }

    @Override
    public int getItemCount() {
        int cnt = 0;

        if (mItemsFiltered != null) {
            cnt = mItemsFiltered.size();
        }
        //Log.d(TAG,"items " + cnt  );
        return cnt;
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.mTouchHelper = touchHelper;
    }

    public void setFilterIdx(int idx) {
        mFilterIdx = idx;
    }

    @Override
    public Filter getFilter() {
        Log.d(TAG, "getFilter #" + mFilterIdx);

        Filter oRet = null;

        if (mFilterIdx == 1) { // track name filter
            oRet = filterTrack;
        } else {
            Log.e(TAG, "no Filter #" + mFilterIdx);
        }

        return oRet;
    }

    Filter filterTrack = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<TrackItem> filteredList = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                Log.d(TAG, "no filter set " + mItemsAll.size());
                filteredList.addAll(mItemsAll);

            } else {
                Log.d(TAG, "track filter set " + constraint + " " + mItemsAll.size());

                for (TrackItem item : mItemsAll) {

                    Log.d(TAG, "track filter compare " + item.getName().toLowerCase() + " ?= " + constraint.toString().toLowerCase());

                    if (item.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mItemsFiltered.clear();

            mItemsFiltered.addAll((Collection<? extends TrackItem>) results.values);

            notifyDataSetChanged();
        }
    };

    /*
    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        Log.d(TAG, "item moved, from " + fromPosition + " to " + toPosition);
    }

    @Override
    public void onItemSwiped(int position) {
        Log.d(TAG, "item swiped, position " + position);


    }


     */

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnTouchListener,

            GestureDetector.OnGestureListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView artistTextView;
        public TextView albumTextView;
        public TextView durationTextView;
        public TextView currentPlaytimeTextView;
        //public ProgressBar currentProgressbar;
        public SeekBar currentSeekbar;
        public Button btnPlaySong;
        public Button btnAdd2PlayList;

        GestureDetector mGestureDetector;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.track_Name);
            artistTextView = (TextView) itemView.findViewById(R.id.track_Artist);
            albumTextView = (TextView) itemView.findViewById(R.id.track_Album);
            durationTextView = (TextView) itemView.findViewById(R.id.track_Duration);
            currentPlaytimeTextView = (TextView) itemView.findViewById(R.id.track_CurrentPlaytime);
            //currentProgressbar = (ProgressBar) itemView.findViewById(R.id.track_ProgressBar);
            currentSeekbar = (SeekBar) itemView.findViewById(R.id.track_SeekBar);
            btnPlaySong = (Button) itemView.findViewById(R.id.track_PlaySong);
            btnAdd2PlayList = (Button) itemView.findViewById(R.id.track_Add2Playlist);

            mGestureDetector = new GestureDetector(itemView.getContext(), this);


            itemView.setOnTouchListener(this);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp position " + this.getAdapterPosition());

            int pos =this.getAdapterPosition();

            ArrayList<String> items = new ArrayList<>();

            items.add(String.valueOf(pos));

            mCommunication.messageFromTrackItemsAdapter("TrackSelected",items, null );

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mTouchHelper != null) {
                mTouchHelper.startDrag(this);
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            return true;
        }
    }

    public String GetSerializedData() {
        String sRet = "";

        for (int i = 0; i < mItemsAll.size(); i++) {
            sRet += mItemsAll.get(i).Serialize2();
        }
        return sRet;
    }

    public void ChangeItem(int index, TrackItem item) {
        Log.d(TAG, "change item " + item.getName());
        mItemsFiltered.set(index, item);

        int pos = FindItemInList(mItemsFiltered, item.getName());

        if (pos > -1) {
            mItemsFiltered.set(pos, item);
        }
    }

    public void AddItem(TrackItem item) {
        Log.d(TAG, "add item " + item.getName());

        mItemsAll.add(item);


        getFilter().filter("");

    }

    private int FindItemInList(List<TrackItem> list, String id) {
        int nRet = -1;

        try {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {

                    Log.d(TAG, "check item " + list.get(i).getName() + " =? " + id);

                    if (list.get(i).getName().toLowerCase().contains(id.toLowerCase())) {

                        Log.d(TAG, "item found at " + i);
                        nRet = i;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "exception in FindItemInList " + ex);
            Sentry.captureException(ex);
        }
        return nRet;
    }



    private void writeToFile(String data,String filename) {
        try {
            //File write failed: java.io.FileNotFoundException: /data/user/0/eu.rg_engineering.simplemusicplayer/files/Playlist: open failed: EISDIR (Is a directory)

            File path = mContext.getFilesDir();

            File file = new File(path, filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs(); // Will create parent directories if not exists
                file.createNewFile();
            }

            FileOutputStream writer = new FileOutputStream(file,true);
            writer.write(data.getBytes());
            writer.close();
            Log.d("TAG", "Wrote to file: " + filename);

        } catch (IOException ex) {
            Log.e("Exception", "File write failed: " + ex.toString());
            Sentry.captureException(ex);
            ArrayList<String> params = new ArrayList<>();
            params.add("exception " + ex.toString());

            mCommunication.messageFromTrackItemsAdapter("ShowInfo",params, null );
        }


    }

    public void  SetSortmode(int mode){
       /*
        0 alphabetically_descending
        1 alphabetically_ascending
        2 track_number_on_album_descending
        3 track_number_on_album_ascending
        */

        mSortMode=mode;

        switch (mSortMode){
            case 0:
                Collections.sort(mItemsFiltered, new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        TrackItem t1 = (TrackItem) o1;
                        TrackItem t2 = (TrackItem) o2;
                        return t1.getName().compareToIgnoreCase(t2.getName());
                    }
                });
                break;
            case 1:
                Collections.sort(mItemsFiltered, new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        TrackItem t1 = (TrackItem) o1;
                        TrackItem t2 = (TrackItem) o2;
                        return t2.getName().compareToIgnoreCase(t1.getName());
                    }
                });
                break;
            case 2:
                Collections.sort(mItemsFiltered, new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        TrackItem t1 = (TrackItem) o1;
                        int c1= t1.getAlbumIndex();
                        TrackItem t2 = (TrackItem) o2;
                        int c2= t2.getAlbumIndex();
                        int ret=-1;
                        if (c1>c2){
                            ret=0;
                        }
                        return ret;
                    }
                });
                break;
            case 3:
                Collections.sort(mItemsFiltered, new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        TrackItem t1 = (TrackItem) o1;
                        int c1= t1.getAlbumIndex();
                        TrackItem t2 = (TrackItem) o2;
                        int c2= t2.getAlbumIndex();
                        int ret=-1;
                        if (c1<c2){
                            ret=0;
                        }
                        return ret;
                    }
                });
                break;
            default:
                break;
        }

        notifyDataSetChanged();

        //change musicservice
        GetSongs();

    }


}
